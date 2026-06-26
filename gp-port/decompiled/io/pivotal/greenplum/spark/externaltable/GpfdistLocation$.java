/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple4
 *  scala.runtime.BoxesRunTime
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple4;
import scala.runtime.BoxesRunTime;

public final class GpfdistLocation$
implements Serializable {
    public static GpfdistLocation$ MODULE$;

    static {
        new GpfdistLocation$();
    }

    public GpfdistLocation apply(String host, int port) {
        return new GpfdistLocation("gpfdist", host, port, "/");
    }

    public GpfdistLocation apply(String host, int port, boolean useSsl) {
        String protocol = useSsl ? "gpfdists" : "gpfdist";
        return new GpfdistLocation(protocol, host, port, "/");
    }

    public GpfdistLocation apply(String protocol, String host, int port, String path) {
        return new GpfdistLocation(protocol, host, port, path);
    }

    public Option<Tuple4<String, String, Object, String>> unapply(GpfdistLocation x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple4((Object)x$0.protocol(), (Object)x$0.host(), (Object)BoxesRunTime.boxToInteger((int)x$0.port()), (Object)x$0.path()));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GpfdistLocation$() {
        MODULE$ = this;
    }
}

