package hr.tvz.cyberpunkunfolds.ui.service;

import hr.tvz.cyberpunkunfolds.engine.LocalGameEngine;
import hr.tvz.cyberpunkunfolds.model.GameState;
import hr.tvz.cyberpunkunfolds.model.world.Node;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.Puzzle;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GameUiUpdater {
    private final Label currentPlayerLabel;
    private final Label interactionsLabel;
    private final Label alarmLabel;
    private final ListView<String> nodesList;
    private final Label nodeTitle;
    private final TextArea nodeDescription;
    private final Label puzzleStatus;
    private final TextArea puzzlePrompt;
    private final ListView<String> cluesList;
    private final TextField puzzleAnswerInput;
    private final Button puzzleSubmitButton;
    private final StackPane gameOverOverlay;
    private final Label gameOverTitle;
    private final Label gameOverMessage;

    @SuppressWarnings("java:S107") // More than 7 constructor parameters
    // Either this or the game controller goes back to 400 lines. Further boxing would make it unreadable.
    public GameUiUpdater(Label currentPlayerLabel,
                         Label interactionsLabel,
                         Label alarmLabel,
                         ListView<String> nodesList,
                         Label nodeTitle,
                         TextArea nodeDescription,
                         Label puzzleStatus,
                         TextArea puzzlePrompt,
                         ListView<String> cluesList,
                         TextField puzzleAnswerInput,
                         Button puzzleSubmitButton,
                         StackPane gameOverOverlay,
                         Label gameOverTitle,
                         Label gameOverMessage) {
        this.currentPlayerLabel = currentPlayerLabel;
        this.interactionsLabel = interactionsLabel;
        this.alarmLabel = alarmLabel;
        this.nodesList = nodesList;
        this.nodeTitle = nodeTitle;
        this.nodeDescription = nodeDescription;
        this.puzzleStatus = puzzleStatus;
        this.puzzlePrompt = puzzlePrompt;
        this.cluesList = cluesList;
        this.puzzleAnswerInput = puzzleAnswerInput;
        this.puzzleSubmitButton = puzzleSubmitButton;
        this.gameOverOverlay = gameOverOverlay;
        this.gameOverTitle = gameOverTitle;
        this.gameOverMessage = gameOverMessage;
    }

    public void refresh(GameState state, LocalGameEngine engine, WorldGraph world) {
        updateTopBar(state, engine);
        updateNodeMap(state, world);
        updateCurrentNodeInfo(state, world);
        updatePuzzleInfo(state, world);
        checkGameOver(state);
    }

    private void updateTopBar(GameState state, LocalGameEngine engine) {
        currentPlayerLabel.setText("Turn: " + state.currentPlayer().name());

        int interactions = state.interactionsRemaining();
        interactionsLabel.setText("Interactions: " + interactions + "/" + GameState.INTERACTIONS_PER_TURN);

        alarmLabel.setText("Alarm: " + state.alarm() + "/" + engine.rules().alarmMax());
    }

    private void updateNodeMap(GameState state, WorldGraph world) {
        List<String> nodeItems = new ArrayList<>();
        for (NodeId nodeId : world.allNodeIds()) {
            StringBuilder item = new StringBuilder(nodeId.name());

            item.append(state.isUnlocked(nodeId) ? " [unlocked]" : " [locked]");
            if (state.isSolved(nodeId)) {
                item.append(" [solved]");
            }
            NodeId playerLoc = state.locationOf(state.currentPlayer().id());
            if (nodeId == playerLoc) {
                item.append(" [here]");
            }

            nodeItems.add(item.toString());
        }
        nodesList.getItems().setAll(nodeItems);
    }

    private void updateCurrentNodeInfo(GameState state, WorldGraph world) {
        Optional<Node> nodeOpt = nodeForPlayer(state, world, state.currentPlayer().id());

        if (nodeOpt.isPresent()) {
            Node node = nodeOpt.get();
            nodeTitle.setText(node.title());
            nodeDescription.setText(node.description());
        } else {
            nodeTitle.setText("Unknown");
            nodeDescription.setText("No data available.");
        }
    }

    private void updatePuzzleInfo(GameState state, WorldGraph world) {
        NodeId location = state.locationOf(state.currentPlayer().id());
        Optional<Puzzle> puzzleOpt = puzzleForPlayer(state, world, state.currentPlayer().id());

        if (puzzleOpt.isEmpty()) {
            puzzleStatus.setText("No puzzle at this location");
            puzzlePrompt.setText("---");
            cluesList.getItems().clear();
            updatePuzzleSubmissionControls(false);
            return;
        }

        Puzzle puzzle = puzzleOpt.get();

        if (state.isSolved(location)) {
            puzzleStatus.setText("SOLVED");
            puzzlePrompt.setText(puzzle.prompt());
            cluesList.getItems().setAll(puzzle.revealedClues());
            updatePuzzleSubmissionControls(false);
        } else {
            puzzleStatus.setText("UNSOLVED (" + puzzle.revealedClues().size() + "/" + puzzle.totalClueCount() + " clues)");
            puzzlePrompt.setText(puzzle.prompt());

            List<String> cluesDisplay = new ArrayList<>();
            List<String> revealed = puzzle.revealedClues();
            for (int i = 0; i < revealed.size(); i++) {
                cluesDisplay.add("[" + (i + 1) + "] " + revealed.get(i));
            }
            if (cluesDisplay.isEmpty()) {
                cluesDisplay.add("(Use Inspect to reveal clues)");
            }
            cluesList.getItems().setAll(cluesDisplay);
            updatePuzzleSubmissionControls(true);
        }
    }

    private void updatePuzzleSubmissionControls(boolean enabled) {
        if (puzzleAnswerInput != null) {
            puzzleAnswerInput.setDisable(!enabled);
            if (!enabled) {
                puzzleAnswerInput.clear();
            }
        }
        if (puzzleSubmitButton != null) {
            puzzleSubmitButton.setDisable(!enabled);
        }
    }

    private void checkGameOver(GameState state) {
        if (state.isGameOver()) {
            state.outcome().ifPresent(outcome -> {
                gameOverOverlay.setVisible(true);
                gameOverOverlay.setManaged(true);
                gameOverOverlay.toFront();

                boolean isWin = outcome.name().startsWith("WIN");
                gameOverTitle.setText(isWin ? "VICTORY!" : "DEFEAT");
                gameOverTitle.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #00ff00;");
                gameOverMessage.setText(outcome.message());
            });
        }
    }

    private static Optional<Node> nodeForPlayer(GameState state, WorldGraph world, java.util.UUID playerId) {
        return world.getNode(state.locationOf(playerId));
    }

    private static Optional<Puzzle> puzzleForPlayer(GameState state, WorldGraph world, java.util.UUID playerId) {
        return nodeForPlayer(state, world, playerId).flatMap(Node::puzzle);
    }
}
