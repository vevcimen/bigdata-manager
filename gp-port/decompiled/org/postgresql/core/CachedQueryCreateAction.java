/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.sql.SQLException;
import java.util.List;
import org.postgresql.core.BaseQueryKey;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.CallableQueryKey;
import org.postgresql.core.JdbcCallParseInfo;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.Parser;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.QueryWithReturningColumnsKey;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.util.LruCache;
import org.postgresql.util.internal.Nullness;

class CachedQueryCreateAction
implements LruCache.CreateAction<Object, CachedQuery> {
    private static final String[] EMPTY_RETURNING = new String[0];
    private final QueryExecutor queryExecutor;

    CachedQueryCreateAction(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public CachedQuery create(Object key) throws SQLException {
        boolean isFunction;
        String parsedSql;
        BaseQueryKey queryKey;
        assert (key instanceof String || key instanceof BaseQueryKey) : "Query key should be String or BaseQueryKey. Given " + key.getClass() + ", sql: " + key;
        if (key instanceof BaseQueryKey) {
            queryKey = (BaseQueryKey)key;
            parsedSql = queryKey.sql;
        } else {
            queryKey = null;
            parsedSql = (String)key;
        }
        if (key instanceof String || Nullness.castNonNull(queryKey).escapeProcessing) {
            parsedSql = Parser.replaceProcessing(parsedSql, true, this.queryExecutor.getStandardConformingStrings());
        }
        if (key instanceof CallableQueryKey) {
            JdbcCallParseInfo callInfo = Parser.modifyJdbcCall(parsedSql, this.queryExecutor.getStandardConformingStrings(), this.queryExecutor.getServerVersionNum(), this.queryExecutor.getProtocolVersion(), this.queryExecutor.getEscapeSyntaxCallMode());
            parsedSql = callInfo.getSql();
            isFunction = callInfo.isFunction();
        } else {
            isFunction = false;
        }
        boolean isParameterized = key instanceof String || Nullness.castNonNull(queryKey).isParameterized;
        boolean splitStatements = isParameterized || this.queryExecutor.getPreferQueryMode().compareTo(PreferQueryMode.EXTENDED) >= 0;
        String[] returningColumns = key instanceof QueryWithReturningColumnsKey ? ((QueryWithReturningColumnsKey)key).columnNames : EMPTY_RETURNING;
        List<NativeQuery> queries = Parser.parseJdbcSql(parsedSql, this.queryExecutor.getStandardConformingStrings(), isParameterized, splitStatements, this.queryExecutor.isReWriteBatchedInsertsEnabled(), this.queryExecutor.getQuoteReturningIdentifiers(), returningColumns);
        Query query = this.queryExecutor.wrap(queries);
        return new CachedQuery(key, query, isFunction);
    }
}

