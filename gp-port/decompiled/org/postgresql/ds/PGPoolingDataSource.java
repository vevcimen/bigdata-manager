/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

@Deprecated
public class PGPoolingDataSource
extends BaseDataSource
implements DataSource {
    protected static ConcurrentMap<String, PGPoolingDataSource> dataSources = new ConcurrentHashMap<String, PGPoolingDataSource>();
    protected @Nullable String dataSourceName;
    private int initialConnections = 0;
    private int maxConnections = 0;
    private boolean initialized = false;
    private final Stack<PooledConnection> available = new Stack();
    private final Stack<PooledConnection> used = new Stack();
    private boolean isClosed;
    private final Object lock = new Object();
    private @Nullable PGConnectionPoolDataSource source;
    private final ConnectionEventListener connectionEventListener = new ConnectionEventListener(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void connectionClosed(ConnectionEvent event) {
            ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
            Object object = PGPoolingDataSource.this.lock;
            synchronized (object) {
                if (PGPoolingDataSource.this.isClosed) {
                    return;
                }
                boolean removed = PGPoolingDataSource.this.used.remove(event.getSource());
                if (removed) {
                    PGPoolingDataSource.this.available.push((PooledConnection)event.getSource());
                    PGPoolingDataSource.this.lock.notify();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void connectionErrorOccurred(ConnectionEvent event) {
            ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
            Object object = PGPoolingDataSource.this.lock;
            synchronized (object) {
                if (PGPoolingDataSource.this.isClosed) {
                    return;
                }
                PGPoolingDataSource.this.used.remove(event.getSource());
                PGPoolingDataSource.this.lock.notify();
            }
        }
    };

    public static @Nullable PGPoolingDataSource getDataSource(String name) {
        return (PGPoolingDataSource)dataSources.get(name);
    }

    @Override
    public String getDescription() {
        return "Pooling DataSource '" + this.dataSourceName + " from " + "PostgreSQL JDBC Driver 42.4.3";
    }

    @Override
    public void setServerName(String serverName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setServerName(serverName);
    }

    @Override
    public void setDatabaseName(@Nullable String databaseName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setDatabaseName(databaseName);
    }

    @Override
    public void setUser(@Nullable String user) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setUser(user);
    }

    @Override
    public void setPassword(@Nullable String password) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setPassword(password);
    }

    @Override
    public void setPortNumber(int portNumber) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setPortNumber(portNumber);
    }

    public int getInitialConnections() {
        return this.initialConnections;
    }

    public void setInitialConnections(int initialConnections) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        this.initialConnections = initialConnections;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        this.maxConnections = maxConnections;
    }

    public @Nullable String getDataSourceName() {
        return this.dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        if (this.dataSourceName != null && dataSourceName != null && dataSourceName.equals(this.dataSourceName)) {
            return;
        }
        PGPoolingDataSource previous = dataSources.putIfAbsent(dataSourceName, this);
        if (previous != null) {
            throw new IllegalArgumentException("DataSource with name '" + dataSourceName + "' already exists!");
        }
        if (this.dataSourceName != null) {
            dataSources.remove(this.dataSourceName);
        }
        this.dataSourceName = dataSourceName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void initialize() throws SQLException {
        Object object = this.lock;
        synchronized (object) {
            PGConnectionPoolDataSource source;
            this.source = source = this.createConnectionPool();
            try {
                source.initializeFrom(this);
            }
            catch (Exception e) {
                throw new PSQLException(GT.tr("Failed to setup DataSource.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)e);
            }
            while (this.available.size() < this.initialConnections) {
                this.available.push(source.getPooledConnection());
            }
            this.initialized = true;
        }
    }

    protected boolean isInitialized() {
        return this.initialized;
    }

    protected PGConnectionPoolDataSource createConnectionPool() {
        return new PGConnectionPoolDataSource();
    }

    @Override
    public Connection getConnection(@Nullable String user, @Nullable String password) throws SQLException {
        if (user == null || user.equals(this.getUser()) && (password == null && this.getPassword() == null || password != null && password.equals(this.getPassword()))) {
            return this.getConnection();
        }
        if (!this.initialized) {
            this.initialize();
        }
        return super.getConnection(user, password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (!this.initialized) {
            this.initialize();
        }
        return this.getPooledConnection();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        Object object = this.lock;
        synchronized (object) {
            PooledConnection pci;
            this.isClosed = true;
            while (!this.available.isEmpty()) {
                pci = this.available.pop();
                try {
                    pci.close();
                }
                catch (SQLException sQLException) {}
            }
            while (!this.used.isEmpty()) {
                pci = this.used.pop();
                pci.removeConnectionEventListener(this.connectionEventListener);
                try {
                    pci.close();
                }
                catch (SQLException sQLException) {}
            }
        }
        this.removeStoredDataSource();
    }

    protected void removeStoredDataSource() {
        dataSources.remove(Nullness.castNonNull(this.dataSourceName));
    }

    protected void addDataSource(String dataSourceName) {
        dataSources.put(dataSourceName, this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Connection getPooledConnection() throws SQLException {
        PooledConnection pc = null;
        Object object = this.lock;
        synchronized (object) {
            if (this.isClosed) {
                throw new PSQLException(GT.tr("DataSource has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
            }
            while (true) {
                if (!this.available.isEmpty()) {
                    pc = this.available.pop();
                    this.used.push(pc);
                    break;
                }
                if (this.maxConnections == 0 || this.used.size() < this.maxConnections) {
                    pc = Nullness.castNonNull(this.source).getPooledConnection();
                    this.used.push(pc);
                    break;
                }
                try {
                    this.lock.wait(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
        pc.addConnectionEventListener(this.connectionEventListener);
        return pc.getConnection();
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = super.getReference();
        ref.add(new StringRefAddr("dataSourceName", this.dataSourceName));
        if (this.initialConnections > 0) {
            ref.add(new StringRefAddr("initialConnections", Integer.toString(this.initialConnections)));
        }
        if (this.maxConnections > 0) {
            ref.add(new StringRefAddr("maxConnections", Integer.toString(this.maxConnections)));
        }
        return ref;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
}

