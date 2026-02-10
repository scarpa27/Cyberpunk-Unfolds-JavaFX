package hr.tvz.cyberpunkunfolds.engine.command;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.engine.event.ErrorEvent;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.engine.event.LogEvent;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record EndTurnCommand(UUID playerId) implements GameCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    public EndTurnCommand(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId);
    }

    @Override
    public List<GameEvent> applyTo(GameState state, GameRules rules, WorldGraph world) {
        var actionCheck = state.canUseItem(playerId);
        if (actionCheck.isFailure()) {
            return List.of(new ErrorEvent(actionCheck.errorMessage().orElse("Action blocked.")));
        }

        String previousPlayer = state.currentPlayer().name();
        state.nextTurn();
        String nextPlayer = state.currentPlayer().name();

        return List.of(new LogEvent(previousPlayer + " ended turn. Now: " + nextPlayer + "'s turn. Interactions: " + state.interactionsRemaining()));
    }
}
