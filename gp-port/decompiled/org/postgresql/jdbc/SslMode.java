/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.Properties;
import org.postgresql.PGProperty;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public enum SslMode {
    DISABLE("disable"),
    ALLOW("allow"),
    PREFER("prefer"),
    REQUIRE("require"),
    VERIFY_CA("verify-ca"),
    VERIFY_FULL("verify-full");

    public static final SslMode[] VALUES;
    public final String value;

    private SslMode(String value) {
        this.value = value;
    }

    public boolean requireEncryption() {
        return this.compareTo(REQUIRE) >= 0;
    }

    public boolean verifyCertificate() {
        return this == VERIFY_CA || this == VERIFY_FULL;
    }

    public boolean verifyPeerName() {
        return this == VERIFY_FULL;
    }

    public static SslMode of(Properties info) throws PSQLException {
        String sslmode = PGProperty.SSL_MODE.get(info);
        if (sslmode == null) {
            if (PGProperty.SSL.getBoolean(info) || "".equals(PGProperty.SSL.get(info))) {
                return VERIFY_FULL;
            }
            return PREFER;
        }
        for (SslMode sslMode : VALUES) {
            if (!sslMode.value.equalsIgnoreCase(sslmode)) continue;
            return sslMode;
        }
        throw new PSQLException(GT.tr("Invalid sslmode value: {0}", sslmode), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
    }

    static {
        VALUES = SslMode.values();
    }
}

