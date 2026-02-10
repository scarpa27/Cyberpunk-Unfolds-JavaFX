package hr.tvz.cyberpunkunfolds.engine.event;

public record ErrorEvent(String message) implements GameEvent {
    @Override
    public boolean isError() {
        return true;
    }
}
