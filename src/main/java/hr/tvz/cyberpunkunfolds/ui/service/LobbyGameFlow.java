package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;
import hr.tvz.cyberpunkunfolds.rmi.protocol.LobbyService;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomInfo;
import hr.tvz.cyberpunkunfolds.ui.util.FxThread;
import hr.tvz.cyberpunkunfolds.ui.util.SceneManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public final class LobbyGameFlow {
    private final LobbyService lobby;
    private final LobbyChatService chatService;
    private final Config cfg;
    private final String myName;
    private final Logger log;
    private boolean inGameSession;

    public LobbyGameFlow(LobbyService lobby,
                         LobbyChatService chatService,
                         Config cfg,
                         String myName,
                         Logger log) {
        this.lobby = Objects.requireNonNull(lobby, "lobby");
        this.chatService = Objects.requireNonNull(chatService, "chatService");
        this.cfg = Objects.requireNonNull(cfg, "cfg");
        this.myName = Objects.requireNonNull(myName, "myName");
        this.log = Objects.requireNonNull(log, "log");
    }

    public void startGame(String roomId) {
        if (roomId == null) {
            chatService.append("Join or create a room first.");
            return;
        }

        try {
            List<RoomInfo> rooms = lobby.listRooms();
            RoomInfo currentRoom = rooms.stream()
                    .filter(r -> r.roomId().equals(roomId))
                    .findFirst()
                    .orElse(null);

            if (currentRoom == null) {
                chatService.append("Room not found.");
                return;
            }

            List<Player> players = loadPlayers(roomId);
            if (players.isEmpty()) {
                chatService.append("No players in room.");
                return;
            }

            boolean started = lobby.startGame(roomId, myName);
            if (!started) {
                chatService.append("Failed to start game. Are you the host?");
                return;
            }

            chatService.append("Game server initialized. Connecting...");
            connectToGame(roomId, players);
        } catch (Exception e) {
            log.error("Failed to start game", e);
            chatService.append("Start game failed: " + e.getMessage());
        }
    }

    public void joinGameSession(String roomId) {
        try {
            List<Player> players = loadPlayers(roomId);
            if (players.isEmpty()) {
                chatService.append("No players in room.");
                return;
            }
            connectToGame(roomId, players);
        } catch (Exception e) {
            log.error("Failed to join game session", e);
            chatService.append("Join game failed: " + e.getMessage());
        }
    }

    private List<Player> loadPlayers(String roomId) throws java.rmi.RemoteException {
        List<String> members = lobby.getRoomMembers(roomId);
        return members.stream().map(Player::ofName).toList();
    }

    @SuppressWarnings("java:S2095")
    // GameClient is closed in GameExitHandler.resign() via NetworkGameSession.onExit()
    private void connectToGame(String roomId, List<Player> players) {
        if (inGameSession) {
            return;
        }
        inGameSession = true;

        Player myPlayer = players.stream()
                .filter(p -> p.name().equals(myName))
                .findFirst()
                .orElse(null);

        if (myPlayer == null) {
            throw new IllegalStateException("Local player not found in room members: " + myName);
        }

        List<String> history = chatService.historyForRoom(roomId);
        chatService.shutdown();

        String host = cfg.get("tcp.host", "localhost");
        int port = cfg.getInt("tcp.port", 9999);
        GameClient client = new GameClient(host, port);

        FxThread.run(() -> SceneManager.showGameMultiplayer(client, roomId, players, myPlayer, history));
    }
}
