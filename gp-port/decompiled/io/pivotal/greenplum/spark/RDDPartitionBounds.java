/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.Iterator
 *  scala.math.BigDecimal
 *  scala.reflect.ScalaSignature
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.RDDPartitionBounds$;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.math.BigDecimal;
import scala.reflect.ScalaSignature;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u0005ec\u0001B\r\u001b\u0001\u000eB\u0001\u0002\r\u0001\u0003\u0016\u0004%\t!\r\u0005\t}\u0001\u0011\t\u0012)A\u0005e!Aq\b\u0001BK\u0002\u0013\u0005\u0011\u0007\u0003\u0005A\u0001\tE\t\u0015!\u00033\u0011\u0015\t\u0005\u0001\"\u0001C\u0011\u001d9\u0005!!A\u0005\u0002!Cqa\u0013\u0001\u0012\u0002\u0013\u0005A\nC\u0004X\u0001E\u0005I\u0011\u0001'\t\u000fa\u0003\u0011\u0011!C!3\"9!\rAA\u0001\n\u0003\u0019\u0007bB4\u0001\u0003\u0003%\t\u0001\u001b\u0005\b]\u0002\t\t\u0011\"\u0011p\u0011\u001d1\b!!A\u0005\u0002]Dq\u0001 \u0001\u0002\u0002\u0013\u0005S\u0010C\u0004\u007f\u0001\u0005\u0005I\u0011I@\t\u0013\u0005\u0005\u0001!!A\u0005B\u0005\rqaBA\u00045!\u0005\u0011\u0011\u0002\u0004\u00073iA\t!a\u0003\t\r\u0005\u0013B\u0011AA\u0007\u0011\u001d\tyA\u0005C\u0001\u0003#Aq!a\u0004\u0013\t\u0003\t\t\u0003C\u0005\u0002\u0010I\t\t\u0011\"!\u00026!I\u00111\b\n\u0002\u0002\u0013\u0005\u0015Q\b\u0005\n\u0003\u001f\u0012\u0012\u0011!C\u0005\u0003#\u0012!C\u0015#E!\u0006\u0014H/\u001b;j_:\u0014u.\u001e8eg*\u00111\u0004H\u0001\u0006gB\f'o\u001b\u0006\u0003;y\t\u0011b\u001a:fK:\u0004H.^7\u000b\u0005}\u0001\u0013a\u00029jm>$\u0018\r\u001c\u0006\u0002C\u0005\u0011\u0011n\\\u0002\u0001'\u0011\u0001AEK\u0017\u0011\u0005\u0015BS\"\u0001\u0014\u000b\u0003\u001d\nQa]2bY\u0006L!!\u000b\u0014\u0003\r\u0005s\u0017PU3g!\t)3&\u0003\u0002-M\t9\u0001K]8ek\u000e$\bCA\u0013/\u0013\tycE\u0001\u0007TKJL\u0017\r\\5{C\ndW-A\u0003m_^,'/F\u00013!\t\u00194H\u0004\u00025s9\u0011Q\u0007O\u0007\u0002m)\u0011qGI\u0001\u0007yI|w\u000e\u001e \n\u0003\u001dJ!A\u000f\u0014\u0002\u000fA\f7m[1hK&\u0011A(\u0010\u0002\u000b\u0005&<G)Z2j[\u0006d'B\u0001\u001e'\u0003\u0019awn^3sA\u0005)Q\u000f\u001d9fe\u00061Q\u000f\u001d9fe\u0002\na\u0001P5oSRtDcA\"F\rB\u0011A\tA\u0007\u00025!)\u0001'\u0002a\u0001e!)q(\u0002a\u0001e\u0005!1m\u001c9z)\r\u0019\u0015J\u0013\u0005\ba\u0019\u0001\n\u00111\u00013\u0011\u001dyd\u0001%AA\u0002I\nabY8qs\u0012\"WMZ1vYR$\u0013'F\u0001NU\t\u0011djK\u0001P!\t\u0001V+D\u0001R\u0015\t\u00116+A\u0005v]\u000eDWmY6fI*\u0011AKJ\u0001\u000bC:tw\u000e^1uS>t\u0017B\u0001,R\u0005E)hn\u00195fG.,GMV1sS\u0006t7-Z\u0001\u000fG>\u0004\u0018\u0010\n3fM\u0006,H\u000e\u001e\u00133\u00035\u0001(o\u001c3vGR\u0004&/\u001a4jqV\t!\f\u0005\u0002\\A6\tAL\u0003\u0002^=\u0006!A.\u00198h\u0015\u0005y\u0016\u0001\u00026bm\u0006L!!\u0019/\u0003\rM#(/\u001b8h\u00031\u0001(o\u001c3vGR\f%/\u001b;z+\u0005!\u0007CA\u0013f\u0013\t1gEA\u0002J]R\fa\u0002\u001d:pIV\u001cG/\u00127f[\u0016tG\u000f\u0006\u0002jYB\u0011QE[\u0005\u0003W\u001a\u00121!\u00118z\u0011\u001di7\"!AA\u0002\u0011\f1\u0001\u001f\u00132\u0003=\u0001(o\u001c3vGRLE/\u001a:bi>\u0014X#\u00019\u0011\u0007E$\u0018.D\u0001s\u0015\t\u0019h%\u0001\u0006d_2dWm\u0019;j_:L!!\u001e:\u0003\u0011%#XM]1u_J\f\u0001bY1o\u000bF,\u0018\r\u001c\u000b\u0003qn\u0004\"!J=\n\u0005i4#a\u0002\"p_2,\u0017M\u001c\u0005\b[6\t\t\u00111\u0001j\u0003!A\u0017m\u001d5D_\u0012,G#\u00013\u0002\u0011Q|7\u000b\u001e:j]\u001e$\u0012AW\u0001\u0007KF,\u0018\r\\:\u0015\u0007a\f)\u0001C\u0004n!\u0005\u0005\t\u0019A5\u0002%I#E\tU1si&$\u0018n\u001c8C_VtGm\u001d\t\u0003\tJ\u00192A\u0005\u0013.)\t\tI!A\u0003baBd\u0017\u0010F\u0003D\u0003'\ti\u0002C\u0004\u0002\u0016Q\u0001\r!a\u0006\u0002\u00075Lg\u000eE\u0002&\u00033I1!a\u0007'\u0005\u0011auN\\4\t\u000f\u0005}A\u00031\u0001\u0002\u0018\u0005\u0019Q.\u0019=\u0015\u000b\r\u000b\u0019#a\r\t\u000f\u0005UQ\u00031\u0001\u0002&A!\u0011qEA\u0018\u001d\u0011\tI#a\u000b\u0011\u0005U2\u0013bAA\u0017M\u00051\u0001K]3eK\u001aL1!YA\u0019\u0015\r\tiC\n\u0005\b\u0003?)\u0002\u0019AA\u0013)\u0015\u0019\u0015qGA\u001d\u0011\u0015\u0001d\u00031\u00013\u0011\u0015yd\u00031\u00013\u0003\u001d)h.\u00199qYf$B!a\u0010\u0002LA)Q%!\u0011\u0002F%\u0019\u00111\t\u0014\u0003\r=\u0003H/[8o!\u0015)\u0013q\t\u001a3\u0013\r\tIE\n\u0002\u0007)V\u0004H.\u001a\u001a\t\u0011\u00055s#!AA\u0002\r\u000b1\u0001\u001f\u00131\u0003-\u0011X-\u00193SKN|GN^3\u0015\u0005\u0005M\u0003cA.\u0002V%\u0019\u0011q\u000b/\u0003\r=\u0013'.Z2u\u0001")
public class RDDPartitionBounds
implements Product,
Serializable {
    private final BigDecimal lower;
    private final BigDecimal upper;

    public static Option<Tuple2<BigDecimal, BigDecimal>> unapply(RDDPartitionBounds rDDPartitionBounds) {
        return RDDPartitionBounds$.MODULE$.unapply(rDDPartitionBounds);
    }

    public static RDDPartitionBounds apply(BigDecimal bigDecimal, BigDecimal bigDecimal2) {
        return RDDPartitionBounds$.MODULE$.apply(bigDecimal, bigDecimal2);
    }

    public static RDDPartitionBounds apply(String string, String string2) {
        return RDDPartitionBounds$.MODULE$.apply(string, string2);
    }

    public static RDDPartitionBounds apply(long l, long l2) {
        return RDDPartitionBounds$.MODULE$.apply(l, l2);
    }

    public BigDecimal lower() {
        return this.lower;
    }

    public BigDecimal upper() {
        return this.upper;
    }

    public RDDPartitionBounds copy(BigDecimal lower, BigDecimal upper) {
        return new RDDPartitionBounds(lower, upper);
    }

    public BigDecimal copy$default$1() {
        return this.lower();
    }

    public BigDecimal copy$default$2() {
        return this.upper();
    }

    public String productPrefix() {
        return "RDDPartitionBounds";
    }

    public int productArity() {
        return 2;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.lower();
            }
            case 1: {
                return this.upper();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof RDDPartitionBounds;
    }

    public int hashCode() {
        return ScalaRunTime$.MODULE$._hashCode((Product)this);
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
        if (!(object instanceof RDDPartitionBounds)) return false;
        boolean bl = true;
        if (!bl) return false;
        RDDPartitionBounds rDDPartitionBounds = (RDDPartitionBounds)x$1;
        BigDecimal bigDecimal = this.lower();
        BigDecimal bigDecimal2 = rDDPartitionBounds.lower();
        if (bigDecimal == null) {
            if (bigDecimal2 != null) {
                return false;
            }
        } else if (!bigDecimal.equals(bigDecimal2)) return false;
        BigDecimal bigDecimal3 = this.upper();
        BigDecimal bigDecimal4 = rDDPartitionBounds.upper();
        if (bigDecimal3 == null) {
            if (bigDecimal4 != null) {
                return false;
            }
        } else if (!bigDecimal3.equals(bigDecimal4)) return false;
        if (!rDDPartitionBounds.canEqual(this)) return false;
        return true;
    }

    public RDDPartitionBounds(BigDecimal lower, BigDecimal upper) {
        this.lower = lower;
        this.upper = upper;
        Product.$init$((Product)this);
    }
}

