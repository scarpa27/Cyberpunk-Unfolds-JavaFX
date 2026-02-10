package hr.tvz.cyberpunkunfolds.rmi.server;

import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.tcp.GameServerManager;
import hr.tvz.cyberpunkunfolds.rmi.protocol.*;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public final class LobbyServiceImpl implements LobbyService {
    private final Map<String, RoomState> rooms = new ConcurrentHashMap<>();
    private final RoomEventService roomEvents;
    private final GameServerManager gameServerManager;

    public LobbyServiceImpl(RoomEventService roomEvents, GameServerManager gameServerManager) {
        this.roomEvents = roomEvents;
        this.gameServerManager = gameServerManager;
    }

    @Override
    public List<RoomInfo> listRooms() throws RemoteException {
        return rooms.values().stream()
                .map(RoomState::toInfo)
                .sorted(Comparator.comparing(RoomInfo::roomId))
                .toList();
    }

    @Override
    public RoomInfo createRoom(String hostName, int maxPlayers) throws RemoteException {
        String id = LocalTime.now().format(DateTimeFormatter.ofPattern("mmss")) + "-room-" + UUID.randomUUID().toString().substring(0, 4);
        log.info("Created room: {}", id);
        RoomState state = new RoomState(id, hostName, maxPlayers);
        rooms.put(id, state);
        try { roomEvents.publishEvent(RoomEvent.roomCreated(id, hostName)); }
        catch (RemoteException _) { log.warn("Failed to publish room creation event"); }

        return state.toInfo();
    }

    @Override
    public boolean joinRoom(String roomId, String playerName) throws RemoteException {
        RoomState s = rooms.get(roomId);
        if (s == null) return false;

        boolean ok = s.join(playerName);
        log.info("Player {} joinRoom {} => {}", playerName, roomId, ok);

        if (ok) {
            try { roomEvents.publishEvent(RoomEvent.join(roomId, playerName)); }
            catch (Exception _) { log.warn("Failed to publish join event");}
        }
        return ok;
    }


    @Override
    public void leaveRoom(String roomId, String playerName) throws RemoteException {
        RoomState roomState = rooms.get(roomId);
        if (roomState == null) return;
        boolean wasInProgress = roomState.status == GameStatus.IN_PROGRESS;
        roomState.leave(playerName);
        log.info("Player {} left room {}", playerName, roomId);

        try { roomEvents.publishEvent(RoomEvent.leave(roomId, playerName)); }
        catch (Exception _) { log.warn("Failed to publish leave event");}

        if (wasInProgress || roomState.currentPlayers() == 0) {
            log.info("Room {} closed due to resignation by {}", roomId, playerName);
            roomState.status = GameStatus.FINISHED;

            try { roomEvents.publishEvent(RoomEvent.roomClosed(roomId, playerName)); }
            catch (Exception _) { log.warn("Failed to publish room close event");}

            rooms.remove(roomId);
            gameServerManager.closeRoom(roomId);
        }
    }

    @Override
    public List<String> getRoomMembers(String roomId) throws RemoteException {
        RoomState s = rooms.get(roomId);
        if (s == null) return List.of();
        return s.getMembers();
    }

    @Override
    public boolean startGame(String roomId, String hostName) throws RemoteException {
        RoomState s = rooms.get(roomId);
        if (s == null) {
            log.warn("startGame called for non-existent room: {}", roomId);
            return false;
        }

        if (!s.host.equals(hostName)) {
            log.warn("startGame called by non-host {} for room {}", hostName, roomId);
            return false;
        }

        if (s.status != GameStatus.WAITING) {
            log.warn("startGame called for room {} already in status {}", roomId, s.status);
            return false;
        }

        // update room status
        s.status = GameStatus.IN_PROGRESS;
        log.info("Room {} starting game with {} players", roomId, s.currentPlayers());

        // init game server for this room
        List<Player> players = s.getMembers().stream()
                                .map(Player::ofName)
                                .toList();
        gameServerManager.createRoom(roomId, players);

        // broadcast GAME_STARTED = clients auto-join
        try {
            roomEvents.publishEvent(RoomEvent.gameStarted(roomId, hostName));
        } catch (Exception e) {
            log.warn("Failed to publish game start event", e);
            return false;
        }

        return true;
    }

    private static final class RoomState {
        private final String id;
        private final String host;
        private final int maxPlayers;
        private final Set<String> players = Collections.synchronizedSet(new LinkedHashSet<>());
        private GameStatus status = GameStatus.WAITING;

        private RoomState(String id, String host, int maxPlayers) {
            this.id = id;
            this.host = host;
            this.maxPlayers = Math.max(1, maxPlayers);
            this.players.add(host);
        }

        boolean join(String name) {
            synchronized (players) {
                if (players.contains(name)) return true;
                if (players.size() >= maxPlayers) return false;
                // Prevent joining if game already started
                if (status != GameStatus.WAITING) return false;
                players.add(name);
                return true;
            }
        }

        void leave(String name) {
            synchronized (players) {
                players.remove(name);
            }
        }

        int currentPlayers() {
            synchronized (players) {
                return players.size();
            }
        }

        List<String> getMembers() {
            synchronized (players) {
                return new ArrayList<>(players);
            }
        }

        RoomInfo toInfo() {
            return new RoomInfo(id, host, maxPlayers, currentPlayers(), status);
        }
    }
}
