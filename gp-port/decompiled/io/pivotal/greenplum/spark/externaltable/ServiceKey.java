/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.collection.immutable.List
 *  scala.reflect.ScalaSignature
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.ServiceKey$;
import scala.Function1;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.reflect.ScalaSignature;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u0005-b\u0001B\u000b\u0017\t\u0006B\u0001B\f\u0001\u0003\u0016\u0004%\ta\f\u0005\t\u007f\u0001\u0011\t\u0012)A\u0005a!)\u0001\t\u0001C\u0001\u0003\"9Q\tAA\u0001\n\u00031\u0005b\u0002%\u0001#\u0003%\t!\u0013\u0005\b)\u0002\t\t\u0011\"\u0011V\u0011\u001dq\u0006!!A\u0005\u0002}Cq\u0001\u0019\u0001\u0002\u0002\u0013\u0005\u0011\rC\u0004h\u0001\u0005\u0005I\u0011\t5\t\u000f=\u0004\u0011\u0011!C\u0001a\"9Q\u000fAA\u0001\n\u00032\bbB<\u0001\u0003\u0003%\t\u0005\u001f\u0005\bs\u0002\t\t\u0011\"\u0011{\u000f\u001dah#!A\t\nu4q!\u0006\f\u0002\u0002#%a\u0010\u0003\u0004A\u001f\u0011\u0005\u00111\u0002\u0005\bo>\t\t\u0011\"\u0012y\u0011%\tiaDA\u0001\n\u0003\u000by\u0001C\u0005\u0002\u0014=\t\t\u0011\"!\u0002\u0016!I\u0011\u0011E\b\u0002\u0002\u0013%\u00111\u0005\u0002\u000b'\u0016\u0014h/[2f\u0017\u0016L(BA\f\u0019\u00035)\u0007\u0010^3s]\u0006dG/\u00192mK*\u0011\u0011DG\u0001\u0006gB\f'o\u001b\u0006\u00037q\t\u0011b\u001a:fK:\u0004H.^7\u000b\u0005uq\u0012a\u00029jm>$\u0018\r\u001c\u0006\u0002?\u0005\u0011\u0011n\\\u0002\u0001'\u0011\u0001!\u0005K\u0016\u0011\u0005\r2S\"\u0001\u0013\u000b\u0003\u0015\nQa]2bY\u0006L!a\n\u0013\u0003\r\u0005s\u0017PU3g!\t\u0019\u0013&\u0003\u0002+I\t9\u0001K]8ek\u000e$\bCA\u0012-\u0013\tiCE\u0001\u0007TKJL\u0017\r\\5{C\ndW-\u0001\u0003q_J$X#\u0001\u0019\u0011\u0007EJDH\u0004\u00023o9\u00111GN\u0007\u0002i)\u0011Q\u0007I\u0001\u0007yI|w\u000e\u001e \n\u0003\u0015J!\u0001\u000f\u0013\u0002\u000fA\f7m[1hK&\u0011!h\u000f\u0002\u0005\u0019&\u001cHO\u0003\u00029IA\u00111%P\u0005\u0003}\u0011\u00121!\u00138u\u0003\u0015\u0001xN\u001d;!\u0003\u0019a\u0014N\\5u}Q\u0011!\t\u0012\t\u0003\u0007\u0002i\u0011A\u0006\u0005\u0006]\r\u0001\r\u0001M\u0001\u0005G>\u0004\u0018\u0010\u0006\u0002C\u000f\"9a\u0006\u0002I\u0001\u0002\u0004\u0001\u0014AD2paf$C-\u001a4bk2$H%M\u000b\u0002\u0015*\u0012\u0001gS\u0016\u0002\u0019B\u0011QJU\u0007\u0002\u001d*\u0011q\nU\u0001\nk:\u001c\u0007.Z2lK\u0012T!!\u0015\u0013\u0002\u0015\u0005tgn\u001c;bi&|g.\u0003\u0002T\u001d\n\tRO\\2iK\u000e\\W\r\u001a,be&\fgnY3\u0002\u001bA\u0014x\u000eZ;diB\u0013XMZ5y+\u00051\u0006CA,]\u001b\u0005A&BA-[\u0003\u0011a\u0017M\\4\u000b\u0003m\u000bAA[1wC&\u0011Q\f\u0017\u0002\u0007'R\u0014\u0018N\\4\u0002\u0019A\u0014x\u000eZ;di\u0006\u0013\u0018\u000e^=\u0016\u0003q\na\u0002\u001d:pIV\u001cG/\u00127f[\u0016tG\u000f\u0006\u0002cKB\u00111eY\u0005\u0003I\u0012\u00121!\u00118z\u0011\u001d1\u0007\"!AA\u0002q\n1\u0001\u001f\u00132\u0003=\u0001(o\u001c3vGRLE/\u001a:bi>\u0014X#A5\u0011\u0007)l'-D\u0001l\u0015\taG%\u0001\u0006d_2dWm\u0019;j_:L!A\\6\u0003\u0011%#XM]1u_J\f\u0001bY1o\u000bF,\u0018\r\u001c\u000b\u0003cR\u0004\"a\t:\n\u0005M$#a\u0002\"p_2,\u0017M\u001c\u0005\bM*\t\t\u00111\u0001c\u0003!A\u0017m\u001d5D_\u0012,G#\u0001\u001f\u0002\u0011Q|7\u000b\u001e:j]\u001e$\u0012AV\u0001\u0007KF,\u0018\r\\:\u0015\u0005E\\\bb\u00024\u000e\u0003\u0003\u0005\rAY\u0001\u000b'\u0016\u0014h/[2f\u0017\u0016L\bCA\"\u0010'\ryqp\u000b\t\u0007\u0003\u0003\t9\u0001\r\"\u000e\u0005\u0005\r!bAA\u0003I\u00059!/\u001e8uS6,\u0017\u0002BA\u0005\u0003\u0007\u0011\u0011#\u00112tiJ\f7\r\u001e$v]\u000e$\u0018n\u001c82)\u0005i\u0018!B1qa2LHc\u0001\"\u0002\u0012!)aF\u0005a\u0001a\u00059QO\\1qa2LH\u0003BA\f\u0003;\u0001BaIA\ra%\u0019\u00111\u0004\u0013\u0003\r=\u0003H/[8o\u0011!\tybEA\u0001\u0002\u0004\u0011\u0015a\u0001=%a\u0005Y!/Z1e%\u0016\u001cx\u000e\u001c<f)\t\t)\u0003E\u0002X\u0003OI1!!\u000bY\u0005\u0019y%M[3di\u0002")
public class ServiceKey
implements Product,
Serializable {
    private final List<Object> port;

    public static Option<List<Object>> unapply(ServiceKey serviceKey) {
        return ServiceKey$.MODULE$.unapply(serviceKey);
    }

    public static ServiceKey apply(List<Object> list) {
        return ServiceKey$.MODULE$.apply(list);
    }

    public static <A> Function1<List<Object>, A> andThen(Function1<ServiceKey, A> function1) {
        return ServiceKey$.MODULE$.andThen(function1);
    }

    public static <A> Function1<A, ServiceKey> compose(Function1<A, List<Object>> function1) {
        return ServiceKey$.MODULE$.compose(function1);
    }

    public List<Object> port() {
        return this.port;
    }

    public ServiceKey copy(List<Object> port) {
        return new ServiceKey(port);
    }

    public List<Object> copy$default$1() {
        return this.port();
    }

    public String productPrefix() {
        return "ServiceKey";
    }

    public int productArity() {
        return 1;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.port();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof ServiceKey;
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
        if (!(object instanceof ServiceKey)) return false;
        boolean bl = true;
        if (!bl) return false;
        ServiceKey serviceKey = (ServiceKey)x$1;
        List<Object> list = this.port();
        List<Object> list2 = serviceKey.port();
        if (list == null) {
            if (list2 != null) {
                return false;
            }
        } else if (!list.equals(list2)) return false;
        if (!serviceKey.canEqual(this)) return false;
        return true;
    }

    public ServiceKey(List<Object> port) {
        this.port = port;
        Product.$init$((Product)this);
    }
}

