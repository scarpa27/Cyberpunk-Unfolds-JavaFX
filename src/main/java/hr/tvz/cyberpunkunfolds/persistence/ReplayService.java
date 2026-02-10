package hr.tvz.cyberpunkunfolds.persistence;

import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public final class ReplayService {
    public Timeline buildTimeline(List<CommandDto> moves, Consumer<CommandDto> onMove) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < moves.size(); i++) {
            CommandDto m = moves.get(i);
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500d * i), _ -> onMove.accept(m)));
        }
        timeline.setCycleCount(1);
        return timeline;
    }
}
