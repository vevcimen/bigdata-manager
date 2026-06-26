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
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.jdbc.ColumnValueRange$;
import java.math.BigDecimal;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u0005-d\u0001\u0002\u000e\u001c\u0001\u001aB\u0001b\r\u0001\u0003\u0016\u0004%\t\u0001\u000e\u0005\t\u0003\u0002\u0011\t\u0012)A\u0005k!A!\t\u0001BK\u0002\u0013\u0005A\u0007\u0003\u0005D\u0001\tE\t\u0015!\u00036\u0011\u0015!\u0005\u0001\"\u0001F\u0011\u001dQ\u0005!!A\u0005\u0002-CqA\u0014\u0001\u0012\u0002\u0013\u0005q\nC\u0004[\u0001E\u0005I\u0011A(\t\u000fm\u0003\u0011\u0011!C!9\"9Q\rAA\u0001\n\u00031\u0007b\u00026\u0001\u0003\u0003%\ta\u001b\u0005\bc\u0002\t\t\u0011\"\u0011s\u0011\u001dI\b!!A\u0005\u0002iD\u0001b \u0001\u0002\u0002\u0013\u0005\u0013\u0011\u0001\u0005\n\u0003\u0007\u0001\u0011\u0011!C!\u0003\u000bA\u0011\"a\u0002\u0001\u0003\u0003%\t%!\u0003\b\u000f\u000551\u0004#\u0001\u0002\u0010\u00191!d\u0007E\u0001\u0003#Aa\u0001\u0012\n\u0005\u0002\u0005M\u0001bBA\u000b%\u0011\u0005\u0011q\u0003\u0005\b\u0003+\u0011B\u0011AA\u0012\u0011\u001d\t)B\u0005C\u0001\u0003oA\u0011\"!\u0006\u0013\u0003\u0003%\t)a\u0012\t\u0013\u00055##!A\u0005\u0002\u0006=\u0003\"CA1%\u0005\u0005I\u0011BA2\u0005A\u0019u\u000e\\;n]Z\u000bG.^3SC:<WM\u0003\u0002\u001d;\u0005!!\u000e\u001a2d\u0015\tqr$A\u0003ta\u0006\u00148N\u0003\u0002!C\u0005IqM]3f]BdW/\u001c\u0006\u0003E\r\nq\u0001]5w_R\fGNC\u0001%\u0003\tIwn\u0001\u0001\u0014\t\u00019S\u0006\r\t\u0003Q-j\u0011!\u000b\u0006\u0002U\u0005)1oY1mC&\u0011A&\u000b\u0002\u0007\u0003:L(+\u001a4\u0011\u0005!r\u0013BA\u0018*\u0005\u001d\u0001&o\u001c3vGR\u0004\"\u0001K\u0019\n\u0005IJ#\u0001D*fe&\fG.\u001b>bE2,\u0017aA7j]V\tQ\u0007\u0005\u00027}9\u0011q\u0007\u0010\b\u0003qmj\u0011!\u000f\u0006\u0003u\u0015\na\u0001\u0010:p_Rt\u0014\"\u0001\u0016\n\u0005uJ\u0013a\u00029bG.\fw-Z\u0005\u0003\u007f\u0001\u0013!BQ5h\t\u0016\u001c\u0017.\\1m\u0015\ti\u0014&\u0001\u0003nS:\u0004\u0013aA7bq\u0006!Q.\u0019=!\u0003\u0019a\u0014N\\5u}Q\u0019a\tS%\u0011\u0005\u001d\u0003Q\"A\u000e\t\u000bM*\u0001\u0019A\u001b\t\u000b\t+\u0001\u0019A\u001b\u0002\t\r|\u0007/\u001f\u000b\u0004\r2k\u0005bB\u001a\u0007!\u0003\u0005\r!\u000e\u0005\b\u0005\u001a\u0001\n\u00111\u00016\u00039\u0019w\u000e]=%I\u00164\u0017-\u001e7uIE*\u0012\u0001\u0015\u0016\u0003kE[\u0013A\u0015\t\u0003'bk\u0011\u0001\u0016\u0006\u0003+Z\u000b\u0011\"\u001e8dQ\u0016\u001c7.\u001a3\u000b\u0005]K\u0013AC1o]>$\u0018\r^5p]&\u0011\u0011\f\u0016\u0002\u0012k:\u001c\u0007.Z2lK\u00124\u0016M]5b]\u000e,\u0017AD2paf$C-\u001a4bk2$HEM\u0001\u000eaJ|G-^2u!J,g-\u001b=\u0016\u0003u\u0003\"AX2\u000e\u0003}S!\u0001Y1\u0002\t1\fgn\u001a\u0006\u0002E\u0006!!.\u0019<b\u0013\t!wL\u0001\u0004TiJLgnZ\u0001\raJ|G-^2u\u0003JLG/_\u000b\u0002OB\u0011\u0001\u0006[\u0005\u0003S&\u00121!\u00138u\u00039\u0001(o\u001c3vGR,E.Z7f]R$\"\u0001\\8\u0011\u0005!j\u0017B\u00018*\u0005\r\te.\u001f\u0005\ba.\t\t\u00111\u0001h\u0003\rAH%M\u0001\u0010aJ|G-^2u\u0013R,'/\u0019;peV\t1\u000fE\u0002uo2l\u0011!\u001e\u0006\u0003m&\n!bY8mY\u0016\u001cG/[8o\u0013\tAXO\u0001\u0005Ji\u0016\u0014\u0018\r^8s\u0003!\u0019\u0017M\\#rk\u0006dGCA>\u007f!\tAC0\u0003\u0002~S\t9!i\\8mK\u0006t\u0007b\u00029\u000e\u0003\u0003\u0005\r\u0001\\\u0001\tQ\u0006\u001c\bnQ8eKR\tq-\u0001\u0005u_N#(/\u001b8h)\u0005i\u0016AB3rk\u0006d7\u000fF\u0002|\u0003\u0017Aq\u0001\u001d\t\u0002\u0002\u0003\u0007A.\u0001\tD_2,XN\u001c,bYV,'+\u00198hKB\u0011qIE\n\u0004%\u001d\u0002DCAA\b\u0003\u0015\t\u0007\u000f\u001d7z)\u00151\u0015\u0011DA\u0011\u0011\u0019\u0019D\u00031\u0001\u0002\u001cA\u0019\u0001&!\b\n\u0007\u0005}\u0011F\u0001\u0003M_:<\u0007B\u0002\"\u0015\u0001\u0004\tY\u0002F\u0003G\u0003K\t)\u0004\u0003\u00044+\u0001\u0007\u0011q\u0005\t\u0005\u0003S\t\tD\u0004\u0003\u0002,\u00055\u0002C\u0001\u001d*\u0013\r\ty#K\u0001\u0007!J,G-\u001a4\n\u0007\u0011\f\u0019DC\u0002\u00020%BaAQ\u000bA\u0002\u0005\u001dB#\u0002$\u0002:\u0005\u0015\u0003BB\u001a\u0017\u0001\u0004\tY\u0004\u0005\u0003\u0002>\u0005\rSBAA \u0015\r\t\t%Y\u0001\u0005[\u0006$\b.C\u0002@\u0003\u007fAaA\u0011\fA\u0002\u0005mB#\u0002$\u0002J\u0005-\u0003\"B\u001a\u0018\u0001\u0004)\u0004\"\u0002\"\u0018\u0001\u0004)\u0014aB;oCB\u0004H.\u001f\u000b\u0005\u0003#\ni\u0006E\u0003)\u0003'\n9&C\u0002\u0002V%\u0012aa\u00149uS>t\u0007#\u0002\u0015\u0002ZU*\u0014bAA.S\t1A+\u001e9mKJB\u0001\"a\u0018\u0019\u0003\u0003\u0005\rAR\u0001\u0004q\u0012\u0002\u0014a\u0003:fC\u0012\u0014Vm]8mm\u0016$\"!!\u001a\u0011\u0007y\u000b9'C\u0002\u0002j}\u0013aa\u00142kK\u000e$\b")
public class ColumnValueRange
implements Product,
Serializable {
    private final scala.math.BigDecimal min;
    private final scala.math.BigDecimal max;

    public static Option<Tuple2<scala.math.BigDecimal, scala.math.BigDecimal>> unapply(ColumnValueRange columnValueRange) {
        return ColumnValueRange$.MODULE$.unapply(columnValueRange);
    }

    public static ColumnValueRange apply(scala.math.BigDecimal bigDecimal, scala.math.BigDecimal bigDecimal2) {
        return ColumnValueRange$.MODULE$.apply(bigDecimal, bigDecimal2);
    }

    public static ColumnValueRange apply(BigDecimal bigDecimal, BigDecimal bigDecimal2) {
        return ColumnValueRange$.MODULE$.apply(bigDecimal, bigDecimal2);
    }

    public static ColumnValueRange apply(String string, String string2) {
        return ColumnValueRange$.MODULE$.apply(string, string2);
    }

    public static ColumnValueRange apply(long l, long l2) {
        return ColumnValueRange$.MODULE$.apply(l, l2);
    }

    public scala.math.BigDecimal min() {
        return this.min;
    }

    public scala.math.BigDecimal max() {
        return this.max;
    }

    public ColumnValueRange copy(scala.math.BigDecimal min2, scala.math.BigDecimal max) {
        return new ColumnValueRange(min2, max);
    }

    public scala.math.BigDecimal copy$default$1() {
        return this.min();
    }

    public scala.math.BigDecimal copy$default$2() {
        return this.max();
    }

    public String productPrefix() {
        return "ColumnValueRange";
    }

    public int productArity() {
        return 2;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.min();
            }
            case 1: {
                return this.max();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof ColumnValueRange;
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
        if (!(object instanceof ColumnValueRange)) return false;
        boolean bl = true;
        if (!bl) return false;
        ColumnValueRange columnValueRange = (ColumnValueRange)x$1;
        scala.math.BigDecimal bigDecimal = this.min();
        scala.math.BigDecimal bigDecimal2 = columnValueRange.min();
        if (bigDecimal == null) {
            if (bigDecimal2 != null) {
                return false;
            }
        } else if (!bigDecimal.equals(bigDecimal2)) return false;
        scala.math.BigDecimal bigDecimal3 = this.max();
        scala.math.BigDecimal bigDecimal4 = columnValueRange.max();
        if (bigDecimal3 == null) {
            if (bigDecimal4 != null) {
                return false;
            }
        } else if (!bigDecimal3.equals(bigDecimal4)) return false;
        if (!columnValueRange.canEqual(this)) return false;
        return true;
    }

    public ColumnValueRange(scala.math.BigDecimal min2, scala.math.BigDecimal max) {
        this.min = min2;
        this.max = max;
        Product.$init$((Product)this);
    }
}

