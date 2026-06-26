/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ds.common;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.util.internal.Nullness;

public class PGObjectFactory
implements ObjectFactory {
    @Override
    public @Nullable Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference)obj;
        String className = ref.getClassName();
        if (className.equals("org.postgresql.ds.PGSimpleDataSource") || className.equals("org.postgresql.jdbc2.optional.SimpleDataSource") || className.equals("org.postgresql.jdbc3.Jdbc3SimpleDataSource")) {
            return this.loadSimpleDataSource(ref);
        }
        if (className.equals("org.postgresql.ds.PGConnectionPoolDataSource") || className.equals("org.postgresql.jdbc2.optional.ConnectionPool") || className.equals("org.postgresql.jdbc3.Jdbc3ConnectionPool")) {
            return this.loadConnectionPool(ref);
        }
        if (className.equals("org.postgresql.ds.PGPoolingDataSource") || className.equals("org.postgresql.jdbc2.optional.PoolingDataSource") || className.equals("org.postgresql.jdbc3.Jdbc3PoolingDataSource")) {
            return this.loadPoolingDataSource(ref);
        }
        return null;
    }

    private Object loadPoolingDataSource(Reference ref) {
        String max;
        String name = Nullness.castNonNull(this.getProperty(ref, "dataSourceName"));
        PGPoolingDataSource pds = PGPoolingDataSource.getDataSource(name);
        if (pds != null) {
            return pds;
        }
        pds = new PGPoolingDataSource();
        pds.setDataSourceName(name);
        this.loadBaseDataSource(pds, ref);
        String min2 = this.getProperty(ref, "initialConnections");
        if (min2 != null) {
            pds.setInitialConnections(Integer.parseInt(min2));
        }
        if ((max = this.getProperty(ref, "maxConnections")) != null) {
            pds.setMaxConnections(Integer.parseInt(max));
        }
        return pds;
    }

    private Object loadSimpleDataSource(Reference ref) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        return this.loadBaseDataSource(ds, ref);
    }

    private Object loadConnectionPool(Reference ref) {
        PGConnectionPoolDataSource cp = new PGConnectionPoolDataSource();
        return this.loadBaseDataSource(cp, ref);
    }

    protected Object loadBaseDataSource(BaseDataSource ds, Reference ref) {
        ds.setFromReference(ref);
        return ds;
    }

    protected @Nullable String getProperty(Reference ref, String s2) {
        RefAddr addr = ref.get(s2);
        if (addr == null) {
            return null;
        }
        return (String)addr.getContent();
    }
}

