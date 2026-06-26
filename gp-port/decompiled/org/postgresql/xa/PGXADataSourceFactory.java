/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.xa;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.ds.common.PGObjectFactory;
import org.postgresql.xa.PGXADataSource;

public class PGXADataSourceFactory
extends PGObjectFactory {
    @Override
    public @Nullable Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference)obj;
        String className = ref.getClassName();
        if (className.equals("org.postgresql.xa.PGXADataSource")) {
            return this.loadXADataSource(ref);
        }
        return null;
    }

    private Object loadXADataSource(Reference ref) {
        PGXADataSource ds = new PGXADataSource();
        return this.loadBaseDataSource(ds, ref);
    }
}

