package hr.tvz.cyberpunkunfolds.engine.command;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public sealed interface GameCommand extends Serializable
        permits MoveCommand, InspectCommand, SolvePuzzleCommand, EndTurnCommand {

    List<GameEvent> applyTo(GameState state, GameRules rules, WorldGraph world);

    UUID playerId();
}
