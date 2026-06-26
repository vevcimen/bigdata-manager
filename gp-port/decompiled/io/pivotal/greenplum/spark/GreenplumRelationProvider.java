/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.SparkConf
 *  org.apache.spark.SparkContext
 *  org.apache.spark.rdd.RDD
 *  org.apache.spark.sql.DataFrameReader
 *  org.apache.spark.sql.Dataset
 *  org.apache.spark.sql.Row
 *  org.apache.spark.sql.SQLContext
 *  org.apache.spark.sql.SaveMode
 *  org.apache.spark.sql.sources.BaseRelation
 *  org.apache.spark.sql.sources.CreatableRelationProvider
 *  org.apache.spark.sql.sources.DataSourceRegister
 *  org.apache.spark.sql.sources.RelationProvider
 *  org.apache.spark.sql.types.StructField
 *  org.apache.spark.sql.types.StructType
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.Iterator
 *  scala.collection.Seq
 *  scala.collection.immutable.Map
 *  scala.collection.mutable.ArrayOps$ofInt
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ClassManifestFactory$
 *  scala.reflect.ClassTag$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.LazyRef
 *  scala.runtime.java8.JFunction0$mcI$sp
 *  scala.runtime.java8.JFunction2$mcIII$sp
 *  scala.util.Either
 *  scala.util.Left
 *  scala.util.Right
 */
package io.pivotal.greenplum.spark;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.GreenplumRelation;
import io.pivotal.greenplum.spark.GreenplumRelationProvider$;
import io.pivotal.greenplum.spark.GreenplumRelationProvider$GreenplumDataFrameReader$;
import io.pivotal.greenplum.spark.Partitioner$;
import io.pivotal.greenplum.spark.SqlExecutor;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.conf.GreenplumOptions$;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName$;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager$;
import io.pivotal.greenplum.spark.externaltable.PartitionWriter;
import io.pivotal.greenplum.spark.externaltable.RowTransformer$;
import io.pivotal.greenplum.spark.jdbc.ColumnValueRange;
import io.pivotal.greenplum.spark.jdbc.ConnectionManager$;
import io.pivotal.greenplum.spark.jdbc.Jdbc$;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Properties;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.CreatableRelationProvider;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.apache.spark.sql.sources.RelationProvider;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import resource.Resource$;
import resource.package$;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Some;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.immutable.Map;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ClassManifestFactory$;
import scala.reflect.ClassTag$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.LazyRef;
import scala.runtime.java8.JFunction0;
import scala.runtime.java8.JFunction2;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

