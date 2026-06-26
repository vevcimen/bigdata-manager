/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.immutable.StringOps
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcI$sp
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 *  scala.util.Try$
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.externaltable.GpfdistRequest;
import io.pivotal.greenplum.spark.externaltable.WebException;
import io.pivotal.greenplum.spark.externaltable.WebException$;
import java.io.Serializable;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Predef$;
import scala.Some;
import scala.collection.immutable.StringOps;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;
import scala.util.Try$;

public final class GpfdistRequest$
implements LazyLogging {
    public static GpfdistRequest$ MODULE$;
    private final String DISTRIBUTED_TRANSACTION_ID_HEADER;
    private final String SEGMENT_ID_HEADER;
    private final String SEGMENT_COUNT_HEADER;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new GpfdistRequest$();
    }

    private Logger logger$lzycompute() {
        GpfdistRequest$ gpfdistRequest$ = this;
        synchronized (gpfdistRequest$) {
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

    public String DISTRIBUTED_TRANSACTION_ID_HEADER() {
        return this.DISTRIBUTED_TRANSACTION_ID_HEADER;
    }

    public String SEGMENT_ID_HEADER() {
        return this.SEGMENT_ID_HEADER;
    }

    public String SEGMENT_COUNT_HEADER() {
        return this.SEGMENT_COUNT_HEADER;
    }

    public Try<GpfdistRequest> parse(HttpServletRequest request, String path) {
        return Try$.MODULE$.apply((Function0 & Serializable & scala.Serializable)() -> {
            Option option = Option$.MODULE$.apply((Object)request.getHeader(MODULE$.DISTRIBUTED_TRANSACTION_ID_HEADER()));
            if (!(option instanceof Some)) {
                if (None$.MODULE$.equals(option)) {
                    BoxedUnit boxedUnit;
                    String exceptionMessage = new StringBuilder(33).append(MODULE$.DISTRIBUTED_TRANSACTION_ID_HEADER()).append(" header is required for a request").toString();
                    if (MODULE$.logger().underlying().isDebugEnabled()) {
                        MODULE$.logger().underlying().debug("Incomplete headers in request: {}", new Object[]{exceptionMessage});
                        boxedUnit = BoxedUnit.UNIT;
                    } else {
                        boxedUnit = BoxedUnit.UNIT;
                    }
                    throw new WebException(400, exceptionMessage, WebException$.MODULE$.apply$default$3());
                }
                throw new MatchError((Object)option);
            }
            Some some = (Some)option;
            String transactionId = (String)some.value();
            String transactionId2 = transactionId;
            Try try_ = ((Try)Option$.MODULE$.apply((Object)request.getHeader(MODULE$.SEGMENT_ID_HEADER())).map((Function1 & Serializable & scala.Serializable)segIdString -> new Success(segIdString)).getOrElse((Function0 & Serializable & scala.Serializable)() -> new Failure((Throwable)new NoSuchElementException(new StringBuilder(33).append(MODULE$.SEGMENT_ID_HEADER()).append(" header is required for a request").toString())))).flatMap((Function1 & Serializable & scala.Serializable)segIdString -> Try$.MODULE$.apply((Function0)(JFunction0.mcI.sp & Serializable & scala.Serializable)() -> new StringOps(Predef$.MODULE$.augmentString(segIdString)).toInt()));
            if (!(try_ instanceof Success)) {
                if (try_ instanceof Failure) {
                    BoxedUnit boxedUnit;
                    Failure failure = (Failure)try_;
                    Throwable exception = failure.exception();
                    if (MODULE$.logger().underlying().isWarnEnabled()) {
                        MODULE$.logger().underlying().warn("Unable to parse {} request: {}", new String[]{MODULE$.SEGMENT_ID_HEADER(), exception.getMessage()});
                        boxedUnit = BoxedUnit.UNIT;
                    } else {
                        boxedUnit = BoxedUnit.UNIT;
                    }
                    throw new WebException(400, exception.getMessage(), WebException$.MODULE$.apply$default$3());
                }
                throw new MatchError((Object)try_);
            }
            Success success = (Success)try_;
            int segmentId = BoxesRunTime.unboxToInt((Object)success.value());
            int segmentId2 = segmentId;
            return new GpfdistRequest(transactionId2, segmentId2, path, request);
        });
    }

    private GpfdistRequest$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
        this.DISTRIBUTED_TRANSACTION_ID_HEADER = "X-GP-XID";
        this.SEGMENT_ID_HEADER = "X-GP-SEGMENT-ID";
        this.SEGMENT_COUNT_HEADER = "X-GP-SEGMENT-COUNT";
    }
}

