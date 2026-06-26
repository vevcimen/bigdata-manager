/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.Row
 *  scala.Function1
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple4
 *  scala.collection.Iterator
 *  scala.collection.immutable.List
 *  scala.runtime.AbstractFunction4
 *  scala.runtime.BoxesRunTime
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.PartitionData;
import io.pivotal.greenplum.spark.externaltable.RowTransformer$;
import org.apache.spark.sql.Row;
import scala.Function1;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple4;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.runtime.AbstractFunction4;
import scala.runtime.BoxesRunTime;

public final class PartitionData$
extends AbstractFunction4<Object, Iterator<Row>, List<Row>, Function1<Row, Row>, PartitionData>
implements Serializable {
    public static PartitionData$ MODULE$;

    static {
        new PartitionData$();
    }

    public Iterator<Row> $lessinit$greater$default$2() {
        return null;
    }

    public List<Row> $lessinit$greater$default$3() {
        return null;
    }

    public Function1<Row, Row> $lessinit$greater$default$4() {
        return RowTransformer$.MODULE$.identityFunction();
    }

    public final String toString() {
        return "PartitionData";
    }

    public PartitionData apply(int partitionIndex, Iterator<Row> rowIterator, List<Row> rows, Function1<Row, Row> rowTransformer) {
        return new PartitionData(partitionIndex, rowIterator, rows, rowTransformer);
    }

    public Iterator<Row> apply$default$2() {
        return null;
    }

    public List<Row> apply$default$3() {
        return null;
    }

    public Function1<Row, Row> apply$default$4() {
        return RowTransformer$.MODULE$.identityFunction();
    }

    public Option<Tuple4<Object, Iterator<Row>, List<Row>, Function1<Row, Row>>> unapply(PartitionData x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple4((Object)BoxesRunTime.boxToInteger((int)x$0.partitionIndex()), x$0.rowIterator(), x$0.rows(), x$0.rowTransformer()));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private PartitionData$() {
        MODULE$ = this;
    }
}

