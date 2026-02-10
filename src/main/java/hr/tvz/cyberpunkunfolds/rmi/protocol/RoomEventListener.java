package hr.tvz.cyberpunkunfolds.rmi.protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI listener for lobby/chat events
 */
public interface RoomEventListener extends Remote {
    void onEvent(RoomEvent event) throws RemoteException;
}
