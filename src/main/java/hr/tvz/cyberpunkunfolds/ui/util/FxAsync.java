package hr.tvz.cyberpunkunfolds.ui.util;

import javafx.application.Platform;

import java.util.concurrent.*;
import java.util.function.*;

public final class FxAsync {
    private static final ExecutorService EXEC =
            Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                                         r -> {
        Thread t = new Thread(r, "fx-bg");
        t.setDaemon(true); return t;
    });

    private static final Executor FX = Platform::runLater;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EXEC.shutdown();
            try {
                if (!EXEC.awaitTermination(5, TimeUnit.SECONDS)) {
                    EXEC.shutdownNow();
                }
            } catch (InterruptedException _) {
                EXEC.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }, "fx-async-shutdown"));
    }

    private FxAsync() {}

    public static <T> CompletableFuture<T> run(Supplier<T> work,
                                               Consumer<? super T> onSuccess,
                                               Consumer<? super Throwable> onError,
                                               BiConsumer<? super T, ? super Throwable> onFinish) {
        return CompletableFuture.supplyAsync(work, EXEC)
                                .whenCompleteAsync((value, err) -> {
                                    Throwable unwrapped = unwrap(err);
                                    try {
                                        if (unwrapped == null) onSuccess.accept(value);
                                        else onError.accept(unwrapped);
                                    } finally {
                                        onFinish.accept(value, unwrapped);
                                    }}, FX);
    }

    public static <T> CompletableFuture<T> run(Supplier<T> work,
                                               Consumer<? super T> onSuccess,
                                               Consumer<? super Throwable> onError) {
        return CompletableFuture.supplyAsync(work, EXEC)
                                .whenCompleteAsync((value, err) -> {
                                    Throwable unwrapped = unwrap(err);
                                    if (unwrapped == null) onSuccess.accept(value);
                                    else onError.accept(unwrapped);
                                    }, FX);
    }

    public static <T> CompletableFuture<T> run(Supplier<T> work,
                                               BiConsumer<? super T, ? super Throwable> onFinish) {
        return CompletableFuture.supplyAsync(work, EXEC)
                                .whenCompleteAsync((value, err) -> {
                                    Throwable unwrapped = unwrap(err);
                                    onFinish.accept(value, unwrapped);
                                    }, FX);
    }

    private static Throwable unwrap(Throwable t) {
        if (t == null) return null;
        if (t instanceof CompletionException ce && ce.getCause() != null) return ce.getCause();
        return t;
    }
}
