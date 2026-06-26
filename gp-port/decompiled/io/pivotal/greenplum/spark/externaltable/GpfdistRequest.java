/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.GpfdistRequest$;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import scala.reflect.ScalaSignature;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001q4A\u0001G\r\u0001I!A1\u0006\u0001BC\u0002\u0013\u0005A\u0006\u0003\u00059\u0001\t\u0005\t\u0015!\u0003.\u0011!I\u0004A!b\u0001\n\u0003Q\u0004\u0002\u0003 \u0001\u0005\u0003\u0005\u000b\u0011B\u001e\t\u0011}\u0002!Q1A\u0005\u00021B\u0001\u0002\u0011\u0001\u0003\u0002\u0003\u0006I!\f\u0005\t\u0003\u0002\u0011\t\u0011)A\u0005\u0005\")A\n\u0001C\u0001\u001b\")A\u000b\u0001C\u0001Y!)Q\u000b\u0001C\u0001u!)a\u000b\u0001C\u0001/\"9A\f\u0001b\u0001\n\u0003a\u0003BB/\u0001A\u0003%QfB\u0003_3!\u0005qLB\u0003\u00193!\u0005\u0001\rC\u0003M\u001f\u0011\u00051\u000eC\u0004m\u001f\t\u0007I\u0011\u0001\u0017\t\r5|\u0001\u0015!\u0003.\u0011\u001dqwB1A\u0005\u00021Baa\\\b!\u0002\u0013i\u0003b\u00029\u0010\u0005\u0004%\t\u0001\f\u0005\u0007c>\u0001\u000b\u0011B\u0017\t\u000bI|A\u0011A:\u0003\u001d\u001d\u0003h\rZ5tiJ+\u0017/^3ti*\u0011!dG\u0001\u000eKb$XM\u001d8bYR\f'\r\\3\u000b\u0005qi\u0012!B:qCJ\\'B\u0001\u0010 \u0003%9'/Z3oa2,XN\u0003\u0002!C\u00059\u0001/\u001b<pi\u0006d'\"\u0001\u0012\u0002\u0005%|7\u0001A\n\u0003\u0001\u0015\u0002\"AJ\u0015\u000e\u0003\u001dR\u0011\u0001K\u0001\u0006g\u000e\fG.Y\u0005\u0003U\u001d\u0012a!\u00118z%\u00164\u0017!\u0004;sC:\u001c\u0018m\u0019;j_:LE-F\u0001.!\tqSG\u0004\u00020gA\u0011\u0001gJ\u0007\u0002c)\u0011!gI\u0001\u0007yI|w\u000e\u001e \n\u0005Q:\u0013A\u0002)sK\u0012,g-\u0003\u00027o\t11\u000b\u001e:j]\u001eT!\u0001N\u0014\u0002\u001dQ\u0014\u0018M\\:bGRLwN\\%eA\u0005I1/Z4nK:$\u0018\nZ\u000b\u0002wA\u0011a\u0005P\u0005\u0003{\u001d\u00121!\u00138u\u0003)\u0019XmZ7f]RLE\rI\u0001\u0005a\u0006$\b.A\u0003qCRD\u0007%A\u0004sKF,Xm\u001d;\u0011\u0005\rSU\"\u0001#\u000b\u0005\u00153\u0015\u0001\u00025uiBT!a\u0012%\u0002\u000fM,'O\u001e7fi*\t\u0011*A\u0003kCZ\f\u00070\u0003\u0002L\t\n\u0011\u0002\n\u001e;q'\u0016\u0014h\u000f\\3u%\u0016\fX/Z:u\u0003\u0019a\u0014N\\5u}Q)a\nU)S'B\u0011q\nA\u0007\u00023!)1\u0006\u0003a\u0001[!)\u0011\b\u0003a\u0001w!)q\b\u0003a\u0001[!)\u0011\t\u0003a\u0001\u0005\u0006Y!/Z9vKN$H+\u001f9f\u00035\u0019wN\u001c;f]RdUM\\4uQ\u0006Y\u0011N\u001c9viN#(/Z1n+\u0005A\u0006CA-[\u001b\u00051\u0015BA.G\u0005I\u0019VM\u001d<mKRLe\u000e];u'R\u0014X-Y7\u0002\u0015%$WM\u001c;jM&,'/A\u0006jI\u0016tG/\u001b4jKJ\u0004\u0013AD$qM\u0012L7\u000f\u001e*fcV,7\u000f\u001e\t\u0003\u001f>\u00192aD\u0013b!\t\u0011\u0017.D\u0001d\u0015\t!W-\u0001\u0007tG\u0006d\u0017\r\\8hO&twM\u0003\u0002gO\u0006AA/\u001f9fg\u00064WMC\u0001i\u0003\r\u0019w.\\\u0005\u0003U\u000e\u00141\u0002T1{s2{wmZ5oOR\tq,A\u0011E\u0013N#&+\u0013\"V)\u0016#u\f\u0016*B\u001dN\u000b5\tV%P\u001d~KEi\u0018%F\u0003\u0012+%+\u0001\u0012E\u0013N#&+\u0013\"V)\u0016#u\f\u0016*B\u001dN\u000b5\tV%P\u001d~KEi\u0018%F\u0003\u0012+%\u000bI\u0001\u0012'\u0016;U*\u0012(U?&#u\fS#B\t\u0016\u0013\u0016AE*F\u000f6+e\nV0J\t~CU)\u0011#F%\u0002\nAcU#H\u001b\u0016sEkX\"P+:#v\fS#B\t\u0016\u0013\u0016!F*F\u000f6+e\nV0D\u001fVsEk\u0018%F\u0003\u0012+%\u000bI\u0001\u0006a\u0006\u00148/\u001a\u000b\u0004ij\\\bcA;y\u001d6\taO\u0003\u0002xO\u0005!Q\u000f^5m\u0013\tIhOA\u0002UefDQ!Q\fA\u0002\tCQaP\fA\u00025\u0002")
public class GpfdistRequest {
    private final String transactionId;
    private final int segmentId;
    private final String path;
    private final HttpServletRequest request;
    private final String identifier;

    public static Try<GpfdistRequest> parse(HttpServletRequest httpServletRequest, String string) {
        return GpfdistRequest$.MODULE$.parse(httpServletRequest, string);
    }

    public static String SEGMENT_COUNT_HEADER() {
        return GpfdistRequest$.MODULE$.SEGMENT_COUNT_HEADER();
    }

    public static String SEGMENT_ID_HEADER() {
        return GpfdistRequest$.MODULE$.SEGMENT_ID_HEADER();
    }

    public static String DISTRIBUTED_TRANSACTION_ID_HEADER() {
        return GpfdistRequest$.MODULE$.DISTRIBUTED_TRANSACTION_ID_HEADER();
    }

    public String transactionId() {
        return this.transactionId;
    }

    public int segmentId() {
        return this.segmentId;
    }

    public String path() {
        return this.path;
    }

    public String requestType() {
        return this.request.getMethod();
    }

    public int contentLength() {
        return this.request.getContentLength();
    }

    public ServletInputStream inputStream() {
        return this.request.getInputStream();
    }

    public String identifier() {
        return this.identifier;
    }

    public GpfdistRequest(String transactionId, int segmentId, String path, HttpServletRequest request) {
        this.transactionId = transactionId;
        this.segmentId = segmentId;
        this.path = path;
        this.request = request;
        this.identifier = new StringBuilder(5).append("seg").append(segmentId).append(",").append(transactionId).append(",").append(path).toString();
    }
}

