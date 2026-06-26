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

@ScalaSignature(bytes="\u0006\u0001\u00052q!\u0001\u0002\u0011\u0002\u0007\u0005\u0011BA\u0006MCjLHj\\4hS:<'BA\u0002\u0005\u00031\u00198-\u00197bY><w-\u001b8h\u0015\t)a!\u0001\u0005usB,7/\u00194f\u0015\u00059\u0011aA2p[\u000e\u00011C\u0001\u0001\u000b!\tYa\"D\u0001\r\u0015\u0005i\u0011!B:dC2\f\u0017BA\b\r\u0005\u0019\te.\u001f*fM\")\u0011\u0003\u0001C\u0001%\u00051A%\u001b8ji\u0012\"\u0012a\u0005\t\u0003\u0017QI!!\u0006\u0007\u0003\tUs\u0017\u000e\u001e\u0005\t/\u0001A)\u0019!C\t1\u00051An\\4hKJ,\u0012!\u0007\t\u00035mi\u0011AA\u0005\u00039\t\u0011a\u0001T8hO\u0016\u0014\bF\u0001\f\u001f!\tYq$\u0003\u0002!\u0019\tIAO]1og&,g\u000e\u001e")
public interface LazyLogging {
    public static /* synthetic */ Logger logger$(LazyLogging $this) {
        return $this.logger();
    }

    default public Logger logger() {
        return Logger$.MODULE$.apply(LoggerFactory.getLogger(this.getClass().getName()));
    }

    public static void $init$(LazyLogging $this) {
    }
}

