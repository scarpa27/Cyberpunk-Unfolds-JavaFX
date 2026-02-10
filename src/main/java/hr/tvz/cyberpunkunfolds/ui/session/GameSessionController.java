package hr.tvz.cyberpunkunfolds.ui.session;

import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;
import hr.tvz.cyberpunkunfolds.ui.service.GameChatService;
import hr.tvz.cyberpunkunfolds.ui.util.GameAsyncRunner;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class GameSessionController {
    private final GameSession session;
    private final GameChatService chatService;
    private final Runnable refreshUi;
    private final Consumer<String> append;
    private final Consumer<String> setLocalPlayerName;
    private final Consumer<CommandDto> onRemoteCommand;
    private final GameAsyncRunner asyncRunner;

    public GameSessionController(GameSession session,
                                 GameChatService chatService,
                                 Runnable refreshUi,
                                 Consumer<String> append,
                                 Consumer<String> setLocalPlayerName,
                                 Consumer<CommandDto> onRemoteCommand,
                                 GameAsyncRunner asyncRunner) {
        this.session = Objects.requireNonNull(session, "session");
        this.chatService = Objects.requireNonNull(chatService, "chatService");
        this.refreshUi = Objects.requireNonNull(refreshUi, "refreshUi");
        this.append = Objects.requireNonNull(append, "append");
        this.setLocalPlayerName = Objects.requireNonNull(setLocalPlayerName, "setLocalPlayerName");
        this.onRemoteCommand = Objects.requireNonNull(onRemoteCommand, "onRemoteCommand");
        this.asyncRunner = Objects.requireNonNull(asyncRunner, "asyncRunner");
    }

    public void initializeSinglePlayer() {
        Player p1 = Player.ofName("Player-1");
        setLocalPlayerName.accept(p1.name());
        chatService.setEnabled(false);
        asyncRunner.run(() -> session.startSinglePlayer(List.of(p1)),
                () -> {
                    refreshUi.run();
                    append.accept("Game initialized. Welcome to Neo-Zagreb.");
                    append.accept("Starting location: SLUMS");
                },
                "Failed to initialize game");
    }

    public void initializeMultiplayer(GameClient client,
                                      String roomId,
                                      List<Player> players,
                                      Player myPlayer,
                                      List<String> chatHistory) {
        setLocalPlayerName.accept(myPlayer.name());
        chatService.setEnabled(true);
        asyncRunner.run(() -> {
            client.setOnCommand(onRemoteCommand);
            session.startMultiplayer(client, roomId, players, myPlayer);
            chatService.init(roomId, myPlayer.name(), chatHistory);
        }, () -> {
            refreshUi.run();
            append.accept("Multiplayer game initialized.");
            append.accept("Room: " + roomId);
            append.accept("Players: " + players.stream().map(Player::name).toList());
            append.accept("You are: " + myPlayer.name());
            append.accept("Starting location: SLUMS");
        }, "Failed to initialize multiplayer");
    }
}
