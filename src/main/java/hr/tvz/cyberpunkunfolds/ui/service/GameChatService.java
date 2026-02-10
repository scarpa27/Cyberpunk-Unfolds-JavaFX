package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.rmi.protocol.LobbyService;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;
import hr.tvz.cyberpunkunfolds.rmi.client.RmiClientFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Objects;

public final class GameChatService extends BaseChatService {
    private final Config cfg;

    private LobbyService lobby;
    private RoomEventService roomEvents;
    private String roomId;
    private String playerName;

    public GameChatService(Config cfg, TextArea chatArea, TextField chatInput) {
        super(chatArea, chatInput);
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    public void init(String roomId, String playerName, List<String> history) {
        this.roomId = roomId;
        this.playerName = playerName;

        try {
            lobby = RmiClientFactory.lobby(cfg);
            roomEvents = RmiClientFactory.roomEvents(cfg);
        } catch (Exception e) {
            append("Chat unavailable: " + e.getMessage());
            setEnabled(false);
            return;
        }

        registerListener();

        if (history != null) {
            for (String line : history) {
                append(line);
            }
        }
    }

    public void sendFromInput() {
        if (roomEvents == null || roomId == null || playerName == null) {
            append("Chat is available in multiplayer only.");
            return;
        }

        String text = drainInput();
        if (text == null) {
            return;
        }

        try {
            roomEvents.sendMessage(roomId, playerName, text);
        } catch (Exception e) {
            append("Send failed: " + e.getMessage());
        }
    }

    public void leaveRoom() {
        if (lobby == null || roomId == null || playerName == null) {
            return;
        }
        try {
            lobby.leaveRoom(roomId, playerName);
        } catch (Exception e) {
            append("Failed to leave room: " + e.getMessage());
        }
    }

    public void shutdown() {
        removeListener(roomEvents, roomId, "Failed to remove chat listener");
        unexportListener("Failed to cleanup chat listener");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public LobbyService lobby() {
        return lobby;
    }

    private void registerListener() {
        if (roomEvents == null || roomId == null) {
            return;
        }
        if (!isListenerReady()) {
            initListener(null);
        }
        if (!isListenerReady()) {
            return;
        }
        try {
            roomEvents.addListener(roomId, listenerStub);
        } catch (Exception e) {
            append("Failed to init chat listener: " + e.getMessage());
        }
    }
}
