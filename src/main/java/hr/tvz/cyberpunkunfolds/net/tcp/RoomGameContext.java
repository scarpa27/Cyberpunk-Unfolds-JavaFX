package hr.tvz.cyberpunkunfolds.net.tcp;

import hr.tvz.cyberpunkunfolds.engine.EngineResult;
import hr.tvz.cyberpunkunfolds.engine.LocalGameEngine;
import hr.tvz.cyberpunkunfolds.engine.command.GameCommand;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.net.protocol.DtoMapper;
import hr.tvz.cyberpunkunfolds.net.sync.GameStateStore;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
final class RoomGameContext {
    private final GameStateStore store;
    private final LocalGameEngine engine;
    private final List<ClientConnection> clients = new CopyOnWriteArrayList<>();
    private final List<CommandDto> history = new ArrayList<>();
    private final Object lock = new Object();

    RoomGameContext(GameStateStore store, LocalGameEngine engine) {
        this.store = store;
        this.engine = engine;
    }

    void processCommand(CommandDto dto, ClientConnection client) {
        synchronized (lock) {
            if ("JOIN".equals(dto.type())) {
                registerClient(client);
                syncClient(dto, client);
                return;
            }

            registerClient(client);

            try {
                GameCommand cmd = DtoMapper.fromDto(dto);
                EngineResult result = store.withWriteLock(state -> engine.apply(state, cmd));

                boolean hasErrors = result.events().stream().anyMatch(GameEvent::isError);

                if (!hasErrors) {
                    history.add(dto);
                    broadcast(dto);
                } else {
                    String message = result.events().isEmpty()
                            ? "Command failed."
                            : result.events().get(0).message();
                    sendToClient(CommandDto.error(dto, message), client);
                }
            } catch (IllegalArgumentException e) {
                sendToClient(CommandDto.error(dto, e.getMessage()), client);
            }
        }
    }

    private void broadcast(CommandDto dto) {
        for (ClientConnection client : clients) {
            sendToClient(dto, client);
        }
    }

    private void sendToClient(CommandDto dto, ClientConnection client) {
        try {
            client.send(dto);
        } catch (IOException e) {
            log.warn("Failed to send to client", e);
            clients.remove(client);
        }
    }

    private void registerClient(ClientConnection client) {
        if (!clients.contains(client)) {
            clients.add(client);
        }
    }

    private void syncClient(CommandDto joinDto, ClientConnection client) {
        sendToClient(syncDto("SYNC_BEGIN", joinDto), client);
        for (CommandDto past : history) {
            sendToClient(past, client);
        }
        sendToClient(syncDto("SYNC_END", joinDto), client);
    }

    private CommandDto syncDto(String type, CommandDto joinDto) {
        return new CommandDto(
                type,
                joinDto.playerId(),
                "",
                System.currentTimeMillis(),
                joinDto.roomId()
        );
    }

}
