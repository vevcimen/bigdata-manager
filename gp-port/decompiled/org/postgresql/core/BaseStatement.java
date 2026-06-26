/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.postgresql.PGStatement;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.Field;
import org.postgresql.core.Query;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.Tuple;

public interface BaseStatement
extends PGStatement,
Statement {
    public ResultSet createDriverResultSet(Field[] var1, List<Tuple> var2) throws SQLException;

    public ResultSet createResultSet(Query var1, Field[] var2, List<Tuple> var3, ResultCursor var4) throws SQLException;

    public boolean executeWithFlags(String var1, int var2) throws SQLException;

    public boolean executeWithFlags(CachedQuery var1, int var2) throws SQLException;

    public boolean executeWithFlags(int var1) throws SQLException;
}

