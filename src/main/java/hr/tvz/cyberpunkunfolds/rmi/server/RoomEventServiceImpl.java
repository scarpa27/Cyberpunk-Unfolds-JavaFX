package hr.tvz.cyberpunkunfolds.rmi.server;

import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEvent;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventListener;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public final class RoomEventServiceImpl implements RoomEventService {
    private final Map<String, List<RoomEventListener>> listeners = new ConcurrentHashMap<>();

    @Override
    public void sendMessage(String roomId, String from, String text) throws RemoteException {
        publishEvent(RoomEvent.message(roomId, from, text));
    }

    @Override
    public void publishEvent(RoomEvent event) throws RemoteException {
        List<RoomEventListener> roomListeners = listeners.get(event.roomId());
        if (roomListeners == null) return;

        for (var listener : roomListeners) {
            try {
                listener.onEvent(event);
            } catch (RemoteException e) {
                log.error("Failed to notify listener. Client likely disconnected.", e);
            }
        }
    }

    @Override
    public void addListener(String roomId, RoomEventListener listener) throws RemoteException {
        log.info("Adding listener for room {}", roomId);
        listeners.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void removeListener(String roomId, RoomEventListener listener) throws RemoteException {
        log.info("Removing listener for room {}", roomId);
        List<RoomEventListener> ls = listeners.get(roomId);
        if (ls == null) return;
        ls.remove(listener);
    }
}
