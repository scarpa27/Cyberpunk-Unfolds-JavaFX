package hr.tvz.cyberpunkunfolds.ui.session;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.engine.command.GameCommand;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.protocol.DtoMapper;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class LocalGameSession extends GameSession {
    public LocalGameSession(Config cfg) {
        super(cfg);
    }

    @Override
    public void startSinglePlayer(List<Player> players) {
        initSession(players);
    }

    @Override
    public void startMultiplayer(GameClient client,
                                 String roomId,
                                 List<Player> players,
                                 Player myPlayer) {
        throw new UnsupportedOperationException("Multiplayer start not supported in local session.");
    }

    @Override
    public UUID playerIdForCommand() {
        return state.currentPlayer().id();
    }

    @Override
    public void applyCommand(GameCommand cmd, Consumer<List<GameEvent>> onEvents, Runnable onStateChanged) {
        if (state == null || engine == null) {
            return;
        }
        moveLog.add(DtoMapper.toDto(cmd, ""));
        var result = engine.apply(state, cmd);
        onEvents.accept(result.events());
        onStateChanged.run();
    }
}
