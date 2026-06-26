/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public interface PGXmlFactoryFactory {
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException;

    public TransformerFactory newTransformerFactory();

    public SAXTransformerFactory newSAXTransformerFactory();

    public XMLInputFactory newXMLInputFactory();

    public XMLOutputFactory newXMLOutputFactory();

    public XMLReader createXMLReader() throws SAXException;
}

