package hr.tvz.cyberpunkunfolds.net.sync;

import hr.tvz.cyberpunkunfolds.model.GameState;

import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class GameStateStore {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private GameState state;

    public GameStateStore(GameState initial) {
        this.state = Objects.requireNonNull(initial);
    }

    public <T> T withWriteLock(Function<GameState, T> action) {
        lock.writeLock().lock();
        try {
            return action.apply(state);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void set(GameState newState) {
        lock.writeLock().lock();
        try {
            this.state = Objects.requireNonNull(newState);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
