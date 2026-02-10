package hr.tvz.cyberpunkunfolds.xml.moves;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.xml.XmlFactories;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public final class MoveLogWriterDom {
    private final Config cfg = Config.load();

    public Path write(List<CommandDto> moves, String fileName) {
        try {
            Files.createDirectories(cfg.xmlDir());
            Path outPath = cfg.xmlDir().resolve(fileName);

            Document doc = XmlFactories.safeDocumentBuilderFactory().newDocumentBuilder().newDocument();
            Element root = doc.createElement("moves");
            doc.appendChild(root);

            for (CommandDto m : moves) {
                Element e = doc.createElement("move");
                e.setAttribute("t", Long.toString(m.epochMillis()));
                e.setAttribute("player", m.playerId());
                e.setAttribute("type", m.type());
                e.setAttribute("payload", m.payload());
                e.setAttribute("roomId", m.roomId());
                root.appendChild(e);
            }

            var transformer = XmlFactories.safeTransformerFactory().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            try (OutputStream out = Files.newOutputStream(outPath)) {
                transformer.transform(new DOMSource(doc), new StreamResult(out));
            }

            log.info("Wrote {} moves to {}", moves.size(), outPath);
            return outPath;
        } catch (Exception e) {
            throw new IllegalStateException("Failed writing move log", e);
        }
    }
}
