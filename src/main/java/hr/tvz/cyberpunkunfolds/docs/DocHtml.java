package hr.tvz.cyberpunkunfolds.docs;

final class DocHtml {
    private DocHtml() { }

    static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static String badge(String css, String text) {
        String cls = css == null ? "" : css;
        return DocHtmlTemplates.BADGE.formatted(cls, escape(text));
    }
}
