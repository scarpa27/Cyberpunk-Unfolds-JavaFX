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

public record InspectCommand(UUID playerId) implements GameCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    public InspectCommand(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId);
    }

    @Override
    public List<GameEvent> applyTo(GameState state, GameRules rules, WorldGraph world) {
        var interactCheck = state.canInteract(playerId);
        if (interactCheck.isFailure()) {
            return List.of(new ErrorEvent(interactCheck.errorMessage().orElse("Action blocked.")));
        }

        NodeId currentLocation = state.locationOf(playerId);
        var nodeOpt = world.getNode(currentLocation);

        if (nodeOpt.isEmpty() || nodeOpt.get().puzzle().isEmpty()) {
            return List.of(new ErrorEvent("Nothing to inspect here. No puzzle at " + currentLocation + "."));
        }

        Puzzle puzzle = nodeOpt.get().puzzle().get(); // NOSONAR called Optional#isEmpty in the block above

        if (state.isSolved(currentLocation)) {
            return List.of(new ErrorEvent("Puzzle already solved at " + currentLocation + "."));
        }

        if (!puzzle.hasMoreClues()) {
            return List.of(new ErrorEvent("All clues already revealed. Try solving the puzzle."));
        }

        puzzle.revealNextClue();
        state.increaseAlarm(1);
        var consumeResult = state.consumeInteraction();
        if (consumeResult.isFailure()) {
            return List.of(new ErrorEvent(consumeResult.errorMessage().orElse("No interactions remaining.")));
        }

        List<String> revealed = puzzle.revealedClues();
        String latestClue = revealed.getLast();

        return List.of(new LogEvent("Clue [" + revealed.size() + "/" + puzzle.totalClueCount() + "]: " + latestClue + " | Alarm +1. Interactions: " + state.interactionsRemaining()));
    }
}
