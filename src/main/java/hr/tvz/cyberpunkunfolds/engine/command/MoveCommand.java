package hr.tvz.cyberpunkunfolds.engine.command;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.engine.event.ErrorEvent;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.engine.event.LogEvent;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record MoveCommand(UUID playerId, NodeId to) implements GameCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    public MoveCommand(UUID playerId, NodeId to) {
        this.playerId = Objects.requireNonNull(playerId);
        this.to = Objects.requireNonNull(to);
    }

    @Override
    public List<GameEvent> applyTo(GameState state, GameRules rules, WorldGraph world) {
        var moveCheck = state.canMoveTo(playerId, to, world);
        if (moveCheck.isFailure()) {
            return List.of(new ErrorEvent(moveCheck.errorMessage().orElse("Move blocked.")));
        }

        state.moveAllPlayers(to);
        var consumeResult = state.consumeInteraction();
        if (consumeResult.isFailure()) {
            return List.of(new ErrorEvent(consumeResult.errorMessage().orElse("No interactions remaining.")));
        }

        return List.of(new LogEvent("Moved to " + to + ". Interactions: " + state.interactionsRemaining()));
    }
}
