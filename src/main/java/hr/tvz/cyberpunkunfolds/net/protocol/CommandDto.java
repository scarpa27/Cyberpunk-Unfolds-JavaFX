package hr.tvz.cyberpunkunfolds.net.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * network representation of a player action.
 * sent over TCP sockets and stored into XML replay logs.
 */
public record CommandDto(String type, String playerId, String payload, long epochMillis,
                         String roomId) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public CommandDto(String type, String playerId, String payload, long epochMillis, String roomId) {
        this.type = Objects.requireNonNull(type);
        this.playerId = Objects.requireNonNull(playerId);
        this.payload = payload == null ? "" : payload;
        this.epochMillis = epochMillis;
        this.roomId = roomId == null ? "" : roomId;
    }

    public static CommandDto error(CommandDto source, String message) {
        Objects.requireNonNull(source, "source");
        return new CommandDto(
                "ERROR",
                source.playerId(),
                message,
                System.currentTimeMillis(),
                source.roomId()
        );
    }
}
