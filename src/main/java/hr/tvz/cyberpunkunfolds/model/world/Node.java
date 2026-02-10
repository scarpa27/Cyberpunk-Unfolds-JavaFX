package hr.tvz.cyberpunkunfolds.model.world;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class Node implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final NodeId id;
    private final String title;
    private final String description;
    private final Puzzle puzzle;

    public Node(NodeId id, String title, String description, Puzzle puzzle) {
        this.id = Objects.requireNonNull(id, "NodeId cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.puzzle = puzzle;
    }

    public Node(NodeId id, String title, String description) {
        this(id, title, description, null);
    }

    public NodeId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Optional<Puzzle> puzzle() {
        return Optional.ofNullable(puzzle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{id=" + id + ", title='" + title + "'}";
    }
}
