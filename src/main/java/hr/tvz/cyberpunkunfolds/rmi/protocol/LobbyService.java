package hr.tvz.cyberpunkunfolds.rmi.protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface LobbyService extends Remote {
    String BIND_NAME = "cyberpunk.lobby";

    List<RoomInfo> listRooms() throws RemoteException;

    RoomInfo createRoom(String hostName, int maxPlayers) throws RemoteException;

    boolean joinRoom(String roomId, String playerName) throws RemoteException;

    void leaveRoom(String roomId, String playerName) throws RemoteException;

    List<String> getRoomMembers(String roomId) throws RemoteException;

    /**
     * Start the game for a room. This locks the room and initializes the game server.
     * Only the host can start the game.
     * @param roomId The room to start
     * @param hostName The host name (must match room host)
     * @return true if the game started successfully, false if not authorized or room not found
     */
    boolean startGame(String roomId, String hostName) throws RemoteException;
}
