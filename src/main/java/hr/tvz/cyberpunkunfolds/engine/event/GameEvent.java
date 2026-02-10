package hr.tvz.cyberpunkunfolds.engine.event;

import java.io.Serializable;

public sealed interface GameEvent extends Serializable permits LogEvent, ErrorEvent {
    String message();

    default boolean isError() {
        return false;
    }
}
