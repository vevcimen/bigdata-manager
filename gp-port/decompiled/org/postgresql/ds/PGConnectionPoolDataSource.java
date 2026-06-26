/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.postgresql.ds.PGPooledConnection;
import org.postgresql.ds.common.BaseDataSource;

public class PGConnectionPoolDataSource
extends BaseDataSource
implements ConnectionPoolDataSource,
Serializable {
    private boolean defaultAutoCommit = true;

    @Override
    public String getDescription() {
        return "ConnectionPoolDataSource from PostgreSQL JDBC Driver 42.4.3";
    }

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return new PGPooledConnection(this.getConnection(), this.defaultAutoCommit);
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return new PGPooledConnection(this.getConnection(user, password), this.defaultAutoCommit);
    }

    public boolean isDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeBaseObject(out);
        out.writeBoolean(this.defaultAutoCommit);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.readBaseObject(in);
        this.defaultAutoCommit = in.readBoolean();
    }
}

