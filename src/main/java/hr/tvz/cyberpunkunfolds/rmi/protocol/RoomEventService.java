package hr.tvz.cyberpunkunfolds.rmi.protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RoomEventService extends Remote {
    String BIND_NAME = "cyberpunk.chat";

    void publishEvent(RoomEvent event) throws RemoteException;

    void sendMessage(String roomId, String from, String text) throws RemoteException;

    void addListener(String roomId, RoomEventListener listener) throws RemoteException;

    void removeListener(String roomId, RoomEventListener listener) throws RemoteException;
}
