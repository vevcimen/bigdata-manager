/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.types.StructField
 *  scala.Array$
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.mutable.ArrayBuffer
 *  scala.collection.mutable.ArrayOps$ofInt
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.math.BigDecimal
 *  scala.reflect.ClassTag$
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ObjectRef
 *  scala.runtime.RichInt$
 */
package io.pivotal.greenplum.spark;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.Partitioner;
import io.pivotal.greenplum.spark.jdbc.ColumnValueRange;
import java.io.Serializable;
import org.apache.spark.sql.types.StructField;
import scala.Array$;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.ArrayOps;
import scala.math.BigDecimal;
import scala.reflect.ClassTag$;
import scala.runtime.BoxesRunTime;
import scala.runtime.ObjectRef;
import scala.runtime.RichInt$;

public final class Partitioner$
implements LazyLogging {
    public static Partitioner$ MODULE$;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new Partitioner$();
    }

    private Logger logger$lzycompute() {
        Partitioner$ partitioner$ = this;
        synchronized (partitioner$) {
            if (!this.bitmap$trans$0) {
                this.logger = LazyLogging.logger$(this);
                this.bitmap$trans$0 = true;
            }
        }
        return this.logger;
    }

    @Override
    public Logger logger() {
        if (!this.bitmap$trans$0) {
            return this.logger$lzycompute();
        }
        return this.logger;
    }

    public GreenplumPartition[] segmentPartitions(int[] segmentIds) {
        return (GreenplumPartition[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofInt(Predef$.MODULE$.intArrayOps(segmentIds)).zipWithIndex(Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Tuple2.class))))).map((Function1 & Serializable & scala.Serializable)x0$1 -> {
            Tuple2 tuple2 = x0$1;
            if (tuple2 != null) {
                int segId = tuple2._1$mcI$sp();
                int index = tuple2._2$mcI$sp();
                return new GreenplumPartition(new StringBuilder(16).append("gp_segment_id = ").append(segId).toString(), index);
            }
            throw new MatchError((Object)tuple2);
        }, Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(GreenplumPartition.class)));
    }

    public GreenplumPartition[] columnPartitions(int requestedNumberOfPartitions, StructField partitionColumn, ColumnValueRange valueRange) {
        Partitioner p = new Partitioner(requestedNumberOfPartitions, partitionColumn, valueRange);
        if (Partitioner$.onlyOnePartition$1(partitionColumn, valueRange)) {
            return (GreenplumPartition[])((Object[])new GreenplumPartition[]{new GreenplumPartition("1 = 1", 0)});
        }
        BigDecimal lowerBound = valueRange.min();
        BigDecimal upperBound = valueRange.max();
        Predef$.MODULE$.require(lowerBound.$less$eq((Object)upperBound), (Function0 & Serializable & scala.Serializable)() -> new StringBuilder(122).append("Operation not allowed: the lower bound of partitioning column is larger than the upper ").append("bound. Lower bound: ").append(lowerBound).append("; Upper bound: ").append(upperBound).toString());
        ObjectRef partitionArrayBuf = ObjectRef.create((Object)new ArrayBuffer());
        RichInt$.MODULE$.until$extension0(Predef$.MODULE$.intWrapper(0), p.numberOfPartitions()).foreach((Function1 & Serializable & scala.Serializable)partitionIndex -> Partitioner$.$anonfun$columnPartitions$2(p, partitionArrayBuf, BoxesRunTime.unboxToInt((Object)partitionIndex)));
        return (GreenplumPartition[])((ArrayBuffer)partitionArrayBuf.elem).toArray(ClassTag$.MODULE$.apply(GreenplumPartition.class));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static final boolean onlyOnePartition$1(StructField partitionColumn$1, ColumnValueRange valueRange$1) {
        if (partitionColumn$1 == null) return true;
        if (valueRange$1 == null) return true;
        BigDecimal bigDecimal = valueRange$1.min();
        BigDecimal bigDecimal2 = valueRange$1.max();
        if (bigDecimal != null) {
            if (!bigDecimal.equals(bigDecimal2)) return false;
            return true;
        }
        if (bigDecimal2 == null) return true;
        return false;
    }

    public static final /* synthetic */ ArrayBuffer $anonfun$columnPartitions$2(Partitioner p$1, ObjectRef partitionArrayBuf$1, int partitionIndex) {
        String whereClause = p$1.createSqlPredicate(partitionIndex);
        return ((ArrayBuffer)partitionArrayBuf$1.elem).$plus$eq((Object)new GreenplumPartition(whereClause, partitionIndex));
    }

    private Partitioner$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
    }
}

