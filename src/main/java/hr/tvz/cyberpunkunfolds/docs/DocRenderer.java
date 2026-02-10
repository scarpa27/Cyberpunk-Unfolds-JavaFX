package hr.tvz.cyberpunkunfolds.docs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class DocRenderer {
    private final DocSignatureFormatter formatter = new DocSignatureFormatter();
    private final DocMemberRenderer memberRenderer = new DocMemberRenderer(formatter);

    String renderToolbar() {
        return DocHtmlTemplates.TOOLBAR;
    }

    String renderSidebar(List<String> classNames) {
        StringBuilder sb = new StringBuilder(16_000);
        sb.append("""
            <aside class="sidebar">
              <div class="sideHead">
                <div class="label">Index</div>
                <div class="count">""").append(classNames.size()).append("""
                </div>
              </div>
              <div class="nav">
            """);

        for (int i = 0; i < classNames.size(); i++) {
            String fullClassName = classNames.get(i);
            String anchor = anchor(i);
            String displaySub = stripBasePackage(fullClassName);
            sb.append(DocHtmlTemplates.SIDEBAR_ITEM.formatted(
                    anchor,
                    DocHtml.escape(simpleName(fullClassName)),
                    DocHtml.escape(displaySub)));
        }

        sb.append("</div></aside>");
        return sb.toString();
    }

    String renderContent(List<String> classNames) {
        StringBuilder sb = new StringBuilder(256_000);
        sb.append("<main id=\"content\">");

        for (int i = 0; i < classNames.size(); i++) {
            String fullClassName = classNames.get(i);
            String anchor = anchor(i);

            Class<?> c;
            try {
                c = loadClassNoInit(fullClassName);
            } catch (Exception | LinkageError t) {
                sb.append(renderUnloadable(anchor, fullClassName, t));
                continue;
            }

            sb.append(renderClassCard(anchor, fullClassName, c));
        }

        sb.append("</main>");
        return sb.toString();
    }

    private String renderUnloadable(String anchor, String fullClassName, Throwable t) {
        String displayName = stripBasePackage(fullClassName);
        return """
            <section class="cls" id="%s">
              <details class="clsDetails" open>
                <summary>
                  <div class="clsName">%s</div>
                  <div class="badges">%s</div>
                </summary>
                <div class="clsBody">
                  <div class="muted">Failed to load class: %s</div>
                </div>
              </details>
            </section>
            """.formatted(
                DocHtml.escape(anchor),
                DocHtml.escape(displayName),
                DocHtml.badge("kind", "unloadable"),
                DocHtml.escape(String.valueOf(t)));
    }

    private String renderClassCard(String anchor, String fullClassName, Class<?> c) {
        Field[] fields = safe(c::getDeclaredFields, new Field[0]);
        Constructor<?>[] constructors = safe(c::getDeclaredConstructors, new Constructor<?>[0]);
        Method[] methods = safe(c::getDeclaredMethods, new Method[0]);

        Arrays.sort(fields, Comparator.comparing(Field::getName));
        Arrays.sort(constructors, Comparator.comparingInt(Constructor::getParameterCount));
        Arrays.sort(methods, Comparator.comparing(Method::getName).thenComparingInt(Method::getParameterCount));

        String kind = formatter.classKind(c);
        String vis = formatter.visibility(c.getModifiers());
        String visBadgeClass = switch (vis) {
            case "public" -> "public";
            case "private" -> "private";
            default -> "";
        };
        
        StringBuilder sb = new StringBuilder(24_000);
        sb.append("<section class=\"cls\" id=\"").append(DocHtml.escape(anchor))
                .append("\" data-name=\"").append(DocHtml.escape(fullClassName)).append("\">");

        sb.append("<details class=\"clsDetails\" open><summary>");
        sb.append("<div class=\"clsName\">").append(DocHtml.escape(formatter.typeToString(c))).append("</div>");
        sb.append("<div class=\"badges\">")
                .append(DocHtml.badge("kind", kind))
                .append(DocHtml.badge(visBadgeClass, vis))
                .append(DocHtml.badge("", "fields: " + fields.length))
                .append(DocHtml.badge("", "constructors: " + constructors.length))
                .append(DocHtml.badge("", "methods: " + methods.length))
                .append("</div>");
        sb.append("</summary><div class=\"clsBody\">");

        for (Annotation a : safe(c::getDeclaredAnnotations, new Annotation[0])) {
            sb.append("<div class=\"annoLine\">@")
                    .append(DocHtml.escape(a.annotationType().getName()))
                    .append("</div>");
        }

        sb.append("<div class=\"sig\"><pre class=\"code\">")
                .append(DocHtml.escape(formatter.classSignature(c)))
                .append("</pre></div>");

        sb.append(memberRenderer.renderBlock("Fields", "field", Arrays.asList(fields), memberRenderer::renderField));
        sb.append(memberRenderer.renderBlock("Constructors", "ctor", Arrays.asList(constructors),
                                             (kindLabel, ctor) -> memberRenderer.renderCtor(kindLabel, c, ctor)));
        sb.append(memberRenderer.renderBlock("Methods", "method", Arrays.asList(methods), memberRenderer::renderMethod));

        sb.append("</div></details></section>");
        return sb.toString();
    }

    private static Class<?> loadClassNoInit(String fullClassName) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = DocRenderer.class.getClassLoader();
        return Class.forName(fullClassName, false, cl);
    }

    private static String anchor(int i) {
        return "c" + i;
    }

    private static String simpleName(String fullClassName) {
        int idx = fullClassName.lastIndexOf('.');
        return idx >= 0 ? fullClassName.substring(idx + 1) : fullClassName;
    }

    private static String stripBasePackage(String fullClassName) {
        String prefix = "hr.tvz.cyberpunkunfolds.";
        if (fullClassName.startsWith(prefix)) {
            return fullClassName.substring(prefix.length());
        }
        return fullClassName;
    }

    private static <T> T safe(SupplierEx<T> s, T fallback) {
        try {
            return s.get();
        } catch (Exception | LinkageError _) {
            return fallback;
        }
    }

    @FunctionalInterface
    private interface SupplierEx<T> {
        T get() throws Exception; //NOSONAR the point is that Exception is a top-level class
    }
}
