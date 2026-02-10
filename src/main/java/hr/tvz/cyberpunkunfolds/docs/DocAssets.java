package hr.tvz.cyberpunkunfolds.docs;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class DocAssets {
    private static final String TEMPLATE = "/docs/template.html";
    private static final String CSS = "/docs/docs.css";
    private static final String JS = "/docs/docs.js";

    String renderFromTemplate(DocTemplateModel model) {
        String template = readResource(TEMPLATE);
        String css = readResource(CSS);
        String js = readResource(JS);

        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("TITLE", DocHtml.escape(model.title()));
        vars.put("HEADER_TITLE", DocHtml.escape(model.headerTitle()));
        vars.put("BASE_PACKAGE", DocHtml.escape(model.basePackage()));
        vars.put("CLASS_COUNT", DocHtml.escape(model.classCount()));
        vars.put("GENERATED_AT", DocHtml.escape(model.generatedAt()));

        vars.put("CSS", css);
        vars.put("JS", js);
        vars.put("TOOLBAR", model.toolbar());
        vars.put("SIDEBAR", model.sidebar());
        vars.put("CONTENT", model.content());

        String out = apply(template, vars);

        if (out.contains("{{")) {
            throw new IllegalStateException("Unresolved template placeholders remain in docs output.");
        }
        return out;
    }

    private static String apply(String template, Map<String, String> vars) {
        String out = template;
        for (var e : vars.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return out;
    }

    private static String readResource(String path) {
        try (InputStream in = DocAssets.class.getResourceAsStream(path)) {
            Objects.requireNonNull(in, "Missing resource: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }
}
