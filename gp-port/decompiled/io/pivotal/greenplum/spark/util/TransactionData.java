/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Predef$
 *  scala.Serializable
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.util;

import io.pivotal.greenplum.spark.util.LargeByteBuffer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import scala.Function0;
import scala.Predef$;
import scala.Serializable;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001Y3Aa\u0003\u0007\u0001/!)a\u0004\u0001C\u0001?!9!\u0005\u0001b\u0001\n\u0003\u0019\u0003BB\u0014\u0001A\u0003%A\u0005C\u0004)\u0001\u0001\u0007I\u0011B\u0015\t\u000f5\u0002\u0001\u0019!C\u0005]!1A\u0007\u0001Q!\n)Bq!\u000e\u0001C\u0002\u0013%a\u0007\u0003\u0004B\u0001\u0001\u0006Ia\u000e\u0005\u0006\u0005\u0002!\ta\u0011\u0005\u0006\u0013\u0002!\tA\u0013\u0002\u0010)J\fgn]1di&|g\u000eR1uC*\u0011QBD\u0001\u0005kRLGN\u0003\u0002\u0010!\u0005)1\u000f]1sW*\u0011\u0011CE\u0001\nOJ,WM\u001c9mk6T!a\u0005\u000b\u0002\u000fALgo\u001c;bY*\tQ#\u0001\u0002j_\u000e\u00011C\u0001\u0001\u0019!\tIB$D\u0001\u001b\u0015\u0005Y\u0012!B:dC2\f\u0017BA\u000f\u001b\u0005\u0019\te.\u001f*fM\u00061A(\u001b8jiz\"\u0012\u0001\t\t\u0003C\u0001i\u0011\u0001D\u0001\u000e\u000b:#ul\u0014$`'R\u0013V)Q'\u0016\u0003\u0011\u0002\"!G\u0013\n\u0005\u0019R\"aA%oi\u0006qQI\u0014#`\u001f\u001a{6\u000b\u0016*F\u00036\u0003\u0013AD:uCJ$X\r\u001a*fC\u0012LgnZ\u000b\u0002UA\u0011\u0011dK\u0005\u0003Yi\u0011qAQ8pY\u0016\fg.\u0001\nti\u0006\u0014H/\u001a3SK\u0006$\u0017N\\4`I\u0015\fHCA\u00183!\tI\u0002'\u0003\u000225\t!QK\\5u\u0011\u001d\u0019T!!AA\u0002)\n1\u0001\u001f\u00132\u0003=\u0019H/\u0019:uK\u0012\u0014V-\u00193j]\u001e\u0004\u0013!\u00032vM\u001a,'/T1q+\u00059\u0004\u0003\u0002\u001d=Iyj\u0011!\u000f\u0006\u0003\u001biR\u0011aO\u0001\u0005U\u00064\u0018-\u0003\u0002>s\t\u0019Q*\u00199\u0011\u0005\u0005z\u0014B\u0001!\r\u0005=a\u0015M]4f\u0005f$XMQ;gM\u0016\u0014\u0018A\u00032vM\u001a,'/T1qA\u0005qq-\u001a;J]B,Ho\u0015;sK\u0006lG#\u0001#\u0011\u0005\u0015;U\"\u0001$\u000b\u0005UQ\u0014B\u0001%G\u0005-Ie\u000e];u'R\u0014X-Y7\u0002\u000b]\u0014\u0018\u000e^3\u0015\t-\u0003&\u000b\u0016\t\u0004\u0019:{S\"A'\u000b\u00055Q\u0012BA(N\u0005\r!&/\u001f\u0005\u0006#*\u0001\r\u0001J\u0001\ng\u0016<W.\u001a8u\u0013\u0012DQa\u0015\u0006A\u0002\u0011\u000b1\"\u001b8qkR\u001cFO]3b[\")QK\u0003a\u0001I\u0005i1m\u001c8uK:$H*\u001a8hi\"\u0004")
public class TransactionData {
    private final int END_OF_STREAM;
    private boolean io$pivotal$greenplum$spark$util$TransactionData$$startedReading = false;
    private final Map<Object, LargeByteBuffer> io$pivotal$greenplum$spark$util$TransactionData$$bufferMap = new HashMap<Object, LargeByteBuffer>();

