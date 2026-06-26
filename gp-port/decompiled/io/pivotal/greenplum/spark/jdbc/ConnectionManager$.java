/*
 * Decompiled with CFR 0.152.
 */
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionManager;
import io.pivotal.greenplum.spark.jdbc.HikariProvider$;
import java.sql.Connection;

public final class ConnectionManager$ {
    public static ConnectionManager$ MODULE$;
    private ConnectionManager connectionManager;
    private volatile boolean bitmap$0;

    static {
        new ConnectionManager$();
    }

    private ConnectionManager connectionManager$lzycompute() {
        ConnectionManager$ connectionManager$ = this;
        synchronized (connectionManager$) {
            if (!this.bitmap$0) {
                this.connectionManager = new ConnectionManager(HikariProvider$.MODULE$);
                this.bitmap$0 = true;
            }
        }
        return this.connectionManager;
    }

    public ConnectionManager connectionManager() {
        if (!this.bitmap$0) {
            return this.connectionManager$lzycompute();
        }
        return this.connectionManager;
    }

    public Connection getConnection(GreenplumOptions dbOpts, boolean autoCommit) {
        return this.connectionManager().getConnection(dbOpts, autoCommit);
    }

    public boolean getConnection$default$2() {
        return true;
    }

    private ConnectionManager$() {
        MODULE$ = this;
    }
}

