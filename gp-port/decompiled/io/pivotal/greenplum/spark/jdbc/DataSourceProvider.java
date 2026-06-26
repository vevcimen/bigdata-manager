/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey;
import javax.sql.DataSource;
import scala.Option;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001}2qAA\u0002\u0011\u0002G\u0005a\u0002C\u0003\u0016\u0001\u0019\u0005aC\u0001\nECR\f7k\\;sG\u0016\u0004&o\u001c<jI\u0016\u0014(B\u0001\u0003\u0006\u0003\u0011QGMY2\u000b\u0005\u00199\u0011!B:qCJ\\'B\u0001\u0005\n\u0003%9'/Z3oa2,XN\u0003\u0002\u000b\u0017\u00059\u0001/\u001b<pi\u0006d'\"\u0001\u0007\u0002\u0005%|7\u0001A\n\u0003\u0001=\u0001\"\u0001E\n\u000e\u0003EQ\u0011AE\u0001\u0006g\u000e\fG.Y\u0005\u0003)E\u0011a!\u00118z%\u00164\u0017\u0001E2sK\u0006$X\rR1uCN{WO]2f)\u00159r$J\u001b8!\tAR$D\u0001\u001a\u0015\tQ2$A\u0002tc2T\u0011\u0001H\u0001\u0006U\u00064\u0018\r_\u0005\u0003=e\u0011!\u0002R1uCN{WO]2f\u0011\u0015\u0001\u0013\u00011\u0001\"\u0003\rYW-\u001f\t\u0003E\rj\u0011aA\u0005\u0003I\r\u0011QbQ8o]\u0016\u001cG/[8o\u0017\u0016L\b\"\u0002\u0014\u0002\u0001\u00049\u0013\u0001\u00039bgN<xN\u001d3\u0011\u0007AA#&\u0003\u0002*#\t1q\n\u001d;j_:\u0004\"a\u000b\u001a\u000f\u00051\u0002\u0004CA\u0017\u0012\u001b\u0005q#BA\u0018\u000e\u0003\u0019a$o\\8u}%\u0011\u0011'E\u0001\u0007!J,G-\u001a4\n\u0005M\"$AB*ue&twM\u0003\u00022#!)a'\u0001a\u0001U\u00051AM]5wKJDQ\u0001O\u0001A\u0002e\nqa\u001c9uS>t7\u000f\u0005\u0002;{5\t1H\u0003\u0002=\u000b\u0005!1m\u001c8g\u0013\tq4HA\u000bD_:tWm\u0019;j_:\u0004vn\u001c7PaRLwN\\:")
public interface DataSourceProvider {
    public DataSource createDataSource(ConnectionKey var1, Option<String> var2, String var3, ConnectionPoolOptions var4);
}

