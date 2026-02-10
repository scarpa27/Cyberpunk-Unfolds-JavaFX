package hr.tvz.cyberpunkunfolds.ui.util;

@FunctionalInterface
public interface GameAsyncRunner {
    void run(Runnable work, Runnable onSuccess, String errorPrefix);
}
