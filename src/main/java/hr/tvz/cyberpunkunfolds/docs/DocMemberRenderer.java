package hr.tvz.cyberpunkunfolds.docs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

final class DocMemberRenderer {
    private final DocSignatureFormatter formatter;

    DocMemberRenderer(DocSignatureFormatter formatter) {
        this.formatter = formatter;
    }

    <T> String renderBlock(String title, String kind, List<T> items, Renderer<T> r) {
        StringBuilder sb = new StringBuilder(16_000);
        sb.append("<details class=\"block\" open><summary><span>")
                .append(DocHtml.escape(title))
                .append("</span><span class=\"smallCount\">")
                .append(items.size())
                .append("</span></summary><div class=\"members\">");

        for (T item : items) sb.append(r.render(kind, item));

        sb.append("</div></details>");
        return sb.toString();
    }

    String renderField(String kind, Field f) {
        String vis = formatter.visibility(f.getModifiers());
        boolean isStatic = Modifier.isStatic(f.getModifiers());
        boolean synth = f.isSynthetic();

        String name = f.getName();
        String sig = formatter.joinMods(f.getModifiers()) + formatter.typeToString(f.getGenericType()) + " " + name + ";";
        String tags = tags(isStatic, synth);
        String search = DocHtml.escape((name + " " + sig).toLowerCase(Locale.ROOT));

        return DocHtmlTemplates.MEMBER.formatted(
                kind, vis, String.valueOf(synth), search,
                DocHtml.escape(name),
                tags,
                DocHtml.escape(sig)
        );
    }

    String renderCtor(String kind, Class<?> owner, Constructor<?> ctor) {
        String vis = formatter.visibility(ctor.getModifiers());
        boolean synth = ctor.isSynthetic();

        String name = owner.getSimpleName();
        String sig = formatter.joinMods(ctor.getModifiers()) + formatter.constructorSignature(owner, ctor);
        String tags = tags(false, synth);
        String search = DocHtml.escape((name + " " + sig).toLowerCase(Locale.ROOT));

        return DocHtmlTemplates.MEMBER.formatted(
                kind, vis, String.valueOf(synth), search,
                DocHtml.escape(name),
                tags,
                DocHtml.escape(sig)
        );
    }

    String renderMethod(String kind, Method m) {
        String vis = formatter.visibility(m.getModifiers());
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        boolean synth = m.isSynthetic() || m.isBridge();

        String name = m.getName();
        String sig = formatter.joinMods(m.getModifiers()) + formatter.methodSignature(m);
        String tags = tags(isStatic, synth);
        String search = DocHtml.escape((name + " " + sig).toLowerCase(Locale.ROOT));

        return DocHtmlTemplates.MEMBER.formatted(
                kind, vis, String.valueOf(synth), search,
                DocHtml.escape(name),
                tags,
                DocHtml.escape(sig)
        );
    }

    String tags(boolean isStatic, boolean synth) {
        StringBuilder sb = new StringBuilder();
        if (isStatic) sb.append("<span class=\"tag static\">static</span>");
        if (synth) sb.append("<span class=\"tag synth\">compiler</span>");
        return sb.toString();
    }

    @FunctionalInterface
    interface Renderer<T> {
        String render(String kind, T item);
    }
}
