/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.transaction.Transaction
 */
package resource.jta;

import javax.transaction.Transaction;
import resource.Resource;

public final class package$ {
    public static package$ MODULE$;

    static {
        new package$();
    }

    public <A extends Transaction> Resource<A> transactionSupport() {
        return new Resource<A>(){

            public void open(Object r) {
                Resource.open$(this, r);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                r.commit();
            }

            public void closeAfterException(A r, Throwable t) {
                r.rollback();
            }
            {
                Resource.$init$(this);
            }
        };
    }

    private package$() {
        MODULE$ = this;
    }
}

