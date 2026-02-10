package hr.tvz.cyberpunkunfolds.net.tcp;

import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

final class ClientConnection {
    private final ObjectOutputStream out;
    private final Object lock = new Object();

    ClientConnection(ObjectOutputStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    void send(CommandDto dto) throws IOException {
        synchronized (lock) {
            out.writeObject(dto);
            out.flush();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ClientConnection other && out == other.out);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(out);
    }
}
