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
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.jdbc.ColumnValueRange;
import java.math.BigDecimal;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple2;
import scala.package$;

public final class ColumnValueRange$
implements Serializable {
    public static ColumnValueRange$ MODULE$;

    static {
        new ColumnValueRange$();
    }

    public ColumnValueRange apply(long min2, long max) {
        return new ColumnValueRange(package$.MODULE$.BigDecimal().apply(min2), package$.MODULE$.BigDecimal().apply(max));
    }

    public ColumnValueRange apply(String min2, String max) {
        return new ColumnValueRange(package$.MODULE$.BigDecimal().apply(min2), package$.MODULE$.BigDecimal().apply(max));
    }

    public ColumnValueRange apply(BigDecimal min2, BigDecimal max) {
        scala.math.BigDecimal lower = new scala.math.BigDecimal(min2);
        scala.math.BigDecimal upper = new scala.math.BigDecimal(max);
        return new ColumnValueRange(lower, upper);
    }

    public ColumnValueRange apply(scala.math.BigDecimal min2, scala.math.BigDecimal max) {
        return new ColumnValueRange(min2, max);
    }

    public Option<Tuple2<scala.math.BigDecimal, scala.math.BigDecimal>> unapply(ColumnValueRange x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple2((Object)x$0.min(), (Object)x$0.max()));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ColumnValueRange$() {
        MODULE$ = this;
    }
}

