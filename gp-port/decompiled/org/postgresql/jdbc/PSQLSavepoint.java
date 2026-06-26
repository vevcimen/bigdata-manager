/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.SQLException;
import java.sql.Savepoint;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Utils;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PSQLSavepoint
implements Savepoint {
    private boolean isValid = true;
    private final boolean isNamed;
    private int id;
    private @Nullable String name;

    public PSQLSavepoint(int id) {
        this.isNamed = false;
        this.id = id;
    }

    public PSQLSavepoint(String name) {
        this.isNamed = true;
        this.name = name;
    }

    @Override
    public int getSavepointId() throws SQLException {
        if (!this.isValid) {
            throw new PSQLException(GT.tr("Cannot reference a savepoint after it has been released.", new Object[0]), PSQLState.INVALID_SAVEPOINT_SPECIFICATION);
        }
        if (this.isNamed) {
            throw new PSQLException(GT.tr("Cannot retrieve the id of a named savepoint.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
        }
        return this.id;
    }

    @Override
    public String getSavepointName() throws SQLException {
        if (!this.isValid) {
            throw new PSQLException(GT.tr("Cannot reference a savepoint after it has been released.", new Object[0]), PSQLState.INVALID_SAVEPOINT_SPECIFICATION);
        }
        if (!this.isNamed || this.name == null) {
            throw new PSQLException(GT.tr("Cannot retrieve the name of an unnamed savepoint.", new Object[0]), PSQLState.WRONG_OBJECT_TYPE);
        }
        return this.name;
    }

    public void invalidate() {
        this.isValid = false;
    }

    public String getPGName() throws SQLException {
        if (!this.isValid) {
            throw new PSQLException(GT.tr("Cannot reference a savepoint after it has been released.", new Object[0]), PSQLState.INVALID_SAVEPOINT_SPECIFICATION);
        }
        if (this.isNamed && this.name != null) {
            return Utils.escapeIdentifier(null, this.name).toString();
        }
        return "JDBC_SAVEPOINT_" + this.id;
    }
}

