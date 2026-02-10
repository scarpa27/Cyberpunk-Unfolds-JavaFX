package hr.tvz.cyberpunkunfolds.ui.session;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.engine.command.GameCommand;
import hr.tvz.cyberpunkunfolds.engine.event.ErrorEvent;
import hr.tvz.cyberpunkunfolds.engine.event.GameEvent;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.net.protocol.DtoMapper;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class NetworkGameSession extends GameSession {
    private GameClient networkClient;
    private String roomId;
    private Player myPlayer;
    private boolean syncComplete;

    public NetworkGameSession(Config cfg) {
        super(cfg);
    }

    @Override
    public void startSinglePlayer(List<Player> players) {
        throw new UnsupportedOperationException("Single-player start not supported in network session.");
    }

    @Override
    public void startMultiplayer(GameClient client,
                                 String roomId,
                                 List<Player> players,
                                 Player myPlayer) {
        this.networkClient = Objects.requireNonNull(client, "client");
        this.roomId = Objects.requireNonNull(roomId, "roomId");
        this.myPlayer = Objects.requireNonNull(myPlayer, "myPlayer");
        this.syncComplete = false;
        initSession(players);
        client.connectAsync();
        sendJoin();
    }

    @Override
    public UUID playerIdForCommand() {
        return myPlayer.id();
    }

    @Override
    public void applyCommand(GameCommand cmd, Consumer<List<GameEvent>> onEvents, Runnable onStateChanged) {
        if (state == null || engine == null) {
            return;
        }
        if (!syncComplete) {
            onEvents.accept(List.of(new ErrorEvent("Sync in progress. Please wait.")));
            return;
        }
        CommandDto dto = DtoMapper.toDto(cmd, roomId);
        moveLog.add(dto);
        networkClient.send(dto);
    }

    @Override
    public void handleRemoteCommand(CommandDto dto,
                                    Consumer<List<GameEvent>> onEvents,
                                    Runnable onStateChanged,
                                    Consumer<String> onError) {
        if (state == null || engine == null) {
            return;
        }
        switch (dto.type()) {
            case "ERROR" -> {
                onError.accept(dto.payload());
                return;
            }
            case "SYNC_BEGIN" -> {
                syncComplete = false;
                return;
            }
            case "SYNC_END" -> {
                syncComplete = true;
                onStateChanged.run();
                return;
            }
            case "JOIN" -> {
                return;
            }
            default -> {
                // fallthrough and get parsed below
            }
        }
        try {
            GameCommand cmd = DtoMapper.fromDto(dto);
            var result = engine.apply(state, cmd);
            onEvents.accept(result.events());
            onStateChanged.run();
        } catch (IllegalArgumentException e) {
            onError.accept("Invalid command received: " + e.getMessage());
        }
    }

    public GameClient networkClient() {
        return networkClient;
    }

    private void sendJoin() {
        CommandDto join = new CommandDto(
                "JOIN",
                myPlayer.id().toString(),
                myPlayer.name(),
                System.currentTimeMillis(),
                roomId
        );
        networkClient.send(join);
    }
}
