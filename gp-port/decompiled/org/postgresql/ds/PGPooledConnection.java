/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ds;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGConnection;
import org.postgresql.PGStatement;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PGPooledConnection
implements PooledConnection {
    private final List<ConnectionEventListener> listeners = new LinkedList<ConnectionEventListener>();
    private @Nullable Connection con;
    private @Nullable ConnectionHandler last;
    private final boolean autoCommit;
    private final boolean isXA;
    private static final String[] fatalClasses = new String[]{"08", "53", "57P01", "57P02", "57P03", "58", "60", "99", "F0", "XX"};

    public PGPooledConnection(Connection con, boolean autoCommit, boolean isXA) {
        this.con = con;
        this.autoCommit = autoCommit;
        this.isXA = isXA;
    }

    public PGPooledConnection(Connection con, boolean autoCommit) {
        this(con, autoCommit, false);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
        this.listeners.add(connectionEventListener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
        this.listeners.remove(connectionEventListener);
    }

    @Override
    public void close() throws SQLException {
        if (this.last != null) {
            this.last.close();
            if (this.con != null && !this.con.isClosed() && !this.con.getAutoCommit()) {
                try {
                    this.con.rollback();
                }
                catch (SQLException sQLException) {
                    // empty catch block
                }
            }
        }
        if (this.con == null) {
            return;
        }
        try {
            this.con.close();
        }
        finally {
            this.con = null;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        ConnectionHandler handler;
        if (this.con == null) {
            PSQLException sqlException = new PSQLException(GT.tr("This PooledConnection has already been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
            this.fireConnectionFatalError(sqlException);
            throw sqlException;
        }
        try {
            if (this.last != null) {
                this.last.close();
                if (this.con != null) {
                    if (!this.con.getAutoCommit()) {
                        try {
                            this.con.rollback();
                        }
                        catch (SQLException sqlException) {
                            // empty catch block
                        }
                    }
                    this.con.clearWarnings();
                }
            }
            if (!this.isXA && this.con != null) {
                this.con.setAutoCommit(this.autoCommit);
            }
        }
        catch (SQLException sqlException) {
            this.fireConnectionFatalError(sqlException);
            throw (SQLException)sqlException.fillInStackTrace();
        }
        this.last = handler = new ConnectionHandler(Nullness.castNonNull(this.con));
        Connection proxyCon = (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class, PGConnection.class}, (InvocationHandler)handler);
        handler.setProxy(proxyCon);
        return proxyCon;
    }

    void fireConnectionClosed() {
        ConnectionEventListener[] local;
        ConnectionEvent evt = null;
        for (ConnectionEventListener listener : local = this.listeners.toArray(new ConnectionEventListener[0])) {
            if (evt == null) {
                evt = this.createConnectionEvent(null);
            }
            listener.connectionClosed(evt);
        }
    }

    void fireConnectionFatalError(SQLException e) {
        ConnectionEventListener[] local;
        ConnectionEvent evt = null;
        for (ConnectionEventListener listener : local = this.listeners.toArray(new ConnectionEventListener[0])) {
            if (evt == null) {
                evt = this.createConnectionEvent(e);
            }
            listener.connectionErrorOccurred(evt);
        }
    }

    protected ConnectionEvent createConnectionEvent(@Nullable SQLException e) {
        return e == null ? new ConnectionEvent(this) : new ConnectionEvent(this, e);
    }

    private static boolean isFatalState(@Nullable String state) {
        if (state == null) {
            return true;
        }
        if (state.length() < 2) {
            return true;
        }
        for (String fatalClass : fatalClasses) {
            if (!state.startsWith(fatalClass)) continue;
            return true;
        }
        return false;
    }

    private void fireConnectionError(SQLException e) {
        if (!PGPooledConnection.isFatalState(e.getSQLState())) {
            return;
        }
        this.fireConnectionFatalError(e);
    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
    }

    @Override
    public void addStatementEventListener(StatementEventListener listener) {
    }

    private class StatementHandler
    implements InvocationHandler {
        private @Nullable ConnectionHandler con;
        private @Nullable Statement st;

        StatementHandler(ConnectionHandler con, Statement st) {
            this.con = con;
            this.st = st;
        }

        @Override
        public @Nullable Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
            String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if (methodName.equals("toString")) {
                    return "Pooled statement wrapping physical statement " + this.st;
                }
                if (methodName.equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (methodName.equals("equals")) {
                    return proxy == args[0];
                }
                return method.invoke(this.st, args);
            }
            if (methodName.equals("isClosed")) {
                return this.st == null || this.st.isClosed();
            }
            if (methodName.equals("close")) {
                if (this.st == null || this.st.isClosed()) {
                    return null;
                }
                this.con = null;
                Statement oldSt = this.st;
                this.st = null;
                oldSt.close();
                return null;
            }
            if (this.st == null || this.st.isClosed()) {
                throw new PSQLException(GT.tr("Statement has been closed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
            }
            if (methodName.equals("getConnection")) {
                return Nullness.castNonNull(this.con).getProxy();
            }
            try {
                return method.invoke(this.st, args);
            }
            catch (InvocationTargetException ite) {
                Throwable te = ite.getTargetException();
                if (te instanceof SQLException) {
                    PGPooledConnection.this.fireConnectionError((SQLException)te);
                }
                throw te;
            }
        }
    }

    private class ConnectionHandler
    implements InvocationHandler {
        private @Nullable Connection con;
        private @Nullable Connection proxy;
        private boolean automatic = false;

        ConnectionHandler(Connection con) {
            this.con = con;
        }

        @Override
        public @Nullable Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
            String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if (methodName.equals("toString")) {
                    return "Pooled connection wrapping physical connection " + this.con;
                }
                if (methodName.equals("equals")) {
                    return proxy == args[0];
                }
                if (methodName.equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                try {
                    return method.invoke(this.con, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
            if (methodName.equals("isClosed")) {
                return this.con == null || this.con.isClosed();
            }
            if (methodName.equals("close")) {
                if (this.con == null) {
                    return null;
                }
                SQLException ex = null;
                if (!this.con.isClosed()) {
                    if (!PGPooledConnection.this.isXA && !this.con.getAutoCommit()) {
                        try {
                            this.con.rollback();
                        }
                        catch (SQLException e) {
                            ex = e;
                        }
                    }
                    this.con.clearWarnings();
                }
                this.con = null;
                this.proxy = null;
                PGPooledConnection.this.last = null;
                PGPooledConnection.this.fireConnectionClosed();
                if (ex != null) {
                    throw ex;
                }
                return null;
            }
            if (this.con == null || this.con.isClosed()) {
                throw new PSQLException(this.automatic ? GT.tr("Connection has been closed automatically because a new connection was opened for the same PooledConnection or the PooledConnection has been closed.", new Object[0]) : GT.tr("Connection has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
            }
            try {
                if (methodName.equals("createStatement")) {
                    Statement st = Nullness.castNonNull((Statement)method.invoke(this.con, args));
                    return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Statement.class, PGStatement.class}, (InvocationHandler)new StatementHandler(this, st));
                }
                if (methodName.equals("prepareCall")) {
                    Statement st = Nullness.castNonNull((Statement)method.invoke(this.con, args));
                    return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{CallableStatement.class, PGStatement.class}, (InvocationHandler)new StatementHandler(this, st));
                }
                if (methodName.equals("prepareStatement")) {
                    Statement st = Nullness.castNonNull((Statement)method.invoke(this.con, args));
                    return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{PreparedStatement.class, PGStatement.class}, (InvocationHandler)new StatementHandler(this, st));
                }
                return method.invoke(this.con, args);
            }
            catch (InvocationTargetException ite) {
                Throwable te = ite.getTargetException();
                if (te instanceof SQLException) {
                    PGPooledConnection.this.fireConnectionError((SQLException)te);
                }
                throw te;
            }
        }

        Connection getProxy() {
            return Nullness.castNonNull(this.proxy);
        }

        void setProxy(Connection proxy) {
            this.proxy = proxy;
        }

        public void close() {
            if (this.con != null) {
                this.automatic = true;
            }
            this.con = null;
            this.proxy = null;
        }

        public boolean isClosed() {
            return this.con == null;
        }
    }
}

