package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.engine.command.EndTurnCommand;
import hr.tvz.cyberpunkunfolds.engine.command.GameCommand;
import hr.tvz.cyberpunkunfolds.engine.command.InspectCommand;
import hr.tvz.cyberpunkunfolds.engine.command.MoveCommand;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.ui.session.GameSession;
import hr.tvz.cyberpunkunfolds.ui.util.FxThread;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class GameCommandHandler {
    private final GameSession session;
    private final Runnable onStateChanged;
    private final Consumer<String> append;

    public GameCommandHandler(GameSession session, Runnable onStateChanged, Consumer<String> append) {
        this.session = Objects.requireNonNull(session, "session");
        this.onStateChanged = Objects.requireNonNull(onStateChanged, "onStateChanged");
        this.append = Objects.requireNonNull(append, "append");
    }

    public void onMove(String selectedNode) {
        if (selectedNode == null) {
            append.accept("Select a node from the map first.");
            return;
        }

        String nodeIdStr = selectedNode.split(" ")[0];
        NodeId to = NodeId.valueOf(nodeIdStr);
        apply(new MoveCommand(playerIdForCommand(), to));
    }

    public void onInspect() {
        apply(new InspectCommand(playerIdForCommand()));
    }

    public void onEndTurn() {
        apply(new EndTurnCommand(playerIdForCommand()));
    }

    public void onSubmitPuzzle(String answer, Runnable clearInput) {
        var cmdOpt = session.buildSolveCommand(answer, append);
        if (cmdOpt.isEmpty()) {
            return;
        }
        if (clearInput != null) {
            clearInput.run();
        }
        apply(cmdOpt.get());
    }

    public void onRemoteCommand(CommandDto dto) {
        FxThread.run(() -> session.handleRemoteCommand(dto, this::appendEvents, onStateChanged, append));
    }

    private void apply(GameCommand cmd) {
        session.applyCommand(cmd, this::appendEvents, onStateChanged);
    }

    private void appendEvents(List<GameEvent> events) {
        for (GameEvent e : events) {
            append.accept(e.message());
        }
    }

    private UUID playerIdForCommand() {
        return session.playerIdForCommand();
    }
}
