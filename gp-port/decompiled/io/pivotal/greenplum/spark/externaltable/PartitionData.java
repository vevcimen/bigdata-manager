/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.Row
 *  scala.Function1
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.Tuple4
 *  scala.collection.Iterator
 *  scala.collection.immutable.List
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 *  scala.runtime.Statics
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.PartitionData$;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.spark.sql.Row;
import scala.Function1;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.Tuple4;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;
import scala.runtime.Statics;

@ScalaSignature(bytes="\u0006\u0001\u0005mg\u0001B\u0014)\u0001NB\u0001\u0002\u0011\u0001\u0003\u0016\u0004%\t!\u0011\u0005\t\u000b\u0002\u0011\t\u0012)A\u0005\u0005\"Aa\t\u0001BK\u0002\u0013\u0005q\t\u0003\u0005`\u0001\tE\t\u0015!\u0003I\u0011!\u0001\u0007A!e\u0001\n\u0003\t\u0007\u0002C3\u0001\u0005\u0003\u0007I\u0011\u00014\t\u00111\u0004!\u0011#Q!\n\tD\u0001\"\u001c\u0001\u0003\u0016\u0004%\tA\u001c\u0005\te\u0002\u0011\t\u0012)A\u0005_\")1\u000f\u0001C\u0001i\"91\u0010\u0001b\u0001\n\u0003a\bbBA\n\u0001\u0001\u0006I! \u0005\n\u0003+\u0001\u0011\u0011!C\u0001\u0003/A\u0011\"!\t\u0001#\u0003%\t!a\t\t\u0013\u0005e\u0002!%A\u0005\u0002\u0005m\u0002\"CA \u0001E\u0005I\u0011AA!\u0011%\t)\u0005AI\u0001\n\u0003\t9\u0005C\u0005\u0002L\u0001\t\t\u0011\"\u0011\u0002N!A\u00111\f\u0001\u0002\u0002\u0013\u0005\u0011\tC\u0005\u0002^\u0001\t\t\u0011\"\u0001\u0002`!I\u0011\u0011\u000e\u0001\u0002\u0002\u0013\u0005\u00131\u000e\u0005\n\u0003o\u0002\u0011\u0011!C\u0001\u0003sB\u0011\"a!\u0001\u0003\u0003%\t%!\"\t\u0013\u0005\u001d\u0005!!A\u0005B\u0005%\u0005\"CAF\u0001\u0005\u0005I\u0011IAG\u000f%\t\t\nKA\u0001\u0012\u0003\t\u0019J\u0002\u0005(Q\u0005\u0005\t\u0012AAK\u0011\u0019\u00198\u0004\"\u0001\u0002$\"I\u0011qQ\u000e\u0002\u0002\u0013\u0015\u0013\u0011\u0012\u0005\n\u0003K[\u0012\u0011!CA\u0003OC\u0011\"!-\u001c#\u0003%\t!a\u000f\t\u0013\u0005M6$%A\u0005\u0002\u0005\u0005\u0003\"CA[7E\u0005I\u0011AA$\u0011%\t9lGA\u0001\n\u0003\u000bI\fC\u0005\u0002Ln\t\n\u0011\"\u0001\u0002<!I\u0011QZ\u000e\u0012\u0002\u0013\u0005\u0011\u0011\t\u0005\n\u0003\u001f\\\u0012\u0013!C\u0001\u0003\u000fB\u0011\"!5\u001c\u0003\u0003%I!a5\u0003\u001bA\u000b'\u000f^5uS>tG)\u0019;b\u0015\tI#&A\u0007fqR,'O\\1mi\u0006\u0014G.\u001a\u0006\u0003W1\nQa\u001d9be.T!!\f\u0018\u0002\u0013\u001d\u0014X-\u001a8qYVl'BA\u00181\u0003\u001d\u0001\u0018N^8uC2T\u0011!M\u0001\u0003S>\u001c\u0001a\u0005\u0003\u0001iij\u0004CA\u001b9\u001b\u00051$\"A\u001c\u0002\u000bM\u001c\u0017\r\\1\n\u0005e2$AB!osJ+g\r\u0005\u00026w%\u0011AH\u000e\u0002\b!J|G-^2u!\t)d(\u0003\u0002@m\ta1+\u001a:jC2L'0\u00192mK\u0006q\u0001/\u0019:uSRLwN\\%oI\u0016DX#\u0001\"\u0011\u0005U\u001a\u0015B\u0001#7\u0005\rIe\u000e^\u0001\u0010a\u0006\u0014H/\u001b;j_:Le\u000eZ3yA\u0005Y!o\\<Ji\u0016\u0014\u0018\r^8s+\u0005A\u0005cA%R):\u0011!j\u0014\b\u0003\u0017:k\u0011\u0001\u0014\u0006\u0003\u001bJ\na\u0001\u0010:p_Rt\u0014\"A\u001c\n\u0005A3\u0014a\u00029bG.\fw-Z\u0005\u0003%N\u0013\u0001\"\u0013;fe\u0006$xN\u001d\u0006\u0003!Z\u0002\"!V/\u000e\u0003YS!a\u0016-\u0002\u0007M\fHN\u0003\u0002,3*\u0011!lW\u0001\u0007CB\f7\r[3\u000b\u0003q\u000b1a\u001c:h\u0013\tqfKA\u0002S_^\fAB]8x\u0013R,'/\u0019;pe\u0002\nAA]8xgV\t!\rE\u0002JGRK!\u0001Z*\u0003\t1K7\u000f^\u0001\te><8o\u0018\u0013fcR\u0011qM\u001b\t\u0003k!L!!\u001b\u001c\u0003\tUs\u0017\u000e\u001e\u0005\bW\u001a\t\t\u00111\u0001c\u0003\rAH%M\u0001\u0006e><8\u000fI\u0001\u000fe><HK]1og\u001a|'/\\3s+\u0005y\u0007\u0003B\u001bq)RK!!\u001d\u001c\u0003\u0013\u0019+hn\u0019;j_:\f\u0014a\u0004:poR\u0013\u0018M\\:g_JlWM\u001d\u0011\u0002\rqJg.\u001b;?)\u0015)x\u000f_={!\t1\b!D\u0001)\u0011\u0015\u0001%\u00021\u0001C\u0011\u001d1%\u0002%AA\u0002!Cq\u0001\u0019\u0006\u0011\u0002\u0003\u0007!\rC\u0004n\u0015A\u0005\t\u0019A8\u0002\u000f!\fg\u000e\u001a7fIV\tQ\u0010E\u0002\u007f\u0003\u001fi\u0011a \u0006\u0005\u0003\u0003\t\u0019!\u0001\u0004bi>l\u0017n\u0019\u0006\u0005\u0003\u000b\t9!\u0001\u0006d_:\u001cWO\u001d:f]RTA!!\u0003\u0002\f\u0005!Q\u000f^5m\u0015\t\ti!\u0001\u0003kCZ\f\u0017bAA\t\u007f\ni\u0011\t^8nS\u000e\u0014un\u001c7fC:\f\u0001\u0002[1oI2,G\rI\u0001\u0005G>\u0004\u0018\u0010F\u0005v\u00033\tY\"!\b\u0002 !9\u0001)\u0004I\u0001\u0002\u0004\u0011\u0005b\u0002$\u000e!\u0003\u0005\r\u0001\u0013\u0005\bA6\u0001\n\u00111\u0001c\u0011\u001diW\u0002%AA\u0002=\fabY8qs\u0012\"WMZ1vYR$\u0013'\u0006\u0002\u0002&)\u001a!)a\n,\u0005\u0005%\u0002\u0003BA\u0016\u0003ki!!!\f\u000b\t\u0005=\u0012\u0011G\u0001\nk:\u001c\u0007.Z2lK\u0012T1!a\r7\u0003)\tgN\\8uCRLwN\\\u0005\u0005\u0003o\tiCA\tv]\u000eDWmY6fIZ\u000b'/[1oG\u0016\fabY8qs\u0012\"WMZ1vYR$#'\u0006\u0002\u0002>)\u001a\u0001*a\n\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%gU\u0011\u00111\t\u0016\u0004E\u0006\u001d\u0012AD2paf$C-\u001a4bk2$H\u0005N\u000b\u0003\u0003\u0013R3a\\A\u0014\u00035\u0001(o\u001c3vGR\u0004&/\u001a4jqV\u0011\u0011q\n\t\u0005\u0003#\n9&\u0004\u0002\u0002T)!\u0011QKA\u0006\u0003\u0011a\u0017M\\4\n\t\u0005e\u00131\u000b\u0002\u0007'R\u0014\u0018N\\4\u0002\u0019A\u0014x\u000eZ;di\u0006\u0013\u0018\u000e^=\u0002\u001dA\u0014x\u000eZ;di\u0016cW-\\3oiR!\u0011\u0011MA4!\r)\u00141M\u0005\u0004\u0003K2$aA!os\"91\u000eFA\u0001\u0002\u0004\u0011\u0015a\u00049s_\u0012,8\r^%uKJ\fGo\u001c:\u0016\u0005\u00055\u0004CBA8\u0003k\n\t'\u0004\u0002\u0002r)\u0019\u00111\u000f\u001c\u0002\u0015\r|G\u000e\\3di&|g.C\u0002S\u0003c\n\u0001bY1o\u000bF,\u0018\r\u001c\u000b\u0005\u0003w\n\t\tE\u00026\u0003{J1!a 7\u0005\u001d\u0011un\u001c7fC:D\u0001b\u001b\f\u0002\u0002\u0003\u0007\u0011\u0011M\u0001\tQ\u0006\u001c\bnQ8eKR\t!)\u0001\u0005u_N#(/\u001b8h)\t\ty%\u0001\u0004fcV\fGn\u001d\u000b\u0005\u0003w\ny\t\u0003\u0005l3\u0005\u0005\t\u0019AA1\u00035\u0001\u0016M\u001d;ji&|g\u000eR1uCB\u0011aoG\n\u00057\u0005]U\bE\u0005\u0002\u001a\u0006}%\t\u00132pk6\u0011\u00111\u0014\u0006\u0004\u0003;3\u0014a\u0002:v]RLW.Z\u0005\u0005\u0003C\u000bYJA\tBEN$(/Y2u\rVt7\r^5p]R\"\"!a%\u0002\u000b\u0005\u0004\b\u000f\\=\u0015\u0013U\fI+a+\u0002.\u0006=\u0006\"\u0002!\u001f\u0001\u0004\u0011\u0005b\u0002$\u001f!\u0003\u0005\r\u0001\u0013\u0005\bAz\u0001\n\u00111\u0001c\u0011\u001dig\u0004%AA\u0002=\fq\"\u00199qYf$C-\u001a4bk2$HEM\u0001\u0010CB\u0004H.\u001f\u0013eK\u001a\fW\u000f\u001c;%g\u0005y\u0011\r\u001d9ms\u0012\"WMZ1vYR$C'A\u0004v]\u0006\u0004\b\u000f\\=\u0015\t\u0005m\u0016q\u0019\t\u0006k\u0005u\u0016\u0011Y\u0005\u0004\u0003\u007f3$AB(qi&|g\u000eE\u00046\u0003\u0007\u0014\u0005JY8\n\u0007\u0005\u0015gG\u0001\u0004UkBdW\r\u000e\u0005\t\u0003\u0013\u0014\u0013\u0011!a\u0001k\u0006\u0019\u0001\u0010\n\u0019\u00027\u0011bWm]:j]&$He\u001a:fCR,'\u000f\n3fM\u0006,H\u000e\u001e\u00133\u0003m!C.Z:tS:LG\u000fJ4sK\u0006$XM\u001d\u0013eK\u001a\fW\u000f\u001c;%g\u0005YB\u0005\\3tg&t\u0017\u000e\u001e\u0013he\u0016\fG/\u001a:%I\u00164\u0017-\u001e7uIQ\n1B]3bIJ+7o\u001c7wKR\u0011\u0011Q\u001b\t\u0005\u0003#\n9.\u0003\u0003\u0002Z\u0006M#AB(cU\u0016\u001cG\u000f")
public class PartitionData
implements Product,
Serializable {
    private final int partitionIndex;
    private final Iterator<Row> rowIterator;
    private List<Row> rows;
    private final Function1<Row, Row> rowTransformer;
    private final AtomicBoolean handled;

    public static Function1<Row, Row> $lessinit$greater$default$4() {
        return PartitionData$.MODULE$.$lessinit$greater$default$4();
    }

    public static List<Row> $lessinit$greater$default$3() {
        return PartitionData$.MODULE$.$lessinit$greater$default$3();
    }

    public static Iterator<Row> $lessinit$greater$default$2() {
        return PartitionData$.MODULE$.$lessinit$greater$default$2();
    }

    public static Option<Tuple4<Object, Iterator<Row>, List<Row>, Function1<Row, Row>>> unapply(PartitionData partitionData) {
        return PartitionData$.MODULE$.unapply(partitionData);
    }

    public static Function1<Row, Row> apply$default$4() {
        return PartitionData$.MODULE$.apply$default$4();
    }

    public static List<Row> apply$default$3() {
        return PartitionData$.MODULE$.apply$default$3();
    }

    public static Iterator<Row> apply$default$2() {
        return PartitionData$.MODULE$.apply$default$2();
    }

    public static PartitionData apply(int n, Iterator<Row> iterator, List<Row> list, Function1<Row, Row> function1) {
        return PartitionData$.MODULE$.apply(n, iterator, list, function1);
    }

    public static Function1<Tuple4<Object, Iterator<Row>, List<Row>, Function1<Row, Row>>, PartitionData> tupled() {
        return PartitionData$.MODULE$.tupled();
    }

    public static Function1<Object, Function1<Iterator<Row>, Function1<List<Row>, Function1<Function1<Row, Row>, PartitionData>>>> curried() {
        return PartitionData$.MODULE$.curried();
    }

    public int partitionIndex() {
        return this.partitionIndex;
    }

    public Iterator<Row> rowIterator() {
        return this.rowIterator;
    }

    public List<Row> rows() {
        return this.rows;
    }

    public void rows_$eq(List<Row> x$1) {
        this.rows = x$1;
    }

    public Function1<Row, Row> rowTransformer() {
        return this.rowTransformer;
    }

    public AtomicBoolean handled() {
        return this.handled;
    }

    public PartitionData copy(int partitionIndex, Iterator<Row> rowIterator, List<Row> rows, Function1<Row, Row> rowTransformer) {
        return new PartitionData(partitionIndex, rowIterator, rows, rowTransformer);
    }

    public int copy$default$1() {
        return this.partitionIndex();
    }

    public Iterator<Row> copy$default$2() {
        return this.rowIterator();
    }

    public List<Row> copy$default$3() {
        return this.rows();
    }

    public Function1<Row, Row> copy$default$4() {
        return this.rowTransformer();
    }

    public String productPrefix() {
        return "PartitionData";
    }

    public int productArity() {
        return 4;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return BoxesRunTime.boxToInteger((int)this.partitionIndex());
            }
            case 1: {
                return this.rowIterator();
            }
            case 2: {
                return this.rows();
            }
            case 3: {
                return this.rowTransformer();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof PartitionData;
    }

    public int hashCode() {
        int n = -889275714;
        n = Statics.mix((int)n, (int)this.partitionIndex());
        n = Statics.mix((int)n, (int)Statics.anyHash(this.rowIterator()));
        n = Statics.mix((int)n, (int)Statics.anyHash(this.rows()));
        n = Statics.mix((int)n, (int)Statics.anyHash(this.rowTransformer()));
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
        if (!(object instanceof PartitionData)) return false;
        boolean bl = true;
        if (!bl) return false;
        PartitionData partitionData = (PartitionData)x$1;
        if (this.partitionIndex() != partitionData.partitionIndex()) return false;
        Iterator<Row> iterator = this.rowIterator();
        Iterator<Row> iterator2 = partitionData.rowIterator();
        if (iterator == null) {
            if (iterator2 != null) {
                return false;
            }
        } else if (!iterator.equals(iterator2)) return false;
        List<Row> list = this.rows();
        List<Row> list2 = partitionData.rows();
        if (list == null) {
            if (list2 != null) {
                return false;
            }
        } else if (!list.equals(list2)) return false;
        Function1<Row, Row> function1 = this.rowTransformer();
        Function1<Row, Row> function12 = partitionData.rowTransformer();
        if (function1 == null) {
            if (function12 != null) {
                return false;
            }
        } else if (!function1.equals(function12)) return false;
        if (!partitionData.canEqual(this)) return false;
        return true;
    }

    public PartitionData(int partitionIndex, Iterator<Row> rowIterator, List<Row> rows, Function1<Row, Row> rowTransformer) {
        this.partitionIndex = partitionIndex;
        this.rowIterator = rowIterator;
        this.rows = rows;
        this.rowTransformer = rowTransformer;
        Product.$init$((Product)this);
        this.handled = new AtomicBoolean(false);
    }
}

