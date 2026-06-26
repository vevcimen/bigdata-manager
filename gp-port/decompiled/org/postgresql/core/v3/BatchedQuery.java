/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.ParameterList;
import org.postgresql.core.v3.SimpleQuery;
import org.postgresql.core.v3.TypeTransferModeRegistry;

public class BatchedQuery
extends SimpleQuery {
    private @Nullable String sql;
    private final int valuesBraceOpenPosition;
    private final int valuesBraceClosePosition;
    private final int batchSize;
    private BatchedQuery @Nullable [] blocks;

    public BatchedQuery(NativeQuery query, TypeTransferModeRegistry transferModeRegistry, int valuesBraceOpenPosition, int valuesBraceClosePosition, boolean sanitiserDisabled) {
        super(query, transferModeRegistry, sanitiserDisabled);
        this.valuesBraceOpenPosition = valuesBraceOpenPosition;
        this.valuesBraceClosePosition = valuesBraceClosePosition;
        this.batchSize = 1;
    }

    private BatchedQuery(BatchedQuery src, int batchSize) {
        super(src);
        this.valuesBraceOpenPosition = src.valuesBraceOpenPosition;
        this.valuesBraceClosePosition = src.valuesBraceClosePosition;
        this.batchSize = batchSize;
    }

    public BatchedQuery deriveForMultiBatch(int valueBlock) {
        BatchedQuery bq;
        if (this.getBatchSize() != 1) {
            throw new IllegalStateException("Only the original decorator can be derived.");
        }
        if (valueBlock == 1) {
            return this;
        }
        int index = Integer.numberOfTrailingZeros(valueBlock) - 1;
        if (valueBlock > 128 || valueBlock != 1 << index + 1) {
            throw new IllegalArgumentException("Expected value block should be a power of 2 smaller or equal to 128. Actual block is " + valueBlock);
        }
        if (this.blocks == null) {
            this.blocks = new BatchedQuery[7];
        }
        if ((bq = this.blocks[index]) == null) {
            this.blocks[index] = bq = new BatchedQuery(this, valueBlock);
        }
        return bq;
    }

    @Override
    public int getBatchSize() {
        return this.batchSize;
    }

    @Override
    public String getNativeSql() {
        if (this.sql != null) {
            return this.sql;
        }
        this.sql = this.buildNativeSql(null);
        return this.sql;
    }

    private String buildNativeSql(@Nullable ParameterList params) {
        int pos;
        String sql = null;
        String nativeSql = super.getNativeSql();
        int batchSize = this.getBatchSize();
        if (batchSize < 2) {
            sql = nativeSql;
            return sql;
        }
        if (nativeSql == null) {
            sql = "";
            return sql;
        }
        int valuesBlockCharCount = 0;
        int[] bindPositions = this.getNativeQuery().bindPositions;
        int[] chunkStart = new int[1 + bindPositions.length];
        int[] chunkEnd = new int[1 + bindPositions.length];
        chunkStart[0] = this.valuesBraceOpenPosition;
        if (bindPositions.length == 0) {
            valuesBlockCharCount = this.valuesBraceClosePosition - this.valuesBraceOpenPosition + 1;
            chunkEnd[0] = this.valuesBraceClosePosition + 1;
        } else {
            chunkEnd[0] = bindPositions[0];
            valuesBlockCharCount += chunkEnd[0] - chunkStart[0];
            for (int i = 0; i < bindPositions.length; ++i) {
                int startIndex;
                int endIndex;
                int n = endIndex = i < bindPositions.length - 1 ? bindPositions[i + 1] : this.valuesBraceClosePosition + 1;
                for (startIndex = bindPositions[i] + 2; startIndex < endIndex && Character.isDigit(nativeSql.charAt(startIndex)); ++startIndex) {
                }
                chunkStart[i + 1] = startIndex;
                chunkEnd[i + 1] = endIndex;
                valuesBlockCharCount += chunkEnd[i + 1] - chunkStart[i + 1];
            }
        }
        int length = nativeSql.length();
        length += NativeQuery.calculateBindLength(bindPositions.length * batchSize);
        length -= NativeQuery.calculateBindLength(bindPositions.length);
        StringBuilder s2 = new StringBuilder(length += (valuesBlockCharCount + 1) * (batchSize - 1));
        if (bindPositions.length > 0 && params == null) {
            s2.append(nativeSql, 0, this.valuesBraceClosePosition + 1);
            pos = bindPositions.length + 1;
        } else {
            pos = 1;
            ++batchSize;
            s2.append(nativeSql, 0, this.valuesBraceOpenPosition);
        }
        for (int i = 2; i <= batchSize; ++i) {
            if (i > 2 || pos != 1) {
                s2.append(',');
            }
            s2.append(nativeSql, chunkStart[0], chunkEnd[0]);
            for (int j = 1; j < chunkStart.length; ++j) {
                if (params == null) {
                    NativeQuery.appendBindName(s2, pos++);
                } else {
                    s2.append(params.toString(pos++, true));
                }
                s2.append(nativeSql, chunkStart[j], chunkEnd[j]);
            }
        }
        s2.append(nativeSql, this.valuesBraceClosePosition + 1, nativeSql.length());
        sql = s2.toString();
        assert (params != null || s2.length() == length) : "Predicted length != actual: " + length + " !=" + s2.length();
        return sql;
    }

    @Override
    public String toString(@Nullable ParameterList params) {
        if (this.getBatchSize() < 2) {
            return super.toString(params);
        }
        return this.buildNativeSql(params);
    }
}

