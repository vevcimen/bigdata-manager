/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.Partition
 *  scala.Function1
 *  scala.Option
 *  scala.Product
 *  scala.Tuple2
 *  scala.collection.Iterator
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.GreenplumPartition$;
import org.apache.spark.Partition;
import scala.Function1;
import scala.Option;
import scala.Product;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u0005]c\u0001B\r\u001b\u0001\u000eB\u0001\"\u000f\u0001\u0003\u0016\u0004%\tA\u000f\u0005\t\r\u0002\u0011\t\u0012)A\u0005w!Aq\t\u0001BK\u0002\u0013\u0005\u0001\n\u0003\u0005M\u0001\tE\t\u0015!\u0003J\u0011\u0015i\u0005\u0001\"\u0001O\u0011\u0015\u0019\u0006\u0001\"\u0011I\u0011\u0015!\u0006\u0001\"\u0011V\u0011\u00151\u0006\u0001\"\u0011X\u0011\u001d\u0001\u0007!!A\u0005\u0002\u0005Dq\u0001\u001a\u0001\u0012\u0002\u0013\u0005Q\rC\u0004q\u0001E\u0005I\u0011A9\t\u000fM\u0004\u0011\u0011!C!i\"9A\u0010AA\u0001\n\u0003A\u0005bB?\u0001\u0003\u0003%\tA \u0005\n\u0003\u0007\u0001\u0011\u0011!C!\u0003\u000bA\u0011\"a\u0005\u0001\u0003\u0003%\t!!\u0006\t\u0013\u0005e\u0001!!A\u0005B\u0005mq!CA\u000f5\u0005\u0005\t\u0012AA\u0010\r!I\"$!A\t\u0002\u0005\u0005\u0002BB'\u0014\t\u0003\ty\u0003C\u0005\u0002\u001aM\t\t\u0011\"\u0012\u0002\u001c!I\u0011\u0011G\n\u0002\u0002\u0013\u0005\u00151\u0007\u0005\n\u0003s\u0019\u0012\u0011!CA\u0003wA\u0011\"!\u0014\u0014\u0003\u0003%I!a\u0014\u0003%\u001d\u0013X-\u001a8qYVl\u0007+\u0019:uSRLwN\u001c\u0006\u00037q\tQa\u001d9be.T!!\b\u0010\u0002\u0013\u001d\u0014X-\u001a8qYVl'BA\u0010!\u0003\u001d\u0001\u0018N^8uC2T\u0011!I\u0001\u0003S>\u001c\u0001aE\u0003\u0001I)\u001ad\u0007\u0005\u0002&Q5\taEC\u0001(\u0003\u0015\u00198-\u00197b\u0013\tIcE\u0001\u0004B]f\u0014VM\u001a\t\u0003WEj\u0011\u0001\f\u0006\u000375R!AL\u0018\u0002\r\u0005\u0004\u0018m\u00195f\u0015\u0005\u0001\u0014aA8sO&\u0011!\u0007\f\u0002\n!\u0006\u0014H/\u001b;j_:\u0004\"!\n\u001b\n\u0005U2#a\u0002)s_\u0012,8\r\u001e\t\u0003K]J!\u0001\u000f\u0014\u0003\u0019M+'/[1mSj\f'\r\\3\u0002\u0017]DWM]3DY\u0006,8/Z\u000b\u0002wA\u0011Ah\u0011\b\u0003{\u0005\u0003\"A\u0010\u0014\u000e\u0003}R!\u0001\u0011\u0012\u0002\rq\u0012xn\u001c;?\u0013\t\u0011e%\u0001\u0004Qe\u0016$WMZ\u0005\u0003\t\u0016\u0013aa\u0015;sS:<'B\u0001\"'\u000319\b.\u001a:f\u00072\fWo]3!\u0003\rIG\r_\u000b\u0002\u0013B\u0011QES\u0005\u0003\u0017\u001a\u00121!\u00138u\u0003\u0011IG\r\u001f\u0011\u0002\rqJg.\u001b;?)\ry\u0015K\u0015\t\u0003!\u0002i\u0011A\u0007\u0005\u0006s\u0015\u0001\ra\u000f\u0005\u0006\u000f\u0016\u0001\r!S\u0001\u0006S:$W\r_\u0001\tQ\u0006\u001c\bnQ8eKR\t\u0011*\u0001\u0004fcV\fGn\u001d\u000b\u00031n\u0003\"!J-\n\u0005i3#a\u0002\"p_2,\u0017M\u001c\u0005\u00069\"\u0001\r!X\u0001\u0006_RDWM\u001d\t\u0003KyK!a\u0018\u0014\u0003\u0007\u0005s\u00170\u0001\u0003d_BLHcA(cG\"9\u0011(\u0003I\u0001\u0002\u0004Y\u0004bB$\n!\u0003\u0005\r!S\u0001\u000fG>\u0004\u0018\u0010\n3fM\u0006,H\u000e\u001e\u00132+\u00051'FA\u001ehW\u0005A\u0007CA5o\u001b\u0005Q'BA6m\u0003%)hn\u00195fG.,GM\u0003\u0002nM\u0005Q\u0011M\u001c8pi\u0006$\u0018n\u001c8\n\u0005=T'!E;oG\",7m[3e-\u0006\u0014\u0018.\u00198dK\u0006q1m\u001c9zI\u0011,g-Y;mi\u0012\u0012T#\u0001:+\u0005%;\u0017!\u00049s_\u0012,8\r\u001e)sK\u001aL\u00070F\u0001v!\t180D\u0001x\u0015\tA\u00180\u0001\u0003mC:<'\"\u0001>\u0002\t)\fg/Y\u0005\u0003\t^\fA\u0002\u001d:pIV\u001cG/\u0011:jif\fa\u0002\u001d:pIV\u001cG/\u00127f[\u0016tG\u000f\u0006\u0002^\u007f\"A\u0011\u0011\u0001\b\u0002\u0002\u0003\u0007\u0011*A\u0002yIE\nq\u0002\u001d:pIV\u001cG/\u0013;fe\u0006$xN]\u000b\u0003\u0003\u000f\u0001R!!\u0003\u0002\u0010uk!!a\u0003\u000b\u0007\u00055a%\u0001\u0006d_2dWm\u0019;j_:LA!!\u0005\u0002\f\tA\u0011\n^3sCR|'/\u0001\u0005dC:,\u0015/^1m)\rA\u0016q\u0003\u0005\t\u0003\u0003\u0001\u0012\u0011!a\u0001;\u0006AAo\\*ue&tw\rF\u0001v\u0003I9%/Z3oa2,X\u000eU1si&$\u0018n\u001c8\u0011\u0005A\u001b2\u0003B\n\u0002$Y\u0002r!!\n\u0002,mJu*\u0004\u0002\u0002()\u0019\u0011\u0011\u0006\u0014\u0002\u000fI,h\u000e^5nK&!\u0011QFA\u0014\u0005E\t%m\u001d;sC\u000e$h)\u001e8di&|gN\r\u000b\u0003\u0003?\tQ!\u00199qYf$RaTA\u001b\u0003oAQ!\u000f\fA\u0002mBQa\u0012\fA\u0002%\u000bq!\u001e8baBd\u0017\u0010\u0006\u0003\u0002>\u0005%\u0003#B\u0013\u0002@\u0005\r\u0013bAA!M\t1q\n\u001d;j_:\u0004R!JA#w%K1!a\u0012'\u0005\u0019!V\u000f\u001d7fe!A\u00111J\f\u0002\u0002\u0003\u0007q*A\u0002yIA\n1B]3bIJ+7o\u001c7wKR\u0011\u0011\u0011\u000b\t\u0004m\u0006M\u0013bAA+o\n1qJ\u00196fGR\u0004")
public class GreenplumPartition
implements Partition,
Product {
    private final String whereClause;
    private final int idx;

    public static Option<Tuple2<String, Object>> unapply(GreenplumPartition greenplumPartition) {
        return GreenplumPartition$.MODULE$.unapply(greenplumPartition);
    }

    public static GreenplumPartition apply(String string, int n) {
        return GreenplumPartition$.MODULE$.apply(string, n);
    }

    public static Function1<Tuple2<String, Object>, GreenplumPartition> tupled() {
        return GreenplumPartition$.MODULE$.tupled();
    }

    public static Function1<String, Function1<Object, GreenplumPartition>> curried() {
        return GreenplumPartition$.MODULE$.curried();
    }

    public /* synthetic */ boolean org$apache$spark$Partition$$super$equals(Object x$1) {
        return super.equals(x$1);
    }

    public String whereClause() {
        return this.whereClause;
    }

    public int idx() {
        return this.idx;
    }

    public int index() {
        return this.idx();
    }

    public int hashCode() {
        return Partition.hashCode$((Partition)this);
    }

    public boolean equals(Object other) {
        Object object = other;
        if (object instanceof GreenplumPartition) {
            GreenplumPartition greenplumPartition = (GreenplumPartition)object;
            return this.whereClause().equals(greenplumPartition.whereClause()) && this.idx() == greenplumPartition.idx();
        }
        return false;
    }

    public GreenplumPartition copy(String whereClause, int idx) {
        return new GreenplumPartition(whereClause, idx);
    }

    public String copy$default$1() {
        return this.whereClause();
    }

    public int copy$default$2() {
        return this.idx();
    }

    public String productPrefix() {
        return "GreenplumPartition";
    }

    public int productArity() {
        return 2;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.whereClause();
            }
            case 1: {
                return BoxesRunTime.boxToInteger((int)this.idx());
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof GreenplumPartition;
    }

    public String toString() {
        return ScalaRunTime$.MODULE$._toString((Product)this);
    }

    public GreenplumPartition(String whereClause, int idx) {
        this.whereClause = whereClause;
        this.idx = idx;
        Partition.$init$((Partition)this);
        Product.$init$((Product)this);
    }
}

