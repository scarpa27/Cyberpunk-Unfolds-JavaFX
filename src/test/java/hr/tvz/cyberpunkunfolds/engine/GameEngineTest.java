package hr.tvz.cyberpunkunfolds.engine;

import hr.tvz.cyberpunkunfolds.engine.command.MoveCommand;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class GameEngineTest {

    @Test
    void moveBlockedWhenLocked() {
        var world = new hr.tvz.cyberpunkunfolds.xml.config.WorldConfigReaderDom()
                .readFromClasspath("world/world.xml");
        LocalGameEngine engine = new LocalGameEngine(new GameRules(10), world);
        Player p = Player.ofName("P1");
        GameState state = new GameState(List.of(p), NodeId.SLUMS, world.initiallyUnlockedNodes());

        var res = engine.apply(state, new MoveCommand(p.id(), NodeId.GATE));
        assertFalse(res.events().isEmpty());
        assertEquals(NodeId.SLUMS, state.locationOf(p.id()));
    }
}
