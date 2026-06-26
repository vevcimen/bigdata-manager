/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.ConnectionStatistics;
import org.eclipse.jetty.util.IncludeExcludeSet;

public class IncludeExcludeConnectionStatistics
extends ConnectionStatistics {
    private final IncludeExcludeSet<Class<? extends Connection>, Connection> _set = new IncludeExcludeSet(ConnectionSet.class);

    public void include(String className) throws ClassNotFoundException {
        this._set.include(this.connectionForName(className));
    }

    public void include(Class<? extends Connection> clazz) {
        this._set.include(clazz);
    }

    public void exclude(String className) throws ClassNotFoundException {
        this._set.exclude(this.connectionForName(className));
    }

    public void exclude(Class<? extends Connection> clazz) {
        this._set.exclude(clazz);
    }

    private Class<? extends Connection> connectionForName(String className) throws ClassNotFoundException {
        Class<?> aClass = Class.forName(className);
        if (!Connection.class.isAssignableFrom(aClass)) {
            throw new IllegalArgumentException("Class is not a Connection");
        }
        Class<?> connectionClass = aClass;
        return connectionClass;
    }

    @Override
    public void onOpened(Connection connection) {
        if (this._set.test(connection)) {
            super.onOpened(connection);
        }
    }

    @Override
    public void onClosed(Connection connection) {
        if (this._set.test(connection)) {
            super.onClosed(connection);
        }
    }

    public static class ConnectionSet
    extends AbstractSet<Class<? extends Connection>>
    implements Predicate<Connection> {
        private final Set<Class<? extends Connection>> set = new HashSet<Class<? extends Connection>>();

        @Override
        public boolean add(Class<? extends Connection> aClass) {
            return this.set.add(aClass);
        }

        @Override
        public boolean remove(Object o) {
            return this.set.remove(o);
        }

        @Override
        public Iterator<Class<? extends Connection>> iterator() {
            return this.set.iterator();
        }

        @Override
        public int size() {
            return this.set.size();
        }

        @Override
        public boolean test(Connection connection) {
            if (connection == null) {
                return false;
            }
            return this.set.stream().anyMatch(c -> c.isAssignableFrom(connection.getClass()));
        }
    }
}

