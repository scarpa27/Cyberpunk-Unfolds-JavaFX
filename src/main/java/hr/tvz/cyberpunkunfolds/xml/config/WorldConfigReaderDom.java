package hr.tvz.cyberpunkunfolds.xml.config;

import hr.tvz.cyberpunkunfolds.model.world.*;
import hr.tvz.cyberpunkunfolds.xml.XmlFactories;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.*;

@Slf4j
public final class WorldConfigReaderDom {

    public WorldGraph readFromClasspath(String resourcePath) {
        try (InputStream in = WorldConfigReaderDom.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("World config not found on classpath: " + resourcePath);
            }
            Document doc = XmlFactories.safeDocumentBuilderFactory().newDocumentBuilder().parse(in);

            WorldGraph graph = new WorldGraph();

            Map<NodeId, Puzzle> puzzleMap = parsePuzzles(doc);

            NodeList nodes = doc.getElementsByTagName("node");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);
                NodeId id = NodeId.valueOf(e.getAttribute("id"));
                String title = e.getAttribute("title");
                String description = getTextContent(e, "description", "No description available.");
                boolean unlocked = Boolean.parseBoolean(e.getAttribute("unlocked"));

                Puzzle puzzle = puzzleMap.get(id);
                Node node = new Node(id, title, description, puzzle);
                graph.addNode(node);
                if (unlocked) {
                    graph.markInitiallyUnlocked(id);
                }
            }

            NodeList edges = doc.getElementsByTagName("edge");
            for (int i = 0; i < edges.getLength(); i++) {
                Element e = (Element) edges.item(i);
                NodeId a = NodeId.valueOf(e.getAttribute("a"));
                NodeId b = NodeId.valueOf(e.getAttribute("b"));
                graph.addEdge(a, b);
            }

            log.info("Loaded world graph with {} nodes.", graph.allNodeIds().size());
            return graph;
        } catch (Exception e) {
            throw new IllegalStateException("Failed reading world config: " + resourcePath, e);
        }
    }

    private Map<NodeId, Puzzle> parsePuzzles(Document doc) {
        Map<NodeId, Puzzle> puzzles = new EnumMap<>(NodeId.class);
        NodeList puzzleNodes = doc.getElementsByTagName("puzzle");

        for (int i = 0; i < puzzleNodes.getLength(); i++) {
            Element e = (Element) puzzleNodes.item(i);
            String id = e.getAttribute("id");
            NodeId nodeId = NodeId.valueOf(e.getAttribute("node"));
            PuzzleType type = PuzzleType.valueOf(e.getAttribute("type"));
            String solution = e.getAttribute("solution");
            String prompt = getTextContent(e, "prompt", "Solve this puzzle.");

            List<String> clues = new ArrayList<>();
            NodeList clueNodes = e.getElementsByTagName("clue");
            for (int j = 0; j < clueNodes.getLength(); j++) {
                clues.add(clueNodes.item(j).getTextContent());
            }

            Puzzle puzzle = new Puzzle(id, nodeId, type, prompt, solution, clues);
            puzzles.put(nodeId, puzzle);
        }

        return puzzles;
    }

    private String getTextContent(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return defaultValue;
    }
}
