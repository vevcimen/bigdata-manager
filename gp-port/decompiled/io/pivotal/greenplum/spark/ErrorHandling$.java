/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.PartialFunction
 *  scala.Serializable
 *  scala.runtime.BoxedUnit
 *  scala.util.Failure
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import scala.Function1;
import scala.PartialFunction;
import scala.Serializable;
import scala.runtime.BoxedUnit;
import scala.util.Failure;
import scala.util.Try;

public final class ErrorHandling$
implements LazyLogging {
    public static ErrorHandling$ MODULE$;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new ErrorHandling$();
    }

    private Logger logger$lzycompute() {
        ErrorHandling$ errorHandling$ = this;
        synchronized (errorHandling$) {
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

    public <U> PartialFunction<Throwable, Try<U>> wrapErrorMessage(String message) {
        return new Serializable(message){
            public static final long serialVersionUID = 0L;
            private final String message$1;

            public final <A1 extends Throwable, B1> B1 applyOrElse(A1 x1, Function1<A1, B1> function1) {
                A1 A1 = x1;
                return (B1)new Failure((Throwable)new RuntimeException(this.message$1, A1));
            }

            public final boolean isDefinedAt(Throwable x1) {
                Throwable throwable = x1;
                return true;
            }
            {
                this.message$1 = message$1;
            }
        };
    }

    public void appendCauseToErrorChain(Throwable originalError, Throwable newError) {
        this.inner$1(originalError, newError, 100);
    }

    private final void inner$1(Throwable origErr, Throwable newErr, int counter) {
        while (true) {
            BoxedUnit boxedUnit;
            if (origErr.getCause() == null) {
                origErr.initCause(newErr);
                boxedUnit = BoxedUnit.UNIT;
                break;
            }
            if (counter == 0) {
                BoxedUnit boxedUnit2;
                if (this.logger().underlying().isWarnEnabled()) {
                    this.logger().underlying().warn("Tried to append {} as a cause of {} and failed.", new String[]{newErr.getMessage(), origErr.getMessage()});
                    boxedUnit2 = BoxedUnit.UNIT;
                } else {
                    boxedUnit2 = BoxedUnit.UNIT;
                }
                if (this.logger().underlying().isDebugEnabled()) {
                    this.logger().underlying().debug("The cause of {} was {}", new String[]{newErr.getMessage(), newErr.getCause().getMessage()});
                    boxedUnit = BoxedUnit.UNIT;
                    break;
                }
                boxedUnit = BoxedUnit.UNIT;
                break;
            }
            --counter;
            origErr = origErr.getCause();
        }
    }

    private ErrorHandling$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
    }
}

