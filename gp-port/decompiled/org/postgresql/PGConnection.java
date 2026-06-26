/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGNotification;
import org.postgresql.copy.CopyManager;
import org.postgresql.fastpath.Fastpath;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.util.PGobject;

public interface PGConnection {
    public Array createArrayOf(String var1, @Nullable Object var2) throws SQLException;

    public PGNotification[] getNotifications() throws SQLException;

    public PGNotification[] getNotifications(int var1) throws SQLException;

    public CopyManager getCopyAPI() throws SQLException;

    public LargeObjectManager getLargeObjectAPI() throws SQLException;

    @Deprecated
    public Fastpath getFastpathAPI() throws SQLException;

    @Deprecated
    public void addDataType(String var1, String var2);

    public void addDataType(String var1, Class<? extends PGobject> var2) throws SQLException;

    public void setPrepareThreshold(int var1);

    public int getPrepareThreshold();

    public void setDefaultFetchSize(int var1) throws SQLException;

    public int getDefaultFetchSize();

    public int getBackendPID();

    public void cancelQuery() throws SQLException;

    public String escapeIdentifier(String var1) throws SQLException;

    public String escapeLiteral(String var1) throws SQLException;

    public PreferQueryMode getPreferQueryMode();

    public AutoSave getAutosave();

    public void setAutosave(AutoSave var1);

    public PGReplicationConnection getReplicationAPI();

    public Map<String, String> getParameterStatuses();

    public @Nullable String getParameterStatus(String var1);

    public void setAdaptiveFetch(boolean var1);

    public boolean getAdaptiveFetch();
}

