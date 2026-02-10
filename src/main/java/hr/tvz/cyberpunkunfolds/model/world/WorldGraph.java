package hr.tvz.cyberpunkunfolds.model.world;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class WorldGraph implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<NodeId, Node> nodes = new EnumMap<>(NodeId.class);
    private final Map<NodeId, Set<NodeId>> adjacency = new EnumMap<>(NodeId.class);
    private final Set<NodeId> initiallyUnlocked = EnumSet.noneOf(NodeId.class);

    public void addNode(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        nodes.put(node.id(), node);
        adjacency.computeIfAbsent(node.id(), k -> EnumSet.noneOf(NodeId.class));
    }

    public void markInitiallyUnlocked(NodeId id) {
        initiallyUnlocked.add(Objects.requireNonNull(id, "NodeId cannot be null"));
    }

    public void addEdge(NodeId a, NodeId b) {
        adjacency.computeIfAbsent(a, k -> EnumSet.noneOf(NodeId.class)).add(b);
        adjacency.computeIfAbsent(b, k -> EnumSet.noneOf(NodeId.class)).add(a);
    }

    public Optional<Node> getNode(NodeId id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Set<NodeId> neighbors(NodeId id) {
        return Collections.unmodifiableSet(adjacency.getOrDefault(id, EnumSet.noneOf(NodeId.class)));
    }

    public boolean areAdjacent(NodeId a, NodeId b) {
        return adjacency.getOrDefault(a, EnumSet.noneOf(NodeId.class)).contains(b);
    }

    public Set<NodeId> allNodeIds() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    public Set<NodeId> initiallyUnlockedNodes() {
        return Collections.unmodifiableSet(initiallyUnlocked);
    }
}
