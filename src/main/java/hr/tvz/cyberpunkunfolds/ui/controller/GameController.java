package hr.tvz.cyberpunkunfolds.ui.controller;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.model.Player;
import hr.tvz.cyberpunkunfolds.net.tcp.GameClient;
import hr.tvz.cyberpunkunfolds.ui.service.GameChatService;
import hr.tvz.cyberpunkunfolds.ui.service.GameCommandHandler;
import hr.tvz.cyberpunkunfolds.ui.service.GameExitHandler;
import hr.tvz.cyberpunkunfolds.ui.service.GameFileOperations;
import hr.tvz.cyberpunkunfolds.ui.service.GameUiUpdater;
import hr.tvz.cyberpunkunfolds.ui.session.GameSession;
import hr.tvz.cyberpunkunfolds.ui.session.GameSessionController;
import hr.tvz.cyberpunkunfolds.ui.session.LocalGameSession;
import hr.tvz.cyberpunkunfolds.ui.session.NetworkGameSession;
import hr.tvz.cyberpunkunfolds.ui.util.ControllerActions;
import hr.tvz.cyberpunkunfolds.ui.util.FxThread;
import hr.tvz.cyberpunkunfolds.ui.util.GameAsyncRunner;
import hr.tvz.cyberpunkunfolds.ui.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.List;

public final class GameController {
    @FXML private Label currentPlayerLabel;
    @FXML private Label localPlayerLabel;
    @FXML private Label interactionsLabel;
    @FXML private Label alarmLabel;
    @FXML private ListView<String> nodesList;
    @FXML private Label nodeTitle;
    @FXML private TextArea nodeDescription;
    @FXML private Label puzzleStatus;
    @FXML private TextArea puzzlePrompt;
    @FXML private ListView<String> cluesList;
    @FXML private TextField puzzleAnswerInput;
    @FXML private Button puzzleSubmitButton;
    @FXML private TextArea logArea;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private StackPane gameOverOverlay;
    @FXML private Label gameOverTitle;
    @FXML private Label gameOverMessage;

    private final Config cfg = Config.load();
    private GameSession session;
    private GameUiUpdater uiUpdater;
    private GameChatService chatService;
    private GameExitHandler exitHandler;
    private GameSessionController sessionController;
    private GameCommandHandler commandHandler;
    private GameFileOperations fileOperations;

    @FXML
    public void initialize() {
        uiUpdater = new GameUiUpdater(currentPlayerLabel, interactionsLabel, alarmLabel, nodesList, nodeTitle,
                                      nodeDescription, puzzleStatus, puzzlePrompt, cluesList, puzzleAnswerInput,
                                      puzzleSubmitButton, gameOverOverlay, gameOverTitle, gameOverMessage);
        chatService = new GameChatService(cfg, chatArea, chatInput);
        chatService.setEnabled(false);
    }

    public void initializeSinglePlayer() {
        setupSession(new LocalGameSession(cfg));
        exitHandler = null;
        sessionController.initializeSinglePlayer();
    }

    public void initializeMultiplayer(GameClient client,
                                      String roomId,
                                      List<Player> players,
                                      Player myPlayer,
                                      List<String> chatHistory) {
        NetworkGameSession networkSession = new NetworkGameSession(cfg);
        setupSession(networkSession);
        exitHandler = new GameExitHandler(networkSession, chatService, this::append);
        sessionController.initializeMultiplayer(client, roomId, players, myPlayer, chatHistory);
    }

    @FXML
    public void onBack() {
        if (session == null) {
            SceneManager.showMainMenu();
            return;
        }

        if (!session.confirmExit(exitHandler)) {
            return;
        }
        session.onExit(exitHandler);
        SceneManager.showMainMenu();
    }

    @FXML
    public void onMove() {
        commandHandler.onMove(nodesList.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void onInspect() {
        commandHandler.onInspect();
    }

    @FXML
    public void onEndTurn() {
        commandHandler.onEndTurn();
    }

    @FXML
    public void onSubmitPuzzle() {
        commandHandler.onSubmitPuzzle(puzzleAnswerInput.getText(), puzzleAnswerInput::clear);
    }

    @FXML
    public void onSave() {
        fileOperations.save();
    }

    @FXML
    public void onLoad() {
        fileOperations.load();
    }

    @FXML
    public void onExportMoves() {
        fileOperations.exportMoves();
    }

    @FXML
    public void onSendChat() {
        chatService.sendFromInput();
    }

    private void refreshUi() {
        FxThread.run(() -> uiUpdater.refresh(session.state(), session.engine(), session.world()));
    }

    private void runAsync(Runnable work, Runnable onSuccess, String errorPrefix) {
        ControllerActions.runAsync(errorPrefix, work, onSuccess, this::append);
    }

    private void append(String msg) {
        FxThread.run(() -> logArea.appendText(msg + "\n"));
    }

    private void setLocalPlayerName(String name) {
        FxThread.run(() -> localPlayerLabel.setText("You: " + name));
    }

    private void setupSession(GameSession session) {
        this.session = session;
        commandHandler = new GameCommandHandler(session, this::refreshUi, this::append);
        GameAsyncRunner asyncRunner = this::runAsync;
        fileOperations = new GameFileOperations(session, this::refreshUi, this::append, asyncRunner);
        sessionController = new GameSessionController(session, chatService, this::refreshUi, this::append,
                this::setLocalPlayerName, commandHandler::onRemoteCommand, asyncRunner);
    }
}
