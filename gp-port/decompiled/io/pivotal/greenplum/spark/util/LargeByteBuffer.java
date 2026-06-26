/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.mutable.ArrayOps$ofByte
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.java8.JFunction0$mcV$sp
 *  scala.util.Try
 *  scala.util.Try$
 */
package io.pivotal.greenplum.spark.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import scala.Function0;
import scala.Predef$;
import scala.Serializable;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.java8.JFunction0;
import scala.util.Try;
import scala.util.Try$;

@ScalaSignature(bytes="\u0006\u0001\u00153AAB\u0004\u0001%!)\u0011\u0004\u0001C\u00015!9Q\u0004\u0001b\u0001\n\u0013q\u0002B\u0002\u0017\u0001A\u0003%q\u0004C\u0003.\u0001\u0011\u0005a\u0006C\u00035\u0001\u0011\u0005QGA\bMCJ<WMQ=uK\n+hMZ3s\u0015\tA\u0011\"\u0001\u0003vi&d'B\u0001\u0006\f\u0003\u0015\u0019\b/\u0019:l\u0015\taQ\"A\u0005he\u0016,g\u000e\u001d7v[*\u0011abD\u0001\ba&4x\u000e^1m\u0015\u0005\u0001\u0012AA5p\u0007\u0001\u0019\"\u0001A\n\u0011\u0005Q9R\"A\u000b\u000b\u0003Y\tQa]2bY\u0006L!\u0001G\u000b\u0003\r\u0005s\u0017PU3g\u0003\u0019a\u0014N\\5u}Q\t1\u0004\u0005\u0002\u001d\u00015\tq!\u0001\u0006ck\u001a4WM\u001d'jgR,\u0012a\b\t\u0004A\u00112S\"A\u0011\u000b\u0005!\u0011#\"A\u0012\u0002\t)\fg/Y\u0005\u0003K\u0005\u0012A\u0001T5tiB\u0019AcJ\u0015\n\u0005!*\"!B!se\u0006L\bC\u0001\u000b+\u0013\tYSC\u0001\u0003CsR,\u0017a\u00032vM\u001a,'\u000fT5ti\u0002\nabZ3u\u0013:\u0004X\u000f^*ue\u0016\fW\u000eF\u00010!\t\u0001$'D\u00012\u0015\t\u0001\"%\u0003\u00024c\tY\u0011J\u001c9viN#(/Z1n\u0003\u00159(/\u001b;f)\r1d\b\u0011\t\u0004oeZT\"\u0001\u001d\u000b\u0005!)\u0012B\u0001\u001e9\u0005\r!&/\u001f\t\u0003)qJ!!P\u000b\u0003\tUs\u0017\u000e\u001e\u0005\u0006\u007f\u0015\u0001\raL\u0001\fS:\u0004X\u000f^*ue\u0016\fW\u000eC\u0003B\u000b\u0001\u0007!)\u0001\u0003tSj,\u0007C\u0001\u000bD\u0013\t!UCA\u0002J]R\u0004")
public class LargeByteBuffer {
    private final List<byte[]> io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList = new ArrayList<byte[]>();

    public List<byte[]> io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList() {
        return this.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList;
    }

    public InputStream getInputStream() {
        return new InputStream(this){
            private int pos;
            private int listPos;
            private byte[] currArray;
            private final /* synthetic */ LargeByteBuffer $outer;

            private int pos() {
                return this.pos;
            }

            private void pos_$eq(int x$1) {
                this.pos = x$1;
            }

            private int listPos() {
                return this.listPos;
            }

            private void listPos_$eq(int x$1) {
                this.listPos = x$1;
            }

            private byte[] currArray() {
                return this.currArray;
            }

            private void currArray_$eq(byte[] x$1) {
                this.currArray = x$1;
            }

            public int read() {
                byte value;
                block2: {
                    if (this.listPos() == -1) {
                        this.currArray_$eq(this.nextArray());
                    }
                    if (this.currArray() == null) {
                        return -1;
                    }
                    value = this.currArray()[this.pos()];
                    this.pos_$eq(this.pos() + 1);
                    if (this.pos() < new ArrayOps.ofByte(Predef$.MODULE$.byteArrayOps(this.currArray())).size()) break block2;
                    this.currArray_$eq(this.nextArray());
                    this.pos_$eq(0);
                }
                return value;
            }

            public void close() {
                this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().clear();
            }

            private byte[] nextArray() {
                do {
                    Object object = this.listPos() >= 0 && this.listPos() < this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().size() ? this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().set(this.listPos(), null) : BoxedUnit.UNIT;
                    this.listPos_$eq(this.listPos() + 1);
                } while (this.listPos() < this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().size() && this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().get(this.listPos()).length == 0);
                if (this.listPos() < this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().size()) {
                    return this.$outer.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().get(this.listPos());
                }
                return null;
            }
            {
                if ($outer == null) {
                    throw null;
                }
                this.$outer = $outer;
                this.pos = 0;
                this.listPos = -1;
            }
        };
    }

    public Try<BoxedUnit> write(InputStream inputStream, int size) {
        return Try$.MODULE$.apply((Function0)(JFunction0.mcV.sp & java.io.Serializable & Serializable)() -> {
            byte[] buffer = new byte[size];
            int bytesCopied = IOUtils.read(inputStream, buffer);
            if (bytesCopied != size) {
                throw new IllegalStateException(new StringBuilder(46).append("Expected to read ").append(size).append(" bytes from stream, but read ").append(bytesCopied).toString());
            }
            if (inputStream.read() != -1) {
                throw new IllegalStateException(new StringBuilder(65).append("Expected to read ").append(size).append(" bytes from stream, but the stream has more data").toString());
            }
            this.io$pivotal$greenplum$spark$util$LargeByteBuffer$$bufferList().add(buffer);
        });
    }
}

