/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.xml.DefaultPGXmlFactoryFactory;
import org.postgresql.xml.PGXmlFactoryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class PgSQLXML
implements SQLXML {
    private final BaseConnection conn;
    private @Nullable String data;
    private boolean initialized;
    private boolean active;
    private boolean freed;
    private @Nullable ByteArrayOutputStream byteArrayOutputStream;
    private @Nullable StringWriter stringWriter;
    private @Nullable DOMResult domResult;

    public PgSQLXML(BaseConnection conn) {
        this(conn, null, false);
    }

    public PgSQLXML(BaseConnection conn, @Nullable String data) {
        this(conn, data, true);
    }

    private PgSQLXML(BaseConnection conn, @Nullable String data, boolean initialized) {
        this.conn = conn;
        this.data = data;
        this.initialized = initialized;
        this.active = false;
        this.freed = false;
    }

    private PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException {
        if (this.conn != null) {
            return this.conn.getXmlFactoryFactory();
        }
        return DefaultPGXmlFactoryFactory.INSTANCE;
    }

    @Override
    public synchronized void free() {
        this.freed = true;
        this.data = null;
    }

    @Override
    public synchronized @Nullable InputStream getBinaryStream() throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        if (this.data == null) {
            return null;
        }
        try {
            return new ByteArrayInputStream(this.conn.getEncoding().encode(this.data));
        }
        catch (IOException ioe) {
            throw new PSQLException("Failed to re-encode xml data.", PSQLState.DATA_ERROR, (Throwable)ioe);
        }
    }

    @Override
    public synchronized @Nullable Reader getCharacterStream() throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        if (this.data == null) {
            return null;
        }
        return new StringReader(this.data);
    }

    @Override
    public synchronized <T extends Source> @Nullable T getSource(@Nullable Class<T> sourceClass) throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        String data = this.data;
        if (data == null) {
            return null;
        }
        try {
            if (sourceClass == null || DOMSource.class.equals(sourceClass)) {
                DocumentBuilder builder = this.getXmlFactoryFactory().newDocumentBuilder();
                InputSource input = new InputSource(new StringReader(data));
                DOMSource domSource = new DOMSource(builder.parse(input));
                return (T)domSource;
            }
            if (SAXSource.class.equals(sourceClass)) {
                XMLReader reader = this.getXmlFactoryFactory().createXMLReader();
                InputSource is = new InputSource(new StringReader(data));
                return (T)((Source)sourceClass.cast(new SAXSource(reader, is)));
            }
            if (StreamSource.class.equals(sourceClass)) {
                return (T)((Source)sourceClass.cast(new StreamSource(new StringReader(data))));
            }
            if (StAXSource.class.equals(sourceClass)) {
                XMLInputFactory xif = this.getXmlFactoryFactory().newXMLInputFactory();
                XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(data));
                return (T)((Source)sourceClass.cast(new StAXSource(xsr)));
            }
        }
        catch (Exception e) {
            throw new PSQLException(GT.tr("Unable to decode xml data.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)e);
        }
        throw new PSQLException(GT.tr("Unknown XML Source class: {0}", sourceClass), PSQLState.INVALID_PARAMETER_TYPE);
    }

    @Override
    public synchronized @Nullable String getString() throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        return this.data;
    }

    @Override
    public synchronized OutputStream setBinaryStream() throws SQLException {
        this.checkFreed();
        this.initialize();
        this.active = true;
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        return this.byteArrayOutputStream;
    }

    @Override
    public synchronized Writer setCharacterStream() throws SQLException {
        this.checkFreed();
        this.initialize();
        this.active = true;
        this.stringWriter = new StringWriter();
        return this.stringWriter;
    }

    @Override
    public synchronized <T extends Result> T setResult(Class<T> resultClass) throws SQLException {
        this.checkFreed();
        this.initialize();
        if (resultClass == null || DOMResult.class.equals(resultClass)) {
            this.domResult = new DOMResult();
            this.active = true;
            return (T)this.domResult;
        }
        if (SAXResult.class.equals(resultClass)) {
            try {
                SAXTransformerFactory transformerFactory = this.getXmlFactoryFactory().newSAXTransformerFactory();
                TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
                this.stringWriter = new StringWriter();
                transformerHandler.setResult(new StreamResult(this.stringWriter));
                this.active = true;
                return (T)((Result)resultClass.cast(new SAXResult(transformerHandler)));
            }
            catch (TransformerException te) {
                throw new PSQLException(GT.tr("Unable to create SAXResult for SQLXML.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)te);
            }
        }
        if (StreamResult.class.equals(resultClass)) {
            this.stringWriter = new StringWriter();
            this.active = true;
            return (T)((Result)resultClass.cast(new StreamResult(this.stringWriter)));
        }
        if (StAXResult.class.equals(resultClass)) {
            StringWriter stringWriter;
            this.stringWriter = stringWriter = new StringWriter();
            try {
                XMLOutputFactory xof = this.getXmlFactoryFactory().newXMLOutputFactory();
                XMLStreamWriter xsw = xof.createXMLStreamWriter(stringWriter);
                this.active = true;
                return (T)((Result)resultClass.cast(new StAXResult(xsw)));
            }
            catch (XMLStreamException xse) {
                throw new PSQLException(GT.tr("Unable to create StAXResult for SQLXML", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)xse);
            }
        }
        throw new PSQLException(GT.tr("Unknown XML Result class: {0}", resultClass), PSQLState.INVALID_PARAMETER_TYPE);
    }

    @Override
    public synchronized void setString(String value) throws SQLException {
        this.checkFreed();
        this.initialize();
        this.data = value;
    }

    private void checkFreed() throws SQLException {
        if (this.freed) {
            throw new PSQLException(GT.tr("This SQLXML object has already been freed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
    }

    private void ensureInitialized() throws SQLException {
        if (!this.initialized) {
            throw new PSQLException(GT.tr("This SQLXML object has not been initialized, so you cannot retrieve data from it.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        if (!this.active) {
            return;
        }
        if (this.byteArrayOutputStream != null) {
            try {
                this.data = this.conn.getEncoding().decode(this.byteArrayOutputStream.toByteArray());
            }
            catch (IOException ioe) {
                throw new PSQLException(GT.tr("Failed to convert binary xml data to encoding: {0}.", this.conn.getEncoding().name()), PSQLState.DATA_ERROR, (Throwable)ioe);
            }
            finally {
                this.byteArrayOutputStream = null;
                this.active = false;
            }
        }
        if (this.stringWriter != null) {
            this.data = this.stringWriter.toString();
            this.stringWriter = null;
            this.active = false;
        } else if (this.domResult != null) {
            DOMResult domResult = this.domResult;
            try {
                TransformerFactory factory = this.getXmlFactoryFactory().newTransformerFactory();
                Transformer transformer = factory.newTransformer();
                DOMSource domSource = new DOMSource(domResult.getNode());
                StringWriter stringWriter = new StringWriter();
                StreamResult streamResult = new StreamResult(stringWriter);
                transformer.transform(domSource, streamResult);
                this.data = stringWriter.toString();
            }
            catch (TransformerException te) {
                throw new PSQLException(GT.tr("Unable to convert DOMResult SQLXML data to a string.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)te);
            }
            finally {
                domResult = null;
                this.active = false;
            }
        }
    }

    private void initialize() throws SQLException {
        if (this.initialized) {
            throw new PSQLException(GT.tr("This SQLXML object has already been initialized, so you cannot manipulate it further.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        this.initialized = true;
    }
}

