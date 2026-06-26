/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql;

import java.sql.SQLException;

public interface PGStatement {
    public static final long DATE_POSITIVE_INFINITY = 9223372036825200000L;
    public static final long DATE_NEGATIVE_INFINITY = -9223372036832400000L;
    public static final long DATE_POSITIVE_SMALLER_INFINITY = 185543533774800000L;
    public static final long DATE_NEGATIVE_SMALLER_INFINITY = -185543533774800000L;

    public long getLastOID() throws SQLException;

    @Deprecated
    public void setUseServerPrepare(boolean var1) throws SQLException;

    public boolean isUseServerPrepare();

    public void setPrepareThreshold(int var1) throws SQLException;

    public int getPrepareThreshold();

    public void setAdaptiveFetch(boolean var1);

    public boolean getAdaptiveFetch();
}

