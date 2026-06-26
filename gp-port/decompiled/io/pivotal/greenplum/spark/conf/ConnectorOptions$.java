/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Array$
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.PartialFunction
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.Seq
 *  scala.collection.immutable.List
 *  scala.collection.immutable.List$
 *  scala.collection.immutable.Map
 *  scala.collection.immutable.StringOps
 *  scala.collection.mutable.ArrayOps$ofInt
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.math.Integral
 *  scala.math.Numeric$IntIsIntegral$
 *  scala.math.Ordering
 *  scala.math.Ordering$Int$
 *  scala.reflect.ClassTag$
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcI$sp
 *  scala.sys.package$
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 *  scala.util.Try$
 */
package io.pivotal.greenplum.spark.conf;

import io.pivotal.greenplum.spark.ConnectorUtils;
import io.pivotal.greenplum.spark.ConnectorUtils$;
import java.io.Serializable;
import scala.Array$;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.PartialFunction;
import scala.Predef$;
import scala.collection.Seq;
import scala.collection.immutable.List;
import scala.collection.immutable.List$;
import scala.collection.immutable.Map;
import scala.collection.immutable.StringOps;
import scala.collection.mutable.ArrayOps;
import scala.math.Integral;
import scala.math.Numeric;
import scala.math.Ordering;
import scala.reflect.ClassTag$;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;
import scala.sys.package$;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;
import scala.util.Try$;

