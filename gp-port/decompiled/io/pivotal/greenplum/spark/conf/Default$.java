/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.runtime.AbstractFunction1
 */
package io.pivotal.greenplum.spark.conf;

import io.pivotal.greenplum.spark.conf.Default;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.runtime.AbstractFunction1;

public final class Default$
extends AbstractFunction1<String, Default>
implements Serializable {
    public static Default$ MODULE$;

    static {
        new Default$();
    }

    public final String toString() {
        return "Default";
    }

    public Default apply(String value) {
        return new Default(value);
    }

    public Option<String> unapply(Default x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)x$0.value());
    }

    private Object readResolve() {
        return MODULE$;
    }

    private Default$() {
        MODULE$ = this;
    }
}

