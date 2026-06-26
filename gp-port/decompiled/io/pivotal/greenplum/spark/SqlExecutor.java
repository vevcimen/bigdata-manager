/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Array$
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ClassManifestFactory$
 *  scala.reflect.ClassTag$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import resource.ManagedResource;
import resource.Resource$;
import resource.package$;
import scala.Array$;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ClassManifestFactory$;
import scala.reflect.ClassTag$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\u0005\u0015b\u0001B\u0005\u000b\u0001MA\u0001\u0002\n\u0001\u0003\u0002\u0003\u0006I!\n\u0005\u0006[\u0001!\tA\f\u0005\u0006e\u0001!\ta\r\u0005\u0006\u0013\u0002!\tA\u0013\u0005\u0006C\u0002!\tA\u0019\u0005\u0006\u0013\u0002!\t\u0001\u001b\u0005\u0006k\u0002!IA\u001e\u0005\b\u0003\u0013\u0001A\u0011BA\u0006\u0005-\u0019\u0016\u000f\\#yK\u000e,Ho\u001c:\u000b\u0005-a\u0011!B:qCJ\\'BA\u0007\u000f\u0003%9'/Z3oa2,XN\u0003\u0002\u0010!\u00059\u0001/\u001b<pi\u0006d'\"A\t\u0002\u0005%|7\u0001A\n\u0004\u0001QQ\u0002CA\u000b\u0019\u001b\u00051\"\"A\f\u0002\u000bM\u001c\u0017\r\\1\n\u0005e1\"AB!osJ+g\r\u0005\u0002\u001cE5\tAD\u0003\u0002\u001e=\u0005a1oY1mC2|wmZ5oO*\u0011q\u0004I\u0001\tif\u0004Xm]1gK*\t\u0011%A\u0002d_6L!a\t\u000f\u0003\u00171\u000b'0\u001f'pO\u001eLgnZ\u0001\u000bG>tg.Z2uS>t\u0007C\u0001\u0014,\u001b\u00059#B\u0001\u0015*\u0003\r\u0019\u0018\u000f\u001c\u0006\u0002U\u0005!!.\u0019<b\u0013\tasE\u0001\u0006D_:tWm\u0019;j_:\fa\u0001P5oSRtDCA\u00182!\t\u0001\u0004!D\u0001\u000b\u0011\u0015!#\u00011\u0001&\u0003\u001d)\u00070Z2vi\u0016$\"\u0001N\u001f\u0011\u0007UB$(D\u00017\u0015\t9d#\u0001\u0003vi&d\u0017BA\u001d7\u0005\r!&/\u001f\t\u0003+mJ!\u0001\u0010\f\u0003\u000f\t{w\u000e\\3b]\")\u0001f\u0001a\u0001}A\u0011qH\u0012\b\u0003\u0001\u0012\u0003\"!\u0011\f\u000e\u0003\tS!a\u0011\n\u0002\rq\u0012xn\u001c;?\u0013\t)e#\u0001\u0004Qe\u0016$WMZ\u0005\u0003\u000f\"\u0013aa\u0015;sS:<'BA#\u0017\u00031)\u00070Z2vi\u0016\fV/\u001a:z+\tYu\nF\u0002M1f\u00032!\u000e\u001dN!\tqu\n\u0004\u0001\u0005\u000bA#!\u0019A)\u0003\u0007=+H/\u0005\u0002S+B\u0011QcU\u0005\u0003)Z\u0011qAT8uQ&tw\r\u0005\u0002\u0016-&\u0011qK\u0006\u0002\u0004\u0003:L\b\"\u0002\u0015\u0005\u0001\u0004q\u0004\"\u0002.\u0005\u0001\u0004Y\u0016a\u0003;sC:\u001chm\u001c:nKJ\u0004B!\u0006/_\u001b&\u0011QL\u0006\u0002\n\rVt7\r^5p]F\u0002\"AJ0\n\u0005\u0001<#!\u0003*fgVdGoU3u\u00035)\u00070Z2vi\u0016,\u0006\u000fZ1uKR\u00111m\u001a\t\u0004ka\"\u0007CA\u000bf\u0013\t1gCA\u0002J]RDQ\u0001K\u0003A\u0002y*\"!\u001b7\u0015\t)lgn\u001d\t\u0004kaZ\u0007C\u0001(m\t\u0015\u0001fA1\u0001R\u0011\u0015Ac\u00011\u0001?\u0011\u0015yg\u00011\u0001q\u0003\u0011\t'oZ:\u0011\u0007U\tX+\u0003\u0002s-\t)\u0011I\u001d:bs\")!L\u0002a\u0001iB!Q\u0003\u00180l\u00039!(/\u001f$s_6l\u0015M\\1hK\u0012,\"a\u001e>\u0015\u0005ad\bcA\u001b9sB\u0011aJ\u001f\u0003\u0006w\u001e\u0011\r!\u0015\u0002\u0002)\")Qp\u0002a\u0001}\u0006yQ.\u00198bO\u0016$'+Z:pkJ\u001cW\r\u0005\u0003\u0000\u0003\u000bIXBAA\u0001\u0015\t\t\u0019!\u0001\u0005sKN|WO]2f\u0013\u0011\t9!!\u0001\u0003\u001f5\u000bg.Y4fIJ+7o\\;sG\u0016\fQB\\8pa\u000ecwn]3bE2,WCAA\u0007%\u0019\ty!a\u0005\u0002 \u00191\u0011\u0011\u0003\u0005\u0001\u0003\u001b\u0011A\u0002\u0010:fM&tW-\\3oiz\u0002B!!\u0006\u0002\u001c5\u0011\u0011q\u0003\u0006\u0004\u00033I\u0013\u0001\u00027b]\u001eLA!!\b\u0002\u0018\t1qJ\u00196fGR\u0004B!!\u0006\u0002\"%!\u00111EA\f\u00055\tU\u000f^8DY>\u001cX-\u00192mK\u0002")
public class SqlExecutor
implements LazyLogging {
    private final Connection connection;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        SqlExecutor sqlExecutor = this;
        synchronized (sqlExecutor) {
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

    public Try<Object> execute(String sql) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("executing query: {}", new Object[]{sql});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return this.tryFromManaged(package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> $this.connection.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).map((Function1 & Serializable & scala.Serializable)statement -> BoxesRunTime.boxToBoolean((boolean)statement.execute(sql))));
    }

    public <Out> Try<Out> executeQuery(String sql, Function1<ResultSet, Out> transformer) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("executing query: {}", new Object[]{sql});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return this.tryFromManaged(package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> $this.connection.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).flatMap((Function1 & Serializable & scala.Serializable)statement -> package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> statement.executeQuery(sql), Resource$.MODULE$.resultSetResource(), ClassManifestFactory$.MODULE$.classType(ResultSet.class)).map((Function1 & Serializable & scala.Serializable)resultSet -> transformer.apply(resultSet))));
    }

    public Try<Object> executeUpdate(String sql) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("executing query: {}", new Object[]{sql});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return this.tryFromManaged(package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> $this.connection.createStatement(), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(Statement.class)).map((Function1 & Serializable & scala.Serializable)statement -> BoxesRunTime.boxToInteger((int)statement.executeUpdate(sql))));
    }

    public <Out> Try<Out> executeQuery(String sql, Object[] args, Function1<ResultSet, Out> transformer) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("executing query: {}", new Object[]{sql});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        if (Predef$.MODULE$.genericArrayOps((Object)args).isEmpty()) {
            return this.executeQuery(sql, transformer);
        }
        return this.tryFromManaged(package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> $this.connection.prepareStatement(sql), Resource$.MODULE$.statementResource(), ClassManifestFactory$.MODULE$.classType(PreparedStatement.class)).flatMap((Function1 & Serializable & scala.Serializable)preparedStatement -> package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> {
            new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])Predef$.MODULE$.genericArrayOps((Object)args).zipWithIndex(Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Tuple2.class))))).foreach((Function1 & Serializable & scala.Serializable)x0$1 -> {
                SqlExecutor.$anonfun$executeQuery$8(preparedStatement, sql, x0$1);
                return BoxedUnit.UNIT;
            });
            return this.noopCloseable();
        }, Resource$.MODULE$.reflectiveCloseableResource(), ClassManifestFactory$.MODULE$.classType(AutoCloseable.class)).flatMap((Function1 & Serializable & scala.Serializable)_ -> package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> preparedStatement.executeQuery(), Resource$.MODULE$.resultSetResource(), ClassManifestFactory$.MODULE$.classType(ResultSet.class)).map((Function1 & Serializable & scala.Serializable)resultSet -> transformer.apply(resultSet)))));
    }

    private <T> Try<T> tryFromManaged(ManagedResource<T> managedResource) {
        return managedResource.map((Function1 & Serializable & scala.Serializable)x -> x).tried();
    }

    private AutoCloseable noopCloseable() {
        return new AutoCloseable(null){

            public void close() {
            }
        };
    }

    public static final /* synthetic */ void $anonfun$executeQuery$8(PreparedStatement preparedStatement$1, String sql$4, Tuple2 x0$1) {
        Tuple2 tuple2 = x0$1;
        if (tuple2 != null) {
            Object arg = tuple2._1();
            int i = tuple2._2$mcI$sp();
            if (arg instanceof Integer) {
                int n = BoxesRunTime.unboxToInt((Object)arg);
                int n2 = i;
                preparedStatement$1.setInt(n2 + 1, n);
                return;
            }
        }
        if (tuple2 != null) {
            Object arg = tuple2._1();
            int i = tuple2._2$mcI$sp();
            if (arg instanceof String) {
                String string = (String)arg;
                int n = i;
                preparedStatement$1.setString(n + 1, string);
                return;
            }
        }
        if (tuple2 != null) {
            Object arg = tuple2._1();
            throw new IllegalArgumentException(new StringBuilder(61).append("Unsupported type ").append(arg.getClass().getSimpleName()).append(" for prepared ").append("statement argument ").append(arg).append(". SQL was: ").append(sql$4).toString());
        }
        throw new MatchError((Object)tuple2);
    }

    public SqlExecutor(Connection connection) {
        this.connection = connection;
        LazyLogging.$init$(this);
    }
}

