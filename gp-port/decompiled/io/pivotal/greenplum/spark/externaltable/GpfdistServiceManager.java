/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.conf.ConnectorOptions;
import io.pivotal.greenplum.spark.externaltable.GpfdistService;
import io.pivotal.greenplum.spark.externaltable.GpfdistServiceManager$;
import io.pivotal.greenplum.spark.externaltable.ServiceKey;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001E<Qa\u0003\u0007\t\u0002]1Q!\u0007\u0007\t\u0002iAQaK\u0001\u0005\u00021Bq!L\u0001C\u0002\u0013%a\u0006\u0003\u0004O\u0003\u0001\u0006Ia\f\u0005\b\u001f\u0006\u0011\r\u0011\"\u0003Q\u0011\u0019)\u0016\u0001)A\u0005#\"9a+\u0001b\u0001\n\u00139\u0006BB0\u0002A\u0003%\u0001\fC\u0003a\u0003\u0011\u0005\u0011\rC\u0003k\u0003\u0011\u00051.A\u000bHa\u001a$\u0017n\u001d;TKJ4\u0018nY3NC:\fw-\u001a:\u000b\u00055q\u0011!D3yi\u0016\u0014h.\u00197uC\ndWM\u0003\u0002\u0010!\u0005)1\u000f]1sW*\u0011\u0011CE\u0001\nOJ,WM\u001c9mk6T!a\u0005\u000b\u0002\u000fALgo\u001c;bY*\tQ#\u0001\u0002j_\u000e\u0001\u0001C\u0001\r\u0002\u001b\u0005a!!F$qM\u0012L7\u000f^*feZL7-Z'b]\u0006<WM]\n\u0004\u0003m\t\u0003C\u0001\u000f \u001b\u0005i\"\"\u0001\u0010\u0002\u000bM\u001c\u0017\r\\1\n\u0005\u0001j\"AB!osJ+g\r\u0005\u0002#S5\t1E\u0003\u0002%K\u0005a1oY1mC2|wmZ5oO*\u0011aeJ\u0001\tif\u0004Xm]1gK*\t\u0001&A\u0002d_6L!AK\u0012\u0003\u00171\u000b'0\u001f'pO\u001eLgnZ\u0001\u0007y%t\u0017\u000e\u001e \u0015\u0003]\t\u0011BY;gM\u0016\u0014X*\u00199\u0016\u0003=\u0002B\u0001M\u001c:\t6\t\u0011G\u0003\u00023g\u0005Q1m\u001c8dkJ\u0014XM\u001c;\u000b\u0005Q*\u0014\u0001B;uS2T\u0011AN\u0001\u0005U\u00064\u0018-\u0003\u00029c\t\t2i\u001c8dkJ\u0014XM\u001c;ICNDW*\u00199\u0011\u0005i\neBA\u001e@!\taT$D\u0001>\u0015\tqd#\u0001\u0004=e>|GOP\u0005\u0003\u0001v\ta\u0001\u0015:fI\u00164\u0017B\u0001\"D\u0005\u0019\u0019FO]5oO*\u0011\u0001)\b\t\u0004\u000b\u001eKU\"\u0001$\u000b\u0005Qj\u0012B\u0001%G\u0005\r!&/\u001f\t\u0003\u00152k\u0011a\u0013\u0006\u0003i9I!!T&\u0003\u001fQ\u0013\u0018M\\:bGRLwN\u001c#bi\u0006\f!BY;gM\u0016\u0014X*\u00199!\u00035\u0019XM\u001c3Ck\u001a4WM]'baV\t\u0011\u000b\u0005\u00031oe\u0012\u0006C\u0001\rT\u0013\t!FBA\u0007QCJ$\u0018\u000e^5p]\u0012\u000bG/Y\u0001\u000fg\u0016tGMQ;gM\u0016\u0014X*\u00199!\u0003-\u0019XM\u001d<jG\u0016\u001cX*\u00199\u0016\u0003a\u0003B\u0001M\u001cZ9B\u0011\u0001DW\u0005\u000372\u0011!bU3sm&\u001cWmS3z!\tAR,\u0003\u0002_\u0019\tqq\t\u001d4eSN$8+\u001a:wS\u000e,\u0017\u0001D:feZL7-Z:NCB\u0004\u0013AC4fiN+'O^5dKR\u0011AL\u0019\u0005\u0006G&\u0001\r\u0001Z\u0001\u0011G>tg.Z2u_J|\u0005\u000f^5p]N\u0004\"!\u001a5\u000e\u0003\u0019T!a\u001a\b\u0002\t\r|gNZ\u0005\u0003S\u001a\u0014\u0001cQ8o]\u0016\u001cGo\u001c:PaRLwN\\:\u0002\u001bM$x\u000e]!oIJ+Wn\u001c<f)\taw\u000e\u0005\u0002\u001d[&\u0011a.\b\u0002\u0005+:LG\u000fC\u0003q\u0015\u0001\u0007\u0011,A\u0002lKf\u0004")
public final class GpfdistServiceManager {
    public static void stopAndRemove(ServiceKey serviceKey) {
        GpfdistServiceManager$.MODULE$.stopAndRemove(serviceKey);
    }

    public static GpfdistService getService(ConnectorOptions connectorOptions) {
        return GpfdistServiceManager$.MODULE$.getService(connectorOptions);
    }
}

