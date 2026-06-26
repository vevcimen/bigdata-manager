/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Field;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.Tuple;
import org.postgresql.core.v3.BatchedQuery;
import org.postgresql.jdbc.PgResultSet;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class BatchResultHandler
extends ResultHandlerBase {
    private final PgStatement pgStatement;
    private int resultIndex = 0;
    private final Query[] queries;
    private final long[] longUpdateCounts;
    private final @Nullable ParameterList @Nullable [] parameterLists;
    private final boolean expectGeneratedKeys;
    private @Nullable PgResultSet generatedKeys;
    private int committedRows;
    private final @Nullable List<List<Tuple>> allGeneratedRows;
    private @Nullable List<Tuple> latestGeneratedRows;
    private @Nullable PgResultSet latestGeneratedKeysRs;

    BatchResultHandler(PgStatement pgStatement, Query[] queries, @Nullable ParameterList @Nullable [] parameterLists, boolean expectGeneratedKeys) {
        this.pgStatement = pgStatement;
        this.queries = queries;
        this.parameterLists = parameterLists;
        this.longUpdateCounts = new long[queries.length];
        this.expectGeneratedKeys = expectGeneratedKeys;
        this.allGeneratedRows = !expectGeneratedKeys ? null : new ArrayList();
    }

    @Override
    public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
        ++this.resultIndex;
        if (!this.expectGeneratedKeys) {
            return;
        }
        if (this.generatedKeys == null) {
            try {
                this.latestGeneratedKeysRs = (PgResultSet)this.pgStatement.createResultSet(fromQuery, fields, new ArrayList<Tuple>(), cursor);
            }
            catch (SQLException e) {
                this.handleError(e);
            }
        }
        this.latestGeneratedRows = tuples;
    }

    @Override
    public void handleCommandStatus(String status, long updateCount, long insertOID) {
        List<Tuple> latestGeneratedRows = this.latestGeneratedRows;
        if (latestGeneratedRows != null) {
            --this.resultIndex;
            if (updateCount > 0L && (this.getException() == null || this.isAutoCommit())) {
                List<List<Tuple>> allGeneratedRows = Nullness.castNonNull(this.allGeneratedRows, "allGeneratedRows");
                allGeneratedRows.add(latestGeneratedRows);
                if (this.generatedKeys == null) {
                    this.generatedKeys = this.latestGeneratedKeysRs;
                }
            }
            this.latestGeneratedRows = null;
        }
        if (this.resultIndex >= this.queries.length) {
            this.handleError(new PSQLException(GT.tr("Too many update results were returned.", new Object[0]), PSQLState.TOO_MANY_RESULTS));
            return;
        }
        this.latestGeneratedKeysRs = null;
        this.longUpdateCounts[this.resultIndex++] = updateCount;
    }

    private boolean isAutoCommit() {
        try {
            return this.pgStatement.getConnection().getAutoCommit();
        }
        catch (SQLException e) {
            assert (false) : "pgStatement.getConnection().getAutoCommit() should not throw";
            return false;
        }
    }

    @Override
    public void secureProgress() {
        if (this.isAutoCommit()) {
            this.committedRows = this.resultIndex;
            this.updateGeneratedKeys();
        }
    }

    private void updateGeneratedKeys() {
        List<List<Tuple>> allGeneratedRows = this.allGeneratedRows;
        if (allGeneratedRows == null || allGeneratedRows.isEmpty()) {
            return;
        }
        PgResultSet generatedKeys = Nullness.castNonNull(this.generatedKeys, "generatedKeys");
        for (List<Tuple> rows : allGeneratedRows) {
            generatedKeys.addRows(rows);
        }
        allGeneratedRows.clear();
    }

    @Override
    public void handleWarning(SQLWarning warning) {
        this.pgStatement.addWarning(warning);
    }

    @Override
    public void handleError(SQLException newError) {
        if (this.getException() == null) {
            Arrays.fill(this.longUpdateCounts, this.committedRows, this.longUpdateCounts.length, -3L);
            if (this.allGeneratedRows != null) {
                this.allGeneratedRows.clear();
            }
            String queryString = "<unknown>";
            if (this.pgStatement.getPGConnection().getLogServerErrorDetail() && this.resultIndex < this.queries.length) {
                queryString = this.queries[this.resultIndex].toString(this.parameterLists == null ? null : this.parameterLists[this.resultIndex]);
            }
            BatchUpdateException batchException = new BatchUpdateException(GT.tr("Batch entry {0} {1} was aborted: {2}  Call getNextException to see other errors in the batch.", this.resultIndex, queryString, newError.getMessage()), newError.getSQLState(), 0, this.uncompressLongUpdateCount(), (Throwable)newError);
            super.handleError(batchException);
        }
        ++this.resultIndex;
        super.handleError(newError);
    }

    @Override
    public void handleCompletion() throws SQLException {
        this.updateGeneratedKeys();
        SQLException batchException = this.getException();
        if (batchException != null) {
            if (this.isAutoCommit()) {
                BatchUpdateException newException = new BatchUpdateException(batchException.getMessage(), batchException.getSQLState(), 0, this.uncompressLongUpdateCount(), batchException.getCause());
                SQLException next = batchException.getNextException();
                if (next != null) {
                    newException.setNextException(next);
                }
                batchException = newException;
            }
            throw batchException;
        }
    }

    public @Nullable ResultSet getGeneratedKeys() {
        return this.generatedKeys;
    }

    private int[] uncompressUpdateCount() {
        long[] original = this.uncompressLongUpdateCount();
        int[] copy = new int[original.length];
        for (int i = 0; i < original.length; ++i) {
            copy[i] = original[i] > Integer.MAX_VALUE ? -2 : (int)original[i];
        }
        return copy;
    }

    public int[] getUpdateCount() {
        return this.uncompressUpdateCount();
    }

    private long[] uncompressLongUpdateCount() {
        int batchSize;
        if (!(this.queries[0] instanceof BatchedQuery)) {
            return this.longUpdateCounts;
        }
        int totalRows = 0;
        boolean hasRewrites = false;
        for (Query query : this.queries) {
            batchSize = query.getBatchSize();
            totalRows += batchSize;
            hasRewrites |= batchSize > 1;
        }
        if (!hasRewrites) {
            return this.longUpdateCounts;
        }
        long[] newUpdateCounts = new long[totalRows];
        int offset = 0;
        for (int i = 0; i < this.queries.length; ++i) {
            Query query;
            query = this.queries[i];
            batchSize = query.getBatchSize();
            long superBatchResult = this.longUpdateCounts[i];
            if (batchSize == 1) {
                newUpdateCounts[offset++] = superBatchResult;
                continue;
            }
            if (superBatchResult > 0L) {
                superBatchResult = -2L;
            }
            Arrays.fill(newUpdateCounts, offset, offset + batchSize, superBatchResult);
            offset += batchSize;
        }
        return newUpdateCounts;
    }

    public long[] getLargeUpdateCount() {
        return this.uncompressLongUpdateCount();
    }
}

