package hr.tvz.cyberpunkunfolds.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

public final class XmlFactories {
    private XmlFactories() {}

    public static DocumentBuilderFactory safeDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        configure(factory);
        return factory;
    }

    @SuppressWarnings("java:S2755") // made safe in configure(factory) below
    public static SAXParserFactory safeSaxParserFactory() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        configure(factory);
        return factory;
    }

    @SuppressWarnings("java:S2755") // made safe in configure(factory) below
    public static TransformerFactory safeTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        configure(factory);
        return factory;
    }

    private static void configure(DocumentBuilderFactory factory) {
        trySetFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        trySetFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        trySetFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
    }

    private static void configure(SAXParserFactory factory) {
        trySetFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        trySetFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        trySetFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
    }

    private static void configure(TransformerFactory factory) {
        trySetFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetAttribute(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        trySetAttribute(factory, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    }

    private static void trySetFeature(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (Exception _) {
            // ignore unsupported features
        }
    }

    private static void trySetFeature(SAXParserFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (Exception _) {
            // ignore unsupported features
        }
    }


    private static void trySetFeature(TransformerFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (Exception _) {
            // ignore unsupported features
        }
    }

    private static void trySetAttribute(TransformerFactory factory, String attribute, String value) {
        try {
            factory.setAttribute(attribute, value);
        } catch (Exception _) {
            // ignore unsupported attributes
        }
    }
}
