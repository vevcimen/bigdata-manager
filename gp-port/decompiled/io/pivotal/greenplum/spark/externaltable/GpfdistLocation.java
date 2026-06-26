/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.Predef$
 *  scala.Product
 *  scala.Serializable
 *  scala.Tuple4
 *  scala.collection.Iterator
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 *  scala.runtime.Statics
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.GpfdistLocation$;
import scala.Option;
import scala.Predef$;
import scala.Product;
import scala.Serializable;
import scala.Tuple4;
import scala.collection.Iterator;
import scala.collection.immutable.StringOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;
import scala.runtime.Statics;

@ScalaSignature(bytes="\u0006\u0001\u0005ed\u0001B\u0011#\u00016B\u0001B\u000f\u0001\u0003\u0016\u0004%\ta\u000f\u0005\t\u000f\u0002\u0011\t\u0012)A\u0005y!A\u0001\n\u0001BK\u0002\u0013\u00051\b\u0003\u0005J\u0001\tE\t\u0015!\u0003=\u0011!Q\u0005A!f\u0001\n\u0003Y\u0005\u0002C(\u0001\u0005#\u0005\u000b\u0011\u0002'\t\u0011A\u0003!Q3A\u0005\u0002mB\u0001\"\u0015\u0001\u0003\u0012\u0003\u0006I\u0001\u0010\u0005\u0006%\u0002!\ta\u0015\u0005\u00065\u0002!\ta\u0017\u0005\u0006;\u0002!\ta\u000f\u0005\b=\u0002\t\t\u0011\"\u0001`\u0011\u001d!\u0007!%A\u0005\u0002\u0015Dq\u0001\u001d\u0001\u0012\u0002\u0013\u0005Q\rC\u0004r\u0001E\u0005I\u0011\u0001:\t\u000fQ\u0004\u0011\u0013!C\u0001K\"9Q\u000fAA\u0001\n\u00032\bb\u0002@\u0001\u0003\u0003%\ta\u0013\u0005\t\u007f\u0002\t\t\u0011\"\u0001\u0002\u0002!I\u0011Q\u0002\u0001\u0002\u0002\u0013\u0005\u0013q\u0002\u0005\n\u0003;\u0001\u0011\u0011!C\u0001\u0003?A\u0011\"!\u000b\u0001\u0003\u0003%\t%a\u000b\t\u0013\u00055\u0002!!A\u0005B\u0005=\u0002\"CA\u0019\u0001\u0005\u0005I\u0011IA\u001a\u000f\u001d\t9D\tE\u0001\u0003s1a!\t\u0012\t\u0002\u0005m\u0002B\u0002*\u001b\t\u0003\ti\u0004C\u0004\u0002@i!\t!!\u0011\t\u000f\u0005}\"\u0004\"\u0001\u0002H!I\u0011q\b\u000e\u0002\u0002\u0013\u0005\u0015\u0011\u000b\u0005\n\u00037R\u0012\u0011!CA\u0003;B\u0011\"a\u001c\u001b\u0003\u0003%I!!\u001d\u0003\u001f\u001d\u0003h\rZ5ti2{7-\u0019;j_:T!a\t\u0013\u0002\u001b\u0015DH/\u001a:oC2$\u0018M\u00197f\u0015\t)c%A\u0003ta\u0006\u00148N\u0003\u0002(Q\u0005IqM]3f]BdW/\u001c\u0006\u0003S)\nq\u0001]5w_R\fGNC\u0001,\u0003\tIwn\u0001\u0001\u0014\t\u0001qCg\u000e\t\u0003_Ij\u0011\u0001\r\u0006\u0002c\u0005)1oY1mC&\u00111\u0007\r\u0002\u0007\u0003:L(+\u001a4\u0011\u0005=*\u0014B\u0001\u001c1\u0005\u001d\u0001&o\u001c3vGR\u0004\"a\f\u001d\n\u0005e\u0002$\u0001D*fe&\fG.\u001b>bE2,\u0017\u0001\u00039s_R|7m\u001c7\u0016\u0003q\u0002\"!\u0010#\u000f\u0005y\u0012\u0005CA 1\u001b\u0005\u0001%BA!-\u0003\u0019a$o\\8u}%\u00111\tM\u0001\u0007!J,G-\u001a4\n\u0005\u00153%AB*ue&twM\u0003\u0002Da\u0005I\u0001O]8u_\u000e|G\u000eI\u0001\u0005Q>\u001cH/A\u0003i_N$\b%\u0001\u0003q_J$X#\u0001'\u0011\u0005=j\u0015B\u0001(1\u0005\rIe\u000e^\u0001\u0006a>\u0014H\u000fI\u0001\u0005a\u0006$\b.A\u0003qCRD\u0007%\u0001\u0004=S:LGO\u0010\u000b\u0006)Z;\u0006,\u0017\t\u0003+\u0002i\u0011A\t\u0005\u0006u%\u0001\r\u0001\u0010\u0005\u0006\u0011&\u0001\r\u0001\u0010\u0005\u0006\u0015&\u0001\r\u0001\u0014\u0005\u0006!&\u0001\r\u0001P\u0001\to&$\b\u000eU1uQR\u0011A\u000b\u0018\u0005\u0006!*\u0001\r\u0001P\u0001\u0007O\u0016$XK\u001d7\u0002\t\r|\u0007/\u001f\u000b\u0006)\u0002\f'm\u0019\u0005\bu1\u0001\n\u00111\u0001=\u0011\u001dAE\u0002%AA\u0002qBqA\u0013\u0007\u0011\u0002\u0003\u0007A\nC\u0004Q\u0019A\u0005\t\u0019\u0001\u001f\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%cU\taM\u000b\u0002=O.\n\u0001\u000e\u0005\u0002j]6\t!N\u0003\u0002lY\u0006IQO\\2iK\u000e\\W\r\u001a\u0006\u0003[B\n!\"\u00198o_R\fG/[8o\u0013\ty'NA\tv]\u000eDWmY6fIZ\u000b'/[1oG\u0016\fabY8qs\u0012\"WMZ1vYR$#'\u0001\bd_BLH\u0005Z3gCVdG\u000fJ\u001a\u0016\u0003MT#\u0001T4\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%i\u0005i\u0001O]8ek\u000e$\bK]3gSb,\u0012a\u001e\t\u0003qvl\u0011!\u001f\u0006\u0003un\fA\u0001\\1oO*\tA0\u0001\u0003kCZ\f\u0017BA#z\u00031\u0001(o\u001c3vGR\f%/\u001b;z\u00039\u0001(o\u001c3vGR,E.Z7f]R$B!a\u0001\u0002\nA\u0019q&!\u0002\n\u0007\u0005\u001d\u0001GA\u0002B]fD\u0001\"a\u0003\u0014\u0003\u0003\u0005\r\u0001T\u0001\u0004q\u0012\n\u0014a\u00049s_\u0012,8\r^%uKJ\fGo\u001c:\u0016\u0005\u0005E\u0001CBA\n\u00033\t\u0019!\u0004\u0002\u0002\u0016)\u0019\u0011q\u0003\u0019\u0002\u0015\r|G\u000e\\3di&|g.\u0003\u0003\u0002\u001c\u0005U!\u0001C%uKJ\fGo\u001c:\u0002\u0011\r\fg.R9vC2$B!!\t\u0002(A\u0019q&a\t\n\u0007\u0005\u0015\u0002GA\u0004C_>dW-\u00198\t\u0013\u0005-Q#!AA\u0002\u0005\r\u0011\u0001\u00035bg\"\u001cu\u000eZ3\u0015\u00031\u000b\u0001\u0002^8TiJLgn\u001a\u000b\u0002o\u00061Q-];bYN$B!!\t\u00026!I\u00111\u0002\r\u0002\u0002\u0003\u0007\u00111A\u0001\u0010\u000fB4G-[:u\u0019>\u001c\u0017\r^5p]B\u0011QKG\n\u000459:DCAA\u001d\u0003\u0015\t\u0007\u000f\u001d7z)\u0015!\u00161IA#\u0011\u0015AE\u00041\u0001=\u0011\u0015QE\u00041\u0001M)\u001d!\u0016\u0011JA&\u0003\u001bBQ\u0001S\u000fA\u0002qBQAS\u000fA\u00021Cq!a\u0014\u001e\u0001\u0004\t\t#\u0001\u0004vg\u0016\u001c6\u000f\u001c\u000b\n)\u0006M\u0013QKA,\u00033BQA\u000f\u0010A\u0002qBQ\u0001\u0013\u0010A\u0002qBQA\u0013\u0010A\u00021CQ\u0001\u0015\u0010A\u0002q\nq!\u001e8baBd\u0017\u0010\u0006\u0003\u0002`\u0005-\u0004#B\u0018\u0002b\u0005\u0015\u0014bAA2a\t1q\n\u001d;j_:\u0004raLA4yqbE(C\u0002\u0002jA\u0012a\u0001V;qY\u0016$\u0004\u0002CA7?\u0005\u0005\t\u0019\u0001+\u0002\u0007a$\u0003'A\u0006sK\u0006$'+Z:pYZ,GCAA:!\rA\u0018QO\u0005\u0004\u0003oJ(AB(cU\u0016\u001cG\u000f")
public class GpfdistLocation
implements Product,
Serializable {
    private final String protocol;
    private final String host;
    private final int port;
    private final String path;

    public static Option<Tuple4<String, String, Object, String>> unapply(GpfdistLocation gpfdistLocation) {
        return GpfdistLocation$.MODULE$.unapply(gpfdistLocation);
    }

    public static GpfdistLocation apply(String string, String string2, int n, String string3) {
        return GpfdistLocation$.MODULE$.apply(string, string2, n, string3);
    }

    public static GpfdistLocation apply(String string, int n, boolean bl) {
        return GpfdistLocation$.MODULE$.apply(string, n, bl);
    }

    public static GpfdistLocation apply(String string, int n) {
        return GpfdistLocation$.MODULE$.apply(string, n);
    }

    public String protocol() {
        return this.protocol;
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public String path() {
        return this.path;
    }

    public GpfdistLocation withPath(String path) {
        String x$1 = path;
        String x$2 = this.copy$default$1();
        String x$3 = this.copy$default$2();
        int x$4 = this.copy$default$3();
        return this.copy(x$2, x$3, x$4, x$1);
    }

    public String getUrl() {
        String normalizedPath = new StringOps(Predef$.MODULE$.augmentString(new StringOps(Predef$.MODULE$.augmentString(this.path())).stripPrefix("/"))).stripSuffix("/");
        return new StringBuilder(5).append(this.protocol()).append("://").append(this.host()).append(":").append(this.port()).append("/").append(normalizedPath).toString();
    }

    public GpfdistLocation copy(String protocol, String host, int port, String path) {
        return new GpfdistLocation(protocol, host, port, path);
    }

    public String copy$default$1() {
        return this.protocol();
    }

    public String copy$default$2() {
        return this.host();
    }

    public int copy$default$3() {
        return this.port();
    }

    public String copy$default$4() {
        return this.path();
    }

    public String productPrefix() {
        return "GpfdistLocation";
    }

    public int productArity() {
        return 4;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.protocol();
            }
            case 1: {
                return this.host();
            }
            case 2: {
                return BoxesRunTime.boxToInteger((int)this.port());
            }
            case 3: {
                return this.path();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof GpfdistLocation;
    }

    public int hashCode() {
        int n = -889275714;
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.protocol()));
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.host()));
        n = Statics.mix((int)n, (int)this.port());
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.path()));
        return Statics.finalizeHash((int)n, (int)4);
    }

    public String toString() {
        return ScalaRunTime$.MODULE$._toString((Product)this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object x$1) {
        if (this == x$1) return true;
        Object object = x$1;
        if (!(object instanceof GpfdistLocation)) return false;
        boolean bl = true;
        if (!bl) return false;
        GpfdistLocation gpfdistLocation = (GpfdistLocation)x$1;
        String string = this.protocol();
        String string2 = gpfdistLocation.protocol();
        if (string == null) {
            if (string2 != null) {
                return false;
            }
        } else if (!string.equals(string2)) return false;
        String string3 = this.host();
        String string4 = gpfdistLocation.host();
        if (string3 == null) {
            if (string4 != null) {
                return false;
            }
        } else if (!string3.equals(string4)) return false;
        if (this.port() != gpfdistLocation.port()) return false;
        String string5 = this.path();
        String string6 = gpfdistLocation.path();
        if (string5 == null) {
            if (string6 != null) {
                return false;
            }
        } else if (!string5.equals(string6)) return false;
        if (!gpfdistLocation.canEqual(this)) return false;
        return true;
    }

    public GpfdistLocation(String protocol, String host, int port, String path) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        Product.$init$((Product)this);
    }
}

