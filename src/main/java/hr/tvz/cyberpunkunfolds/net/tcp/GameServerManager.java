package hr.tvz.cyberpunkunfolds.net.tcp;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.engine.LocalGameEngine;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.net.sync.GameStateStore;
import hr.tvz.cyberpunkunfolds.xml.config.WorldConfigReaderDom;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-room game contexts
 * Rooms share a single TCP port. Routing is based on roomId in CommandDto.
 */
@Slf4j
public final class GameServerManager implements Closeable {
    private final int port;
    private final String worldConfigPath;
    private final GameRules rulesTemplate;
    private final WorldConfigReaderDom worldReader = new WorldConfigReaderDom();
    private final Map<String, RoomGameContext> rooms = new ConcurrentHashMap<>();

    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public GameServerManager(int port, String worldConfigPath, GameRules rulesTemplate) {
        this.port = port;
        this.worldConfigPath = worldConfigPath;
        this.rulesTemplate = rulesTemplate;
    }

    public void startAsync() {
        Thread acceptThread = new Thread(this::acceptLoop, "tcp-server-manager");
        acceptThread.setDaemon(true);
        acceptThread.start();
        log.info("GameServerManager listening on port {}", port);
    }

    private void acceptLoop() {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            while (running) {
                Socket clientSocket = ss.accept();
                handleClient(clientSocket);
            }
        }
        catch (IOException e) {
            if (running) {
                log.error("TCP server manager failed", e);
            }
        }
    }

    private void handleClient(Socket socket) {
        Thread clientThread = new Thread(() -> runClient(socket), "tcp-client-handler");
        clientThread.setDaemon(true);
        clientThread.start();
    }

    private void runClient(Socket socket) {
        try (socket) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            try (out; ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                ClientConnection client = new ClientConnection(out);
                String assignedRoomId = null;
                boolean connected = true;

                while (running && connected && !socket.isClosed()) {
                    GameServerCommandIO.ReadResult result = GameServerCommandIO.readCommand(in, socket);
                    connected = result.connected();

                    CommandDto dto = result.dto();
                    if (dto != null) {
                        assignedRoomId = handleCommand(socket, client, dto, assignedRoomId);
                    }
                }
            }
        }
        catch (IOException e) {
            if (running) {
                log.debug("Client disconnected", e);
            }
        }
    }

    private String handleCommand(Socket socket, ClientConnection client, CommandDto dto, String assignedRoomId) {
        String roomId = dto.roomId();

        if (roomId.isBlank()) {
            sendError(client, dto, "Missing room id.");
            return assignedRoomId;
        }

        if (assignedRoomId == null) {
            assignedRoomId = roomId;
            log.info("Client {} assigned to room {}", socket.getRemoteSocketAddress(), roomId);
        }
        else if (!assignedRoomId.equals(roomId)) {
            sendError(client, dto, "Room mismatch for this connection.");
            return assignedRoomId;
        }

        RoomGameContext room = rooms.get(roomId);
        if (room == null) {
            sendError(client, dto, "Room not found: " + roomId);
            return assignedRoomId;
        }

        room.processCommand(dto, client);
        return assignedRoomId;
    }

    // create a new game room with the given players.
    public void createRoom(String roomId, List<Player> players) {
        if (rooms.containsKey(roomId)) {
            log.warn("Room already exists: {}", roomId);
            return;
        }

        if (players == null || players.isEmpty()) {
            log.warn("Cannot create room {} with no players", roomId);
            return;
        }

        WorldGraph worldTemplate = readWorldTemplate(roomId);
        if (worldTemplate == null) {
            return;
        }

        GameState initialState = new GameState(players, NodeId.SLUMS, worldTemplate.initiallyUnlockedNodes());
        GameStateStore store = new GameStateStore(initialState);
        LocalGameEngine engine = new LocalGameEngine(rulesTemplate, worldTemplate);

        RoomGameContext context = new RoomGameContext(store, engine);
        rooms.put(roomId, context);

        log.info("Created game room: {} with {} players", roomId, players.size());
    }

    // remove a room when the game ends.
    public void closeRoom(String roomId) {
        RoomGameContext removed = rooms.remove(roomId);
        if (removed != null) {
            log.info("Closed game room: {}", roomId);
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        rooms.clear();
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private void sendError(ClientConnection client, CommandDto dto, String message) {
        CommandDto errorDto = CommandDto.error(dto, message);
        try {
            client.send(errorDto);
        }
        catch (IOException e) {
            log.warn("Failed to send error to client", e);
        }
    }

    private WorldGraph readWorldTemplate(String roomId) {
        try {
            return worldReader.readFromClasspath(worldConfigPath);
        }
        catch (RuntimeException e) {
            log.error("Failed to load world for room {}: {}", roomId, e.getMessage(), e);
            return null;
        }
    }
}
