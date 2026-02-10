package hr.tvz.cyberpunkunfolds.ui.controller;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.persistence.ReplayService;
import hr.tvz.cyberpunkunfolds.ui.util.FxThread;
import hr.tvz.cyberpunkunfolds.ui.util.SceneManager;
import hr.tvz.cyberpunkunfolds.xml.moves.MoveLogReaderSax;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;

import java.nio.file.Path;
import java.util.List;

public final class ReplayController {
    @FXML private Slider progressSlider;
    @FXML private TextArea replayArea;

    private final Config cfg = Config.load();
    private final MoveLogReaderSax reader = new MoveLogReaderSax();
    private final ReplayService replayService = new ReplayService();

    private List<CommandDto> moves = List.of();
    private Timeline timeline;

    @FXML
    public void onBack() {
        if (timeline != null) timeline.stop();
        SceneManager.showMainMenu();
    }

    @FXML
    public void onLoadXml() {
        Path p = cfg.xmlDir().resolve("moves.xml");
        moves = reader.read(p);
        FxThread.run(() -> {
            replayArea.setText("Loaded " + moves.size() + " moves from " + p + "\n");
            progressSlider.setValue(0);
        });
    }

    @FXML
    public void onPlay() {
        if (moves.isEmpty()) {
            FxThread.run(() -> replayArea.appendText("No moves loaded.\n"));
            return;
        }
        if (timeline != null) timeline.stop();

        timeline = replayService.buildTimeline(moves, m -> FxThread.run(() ->
                replayArea.appendText("Replay: " + m.type() + " player=" + m.playerId() + " payload=" + m.payload() + "\n")
        ));
        timeline.playFromStart();
    }

    @FXML
    public void onPause() {
        if (timeline != null) timeline.pause();
    }
}
