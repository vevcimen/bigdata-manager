/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.jdbc;

import com.zaxxer.hikari.HikariConfig;
import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey;
import io.pivotal.greenplum.spark.jdbc.HikariProvider$;
import javax.sql.DataSource;
import scala.Option;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001\u0011<Qa\u0002\u0005\t\u0002M1Q!\u0006\u0005\t\u0002YAQAK\u0001\u0005\u0002-Bq\u0001L\u0001C\u0002\u0013%Q\u0006\u0003\u0004:\u0003\u0001\u0006IA\f\u0005\u0006u\u0005!\ta\u000f\u0005\u0006\u0019\u0006!\t%T\u0001\u000f\u0011&\\\u0017M]5Qe>4\u0018\u000eZ3s\u0015\tI!\"\u0001\u0003kI\n\u001c'BA\u0006\r\u0003\u0015\u0019\b/\u0019:l\u0015\tia\"A\u0005he\u0016,g\u000e\u001d7v[*\u0011q\u0002E\u0001\ba&4x\u000e^1m\u0015\u0005\t\u0012AA5p\u0007\u0001\u0001\"\u0001F\u0001\u000e\u0003!\u0011a\u0002S5lCJL\u0007K]8wS\u0012,'o\u0005\u0003\u0002/u\u0001\u0003C\u0001\r\u001c\u001b\u0005I\"\"\u0001\u000e\u0002\u000bM\u001c\u0017\r\\1\n\u0005qI\"AB!osJ+g\r\u0005\u0002\u0015=%\u0011q\u0004\u0003\u0002\u0013\t\u0006$\u0018mU8ve\u000e,\u0007K]8wS\u0012,'\u000f\u0005\u0002\"Q5\t!E\u0003\u0002$I\u0005a1oY1mC2|wmZ5oO*\u0011QEJ\u0001\tif\u0004Xm]1gK*\tq%A\u0002d_6L!!\u000b\u0012\u0003\u00171\u000b'0\u001f'pO\u001eLgnZ\u0001\u0007y%t\u0017\u000e\u001e \u0015\u0003M\tq\u0002\u0015*P!\u0016\u0013F+W0Q%\u00163\u0015\nW\u000b\u0002]A\u0011qF\u000e\b\u0003aQ\u0002\"!M\r\u000e\u0003IR!a\r\n\u0002\rq\u0012xn\u001c;?\u0013\t)\u0014$\u0001\u0004Qe\u0016$WMZ\u0005\u0003oa\u0012aa\u0015;sS:<'BA\u001b\u001a\u0003A\u0001&k\u0014)F%RKv\f\u0015*F\r&C\u0006%A\bhKRD\u0015n[1sS\u000e{gNZ5h)\taD\t\u0005\u0002>\u00056\taH\u0003\u0002@\u0001\u00061\u0001.[6be&T!!\u0011\u0014\u0002\ri\f\u0007\u0010_3s\u0013\t\u0019eH\u0001\u0007IS.\f'/[\"p]\u001aLw\rC\u0003F\u000b\u0001\u0007a)A\u000bd_:tWm\u0019;j_:\u0004vn\u001c7PaRLwN\\:\u0011\u0005\u001dSU\"\u0001%\u000b\u0005%S\u0011\u0001B2p]\u001aL!a\u0013%\u0003+\r{gN\\3di&|g\u000eU8pY>\u0003H/[8og\u0006\u00012M]3bi\u0016$\u0015\r^1T_V\u00148-\u001a\u000b\u0006\u001dZ[\u0006M\u0019\t\u0003\u001fRk\u0011\u0001\u0015\u0006\u0003#J\u000b1a]9m\u0015\u0005\u0019\u0016!\u00026bm\u0006D\u0018BA+Q\u0005)!\u0015\r^1T_V\u00148-\u001a\u0005\u0006/\u001a\u0001\r\u0001W\u0001\u0004W\u0016L\bC\u0001\u000bZ\u0013\tQ\u0006BA\u0007D_:tWm\u0019;j_:\\U-\u001f\u0005\u00069\u001a\u0001\r!X\u0001\ta\u0006\u001c8o^8sIB\u0019\u0001D\u0018\u0018\n\u0005}K\"AB(qi&|g\u000eC\u0003b\r\u0001\u0007a&\u0001\u0004ee&4XM\u001d\u0005\u0006G\u001a\u0001\rAR\u0001\b_B$\u0018n\u001c8t\u0001")
public final class HikariProvider {
    public static DataSource createDataSource(ConnectionKey connectionKey, Option<String> option, String string, ConnectionPoolOptions connectionPoolOptions) {
        return HikariProvider$.MODULE$.createDataSource(connectionKey, option, string, connectionPoolOptions);
    }

    public static HikariConfig getHikariConfig(ConnectionPoolOptions connectionPoolOptions) {
        return HikariProvider$.MODULE$.getHikariConfig(connectionPoolOptions);
    }
}

