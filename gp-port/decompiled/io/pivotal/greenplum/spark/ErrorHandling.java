/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.PartialFunction
 *  scala.reflect.ScalaSignature
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.ErrorHandling$;
import scala.PartialFunction;
import scala.reflect.ScalaSignature;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001m;Q!\u0002\u0004\t\u0002=1Q!\u0005\u0004\t\u0002IAQaI\u0001\u0005\u0002\u0011BQ!J\u0001\u0005\u0002\u0019BQAU\u0001\u0005\u0002M\u000bQ\"\u0012:s_JD\u0015M\u001c3mS:<'BA\u0004\t\u0003\u0015\u0019\b/\u0019:l\u0015\tI!\"A\u0005he\u0016,g\u000e\u001d7v[*\u00111\u0002D\u0001\ba&4x\u000e^1m\u0015\u0005i\u0011AA5p\u0007\u0001\u0001\"\u0001E\u0001\u000e\u0003\u0019\u0011Q\"\u0012:s_JD\u0015M\u001c3mS:<7cA\u0001\u00143A\u0011AcF\u0007\u0002+)\ta#A\u0003tG\u0006d\u0017-\u0003\u0002\u0019+\t1\u0011I\\=SK\u001a\u0004\"AG\u0011\u000e\u0003mQ!\u0001H\u000f\u0002\u0019M\u001c\u0017\r\\1m_\u001e<\u0017N\\4\u000b\u0005yy\u0012\u0001\u0003;za\u0016\u001c\u0018MZ3\u000b\u0003\u0001\n1aY8n\u0013\t\u00113DA\u0006MCjLHj\\4hS:<\u0017A\u0002\u001fj]&$h\bF\u0001\u0010\u0003A9(/\u00199FeJ|'/T3tg\u0006<W-\u0006\u0002(\u007fQ\u0011\u0001\u0006\u0013\t\u0005)%Zs'\u0003\u0002++\ty\u0001+\u0019:uS\u0006dg)\u001e8di&|g\u000e\u0005\u0002-i9\u0011QF\r\b\u0003]Ej\u0011a\f\u0006\u0003a9\ta\u0001\u0010:p_Rt\u0014\"\u0001\f\n\u0005M*\u0012a\u00029bG.\fw-Z\u0005\u0003kY\u0012\u0011\u0002\u00165s_^\f'\r\\3\u000b\u0005M*\u0002c\u0001\u001d<{5\t\u0011H\u0003\u0002;+\u0005!Q\u000f^5m\u0013\ta\u0014HA\u0002Uef\u0004\"AP \r\u0001\u0011)\u0001i\u0001b\u0001\u0003\n\tQ+\u0005\u0002C\u000bB\u0011AcQ\u0005\u0003\tV\u0011qAT8uQ&tw\r\u0005\u0002\u0015\r&\u0011q)\u0006\u0002\u0004\u0003:L\b\"B%\u0004\u0001\u0004Q\u0015aB7fgN\fw-\u001a\t\u0003\u0017>s!\u0001T'\u0011\u00059*\u0012B\u0001(\u0016\u0003\u0019\u0001&/\u001a3fM&\u0011\u0001+\u0015\u0002\u0007'R\u0014\u0018N\\4\u000b\u00059+\u0012aF1qa\u0016tGmQ1vg\u0016$v.\u0012:s_J\u001c\u0005.Y5o)\r!v+\u0017\t\u0003)UK!AV\u000b\u0003\tUs\u0017\u000e\u001e\u0005\u00061\u0012\u0001\raK\u0001\u000e_JLw-\u001b8bY\u0016\u0013(o\u001c:\t\u000bi#\u0001\u0019A\u0016\u0002\u00119,w/\u0012:s_J\u0004")
public final class ErrorHandling {
    public static void appendCauseToErrorChain(Throwable throwable, Throwable throwable2) {
        ErrorHandling$.MODULE$.appendCauseToErrorChain(throwable, throwable2);
    }

    public static <U> PartialFunction<Throwable, Try<U>> wrapErrorMessage(String string) {
        return ErrorHandling$.MODULE$.wrapErrorMessage(string);
    }
}

