package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.ui.session.NetworkGameSession;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Objects;
import java.util.function.Consumer;

public final class GameExitHandler {
    private final NetworkGameSession session;
    private final GameChatService chatService;
    private final Consumer<String> log;

    public GameExitHandler(NetworkGameSession session, GameChatService chatService, Consumer<String> log) {
        this.session = Objects.requireNonNull(session, "session");
        this.chatService = Objects.requireNonNull(chatService, "chatService");
        this.log = Objects.requireNonNull(log, "log");
    }

    public boolean confirmResign() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        var windows = Window.getWindows();
        Window owner = windows.stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElseGet(() -> windows.stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null));
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        } else {
            alert.initModality(Modality.APPLICATION_MODAL);
        }
        alert.setTitle("Leave Game");
        alert.setHeaderText("Leave multiplayer game?");
        alert.setContentText("Leaving ends the room for everyone.");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public void resign() {
        chatService.shutdown();

        if (session.networkClient() != null) {
            try {
                session.networkClient().close();
            } catch (Exception e) {
                log.accept("Failed to close network client: " + e.getMessage());
            }
        }

        chatService.leaveRoom();
    }
}