public final class ConnectorOptions$
implements scala.Serializable {
    public static ConnectorOptions$ MODULE$;
    private final String GPDB_NETWORK_PORT;
    private final String GPDB_NETWORK_ADDRESS;
    private final String GPDB_NETWORK_INTERFACE;
    private final String GPDB_NETWORK_HOSTNAME;
    private final String GPDB_NETWORK_TIMEOUT;
    private final String GPDB_ENV_VAR_PREFIX;
    private final String MATCH_DISTRIBUTION_POLICY;
    private final String GPDB_DEFAULT_NETWORK_INTERFACE;
    private final String GPDB_DEFAULT_SERVER_HOST;

    static {
        new ConnectorOptions$();
    }

    public Map<String, String> $lessinit$greater$default$3() {
        return package$.MODULE$.env();
    }

    public ConnectorUtils $lessinit$greater$default$4() {
        return new ConnectorUtils(ConnectorUtils$.MODULE$.$lessinit$greater$default$1());
    }

    public String GPDB_NETWORK_PORT() {
        return this.GPDB_NETWORK_PORT;
    }

    public String GPDB_NETWORK_ADDRESS() {
        return this.GPDB_NETWORK_ADDRESS;
    }

    public String GPDB_NETWORK_INTERFACE() {
        return this.GPDB_NETWORK_INTERFACE;
    }

    public String GPDB_NETWORK_HOSTNAME() {
        return this.GPDB_NETWORK_HOSTNAME;
    }

    public String GPDB_NETWORK_TIMEOUT() {
        return this.GPDB_NETWORK_TIMEOUT;
    }

    public String GPDB_ENV_VAR_PREFIX() {
        return this.GPDB_ENV_VAR_PREFIX;
    }

    public String MATCH_DISTRIBUTION_POLICY() {
        return this.MATCH_DISTRIBUTION_POLICY;
    }

    public String GPDB_DEFAULT_NETWORK_INTERFACE() {
        return this.GPDB_DEFAULT_NETWORK_INTERFACE;
    }

    public String GPDB_DEFAULT_SERVER_HOST() {
        return this.GPDB_DEFAULT_SERVER_HOST;
    }

    public List<Object> parsePortString(String portStr) {
        return new ArrayOps.ofInt(Predef$.MODULE$.intArrayOps((int[])new ArrayOps.ofInt(Predef$.MODULE$.intArrayOps((int[])new ArrayOps.ofInt(Predef$.MODULE$.intArrayOps((int[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new StringOps(Predef$.MODULE$.augmentString(portStr)).split(','))).flatMap((Function1 & Serializable & scala.Serializable)s2 -> (List)MODULE$.parsePortOrRange((String)s2).recoverWith((PartialFunction)new scala.Serializable(portStr){
            public static final long serialVersionUID = 0L;
            private final String portStr$1;

            public final <A1 extends Throwable, B1> B1 applyOrElse(A1 x1, Function1<A1, B1> function1) {
                A1 A1 = x1;
                if (A1 instanceof Exception) {
                    Exception exception = (Exception)A1;
                    return (B1)new Failure((Throwable)new IllegalArgumentException(new StringBuilder(32).append("failed to parse port string '").append(this.portStr$1).append("': ").append(exception.getMessage()).toString(), exception));
                }
                return (B1)function1.apply(x1);
            }

            public final boolean isDefinedAt(Throwable x1) {
                Throwable throwable = x1;
                return throwable instanceof Exception;
            }
            {
                this.portStr$1 = portStr$1;
            }
        }).get(), Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.Int())))).distinct())).sorted((Ordering)Ordering.Int$.MODULE$))).toList();
    }

    private Try<List<Object>> parsePortOrRange(String str) {
        Try[] tryArray = (Try[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new StringOps(Predef$.MODULE$.augmentString(str)).split('-'))).map((Function1 & Serializable & scala.Serializable)s2 -> Try$.MODULE$.apply((Function0)(JFunction0.mcI.sp & Serializable & scala.Serializable)() -> Integer.parseInt(s2.trim())), Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Try.class))))).map((Function1 & Serializable & scala.Serializable)x0$1 -> {
            boolean bl = false;
            Success success = null;
            Try try_ = x0$1;
            if (try_ instanceof Success) {
                bl = true;
                success = (Success)try_;
                int p = BoxesRunTime.unboxToInt((Object)success.value());
                if (p < 0 || p > 65535) {
                    return new Failure((Throwable)new IllegalArgumentException(new StringBuilder(64).append(p).append(" is not a valid port number. Specify a value between 0 and 65535").toString()));
                }
            }
            if (bl) {
                int p = BoxesRunTime.unboxToInt((Object)success.value());
                return new Success((Object)BoxesRunTime.boxToInteger((int)p));
            }
            if (try_ instanceof Failure) {
                Failure failure = (Failure)try_;
                Throwable e = failure.exception();
                return new Failure(e);
            }
            throw new MatchError((Object)try_);
        }, Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Try.class)));
        if (tryArray.length == 1) {
            return Try$.MODULE$.apply((Function0 & Serializable & scala.Serializable)() -> List$.MODULE$.apply((Seq)Predef$.MODULE$.wrapIntArray(new int[]{BoxesRunTime.unboxToInt((Object)tryArray[0].get())})));
        }
        if (tryArray.length == 2) {
            return Try$.MODULE$.apply((Function0 & Serializable & scala.Serializable)() -> (List)List$.MODULE$.range(tryArray[0].get(), (Object)BoxesRunTime.boxToInteger((int)(BoxesRunTime.unboxToInt((Object)tryArray[1].get()) + 1)), (Integral)Numeric.IntIsIntegral$.MODULE$));
        }
        return new Failure((Throwable)new IllegalArgumentException("invalid port range"));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ConnectorOptions$() {
        MODULE$ = this;
        this.GPDB_NETWORK_PORT = "server.port";
        this.GPDB_NETWORK_ADDRESS = "server.hostEnv";
        this.GPDB_NETWORK_INTERFACE = "server.nic";
        this.GPDB_NETWORK_HOSTNAME = "server.useHostname";
        this.GPDB_NETWORK_TIMEOUT = "server.timeout";
        this.GPDB_ENV_VAR_PREFIX = "env.";
        this.MATCH_DISTRIBUTION_POLICY = "gpdb.matchDistributionPolicy";
        this.GPDB_DEFAULT_NETWORK_INTERFACE = "eth0";
        this.GPDB_DEFAULT_SERVER_HOST = "0.0.0.0";
    }
}

