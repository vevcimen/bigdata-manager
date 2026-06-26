/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class NullErrorHandler
implements ErrorHandler {
    public static final NullErrorHandler INSTANCE = new NullErrorHandler();

    @Override
    public void error(SAXParseException e) {
    }

    @Override
    public void fatalError(SAXParseException e) {
    }

    @Override
    public void warning(SAXParseException e) {
    }
}

