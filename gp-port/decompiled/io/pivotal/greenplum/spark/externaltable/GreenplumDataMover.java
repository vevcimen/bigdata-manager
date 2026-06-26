/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.SparkEnv$
 *  scala.Function1
 *  scala.Serializable
 *  scala.collection.Seq
 *  scala.collection.immutable.Nil$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.ConnectorUtils;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import io.pivotal.greenplum.spark.externaltable.GpfdistService;
import io.pivotal.greenplum.spark.externaltable.GreenplumDataMover$;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName$;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager$;
import io.pivotal.greenplum.spark.externaltable.PartitionData;
import java.io.Serializable;
import org.apache.spark.SparkEnv$;
import scala.Function1;
import scala.collection.Seq;
import scala.collection.immutable.Nil$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001I4A\u0001D\u0007\u00011!A\u0011\u0006\u0001B\u0001B\u0003%!\u0006\u0003\u00056\u0001\t\u0005\t\u0015!\u00037\u0011!a\u0004A!A!\u0002\u0013i\u0004\u0002C!\u0001\u0005\u0003\u0005\u000b\u0011\u0002\"\t\u0011\u0015\u0003!\u0011!Q\u0001\n\u0019CQA\u0013\u0001\u0005\u0002-CQA\u0015\u0001\u0005\u0002M;qAY\u0007\u0002\u0002#\u00051MB\u0004\r\u001b\u0005\u0005\t\u0012\u00013\t\u000b)KA\u0011A3\t\u000f\u0019L\u0011\u0013!C\u0001O\n\u0011rI]3f]BdW/\u001c#bi\u0006luN^3s\u0015\tqq\"A\u0007fqR,'O\\1mi\u0006\u0014G.\u001a\u0006\u0003!E\tQa\u001d9be.T!AE\n\u0002\u0013\u001d\u0014X-\u001a8qYVl'B\u0001\u000b\u0016\u0003\u001d\u0001\u0018N^8uC2T\u0011AF\u0001\u0003S>\u001c\u0001aE\u0002\u00013}\u0001\"AG\u000f\u000e\u0003mQ\u0011\u0001H\u0001\u0006g\u000e\fG.Y\u0005\u0003=m\u0011a!\u00118z%\u00164\u0007C\u0001\u0011(\u001b\u0005\t#B\u0001\u0012$\u00031\u00198-\u00197bY><w-\u001b8h\u0015\t!S%\u0001\u0005usB,7/\u00194f\u0015\u00051\u0013aA2p[&\u0011\u0001&\t\u0002\f\u0019\u0006T\u0018\u0010T8hO&tw-A\u0007baBd\u0017nY1uS>t\u0017\n\u001a\t\u0003WIr!\u0001\f\u0019\u0011\u00055ZR\"\u0001\u0018\u000b\u0005=:\u0012A\u0002\u001fs_>$h(\u0003\u000227\u00051\u0001K]3eK\u001aL!a\r\u001b\u0003\rM#(/\u001b8h\u0015\t\t4$\u0001\the\u0016,g\u000e\u001d7v[>\u0003H/[8ogB\u0011qGO\u0007\u0002q)\u0011\u0011hD\u0001\u0005G>tg-\u0003\u0002<q\t\u0001rI]3f]BdW/\\(qi&|gn]\u0001\ri\u0006\u0014G.Z'b]\u0006<WM\u001d\t\u0003}}j\u0011!D\u0005\u0003\u00016\u0011Qc\u0012:fK:\u0004H.^7UC\ndW-T1oC\u001e,'/A\u0004tKJ4\u0018nY3\u0011\u0005y\u001a\u0015B\u0001#\u000e\u000599\u0005O\u001a3jgR\u001cVM\u001d<jG\u0016\fabY8o]\u0016\u001cGo\u001c:Vi&d7\u000f\u0005\u0002H\u00116\tq\"\u0003\u0002J\u001f\tq1i\u001c8oK\u000e$xN]+uS2\u001c\u0018A\u0002\u001fj]&$h\b\u0006\u0004M\u001b:{\u0005+\u0015\t\u0003}\u0001AQ!\u000b\u0004A\u0002)BQ!\u000e\u0004A\u0002YBQ\u0001\u0010\u0004A\u0002uBQ!\u0011\u0004A\u0002\tCq!\u0012\u0004\u0011\u0002\u0003\u0007a)\u0001\u0005n_Z,G)\u0019;b)\t!V\fE\u0002V1jk\u0011A\u0016\u0006\u0003/n\tA!\u001e;jY&\u0011\u0011L\u0016\u0002\u0004)JL\bC\u0001\u000e\\\u0013\ta6DA\u0002J]RDQAX\u0004A\u0002}\u000bQ\u0002]1si&$\u0018n\u001c8ECR\f\u0007C\u0001 a\u0013\t\tWBA\u0007QCJ$\u0018\u000e^5p]\u0012\u000bG/Y\u0001\u0013\u000fJ,WM\u001c9mk6$\u0015\r^1N_Z,'\u000f\u0005\u0002?\u0013M\u0011\u0011\"\u0007\u000b\u0002G\u0006YB\u0005\\3tg&t\u0017\u000e\u001e\u0013he\u0016\fG/\u001a:%I\u00164\u0017-\u001e7uIU*\u0012\u0001\u001b\u0016\u0003\r&\\\u0013A\u001b\t\u0003WBl\u0011\u0001\u001c\u0006\u0003[:\f\u0011\"\u001e8dQ\u0016\u001c7.\u001a3\u000b\u0005=\\\u0012AC1o]>$\u0018\r^5p]&\u0011\u0011\u000f\u001c\u0002\u0012k:\u001c\u0007.Z2lK\u00124\u0016M]5b]\u000e,\u0007")
public class GreenplumDataMover
implements LazyLogging {
    private final String applicationId;
    private final GreenplumOptions greenplumOptions;
    private final GreenplumTableManager tableManager;
    private final GpfdistService service;
    private final ConnectorUtils connectorUtils;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static ConnectorUtils $lessinit$greater$default$5() {
        return GreenplumDataMover$.MODULE$.$lessinit$greater$default$5();
    }

    private Logger logger$lzycompute() {
        GreenplumDataMover greenplumDataMover = this;
        synchronized (greenplumDataMover) {
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

    public Try<Object> moveData(PartitionData partitionData) {
        String executorId = SparkEnv$.MODULE$.get().executorId();
        long threadId = Thread.currentThread().getId();
        GreenplumQualifiedName.Table targetTable = GreenplumQualifiedName$.MODULE$.forTable(this.greenplumOptions.dbSchema(), this.greenplumOptions.dbTable());
        String externalTableName = GreenplumTableManager$.MODULE$.generateExternalTableName(this.applicationId, targetTable.name(), executorId, threadId, (Seq<String>)((Seq)Nil$.MODULE$));
        GreenplumQualifiedName.TempTable extTable = GreenplumQualifiedName$.MODULE$.forTempTable(externalTableName);
        GpfdistLocation location = this.connectorUtils.getLocation(this.greenplumOptions.connectorOptions(), this.service.getPort());
        String pathPrefix = this.connectorUtils.getLocationPathPrefix(this.applicationId, executorId);
        GpfdistLocation locationWithPath = location.withPath(pathPrefix);
        return this.tableManager.createReadableExternalTableIfNotExists(targetTable, extTable, locationWithPath).flatMap((Function1 & Serializable & scala.Serializable)x$1 -> $this.tableManager.getDistributedTransactionId()).flatMap((Function1 & Serializable & scala.Serializable)transactionId -> {
            BoxedUnit boxedUnit;
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("Setting RDD(index={}) data for distributed transaction {}", new Object[]{BoxesRunTime.boxToInteger((int)partitionData.partitionIndex()), transactionId});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            Try rowsCopied = $this.service.setPartitionDataFor((String)transactionId, partitionData).flatMap((Function1 & Serializable & scala.Serializable)x$2 -> $this.tableManager.copyTableFromExternal(extTable, targetTable));
            $this.service.removePartitionDataFor((String)transactionId);
            return rowsCopied;
        });
    }

    public GreenplumDataMover(String applicationId, GreenplumOptions greenplumOptions, GreenplumTableManager tableManager, GpfdistService service, ConnectorUtils connectorUtils) {
        this.applicationId = applicationId;
        this.greenplumOptions = greenplumOptions;
        this.tableManager = tableManager;
        this.service = service;
        this.connectorUtils = connectorUtils;
        LazyLogging.$init$(this);
    }
}

