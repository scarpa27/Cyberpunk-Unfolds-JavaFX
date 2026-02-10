package hr.tvz.cyberpunkunfolds.net.tcp;

import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

@Slf4j
final class GameServerCommandIO {
    private GameServerCommandIO() {}

    static ReadResult readCommand(ObjectInputStream in, Socket socket) throws IOException {
        try {
            Object obj = in.readObject();
            if (obj instanceof CommandDto dto) {
                return new ReadResult(dto, true);
            }
            log.warn("Ignoring unknown payload from client {}", socket.getRemoteSocketAddress());
            return new ReadResult(null, true);
        }
        catch (EOFException _) {
            log.debug("Client disconnected: {}", socket.getRemoteSocketAddress());
            return new ReadResult(null, false);
        }
        catch (ClassNotFoundException e) {
            log.warn("Unknown object from client {}", socket.getRemoteSocketAddress(), e);
            return new ReadResult(null, false);
        }
    }

    record ReadResult(CommandDto dto, boolean connected) {}
}
