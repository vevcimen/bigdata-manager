/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package com.typesafe.scalalogging;

import com.typesafe.scalalogging.Logger;
import com.typesafe.scalalogging.Logger$;
import org.slf4j.LoggerFactory;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001u1q!\u0001\u0002\u0011\u0002\u0007\u0005\u0011BA\u0007TiJL7\r\u001e'pO\u001eLgn\u001a\u0006\u0003\u0007\u0011\tAb]2bY\u0006dwnZ4j]\u001eT!!\u0002\u0004\u0002\u0011QL\b/Z:bM\u0016T\u0011aB\u0001\u0004G>l7\u0001A\n\u0003\u0001)\u0001\"a\u0003\b\u000e\u00031Q\u0011!D\u0001\u0006g\u000e\fG.Y\u0005\u0003\u001f1\u0011a!\u00118z%\u00164\u0007\"B\t\u0001\t\u0003\u0011\u0012A\u0002\u0013j]&$H\u0005F\u0001\u0014!\tYA#\u0003\u0002\u0016\u0019\t!QK\\5u\u0011\u001d9\u0002A1A\u0005\u0012a\ta\u0001\\8hO\u0016\u0014X#A\r\u0011\u0005iYR\"\u0001\u0002\n\u0005q\u0011!A\u0002'pO\u001e,'\u000f")
public interface StrictLogging {
    public void com$typesafe$scalalogging$StrictLogging$_setter_$logger_$eq(Logger var1);

    public Logger logger();

    public static void $init$(StrictLogging $this) {
        $this.com$typesafe$scalalogging$StrictLogging$_setter_$logger_$eq(Logger$.MODULE$.apply(LoggerFactory.getLogger($this.getClass().getName())));
    }
}

