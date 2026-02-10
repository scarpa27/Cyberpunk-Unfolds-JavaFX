package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEvent;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventListener;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;
import hr.tvz.cyberpunkunfolds.rmi.client.RoomEventListenerAdapter;
import hr.tvz.cyberpunkunfolds.ui.util.FxThread;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

abstract class BaseChatService {
    protected final TextArea chatArea;
    protected final TextField chatInput;
    protected RoomEventListener listenerImpl;
    protected RoomEventListener listenerStub;

    protected BaseChatService(TextArea chatArea, TextField chatInput) {
        this.chatArea = chatArea;
        this.chatInput = chatInput;
    }

    protected void initListener(Consumer<RoomEvent> onEvent) {
        try {
            listenerImpl = new RoomEventListenerAdapter(event -> {
                append(event.toDisplayLine());
                if (onEvent != null) {
                    onEvent.accept(event);
                }
            });
            listenerStub = (RoomEventListener) UnicastRemoteObject.exportObject(listenerImpl, 0);
        } catch (Exception e) {
            append("Failed to init chat listener: " + e.getMessage());
        }
    }

    protected boolean isListenerReady() {
        return listenerStub != null;
    }

    protected void removeListener(RoomEventService roomEvents, String roomId, String errorPrefix) {
        if (roomEvents == null || listenerStub == null || roomId == null) {
            return;
        }
        try {
            roomEvents.removeListener(roomId, listenerStub);
        } catch (Exception e) {
            append(errorPrefix + ": " + e.getMessage());
        }
    }

    protected void unexportListener(String errorPrefix) {
        try {
            if (listenerImpl != null) {
                UnicastRemoteObject.unexportObject(listenerImpl, true);
            }
        } catch (Exception e) {
            append(errorPrefix + ": " + e.getMessage());
        }
    }

    protected void setEnabled(boolean enabled) {
        FxThread.run(() -> {
            if (chatArea != null) {
                chatArea.setDisable(!enabled);
            }
            if (chatInput != null) {
                chatInput.setDisable(!enabled);
            }
        });
    }

    protected String drainInput() {
        if (chatInput == null) {
            return null;
        }
        String text = chatInput.getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        chatInput.clear();
        return text.trim();
    }

    protected void append(String message) {
        if (message == null) {
            return;
        }
        FxThread.run(() -> {
            if (chatArea != null) {
                chatArea.appendText(message + "\n");
            }
        });
    }
}
