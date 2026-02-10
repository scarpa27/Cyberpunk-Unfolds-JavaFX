package hr.tvz.cyberpunkunfolds.engine;

import hr.tvz.cyberpunkunfolds.engine.command.GameCommand;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.engine.event.LogEvent;
import hr.tvz.cyberpunkunfolds.model.GameOutcome;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record LocalGameEngine(GameRules rules, WorldGraph world) {
    public LocalGameEngine(GameRules rules, WorldGraph world) {
        this.rules = Objects.requireNonNull(rules);
        this.world = Objects.requireNonNull(world);
    }

    public EngineResult apply(GameState state, GameCommand command) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(command);

        List<GameEvent> events = new ArrayList<>(command.applyTo(state, rules, world));

        checkGameOver(state, events);

        return new EngineResult(state, events);
    }

    private void checkGameOver(GameState state, List<GameEvent> events) {
        if (state.isGameOver()) {
            return;
        }

        if (state.alarm() >= rules.alarmMax()) {
            state.endGame(GameOutcome.LOSE_ALARM_MAX);
            events.add(new LogEvent(GameOutcome.LOSE_ALARM_MAX.message()));
        }
        else if (state.isSolved(NodeId.DATACORE)) {
            state.endGame(GameOutcome.WIN_DATACORE_SOLVED);
            events.add(new LogEvent(GameOutcome.WIN_DATACORE_SOLVED.message()));
        }
    }
}
