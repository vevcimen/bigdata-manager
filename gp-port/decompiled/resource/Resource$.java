/*
 * Decompiled with CFR 0.152.
 */
package resource;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.PooledConnection;
import resource.LowPriorityResourceImplicits;
import resource.MediumPriorityResourceImplicits;
import resource.MediumPriorityResourceImplicits$HttpURLConnectionResource$;
import resource.MediumPriorityResourceImplicits$gzipOuputStraemResource$;
import resource.MediumPriorityResourceImplicits$jarFileResource$;
import resource.Resource;

public final class Resource$
implements MediumPriorityResourceImplicits {
    public static Resource$ MODULE$;
    private volatile MediumPriorityResourceImplicits$gzipOuputStraemResource$ gzipOuputStraemResource$module;
    private volatile MediumPriorityResourceImplicits$jarFileResource$ jarFileResource$module;
    private volatile MediumPriorityResourceImplicits$HttpURLConnectionResource$ HttpURLConnectionResource$module;

    static {
        new Resource$();
    }

    @Override
    public <A extends Closeable> Resource<A> closeableResource() {
        return MediumPriorityResourceImplicits.closeableResource$(this);
    }

    @Override
    public <A extends Connection> Resource<A> connectionResource() {
        return MediumPriorityResourceImplicits.connectionResource$(this);
    }

    @Override
    public <A extends Statement> Resource<A> statementResource() {
        return MediumPriorityResourceImplicits.statementResource$(this);
    }

    @Override
    public <A extends ResultSet> Resource<A> resultSetResource() {
        return MediumPriorityResourceImplicits.resultSetResource$(this);
    }

    @Override
    public <A extends PooledConnection> Resource<A> pooledConnectionResource() {
        return MediumPriorityResourceImplicits.pooledConnectionResource$(this);
    }

    @Override
    public <A> Resource<A> reflectiveCloseableResource() {
        return LowPriorityResourceImplicits.reflectiveCloseableResource$(this);
    }

    @Override
    public <A> Resource<A> reflectiveDisposableResource() {
        return LowPriorityResourceImplicits.reflectiveDisposableResource$(this);
    }

    @Override
    public MediumPriorityResourceImplicits$gzipOuputStraemResource$ gzipOuputStraemResource() {
        if (this.gzipOuputStraemResource$module == null) {
            this.gzipOuputStraemResource$lzycompute$1();
        }
        return this.gzipOuputStraemResource$module;
    }

    @Override
    public MediumPriorityResourceImplicits$jarFileResource$ jarFileResource() {
        if (this.jarFileResource$module == null) {
            this.jarFileResource$lzycompute$1();
        }
        return this.jarFileResource$module;
    }

    @Override
    public MediumPriorityResourceImplicits$HttpURLConnectionResource$ HttpURLConnectionResource() {
        if (this.HttpURLConnectionResource$module == null) {
            this.HttpURLConnectionResource$lzycompute$1();
        }
        return this.HttpURLConnectionResource$module;
    }

    private final void gzipOuputStraemResource$lzycompute$1() {
        Resource$ resource$ = this;
        synchronized (resource$) {
            if (this.gzipOuputStraemResource$module == null) {
                this.gzipOuputStraemResource$module = new MediumPriorityResourceImplicits$gzipOuputStraemResource$(null);
            }
        }
    }

    private final void jarFileResource$lzycompute$1() {
        Resource$ resource$ = this;
        synchronized (resource$) {
            if (this.jarFileResource$module == null) {
                this.jarFileResource$module = new MediumPriorityResourceImplicits$jarFileResource$(null);
            }
        }
    }

    private final void HttpURLConnectionResource$lzycompute$1() {
        Resource$ resource$ = this;
        synchronized (resource$) {
            if (this.HttpURLConnectionResource$module == null) {
                this.HttpURLConnectionResource$module = new MediumPriorityResourceImplicits$HttpURLConnectionResource$(null);
            }
        }
    }

    private Resource$() {
        MODULE$ = this;
        LowPriorityResourceImplicits.$init$(this);
        MediumPriorityResourceImplicits.$init$(this);
    }
}

