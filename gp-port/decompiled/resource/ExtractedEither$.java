/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.util.Either
 */
package resource;

import resource.ExtractedEither;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.util.Either;

public final class ExtractedEither$
implements Serializable {
    public static ExtractedEither$ MODULE$;

    static {
        new ExtractedEither$();
    }

    public final String toString() {
        return "ExtractedEither";
    }

    public <A, B> ExtractedEither<A, B> apply(Either<A, B> either) {
        return new ExtractedEither<A, B>(either);
    }

    public <A, B> Option<Either<A, B>> unapply(ExtractedEither<A, B> x$0) {
        return x$0 == null ? None$.MODULE$ : new Some(x$0.either());
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ExtractedEither$() {
        MODULE$ = this;
    }
}

