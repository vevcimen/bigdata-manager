/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple3
 *  scala.runtime.AbstractFunction3
 *  scala.runtime.BoxesRunTime
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.WebException;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple3;
import scala.runtime.AbstractFunction3;
import scala.runtime.BoxesRunTime;

public final class WebException$
extends AbstractFunction3<Object, String, Option<Throwable>, WebException>
implements Serializable {
    public static WebException$ MODULE$;

    static {
        new WebException$();
    }

    public String $lessinit$greater$default$2() {
        return null;
    }

    public Option<Throwable> $lessinit$greater$default$3() {
        return None$.MODULE$;
    }

    public final String toString() {
        return "WebException";
    }

    public WebException apply(int code, String message, Option<Throwable> cause) {
        return new WebException(code, message, cause);
    }

    public String apply$default$2() {
        return null;
    }

    public Option<Throwable> apply$default$3() {
        return None$.MODULE$;
    }

    public Option<Tuple3<Object, String, Option<Throwable>>> unapply(WebException x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple3((Object)BoxesRunTime.boxToInteger((int)x$0.code()), (Object)x$0.message(), x$0.cause()));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private WebException$() {
        MODULE$ = this;
    }
}

