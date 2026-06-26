/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.Row
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.collection.Seq
 *  scala.collection.immutable.List
 *  scala.package$
 *  scala.reflect.ClassManifestFactory$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.SqlExecutor;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.externaltable.GpfdistService;
import io.pivotal.greenplum.spark.externaltable.GpfdistServiceManager$;
import io.pivotal.greenplum.spark.externaltable.GreenplumDataMover;
import io.pivotal.greenplum.spark.externaltable.GreenplumDataMover$;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager;
import io.pivotal.greenplum.spark.externaltable.PartitionData;
import io.pivotal.greenplum.spark.externaltable.PartitionData$;
import io.pivotal.greenplum.spark.jdbc.ConnectionManager$;
import java.io.Serializable;
import java.sql.Connection;
import org.apache.spark.sql.Row;
import resource.ExtractableManagedResource;
import resource.Resource$;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.Predef$;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.immutable.List;
import scala.package$;
import scala.reflect.ClassManifestFactory$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;

@ScalaSignature(bytes="\u0006\u0001e4A!\u0003\u0006\u0001+!A\u0011\u0006\u0001B\u0001B\u0003%!\u0006\u0003\u00056\u0001\t\u0005\t\u0015!\u00037\u0011!a\u0004A!A!\u0002\u0013i\u0004\"B&\u0001\t\u0003a\u0005\u0002\u0003*\u0001\u0011\u000b\u0007I\u0011A*\t\u000b]\u0003A\u0011\u0001-\t\u000b%\u0004A\u0011\u00016\t\u000b]\u0004A\u0011\u0001=\u0003\u001fA\u000b'\u000f^5uS>twK]5uKJT!a\u0003\u0007\u0002\u001b\u0015DH/\u001a:oC2$\u0018M\u00197f\u0015\tia\"A\u0003ta\u0006\u00148N\u0003\u0002\u0010!\u0005IqM]3f]BdW/\u001c\u0006\u0003#I\tq\u0001]5w_R\fGNC\u0001\u0014\u0003\tIwn\u0001\u0001\u0014\t\u00011Bd\b\t\u0003/ii\u0011\u0001\u0007\u0006\u00023\u0005)1oY1mC&\u00111\u0004\u0007\u0002\u0007\u0003:L(+\u001a4\u0011\u0005]i\u0012B\u0001\u0010\u0019\u00051\u0019VM]5bY&T\u0018M\u00197f!\t\u0001s%D\u0001\"\u0015\t\u00113%\u0001\u0007tG\u0006d\u0017\r\\8hO&twM\u0003\u0002%K\u0005AA/\u001f9fg\u00064WMC\u0001'\u0003\r\u0019w.\\\u0005\u0003Q\u0005\u00121\u0002T1{s2{wmZ5oO\u0006i\u0011\r\u001d9mS\u000e\fG/[8o\u0013\u0012\u0004\"a\u000b\u001a\u000f\u00051\u0002\u0004CA\u0017\u0019\u001b\u0005q#BA\u0018\u0015\u0003\u0019a$o\\8u}%\u0011\u0011\u0007G\u0001\u0007!J,G-\u001a4\n\u0005M\"$AB*ue&twM\u0003\u000221\u0005\u0001rM]3f]BdW/\\(qi&|gn\u001d\t\u0003oij\u0011\u0001\u000f\u0006\u0003s1\tAaY8oM&\u00111\b\u000f\u0002\u0011\u000fJ,WM\u001c9mk6|\u0005\u000f^5p]N\faB]8x)J\fgn\u001d4pe6,'\u000f\u0005\u0003\u0018}\u0001\u0003\u0015BA \u0019\u0005%1UO\\2uS>t\u0017\u0007\u0005\u0002B\u00136\t!I\u0003\u0002D\t\u0006\u00191/\u001d7\u000b\u00055)%B\u0001$H\u0003\u0019\t\u0007/Y2iK*\t\u0001*A\u0002pe\u001eL!A\u0013\"\u0003\u0007I{w/\u0001\u0004=S:LGO\u0010\u000b\u0005\u001b>\u0003\u0016\u000b\u0005\u0002O\u00015\t!\u0002C\u0003*\t\u0001\u0007!\u0006C\u00036\t\u0001\u0007a\u0007C\u0003=\t\u0001\u0007Q(\u0001\bha\u001a$\u0017n\u001d;TKJ4\u0018nY3\u0016\u0003Q\u0003\"AT+\n\u0005YS!AD$qM\u0012L7\u000f^*feZL7-Z\u0001\u000bO\u0016$8\t\\8tkJ,W#A-\u0011\u000b]QFl\u00185\n\u0005mC\"!\u0003$v]\u000e$\u0018n\u001c83!\t9R,\u0003\u0002_1\t\u0019\u0011J\u001c;\u0011\u0007\u0001,\u0007I\u0004\u0002bG:\u0011QFY\u0005\u00023%\u0011A\rG\u0001\ba\u0006\u001c7.Y4f\u0013\t1wM\u0001\u0005Ji\u0016\u0014\u0018\r^8s\u0015\t!\u0007\u0004E\u0002aKr\u000bAbZ3u\t\u0006$\u0018-T8wKJ$\"a\u001b8\u0011\u00059c\u0017BA7\u000b\u0005I9%/Z3oa2,X\u000eR1uC6{g/\u001a:\t\u000b=<\u0001\u0019\u00019\u0002\t\r|gN\u001c\t\u0003cVl\u0011A\u001d\u0006\u0003\u0007NT\u0011\u0001^\u0001\u0005U\u00064\u0018-\u0003\u0002we\nQ1i\u001c8oK\u000e$\u0018n\u001c8\u0002\u001b\u001d,GoQ8o]\u0016\u001cG/[8o)\u0005\u0001\b")
public class PartitionWriter
implements scala.Serializable,
LazyLogging {
    private GpfdistService gpfdistService;
    private final String applicationId;
    private final GreenplumOptions greenplumOptions;
    private final Function1<Row, Row> rowTransformer;
    private transient Logger logger;
    private volatile boolean bitmap$0;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        PartitionWriter partitionWriter = this;
        synchronized (partitionWriter) {
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

    private GpfdistService gpfdistService$lzycompute() {
        PartitionWriter partitionWriter = this;
        synchronized (partitionWriter) {
            if (!this.bitmap$0) {
                this.gpfdistService = GpfdistServiceManager$.MODULE$.getService(this.greenplumOptions.connectorOptions());
                this.bitmap$0 = true;
            }
        }
        return this.gpfdistService;
    }

    public GpfdistService gpfdistService() {
        if (!this.bitmap$0) {
            return this.gpfdistService$lzycompute();
        }
        return this.gpfdistService;
    }

    public Function2<Object, Iterator<Row>, Iterator<Object>> getClosure() {
        return (Function2 & Serializable & scala.Serializable)(idx, it) -> PartitionWriter.$anonfun$getClosure$1(this, BoxesRunTime.unboxToInt((Object)idx), it);
    }

    public GreenplumDataMover getDataMover(Connection conn) {
        SqlExecutor sqlExecutor = new SqlExecutor(conn);
        GreenplumTableManager tableManager = new GreenplumTableManager(sqlExecutor);
        return new GreenplumDataMover(this.applicationId, this.greenplumOptions, tableManager, this.gpfdistService(), GreenplumDataMover$.MODULE$.$lessinit$greater$default$5());
    }

    public Connection getConnection() {
        return ConnectionManager$.MODULE$.getConnection(this.greenplumOptions, false);
    }

    public static final /* synthetic */ Iterator $anonfun$getClosure$1(PartitionWriter $this, int idx, Iterator it) {
        PartitionData partitionData;
        if (it.isEmpty()) {
            BoxedUnit boxedUnit;
            if ($this.logger().underlying().isDebugEnabled()) {
                $this.logger().underlying().debug("Datamover {} skipped work as partition iterator is empty", new Object[]{BoxesRunTime.boxToInteger((int)idx)});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return package$.MODULE$.Iterator().apply((Seq)Predef$.MODULE$.wrapIntArray(new int[]{0}));
        }
        $this.gpfdistService().start();
        if ($this.greenplumOptions.iteratorOptimization()) {
            int x$1 = idx;
            Iterator x$2 = it;
            Function1<Row, Row> x$3 = $this.rowTransformer;
            List<Row> x$4 = PartitionData$.MODULE$.apply$default$3();
            partitionData = new PartitionData(x$1, (Iterator<Row>)x$2, x$4, x$3);
        } else {
            int x$5 = idx;
            List x$6 = it.toList();
            Function1<Row, Row> x$7 = $this.rowTransformer;
            Iterator<Row> x$8 = PartitionData$.MODULE$.apply$default$2();
            partitionData = new PartitionData(x$5, x$8, (List<Row>)x$6, x$7);
        }
        PartitionData partitionData2 = partitionData;
        ExtractableManagedResource copiedCountIterator = resource.package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> $this.getConnection(), Resource$.MODULE$.connectionResource(), ClassManifestFactory$.MODULE$.classType(Connection.class)).map((Function1 & Serializable & scala.Serializable)connection -> {
            BoxedUnit boxedUnit;
            GreenplumDataMover dataMover = $this.getDataMover((Connection)connection);
            int count = BoxesRunTime.unboxToInt((Object)dataMover.moveData(partitionData2).get());
            connection.commit();
            if ($this.logger().underlying().isDebugEnabled()) {
                $this.logger().underlying().debug("Datamover {} copied {} rows", new Object[]{BoxesRunTime.boxToInteger((int)idx), BoxesRunTime.boxToInteger((int)count)});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return package$.MODULE$.Iterator().apply((Seq)Predef$.MODULE$.wrapIntArray(new int[]{count}));
        });
        return (Iterator)copiedCountIterator.tried().get();
    }

    public PartitionWriter(String applicationId, GreenplumOptions greenplumOptions, Function1<Row, Row> rowTransformer) {
        this.applicationId = applicationId;
        this.greenplumOptions = greenplumOptions;
        this.rowTransformer = rowTransformer;
        LazyLogging.$init$(this);
    }
}

