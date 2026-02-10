package hr.tvz.cyberpunkunfolds.engine.command;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.engine.event.ErrorEvent;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.engine.event.LogEvent;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.Puzzle;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record SolvePuzzleCommand(UUID playerId, String answer) implements GameCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    public SolvePuzzleCommand(UUID playerId, String answer) {
        this.playerId = Objects.requireNonNull(playerId);
        this.answer = Objects.requireNonNull(answer);
    }

    @Override
    public List<GameEvent> applyTo(GameState state, GameRules rules, WorldGraph world) {
        var actionCheck = state.canUseItem(playerId);
        if (actionCheck.isFailure()) {
            return List.of(new ErrorEvent(actionCheck.errorMessage().orElse("Action blocked.")));
        }

        NodeId currentLocation = state.locationOf(playerId);
        var nodeOpt = world.getNode(currentLocation);

        if (nodeOpt.isEmpty() || nodeOpt.get().puzzle().isEmpty()) {
            return List.of(new ErrorEvent("No puzzle to solve at " + currentLocation + "."));
        }

        Puzzle puzzle = nodeOpt.get().puzzle().get(); // NOSONAR called Optional#isEmpty in the block above

        if (state.isSolved(currentLocation)) {
            return List.of(new ErrorEvent("Puzzle at " + currentLocation + " is already solved."));
        }

        if (puzzle.isCorrectAnswer(answer)) {
            state.markSolved(currentLocation);
            unlockNextNodes(state, world, currentLocation);
            state.increaseAlarm(1);
            state.nextTurn();

            return List.of(new LogEvent("Correct! Puzzle solved at " + currentLocation + ". Next nodes unlocked. Alarm +1."));
        }
        else {
            state.increaseAlarm(2);
            state.nextTurn();

            return List.of(new LogEvent("Wrong answer. Alarm +2. Turn ended."));
        }
    }

    private void unlockNextNodes(GameState state, WorldGraph world, NodeId solvedNode) {
        for (NodeId neighbor : world.neighbors(solvedNode)) {
            if (!state.isUnlocked(neighbor)) {
                state.unlock(neighbor);
            }
        }
    }
}
