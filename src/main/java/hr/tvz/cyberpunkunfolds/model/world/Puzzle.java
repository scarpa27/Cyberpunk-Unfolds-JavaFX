package hr.tvz.cyberpunkunfolds.model.world;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Puzzle implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final NodeId nodeId;
    private final PuzzleType type;
    private final String prompt;
    private final String correctAnswer;
    private final List<String> clues;

    private int revealedClueCount;

    public Puzzle(String id, NodeId nodeId, PuzzleType type, String prompt,
                  String correctAnswer, List<String> clues) {
        this.id = Objects.requireNonNull(id, "Puzzle id cannot be null");
        this.nodeId = Objects.requireNonNull(nodeId, "NodeId cannot be null");
        this.type = Objects.requireNonNull(type, "PuzzleType cannot be null");
        this.prompt = Objects.requireNonNull(prompt, "Prompt cannot be null");
        this.correctAnswer = Objects.requireNonNull(correctAnswer, "Correct answer cannot be null");
        this.clues = new ArrayList<>(Objects.requireNonNull(clues, "Clues cannot be null"));
        this.revealedClueCount = 0;
    }

    public String id() {
        return id;
    }

    public PuzzleType type() {
        return type;
    }

    public String prompt() {
        return prompt;
    }

    public List<String> revealedClues() {
        return Collections.unmodifiableList(clues.subList(0, Math.min(revealedClueCount, clues.size())));
    }

    public boolean hasMoreClues() {
        return revealedClueCount < clues.size();
    }

    public void revealNextClue() {
        if (hasMoreClues()) {
            revealedClueCount++;
        }
    }

    public int totalClueCount() {
        return clues.size();
    }

    public boolean isCorrectAnswer(String answer) {
        if (answer == null) {
            return false;
        }
        return correctAnswer.equalsIgnoreCase(answer.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Puzzle puzzle)) return false;
        return id.equals(puzzle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Puzzle{id='" + id + "', node=" + nodeId + ", type=" + type + "}";
    }
}
