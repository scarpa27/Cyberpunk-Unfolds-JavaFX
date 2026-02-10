package hr.tvz.cyberpunkunfolds.ui.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public final class ControllerActions {
    private ControllerActions() { }

    public static void runAsync(String errorPrefix,
                                Runnable work,
                                Runnable onSuccess,
                                Consumer<String> onError) {
        runAsync(errorPrefix,
                () -> {
                    work.run();
                    return null;
                },
                _ -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },
                onError);
    }

    public static <T> void runAsync(String errorPrefix,
                                    Supplier<T> work,
                                    Consumer<T> onSuccess,
                                    Consumer<String> onError) {
        runAsync(errorPrefix, work, onSuccess, wrap(onError), null);
    }

    public static <T> void runAsync(String errorPrefix,
                                    Supplier<T> work,
                                    Consumer<T> onSuccess,
                                    Consumer<String> onError,
                                    Runnable onFinish) {
        runAsync(errorPrefix, work, onSuccess, wrap(onError), onFinish);
    }

    public static <T> void runAsync(String errorPrefix,
                                    Supplier<T> work,
                                    Consumer<T> onSuccess,
                                    BiConsumer<String, Throwable> onError) {
        runAsync(errorPrefix, work, onSuccess, onError, null);
    }

    public static <T> void runAsync(String errorPrefix,
                                    Supplier<T> work,
                                    Consumer<T> onSuccess,
                                    BiConsumer<String, Throwable> onError,
                                    Runnable onFinish) {
        Runnable finish = onFinish == null ? () -> { } : onFinish;
        FxAsync.run(work,
                value -> {
                    if (onSuccess != null) {
                        onSuccess.accept(value);
                    }
                },
                err -> {
                    String message = formatError(errorPrefix, err);
                    logError(message, err);
                    if (onError != null) {
                        onError.accept(message, err);
                    }
                },
                (_, _) -> finish.run());
    }

    private static String formatError(String errorPrefix, Throwable err) {
        String prefix = errorPrefix == null ? "Action failed" : errorPrefix;
        String message = (err == null || err.getMessage() == null) ? "Unknown error" : err.getMessage();
        return prefix + ": " + message;
    }

    private static BiConsumer<String, Throwable> wrap(Consumer<String> onError) {
        if (onError == null) {
            return null;
        }
        return (message, _) -> onError.accept(message);
    }

    private static void logError(String message, Throwable err) {
        if (err == null) {
            log.error(message);
            return;
        }
        log.error(message, err);
    }
}
