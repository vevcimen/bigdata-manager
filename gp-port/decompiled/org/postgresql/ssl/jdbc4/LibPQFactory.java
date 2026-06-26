/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl.jdbc4;

import java.net.IDN;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import org.postgresql.jdbc.SslMode;
import org.postgresql.ssl.PGjdbcHostnameVerifier;
import org.postgresql.util.PSQLException;

@Deprecated
public class LibPQFactory
extends org.postgresql.ssl.LibPQFactory
implements HostnameVerifier {
    private final SslMode sslMode;

    @Deprecated
    public LibPQFactory(Properties info) throws PSQLException {
        super(info);
        this.sslMode = SslMode.of(info);
    }

    @Deprecated
    public static boolean verifyHostName(String hostname, String pattern) {
        String canonicalHostname;
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
            canonicalHostname = hostname.substring(1, hostname.length() - 1);
        } else {
            try {
                canonicalHostname = IDN.toASCII(hostname);
            }
            catch (IllegalArgumentException e) {
                return false;
            }
        }
        return PGjdbcHostnameVerifier.INSTANCE.verifyHostName(canonicalHostname, pattern);
    }

    @Override
    @Deprecated
    public boolean verify(String hostname, SSLSession session) {
        if (!this.sslMode.verifyPeerName()) {
            return true;
        }
        return PGjdbcHostnameVerifier.INSTANCE.verify(hostname, session);
    }
}

