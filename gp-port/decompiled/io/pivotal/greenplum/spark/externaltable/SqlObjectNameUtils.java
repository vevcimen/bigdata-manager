/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.SqlObjectNameUtils$;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001-:Q\u0001B\u0003\t\u0002A1QAE\u0003\t\u0002MAQAG\u0001\u0005\u0002mAQ\u0001H\u0001\u0005\u0002u\t!cU9m\u001f\nTWm\u0019;OC6,W\u000b^5mg*\u0011aaB\u0001\u000eKb$XM\u001d8bYR\f'\r\\3\u000b\u0005!I\u0011!B:qCJ\\'B\u0001\u0006\f\u0003%9'/Z3oa2,XN\u0003\u0002\r\u001b\u00059\u0001/\u001b<pi\u0006d'\"\u0001\b\u0002\u0005%|7\u0001\u0001\t\u0003#\u0005i\u0011!\u0002\u0002\u0013'FdwJ\u00196fGRt\u0015-\\3Vi&d7o\u0005\u0002\u0002)A\u0011Q\u0003G\u0007\u0002-)\tq#A\u0003tG\u0006d\u0017-\u0003\u0002\u001a-\t1\u0011I\\=SK\u001a\fa\u0001P5oSRtD#\u0001\t\u0002\r\u0015\u001c8-\u00199f)\tq\u0012\u0006\u0005\u0002 M9\u0011\u0001\u0005\n\t\u0003CYi\u0011A\t\u0006\u0003G=\ta\u0001\u0010:p_Rt\u0014BA\u0013\u0017\u0003\u0019\u0001&/\u001a3fM&\u0011q\u0005\u000b\u0002\u0007'R\u0014\u0018N\\4\u000b\u0005\u00152\u0002\"\u0002\u0016\u0004\u0001\u0004q\u0012!A:")
public final class SqlObjectNameUtils {
    public static String escape(String string) {
        return SqlObjectNameUtils$.MODULE$.escape(string);
    }
}

