/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.Properties;
import org.postgresql.PGProperty;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public enum GSSEncMode {
    DISABLE("disable"),
    ALLOW("allow"),
    PREFER("prefer"),
    REQUIRE("require");

    private static final GSSEncMode[] VALUES;
    public final String value;

    private GSSEncMode(String value) {
        this.value = value;
    }

    public boolean requireEncryption() {
        return this.compareTo(REQUIRE) >= 0;
    }

    public static GSSEncMode of(Properties info) throws PSQLException {
        String gssEncMode = PGProperty.GSS_ENC_MODE.get(info);
        if (gssEncMode == null) {
            return ALLOW;
        }
        for (GSSEncMode mode : VALUES) {
            if (!mode.value.equalsIgnoreCase(gssEncMode)) continue;
            return mode;
        }
        throw new PSQLException(GT.tr("Invalid gssEncMode value: {0}", gssEncMode), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
    }

    static {
        VALUES = GSSEncMode.values();
    }
}

