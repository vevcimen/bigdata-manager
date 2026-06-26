/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.Row
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.Iterator
 *  scala.collection.Seq$
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.List
 *  scala.collection.immutable.List$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcV$sp
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 *  scala.util.Try$
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.GreenplumCSVFormat$;
import io.pivotal.greenplum.spark.externaltable.GpfdistRequest;
import io.pivotal.greenplum.spark.externaltable.GpfdistRequest$;
import io.pivotal.greenplum.spark.externaltable.PartitionData;
import io.pivotal.greenplum.spark.externaltable.WebException;
import io.pivotal.greenplum.spark.externaltable.WebException$;
import io.pivotal.greenplum.spark.util.TransactionData;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.spark.sql.Row;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Some;
import scala.collection.Iterator;
import scala.collection.Seq$;
import scala.collection.TraversableOnce;
import scala.collection.immutable.List;
import scala.collection.immutable.List$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;
import scala.util.Try$;

@ScalaSignature(bytes="\u0006\u0001\u0005Ud\u0001B\u0007\u000f\u0001eA\u0001B\r\u0001\u0003\u0002\u0003\u0006Ia\r\u0005\t%\u0002\u0011\t\u0011)A\u0005'\")\u0001\f\u0001C\u00013\")\u0001\f\u0001C\u0001;\")a\f\u0001C!?\")Q\u0010\u0001C\u0001}\"9\u0011Q\u0002\u0001\u0005\n\u0005=\u0001bBA\f\u0001\u0011%\u0011\u0011\u0004\u0005\b\u0003C\u0001A\u0011BA\u0012\u0011\u001d\tY\u0003\u0001C\u0005\u0003[Aq!!\r\u0001\t\u0013\t\u0019\u0004C\u0004\u0002L\u0001!I!!\u0014\u0003\u001d\u001d\u0003h\rZ5ti\"\u000bg\u000e\u001a7fe*\u0011q\u0002E\u0001\u000eKb$XM\u001d8bYR\f'\r\\3\u000b\u0005E\u0011\u0012!B:qCJ\\'BA\n\u0015\u0003%9'/Z3oa2,XN\u0003\u0002\u0016-\u00059\u0001/\u001b<pi\u0006d'\"A\f\u0002\u0005%|7\u0001A\n\u0004\u0001iA\u0003CA\u000e'\u001b\u0005a\"BA\u000f\u001f\u0003\u001dA\u0017M\u001c3mKJT!a\b\u0011\u0002\rM,'O^3s\u0015\t\t#%A\u0003kKR$\u0018P\u0003\u0002$I\u00059Qm\u00197jaN,'\"A\u0013\u0002\u0007=\u0014x-\u0003\u0002(9\ty\u0011IY:ue\u0006\u001cG\u000fS1oI2,'\u000f\u0005\u0002*a5\t!F\u0003\u0002,Y\u0005a1oY1mC2|wmZ5oO*\u0011QFL\u0001\tif\u0004Xm]1gK*\tq&A\u0002d_6L!!\r\u0016\u0003\u00171\u000b'0\u001f'pO\u001eLgnZ\u0001\nEV4g-\u001a:NCB\u0004B\u0001N\u001d<\u00116\tQG\u0003\u00027o\u0005!Q\u000f^5m\u0015\u0005A\u0014\u0001\u00026bm\u0006L!AO\u001b\u0003\u00075\u000b\u0007\u000f\u0005\u0002=\u000b:\u0011Qh\u0011\t\u0003}\u0005k\u0011a\u0010\u0006\u0003\u0001b\ta\u0001\u0010:p_Rt$\"\u0001\"\u0002\u000bM\u001c\u0017\r\\1\n\u0005\u0011\u000b\u0015A\u0002)sK\u0012,g-\u0003\u0002G\u000f\n11\u000b\u001e:j]\u001eT!\u0001R!\u0011\u0007%[U*D\u0001K\u0015\t1\u0014)\u0003\u0002M\u0015\n\u0019AK]=\u0011\u00059\u0003V\"A(\u000b\u0005Y\u0002\u0012BA)P\u0005=!&/\u00198tC\u000e$\u0018n\u001c8ECR\f\u0017!D:f]\u0012\u0014UO\u001a4fe6\u000b\u0007\u000f\u0005\u00035sm\"\u0006CA+W\u001b\u0005q\u0011BA,\u000f\u00055\u0001\u0016M\u001d;ji&|g\u000eR1uC\u00061A(\u001b8jiz\"2AW.]!\t)\u0006\u0001C\u00033\u0007\u0001\u00071\u0007C\u0003S\u0007\u0001\u00071\u000bF\u0001[\u0003\u0019A\u0017M\u001c3mKR)\u0001\r\u001a4mqB\u0011\u0011MY\u0007\u0002\u0003&\u00111-\u0011\u0002\u0005+:LG\u000fC\u0003f\u000b\u0001\u00071(\u0001\u0003qCRD\u0007\"B4\u0006\u0001\u0004A\u0017a\u00032bg\u0016\u0014V-];fgR\u0004\"!\u001b6\u000e\u0003yI!a\u001b\u0010\u0003\u000fI+\u0017/^3ti\")Q.\u0002a\u0001]\u00069!/Z9vKN$\bCA8w\u001b\u0005\u0001(BA9s\u0003\u0011AG\u000f\u001e9\u000b\u0005M$\u0018aB:feZdW\r\u001e\u0006\u0002k\u0006)!.\u0019<bq&\u0011q\u000f\u001d\u0002\u0013\u0011R$\boU3sm2,GOU3rk\u0016\u001cH\u000fC\u0003z\u000b\u0001\u0007!0\u0001\u0005sKN\u0004xN\\:f!\ty70\u0003\u0002}a\n\u0019\u0002\n\u001e;q'\u0016\u0014h\u000f\\3u%\u0016\u001c\bo\u001c8tK\u0006I\u0002.\u00198eY\u00164\u0016\r\\5e\u000fB4G-[:u%\u0016\fX/Z:u)\u0015y\u0018qAA\u0006!\u0011I5*!\u0001\u0011\u0007U\u000b\u0019!C\u0002\u0002\u00069\u0011ab\u00129gI&\u001cHOU3rk\u0016\u001cH\u000fC\u0004\u0002\n\u0019\u0001\r!!\u0001\u0002\u001d\u001d\u0004h\rZ5tiJ+\u0017/^3ti\")\u0011P\u0002a\u0001u\u0006I\u0001.\u00198eY\u0016<U\t\u0016\u000b\u0007\u0003#\t\u0019\"!\u0006\u0011\u0007%[\u0005\rC\u0004\u0002\n\u001d\u0001\r!!\u0001\t\u000be<\u0001\u0019\u0001>\u0002)A\u0014xnY3tgB\u000b'\u000f^5uS>tG)\u0019;b)\u0019\t\t\"a\u0007\u0002\u001e!)\u0011\u0010\u0003a\u0001u\"1\u0011q\u0004\u0005A\u0002m\nQ\u0002\u001e:b]N\f7\r^5p]&#\u0017!C:feZ,G)\u0019;b)\u0019\t\t\"!\n\u0002(!)\u00110\u0003a\u0001u\"1\u0011\u0011F\u0005A\u0002Q\u000bQ\u0002]1si&$\u0018n\u001c8ECR\f\u0017A\u00035b]\u0012dW\rU(T)R\u0019\u0001-a\f\t\u000f\u0005%!\u00021\u0001\u0002\u0002\u0005q!o\\<U_\u000e\u001bfk\u0015;sS:<GcA\u001e\u00026!9\u0011qG\u0006A\u0002\u0005e\u0012a\u0001:poB!\u00111HA$\u001b\t\tiD\u0003\u0003\u0002@\u0005\u0005\u0013aA:rY*\u0019\u0011#a\u0011\u000b\u0007\u0005\u0015C%\u0001\u0004ba\u0006\u001c\u0007.Z\u0005\u0005\u0003\u0013\niDA\u0002S_^\fA\u0002\u001d:pG\u0016\u001c8/\u0012:s_J$2\u0002YA(\u0003#\n\u0019&!\u0016\u0002`!)Q\r\u0004a\u0001w!)Q\u000e\u0004a\u0001]\")\u0011\u0010\u0004a\u0001u\"9\u0011q\u000b\u0007A\u0002\u0005e\u0013\u0001B2pI\u0016\u00042!YA.\u0013\r\ti&\u0011\u0002\u0004\u0013:$\bbBA1\u0019\u0001\u0007\u00111M\u0001\u0002KB!\u0011QMA8\u001d\u0011\t9'a\u001b\u000f\u0007y\nI'C\u0001C\u0013\r\ti'Q\u0001\ba\u0006\u001c7.Y4f\u0013\u0011\t\t(a\u001d\u0003\u0013QC'o\\<bE2,'bAA7\u0003\u0002")
public class GpfdistHandler
extends AbstractHandler
implements LazyLogging {
    private final Map<String, Try<TransactionData>> bufferMap;
    private final Map<String, PartitionData> sendBufferMap;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        GpfdistHandler gpfdistHandler = this;
        synchronized (gpfdistHandler) {
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

    @Override
    public void handle(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        Try result = GpfdistRequest$.MODULE$.parse(request, path).flatMap((Function1 & Serializable & scala.Serializable)gpfdistRequest -> this.handleValidGpfdistRequest((GpfdistRequest)gpfdistRequest, response));
        Try try_ = result;
        if (try_ instanceof Success) {
            Success success = (Success)try_;
            GpfdistRequest gpfdistRequest2 = (GpfdistRequest)success.value();
            response.setStatus(200);
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("[{}] Successfully handled {} request for {}", gpfdistRequest2.identifier(), gpfdistRequest2.requestType(), path);
            }
        } else if (try_ instanceof Failure) {
            BoxedUnit boxedUnit;
            int code;
            int n;
            Failure failure = (Failure)try_;
            Throwable e = failure.exception();
            Throwable throwable = e;
            if (throwable instanceof WebException) {
                int c;
                WebException webException = (WebException)throwable;
                n = c = webException.code();
            } else {
                n = code = 500;
            }
            if (this.logger().underlying().isDebugEnabled()) {
                e.printStackTrace();
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            this.processError(path, request, response, code, e);
        } else {
            throw new MatchError((Object)try_);
        }
        baseRequest.setHandled(true);
    }

    public Try<GpfdistRequest> handleValidGpfdistRequest(GpfdistRequest gpfdistRequest, HttpServletResponse response) {
        BoxedUnit boxedUnit;
        String requestType = gpfdistRequest.requestType();
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("[{}] Received {} request", new String[]{gpfdistRequest.identifier(), requestType});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        String string = requestType;
        if ("GET".equals(string)) {
            return this.handleGET(gpfdistRequest, response).map((Function1 & Serializable & scala.Serializable)x$1 -> gpfdistRequest);
        }
        if ("POST".equals(string)) {
            return Try$.MODULE$.apply((Function0)(JFunction0.mcV.sp & Serializable & scala.Serializable)() -> this.handlePOST(gpfdistRequest)).map((Function1 & Serializable & scala.Serializable)x$2 -> gpfdistRequest);
        }
        return new Failure((Throwable)new WebException(405, new StringBuilder(24).append("Method ").append(requestType).append(" is not supported").toString(), WebException$.MODULE$.apply$default$3()));
    }

    private Try<BoxedUnit> handleGET(GpfdistRequest gpfdistRequest, HttpServletResponse response) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("[{}] Processing GET request for {}", new String[]{gpfdistRequest.identifier(), gpfdistRequest.path()});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return this.processPartitionData(response, gpfdistRequest.transactionId());
    }

    private Try<BoxedUnit> processPartitionData(HttpServletResponse response, String transactionId) {
        Option option = Option$.MODULE$.apply((Object)this.sendBufferMap.get(transactionId));
        if (None$.MODULE$.equals(option)) {
            return new Failure((Throwable)new WebException(400, new StringBuilder(22).append("no data available for ").append(transactionId).toString(), WebException$.MODULE$.apply$default$3()));
        }
        if (option instanceof Some) {
            Some some = (Some)option;
            PartitionData partitionData = (PartitionData)some.value();
            if (partitionData.handled().compareAndSet(false, true)) {
                return this.serveData(response, partitionData);
            }
            return new Success((Object)BoxedUnit.UNIT);
        }
        throw new MatchError((Object)option);
    }

    private Try<BoxedUnit> serveData(HttpServletResponse response, PartitionData partitionData) {
        return Try$.MODULE$.apply((Function0)(JFunction0.mcV.sp & Serializable & scala.Serializable)() -> {
            response.setContentType("text/plain");
            response.setCharacterEncoding(GreenplumCSVFormat$.MODULE$.DEFAULT_ENCODING());
            PrintWriter writer = response.getWriter();
            Function1<Row, Row> reorder = partitionData.rowTransformer();
            if (partitionData.rowIterator() != null) {
                Iterator<Row> iterator = partitionData.rowIterator();
                while (iterator.hasNext()) {
                    writer.println(this.rowToCSVString((Row)reorder.apply(iterator.next())));
                }
            } else {
                ((List)partitionData.rows().map(reorder, List$.MODULE$.canBuildFrom())).foreach((Function1 & Serializable & scala.Serializable)row -> {
                    writer.println(this.rowToCSVString(row));
                    return BoxedUnit.UNIT;
                });
                partitionData.rows_$eq(null);
            }
            writer.flush();
        });
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void handlePOST(GpfdistRequest gpfdistRequest) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("[{}] Processing POST request for {}", new String[]{gpfdistRequest.identifier(), gpfdistRequest.path()});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        int n = gpfdistRequest.contentLength();
        if (n < 0) {
            throw new WebException(413, "Size of request is too large", WebException$.MODULE$.apply$default$3());
        }
        if (0 == n) {
            return;
        }
        Option option = Option$.MODULE$.apply((Object)gpfdistRequest.inputStream());
        if (None$.MODULE$.equals(option)) {
            BoxedUnit boxedUnit2;
            if (this.logger().underlying().isWarnEnabled()) {
                this.logger().underlying().warn(new StringBuilder(103).append("[").append(gpfdistRequest.identifier()).append("] Received POST request with non-zero content length ").append("(").append(gpfdistRequest.contentLength()).append(") but unable to retrieve the body of the request").toString());
                boxedUnit2 = BoxedUnit.UNIT;
                throw new WebException(400, WebException$.MODULE$.apply$default$2(), WebException$.MODULE$.apply$default$3());
            } else {
                boxedUnit2 = BoxedUnit.UNIT;
            }
            throw new WebException(400, WebException$.MODULE$.apply$default$2(), WebException$.MODULE$.apply$default$3());
        }
        if (!(option instanceof Some)) throw new MatchError((Object)option);
        Some some = (Some)option;
        ServletInputStream requestInputStream = (ServletInputStream)some.value();
        this.bufferMap.putIfAbsent(gpfdistRequest.transactionId(), (Try<TransactionData>)new Success((Object)new TransactionData()));
        Try<TransactionData> try_ = this.bufferMap.get(gpfdistRequest.transactionId());
        if (try_ instanceof Failure) {
            BoxedUnit boxedUnit3;
            Failure failure = (Failure)try_;
            Throwable exception = failure.exception();
            if (this.logger().underlying().isWarnEnabled()) {
                this.logger().underlying().warn(new StringBuilder(79).append("[").append(gpfdistRequest.identifier()).append("] Received additional request data for a transaction ").append("that previously errored: ").append(exception.getMessage()).toString());
                boxedUnit3 = BoxedUnit.UNIT;
                throw new WebException(500, WebException$.MODULE$.apply$default$2(), WebException$.MODULE$.apply$default$3());
            } else {
                boxedUnit3 = BoxedUnit.UNIT;
            }
            throw new WebException(500, WebException$.MODULE$.apply$default$2(), WebException$.MODULE$.apply$default$3());
        }
        if (!(try_ instanceof Success)) throw new MatchError(try_);
        Success success = (Success)try_;
        TransactionData txData = (TransactionData)success.value();
        Try<BoxedUnit> try_2 = txData.write(gpfdistRequest.segmentId(), requestInputStream, gpfdistRequest.contentLength());
        if (try_2 instanceof Failure) {
            BoxedUnit boxedUnit4;
            Failure failure = (Failure)try_2;
            Throwable exception = failure.exception();
            this.bufferMap.put(gpfdistRequest.transactionId(), (Try<TransactionData>)new Failure(exception));
            if (this.logger().underlying().isWarnEnabled()) {
                this.logger().underlying().warn("[{}] Attempted to copy data from the request but failed: {}", new String[]{gpfdistRequest.identifier(), exception.getMessage()});
                boxedUnit4 = BoxedUnit.UNIT;
                throw new WebException(400, exception.getMessage(), WebException$.MODULE$.apply$default$3());
            } else {
                boxedUnit4 = BoxedUnit.UNIT;
            }
            throw new WebException(400, exception.getMessage(), WebException$.MODULE$.apply$default$3());
        }
        if (!(try_2 instanceof Success)) throw new MatchError(try_2);
        if (!this.logger().underlying().isDebugEnabled()) return;
        this.logger().underlying().debug("[{}] Copied {} additional bytes of data from the request", new Object[]{gpfdistRequest.identifier(), BoxesRunTime.boxToInteger((int)gpfdistRequest.contentLength())});
    }

    private String rowToCSVString(Row row) {
        return ((TraversableOnce)row.toSeq().map((Function1 & Serializable & scala.Serializable)x0$1 -> {
            Object object = x0$1;
            if (object == null) {
                return GreenplumCSVFormat$.MODULE$.VALUE_OF_NULL();
            }
            if (object instanceof String) {
                String string = (String)object;
                return new StringBuilder(0).append(GreenplumCSVFormat$.MODULE$.QUOTE()).append(string.replace(GreenplumCSVFormat$.MODULE$.QUOTE_STRING(), GreenplumCSVFormat$.MODULE$.ESCAPED_QUOTE())).append(GreenplumCSVFormat$.MODULE$.QUOTE()).toString();
            }
            return object;
        }, Seq$.MODULE$.canBuildFrom())).mkString(Character.toString(GreenplumCSVFormat$.MODULE$.CHAR_DELIMITER()));
    }

    private void processError(String path, HttpServletRequest request, HttpServletResponse response, int code, Throwable e) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isErrorEnabled()) {
            this.logger().underlying().error("Failed to handle {} request for {} : {}", request.getMethod(), path, e.toString());
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        response.setStatus(code);
        if (e != null && e.getMessage() != null) {
            response.getWriter().write(e.getMessage());
            return;
        }
    }

    public GpfdistHandler(Map<String, Try<TransactionData>> bufferMap, Map<String, PartitionData> sendBufferMap) {
        this.bufferMap = bufferMap;
        this.sendBufferMap = sendBufferMap;
        LazyLogging.$init$(this);
    }

    public GpfdistHandler() {
        this(new ConcurrentHashMap<String, Try<TransactionData>>(), new ConcurrentHashMap<String, PartitionData>());
    }
}

