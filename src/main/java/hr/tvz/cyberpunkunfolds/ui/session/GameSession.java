package hr.tvz.cyberpunkunfolds.ui.session;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.engine.LocalGameEngine;
import hr.tvz.cyberpunkunfolds.engine.command.GameCommand;
import hr.tvz.cyberpunkunfolds.engine.command.SolvePuzzleCommand;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.model.world.Node;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.Puzzle;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;
import hr.tvz.cyberpunkunfolds.persistence.SaveGameService;
import hr.tvz.cyberpunkunfolds.ui.service.GameExitHandler;
import hr.tvz.cyberpunkunfolds.xml.config.WorldConfigReaderDom;
import hr.tvz.cyberpunkunfolds.xml.moves.MoveLogWriterDom;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class GameSession {
    protected final Config cfg;
    private final SaveGameService saveService = new SaveGameService();
    private final MoveLogWriterDom moveLogWriter = new MoveLogWriterDom();
    protected final List<CommandDto> moveLog = new ArrayList<>();

    protected WorldGraph world;
    protected GameState state;
    protected LocalGameEngine engine;

    protected GameSession(Config cfg) {
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    public abstract void startSinglePlayer(List<Player> players);

    public abstract void startMultiplayer(GameClient client,
                                          String roomId,
                                          List<Player> players,
                                          Player myPlayer);

    public abstract UUID playerIdForCommand();

    public abstract void applyCommand(GameCommand cmd, Consumer<List<GameEvent>> onEvents, Runnable onStateChanged);

    public void handleRemoteCommand(CommandDto dto,
                                    Consumer<List<GameEvent>> onEvents,
                                    Runnable onStateChanged,
                                    Consumer<String> onError) {
        // no-op for local sessions
    }

    public boolean confirmExit(GameExitHandler exitHandler) {
        return exitHandler == null || exitHandler.confirmResign();
    }

    public void onExit(GameExitHandler exitHandler) {
        if (exitHandler != null) {
            exitHandler.resign();
        }
    }

    public Optional<GameCommand> buildSolveCommand(String answer, Consumer<String> onError) {
        UUID playerId = playerIdForCommand();
        NodeId location = state.locationOf(playerId);
        Optional<Puzzle> puzzleOpt = nodeForPlayer(location);

        if (puzzleOpt.isEmpty()) {
            onError.accept("No puzzle at current location.");
            return Optional.empty();
        }

        if (state.isSolved(location)) {
            onError.accept("Puzzle already solved here.");
            return Optional.empty();
        }

        if (answer == null || answer.isBlank()) {
            onError.accept("Enter an answer first.");
            return Optional.empty();
        }

        return Optional.of(new SolvePuzzleCommand(playerId, answer.trim()));
    }

    public void save() {
        saveService.save(state, "gameSave.dat");
    }

    public void load() {
        state = saveService.load("gameSave.dat");
    }

    public void exportMoves() {
        moveLogWriter.write(moveLog, "moves.xml");
    }

    public GameState state() {
        return state;
    }

    public WorldGraph world() {
        return world;
    }

    public LocalGameEngine engine() {
        return engine;
    }

    protected void initSession(List<Player> players) {
        moveLog.clear();
        world = new WorldConfigReaderDom().readFromClasspath(cfg.get("world.config", "world/world.xml"));
        engine = new LocalGameEngine(new GameRules(cfg.getInt("game.alarm.max", 19)), world);
        state = new GameState(players, NodeId.SLUMS, world.initiallyUnlockedNodes());
    }

    protected Optional<Puzzle> nodeForPlayer(NodeId location) {
        return world.getNode(location).flatMap(Node::puzzle);
    }
}
