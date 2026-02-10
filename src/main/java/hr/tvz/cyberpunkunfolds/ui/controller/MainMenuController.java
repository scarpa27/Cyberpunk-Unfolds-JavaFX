package hr.tvz.cyberpunkunfolds.ui.controller;

import hr.tvz.cyberpunkunfolds.docs.DocGenerator;
import hr.tvz.cyberpunkunfolds.ui.util.ControllerActions;
import hr.tvz.cyberpunkunfolds.ui.util.SceneManager;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;

import java.awt.Desktop;
import java.nio.file.Path;

public final class MainMenuController {
    @FXML private Button generateDocsButton;
    private final BooleanProperty generatingDocs = new SimpleBooleanProperty(false);
    @FXML private Hyperlink statusLabel;
    private Path docsPath;

    @FXML
    public void initialize() {
        generateDocsButton.disableProperty().bind(generatingDocs);
        generatingDocs.addListener((_, _, is) -> {
            if (Boolean.TRUE.equals(is)) {
                var pi = new ProgressIndicator(-1);
                pi.setPrefSize(16, 16);
                pi.getStyleClass().add("docs-spinner");
                generateDocsButton.setGraphic(pi);
                generateDocsButton.setText("Generating...");
            } else {
                generateDocsButton.setGraphic(null);
                generateDocsButton.setText("Generate Docs (Reflection)");
            }
        });
        statusLabel.setDisable(true);
        statusLabel.setOnAction(_ -> openDocs());
    }

    @FXML
    public void onSinglePlayer() {
        SceneManager.showGame();
    }

    @FXML
    public void onLobby() {
        SceneManager.showLobby();
    }

    @FXML
    public void onReplay() {
        SceneManager.showReplay();
    }

    @FXML
    public void onGenerateDocs() {
        generatingDocs.set(true);
        setStatus("Generating docs...");

        ControllerActions.runAsync(
                "Error generating docs",
                () -> new DocGenerator().generateHtml("hr.tvz.cyberpunkunfolds"),
                path -> setDocsLink(path, "Docs generated into %s".formatted(path.toString())),
                this::setStatus,
                () -> generatingDocs.set(false));
    }

    @FXML
    public void onExit() {
        Platform.exit();
    }

    private void setStatus(String message) {
        docsPath = null;
        statusLabel.setDisable(true);
        statusLabel.setText(message);
    }

    private void setDocsLink(Path path, String message) {
        docsPath = path;
        statusLabel.setDisable(false);
        statusLabel.setText(message);
    }

    private void openDocs() {
        if (docsPath == null) {
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            setStatus("Opening docs not supported on this system.");
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(docsPath.toFile());
                return;
            }
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(docsPath.toUri());
                return;
            }
            setStatus("Opening docs not supported on this system.");
        } catch (Exception e) {
            setStatus("Failed to open docs: " + e.getMessage());
        }
    }
}
