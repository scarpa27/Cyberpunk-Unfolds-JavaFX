package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.ui.session.GameSession;
import hr.tvz.cyberpunkunfolds.ui.util.GameAsyncRunner;

import java.util.Objects;
import java.util.function.Consumer;

public final class GameFileOperations {
    private final GameSession session;
    private final Runnable refreshUi;
    private final Consumer<String> append;
    private final GameAsyncRunner asyncRunner;

    public GameFileOperations(GameSession session,
                              Runnable refreshUi,
                              Consumer<String> append,
                              GameAsyncRunner asyncRunner) {
        this.session = Objects.requireNonNull(session, "session");
        this.refreshUi = Objects.requireNonNull(refreshUi, "refreshUi");
        this.append = Objects.requireNonNull(append, "append");
        this.asyncRunner = Objects.requireNonNull(asyncRunner, "asyncRunner");
    }

    public void save() {
        asyncRunner.run(session::save,
                () -> append.accept("Game saved (serialization)."),
                "Save failed");
    }

    public void load() {
        asyncRunner.run(session::load,
                () -> {
                    append.accept("Game loaded (serialization).");
                    refreshUi.run();
                },
                "Load failed");
    }

    public void exportMoves() {
        asyncRunner.run(session::exportMoves,
                () -> append.accept("Exported moves to XML: ~/.cyberpunk-unfolds/xml/moves.xml"),
                "Export failed");
    }
}
