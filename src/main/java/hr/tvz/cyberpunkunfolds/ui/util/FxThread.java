package hr.tvz.cyberpunkunfolds.ui.util;

import javafx.application.Platform;

public final class FxThread {
    private FxThread() { }

    public static void run(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
