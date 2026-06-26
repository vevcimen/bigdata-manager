/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.SaveMode
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.StructType
 *  scala.Function1
 *  scala.MatchError
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.Seq
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.ErrorHandling$;
import io.pivotal.greenplum.spark.GreenplumCSVFormat$;
import io.pivotal.greenplum.spark.SqlExecutor;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager$;
import io.pivotal.greenplum.spark.jdbc.Jdbc$;
import java.io.Serializable;
import java.sql.ResultSet;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;
import scala.Function1;
import scala.MatchError;
import scala.Option;
import scala.Predef$;
import scala.collection.Seq;
import scala.collection.immutable.StringOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\t\rb\u0001\u0002\u000f\u001e\u0001!B\u0001\"\u000f\u0001\u0003\u0002\u0003\u0006IA\u000f\u0005\u0006}\u0001!\ta\u0010\u0005\u0006\u0007\u0002!\t\u0001\u0012\u0005\ba\u0002\t\n\u0011\"\u0001r\u0011\u0019a\b\u0001\"\u0001\u001e{\"9\u00111\u0002\u0001\u0005\n\u00055\u0001bBA\u000f\u0001\u0011\u0005\u0011q\u0004\u0005\b\u0003\u0003\u0002A\u0011BA\"\u0011\u001d\tY\u0005\u0001C\u0001\u0003\u001bBq!a\u0018\u0001\t\u0013\t\t\u0007C\u0004\u0002\u0000\u0001!\t!!!\t\u000f\u0005e\u0005\u0001\"\u0001\u0002\u001c\"9\u0011q\u0014\u0001\u0005\u0002\u0005\u0005\u0006\u0002CAS\u0001\u0011\u0005Q$a*\t\u0011\u0005-\u0006\u0001\"\u0001\u001e\u0003[;q!!-\u001e\u0011\u0003\t\u0019L\u0002\u0004\u001d;!\u0005\u0011Q\u0017\u0005\u0007}E!\t!a.\t\u000f\u0005e\u0016\u0003\"\u0001\u0002<\"9\u0011QZ\t\u0005\u0002\u0005=\u0007bBAj#\u0011\u0005\u0011Q\u001b\u0005\b\u00033\fB\u0011AAn\u0011\u001d\t9/\u0005C\u0001\u0003SD\u0001\"!=\u0012\t\u0003i\u00121\u001f\u0005\b\u0003{\fB\u0011AA\u0000\u0011%\u0011)\"EI\u0001\n\u0003\u00119\u0002C\u0004\u0003\u001cE!IA!\b\u0003+\u001d\u0013X-\u001a8qYVlG+\u00192mK6\u000bg.Y4fe*\u0011adH\u0001\u000eKb$XM\u001d8bYR\f'\r\\3\u000b\u0005\u0001\n\u0013!B:qCJ\\'B\u0001\u0012$\u0003%9'/Z3oa2,XN\u0003\u0002%K\u00059\u0001/\u001b<pi\u0006d'\"\u0001\u0014\u0002\u0005%|7\u0001A\n\u0004\u0001%z\u0003C\u0001\u0016.\u001b\u0005Y#\"\u0001\u0017\u0002\u000bM\u001c\u0017\r\\1\n\u00059Z#AB!osJ+g\r\u0005\u00021o5\t\u0011G\u0003\u00023g\u0005a1oY1mC2|wmZ5oO*\u0011A'N\u0001\tif\u0004Xm]1gK*\ta'A\u0002d_6L!\u0001O\u0019\u0003\u00171\u000b'0\u001f'pO\u001eLgnZ\u0001\fgFdW\t_3dkR|'\u000f\u0005\u0002<y5\tq$\u0003\u0002>?\tY1+\u001d7Fq\u0016\u001cW\u000f^8s\u0003\u0019a\u0014N\\5u}Q\u0011\u0001I\u0011\t\u0003\u0003\u0002i\u0011!\b\u0005\u0006s\t\u0001\rAO\u0001\u0015aJ,\u0007/\u0019:f)\u0006\u0014G.\u001a$pe^\u0013\u0018\u000e^3\u0015\u000b\u0015s5K\u00196\u0011\u0007\u0019K5*D\u0001H\u0015\tA5&\u0001\u0003vi&d\u0017B\u0001&H\u0005\r!&/\u001f\t\u0003U1K!!T\u0016\u0003\u000f\t{w\u000e\\3b]\")qj\u0001a\u0001!\u0006)A/\u00192mKB\u0011\u0011)U\u0005\u0003%v\u0011ac\u0012:fK:\u0004H.^7Rk\u0006d\u0017NZ5fI:\u000bW.\u001a\u0005\u0006)\u000e\u0001\r!V\u0001\fgB\f'o[*dQ\u0016l\u0017\r\u0005\u0002WA6\tqK\u0003\u0002Y3\u0006)A/\u001f9fg*\u0011!lW\u0001\u0004gFd'B\u0001\u0011]\u0015\tif,\u0001\u0004ba\u0006\u001c\u0007.\u001a\u0006\u0002?\u0006\u0019qN]4\n\u0005\u0005<&AC*ueV\u001cG\u000fV=qK\")1m\u0001a\u0001I\u00069q\u000e\u001d;j_:\u001c\bCA3i\u001b\u00051'BA4 \u0003\u0011\u0019wN\u001c4\n\u0005%4'\u0001E$sK\u0016t\u0007\u000f\\;n\u001fB$\u0018n\u001c8t\u0011\u001dY7\u0001%AA\u00021\fA!\\8eKB\u0011QN\\\u0007\u00023&\u0011q.\u0017\u0002\t'\u00064X-T8eK\u0006q\u0002O]3qCJ,G+\u00192mK\u001a{'o\u0016:ji\u0016$C-\u001a4bk2$H\u0005N\u000b\u0002e*\u0012An]\u0016\u0002iB\u0011QO_\u0007\u0002m*\u0011q\u000f_\u0001\nk:\u001c\u0007.Z2lK\u0012T!!_\u0016\u0002\u0015\u0005tgn\u001c;bi&|g.\u0003\u0002|m\n\tRO\\2iK\u000e\\W\r\u001a,be&\fgnY3\u0002\u001d=4XM]<sSR,G+\u00192mKR9a0!\u0002\u0002\b\u0005%\u0001c\u0001$J\u007fB\u0019!&!\u0001\n\u0007\u0005\r1F\u0001\u0004B]f4\u0016\r\u001c\u0005\u0006\u001f\u0016\u0001\r\u0001\u0015\u0005\u0006)\u0016\u0001\r!\u0016\u0005\u0006G\u0016\u0001\r\u0001Z\u0001\fGJ,\u0017\r^3UC\ndW\r\u0006\u0005\u0002\u0010\u0005]\u0011\u0011DA\u000e!\u00111\u0015*!\u0005\u0011\u0007)\n\u0019\"C\u0002\u0002\u0016-\u0012A!\u00168ji\")qJ\u0002a\u0001!\")AK\u0002a\u0001+\")1M\u0002a\u0001I\u000613M]3bi\u0016\u0014V-\u00193bE2,W\t\u001f;fe:\fG\u000eV1cY\u0016LeMT8u\u000bbL7\u000f^:\u0015\u0011\u0005=\u0011\u0011EA\u0013\u0003oAa!a\t\b\u0001\u0004\u0001\u0016!D5oi\u0016\u0014h.\u00197UC\ndW\rC\u0004\u0002(\u001d\u0001\r!!\u000b\u0002\u001b\u0015DH/\u001a:oC2$\u0016M\u00197f!\u0011\tY#!\r\u000f\u0007\u0005\u000bi#C\u0002\u00020u\tac\u0012:fK:\u0004H.^7Rk\u0006d\u0017NZ5fI:\u000bW.Z\u0005\u0005\u0003g\t)DA\u0005UK6\u0004H+\u00192mK*\u0019\u0011qF\u000f\t\u000f\u0005er\u00011\u0001\u0002<\u0005yq\r\u001d4eSN$Hj\\2bi&|g\u000eE\u0002B\u0003{I1!a\u0010\u001e\u0005=9\u0005O\u001a3jgRdunY1uS>t\u0017aG2sK\u0006$XMU3bI\u0006\u0014G.Z#yi\u0016\u0014h.\u00197UC\ndW\r\u0006\u0005\u0002\u0010\u0005\u0015\u0013qIA%\u0011\u0019\t\u0019\u0003\u0003a\u0001!\"9\u0011q\u0005\u0005A\u0002\u0005%\u0002bBA\u001d\u0011\u0001\u0007\u00111H\u0001\u0016G>\u0004\u0018\u0010V1cY\u00164%o\\7FqR,'O\\1m)\u0019\ty%a\u0016\u0002\\A!a)SA)!\rQ\u00131K\u0005\u0004\u0003+Z#aA%oi\"1\u0011\u0011L\u0005A\u0002A\u000b\u0001\"\u001a=u)\u0006\u0014G.\u001a\u0005\u0007\u0003;J\u0001\u0019\u0001)\u0002\rQ\f'oZ3u\u0003y\u0019'/Z1uKJ+\u0017\rZ1cY\u0016,\u0005\u0010^3s]\u0006dG+\u00192mKN\u000bH\u000e\u0006\u0005\u0002d\u0005e\u00141PA?!\u0011\t)'a\u001d\u000f\t\u0005\u001d\u0014q\u000e\t\u0004\u0003SZSBAA6\u0015\r\tigJ\u0001\u0007yI|w\u000e\u001e \n\u0007\u0005E4&\u0001\u0004Qe\u0016$WMZ\u0005\u0005\u0003k\n9H\u0001\u0004TiJLgn\u001a\u0006\u0004\u0003cZ\u0003BBA\u0012\u0015\u0001\u0007\u0001\u000bC\u0004\u0002()\u0001\r!!\u000b\t\u000f\u0005e\"\u00021\u0001\u0002<\u0005qq-\u001a;D_2,XN\u001c(b[\u0016\u001cH\u0003BAB\u0003/\u0003BAR%\u0002\u0006B1\u0011qQAI\u0003GrA!!#\u0002\u000e:!\u0011\u0011NAF\u0013\u0005a\u0013bAAHW\u00059\u0001/Y2lC\u001e,\u0017\u0002BAJ\u0003+\u00131aU3r\u0015\r\tyi\u000b\u0005\u0006\u001f.\u0001\r\u0001U\u0001\fi\u0006\u0014G.Z#ySN$8\u000fF\u0002F\u0003;CQa\u0014\u0007A\u0002A\u000b1dZ3u\t&\u001cHO]5ckR,G\r\u0016:b]N\f7\r^5p]&#GCAAR!\u00111\u0015*a\u0019\u0002\u001bQ\u0014XO\\2bi\u0016$\u0016M\u00197f)\r)\u0015\u0011\u0016\u0005\u0006\u001f:\u0001\r\u0001U\u0001\nIJ|\u0007\u000fV1cY\u0016$2!RAX\u0011\u0015yu\u00021\u0001Q\u0003U9%/Z3oa2,X\u000eV1cY\u0016l\u0015M\\1hKJ\u0004\"!Q\t\u0014\u0005EICCAAZ\u000399\u0007\u000f\u001a2D_2,XN\u001c+za\u0016$B!!0\u0002DB)!&a0\u0002d%\u0019\u0011\u0011Y\u0016\u0003\r=\u0003H/[8o\u0011\u001d\t)m\u0005a\u0001\u0003\u000f\f!\u0001\u001a;\u0011\u0007Y\u000bI-C\u0002\u0002L^\u0013\u0001\u0002R1uCRK\b/Z\u0001\u0016GJ,\u0017\r^3UC\ndWmQ8mk6tG*[:u)\u0011\t\u0019+!5\t\u000bQ#\u0002\u0019A+\u00025\u001d,GoU;qa>\u0014H/\u001a3Ta\u0006\u00148\u000eR1uCRK\b/Z:\u0016\u0005\u0005]\u0007CBAD\u0003#\u000b9-\u0001\u000bde\u0016\fG/\u001a+bE2,7\u000b^1uK6,g\u000e\u001e\u000b\t\u0003G\u000bi.!9\u0002f\"1\u0011q\u001c\fA\u0002A\u000b\u0011\u0002^1cY\u0016t\u0015-\\3\t\r\u0005\rh\u00031\u0001V\u0003\u0019\u00198\r[3nC\")1M\u0006a\u0001I\u0006yr-\u001a8fe\u0006$X-\u0012=uKJt\u0017\r\u001c+bE2,g*Y7f!J,g-\u001b=\u0015\r\u0005\r\u00141^Ax\u0011\u001d\tio\u0006a\u0001\u0003G\nQ\"\u00199qY&\u001c\u0017\r^5p]&#\u0007bBAp/\u0001\u0007\u00111M\u0001&O\u0016tWM]1uK\u0016CH/\u001a:oC2$\u0016M\u00197f\u001d\u0006lWmQ8mk6t\u0007K]3gSb$\u0002\"a\u0019\u0002v\u0006]\u0018\u0011 \u0005\b\u0003[D\u0002\u0019AA2\u0011\u0019y\u0005\u00041\u0001\u0002d!9\u00111 \rA\u0002\u0005\u0015\u0015aB2pYVlgn]\u0001\u001aO\u0016tWM]1uK\u0016CH/\u001a:oC2$\u0016M\u00197f\u001d\u0006lW\r\u0006\u0007\u0002d\t\u0005!1\u0001B\u0003\u0005\u0013\u0011\u0019\u0002C\u0004\u0002nf\u0001\r!a\u0019\t\r=K\u0002\u0019AA2\u0011\u001d\u00119!\u0007a\u0001\u0003G\n!\"\u001a=fGV$xN]%e\u0011\u001d\u0011Y!\u0007a\u0001\u0005\u001b\t\u0001\u0002\u001e5sK\u0006$\u0017\n\u001a\t\u0004U\t=\u0011b\u0001B\tW\t!Aj\u001c8h\u0011%\tY0\u0007I\u0001\u0002\u0004\t))A\u0012hK:,'/\u0019;f\u000bb$XM\u001d8bYR\u000b'\r\\3OC6,G\u0005Z3gCVdG\u000fJ\u001b\u0016\u0005\te!fAACg\u0006aam\u001c7eK\u0012lE-\u000e%fqR!\u00111\rB\u0010\u0011\u001d\u0011\tc\u0007a\u0001\u0003G\n\u0011a\u001d")
public class GreenplumTableManager
implements LazyLogging {
    private final SqlExecutor sqlExecutor;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static Seq<String> generateExternalTableName$default$5() {
        return GreenplumTableManager$.MODULE$.generateExternalTableName$default$5();
    }

    public static String generateExternalTableName(String string, String string2, String string3, long l, Seq<String> seq) {
        return GreenplumTableManager$.MODULE$.generateExternalTableName(string, string2, string3, l, seq);
    }

    public static String generateExternalTableNamePrefix(String string, String string2) {
        return GreenplumTableManager$.MODULE$.generateExternalTableNamePrefix(string, string2);
    }

    public static Try<String> createTableStatement(GreenplumQualifiedName greenplumQualifiedName, StructType structType, GreenplumOptions greenplumOptions) {
        return GreenplumTableManager$.MODULE$.createTableStatement(greenplumQualifiedName, structType, greenplumOptions);
    }

    public static Seq<DataType> getSupportedSparkDataTypes() {
        return GreenplumTableManager$.MODULE$.getSupportedSparkDataTypes();
    }

    public static Try<String> createTableColumnList(StructType structType) {
        return GreenplumTableManager$.MODULE$.createTableColumnList(structType);
    }

    public static Option<String> gpdbColumnType(DataType dataType) {
        return GreenplumTableManager$.MODULE$.gpdbColumnType(dataType);
    }

    private Logger logger$lzycompute() {
        GreenplumTableManager greenplumTableManager = this;
        synchronized (greenplumTableManager) {
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

    public Try<Object> prepareTableForWrite(GreenplumQualifiedName table, StructType sparkSchema, GreenplumOptions options, SaveMode mode) {
        Try<Object> try_ = this.tableExists(table);
        if (try_ instanceof Failure) {
            Failure failure = (Failure)try_;
            Throwable exception = failure.exception();
            return new Failure(exception);
        }
        if (try_ instanceof Success) {
            Success success = (Success)try_;
            boolean exists = BoxesRunTime.unboxToBoolean((Object)success.value());
            if (exists) {
                SaveMode saveMode = mode;
                SaveMode saveMode2 = SaveMode.ErrorIfExists;
                if (!(saveMode != null ? !saveMode.equals(saveMode2) : saveMode2 != null)) {
                    return new Failure((Throwable)new RuntimeException(new StringBuilder(55).append("Table ").append(table).append(" exists, and SaveMode.ErrorIfExists was specified").toString()));
                }
                SaveMode saveMode3 = mode;
                SaveMode saveMode4 = SaveMode.Overwrite;
                if (!(saveMode3 != null ? !saveMode3.equals(saveMode4) : saveMode4 != null)) {
                    return this.overwriteTable(table, sparkSchema, options).map((Function1 & Serializable & scala.Serializable)x$1 -> BoxesRunTime.boxToBoolean((boolean)GreenplumTableManager.$anonfun$prepareTableForWrite$1(x$1)));
                }
                return new Success((Object)BoxesRunTime.boxToBoolean((boolean)false));
            }
            return this.createTable(table, sparkSchema, options).map((Function1 & Serializable & scala.Serializable)x$2 -> BoxesRunTime.boxToBoolean((boolean)GreenplumTableManager.$anonfun$prepareTableForWrite$2(x$2)));
        }
        throw new MatchError(try_);
    }

    public SaveMode prepareTableForWrite$default$4() {
        return SaveMode.ErrorIfExists;
    }

    public Try<Object> overwriteTable(GreenplumQualifiedName table, StructType sparkSchema, GreenplumOptions options) {
        if (options.truncateTable()) {
            return this.truncateTable(table);
        }
        return this.dropTable(table).flatMap((Function1 & Serializable & scala.Serializable)_ -> GreenplumTableManager.$anonfun$overwriteTable$1(this, table, sparkSchema, options, BoxesRunTime.unboxToBoolean((Object)_)));
    }

    private Try<BoxedUnit> createTable(GreenplumQualifiedName table, StructType sparkSchema, GreenplumOptions options) {
        return GreenplumTableManager$.MODULE$.createTableStatement(table, sparkSchema, options).flatMap((Function1 & Serializable & scala.Serializable)sql -> $this.sqlExecutor.execute((String)sql).map((Function1 & Serializable & scala.Serializable)_ -> {
            GreenplumTableManager.$anonfun$createTable$2(this, table, BoxesRunTime.unboxToBoolean((Object)_));
            return BoxedUnit.UNIT;
        })).recoverWith(ErrorHandling$.MODULE$.wrapErrorMessage(new StringBuilder(31).append("Exception while creating table ").append(table).toString()));
    }

    public Try<BoxedUnit> createReadableExternalTableIfNotExists(GreenplumQualifiedName internalTable, GreenplumQualifiedName.TempTable externalTable, GpfdistLocation gpfdistLocation) {
        boolean bl;
        boolean bl2 = false;
        Success success = null;
        Try<Object> try_ = this.tableExists(externalTable);
        if (try_ instanceof Success) {
            bl2 = true;
            success = (Success)try_;
            boolean bl3 = BoxesRunTime.unboxToBoolean((Object)success.value());
            if (bl3) {
                return new Success((Object)BoxedUnit.UNIT);
            }
        }
        if (bl2 && !(bl = BoxesRunTime.unboxToBoolean((Object)success.value()))) {
            return this.createReadableExternalTable(internalTable, externalTable, gpfdistLocation);
        }
        if (try_ instanceof Failure) {
            Failure failure = (Failure)try_;
            Throwable exception = failure.exception();
            return new Failure(exception);
        }
        throw new MatchError(try_);
    }

    private Try<BoxedUnit> createReadableExternalTable(GreenplumQualifiedName internalTable, GreenplumQualifiedName.TempTable externalTable, GpfdistLocation gpfdistLocation) {
        String sql = this.createReadableExternalTableSql(internalTable, externalTable, gpfdistLocation);
        Try<Object> try_ = this.sqlExecutor.execute(sql);
        if (try_ instanceof Success) {
            BoxedUnit boxedUnit;
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("External table {} not found, created table with location='{}'", new Object[]{externalTable, gpfdistLocation.getUrl()});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return new Success((Object)BoxedUnit.UNIT);
        }
        if (try_ instanceof Failure) {
            BoxedUnit boxedUnit;
            Failure failure = (Failure)try_;
            Throwable exception = failure.exception();
            if (this.logger().underlying().isErrorEnabled()) {
                this.logger().underlying().error(new StringBuilder(58).append("Exception while creating external table ").append(externalTable).append("  with location='").append(gpfdistLocation.getUrl()).append("'").toString(), exception);
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return new Failure(exception);
        }
        throw new MatchError(try_);
    }

    public Try<Object> copyTableFromExternal(GreenplumQualifiedName extTable, GreenplumQualifiedName target) {
        String sqlQuery = new StringBuilder(27).append("INSERT INTO ").append(target).append(" SELECT * FROM ").append(extTable).toString();
        return this.sqlExecutor.executeUpdate(sqlQuery);
    }

    private String createReadableExternalTableSql(GreenplumQualifiedName internalTable, GreenplumQualifiedName.TempTable externalTable, GpfdistLocation gpfdistLocation) {
        BoxedUnit boxedUnit;
        String url = gpfdistLocation.getUrl();
        String sqlQuery = Jdbc$.MODULE$.formatSqlQuery(new StringBuilder(173).append("CREATE READABLE EXTERNAL TEMP TABLE\n         |").append(externalTable).append(" (LIKE ").append(internalTable).append(")\n         |LOCATION ('").append(url).append("')\n         |FORMAT 'CSV'\n         |(DELIMITER AS '").append(GreenplumCSVFormat$.MODULE$.CHAR_DELIMITER()).append("'\n         | NULL AS '").append(GreenplumCSVFormat$.MODULE$.VALUE_OF_NULL()).append("')\n         |ENCODING '").append(GreenplumCSVFormat$.MODULE$.DEFAULT_ENCODING()).append("'").toString());
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("Create readable external table query: {}", new Object[]{sqlQuery});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return sqlQuery;
    }

    public Try<Seq<String>> getColumnNames(GreenplumQualifiedName table) {
        return this.sqlExecutor.executeQuery(new StringBuilder(22).append("SELECT * FROM ").append(table).append(" LIMIT 0").toString(), (Function1 & Serializable & scala.Serializable)result -> Jdbc$.MODULE$.getColumnNames(result.getMetaData()));
    }

    public Try<Object> tableExists(GreenplumQualifiedName table) {
        String sqlQuery = new StringBuilder(59).append("SELECT EXISTS ( SELECT 1 FROM information_schema.tables ").append(table.preparedSQL()).append(" );").toString();
        return this.sqlExecutor.executeQuery(sqlQuery, table.queryArgs(), (Function1 & Serializable & scala.Serializable)result -> BoxesRunTime.boxToBoolean((boolean)GreenplumTableManager.$anonfun$tableExists$1(result))).recoverWith(ErrorHandling$.MODULE$.wrapErrorMessage(new StringBuilder(33).append("Cannot determine if table ").append(table).append(" exists").toString()));
    }

    public Try<String> getDistributedTransactionId() {
        String sqlQuery = new StringOps(Predef$.MODULE$.augmentString("\n        |SELECT x.distributed_id\n        |FROM pg_settings s, gp_distributed_xacts x\n        |WHERE s.setting::int = x.gp_session_id\n        |AND s.name = 'gp_session_id'\n        |AND x.state IN ('Active Distributed', 'None')\n        |")).stripMargin();
        return this.sqlExecutor.executeQuery(sqlQuery, (Function1 & Serializable & scala.Serializable)resultSet -> {
            resultSet.next();
            return resultSet.getString("distributed_id");
        }).recoverWith(ErrorHandling$.MODULE$.wrapErrorMessage("cannot get distirbuted id"));
    }

    public Try<Object> truncateTable(GreenplumQualifiedName table) {
        String sqlQuery = new StringBuilder(15).append("TRUNCATE TABLE ").append(table).toString();
        return this.sqlExecutor.execute(sqlQuery).recoverWith(ErrorHandling$.MODULE$.wrapErrorMessage(new StringBuilder(33).append("Exception while truncating table ").append(table).toString()));
    }

    public Try<Object> dropTable(GreenplumQualifiedName table) {
        String sqlQuery = new StringBuilder(11).append("DROP TABLE ").append(table).toString();
        return this.sqlExecutor.execute(sqlQuery).recoverWith(ErrorHandling$.MODULE$.wrapErrorMessage(new StringBuilder(31).append("Exception while dropping table ").append(table).toString()));
    }

    public static final /* synthetic */ boolean $anonfun$prepareTableForWrite$1(Object x$1) {
        return true;
    }

    public static final /* synthetic */ boolean $anonfun$prepareTableForWrite$2(BoxedUnit x$2) {
        return true;
    }

    public static final /* synthetic */ void $anonfun$overwriteTable$2(GreenplumTableManager $this, GreenplumQualifiedName table$1, BoxedUnit _) {
        BoxedUnit boxedUnit;
        if ($this.logger().underlying().isDebugEnabled()) {
            $this.logger().underlying().debug("Table {} was successfully dropped and created", new Object[]{table$1});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
    }

    public static final /* synthetic */ Try $anonfun$overwriteTable$1(GreenplumTableManager $this, GreenplumQualifiedName table$1, StructType sparkSchema$1, GreenplumOptions options$1, boolean _2) {
        return $this.createTable(table$1, sparkSchema$1, options$1).map((Function1 & Serializable & scala.Serializable)_ -> {
            GreenplumTableManager.$anonfun$overwriteTable$2($this, table$1, _);
            return BoxedUnit.UNIT;
        });
    }

    public static final /* synthetic */ void $anonfun$createTable$2(GreenplumTableManager $this, GreenplumQualifiedName table$2, boolean _) {
        BoxedUnit boxedUnit;
        if ($this.logger().underlying().isDebugEnabled()) {
            $this.logger().underlying().debug("Table {} was successfully created", new Object[]{table$2});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
    }

    public static final /* synthetic */ boolean $anonfun$tableExists$1(ResultSet result) {
        result.next();
        return result.getBoolean(1);
    }

    public GreenplumTableManager(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
        LazyLogging.$init$(this);
    }
}

