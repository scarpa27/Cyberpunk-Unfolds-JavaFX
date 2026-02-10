package hr.tvz.cyberpunkunfolds.model;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public final class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final String name;


    private Player(UUID id, String name) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        this.id = id;
        this.name = name;
    }

    public static Player ofName(String name) {
        // generate UUID from name so client and server have matching IDs
        return new Player(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name);
    }

    public UUID id() {return id;}

    public String name() {return name;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Player) obj;
        return Objects.equals(this.id, that.id) &&
               Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Player[" +
               "id=" + id + ", " +
               "name=" + name + ']';
    }

}
