/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.immutable.List
 *  scala.runtime.AbstractFunction1
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.ServiceKey;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.collection.immutable.List;
import scala.runtime.AbstractFunction1;

public final class ServiceKey$
extends AbstractFunction1<List<Object>, ServiceKey>
implements Serializable {
    public static ServiceKey$ MODULE$;

    static {
        new ServiceKey$();
    }

    public final String toString() {
        return "ServiceKey";
    }

    public ServiceKey apply(List<Object> port) {
        return new ServiceKey(port);
    }

    public Option<List<Object>> unapply(ServiceKey x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some(x$0.port());
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ServiceKey$() {
        MODULE$ = this;
    }
}

