package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEvent;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class LobbyChatService extends BaseChatService {
    private final RoomEventService roomEvents;
    private final List<String> history = Collections.synchronizedList(new ArrayList<>());

    private String currentRoomId;

    public LobbyChatService(RoomEventService roomEvents, TextArea chatArea, TextField chatInput) {
        super(chatArea, chatInput);
        this.roomEvents = Objects.requireNonNull(roomEvents, "roomEvents");
    }

    public void init(Consumer<RoomEvent> onEvent) {
        initListener(onEvent);
    }

    public boolean hasListener() {
        return isListenerReady();
    }

    public void addListener(String roomId) throws RemoteException {
        roomEvents.addListener(roomId, listenerStub);
        currentRoomId = roomId;
    }

    public void removeListener(String roomId) throws RemoteException {
        roomEvents.removeListener(roomId, listenerStub);
        if (roomId != null && roomId.equals(currentRoomId)) {
            currentRoomId = null;
        }
    }

    public void shutdown() {
        removeListener(roomEvents, currentRoomId, "Failed to remove chat listener");
        unexportListener("Failed to unexport chat listener");
    }

    public void sendMessage(String roomId, String from) {
        String text = drainInput();
        if (text == null) {
            return;
        }

        try {
            roomEvents.sendMessage(roomId, from, text);
        } catch (Exception e) {
            append("Send failed: " + e.getMessage());
        }
    }

    @Override
    public void append(String message) {
        if (message == null) {
            return;
        }
        history.add(message);
        super.append(message);
    }

    public List<String> historyForRoom(String roomId) {
        if (roomId == null) {
            return List.of();
        }
        String prefix = "[" + roomId + "]";
        synchronized (history) {
            return history.stream()
                    .filter(line -> line.startsWith(prefix))
                    .toList();
        }
    }
}
