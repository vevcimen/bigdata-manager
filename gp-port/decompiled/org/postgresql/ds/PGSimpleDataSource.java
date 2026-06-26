/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.postgresql.ds.common.BaseDataSource;

public class PGSimpleDataSource
extends BaseDataSource
implements DataSource,
Serializable {
    @Override
    public String getDescription() {
        return "Non-Pooling DataSource from PostgreSQL JDBC Driver 42.4.3";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeBaseObject(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.readBaseObject(in);
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

