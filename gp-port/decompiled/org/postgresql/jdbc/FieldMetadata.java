/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.CanEstimateSize;

public class FieldMetadata
implements CanEstimateSize {
    final String columnName;
    final String tableName;
    final String schemaName;
    final int nullable;
    final boolean autoIncrement;

    public FieldMetadata(String columnName) {
        this(columnName, "", "", 2, false);
    }

    FieldMetadata(String columnName, String tableName, String schemaName, int nullable, boolean autoIncrement) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.schemaName = schemaName;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
    }

    @Override
    public long getSize() {
        return (long)(this.columnName.length() * 2 + this.tableName.length() * 2 + this.schemaName.length() * 2) + 4L + 1L;
    }

    public String toString() {
        return "FieldMetadata{columnName='" + this.columnName + '\'' + ", tableName='" + this.tableName + '\'' + ", schemaName='" + this.schemaName + '\'' + ", nullable=" + this.nullable + ", autoIncrement=" + this.autoIncrement + '}';
    }

    public static class Key {
        final int tableOid;
        final int positionInTable;

        Key(int tableOid, int positionInTable) {
            this.positionInTable = positionInTable;
            this.tableOid = tableOid;
        }

        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key)o;
            if (this.tableOid != key.tableOid) {
                return false;
            }
            return this.positionInTable == key.positionInTable;
        }

        public int hashCode() {
            int result = this.tableOid;
            result = 31 * result + this.positionInTable;
            return result;
        }

        public String toString() {
            return "Key{tableOid=" + this.tableOid + ", positionInTable=" + this.positionInTable + '}';
        }
    }
}

