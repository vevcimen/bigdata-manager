/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.core.Oid;
import org.postgresql.jdbc.FieldMetadata;

public class Field {
    public static final int TEXT_FORMAT = 0;
    public static final int BINARY_FORMAT = 1;
    private final int length;
    private final int oid;
    private final int mod;
    private String columnLabel;
    private int format = 0;
    private final int tableOid;
    private final int positionInTable;
    private @Nullable FieldMetadata metadata;
    private int sqlType;
    private String pgType = NOT_YET_LOADED;
    private static final String NOT_YET_LOADED = new String("pgType is not yet loaded");

    public Field(String name, int oid, int length, int mod) {
        this(name, oid, length, mod, 0, 0);
    }

    public Field(String name, int oid) {
        this(name, oid, 0, -1);
    }

    public Field(String columnLabel, int oid, int length, int mod, int tableOid, int positionInTable) {
        this.columnLabel = columnLabel;
        this.oid = oid;
        this.length = length;
        this.mod = mod;
        this.tableOid = tableOid;
        this.positionInTable = positionInTable;
        this.metadata = tableOid == 0 ? new FieldMetadata(columnLabel) : null;
    }

    @Pure
    public int getOID() {
        return this.oid;
    }

    public int getMod() {
        return this.mod;
    }

    public String getColumnLabel() {
        return this.columnLabel;
    }

    public int getLength() {
        return this.length;
    }

    public int getFormat() {
        return this.format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getTableOid() {
        return this.tableOid;
    }

    public int getPositionInTable() {
        return this.positionInTable;
    }

    public @Nullable FieldMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(FieldMetadata metadata) {
        this.metadata = metadata;
    }

    public String toString() {
        return "Field(" + (this.columnLabel != null ? this.columnLabel : "") + "," + Oid.toString(this.oid) + "," + this.length + "," + (this.format == 0 ? (char)'T' : 'B') + ")";
    }

    public void setSQLType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getSQLType() {
        return this.sqlType;
    }

    public void setPGType(String pgType) {
        this.pgType = pgType;
    }

    public String getPGType() {
        return this.pgType;
    }

    public boolean isTypeInitialized() {
        return this.pgType != NOT_YET_LOADED;
    }

    public void upperCaseLabel() {
        this.columnLabel = this.columnLabel.toUpperCase(Locale.ROOT);
    }
}