    public int END_OF_STREAM() {
        return this.END_OF_STREAM;
    }

    private boolean io$pivotal$greenplum$spark$util$TransactionData$$startedReading() {
        return this.io$pivotal$greenplum$spark$util$TransactionData$$startedReading;
    }

    public void io$pivotal$greenplum$spark$util$TransactionData$$startedReading_$eq(boolean x$1) {
        this.io$pivotal$greenplum$spark$util$TransactionData$$startedReading = x$1;
    }

    public Map<Object, LargeByteBuffer> io$pivotal$greenplum$spark$util$TransactionData$$bufferMap() {
        return this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap;
    }

    public InputStream getInputStream() {
        return new InputStream(this){
            private InputStream currStream;
            private final /* synthetic */ TransactionData $outer;

            private InputStream currStream() {
                return this.currStream;
            }

            private void currStream_$eq(InputStream x$1) {
                this.currStream = x$1;
            }

            public int read() {
                int value;
                if (this.currStream() == null) {
                    this.currStream_$eq(this.nextStream());
                    if (this.currStream() == null) {
                        return this.$outer.END_OF_STREAM();
                    }
                }
                if ((value = this.currStream().read()) == this.$outer.END_OF_STREAM()) {
                    this.currStream_$eq(this.nextStream());
                    if (this.currStream() == null) {
                        return this.$outer.END_OF_STREAM();
                    }
                    return this.currStream().read();
                }
                return value;
            }

            public void close() {
                this.$outer.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().clear();
                this.currStream_$eq(null);
            }

            private InputStream nextStream() {
                if (this.currStream() != null) {
                    this.currStream().close();
                }
                if (!this.$outer.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().isEmpty()) {
                    return this.$outer.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().remove(this.$outer.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().keySet().iterator().next()).getInputStream();
                }
                return null;
            }
            {
                if ($outer == null) {
                    throw null;
                }
                this.$outer = $outer;
                $outer.io$pivotal$greenplum$spark$util$TransactionData$$startedReading_$eq(true);
            }
        };
    }

    public Try<BoxedUnit> write(int segmentId, InputStream inputStream, int contentLength) {
        BoxedUnit boxedUnit;
        Predef$.MODULE$.require(!this.io$pivotal$greenplum$spark$util$TransactionData$$startedReading(), (Function0 & java.io.Serializable & Serializable)() -> "Cannot write to TransactionData after reading");
        LargeByteBuffer partitionBuffer = null;
        if (!this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().containsKey(BoxesRunTime.boxToInteger((int)segmentId))) {
            Map<Object, LargeByteBuffer> map = this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap();
            synchronized (map) {
                Object object;
                if (!this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().containsKey(BoxesRunTime.boxToInteger((int)segmentId))) {
                    partitionBuffer = new LargeByteBuffer();
                    object = this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().put(BoxesRunTime.boxToInteger((int)segmentId), partitionBuffer);
                } else {
                    partitionBuffer = this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().get(BoxesRunTime.boxToInteger((int)segmentId));
                    object = BoxedUnit.UNIT;
                }
                BoxedUnit boxedUnit2 = object;
                // MONITOREXIT @DISABLED, blocks:[0, 1, 6] lbl14 : MonitorExitStatement: MONITOREXIT : var5_5
                boxedUnit = boxedUnit2;
            }
        } else {
            partitionBuffer = this.io$pivotal$greenplum$spark$util$TransactionData$$bufferMap().get(BoxesRunTime.boxToInteger((int)segmentId));
            boxedUnit = BoxedUnit.UNIT;
        }
        return partitionBuffer.write(inputStream, contentLength);
    }

    public TransactionData() {
        this.END_OF_STREAM = -1;
    }
}

