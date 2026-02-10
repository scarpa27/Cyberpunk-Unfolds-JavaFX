package hr.tvz.cyberpunkunfolds.engine;

import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.model.GameState;

import java.util.List;
import java.util.Objects;

public record EngineResult(GameState state, List<GameEvent> events) {
    public EngineResult {
        Objects.requireNonNull(state);
        Objects.requireNonNull(events);
    }
}
