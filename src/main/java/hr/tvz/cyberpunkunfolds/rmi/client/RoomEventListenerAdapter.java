package hr.tvz.cyberpunkunfolds.rmi.client;

import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEvent;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventListener;

import java.rmi.RemoteException;
import java.util.Objects;
import java.util.function.Consumer;

public final class RoomEventListenerAdapter implements RoomEventListener {
    private final Consumer<RoomEvent> handler;

    public RoomEventListenerAdapter(Consumer<RoomEvent> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public void onEvent(RoomEvent event) throws RemoteException {
        handler.accept(event);
    }
}
