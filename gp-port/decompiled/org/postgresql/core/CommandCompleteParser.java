/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Parser;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public final class CommandCompleteParser {
    private long oid;
    private long rows;

    public long getOid() {
        return this.oid;
    }

    public long getRows() {
        return this.rows;
    }

    void set(long oid, long rows) {
        this.oid = oid;
        this.rows = rows;
    }

    public void parse(String status) throws PSQLException {
        if (!Parser.isDigitAt(status, status.length() - 1)) {
            this.set(0L, 0L);
            return;
        }
        long oid = 0L;
        long rows = 0L;
        try {
            int lastSpace = status.lastIndexOf(32);
            if (Parser.isDigitAt(status, lastSpace + 1)) {
                int penultimateSpace;
                rows = Parser.parseLong(status, lastSpace + 1, status.length());
                if (Parser.isDigitAt(status, lastSpace - 1) && Parser.isDigitAt(status, (penultimateSpace = status.lastIndexOf(32, lastSpace - 1)) + 1)) {
                    oid = Parser.parseLong(status, penultimateSpace + 1, lastSpace);
                }
            }
        }
        catch (NumberFormatException e) {
            throw new PSQLException(GT.tr("Unable to parse the count in command completion tag: {0}.", status), PSQLState.CONNECTION_FAILURE, (Throwable)e);
        }
        this.set(oid, rows);
    }

    public String toString() {
        return "CommandStatus{oid=" + this.oid + ", rows=" + this.rows + '}';
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        CommandCompleteParser that = (CommandCompleteParser)o;
        if (this.oid != that.oid) {
            return false;
        }
        return this.rows == that.rows;
    }

    public int hashCode() {
        int result = (int)(this.oid ^ this.oid >>> 32);
        result = 31 * result + (int)(this.rows ^ this.rows >>> 32);
        return result;
    }
}

