/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.SQLWarning;

class PSQLWarningWrapper {
    private final SQLWarning firstWarning;
    private SQLWarning lastWarning;

    PSQLWarningWrapper(SQLWarning warning) {
        this.firstWarning = warning;
        this.lastWarning = warning;
    }

    void addWarning(SQLWarning sqlWarning) {
        this.lastWarning.setNextWarning(sqlWarning);
        this.lastWarning = sqlWarning;
    }

    SQLWarning getFirstWarning() {
        return this.firstWarning;
    }
}

