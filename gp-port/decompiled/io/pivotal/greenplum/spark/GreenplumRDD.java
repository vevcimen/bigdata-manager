/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.Partition
 *  org.apache.spark.SparkContext
 *  org.apache.spark.TaskContext
 *  org.apache.spark.rdd.RDD
 *  org.apache.spark.sql.catalyst.InternalRow
 *  org.apache.spark.sql.jdbc.JdbcDialect
 *  org.apache.spark.sql.sources.Filter
 *  org.apache.spark.sql.types.StructType
 *  scala.Option
 *  scala.collection.Iterator
 *  scala.collection.Seq
 *  scala.collection.immutable.Nil$
 *  scala.reflect.ClassTag$
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.GreenplumRDD$;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.externaltable.GreenplumRowIterator;
import io.pivotal.greenplum.spark.externaltable.GreenplumRowIterator$;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.types.StructType;
import scala.Option;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.immutable.Nil$;
import scala.reflect.ClassTag$;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001\u0005uc\u0001\u0002\n\u0014\u0001qA\u0001\u0002\r\u0001\u0003\u0002\u0003\u0006I!\r\u0005\tk\u0001\u0011\t\u0011)A\u0005m!AA\b\u0001B\u0001B\u0003%Q\b\u0003\u0005H\u0001\t\u0005\t\u0015!\u0003I\u0011!q\u0005A!A!\u0002\u0013y\u0005\u0002C2\u0001\u0005\u0003\u0005\u000b\u0011\u00023\t\u000b-\u0004A\u0011\u00017\t\u000fQ\u0004!\u0019!C\u0005k\"1a\u000f\u0001Q\u0001\nmCQa\u001e\u0001\u0005BaDQ! \u0001\u0005By<q!a\u0005\u0014\u0011\u0003\t)B\u0002\u0004\u0013'!\u0005\u0011q\u0003\u0005\u0007W6!\t!!\n\t\u000f\u0005\u001dR\u0002\"\u0001\u0002*!9\u0011qE\u0007\u0005\u0002\u0005\u0015\u0003\"CA%\u001b\u0005\u0005I\u0011BA&\u000519%/Z3oa2,XN\u0015#E\u0015\t!R#A\u0003ta\u0006\u00148N\u0003\u0002\u0017/\u0005IqM]3f]BdW/\u001c\u0006\u00031e\tq\u0001]5w_R\fGNC\u0001\u001b\u0003\tIwn\u0001\u0001\u0014\u0005\u0001i\u0002c\u0001\u0010'Q5\tqD\u0003\u0002!C\u0005\u0019!\u000f\u001a3\u000b\u0005Q\u0011#BA\u0012%\u0003\u0019\t\u0007/Y2iK*\tQ%A\u0002pe\u001eL!aJ\u0010\u0003\u0007I#E\t\u0005\u0002*]5\t!F\u0003\u0002,Y\u0005A1-\u0019;bYf\u001cHO\u0003\u0002.C\u0005\u00191/\u001d7\n\u0005=R#aC%oi\u0016\u0014h.\u00197S_^\f!a]2\u0011\u0005I\u001aT\"A\u0011\n\u0005Q\n#\u0001D*qCJ\\7i\u001c8uKb$\u0018AB:dQ\u0016l\u0017\r\u0005\u00028u5\t\u0001H\u0003\u0002:Y\u0005)A/\u001f9fg&\u00111\b\u000f\u0002\u000b'R\u0014Xo\u0019;UsB,\u0017A\u00039beRLG/[8ogB\u0019a(Q\"\u000e\u0003}R\u0011\u0001Q\u0001\u0006g\u000e\fG.Y\u0005\u0003\u0005~\u0012Q!\u0011:sCf\u0004\"\u0001R#\u000e\u0003MI!AR\n\u0003%\u001d\u0013X-\u001a8qYVl\u0007+\u0019:uSRLwN\\\u0001\u0011OJ,WM\u001c9mk6|\u0005\u000f^5p]N\u0004\"!\u0013'\u000e\u0003)S!aS\n\u0002\t\r|gNZ\u0005\u0003\u001b*\u0013\u0001c\u0012:fK:\u0004H.^7PaRLwN\\:\u0002\u001fI,\u0017/^5sK\u0012\u001cu\u000e\\;n]N\u00042\u0001\u0015-\\\u001d\t\tfK\u0004\u0002S+6\t1K\u0003\u0002U7\u00051AH]8pizJ\u0011\u0001Q\u0005\u0003/~\nq\u0001]1dW\u0006<W-\u0003\u0002Z5\n\u00191+Z9\u000b\u0005]{\u0004C\u0001/a\u001d\tif\f\u0005\u0002S\u007f%\u0011qlP\u0001\u0007!J,G-\u001a4\n\u0005\u0005\u0014'AB*ue&twM\u0003\u0002`\u007f\u00059a-\u001b7uKJ\u001c\bc\u0001 BKB\u0011a-[\u0007\u0002O*\u0011\u0001\u000eL\u0001\bg>,(oY3t\u0013\tQwM\u0001\u0004GS2$XM]\u0001\u0007y%t\u0017\u000e\u001e \u0015\u000f5tw\u000e]9sgB\u0011A\t\u0001\u0005\u0006a\u001d\u0001\r!\r\u0005\u0006k\u001d\u0001\rA\u000e\u0005\u0006y\u001d\u0001\r!\u0010\u0005\u0006\u000f\u001e\u0001\r\u0001\u0013\u0005\u0006\u001d\u001e\u0001\ra\u0014\u0005\u0006G\u001e\u0001\r\u0001Z\u0001\u000eCB\u0004H.[2bi&|g.\u00133\u0016\u0003m\u000ba\"\u00199qY&\u001c\u0017\r^5p]&#\u0007%A\u0007hKR\u0004\u0016M\u001d;ji&|gn]\u000b\u0002sB\u0019a(\u0011>\u0011\u0005IZ\u0018B\u0001?\"\u0005%\u0001\u0016M\u001d;ji&|g.A\u0004d_6\u0004X\u000f^3\u0015\u000b}\f)!!\u0003\u0011\tA\u000b\t\u0001K\u0005\u0004\u0003\u0007Q&\u0001C%uKJ\fGo\u001c:\t\r\u0005\u001d1\u00021\u0001{\u0003\u001d!\b.\u001a)beRDq!a\u0003\f\u0001\u0004\ti!A\u0004d_:$X\r\u001f;\u0011\u0007I\ny!C\u0002\u0002\u0012\u0005\u00121\u0002V1tW\u000e{g\u000e^3yi\u0006aqI]3f]BdW/\u001c*E\tB\u0011A)D\n\u0006\u001b\u0005e\u0011q\u0004\t\u0004}\u0005m\u0011bAA\u000f\u007f\t1\u0011I\\=SK\u001a\u00042APA\u0011\u0013\r\t\u0019c\u0010\u0002\r'\u0016\u0014\u0018.\u00197ju\u0006\u0014G.\u001a\u000b\u0003\u0003+\tQbY8na&dWMR5mi\u0016\u0014HCBA\u0016\u0003c\t)\u0004\u0005\u0003?\u0003[Y\u0016bAA\u0018\u007f\t1q\n\u001d;j_:Da!a\r\u0010\u0001\u0004)\u0017!\u00014\t\u000f\u0005]r\u00021\u0001\u0002:\u00059A-[1mK\u000e$\b\u0003BA\u001e\u0003\u0003j!!!\u0010\u000b\u0007\u0005}B&\u0001\u0003kI\n\u001c\u0017\u0002BA\"\u0003{\u00111B\u00133cG\u0012K\u0017\r\\3diR!\u00111FA$\u0011\u0019\t\u0019\u0004\u0005a\u0001K\u0006Y!/Z1e%\u0016\u001cx\u000e\u001c<f)\t\ti\u0005\u0005\u0003\u0002P\u0005eSBAA)\u0015\u0011\t\u0019&!\u0016\u0002\t1\fgn\u001a\u0006\u0003\u0003/\nAA[1wC&!\u00111LA)\u0005\u0019y%M[3di\u0002")
public class GreenplumRDD
extends RDD<InternalRow> {
    private final StructType schema;
    private final GreenplumPartition[] partitions;
    private final GreenplumOptions greenplumOptions;
    private final Seq<String> requiredColumns;
    private final Filter[] filters;
    private final String applicationId;

    public static Option<String> compileFilter(Filter filter) {
        return GreenplumRDD$.MODULE$.compileFilter(filter);
    }

    public static Option<String> compileFilter(Filter filter, JdbcDialect jdbcDialect) {
        return GreenplumRDD$.MODULE$.compileFilter(filter, jdbcDialect);
    }

    private String applicationId() {
        return this.applicationId;
    }

    public Partition[] getPartitions() {
        return this.partitions;
    }

    public Iterator<InternalRow> compute(Partition thePart, TaskContext context) {
        GreenplumPartition partition = (GreenplumPartition)thePart;
        return new GreenplumRowIterator(this.applicationId(), partition, this.schema, this.greenplumOptions, this.requiredColumns, this.filters, GreenplumRowIterator$.MODULE$.$lessinit$greater$default$7());
    }

    public GreenplumRDD(SparkContext sc, StructType schema, GreenplumPartition[] partitions, GreenplumOptions greenplumOptions, Seq<String> requiredColumns, Filter[] filters) {
        this.schema = schema;
        this.partitions = partitions;
        this.greenplumOptions = greenplumOptions;
        this.requiredColumns = requiredColumns;
        this.filters = filters;
        super(sc, (Seq)Nil$.MODULE$, ClassTag$.MODULE$.apply(InternalRow.class));
        this.applicationId = sc.applicationId();
    }
}

