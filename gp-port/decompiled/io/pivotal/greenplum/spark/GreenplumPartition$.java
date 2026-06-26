/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple2
 *  scala.runtime.AbstractFunction2
 *  scala.runtime.BoxesRunTime
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.GreenplumPartition;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple2;
import scala.runtime.AbstractFunction2;
import scala.runtime.BoxesRunTime;

public final class GreenplumPartition$
extends AbstractFunction2<String, Object, GreenplumPartition>
implements Serializable {
    public static GreenplumPartition$ MODULE$;

    static {
        new GreenplumPartition$();
    }

    public final String toString() {
        return "GreenplumPartition";
    }

    public GreenplumPartition apply(String whereClause, int idx) {
        return new GreenplumPartition(whereClause, idx);
    }

    public Option<Tuple2<String, Object>> unapply(GreenplumPartition x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple2((Object)x$0.whereClause(), (Object)BoxesRunTime.boxToInteger((int)x$0.idx())));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GreenplumPartition$() {
        MODULE$ = this;
    }
}

