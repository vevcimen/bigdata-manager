/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple2
 *  scala.math.BigDecimal
 *  scala.package$
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.RDDPartitionBounds;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple2;
import scala.math.BigDecimal;
import scala.package$;

public final class RDDPartitionBounds$
implements Serializable {
    public static RDDPartitionBounds$ MODULE$;

    static {
        new RDDPartitionBounds$();
    }

    public RDDPartitionBounds apply(long min2, long max) {
        return new RDDPartitionBounds(package$.MODULE$.BigDecimal().apply(min2), package$.MODULE$.BigDecimal().apply(max));
    }

    public RDDPartitionBounds apply(String min2, String max) {
        return new RDDPartitionBounds(package$.MODULE$.BigDecimal().apply(min2), package$.MODULE$.BigDecimal().apply(max));
    }

    public RDDPartitionBounds apply(BigDecimal lower, BigDecimal upper) {
        return new RDDPartitionBounds(lower, upper);
    }

    public Option<Tuple2<BigDecimal, BigDecimal>> unapply(RDDPartitionBounds x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple2((Object)x$0.lower(), (Object)x$0.upper()));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private RDDPartitionBounds$() {
        MODULE$ = this;
    }
}

