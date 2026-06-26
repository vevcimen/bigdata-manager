/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 */
package io.pivotal.greenplum.spark.jdbc;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey$;
import io.pivotal.greenplum.spark.jdbc.ConnectionManager$;
import io.pivotal.greenplum.spark.jdbc.DataSourceProvider;
import java.sql.Connection;
import java.util.HashMap;
import javax.sql.DataSource;
import scala.Option;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;

@ScalaSignature(bytes="\u0006\u0001\u0005er!\u0002\t\u0012\u0011\u0003ab!\u0002\u0010\u0012\u0011\u0003y\u0002\"\u0002\u0014\u0002\t\u00039\u0003\u0002\u0003\u0015\u0002\u0011\u000b\u0007I\u0011A\u0015\t\rI\u000bA\u0011AA\u001a\u0011\u001d1\u0017!%A\u0005\u0002\u001d4AAH\t\u0001W!AaG\u0002B\u0001B\u0003%q\u0007C\u0003'\r\u0011\u0005!\bC\u0004=\r\t\u0007I\u0011B\u001f\t\rE3\u0001\u0015!\u0003?\u0011\u0015\u0011f\u0001\"\u0001T\u0011\u001d1g!%A\u0005\u0002\u001dDaA\u001d\u0004\u0005\u0002E\u0019\bBCA\u0010\rE\u0005I\u0011A\t\u0002\"!A\u0011Q\u0005\u0004\u0005\u0002E\t9#A\tD_:tWm\u0019;j_:l\u0015M\\1hKJT!AE\n\u0002\t)$'m\u0019\u0006\u0003)U\tQa\u001d9be.T!AF\f\u0002\u0013\u001d\u0014X-\u001a8qYVl'B\u0001\r\u001a\u0003\u001d\u0001\u0018N^8uC2T\u0011AG\u0001\u0003S>\u001c\u0001\u0001\u0005\u0002\u001e\u00035\t\u0011CA\tD_:tWm\u0019;j_:l\u0015M\\1hKJ\u001c\"!\u0001\u0011\u0011\u0005\u0005\"S\"\u0001\u0012\u000b\u0003\r\nQa]2bY\u0006L!!\n\u0012\u0003\r\u0005s\u0017PU3g\u0003\u0019a\u0014N\\5u}Q\tA$A\td_:tWm\u0019;j_:l\u0015M\\1hKJ,\u0012A\u000b\t\u0003;\u0019\u00192A\u0002\u0011-!\tiC'D\u0001/\u0015\ty\u0003'\u0001\u0007tG\u0006d\u0017\r\\8hO&twM\u0003\u00022e\u0005AA/\u001f9fg\u00064WMC\u00014\u0003\r\u0019w.\\\u0005\u0003k9\u00121\u0002T1{s2{wmZ5oO\u0006A\u0001O]8wS\u0012,'\u000f\u0005\u0002\u001eq%\u0011\u0011(\u0005\u0002\u0013\t\u0006$\u0018mU8ve\u000e,\u0007K]8wS\u0012,'\u000f\u0006\u0002+w!)a\u0007\u0003a\u0001o\u0005)\u0001o\\8mgV\ta\b\u0005\u0003@\t\u001aKU\"\u0001!\u000b\u0005\u0005\u0013\u0015\u0001B;uS2T\u0011aQ\u0001\u0005U\u00064\u0018-\u0003\u0002F\u0001\n9\u0001*Y:i\u001b\u0006\u0004\bCA\u000fH\u0013\tA\u0015CA\u0007D_:tWm\u0019;j_:\\U-\u001f\t\u0003\u0015>k\u0011a\u0013\u0006\u0003\u00196\u000b1a]9m\u0015\u0005q\u0015!\u00026bm\u0006D\u0018B\u0001)L\u0005)!\u0015\r^1T_V\u00148-Z\u0001\u0007a>|Gn\u001d\u0011\u0002\u001b\u001d,GoQ8o]\u0016\u001cG/[8o)\r!\u0016,\u0019\t\u0003+^k\u0011A\u0016\u0006\u0003\u0019\nK!\u0001\u0017,\u0003\u0015\r{gN\\3di&|g\u000eC\u0003[\u0017\u0001\u00071,\u0001\u0004eE>\u0003Ho\u001d\t\u00039~k\u0011!\u0018\u0006\u0003=N\tAaY8oM&\u0011\u0001-\u0018\u0002\u0011\u000fJ,WM\u001c9mk6|\u0005\u000f^5p]NDqAY\u0006\u0011\u0002\u0003\u00071-\u0001\u0006bkR|7i\\7nSR\u0004\"!\t3\n\u0005\u0015\u0014#a\u0002\"p_2,\u0017M\\\u0001\u0018O\u0016$8i\u001c8oK\u000e$\u0018n\u001c8%I\u00164\u0017-\u001e7uII*\u0012\u0001\u001b\u0016\u0003G&\\\u0013A\u001b\t\u0003WBl\u0011\u0001\u001c\u0006\u0003[:\f\u0011\"\u001e8dQ\u0016\u001c7.\u001a3\u000b\u0005=\u0014\u0013AC1o]>$\u0018\r^5p]&\u0011\u0011\u000f\u001c\u0002\u0012k:\u001c\u0007.Z2lK\u00124\u0016M]5b]\u000e,\u0017aE4fiB{w\u000e\\3e\u0007>tg.Z2uS>tGC\u0003+u\u0003\u0007\t9!!\u0005\u0002\u0016!)Q/\u0004a\u0001m\u00069!\u000e\u001a2d+Jd\u0007CA<\u007f\u001d\tAH\u0010\u0005\u0002zE5\t!P\u0003\u0002|7\u00051AH]8pizJ!! \u0012\u0002\rA\u0013X\rZ3g\u0013\ry\u0018\u0011\u0001\u0002\u0007'R\u0014\u0018N\\4\u000b\u0005u\u0014\u0003BBA\u0003\u001b\u0001\u0007a/\u0001\u0005vg\u0016\u0014h*Y7f\u0011\u001d\tI!\u0004a\u0001\u0003\u0017\t\u0001\u0002]1tg^|'\u000f\u001a\t\u0005C\u00055a/C\u0002\u0002\u0010\t\u0012aa\u00149uS>t\u0007BBA\n\u001b\u0001\u0007a/\u0001\u0004ee&4XM\u001d\u0005\n\u0003/i\u0001\u0013!a\u0001\u00033\tqa\u001c9uS>t7\u000fE\u0002]\u00037I1!!\b^\u0005U\u0019uN\u001c8fGRLwN\u001c)p_2|\u0005\u000f^5p]N\fQdZ3u!>|G.\u001a3D_:tWm\u0019;j_:$C-\u001a4bk2$H%N\u000b\u0003\u0003GQ3!!\u0007j\u0003M9W\r\u001e)p_2,G\rR1uCN{WO]2f)-I\u0015\u0011FA\u0016\u0003[\ty#!\r\t\u000bU|\u0001\u0019\u0001<\t\r\u0005\u0015q\u00021\u0001w\u0011\u001d\tIa\u0004a\u0001\u0003\u0017Aa!a\u0005\u0010\u0001\u00041\bbBA\f\u001f\u0001\u0007\u0011\u0011\u0004\u000b\u0006)\u0006U\u0012q\u0007\u0005\u00065\u0012\u0001\ra\u0017\u0005\bE\u0012\u0001\n\u00111\u0001d\u0001")
public class ConnectionManager
implements LazyLogging {
    private final DataSourceProvider provider;
    private final HashMap<ConnectionKey, DataSource> pools;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static ConnectionManager connectionManager() {
        return ConnectionManager$.MODULE$.connectionManager();
    }

    private Logger logger$lzycompute() {
        ConnectionManager connectionManager = this;
        synchronized (connectionManager) {
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

    private HashMap<ConnectionKey, DataSource> pools() {
        return this.pools;
    }

    public Connection getConnection(GreenplumOptions dbOpts, boolean autoCommit) {
        Connection connection = this.getPooledConnection(dbOpts.url(), dbOpts.user(), dbOpts.password(), dbOpts.driver(), dbOpts.connectionPoolOptions());
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    public boolean getConnection$default$2() {
        return true;
    }

    public Connection getPooledConnection(String jdbcUrl, String userName, Option<String> password, String driver, ConnectionPoolOptions options) {
        return this.getPooledDataSource(jdbcUrl, userName, password, driver, options).getConnection();
    }

    public ConnectionPoolOptions getPooledConnection$default$5() {
        return new ConnectionPoolOptions();
    }

    public DataSource getPooledDataSource(String jdbcUrl, String userName, Option<String> password, String driver, ConnectionPoolOptions options) {
        BoxedUnit boxedUnit;
        ConnectionKey connectionKey = ConnectionKey$.MODULE$.apply(jdbcUrl, userName, password, options);
        if (!this.pools().containsKey(connectionKey)) {
            HashMap<ConnectionKey, DataSource> hashMap = this.pools();
            synchronized (hashMap) {
                Object object;
                if (!this.pools().containsKey(connectionKey)) {
                    DataSource dataSource = this.provider.createDataSource(connectionKey, password, driver, options);
                    object = this.pools().put(connectionKey, dataSource);
                } else {
                    object = BoxedUnit.UNIT;
                }
                BoxedUnit boxedUnit2 = object;
                // MONITOREXIT @DISABLED, blocks:[0, 1, 6] lbl12 : MonitorExitStatement: MONITOREXIT : var7_7
                boxedUnit = boxedUnit2;
            }
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return this.pools().get(connectionKey);
    }

    public ConnectionManager(DataSourceProvider provider) {
        this.provider = provider;
        LazyLogging.$init$(this);
        this.pools = new HashMap();
    }
}

