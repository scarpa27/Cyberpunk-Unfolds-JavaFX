package hr.tvz.cyberpunkunfolds;

import hr.tvz.cyberpunkunfolds.ui.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public final class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        SceneManager.showMainMenu();
        stage.setTitle("Cyberpunk Unfolds");
        stage.setOnCloseRequest(_ -> stop());
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
