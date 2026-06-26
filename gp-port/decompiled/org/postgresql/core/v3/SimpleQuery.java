/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.lang.ref.PhantomReference;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Field;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.Oid;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.v3.SimpleParameterList;
import org.postgresql.core.v3.TypeTransferModeRegistry;
import org.postgresql.jdbc.PgResultSet;

class SimpleQuery
implements Query {
    private static final Logger LOGGER = Logger.getLogger(SimpleQuery.class.getName());
    private @Nullable Map<String, Integer> resultSetColumnNameIndexMap;
    private final NativeQuery nativeQuery;
    private final @Nullable TypeTransferModeRegistry transferModeRegistry;
    private @Nullable String statementName;
    private byte @Nullable [] encodedStatementName;
    private Field @Nullable [] fields;
    private boolean needUpdateFieldFormats;
    private boolean hasBinaryFields;
    private boolean portalDescribed;
    private boolean statementDescribed;
    private final boolean sanitiserDisabled;
    private @Nullable PhantomReference<?> cleanupRef;
    private int @Nullable [] preparedTypes;
    private @Nullable BitSet unspecifiedParams;
    private short deallocateEpoch;
    private @Nullable Integer cachedMaxResultRowSize;
    static final SimpleParameterList NO_PARAMETERS = new SimpleParameterList(0, null);

    SimpleQuery(SimpleQuery src) {
        this(src.nativeQuery, src.transferModeRegistry, src.sanitiserDisabled);
    }

    SimpleQuery(NativeQuery query, @Nullable TypeTransferModeRegistry transferModeRegistry, boolean sanitiserDisabled) {
        this.nativeQuery = query;
        this.transferModeRegistry = transferModeRegistry;
        this.sanitiserDisabled = sanitiserDisabled;
    }

    @Override
    public ParameterList createParameterList() {
        if (this.nativeQuery.bindPositions.length == 0) {
            return NO_PARAMETERS;
        }
        return new SimpleParameterList(this.getBindCount(), this.transferModeRegistry);
    }

    @Override
    public String toString(@Nullable ParameterList parameters) {
        return this.nativeQuery.toString(parameters);
    }

    public String toString() {
        return this.toString(null);
    }

    @Override
    public void close() {
        this.unprepare();
    }

    public SimpleQuery @Nullable [] org_postgresql_core_v3_SimpleQuery_arr_getSubqueries() {
        return null;
    }

    public int getMaxResultRowSize() {
        if (this.cachedMaxResultRowSize != null) {
            return this.cachedMaxResultRowSize;
        }
        if (!this.statementDescribed) {
            throw new IllegalStateException("Cannot estimate result row size on a statement that is not described");
        }
        int maxResultRowSize = 0;
        if (this.fields != null) {
            for (Field f : this.fields) {
                int fieldLength = f.getLength();
                if (fieldLength < 1 || fieldLength >= 65535) {
                    maxResultRowSize = -1;
                    break;
                }
                maxResultRowSize += fieldLength;
            }
        }
        this.cachedMaxResultRowSize = maxResultRowSize;
        return maxResultRowSize;
    }

    @Override
    public String getNativeSql() {
        return this.nativeQuery.nativeSql;
    }

    void setStatementName(String statementName, short deallocateEpoch) {
        assert (statementName != null) : "statement name should not be null";
        this.statementName = statementName;
        this.encodedStatementName = statementName.getBytes(StandardCharsets.UTF_8);
        this.deallocateEpoch = deallocateEpoch;
    }

    void setPrepareTypes(int[] paramTypes) {
        for (int i = 0; i < paramTypes.length; ++i) {
            int paramType = paramTypes[i];
            if (paramType != 0) continue;
            if (this.unspecifiedParams == null) {
                this.unspecifiedParams = new BitSet();
            }
            this.unspecifiedParams.set(i);
        }
        if (this.preparedTypes == null) {
            this.preparedTypes = (int[])paramTypes.clone();
            return;
        }
        System.arraycopy(paramTypes, 0, this.preparedTypes, 0, paramTypes.length);
    }

    int @Nullable [] getPrepareTypes() {
        return this.preparedTypes;
    }

    @Nullable String getStatementName() {
        return this.statementName;
    }

    boolean isPreparedFor(int[] paramTypes, short deallocateEpoch) {
        if (this.statementName == null || this.preparedTypes == null) {
            return false;
        }
        if (this.deallocateEpoch != deallocateEpoch) {
            return false;
        }
        assert (paramTypes.length == this.preparedTypes.length) : String.format("paramTypes:%1$d preparedTypes:%2$d", paramTypes.length, this.preparedTypes.length);
        BitSet unspecified = this.unspecifiedParams;
        for (int i = 0; i < paramTypes.length; ++i) {
            int paramType = paramTypes[i];
            int preparedType = this.preparedTypes[i];
            if (paramType == preparedType || paramType == 0 && unspecified != null && unspecified.get(i)) continue;
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Statement {0} does not match new parameter types. Will have to un-prepare it and parse once again. To avoid performance issues, use the same data type for the same bind position. Bind index (1-based) is {1}, preparedType was {2} (after describe {3}), current bind type is {4}", new Object[]{this.statementName, i + 1, Oid.toString(unspecified != null && unspecified.get(i) ? 0 : preparedType), Oid.toString(preparedType), Oid.toString(paramType)});
            }
            return false;
        }
        return true;
    }

    boolean hasUnresolvedTypes() {
        if (this.preparedTypes == null) {
            return true;
        }
        return this.unspecifiedParams != null && !this.unspecifiedParams.isEmpty();
    }

    byte @Nullable [] getEncodedStatementName() {
        return this.encodedStatementName;
    }

    void setFields(Field @Nullable [] fields) {
        this.fields = fields;
        this.resultSetColumnNameIndexMap = null;
        this.cachedMaxResultRowSize = null;
        this.needUpdateFieldFormats = fields != null;
        this.hasBinaryFields = false;
    }

    Field @Nullable [] getFields() {
        return this.fields;
    }

    boolean needUpdateFieldFormats() {
        if (this.needUpdateFieldFormats) {
            this.needUpdateFieldFormats = false;
            return true;
        }
        return false;
    }

    public void resetNeedUpdateFieldFormats() {
        this.needUpdateFieldFormats = this.fields != null;
    }

    public boolean hasBinaryFields() {
        return this.hasBinaryFields;
    }

    public void setHasBinaryFields(boolean hasBinaryFields) {
        this.hasBinaryFields = hasBinaryFields;
    }

    boolean isPortalDescribed() {
        return this.portalDescribed;
    }

    void setPortalDescribed(boolean portalDescribed) {
        this.portalDescribed = portalDescribed;
        this.cachedMaxResultRowSize = null;
    }

    @Override
    public boolean isStatementDescribed() {
        return this.statementDescribed;
    }

    void setStatementDescribed(boolean statementDescribed) {
        this.statementDescribed = statementDescribed;
        this.cachedMaxResultRowSize = null;
    }

    @Override
    public boolean isEmpty() {
        return this.getNativeSql().isEmpty();
    }

    void setCleanupRef(PhantomReference<?> cleanupRef) {
        if (this.cleanupRef != null) {
            this.cleanupRef.clear();
            this.cleanupRef.enqueue();
        }
        this.cleanupRef = cleanupRef;
    }

    void unprepare() {
        if (this.cleanupRef != null) {
            this.cleanupRef.clear();
            this.cleanupRef.enqueue();
            this.cleanupRef = null;
        }
        if (this.unspecifiedParams != null) {
            this.unspecifiedParams.clear();
        }
        this.statementName = null;
        this.encodedStatementName = null;
        this.fields = null;
        this.resultSetColumnNameIndexMap = null;
        this.portalDescribed = false;
        this.statementDescribed = false;
        this.cachedMaxResultRowSize = null;
    }

    @Override
    public int getBatchSize() {
        return 1;
    }

    NativeQuery getNativeQuery() {
        return this.nativeQuery;
    }

    public final int getBindCount() {
        return this.nativeQuery.bindPositions.length * this.getBatchSize();
    }

    @Override
    public @Nullable Map<String, Integer> getResultSetColumnNameIndexMap() {
        Map<String, Integer> columnPositions = this.resultSetColumnNameIndexMap;
        if (columnPositions == null && this.fields != null) {
            columnPositions = PgResultSet.createColumnNameIndexMap(this.fields, this.sanitiserDisabled);
            if (this.statementName != null) {
                this.resultSetColumnNameIndexMap = columnPositions;
            }
        }
        return columnPositions;
    }

    @Override
    public SqlCommand getSqlCommand() {
        return this.nativeQuery.getCommand();
    }
}

