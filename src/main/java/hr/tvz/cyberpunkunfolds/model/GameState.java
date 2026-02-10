package hr.tvz.cyberpunkunfolds.model;

import hr.tvz.cyberpunkunfolds.model.world.NodeId;
import hr.tvz.cyberpunkunfolds.model.world.WorldGraph;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int INTERACTIONS_PER_TURN = 2;

    private final List<Player> players;
    private int currentPlayerIndex;

    private int alarm;
    private final Set<NodeId> unlockedNodes;
    private final Set<NodeId> solvedNodes;
    private final Map<UUID, NodeId> playerLocations;

    private int interactionsRemaining;
    @Getter private boolean gameOver;
    private GameOutcome outcome;

    public GameState(List<Player> players, NodeId startNode) {
        this(players, startNode, EnumSet.of(startNode));
    }

    public GameState(List<Player> players, NodeId startNode, Set<NodeId> initiallyUnlocked) {
        Objects.requireNonNull(players);
        Objects.requireNonNull(startNode, "Start node cannot be null");
        Objects.requireNonNull(initiallyUnlocked, "Initially unlocked nodes cannot be null");
        if (players.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required.");
        }
        this.players = new ArrayList<>(players);
        this.currentPlayerIndex = 0;
        this.alarm = 0;

        this.unlockedNodes = EnumSet.noneOf(NodeId.class);
        this.unlockedNodes.addAll(initiallyUnlocked);
        this.unlockedNodes.add(startNode);

        this.solvedNodes = EnumSet.noneOf(NodeId.class);

        this.playerLocations = new HashMap<>();
        for (Player p : players) {
            this.playerLocations.put(p.id(), startNode);
        }

        this.interactionsRemaining = INTERACTIONS_PER_TURN;
        this.gameOver = false;
        this.outcome = null;
    }

    public List<Player> players() {
        return Collections.unmodifiableList(players);
    }

    public Player currentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int alarm() {
        return alarm;
    }

    public void increaseAlarm(int delta) {
        alarm = Math.max(0, alarm + delta);
    }

    public boolean isUnlocked(NodeId nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    public void unlock(NodeId nodeId) {
        unlockedNodes.add(nodeId);
    }

    public boolean isSolved(NodeId nodeId) {
        return solvedNodes.contains(nodeId);
    }

    public void markSolved(NodeId nodeId) {
        solvedNodes.add(nodeId);
    }

    public NodeId locationOf(UUID playerId) {
        return playerLocations.get(playerId);
    }

    public void moveAllPlayers(NodeId to) {
        for (Player p : players) {
            playerLocations.put(p.id(), to);
        }
    }

    public int interactionsRemaining() {
        return interactionsRemaining;
    }

    public Result<Void> consumeInteraction() {
        if (interactionsRemaining <= 0) {
            return Result.fail("No interactions remaining. Use EndTurn or SolvePuzzle.");
        }
        interactionsRemaining--;
        return Result.ok();
    }

    public boolean hasNoInteractionsRemaining() {
        return interactionsRemaining <= 0;
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        interactionsRemaining = INTERACTIONS_PER_TURN;
    }

    public void endGame(GameOutcome outcome) {
        this.gameOver = true;
        this.outcome = Objects.requireNonNull(outcome);
    }

    public Optional<GameOutcome> outcome() {
        return Optional.ofNullable(outcome);
    }

    public Result<Void> canMoveTo(UUID playerId, NodeId to, WorldGraph world) {
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(world, "world");

        Result<Void> turnCheck = ensureTurn(playerId, "Game is over. No more moves allowed.");
        if (turnCheck.isFailure()) {
            return turnCheck;
        }

        if (hasNoInteractionsRemaining()) {
            return Result.fail("No interactions remaining. Use EndTurn or SolvePuzzle.");
        }

        if (!isUnlocked(to)) {
            return Result.fail("Move blocked: node is locked (" + to + ")");
        }

        NodeId from = locationOf(playerId);
        if (from == null) {
            return Result.fail("Unknown player.");
        }

        if (from.equals(to)) {
            return Result.fail("Already at " + to + ".");
        }

        if (!world.areAdjacent(from, to)) {
            return Result.fail("Cannot move to " + to + ". Not adjacent to " + from + ".");
        }

        return Result.ok();
    }

    public Result<Void> canUseItem(UUID playerId) {
        return ensureTurn(playerId, "Game is over. No more actions allowed.");
    }

    public Result<Void> canInteract(UUID playerId) {
        Result<Void> turnCheck = ensureTurn(playerId, "Game is over. No more actions allowed.");
        if (turnCheck.isFailure()) {
            return turnCheck;
        }
        if (hasNoInteractionsRemaining()) {
            return Result.fail("No interactions remaining. Use EndTurn or SolvePuzzle.");
        }
        return Result.ok();
    }

    private Result<Void> ensureTurn(UUID playerId, String gameOverMessage) {
        Objects.requireNonNull(playerId, "playerId");
        if (isGameOver()) {
            return Result.fail(gameOverMessage);
        }
        if (!playerId.equals(currentPlayer().id())) {
            return Result.fail("Not your turn. Current player: " + currentPlayer().name());
        }
        return Result.ok();
    }

}
