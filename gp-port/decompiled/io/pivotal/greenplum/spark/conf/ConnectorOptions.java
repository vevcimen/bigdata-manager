/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.spark.SparkEnv$
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.Seq
 *  scala.collection.immutable.List
 *  scala.collection.immutable.List$
 *  scala.collection.immutable.Map
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcJ$sp
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.conf;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.ConnectorUtils;
import io.pivotal.greenplum.spark.conf.ConnectorOptions$;
import io.pivotal.greenplum.spark.conf.Default;
import io.pivotal.greenplum.spark.conf.Options;
import io.pivotal.greenplum.spark.conf.WhatIfMissing;
import java.io.Serializable;
import java.time.Duration;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkEnv$;
import org.greenplum.spark.GpfdistConf;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.Option;
import scala.Predef$;
import scala.Some;
import scala.collection.Seq;
import scala.collection.immutable.List;
import scala.collection.immutable.List$;
import scala.collection.immutable.Map;
import scala.collection.immutable.StringOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;
import scala.util.Try;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
@ScalaSignature(bytes="\u0006\u0001\u00055h\u0001B\u001c9\u0001\rC\u0001b\u0017\u0001\u0003\u0006\u0004%\t\u0001\u0018\u0005\tW\u0002\u0011\t\u0011)A\u0005;\"AA\u000e\u0001BC\u0002\u0013%Q\u000e\u0003\u0005w\u0001\t\u0005\t\u0015!\u0003o\u0011!9\bA!b\u0001\n\u0003a\u0006\u0002\u0003=\u0001\u0005\u0003\u0005\u000b\u0011B/\t\u0011e\u0004!Q1A\u0005\niD\u0001b \u0001\u0003\u0002\u0003\u0006Ia\u001f\u0005\b\u0003\u0003\u0001A\u0011AA\u0002\u0011\u001d\t\t\u0001\u0001C\u0001\u0003\u001fA\u0011\"!\u0005\u0001\u0005\u0004%\t!a\u0005\t\u0011\u00055\u0002\u0001)A\u0005\u0003+A\u0011\"a\f\u0001\u0005\u0004%\t!!\r\t\u0011\u0005e\u0002\u0001)A\u0005\u0003gA\u0011\"a\u000f\u0001\u0005\u0004%\t!!\u0010\t\u0011\u0005\u0015\u0003\u0001)A\u0005\u0003\u007fA\u0011\"a\u0012\u0001\u0005\u0004%\t!!\u0010\t\u0011\u0005%\u0003\u0001)A\u0005\u0003\u007fA\u0011\"a\u0013\u0001\u0005\u0004%\t!!\u0014\t\u0011\u0005U\u0003\u0001)A\u0005\u0003\u001fB\u0011\"a\u0016\u0001\u0005\u0004%\t!!\u0014\t\u0011\u0005e\u0003\u0001)A\u0005\u0003\u001fB\u0011\"a\u0017\u0001\u0005\u0004%\t!!\r\t\u0011\u0005u\u0003\u0001)A\u0005\u0003gAq!a\u0018\u0001\t\u0003\t\t\u0007C\u0004\u0002d\u0001!\t!!\u0010\t\u000f\u0005\u0015\u0004\u0001\"\u0001\u0002h!9\u00111\u000e\u0001\u0005\u0002\u0005EraBA7q!\u0005\u0011q\u000e\u0004\u0007oaB\t!!\u001d\t\u000f\u0005\u0005a\u0004\"\u0001\u0002t!I\u0011Q\u000f\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003\u000fs\u0002\u0015!\u0003\u0002z!I\u0011\u0011\u0012\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003\u0017s\u0002\u0015!\u0003\u0002z!I\u0011Q\u0012\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003\u001fs\u0002\u0015!\u0003\u0002z!I\u0011\u0011\u0013\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003's\u0002\u0015!\u0003\u0002z!I\u0011Q\u0013\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003/s\u0002\u0015!\u0003\u0002z!I\u0011\u0011\u0014\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u00037s\u0002\u0015!\u0003\u0002z!I\u0011Q\u0014\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003?s\u0002\u0015!\u0003\u0002z!I\u0011\u0011\u0015\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003Gs\u0002\u0015!\u0003\u0002z!I\u0011Q\u0015\u0010C\u0002\u0013\u0005\u0011q\u000f\u0005\t\u0003Os\u0002\u0015!\u0003\u0002z!9\u0011\u0011\u0016\u0010\u0005\u0002\u0005-\u0006bBAY=\u0011%\u00111\u0017\u0005\n\u0003\u000bt\u0012\u0013!C\u0001\u0003\u000fD\u0011\"!8\u001f#\u0003%\t!a8\t\u0013\u0005\rh$!A\u0005\n\u0005\u0015(\u0001E\"p]:,7\r^8s\u001fB$\u0018n\u001c8t\u0015\tI$(\u0001\u0003d_:4'BA\u001e=\u0003\u0015\u0019\b/\u0019:l\u0015\tid(A\u0005he\u0016,g\u000e\u001d7v[*\u0011q\bQ\u0001\ba&4x\u000e^1m\u0015\u0005\t\u0015AA5p\u0007\u0001\u0019R\u0001\u0001#K\u001bF\u0003\"!\u0012%\u000e\u0003\u0019S\u0011aR\u0001\u0006g\u000e\fG.Y\u0005\u0003\u0013\u001a\u0013a!\u00118z%\u00164\u0007CA#L\u0013\taeI\u0001\u0007TKJL\u0017\r\\5{C\ndW\r\u0005\u0002O\u001f6\t\u0001(\u0003\u0002Qq\t9q\n\u001d;j_:\u001c\bC\u0001*Z\u001b\u0005\u0019&B\u0001+V\u00031\u00198-\u00197bY><w-\u001b8h\u0015\t1v+\u0001\u0005usB,7/\u00194f\u0015\u0005A\u0016aA2p[&\u0011!l\u0015\u0002\f\u0019\u0006T\u0018\u0010T8hO&tw-\u0001\u0006qCJ\fW.\u001a;feN,\u0012!\u0018\t\u0005=\u0016D\u0007N\u0004\u0002`GB\u0011\u0001MR\u0007\u0002C*\u0011!MQ\u0001\u0007yI|w\u000e\u001e \n\u0005\u00114\u0015A\u0002)sK\u0012,g-\u0003\u0002gO\n\u0019Q*\u00199\u000b\u0005\u00114\u0005C\u00010j\u0013\tQwM\u0001\u0004TiJLgnZ\u0001\fa\u0006\u0014\u0018-\\3uKJ\u001c\b%A\u0006ha\u001a$\u0017n\u001d;D_:4W#\u00018\u0011\u0005=$X\"\u00019\u000b\u0005m\n(BA\u001fs\u0015\u0005\u0019\u0018aA8sO&\u0011Q\u000f\u001d\u0002\f\u000fB4G-[:u\u0007>tg-\u0001\u0007ha\u001a$\u0017n\u001d;D_:4\u0007%A\u0002f]Z\fA!\u001a8wA\u0005q1m\u001c8oK\u000e$xN]+uS2\u001cX#A>\u0011\u0005qlX\"\u0001\u001e\n\u0005yT$AD\"p]:,7\r^8s+RLGn]\u0001\u0010G>tg.Z2u_J,F/\u001b7tA\u00051A(\u001b8jiz\"\"\"!\u0002\u0002\b\u0005%\u00111BA\u0007!\tq\u0005\u0001C\u0003\\\u0013\u0001\u0007Q\fC\u0003m\u0013\u0001\u0007a\u000eC\u0004x\u0013A\u0005\t\u0019A/\t\u000feL\u0001\u0013!a\u0001wR\u0011\u0011QA\u0001\u0005a>\u0014H/\u0006\u0002\u0002\u0016A1\u0011qCA\u0011\u0003OqA!!\u0007\u0002\u001e9\u0019\u0001-a\u0007\n\u0003\u001dK1!a\bG\u0003\u001d\u0001\u0018mY6bO\u0016LA!a\t\u0002&\t!A*[:u\u0015\r\tyB\u0012\t\u0004\u000b\u0006%\u0012bAA\u0016\r\n\u0019\u0011J\u001c;\u0002\u000bA|'\u000f\u001e\u0011\u0002!U\u001cX\rT8dC2Dun\u001d;oC6,WCAA\u001a!\r)\u0015QG\u0005\u0004\u0003o1%a\u0002\"p_2,\u0017M\\\u0001\u0012kN,Gj\\2bY\"{7\u000f\u001e8b[\u0016\u0004\u0013\u0001H:feZ,'/\u00113ee\u0016\u001c8O\u0012:p[\u0016sg/\u001b:p]6,g\u000e^\u000b\u0003\u0003\u007f\u0001B!RA!Q&\u0019\u00111\t$\u0003\r=\u0003H/[8o\u0003u\u0019XM\u001d<fe\u0006#GM]3tg\u001a\u0013x.\\#om&\u0014xN\\7f]R\u0004\u0013\u0001\u00068fi^|'o[%oi\u0016\u0014h-Y2f\u001d\u0006lW-A\u000boKR<xN]6J]R,'OZ1dK:\u000bW.\u001a\u0011\u0002%5\u000b\u0005l\u0018+J\u001b\u0016{U\u000bV0N\u00132c\u0015jU\u000b\u0003\u0003\u001f\u00022!RA)\u0013\r\t\u0019F\u0012\u0002\u0005\u0019>tw-A\nN\u0003b{F+S'F\u001fV#v,T%M\u0019&\u001b\u0006%A\buS6,w.\u001e;J]6KG\u000e\\5t\u0003A!\u0018.\\3pkRLe.T5mY&\u001c\b%A\fnCR\u001c\u0007\u000eR5tiJL'-\u001e;j_:\u0004v\u000e\\5ds\u0006AR.\u0019;dQ\u0012K7\u000f\u001e:jEV$\u0018n\u001c8Q_2L7-\u001f\u0011\u0002\u001b\u001d,GoU3sm\u0016\u0014\bj\\:u+\u0005A\u0017aC4qM\u0012L7\u000f\u001e%pgR\facZ3u\u000fB4G-[:u\u0019>\u001c\u0017\r^5p]B{'\u000f^\u000b\u0003\u0003S\u0002R!RA!\u0003O\ta!^:f'Nd\u0017\u0001E\"p]:,7\r^8s\u001fB$\u0018n\u001c8t!\tqedE\u0002\u001f\t*#\"!a\u001c\u0002#\u001d\u0003FIQ0O\u000bR;vJU&`!>\u0013F+\u0006\u0002\u0002zA!\u00111PAC\u001b\t\tiH\u0003\u0003\u0002\u0000\u0005\u0005\u0015\u0001\u00027b]\u001eT!!a!\u0002\t)\fg/Y\u0005\u0004U\u0006u\u0014AE$Q\t\n{f*\u0012+X\u001fJ[u\fU(S)\u0002\nAc\u0012)E\u0005~sU\tV,P%.{\u0016\t\u0012#S\u000bN\u001b\u0016!F$Q\t\n{f*\u0012+X\u001fJ[u,\u0011#E%\u0016\u001b6\u000bI\u0001\u0017\u000fB#%i\u0018(F)^{%kS0J\u001dR+%KR!D\u000b\u00069r\t\u0015#C?:+EkV(S\u0017~Ke\nV#S\r\u0006\u001bU\tI\u0001\u0016\u000fB#%i\u0018(F)^{%kS0I\u001fN#f*Q'F\u0003Y9\u0005\u000b\u0012\"`\u001d\u0016#vk\u0014*L?\"{5\u000b\u0016(B\u001b\u0016\u0003\u0013\u0001F$Q\t\n{f*\u0012+X\u001fJ[u\fV%N\u000b>+F+A\u000bH!\u0012\u0013uLT#U/>\u00136j\u0018+J\u001b\u0016{U\u000b\u0016\u0011\u0002'\u001d\u0003FIQ0F\u001dZ{f+\u0011*`!J+e)\u0013-\u0002)\u001d\u0003FIQ0F\u001dZ{f+\u0011*`!J+e)\u0013-!\u0003ei\u0015\tV\"I?\u0012K5\u000b\u0016*J\u0005V#\u0016j\u0014(`!>c\u0015jQ-\u000255\u000bEk\u0011%`\t&\u001bFKU%C+RKuJT0Q\u001f2K5)\u0017\u0011\u0002=\u001d\u0003FIQ0E\u000b\u001a\u000bU\u000b\u0014+`\u001d\u0016#vk\u0014*L?&sE+\u0012*G\u0003\u000e+\u0015aH$Q\t\n{F)\u0012$B+2#vLT#U/>\u00136jX%O)\u0016\u0013f)Q\"FA\u0005Ar\t\u0015#C?\u0012+e)Q+M)~\u001bVI\u0015,F%~Cuj\u0015+\u00023\u001d\u0003FIQ0E\u000b\u001a\u000bU\u000b\u0014+`'\u0016\u0013f+\u0012*`\u0011>\u001bF\u000bI\u0001\u0010a\u0006\u00148/\u001a)peR\u001cFO]5oOR!\u0011QCAW\u0011\u0019\tyK\ra\u0001Q\u00069\u0001o\u001c:u'R\u0014\u0018\u0001\u00059beN,\u0007k\u001c:u\u001fJ\u0014\u0016M\\4f)\u0011\t),!1\u0011\r\u0005]\u0016QXA\u000b\u001b\t\tILC\u0002\u0002<\u001a\u000bA!\u001e;jY&!\u0011qXA]\u0005\r!&/\u001f\u0005\u0007\u0003\u0007\u001c\u0004\u0019\u00015\u0002\u0007M$(/A\u000e%Y\u0016\u001c8/\u001b8ji\u0012:'/Z1uKJ$C-\u001a4bk2$HeM\u000b\u0003\u0003\u0013T3!XAfW\t\ti\r\u0005\u0003\u0002P\u0006eWBAAi\u0015\u0011\t\u0019.!6\u0002\u0013Ut7\r[3dW\u0016$'bAAl\r\u0006Q\u0011M\u001c8pi\u0006$\u0018n\u001c8\n\t\u0005m\u0017\u0011\u001b\u0002\u0012k:\u001c\u0007.Z2lK\u00124\u0016M]5b]\u000e,\u0017a\u0007\u0013mKN\u001c\u0018N\\5uI\u001d\u0014X-\u0019;fe\u0012\"WMZ1vYR$C'\u0006\u0002\u0002b*\u001a10a3\u0002\u0017I,\u0017\r\u001a*fg>dg/\u001a\u000b\u0003\u0003O\u0004B!a\u001f\u0002j&!\u00111^A?\u0005\u0019y%M[3di\u0002")
public class ConnectorOptions
implements scala.Serializable,
Options,
LazyLogging {
    private final Map<String, String> parameters;
    private final GpfdistConf gpfdistConf;
    private final Map<String, String> env;
    private final ConnectorUtils connectorUtils;
    private final List<Object> port;
    private final boolean useLocalHostname;
    private final Option<String> serverAddressFromEnvironment;
    private final Option<String> networkInterfaceName;
    private final long MAX_TIMEOUT_MILLIS;
    private final long timeoutInMillis;
    private final boolean matchDistributionPolicy;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static ConnectorUtils $lessinit$greater$default$4() {
        return ConnectorOptions$.MODULE$.$lessinit$greater$default$4();
    }

    public static Map<String, String> $lessinit$greater$default$3() {
        return ConnectorOptions$.MODULE$.$lessinit$greater$default$3();
    }

    public static List<Object> parsePortString(String string) {
        return ConnectorOptions$.MODULE$.parsePortString(string);
    }

    public static String GPDB_DEFAULT_SERVER_HOST() {
        return ConnectorOptions$.MODULE$.GPDB_DEFAULT_SERVER_HOST();
    }

    public static String GPDB_DEFAULT_NETWORK_INTERFACE() {
        return ConnectorOptions$.MODULE$.GPDB_DEFAULT_NETWORK_INTERFACE();
    }

    public static String MATCH_DISTRIBUTION_POLICY() {
        return ConnectorOptions$.MODULE$.MATCH_DISTRIBUTION_POLICY();
    }

    public static String GPDB_ENV_VAR_PREFIX() {
        return ConnectorOptions$.MODULE$.GPDB_ENV_VAR_PREFIX();
    }

    public static String GPDB_NETWORK_TIMEOUT() {
        return ConnectorOptions$.MODULE$.GPDB_NETWORK_TIMEOUT();
    }

    public static String GPDB_NETWORK_HOSTNAME() {
        return ConnectorOptions$.MODULE$.GPDB_NETWORK_HOSTNAME();
    }

    public static String GPDB_NETWORK_INTERFACE() {
        return ConnectorOptions$.MODULE$.GPDB_NETWORK_INTERFACE();
    }

    public static String GPDB_NETWORK_ADDRESS() {
        return ConnectorOptions$.MODULE$.GPDB_NETWORK_ADDRESS();
    }

    public static String GPDB_NETWORK_PORT() {
        return ConnectorOptions$.MODULE$.GPDB_NETWORK_PORT();
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
        ConnectorOptions connectorOptions = this;
        synchronized (connectorOptions) {
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

    private GpfdistConf gpfdistConf() {
        return this.gpfdistConf;
    }

    public Map<String, String> env() {
        return this.env;
    }

    private ConnectorUtils connectorUtils() {
        return this.connectorUtils;
    }

    public List<Object> port() {
        return this.port;
    }

    public boolean useLocalHostname() {
        return this.useLocalHostname;
    }

    public Option<String> serverAddressFromEnvironment() {
        return this.serverAddressFromEnvironment;
    }

    public Option<String> networkInterfaceName() {
        return this.networkInterfaceName;
    }

    public long MAX_TIMEOUT_MILLIS() {
        return this.MAX_TIMEOUT_MILLIS;
    }

    public long timeoutInMillis() {
        return this.timeoutInMillis;
    }

    public boolean matchDistributionPolicy() {
        return this.matchDistributionPolicy;
    }

    public String getServerHost() {
        return (String)this.networkInterfaceName().flatMap((Function1 & Serializable & scala.Serializable)name -> this.connectorUtils().getHostAddressByNetworkInterfaceByName((String)name).toOption()).getOrElse((Function0 & Serializable & scala.Serializable)() -> ConnectorOptions$.MODULE$.GPDB_DEFAULT_SERVER_HOST());
    }

    public Option<String> gpfdistHost() {
        return this.gpfdistConf().host();
    }

    public Option<Object> getGpfdistLocationPort() {
        return this.gpfdistConf().locationPort();
    }

    public boolean useSsl() {
        return this.gpfdistConf().isSSL();
    }

    public static final /* synthetic */ boolean $anonfun$serverAddressFromEnvironment$1(String x$1) {
        return StringUtils.isNotBlank((String)x$1);
    }

    public static final /* synthetic */ boolean $anonfun$networkInterfaceName$1(String x$1) {
        return StringUtils.isNotBlank((String)x$1);
    }

    public ConnectorOptions(Map<String, String> parameters, GpfdistConf gpfdistConf, Map<String, String> env, ConnectorUtils connectorUtils) {
        this.parameters = parameters;
        this.gpfdistConf = gpfdistConf;
        this.env = env;
        this.connectorUtils = connectorUtils;
        Options.$init$(this);
        LazyLogging.$init$(this);
        this.port = (List)this.option(ConnectorOptions$.MODULE$.GPDB_NETWORK_PORT()).map((Function1 & Serializable & scala.Serializable)s2 -> {
            String portString = StringUtils.startsWith((String)s2, (String)ConnectorOptions$.MODULE$.GPDB_ENV_VAR_PREFIX()) ? (String)this.env().getOrElse((Object)s2.substring(ConnectorOptions$.MODULE$.GPDB_ENV_VAR_PREFIX().length()), (Function0 & Serializable & scala.Serializable)() -> "0") : s2;
            return ConnectorOptions$.MODULE$.parsePortString(portString);
        }).getOrElse((Function0 & Serializable & scala.Serializable)() -> List$.MODULE$.apply((Seq)Predef$.MODULE$.wrapIntArray(new int[]{this.gpfdistConf().listenPort()})));
        this.useLocalHostname = BoxesRunTime.unboxToBoolean((Object)this.option(ConnectorOptions$.MODULE$.GPDB_NETWORK_HOSTNAME(), new Default("false"), this.bool()));
        this.serverAddressFromEnvironment = this.option(ConnectorOptions$.MODULE$.GPDB_NETWORK_ADDRESS()).filter((Function1 & Serializable & scala.Serializable)x$1 -> BoxesRunTime.boxToBoolean((boolean)ConnectorOptions.$anonfun$serverAddressFromEnvironment$1(x$1))).map((Function1 & Serializable & scala.Serializable)x$1 -> new StringOps(Predef$.MODULE$.augmentString(x$1)).stripPrefix(ConnectorOptions$.MODULE$.GPDB_ENV_VAR_PREFIX())).flatMap((Function1 & Serializable & scala.Serializable)key -> this.env().get(key));
        this.networkInterfaceName = this.option(ConnectorOptions$.MODULE$.GPDB_NETWORK_INTERFACE()).filter((Function1 & Serializable & scala.Serializable)x$1 -> BoxesRunTime.boxToBoolean((boolean)ConnectorOptions.$anonfun$networkInterfaceName$1(x$1))).flatMap((Function1 & Serializable & scala.Serializable)value -> {
            if (value.startsWith(ConnectorOptions$.MODULE$.GPDB_ENV_VAR_PREFIX())) {
                return this.env().get((Object)new StringOps(Predef$.MODULE$.augmentString(value)).stripPrefix(ConnectorOptions$.MODULE$.GPDB_ENV_VAR_PREFIX()));
            }
            return new Some(value);
        });
        this.MAX_TIMEOUT_MILLIS = Duration.ofHours(2L).toMillis();
        this.timeoutInMillis = BoxesRunTime.unboxToLong((Object)this.option(ConnectorOptions$.MODULE$.GPDB_NETWORK_TIMEOUT(), this.naturalLong()).getOrElse((Function0)(JFunction0.mcJ.sp & Serializable & scala.Serializable)() -> Duration.ofMinutes(5L).toMillis()));
        if (this.timeoutInMillis() > this.MAX_TIMEOUT_MILLIS()) {
            throw new IllegalArgumentException(new StringBuilder(112).append("requirement failed: Option ").append("'").append(ConnectorOptions$.MODULE$.GPDB_NETWORK_TIMEOUT()).append("' has a value of '").append(this.timeoutInMillis()).append("' that is ").append("greater than the maximum allowed value of ").append(this.MAX_TIMEOUT_MILLIS()).append(" milliseconds.").toString());
        }
        this.matchDistributionPolicy = BoxesRunTime.unboxToBoolean((Object)this.option(ConnectorOptions$.MODULE$.MATCH_DISTRIBUTION_POLICY(), new Default("false"), this.bool()));
    }

    public ConnectorOptions() {
        this((Map<String, String>)Predef$.MODULE$.Map().empty(), new GpfdistConf(SparkEnv$.MODULE$.get().conf()), ConnectorOptions$.MODULE$.$lessinit$greater$default$3(), ConnectorOptions$.MODULE$.$lessinit$greater$default$4());
    }
}

