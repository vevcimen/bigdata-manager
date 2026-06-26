/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.SparkConf
 *  org.apache.spark.sql.catalyst.util.CaseInsensitiveMap
 *  scala.Function2
 *  scala.Option
 *  scala.Serializable
 *  scala.collection.immutable.Map
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.conf;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.conf.ConnectorOptions;
import io.pivotal.greenplum.spark.conf.ConnectorOptions$;
import io.pivotal.greenplum.spark.conf.Default;
import io.pivotal.greenplum.spark.conf.ErrorIfMissing$;
import io.pivotal.greenplum.spark.conf.GreenplumOptions$;
import io.pivotal.greenplum.spark.conf.Options;
import io.pivotal.greenplum.spark.conf.WhatIfMissing;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap;
import org.greenplum.spark.GpfdistConf;
import scala.Function2;
import scala.Option;
import scala.Serializable;
import scala.collection.immutable.Map;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.util.Try;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
@ScalaSignature(bytes="\u0006\u0001\u0005}f\u0001\u0002 @\u0001)C\u0001B\u0019\u0001\u0003\u0006\u0004%\ta\u0019\u0005\te\u0002\u0011\t\u0011)A\u0005I\"A1\u000f\u0001BC\u0002\u0013%A\u000f\u0003\u0005\u007f\u0001\t\u0005\t\u0015!\u0003v\u0011\u0019y\b\u0001\"\u0001\u0002\u0002!A\u0011\u0011\u0002\u0001C\u0002\u0013\u00051\rC\u0004\u0002\f\u0001\u0001\u000b\u0011\u00023\t\u0013\u00055\u0001A1A\u0005\u0002\u0005=\u0001bBA\t\u0001\u0001\u0006Ia\u001c\u0005\n\u0003'\u0001!\u0019!C\u0001\u0003\u001fAq!!\u0006\u0001A\u0003%q\u000eC\u0005\u0002\u0018\u0001\u0011\r\u0011\"\u0001\u0002\u001a!A\u0011\u0011\u0005\u0001!\u0002\u0013\tY\u0002C\u0005\u0002$\u0001\u0011\r\u0011\"\u0001\u0002\u0010!9\u0011Q\u0005\u0001!\u0002\u0013y\u0007\"CA\u0014\u0001\t\u0007I\u0011AA\b\u0011\u001d\tI\u0003\u0001Q\u0001\n=D\u0011\"a\u000b\u0001\u0005\u0004%\t!a\u0004\t\u000f\u00055\u0002\u0001)A\u0005_\"I\u0011q\u0006\u0001C\u0002\u0013\u0005\u0011\u0011\u0007\u0005\t\u0003w\u0001\u0001\u0015!\u0003\u00024!I\u0011Q\b\u0001C\u0002\u0013\u0005\u0011q\u0002\u0005\b\u0003\u007f\u0001\u0001\u0015!\u0003p\u0011%\t\t\u0005\u0001b\u0001\n\u0003\t\u0019\u0005\u0003\u0005\u0002L\u0001\u0001\u000b\u0011BA#\u0011%\ti\u0005\u0001b\u0001\n\u0003\ty\u0005\u0003\u0005\u0002X\u0001\u0001\u000b\u0011BA)\u0011%\tI\u0006\u0001b\u0001\n\u0003\tY\u0006\u0003\u0005\u0002d\u0001\u0001\u000b\u0011BA/\u0011%\t)\u0007\u0001b\u0001\n\u0003\tI\u0002\u0003\u0005\u0002h\u0001\u0001\u000b\u0011BA\u000e\u0011%\tI\u0007\u0001b\u0001\n\u0003\tY\u0006\u0003\u0005\u0002l\u0001\u0001\u000b\u0011BA/\u000f\u001d\tig\u0010E\u0001\u0003_2aAP \t\u0002\u0005E\u0004BB@$\t\u0003\t\u0019\bC\u0005\u0002v\r\u0012\r\u0011\"\u0001\u0002x!A\u0011qQ\u0012!\u0002\u0013\tI\bC\u0005\u0002\n\u000e\u0012\r\u0011\"\u0001\u0002x!A\u00111R\u0012!\u0002\u0013\tI\bC\u0005\u0002\u000e\u000e\u0012\r\u0011\"\u0001\u0002x!A\u0011qR\u0012!\u0002\u0013\tI\bC\u0005\u0002\u0012\u000e\u0012\r\u0011\"\u0001\u0002x!A\u00111S\u0012!\u0002\u0013\tI\bC\u0005\u0002\u0016\u000e\u0012\r\u0011\"\u0001\u0002x!A\u0011qS\u0012!\u0002\u0013\tI\bC\u0005\u0002\u001a\u000e\u0012\r\u0011\"\u0001\u0002x!A\u00111T\u0012!\u0002\u0013\tI\bC\u0005\u0002\u001e\u000e\u0012\r\u0011\"\u0001\u0002x!A\u0011qT\u0012!\u0002\u0013\tI\bC\u0005\u0002\"\u000e\u0012\r\u0011\"\u0001\u0002x!A\u00111U\u0012!\u0002\u0013\tI\bC\u0005\u0002&\u000e\u0012\r\u0011\"\u0001\u0002x!A\u0011qU\u0012!\u0002\u0013\tI\bC\u0005\u0002*\u000e\u0012\r\u0011\"\u0001\u0002x!A\u00111V\u0012!\u0002\u0013\tI\bC\u0005\u0002.\u000e\u0012\r\u0011\"\u0001\u0002x!A\u0011qV\u0012!\u0002\u0013\tI\bC\u0005\u00022\u000e\u0012\r\u0011\"\u0001\u0002x!A\u00111W\u0012!\u0002\u0013\tI\bC\u0005\u00026\u000e\n\t\u0011\"\u0003\u00028\n\u0001rI]3f]BdW/\\(qi&|gn\u001d\u0006\u0003\u0001\u0006\u000bAaY8oM*\u0011!iQ\u0001\u0006gB\f'o\u001b\u0006\u0003\t\u0016\u000b\u0011b\u001a:fK:\u0004H.^7\u000b\u0005\u0019;\u0015a\u00029jm>$\u0018\r\u001c\u0006\u0002\u0011\u0006\u0011\u0011n\\\u0002\u0001'\u0015\u00011*\u0015+Y!\tau*D\u0001N\u0015\u0005q\u0015!B:dC2\f\u0017B\u0001)N\u0005\u0019\te.\u001f*fMB\u0011AJU\u0005\u0003'6\u0013AbU3sS\u0006d\u0017N_1cY\u0016\u0004\"!\u0016,\u000e\u0003}J!aV \u0003\u000f=\u0003H/[8ogB\u0011\u0011\fY\u0007\u00025*\u00111\fX\u0001\rg\u000e\fG.\u00197pO\u001eLgn\u001a\u0006\u0003;z\u000b\u0001\u0002^=qKN\fg-\u001a\u0006\u0002?\u0006\u00191m\\7\n\u0005\u0005T&a\u0003'bufdunZ4j]\u001e\f!\u0002]1sC6,G/\u001a:t+\u0005!\u0007\u0003B3m_>t!A\u001a6\u0011\u0005\u001dlU\"\u00015\u000b\u0005%L\u0015A\u0002\u001fs_>$h(\u0003\u0002l\u001b\u00061\u0001K]3eK\u001aL!!\u001c8\u0003\u00075\u000b\u0007O\u0003\u0002l\u001bB\u0011Q\r]\u0005\u0003c:\u0014aa\u0015;sS:<\u0017a\u00039be\u0006lW\r^3sg\u0002\n\u0011b\u001d9be.\u001cuN\u001c4\u0016\u0003U\u0004\"A\u001e?\u000e\u0003]T!A\u0011=\u000b\u0005eT\u0018AB1qC\u000eDWMC\u0001|\u0003\ry'oZ\u0005\u0003{^\u0014\u0011b\u00159be.\u001cuN\u001c4\u0002\u0015M\u0004\u0018M]6D_:4\u0007%\u0001\u0004=S:LGO\u0010\u000b\u0007\u0003\u0007\t)!a\u0002\u0011\u0005U\u0003\u0001\"\u00022\u0006\u0001\u0004!\u0007\"B:\u0006\u0001\u0004)\u0018AE8sS\u001eLg.\u00197QCJ\fW.\u001a;feN\f1c\u001c:jO&t\u0017\r\u001c)be\u0006lW\r^3sg\u0002\n1!\u001e:m+\u0005y\u0017\u0001B;sY\u0002\nA!^:fe\u0006)Qo]3sA\u0005A\u0001/Y:to>\u0014H-\u0006\u0002\u0002\u001cA!A*!\bp\u0013\r\ty\"\u0014\u0002\u0007\u001fB$\u0018n\u001c8\u0002\u0013A\f7o]<pe\u0012\u0004\u0013\u0001\u00033c'\u000eDW-\\1\u0002\u0013\u0011\u00147k\u00195f[\u0006\u0004\u0013a\u00023c)\u0006\u0014G.Z\u0001\tI\n$\u0016M\u00197fA\u0005y\u0001/\u0019:uSRLwN\\\"pYVlg.\u0001\tqCJ$\u0018\u000e^5p]\u000e{G.^7oA\u0005Q\u0001/\u0019:uSRLwN\\:\u0016\u0005\u0005M\u0002#\u0002'\u0002\u001e\u0005U\u0002c\u0001'\u00028%\u0019\u0011\u0011H'\u0003\u0007%sG/A\u0006qCJ$\u0018\u000e^5p]N\u0004\u0013A\u00023sSZ,'/A\u0004ee&4XM\u001d\u0011\u0002!\r|gN\\3di>\u0014x\n\u001d;j_:\u001cXCAA#!\r)\u0016qI\u0005\u0004\u0003\u0013z$\u0001E\"p]:,7\r^8s\u001fB$\u0018n\u001c8t\u0003E\u0019wN\u001c8fGR|'o\u00149uS>t7\u000fI\u0001\u0016G>tg.Z2uS>t\u0007k\\8m\u001fB$\u0018n\u001c8t+\t\t\t\u0006E\u0002V\u0003'J1!!\u0016@\u0005U\u0019uN\u001c8fGRLwN\u001c)p_2|\u0005\u000f^5p]N\facY8o]\u0016\u001cG/[8o!>|Gn\u00149uS>t7\u000fI\u0001\u000eiJ,hnY1uKR\u000b'\r\\3\u0016\u0005\u0005u\u0003c\u0001'\u0002`%\u0019\u0011\u0011M'\u0003\u000f\t{w\u000e\\3b]\u0006qAO];oG\u0006$X\rV1cY\u0016\u0004\u0013!\u00043jgR\u0014\u0018NY;uK\u0012\u0014\u00150\u0001\beSN$(/\u001b2vi\u0016$')\u001f\u0011\u0002)%$XM]1u_J|\u0005\u000f^5nSj\fG/[8o\u0003UIG/\u001a:bi>\u0014x\n\u001d;j[&T\u0018\r^5p]\u0002\n\u0001c\u0012:fK:\u0004H.^7PaRLwN\\:\u0011\u0005U\u001b3cA\u0012L#R\u0011\u0011qN\u0001\t\u000fB#%iX+S\u0019V\u0011\u0011\u0011\u0010\t\u0005\u0003w\n))\u0004\u0002\u0002~)!\u0011qPAA\u0003\u0011a\u0017M\\4\u000b\u0005\u0005\r\u0015\u0001\u00026bm\u0006L1!]A?\u0003%9\u0005\u000b\u0012\"`+Jc\u0005%A\u0005H!\u0012\u0013u,V*F%\u0006Qq\t\u0015#C?V\u001bVI\u0015\u0011\u0002\u001b\u001d\u0003FIQ0Q\u0003N\u001bvk\u0014*E\u000399\u0005\u000b\u0012\"`!\u0006\u001b6kV(S\t\u0002\n\u0001c\u0012)E\u0005~\u001b6\tS#N\u0003~s\u0015)T#\u0002#\u001d\u0003FIQ0T\u0007\"+U*Q0O\u00036+\u0005%A\bH!\u0012\u0013u\fV!C\u0019\u0016{f*Q'F\u0003A9\u0005\u000b\u0012\"`)\u0006\u0013E*R0O\u00036+\u0005%A\u000bH!\u0012\u0013u\fU!S)&#\u0016j\u0014(`\u0007>cU+\u0014(\u0002-\u001d\u0003FIQ0Q\u0003J#\u0016\nV%P\u001d~\u001bu\nT+N\u001d\u0002\nqb\u0012)E\u0005~\u0003\u0016I\u0015+J)&{ejU\u0001\u0011\u000fB#%i\u0018)B%RKE+S(O'\u0002\n1b\u0012)E\u0005~#%+\u0013,F%\u0006aq\t\u0015#C?\u0012\u0013\u0016JV#SA\u0005\u0019r\t\u0015#C?\u0012K5\u000b\u0016*J\u0005V#V\tR0C3\u0006!r\t\u0015#C?\u0012K5\u000b\u0016*J\u0005V#V\tR0C3\u0002\n1c\u0012)E\u0005~#&+\u0016(D\u0003R+u\fV!C\u0019\u0016\u000bAc\u0012)E\u0005~#&+\u0016(D\u0003R+u\fV!C\u0019\u0016\u0003\u0013AG$Q\t\n{\u0016\nV#S\u0003R{%kX(Q)&k\u0015JW!U\u0013>s\u0015aG$Q\t\n{\u0016\nV#S\u0003R{%kX(Q)&k\u0015JW!U\u0013>s\u0005%A\u000fE\u000b\u001a\u000bU\u000b\u0014+`!\u0006\u0013F+\u0013+J\u001f:{6i\u0014'V\u001b:{f*Q'F\u0003y!UIR!V\u0019R{\u0006+\u0011*U\u0013RKuJT0D\u001f2+VJT0O\u00036+\u0005%A\u0006sK\u0006$'+Z:pYZ,GCAA]!\u0011\tY(a/\n\t\u0005u\u0016Q\u0010\u0002\u0007\u001f\nTWm\u0019;")
public class GreenplumOptions
implements Serializable,
Options,
LazyLogging {
    private final Map<String, String> parameters;
    private final SparkConf sparkConf;
    private final Map<String, String> originalParameters;
    private final String url;
    private final String user;
    private final Option<String> password;
    private final String dbSchema;
    private final String dbTable;
    private final String partitionColumn;
    private final Option<Object> partitions;
    private final String driver;
    private final ConnectorOptions connectorOptions;
    private final ConnectionPoolOptions connectionPoolOptions;
    private final boolean truncateTable;
    private final Option<String> distributedBy;
    private final boolean iteratorOptimization;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static String DEFAULT_PARTITION_COLUMN_NAME() {
        return GreenplumOptions$.MODULE$.DEFAULT_PARTITION_COLUMN_NAME();
    }

    public static String GPDB_ITERATOR_OPTIMIZATION() {
        return GreenplumOptions$.MODULE$.GPDB_ITERATOR_OPTIMIZATION();
    }

    public static String GPDB_TRUNCATE_TABLE() {
        return GreenplumOptions$.MODULE$.GPDB_TRUNCATE_TABLE();
    }

    public static String GPDB_DISTRIBUTED_BY() {
        return GreenplumOptions$.MODULE$.GPDB_DISTRIBUTED_BY();
    }

    public static String GPDB_DRIVER() {
        return GreenplumOptions$.MODULE$.GPDB_DRIVER();
    }

    public static String GPDB_PARTITIONS() {
        return GreenplumOptions$.MODULE$.GPDB_PARTITIONS();
    }

    public static String GPDB_PARTITION_COLUMN() {
        return GreenplumOptions$.MODULE$.GPDB_PARTITION_COLUMN();
    }

    public static String GPDB_TABLE_NAME() {
        return GreenplumOptions$.MODULE$.GPDB_TABLE_NAME();
    }

    public static String GPDB_SCHEMA_NAME() {
        return GreenplumOptions$.MODULE$.GPDB_SCHEMA_NAME();
    }

    public static String GPDB_PASSWORD() {
        return GreenplumOptions$.MODULE$.GPDB_PASSWORD();
    }

    public static String GPDB_USER() {
        return GreenplumOptions$.MODULE$.GPDB_USER();
    }

    public static String GPDB_URL() {
        return GreenplumOptions$.MODULE$.GPDB_URL();
    }

    @Override
    public String option(String optionName, WhatIfMissing whatIfMissing) {
        return Options.option$((Options)this, optionName, whatIfMissing);
    }

    @Override
    public <T> T option(String optionName, WhatIfMissing whatIfMissing, Function2<String, String, Try<T>> convert) {
        return (T)Options.option$(this, optionName, whatIfMissing, convert);
    }

    @Override
    public Option<String> option(String optionName) {
        return Options.option$(this, optionName);
    }

    @Override
    public <T> Option<T> option(String optionName, Function2<String, String, Try<T>> convert) {
        return Options.option$((Options)this, optionName, convert);
    }

    @Override
    public Function2<String, String, Try<Object>> bool() {
        return Options.bool$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> naturalLong() {
        return Options.naturalLong$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> int() {
        return Options.int$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> positiveInt() {
        return Options.positiveInt$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> nonNegativeInt() {
        return Options.nonNegativeInt$(this);
    }

    private Logger logger$lzycompute() {
        GreenplumOptions greenplumOptions = this;
        synchronized (greenplumOptions) {
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

    @Override
    public Map<String, String> parameters() {
        return this.parameters;
    }

    private SparkConf sparkConf() {
        return this.sparkConf;
    }

    public Map<String, String> originalParameters() {
        return this.originalParameters;
    }

    public String url() {
        return this.url;
    }

    public String user() {
        return this.user;
    }

    public Option<String> password() {
        return this.password;
    }

    public String dbSchema() {
        return this.dbSchema;
    }

    public String dbTable() {
        return this.dbTable;
    }

    public String partitionColumn() {
        return this.partitionColumn;
    }

    public Option<Object> partitions() {
        return this.partitions;
    }

    public String driver() {
        return this.driver;
    }

    public ConnectorOptions connectorOptions() {
        return this.connectorOptions;
    }

    public ConnectionPoolOptions connectionPoolOptions() {
        return this.connectionPoolOptions;
    }

    public boolean truncateTable() {
        return this.truncateTable;
    }

    public Option<String> distributedBy() {
        return this.distributedBy;
    }

    public boolean iteratorOptimization() {
        return this.iteratorOptimization;
    }

    public GreenplumOptions(Map<String, String> parameters, SparkConf sparkConf) {
        Map map;
        this.parameters = parameters;
        this.sparkConf = sparkConf;
        Options.$init$(this);
        LazyLogging.$init$(this);
        Map map2 = parameters;
        if (map2 instanceof CaseInsensitiveMap) {
            CaseInsensitiveMap caseInsensitiveMap = (CaseInsensitiveMap)map2;
            map = caseInsensitiveMap.originalMap();
        } else {
            map = parameters;
        }
        this.originalParameters = map;
        this.url = this.option(GreenplumOptions$.MODULE$.GPDB_URL(), ErrorIfMissing$.MODULE$);
        this.user = this.option(GreenplumOptions$.MODULE$.GPDB_USER(), ErrorIfMissing$.MODULE$);
        this.password = this.option(GreenplumOptions$.MODULE$.GPDB_PASSWORD());
        this.dbSchema = this.option(GreenplumOptions$.MODULE$.GPDB_SCHEMA_NAME(), new Default("public"));
        this.dbTable = this.option(GreenplumOptions$.MODULE$.GPDB_TABLE_NAME(), ErrorIfMissing$.MODULE$);
        this.partitionColumn = this.option(GreenplumOptions$.MODULE$.GPDB_PARTITION_COLUMN(), new Default("gp_segment_id"));
        this.partitions = this.option(GreenplumOptions$.MODULE$.GPDB_PARTITIONS(), this.positiveInt());
        this.driver = this.option(GreenplumOptions$.MODULE$.GPDB_DRIVER(), new Default("org.postgresql.Driver"));
        this.connectorOptions = new ConnectorOptions((Map<String, String>)parameters, new GpfdistConf(sparkConf), ConnectorOptions$.MODULE$.$lessinit$greater$default$3(), ConnectorOptions$.MODULE$.$lessinit$greater$default$4());
        this.connectionPoolOptions = new ConnectionPoolOptions(this.originalParameters());
        this.truncateTable = BoxesRunTime.unboxToBoolean((Object)this.option(GreenplumOptions$.MODULE$.GPDB_TRUNCATE_TABLE(), new Default("false"), this.bool()));
        this.distributedBy = this.option(GreenplumOptions$.MODULE$.GPDB_DISTRIBUTED_BY());
        this.iteratorOptimization = BoxesRunTime.unboxToBoolean((Object)this.option(GreenplumOptions$.MODULE$.GPDB_ITERATOR_OPTIMIZATION(), new Default("true"), this.bool()));
    }
}