@ScalaSignature(bytes="\u0006\u0001\r\u001da\u0001B\u0012%\u00015BQ!\u0015\u0001\u0005\u0002ICQ!\u0016\u0001\u0005BYCQA\u0019\u0001\u0005\u0002\rDaA\u001d\u0001\u0005\u0002\u0011\u001a\bB\u00022\u0001\t\u0003\nI\u0005C\u0004\u0002|\u0001!\t\"! \t\u000f\u0005=\u0005\u0001\"\u0005\u0002\u0012\"9\u0011q\u001b\u0001\u0005\u0012\u0005e\u0007bBAo\u0001\u0011E\u0011q\u001c\u0005\b\u0003G\u0004A\u0011CAs\u0011\u001d\tY\u000f\u0001C\u0005\u0003[<qAa\u0001%\u0011\u0003\u0011)A\u0002\u0004$I!\u0005!q\u0001\u0005\u0007#6!\tA!\u0003\t\u000f\t-Q\u0002\"\u0001\u0003\u000e\u00191!QF\u0007\u0004\u0005_A!Ba\u000e\u0011\u0005\u000b\u0007I\u0011\u0001B\u001d\u0011)\u0011Y\u0004\u0005B\u0001B\u0003%!\u0011\u0003\u0005\u0007#B!\tA!\u0010\t\r\u001d\u0002B\u0011\u0001B#\u0011\u00199\u0003\u0003\"\u0001\u0003`!I!Q\u000f\t\u0012\u0002\u0013\u0005!q\u000f\u0005\n\u0005\u001b\u0003\u0012\u0011!C!\u0005\u001fC\u0011B!%\u0011\u0003\u0003%\tEa%\b\u0013\t}U\"!A\t\u0002\t\u0005f!\u0003B\u0017\u001b\u0005\u0005\t\u0012\u0001BR\u0011\u0019\t&\u0004\"\u0001\u0003&\"9!q\u0015\u000e\u0005\u0006\t%\u0006b\u0002B\\5\u0011\u0015!\u0011\u0018\u0005\n\u0005\u001bT\u0012\u0013!C\u0003\u0005\u001fD\u0011Ba5\u001b\u0003\u0003%)A!6\t\u0013\te'$!A\u0005\u0006\tm\u0007\"\u0003BP\u001b\u0005\u0005I1\u0001Br\u0011\u001d\u00119/\u0004C\u0001\u0005S\u0014\u0011d\u0012:fK:\u0004H.^7SK2\fG/[8o!J|g/\u001b3fe*\u0011QEJ\u0001\u0006gB\f'o\u001b\u0006\u0003O!\n\u0011b\u001a:fK:\u0004H.^7\u000b\u0005%R\u0013a\u00029jm>$\u0018\r\u001c\u0006\u0002W\u0005\u0011\u0011n\\\u0002\u0001'\u0019\u0001a\u0006N!E\u000fB\u0011qFM\u0007\u0002a)\t\u0011'A\u0003tG\u0006d\u0017-\u0003\u00024a\t1\u0011I\\=SK\u001a\u0004\"!N \u000e\u0003YR!a\u000e\u001d\u0002\u000fM|WO]2fg*\u0011\u0011HO\u0001\u0004gFd'BA\u0013<\u0015\taT(\u0001\u0004ba\u0006\u001c\u0007.\u001a\u0006\u0002}\u0005\u0019qN]4\n\u0005\u00013$\u0001\u0005*fY\u0006$\u0018n\u001c8Qe>4\u0018\u000eZ3s!\t)$)\u0003\u0002Dm\t\u0011B)\u0019;b'>,(oY3SK\u001eL7\u000f^3s!\t)T)\u0003\u0002Gm\tI2I]3bi\u0006\u0014G.\u001a*fY\u0006$\u0018n\u001c8Qe>4\u0018\u000eZ3s!\tAu*D\u0001J\u0015\tQ5*\u0001\u0007tG\u0006d\u0017\r\\8hO&twM\u0003\u0002M\u001b\u0006AA/\u001f9fg\u00064WMC\u0001O\u0003\r\u0019w.\\\u0005\u0003!&\u00131\u0002T1{s2{wmZ5oO\u00061A(\u001b8jiz\"\u0012a\u0015\t\u0003)\u0002i\u0011\u0001J\u0001\ng\"|'\u000f\u001e(b[\u0016$\u0012a\u0016\t\u00031~s!!W/\u0011\u0005i\u0003T\"A.\u000b\u0005qc\u0013A\u0002\u001fs_>$h(\u0003\u0002_a\u00051\u0001K]3eK\u001aL!\u0001Y1\u0003\rM#(/\u001b8h\u0015\tq\u0006'\u0001\bde\u0016\fG/\u001a*fY\u0006$\u0018n\u001c8\u0015\u0007\u0011<W\u000e\u0005\u00026K&\u0011aM\u000e\u0002\r\u0005\u0006\u001cXMU3mCRLwN\u001c\u0005\u0006Q\u000e\u0001\r![\u0001\u000bgFd7i\u001c8uKb$\bC\u00016l\u001b\u0005A\u0014B\u000179\u0005)\u0019\u0016\u000bT\"p]R,\u0007\u0010\u001e\u0005\u0006]\u000e\u0001\ra\\\u0001\u000ba\u0006\u0014\u0018-\\3uKJ\u001c\b\u0003\u0002-q/^K!!]1\u0003\u00075\u000b\u0007/A\td_6\u0004X\u000f^3QCJ$\u0018\u000e^5p]N$B\u0002\u001e>\u0002\b\u0005}\u0011qFA\u001a\u0003\u0007\u00022aL;x\u0013\t1\bGA\u0003BeJ\f\u0017\u0010\u0005\u0002Uq&\u0011\u0011\u0010\n\u0002\u0013\u000fJ,WM\u001c9mk6\u0004\u0016M\u001d;ji&|g\u000eC\u0003|\t\u0001\u0007A0\u0001\u0003d_:t\u0007cA?\u0002\u00045\taP\u0003\u0002:\u007f*\u0011\u0011\u0011A\u0001\u0005U\u00064\u0018-C\u0002\u0002\u0006y\u0014!bQ8o]\u0016\u001cG/[8o\u0011\u001d\tI\u0001\u0002a\u0001\u0003\u0017\tQ\u0001^1cY\u0016\u0004B!!\u0004\u0002\u001a9!\u0011qBA\u000b\u001b\t\t\tBC\u0002\u0002\u0014\u0011\nQ\"\u001a=uKJt\u0017\r\u001c;bE2,\u0017\u0002BA\f\u0003#\tac\u0012:fK:\u0004H.^7Rk\u0006d\u0017NZ5fI:\u000bW.Z\u0005\u0005\u00037\tiBA\u0003UC\ndWM\u0003\u0003\u0002\u0018\u0005E\u0001bBA\u0011\t\u0001\u0007\u00111E\u0001\u0007g\u000eDW-\\1\u0011\t\u0005\u0015\u00121F\u0007\u0003\u0003OQ1!!\u000b9\u0003\u0015!\u0018\u0010]3t\u0013\u0011\ti#a\n\u0003\u0015M#(/^2u)f\u0004X\r\u0003\u0004\u00022\u0011\u0001\raV\u0001\u0014a\u0006\u0014H/\u001b;j_:\u001cu\u000e\\;n]:\u000bW.\u001a\u0005\b\u0003k!\u0001\u0019AA\u001c\u0003=\u0001\u0018M\u001d;ji&|gn]\"pk:$\b#B\u0018\u0002:\u0005u\u0012bAA\u001ea\t1q\n\u001d;j_:\u00042aLA \u0013\r\t\t\u0005\r\u0002\u0004\u0013:$\bbBA#\t\u0001\u0007\u0011qI\u0001\rOB\u001cVmZ7f]RLEm\u001d\t\u0005_U\fi\u0004F\u0005e\u0003\u0017\ni%a\u0016\u0002Z!)\u0001.\u0002a\u0001S\"9\u0011qJ\u0003A\u0002\u0005E\u0013\u0001B7pI\u0016\u00042A[A*\u0013\r\t)\u0006\u000f\u0002\t'\u00064X-T8eK\")a.\u0002a\u0001_\"9\u00111L\u0003A\u0002\u0005u\u0013\u0001\u00023bi\u0006\u0004B!a\u0018\u0002v9!\u0011\u0011MA9\u001d\u0011\t\u0019'a\u001c\u000f\t\u0005\u0015\u0014Q\u000e\b\u0005\u0003O\nYGD\u0002[\u0003SJ\u0011AP\u0005\u0003yuJ!!J\u001e\n\u0005eR\u0014bAA:q\u00059\u0001/Y2lC\u001e,\u0017\u0002BA<\u0003s\u0012\u0011\u0002R1uC\u001a\u0013\u0018-\\3\u000b\u0007\u0005M\u0004(A\bhKR$\u0016M\u00197f\u001b\u0006t\u0017mZ3s)\u0011\ty(!\"\u0011\t\u0005=\u0011\u0011Q\u0005\u0005\u0003\u0007\u000b\tBA\u000bHe\u0016,g\u000e\u001d7v[R\u000b'\r\\3NC:\fw-\u001a:\t\u000f\u0005\u001de\u00011\u0001\u0002\n\u0006AQ\r_3dkR|'\u000fE\u0002U\u0003\u0017K1!!$%\u0005-\u0019\u0016\u000f\\#yK\u000e,Ho\u001c:\u0002%\u001d,G\u000fU1si&$\u0018n\u001c8Xe&$XM\u001d\u000b\t\u0003'\u000b\t,!1\u0002NBIq&!&\u0002>\u0005e\u0015qV\u0005\u0004\u0003/\u0003$!\u0003$v]\u000e$\u0018n\u001c83!\u0019\tY*a)\u0002*:!\u0011QTAQ\u001d\rQ\u0016qT\u0005\u0002c%\u0019\u00111\u000f\u0019\n\t\u0005\u0015\u0016q\u0015\u0002\t\u0013R,'/\u0019;pe*\u0019\u00111\u000f\u0019\u0011\u0007)\fY+C\u0002\u0002.b\u00121AU8x!\u0019\tY*a)\u0002>!9\u00111W\u0004A\u0002\u0005U\u0016\u0001E4sK\u0016t\u0007\u000f\\;n\u001fB$\u0018n\u001c8t!\u0011\t9,!0\u000e\u0005\u0005e&bAA^I\u0005!1m\u001c8g\u0013\u0011\ty,!/\u0003!\u001d\u0013X-\u001a8qYVlw\n\u001d;j_:\u001c\bbBAb\u000f\u0001\u0007\u0011QY\u0001\rgB\f'o[\"p]R,\u0007\u0010\u001e\t\u0005\u0003\u000f\fI-D\u0001;\u0013\r\tYM\u000f\u0002\r'B\f'o[\"p]R,\u0007\u0010\u001e\u0005\b\u0003\u001f<\u0001\u0019AAi\u00039\u0011xn\u001e+sC:\u001chm\u001c:nKJ\u0004raLAj\u0003S\u000bI+C\u0002\u0002VB\u0012\u0011BR;oGRLwN\\\u0019\u0002\u001f\u001d,Go\u00159be.\u001cuN\u001c;fqR$B!!2\u0002\\\")\u0001\u000e\u0003a\u0001S\u0006iq-\u001a;D_:tWm\u0019;j_:$2\u0001`Aq\u0011\u001d\t\u0019,\u0003a\u0001\u0003k\u000babZ3u'FdW\t_3dkR|'\u000f\u0006\u0003\u0002\n\u0006\u001d\bBBAu\u0015\u0001\u0007A0\u0001\u0006d_:tWm\u0019;j_:\fQb]1wK\u0012\u000bG/\u0019$sC6,G\u0003DA\u001f\u0003_\f\u00190!@\u0002\u0000\n\u0005\u0001bBAy\u0017\u0001\u0007\u0011qP\u0001\ri\u0006\u0014G.Z'b]\u0006<WM\u001d\u0005\b\u0003k\\\u0001\u0019AA|\u0003%!Wm\u001d;UC\ndW\r\u0005\u0003\u0002\u0010\u0005e\u0018\u0002BA~\u0003#\u0011ac\u0012:fK:\u0004H.^7Rk\u0006d\u0017NZ5fI:\u000bW.\u001a\u0005\b\u00037Z\u0001\u0019AA/\u0011\u001d\t\u0019l\u0003a\u0001\u0003kCq!a1\f\u0001\u0004\t)-A\rHe\u0016,g\u000e\u001d7v[J+G.\u0019;j_:\u0004&o\u001c<jI\u0016\u0014\bC\u0001+\u000e'\tia\u0006\u0006\u0002\u0003\u0006\u0005)\u0011\r\u001d9msV!!q\u0002B\u000e)\u0011\u0011\tBa\u0006\u0011\u0007)\u0014\u0019\"C\u0002\u0003\u0016a\u0012q\u0002R1uC\u001a\u0013\u0018-\\3SK\u0006$WM\u001d\u0005\b\u00053y\u00019\u0001B\t\u0003\u0005!Ha\u0002B\u000f\u001f\t\u0007!q\u0004\u0002\u0002\u0003F!!\u0011\u0005B\u0014!\ry#1E\u0005\u0004\u0005K\u0001$a\u0002(pi\"Lgn\u001a\t\u0004_\t%\u0012b\u0001B\u0016a\t\u0019\u0011I\\=\u00031\u001d\u0013X-\u001a8qYVlG)\u0019;b\rJ\fW.\u001a*fC\u0012,'oE\u0002\u0011\u0005c\u00012a\fB\u001a\u0013\r\u0011)\u0004\r\u0002\u0007\u0003:Lh+\u00197\u0002\rI,\u0017\rZ3s+\t\u0011\t\"A\u0004sK\u0006$WM\u001d\u0011\u0015\t\t}\"1\t\t\u0004\u0005\u0003\u0002R\"A\u0007\t\u000f\t]2\u00031\u0001\u0003\u0012QA\u0011Q\fB$\u0005\u0017\u0012y\u0005\u0003\u0004\u0003JQ\u0001\raV\u0001\bU\u0012\u00147-\u0016:m\u0011\u0019\u0011i\u0005\u0006a\u0001/\u0006IA/\u00192mK:\u000bW.\u001a\u0005\b\u0005#\"\u0002\u0019\u0001B*\u0003)\u0001(o\u001c9feRLWm\u001d\t\u0005\u0005+\u0012Y&\u0004\u0002\u0003X)\u0019!\u0011L@\u0002\tU$\u0018\u000e\\\u0005\u0005\u0005;\u00129F\u0001\u0006Qe>\u0004XM\u001d;jKN$\u0002#!\u0018\u0003b\t\r$Q\rB4\u0005W\u0012yGa\u001d\t\r\t%S\u00031\u0001X\u0011\u0019\u0011i%\u0006a\u0001/\"1\u0011\u0011E\u000bA\u0002]CaA!\u001b\u0016\u0001\u00049\u0016\u0001C;tKJt\u0017-\\3\t\r\t5T\u00031\u0001X\u0003!\u0001\u0018m]:x_J$\u0007B\u0002B9+\u0001\u0007q+A\bqCJ$\u0018\u000e^5p]\u000e{G.^7o\u0011%\u0011\t&\u0006I\u0001\u0002\u0004\u0011\u0019&A\nhe\u0016,g\u000e\u001d7v[\u0012\"WMZ1vYR$s'\u0006\u0002\u0003z)\"!1\u000bB>W\t\u0011i\b\u0005\u0003\u0003\u0000\t%UB\u0001BA\u0015\u0011\u0011\u0019I!\"\u0002\u0013Ut7\r[3dW\u0016$'b\u0001BDa\u0005Q\u0011M\u001c8pi\u0006$\u0018n\u001c8\n\t\t-%\u0011\u0011\u0002\u0012k:\u001c\u0007.Z2lK\u00124\u0016M]5b]\u000e,\u0017\u0001\u00035bg\"\u001cu\u000eZ3\u0015\u0005\u0005u\u0012AB3rk\u0006d7\u000f\u0006\u0003\u0003\u0016\nm\u0005cA\u0018\u0003\u0018&\u0019!\u0011\u0014\u0019\u0003\u000f\t{w\u000e\\3b]\"I!Q\u0014\r\u0002\u0002\u0003\u0007!qE\u0001\u0004q\u0012\n\u0014\u0001G$sK\u0016t\u0007\u000f\\;n\t\u0006$\u0018M\u0012:b[\u0016\u0014V-\u00193feB\u0019!\u0011\t\u000e\u0014\u0005iqCC\u0001BQ\u0003Q9'/Z3oa2,X\u000eJ3yi\u0016t7/[8oaQ!!1\u0016BZ)!\tiF!,\u00030\nE\u0006B\u0002B%9\u0001\u0007q\u000b\u0003\u0004\u0003Nq\u0001\ra\u0016\u0005\b\u0005#b\u0002\u0019\u0001B*\u0011\u001d\u0011)\f\ba\u0001\u0005\u007f\tQ\u0001\n;iSN\fAc\u001a:fK:\u0004H.^7%Kb$XM\\:j_:\fD\u0003\u0002B^\u0005\u0017$\u0002#!\u0018\u0003>\n}&\u0011\u0019Bb\u0005\u000b\u00149M!3\t\r\t%S\u00041\u0001X\u0011\u0019\u0011i%\ba\u0001/\"1\u0011\u0011E\u000fA\u0002]CaA!\u001b\u001e\u0001\u00049\u0006B\u0002B7;\u0001\u0007q\u000b\u0003\u0004\u0003ru\u0001\ra\u0016\u0005\n\u0005#j\u0002\u0013!a\u0001\u0005'BqA!.\u001e\u0001\u0004\u0011y$A\u000fhe\u0016,g\u000e\u001d7v[\u0012\"WMZ1vYR$s\u0007J3yi\u0016t7/[8o)\u0011\u0011IH!5\t\u000f\tUf\u00041\u0001\u0003@\u0005\u0011\u0002.Y:i\u0007>$W\rJ3yi\u0016t7/[8o)\u0011\u0011yIa6\t\u000f\tUv\u00041\u0001\u0003@\u0005\u0001R-];bYN$S\r\u001f;f]NLwN\u001c\u000b\u0005\u0005;\u0014\t\u000f\u0006\u0003\u0003\u0016\n}\u0007\"\u0003BOA\u0005\u0005\t\u0019\u0001B\u0014\u0011\u001d\u0011)\f\ta\u0001\u0005\u007f!BAa\u0010\u0003f\"9!qG\u0011A\u0002\tE\u0011\u0001G2iK\u000e\\\u0007+\u0019:uSRLwN\\\"pYVlg\u000eV=qKR!!1\u001eB\u007f!!\tYJ!<\u0003r\n]\u0018\u0002\u0002Bx\u0003O\u0013a!R5uQ\u0016\u0014\bcA\u0018\u0003t&\u0019!Q\u001f\u0019\u0003\tUs\u0017\u000e\u001e\t\u0005\u00037\u0013I0\u0003\u0003\u0003|\u0006\u001d&\u0001G%mY\u0016<\u0017\r\\!sOVlWM\u001c;Fq\u000e,\u0007\u000f^5p]\"9!q \u0012A\u0002\r\u0005\u0011AB2pYVlg\u000e\u0005\u0003\u0002&\r\r\u0011\u0002BB\u0003\u0003O\u00111b\u0015;sk\u000e$h)[3mI\u0002")
public class GreenplumRelationProvider
implements RelationProvider,
DataSourceRegister,
CreatableRelationProvider,
LazyLogging {
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static Either<BoxedUnit, IllegalArgumentException> checkPartitionColumnType(StructField structField) {
        return GreenplumRelationProvider$.MODULE$.checkPartitionColumnType(structField);
    }

    public static DataFrameReader GreenplumDataFrameReader(DataFrameReader dataFrameReader) {
        return GreenplumRelationProvider$.MODULE$.GreenplumDataFrameReader(dataFrameReader);
    }

    public static <A> DataFrameReader apply(DataFrameReader dataFrameReader) {
        return GreenplumRelationProvider$.MODULE$.apply(dataFrameReader);
    }

    private Logger logger$lzycompute() {
        GreenplumRelationProvider greenplumRelationProvider = this;
        synchronized (greenplumRelationProvider) {
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

    public String shortName() {
        return "greenplum";
    }

    public BaseRelation createRelation(SQLContext sqlContext, Map<String, String> parameters) {
        GreenplumRelation greenplumRelation;
        SparkContext sparkContext = this.getSparkContext(sqlContext);
        SparkConf sparkConf = sparkContext.getConf();
        GreenplumOptions greenplumOptions = new GreenplumOptions(parameters, sparkConf);
        try (Connection conn = ConnectionManager$.MODULE$.getConnection(greenplumOptions, ConnectionManager$.MODULE$.getConnection$default$2());){
            BoxedUnit boxedUnit;
            GreenplumQualifiedName.Table table = GreenplumQualifiedName$.MODULE$.forTable(greenplumOptions.dbSchema(), greenplumOptions.dbTable());
            String url = greenplumOptions.url();
            StructType schema = Jdbc$.MODULE$.resolveTable(conn, url, table);
            String partitionColumnName = greenplumOptions.partitionColumn();
            Option<Object> partitionsCount = greenplumOptions.partitions();
            int[] gpSegmentIds = Jdbc$.MODULE$.retrieveSegmentIds(conn);
            GreenplumPartition[] partitions = this.computePartitions(conn, table, schema, partitionColumnName, partitionsCount, gpSegmentIds);
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("NumPartitions = {}", new Object[]{BoxesRunTime.boxToInteger((int)partitions.length)});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            greenplumRelation = new GreenplumRelation(schema, partitions, greenplumOptions, sqlContext);
        }
        return greenplumRelation;
    }

    public GreenplumPartition[] computePartitions(Connection conn, GreenplumQualifiedName.Table table, StructType schema, String partitionColumnName, Option<Object> partitionsCount, int[] gpSegmentIds) {
        String string = partitionColumnName;
        String string2 = GreenplumOptions$.MODULE$.DEFAULT_PARTITION_COLUMN_NAME();
        if (!(string != null ? !string.equals(string2) : string2 != null)) {
            Predef$.MODULE$.assert(partitionsCount.isEmpty(), (Function0 & Serializable & scala.Serializable)() -> new StringBuilder(49).append("When using '").append(partitionColumnName).append("' for '").append(GreenplumOptions$.MODULE$.GPDB_PARTITION_COLUMN()).append("', ").append("'").append(GreenplumOptions$.MODULE$.GPDB_PARTITIONS()).append("' is expected to be empty.").toString());
            return Partitioner$.MODULE$.segmentPartitions(gpSegmentIds);
        }
        Option option = new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])schema.fields())).find((Function1 & Serializable & scala.Serializable)x$1 -> BoxesRunTime.boxToBoolean((boolean)GreenplumRelationProvider.$anonfun$computePartitions$2(partitionColumnName, x$1)));
        if (!(option instanceof Some)) {
            if (None$.MODULE$.equals(option)) {
                throw new IllegalArgumentException(new StringBuilder(27).append("'").append(partitionColumnName).append("' does not exist in ").append(table).append(" table").toString());
            }
            throw new MatchError((Object)option);
        }
        Some some = (Some)option;
        StructField structField = (StructField)some.value();
        StructField partitionColumn = structField;
        int numPartitions = BoxesRunTime.unboxToInt((Object)partitionsCount.getOrElse((Function0)(JFunction0.mcI.sp & Serializable & scala.Serializable)() -> new ArrayOps.ofInt(Predef$.MODULE$.intArrayOps(gpSegmentIds)).size()));
        Either<BoxedUnit, IllegalArgumentException> either = GreenplumRelationProvider$.MODULE$.checkPartitionColumnType(partitionColumn);
        if (!(either instanceof Left)) {
            if (either instanceof Right) {
                Right right = (Right)either;
                IllegalArgumentException e = (IllegalArgumentException)right.value();
                throw e;
            }
            throw new MatchError(either);
        }
        ColumnValueRange range = Jdbc$.MODULE$.computeColumnValueRange(conn, table, partitionColumnName);
        return Partitioner$.MODULE$.columnPartitions(numPartitions, partitionColumn, range);
    }

    public BaseRelation createRelation(SQLContext sqlContext, SaveMode mode, Map<String, String> parameters, Dataset<Row> data) {
        LazyRef relation$lzy = new LazyRef();
        SparkContext sparkContext = this.getSparkContext(sqlContext);
        SparkConf sparkConf = sparkContext.getConf();
        GreenplumOptions greenplumOptions = new GreenplumOptions(parameters, sparkConf);
        GreenplumQualifiedName.Table destTable = GreenplumQualifiedName$.MODULE$.forTable(greenplumOptions.dbSchema(), greenplumOptions.dbTable());
        String tablePrefix = GreenplumTableManager$.MODULE$.generateExternalTableNamePrefix(sparkContext.applicationId(), greenplumOptions.dbTable());
        package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> this.getConnection(greenplumOptions), Resource$.MODULE$.connectionResource(), ClassManifestFactory$.MODULE$.classType(Connection.class)).foreach((Function1 & Serializable & scala.Serializable)connection -> {
            GreenplumRelationProvider.$anonfun$createRelation$2(this, destTable, data, greenplumOptions, mode, sparkContext, connection);
            return BoxedUnit.UNIT;
        });
        return new BaseRelation(this, relation$lzy, sqlContext, parameters){
            private StructType schema;
            private SQLContext sqlContext;
            private volatile byte bitmap$0;
            private final /* synthetic */ GreenplumRelationProvider $outer;
            private final LazyRef relation$lzy$1;
            private final SQLContext sqlContext$1;
            private final Map parameters$1;

            private StructType schema$lzycompute() {
                $anon$1 var1_1 = this;
                synchronized (var1_1) {
                    if ((byte)(this.bitmap$0 & 1) == 0) {
                        this.schema = this.$outer.io$pivotal$greenplum$spark$GreenplumRelationProvider$$relation$1(this.relation$lzy$1, this.sqlContext$1, this.parameters$1).schema();
                        this.bitmap$0 = (byte)(this.bitmap$0 | 1);
                    }
                }
                return this.schema;
            }

            public StructType schema() {
                if ((byte)(this.bitmap$0 & 1) == 0) {
                    return this.schema$lzycompute();
                }
                return this.schema;
            }

            private SQLContext sqlContext$lzycompute() {
                $anon$1 var1_1 = this;
                synchronized (var1_1) {
                    if ((byte)(this.bitmap$0 & 2) == 0) {
                        this.sqlContext = this.$outer.io$pivotal$greenplum$spark$GreenplumRelationProvider$$relation$1(this.relation$lzy$1, this.sqlContext$1, this.parameters$1).sqlContext();
                        this.bitmap$0 = (byte)(this.bitmap$0 | 2);
                    }
                }
                return this.sqlContext;
            }

            public SQLContext sqlContext() {
                if ((byte)(this.bitmap$0 & 2) == 0) {
                    return this.sqlContext$lzycompute();
                }
                return this.sqlContext;
            }
            {
                if ($outer == null) {
                    throw null;
                }
                this.$outer = $outer;
                this.relation$lzy$1 = relation$lzy$1;
                this.sqlContext$1 = sqlContext$1;
                this.parameters$1 = parameters$1;
            }
        };
    }

    public GreenplumTableManager getTableManager(SqlExecutor executor) {
        return new GreenplumTableManager(executor);
    }

    public Function2<Object, Iterator<Row>, Iterator<Object>> getPartitionWriter(GreenplumOptions greenplumOptions, SparkContext sparkContext, Function1<Row, Row> rowTransformer) {
        return new PartitionWriter(sparkContext.applicationId(), greenplumOptions, rowTransformer).getClosure();
    }

    public SparkContext getSparkContext(SQLContext sqlContext) {
        return sqlContext.sparkContext();
    }

    public Connection getConnection(GreenplumOptions greenplumOptions) {
        return ConnectionManager$.MODULE$.getConnection(greenplumOptions, ConnectionManager$.MODULE$.getConnection$default$2());
    }

    public SqlExecutor getSqlExecutor(Connection connection) {
        return new SqlExecutor(connection);
    }

    private int saveDataFrame(GreenplumTableManager tableManager, GreenplumQualifiedName destTable, Dataset<Row> data, GreenplumOptions greenplumOptions, SparkContext sparkContext) {
        Seq gpdbColumns = (Seq)tableManager.getColumnNames(destTable).get();
        Function1 rowTransformer = (Function1)RowTransformer$.MODULE$.getFunction((Seq<String>)Predef$.MODULE$.wrapRefArray((Object[])data.columns()), (Seq<String>)gpdbColumns).get();
        Function2<Object, Iterator<Row>, Iterator<Object>> partitionWriter = this.getPartitionWriter(greenplumOptions, sparkContext, (Function1<Row, Row>)rowTransformer);
        RDD rowCounts = data.rdd().mapPartitionsWithIndex(partitionWriter, data.rdd().mapPartitionsWithIndex$default$2(), ClassTag$.MODULE$.Int());
        return BoxesRunTime.unboxToInt((Object)rowCounts.fold((Object)BoxesRunTime.boxToInteger((int)0), (Function2)(JFunction2.mcIII.sp & Serializable & scala.Serializable)(x$2, x$3) -> x$2 + x$3));
    }

    public static final /* synthetic */ boolean $anonfun$computePartitions$2(String partitionColumnName$1, StructField x$1) {
        String string = x$1.name();
        String string2 = partitionColumnName$1;
        return !(string != null ? !string.equals(string2) : string2 != null);
    }

    public static final /* synthetic */ void $anonfun$createRelation$2(GreenplumRelationProvider $this, GreenplumQualifiedName.Table destTable$1, Dataset data$1, GreenplumOptions greenplumOptions$1, SaveMode mode$1, SparkContext sparkContext$1, Connection connection) {
        SqlExecutor executor = $this.getSqlExecutor(connection);
        GreenplumTableManager tableManager = $this.getTableManager(executor);
        boolean tableCreated = BoxesRunTime.unboxToBoolean((Object)tableManager.prepareTableForWrite(destTable$1, data$1.schema(), greenplumOptions$1, mode$1).get());
        if (!tableCreated) {
            SaveMode saveMode = mode$1;
            SaveMode saveMode2 = SaveMode.Ignore;
            if (!(saveMode != null ? !saveMode.equals(saveMode2) : saveMode2 != null)) {
                BoxedUnit boxedUnit;
                if ($this.logger().underlying().isDebugEnabled()) {
                    $this.logger().underlying().debug("Table {} already exists with SaveMode.Ignore. Data is ignored.", new Object[]{destTable$1});
                    boxedUnit = BoxedUnit.UNIT;
                } else {
                    boxedUnit = BoxedUnit.UNIT;
                }
                return;
            }
        }
        $this.saveDataFrame(tableManager, destTable$1, (Dataset<Row>)data$1, greenplumOptions$1, sparkContext$1);
    }

    private final /* synthetic */ BaseRelation relation$lzycompute$1(LazyRef relation$lzy$1, SQLContext sqlContext$1, Map parameters$1) {
        BaseRelation baseRelation;
        LazyRef lazyRef = relation$lzy$1;
        synchronized (lazyRef) {
            baseRelation = relation$lzy$1.initialized() ? (BaseRelation)relation$lzy$1.value() : (BaseRelation)relation$lzy$1.initialize((Object)this.createRelation(sqlContext$1, (Map<String, String>)parameters$1));
        }
        return baseRelation;
    }

    public final BaseRelation io$pivotal$greenplum$spark$GreenplumRelationProvider$$relation$1(LazyRef relation$lzy$1, SQLContext sqlContext$1, Map parameters$1) {
        if (relation$lzy$1.initialized()) {
            return (BaseRelation)relation$lzy$1.value();
        }
        return this.relation$lzycompute$1(relation$lzy$1, sqlContext$1, parameters$1);
    }

    public GreenplumRelationProvider() {
        LazyLogging.$init$(this);
    }

    public static final class GreenplumDataFrameReader {
        private final DataFrameReader reader;

        public DataFrameReader reader() {
            return this.reader;
        }

        public Dataset<Row> greenplum(String jdbcUrl, String tableName, Properties properties) {
            return GreenplumRelationProvider$GreenplumDataFrameReader$.MODULE$.greenplum$extension0(this.reader(), jdbcUrl, tableName, properties);
        }

        public Dataset<Row> greenplum(String jdbcUrl, String tableName, String schema, String username, String password, String partitionColumn, Properties properties) {
            return GreenplumRelationProvider$GreenplumDataFrameReader$.MODULE$.greenplum$extension1(this.reader(), jdbcUrl, tableName, schema, username, password, partitionColumn, properties);
        }

        public Properties greenplum$default$7() {
            return GreenplumRelationProvider$GreenplumDataFrameReader$.MODULE$.greenplum$default$7$extension(this.reader());
        }

        public int hashCode() {
            return GreenplumRelationProvider$GreenplumDataFrameReader$.MODULE$.hashCode$extension(this.reader());
        }

        public boolean equals(Object x$1) {
            return GreenplumRelationProvider$GreenplumDataFrameReader$.MODULE$.equals$extension(this.reader(), x$1);
        }

        public GreenplumDataFrameReader(DataFrameReader reader) {
            this.reader = reader;
        }
    }
}

