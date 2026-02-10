package hr.tvz.cyberpunkunfolds.ui.controller;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.rmi.client.RmiClientFactory;
import hr.tvz.cyberpunkunfolds.rmi.protocol.LobbyService;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventType;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomInfo;
import hr.tvz.cyberpunkunfolds.ui.service.LobbyChatService;
import hr.tvz.cyberpunkunfolds.ui.service.LobbyGameFlow;
import hr.tvz.cyberpunkunfolds.ui.util.ControllerActions;
import hr.tvz.cyberpunkunfolds.ui.util.FxThread;
import hr.tvz.cyberpunkunfolds.ui.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.UncheckedIOException;
import java.rmi.RemoteException;

@Slf4j
public final class LobbyController {
    private final Config cfg = Config.load();
    private final String myName = "Toni-" + java.util.UUID.randomUUID().toString().substring(0, 4);
    @FXML private ListView<RoomInfo> roomsList;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    private LobbyService lobby;
    private LobbyChatService chatService;
    private LobbyGameFlow gameFlow;
    private LobbyRoomSession roomSession;

    private static String formatRoom(RoomInfo room) {
        return room.roomId() + " (host=" + room.hostName() + ", " + room.currentPlayers() + "/" + room.maxPlayers() + ", " + room.status() + ")";
    }

    @FXML
    public void initialize() {
        lobby = RmiClientFactory.lobby(cfg);
        RoomEventService roomEvents = RmiClientFactory.roomEvents(cfg);
        chatService = new LobbyChatService(roomEvents, chatArea, chatInput);
        gameFlow = new LobbyGameFlow(lobby, chatService, cfg, myName, log);
        roomSession = new LobbyRoomSession(lobby, chatService, myName);
        roomsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(RoomInfo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatRoom(item));
            }
        });
        chatService.init(event -> FxThread.run(() -> {
            if (event.type() == RoomEventType.GAME_STARTED && event.roomId().equals(roomSession.currentRoomId())) {
                log.info("GAME_STARTED event received for current room, auto-joining game");
                joinGameSession();
            }
        }));
        onRefresh();
    }

    @FXML
    public void onBack() {
        roomSession.cleanup();
        SceneManager.showMainMenu();
    }

    @FXML
    public void onRefresh() {
        ControllerActions.runAsync(
                "Lobby refresh failed",
                () -> {
                    try {
                        return lobby.listRooms();
                    }
                    catch (RemoteException e) {
                        throw new UncheckedIOException("Failed to list rooms", e);
                    }
                },
                rooms -> {
                    if (rooms != null) {
                        roomsList.getItems().setAll(rooms.stream()
                                                         .sorted(java.util.Comparator.comparing(RoomInfo::roomId))
                                                         .toList());
                    }
                },
                (msg, err) -> {
                    log.warn("Failed to refresh rooms list", err);
                    chatService.append(msg);
                });
    }

    @FXML
    public void onCreateRoom() {
        ControllerActions.runAsync(
                "Create room failed",
                () -> {
                    RoomInfo room;
                    try {
                        room = lobby.createRoom(myName, 4);
                    }
                    catch (RemoteException e) {
                        throw new UncheckedIOException("Failed to create room", e);
                    }
                    return roomSession.switchRoom(room.roomId());
                },
                ok -> {
                    boolean created = Boolean.TRUE.equals(ok);
                    if (created) {
                        chatService.append("Created room: " + roomSession.currentRoomId());
                        onRefresh();
                    }
                    else {
                        chatService.append("Create room failed.");
                    }
                },
                chatService::append);
    }

    @FXML
    public void onJoinRoom() {
        RoomInfo selected = roomsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            chatService.append("Select a room first.");
            return;
        }
        String roomId = selected.roomId();
        ControllerActions.runAsync(
                "Join failed",
                () -> roomSession.switchRoom(roomId),
                ok -> {
                    boolean joined = Boolean.TRUE.equals(ok);
                    if (joined) {
                        chatService.append("Joined room: " + roomSession.currentRoomId());
                    }
                    else {
                        chatService.append("Room is full or missing.");
                    }
                },
                chatService::append);
    }

    @FXML
    public void onSendChat() {
        String roomId = roomSession.currentRoomId();
        if (roomId == null) {
            chatService.append("Join or create a room first.");
            return;
        }
        chatService.sendMessage(roomId, myName);
    }

    @FXML
    public void onStartGame() {
        ControllerActions.runAsync(
                "Start game failed",
                () -> gameFlow.startGame(roomSession.currentRoomId()),
                null,
                chatService::append);
    }

    // join the game session - called by host by button or non-hosts by GAME_STARTED event
    private void joinGameSession() {
        String roomId = roomSession.currentRoomId();
        ControllerActions.runAsync(
                "Join game failed",
                () -> gameFlow.joinGameSession(roomId),
                null,
                chatService::append);
    }
}
