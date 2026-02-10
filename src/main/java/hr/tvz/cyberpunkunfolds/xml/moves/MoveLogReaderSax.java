package hr.tvz.cyberpunkunfolds.xml.moves;

import hr.tvz.cyberpunkunfolds.net.protocol.CommandDto;
import hr.tvz.cyberpunkunfolds.xml.XmlFactories;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MoveLogReaderSax {

    public List<CommandDto> read(Path xmlFile) {
        try {
            List<CommandDto> out = new ArrayList<>();
            var parser = XmlFactories.safeSaxParserFactory().newSAXParser();
            parser.parse(xmlFile.toFile(), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if (!"move".equals(qName)) return;
                    long t = Long.parseLong(attributes.getValue("t"));
                    String player = attributes.getValue("player");
                    String type = attributes.getValue("type");
                    String payload = attributes.getValue("payload");
                    String roomId = attributes.getValue("roomId");
                    out.add(new CommandDto(type, player, payload, t, roomId != null ? roomId : "replay"));
                }
            });
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Failed reading move log: " + xmlFile, e);
        }
    }
}
