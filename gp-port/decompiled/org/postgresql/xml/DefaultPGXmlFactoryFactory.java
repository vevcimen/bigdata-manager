/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.postgresql.xml.EmptyStringEntityResolver;
import org.postgresql.xml.NullErrorHandler;
import org.postgresql.xml.PGXmlFactoryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DefaultPGXmlFactoryFactory
implements PGXmlFactoryFactory {
    public static final DefaultPGXmlFactoryFactory INSTANCE = new DefaultPGXmlFactoryFactory();

    private DefaultPGXmlFactoryFactory() {
    }

    private DocumentBuilderFactory getDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DefaultPGXmlFactoryFactory.setFactoryProperties(factory);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder builder = this.getDocumentBuilderFactory().newDocumentBuilder();
        builder.setEntityResolver(EmptyStringEntityResolver.INSTANCE);
        builder.setErrorHandler(NullErrorHandler.INSTANCE);
        return builder;
    }

    @Override
    public TransformerFactory newTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        DefaultPGXmlFactoryFactory.setFactoryProperties(factory);
        return factory;
    }

    @Override
    public SAXTransformerFactory newSAXTransformerFactory() {
        SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        DefaultPGXmlFactoryFactory.setFactoryProperties(factory);
        return factory;
    }

    @Override
    public XMLInputFactory newXMLInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        DefaultPGXmlFactoryFactory.setPropertyQuietly(factory, "javax.xml.stream.supportDTD", false);
        DefaultPGXmlFactoryFactory.setPropertyQuietly(factory, "javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    }

    @Override
    public XMLOutputFactory newXMLOutputFactory() {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        return factory;
    }

    @Override
    public XMLReader createXMLReader() throws SAXException {
        XMLReader factory = XMLReaderFactory.createXMLReader();
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://xml.org/sax/features/external-general-entities", false);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setErrorHandler(NullErrorHandler.INSTANCE);
        return factory;
    }

    private static void setFeatureQuietly(Object factory, String name, boolean value) {
        try {
            if (factory instanceof DocumentBuilderFactory) {
                ((DocumentBuilderFactory)factory).setFeature(name, value);
            } else if (factory instanceof TransformerFactory) {
                ((TransformerFactory)factory).setFeature(name, value);
            } else if (factory instanceof XMLReader) {
                ((XMLReader)factory).setFeature(name, value);
            } else {
                throw new Error("Invalid factory class: " + factory.getClass());
            }
            return;
        }
        catch (Exception exception) {
            return;
        }
    }

    private static void setAttributeQuietly(Object factory, String name, Object value) {
        block4: {
            try {
                if (factory instanceof DocumentBuilderFactory) {
                    ((DocumentBuilderFactory)factory).setAttribute(name, value);
                    break block4;
                }
                if (factory instanceof TransformerFactory) {
                    ((TransformerFactory)factory).setAttribute(name, value);
                    break block4;
                }
                throw new Error("Invalid factory class: " + factory.getClass());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private static void setFactoryProperties(Object factory) {
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://xml.org/sax/features/external-general-entities", false);
        DefaultPGXmlFactoryFactory.setFeatureQuietly(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        DefaultPGXmlFactoryFactory.setAttributeQuietly(factory, "http://javax.xml.XMLConstants/property/accessExternalDTD", "");
        DefaultPGXmlFactoryFactory.setAttributeQuietly(factory, "http://javax.xml.XMLConstants/property/accessExternalSchema", "");
        DefaultPGXmlFactoryFactory.setAttributeQuietly(factory, "http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
    }

    private static void setPropertyQuietly(Object factory, String name, Object value) {
        block4: {
            try {
                if (factory instanceof XMLReader) {
                    ((XMLReader)factory).setProperty(name, value);
                    break block4;
                }
                if (factory instanceof XMLInputFactory) {
                    ((XMLInputFactory)factory).setProperty(name, value);
                    break block4;
                }
                throw new Error("Invalid factory class: " + factory.getClass());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

