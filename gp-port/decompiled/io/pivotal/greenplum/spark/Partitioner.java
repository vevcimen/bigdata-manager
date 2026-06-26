/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.IntegerType$
 *  org.apache.spark.sql.types.LongType$
 *  org.apache.spark.sql.types.NumericType
 *  org.apache.spark.sql.types.ShortType$
 *  org.apache.spark.sql.types.StructField
 *  scala.math.BigDecimal
 *  scala.math.BigDecimal$
 *  scala.math.BigDecimal$RoundingMode$
 *  scala.package$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 */
package io.pivotal.greenplum.spark;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.Partitioner$;
import io.pivotal.greenplum.spark.RDDPartitionBounds;
import io.pivotal.greenplum.spark.externaltable.SqlObjectNameUtils$;
import io.pivotal.greenplum.spark.jdbc.ColumnValueRange;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.IntegerType$;
import org.apache.spark.sql.types.LongType$;
import org.apache.spark.sql.types.NumericType;
import org.apache.spark.sql.types.ShortType$;
import org.apache.spark.sql.types.StructField;
import scala.math.BigDecimal;
import scala.math.BigDecimal$;
import scala.package$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;

@ScalaSignature(bytes="\u0006\u0001\u0005\ra\u0001\u0002\b\u0010\u0001aA\u0001\"\u000b\u0001\u0003\u0002\u0003\u0006IA\u000b\u0005\t[\u0001\u0011\t\u0011)A\u0005]!A1\b\u0001B\u0001B\u0003%A\bC\u0003C\u0001\u0011\u00051\t\u0003\u0005J\u0001!\u0015\r\u0011\"\u0001K\u0011!Y\u0005\u0001#b\u0001\n\u0003a\u0005\"B-\u0001\t\u0003Q\u0006\"\u00021\u0001\t\u0003\tw!B6\u0010\u0011\u0003ag!\u0002\b\u0010\u0011\u0003i\u0007\"\u0002\"\u000b\t\u0003q\u0007\"B8\u000b\t\u0003\u0001\b\"\u0002>\u000b\t\u0003Y(a\u0003)beRLG/[8oKJT!\u0001E\t\u0002\u000bM\u0004\u0018M]6\u000b\u0005I\u0019\u0012!C4sK\u0016t\u0007\u000f\\;n\u0015\t!R#A\u0004qSZ|G/\u00197\u000b\u0003Y\t!![8\u0004\u0001M\u0019\u0001!G\u0010\u0011\u0005iiR\"A\u000e\u000b\u0003q\tQa]2bY\u0006L!AH\u000e\u0003\r\u0005s\u0017PU3g!\t\u0001s%D\u0001\"\u0015\t\u00113%\u0001\u0007tG\u0006d\u0017\r\\8hO&twM\u0003\u0002%K\u0005AA/\u001f9fg\u00064WMC\u0001'\u0003\r\u0019w.\\\u0005\u0003Q\u0005\u00121\u0002T1{s2{wmZ5oO\u00069\"/Z9vKN$X\r\u001a)beRLG/[8o\u0007>,h\u000e\u001e\t\u00035-J!\u0001L\u000e\u0003\u0007%sG/A\bqCJ$\u0018\u000e^5p]\u000e{G.^7o!\ty\u0013(D\u00011\u0015\t\t$'A\u0003usB,7O\u0003\u00024i\u0005\u00191/\u001d7\u000b\u0005A)$B\u0001\u001c8\u0003\u0019\t\u0007/Y2iK*\t\u0001(A\u0002pe\u001eL!A\u000f\u0019\u0003\u0017M#(/^2u\r&,G\u000eZ\u0001\u0007E>,h\u000eZ:\u0011\u0005u\u0002U\"\u0001 \u000b\u0005}z\u0011\u0001\u00026eE\u000eL!!\u0011 \u0003!\r{G.^7o-\u0006dW/\u001a*b]\u001e,\u0017A\u0002\u001fj]&$h\b\u0006\u0003E\r\u001eC\u0005CA#\u0001\u001b\u0005y\u0001\"B\u0015\u0005\u0001\u0004Q\u0003\"B\u0017\u0005\u0001\u0004q\u0003\"B\u001e\u0005\u0001\u0004a\u0014A\u00058v[\n,'o\u00144QCJ$\u0018\u000e^5p]N,\u0012AK\u0001\u0007gR\u0014\u0018\u000eZ3\u0016\u00035\u0003\"A\u0014,\u000f\u0005=#fB\u0001)T\u001b\u0005\t&B\u0001*\u0018\u0003\u0019a$o\\8u}%\tA$\u0003\u0002V7\u00059\u0001/Y2lC\u001e,\u0017BA,Y\u0005)\u0011\u0015n\u001a#fG&l\u0017\r\u001c\u0006\u0003+n\t1cY8naV$XM\u00153e!\u0006\u0014H/\u001b;j_:$\"a\u00170\u0011\u0005\u0015c\u0016BA/\u0010\u0005I\u0011F\t\u0012)beRLG/[8o\u0005>,h\u000eZ:\t\u000b};\u0001\u0019\u0001\u0016\u0002\u000b%tG-\u001a=\u0002%\r\u0014X-\u0019;f'Fd\u0007K]3eS\u000e\fG/\u001a\u000b\u0003E*\u0004\"aY4\u000f\u0005\u0011,\u0007C\u0001)\u001c\u0013\t17$\u0001\u0004Qe\u0016$WMZ\u0005\u0003Q&\u0014aa\u0015;sS:<'B\u00014\u001c\u0011\u0015y\u0006\u00021\u0001+\u0003-\u0001\u0016M\u001d;ji&|g.\u001a:\u0011\u0005\u0015S1c\u0001\u0006\u001a?Q\tA.A\ttK\u001elWM\u001c;QCJ$\u0018\u000e^5p]N$\"!]<\u0011\u0007i\u0011H/\u0003\u0002t7\t)\u0011I\u001d:bsB\u0011Q)^\u0005\u0003m>\u0011!c\u0012:fK:\u0004H.^7QCJ$\u0018\u000e^5p]\")\u0001\u0010\u0004a\u0001s\u0006Q1/Z4nK:$\u0018\nZ:\u0011\u0007i\u0011(&\u0001\td_2,XN\u001c)beRLG/[8ogR!\u0011\u000f @\u0000\u0011\u0015iX\u00021\u0001+\u0003m\u0011X-];fgR,GMT;nE\u0016\u0014xJ\u001a)beRLG/[8og\")Q&\u0004a\u0001]!1\u0011\u0011A\u0007A\u0002q\n!B^1mk\u0016\u0014\u0016M\\4f\u0001")
public class Partitioner
implements LazyLogging {
    private int numberOfPartitions;
    private BigDecimal stride;
    private final int requestedPartitionCount;
    private final StructField partitionColumn;
    private final ColumnValueRange bounds;
    private transient Logger logger;
    private volatile byte bitmap$0;
    private volatile transient boolean bitmap$trans$0;

    public static GreenplumPartition[] columnPartitions(int n, StructField structField, ColumnValueRange columnValueRange) {
        return Partitioner$.MODULE$.columnPartitions(n, structField, columnValueRange);
    }

    public static GreenplumPartition[] segmentPartitions(int[] nArray) {
        return Partitioner$.MODULE$.segmentPartitions(nArray);
    }

    private Logger logger$lzycompute() {
        Partitioner partitioner = this;
        synchronized (partitioner) {
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

    private int numberOfPartitions$lzycompute() {
        Partitioner partitioner = this;
        synchronized (partitioner) {
            if ((byte)(this.bitmap$0 & 1) == 0) {
                int n;
                DataType dataType = this.partitionColumn.dataType();
                if (ShortType$.MODULE$.equals(dataType) ? true : (IntegerType$.MODULE$.equals(dataType) ? true : LongType$.MODULE$.equals(dataType))) {
                    BoxedUnit boxedUnit;
                    BigDecimal width = this.bounds.max().$minus(this.bounds.min()).$plus(package$.MODULE$.BigDecimal().apply(1));
                    int adjusted = width.min(package$.MODULE$.BigDecimal().apply(this.requestedPartitionCount)).toInt();
                    if (adjusted != this.requestedPartitionCount) {
                        if (this.logger().underlying().isWarnEnabled()) {
                            this.logger().underlying().warn(new StringBuilder(302).append("The number of partitions is reduced because the specified number of partitions is larger than the difference between upper bound and lower bound. ").append("Updated number of partitions per segment: ").append(adjusted).append("; ").append("Input number of partitions per segment: ").append(this.requestedPartitionCount).append("; ").append("Selected partition column has type ").append(this.partitionColumn.dataType()).append(" with ").append("Lower bound: ").append(this.bounds.min()).append("; Upper bound: ").append(this.bounds.max()).append(".").toString());
                            boxedUnit = BoxedUnit.UNIT;
                        } else {
                            boxedUnit = BoxedUnit.UNIT;
                        }
                    } else {
                        boxedUnit = BoxedUnit.UNIT;
                    }
                    n = adjusted;
                } else if (dataType instanceof NumericType) {
                    n = this.requestedPartitionCount;
                } else {
                    throw new IllegalStateException(new StringBuilder(43).append("Invalid DataType '").append(this.partitionColumn.dataType()).append("' for partitioning column").toString());
                }
                this.numberOfPartitions = n;
                this.bitmap$0 = (byte)(this.bitmap$0 | 1);
            }
        }
        return this.numberOfPartitions;
    }

    public int numberOfPartitions() {
        if ((byte)(this.bitmap$0 & 1) == 0) {
            return this.numberOfPartitions$lzycompute();
        }
        return this.numberOfPartitions;
    }

    private BigDecimal stride$lzycompute() {
        Partitioner partitioner = this;
        synchronized (partitioner) {
            if ((byte)(this.bitmap$0 & 2) == 0) {
                BigDecimal stride = this.bounds.max().$minus(this.bounds.min()).$div(package$.MODULE$.BigDecimal().apply(this.numberOfPartitions()));
                DataType dataType = this.partitionColumn.dataType();
                this.stride = (ShortType$.MODULE$.equals(dataType) ? true : (IntegerType$.MODULE$.equals(dataType) ? true : LongType$.MODULE$.equals(dataType))) ? stride.setScale(0, BigDecimal.RoundingMode$.MODULE$.CEILING()) : stride;
                this.bitmap$0 = (byte)(this.bitmap$0 | 2);
            }
        }
        return this.stride;
    }

    public BigDecimal stride() {
        if ((byte)(this.bitmap$0 & 2) == 0) {
            return this.stride$lzycompute();
        }
        return this.stride;
    }

    public RDDPartitionBounds computeRddPartition(int index) {
        int offset = index + 1;
        BigDecimal lowBound = this.bounds.min().$plus(this.stride().$times(BigDecimal$.MODULE$.int2bigDecimal(index)));
        BigDecimal upBound = this.bounds.min().$plus(this.stride().$times(BigDecimal$.MODULE$.int2bigDecimal(offset)));
        return new RDDPartitionBounds(lowBound, upBound);
    }

    public String createSqlPredicate(int index) {
        boolean isOnlyPartition;
        RDDPartitionBounds bounds = this.computeRddPartition(index);
        String sqlColumnName = SqlObjectNameUtils$.MODULE$.escape(this.partitionColumn.name());
        boolean nullable = this.partitionColumn.nullable();
        boolean isFirstPartition = index == 0;
        boolean isLastPartition = index == this.numberOfPartitions() - 1;
        boolean bl = isOnlyPartition = isFirstPartition && isLastPartition;
        if (isOnlyPartition) {
            return "1 = 1";
        }
        if (isFirstPartition) {
            String conditionForNulls = nullable ? new StringBuilder(12).append(" OR ").append(sqlColumnName).append(" IS NULL").toString() : "";
            return new StringBuilder(3).append(sqlColumnName).append(" < ").append(bounds.upper()).append(conditionForNulls).toString();
        }
        if (isLastPartition) {
            return new StringBuilder(4).append(sqlColumnName).append(" >= ").append(bounds.lower()).toString();
        }
        return new StringBuilder(12).append(sqlColumnName).append(" >= ").append(bounds.lower()).append(" AND ").append(sqlColumnName).append(" < ").append(bounds.upper()).toString();
    }

    public Partitioner(int requestedPartitionCount, StructField partitionColumn, ColumnValueRange bounds) {
        this.requestedPartitionCount = requestedPartitionCount;
        this.partitionColumn = partitionColumn;
        this.bounds = bounds;
        LazyLogging.$init$(this);
    }
}

