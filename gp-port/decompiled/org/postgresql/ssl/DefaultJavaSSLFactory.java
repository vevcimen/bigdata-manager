/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.util.Properties;
import javax.net.ssl.SSLSocketFactory;
import org.postgresql.ssl.WrappedFactory;

public class DefaultJavaSSLFactory
extends WrappedFactory {
    public DefaultJavaSSLFactory(Properties info) {
        this.factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    }
}

