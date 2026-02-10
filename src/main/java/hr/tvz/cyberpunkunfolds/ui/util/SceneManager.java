package hr.tvz.cyberpunkunfolds.ui.util;

import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;
import hr.tvz.cyberpunkunfolds.ui.controller.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class SceneManager {
    private static Stage stage;

    private SceneManager() { }

    public static void init(Stage primaryStage) {
        stage = Objects.requireNonNull(primaryStage, "stage");
    }

    public static void showMainMenu() {
        setRoot("/ui/main-menu.fxml", 640, 420);
    }

    public static void showGame() {
        var url = SceneManager.class.getResource("/ui/game.fxml");
        if (url == null) {
            throw new IllegalStateException("FXML not found on classpath: /ui/game.fxml");
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initializeSinglePlayer();
            stage.setScene(buildScene(root, 980, 620));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: /ui/game.fxml (" + url + ")", e);
        }
    }

    public static void showGameMultiplayer(GameClient client,
                                           String roomId,
                                           List<Player> players,
                                           Player myPlayer,
                                           List<String> chatHistory) {
        var url = SceneManager.class.getResource("/ui/game.fxml");
        if (url == null) {
            throw new IllegalStateException("FXML not found on classpath: /ui/game.fxml");
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initializeMultiplayer(client, roomId, players, myPlayer, chatHistory);
            stage.setScene(buildScene(root, 980, 620));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: /ui/game.fxml (" + url + ")", e);
        }
    }

    public static void showLobby() {
        setRoot("/ui/lobby.fxml", 980, 620);
    }

    public static void showReplay() {
        setRoot("/ui/replay.fxml", 900, 620);
    }

    private static void setRoot(String fxmlPath, int w, int h) {
        var url = SceneManager.class.getResource(fxmlPath);
        if (url == null) {
            throw new IllegalStateException("FXML not found on classpath: " + fxmlPath);
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            stage.setScene(buildScene(root, w, h));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: " + fxmlPath + " (" + url + ")", e);
        }
    }

    private static Scene buildScene(Parent root, int w, int h) {
        Scene scene = new Scene(root, w, h);
        var css = SceneManager.class.getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        return scene;
    }
}
