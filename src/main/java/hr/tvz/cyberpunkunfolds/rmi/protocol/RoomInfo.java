package hr.tvz.cyberpunkunfolds.rmi.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record RoomInfo(String roomId, String hostName, int maxPlayers, int currentPlayers, GameStatus status) implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    public RoomInfo {
        Objects.requireNonNull(roomId);
        Objects.requireNonNull(hostName);
        Objects.requireNonNull(status);
    }
}
