/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.util.Properties;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.postgresql.PGProperty;
import org.postgresql.ssl.LibPQFactory;
import org.postgresql.util.GT;
import org.postgresql.util.ObjectFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class SocketFactoryFactory {
    public static SocketFactory getSocketFactory(Properties info) throws PSQLException {
        String socketFactoryClassName = PGProperty.SOCKET_FACTORY.get(info);
        if (socketFactoryClassName == null) {
            return SocketFactory.getDefault();
        }
        try {
            return ObjectFactory.instantiate(SocketFactory.class, socketFactoryClassName, info, true, PGProperty.SOCKET_FACTORY_ARG.get(info));
        }
        catch (Exception e) {
            throw new PSQLException(GT.tr("The SocketFactory class provided {0} could not be instantiated.", socketFactoryClassName), PSQLState.CONNECTION_FAILURE, (Throwable)e);
        }
    }

    public static SSLSocketFactory getSslSocketFactory(Properties info) throws PSQLException {
        String classname = PGProperty.SSL_FACTORY.get(info);
        if (classname == null || "org.postgresql.ssl.jdbc4.LibPQFactory".equals(classname) || "org.postgresql.ssl.LibPQFactory".equals(classname)) {
            return new LibPQFactory(info);
        }
        try {
            return ObjectFactory.instantiate(SSLSocketFactory.class, classname, info, true, PGProperty.SSL_FACTORY_ARG.get(info));
        }
        catch (Exception e) {
            throw new PSQLException(GT.tr("The SSLSocketFactory class provided {0} could not be instantiated.", classname), PSQLState.CONNECTION_FAILURE, (Throwable)e);
        }
    }
}

