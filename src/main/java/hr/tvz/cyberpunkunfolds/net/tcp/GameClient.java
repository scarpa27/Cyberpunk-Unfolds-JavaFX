package hr.tvz.cyberpunkunfolds.net.tcp;

import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Slf4j
public final class GameClient implements Closeable {
    private final String host;
    private final int port;

    private Socket socket;
    private ObjectOutputStream out;
    private final Object sendLock = new Object();

    private volatile boolean running;
    private Consumer<CommandDto> onCommand = _ -> { };
    private final Queue<CommandDto> pending = new ConcurrentLinkedQueue<>();

    public GameClient(String host, int port) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
    }

    public void setOnCommand(Consumer<CommandDto> handler) {
        this.onCommand = Objects.requireNonNull(handler);
    }

    public void connectAsync() {
        running = true;
        Thread t = new Thread(this::connectAndLoop, "tcp-client");
        t.setDaemon(true);
        t.start();
    }

    private void connectAndLoop() {
        try (Socket sock = new Socket(host, port);
             ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream())) {
            oos.flush();
            try (ObjectInputStream ois = new ObjectInputStream(sock.getInputStream())) {
                socket = sock;
                out = oos;
                flushPending();

                while (running && !sock.isClosed()) {
                    Object obj = ois.readObject();
                    if (obj instanceof CommandDto dto) {
                        onCommand.accept(dto);
                    }
                }
            }
        } catch (ConnectException _) {
            if (running) {
                log.warn("Connection refused: {}:{}", host, port);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                log.warn("GameClient ended", e);
            }
        } finally {
            clearConnection();
        }
    }

    private void clearConnection() {
        running = false;
        socket = null;
        out = null;
    }

    public void send(CommandDto dto) {
        try {
            ObjectOutputStream currentOut = out;
            if (currentOut == null) {
                pending.add(dto);
                return;
            }
            sendInternal(currentOut, dto);
        } catch (IOException e) {
            log.warn("Failed sending command", e);
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        if (socket != null) socket.close();
    }

    private void flushPending() throws IOException {
        CommandDto dto;
        while ((dto = pending.poll()) != null) {
            sendInternal(out, dto);
        }
    }

    private void sendInternal(ObjectOutputStream target, CommandDto dto) throws IOException {
        synchronized (sendLock) {
            target.writeObject(dto);
            target.flush();
        }
    }
}
