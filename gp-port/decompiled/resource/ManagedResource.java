/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Predef$$less$colon$less
 *  scala.Tuple2
 *  scala.collection.Traversable
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.List
 *  scala.concurrent.ExecutionContext
 *  scala.concurrent.Future
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 */
package resource;

import resource.ExtractableManagedResource;
import resource.ExtractedEither;
import scala.Function1;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.Traversable;
import scala.collection.TraversableOnce;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;

@ScalaSignature(bytes="\u0006\u0001\u0005=baB\u0001\u0003!\u0003\r\n!\u0002\u0002\u0010\u001b\u0006t\u0017mZ3e%\u0016\u001cx.\u001e:dK*\t1!\u0001\u0005sKN|WO]2f\u0007\u0001)\"A\u0002\u0014\u0014\u0005\u00019\u0001C\u0001\u0005\f\u001b\u0005I!\"\u0001\u0006\u0002\u000bM\u001c\u0017\r\\1\n\u00051I!AB!osJ+g\rC\u0003\u000f\u0001\u0019\u0005q\"A\u0002nCB,\"\u0001E\f\u0015\u0005E\u0001\u0003c\u0001\n\u0014+5\t!!\u0003\u0002\u0015\u0005\tQR\t\u001f;sC\u000e$\u0018M\u00197f\u001b\u0006t\u0017mZ3e%\u0016\u001cx.\u001e:dKB\u0011ac\u0006\u0007\u0001\t\u0015ARB1\u0001\u001a\u0005\u0005\u0011\u0015C\u0001\u000e\u001e!\tA1$\u0003\u0002\u001d\u0013\t9aj\u001c;iS:<\u0007C\u0001\u0005\u001f\u0013\ty\u0012BA\u0002B]fDQ!I\u0007A\u0002\t\n\u0011A\u001a\t\u0005\u0011\r*S#\u0003\u0002%\u0013\tIa)\u001e8di&|g.\r\t\u0003-\u0019\"aa\n\u0001\u0005\u0006\u0004I\"!\u0001*\t\u000b%\u0002a\u0011\u0001\u0016\u0002\u000f\u0019d\u0017\r^'baV\u00111F\f\u000b\u0003Y=\u00022A\u0005\u0001.!\t1b\u0006B\u0003\u0019Q\t\u0007\u0011\u0004C\u0003\"Q\u0001\u0007\u0001\u0007\u0005\u0003\tG\u0015b\u0003\"\u0002\u001a\u0001\r\u0003\u0019\u0014a\u00024pe\u0016\f7\r\u001b\u000b\u0003i]\u0002\"\u0001C\u001b\n\u0005YJ!\u0001B+oSRDQ!I\u0019A\u0002a\u0002B\u0001C\u0012&i!)!\b\u0001D\u0001w\u0005i\u0011mY9vSJ,\u0017I\u001c3HKR,\"\u0001\u0010 \u0015\u0005uz\u0004C\u0001\f?\t\u0015A\u0012H1\u0001\u001a\u0011\u0015\t\u0013\b1\u0001A!\u0011A1%J\u001f\t\u000b\t\u0003a\u0011A\"\u0002\u000b\u0005\u0004\b\u000f\\=\u0016\u0005\u00113ECA#H!\t1b\tB\u0003\u0019\u0003\n\u0007\u0011\u0004C\u0003\"\u0003\u0002\u0007\u0001\n\u0005\u0003\tG\u0015*\u0005\"\u0002&\u0001\r\u0003Y\u0015AC1dcVL'/\u001a$peV\u0011A\n\u0019\u000b\u0003\u001b\u0006\u0004BA\u0005(Q?&\u0011qJ\u0001\u0002\u0010\u000bb$(/Y2uK\u0012,\u0015\u000e\u001e5feB\u0019\u0011+\u0017/\u000f\u0005I;fBA*W\u001b\u0005!&BA+\u0005\u0003\u0019a$o\\8u}%\t!\"\u0003\u0002Y\u0013\u00059\u0001/Y2lC\u001e,\u0017B\u0001.\\\u0005\u0011a\u0015n\u001d;\u000b\u0005aK\u0001CA)^\u0013\tq6LA\u0005UQJ|w/\u00192mKB\u0011a\u0003\u0019\u0003\u00061%\u0013\r!\u0007\u0005\u0006C%\u0003\rA\u0019\t\u0005\u0011\r*s\fC\u0003e\u0001\u0019\u0005Q-A\u0007u_R\u0013\u0018M^3sg\u0006\u0014G.Z\u000b\u0003M:$\"aZ8\u0011\u0007!\\W.D\u0001j\u0015\tQ\u0017\"\u0001\u0006d_2dWm\u0019;j_:L!\u0001\\5\u0003\u0017Q\u0013\u0018M^3sg\u0006\u0014G.\u001a\t\u0003-9$Q\u0001G2C\u0002eAQ\u0001]2A\u0004E\f!!\u001a<\u0011\tI4X%\u001f\b\u0003gR\u0004\"aU\u0005\n\u0005UL\u0011A\u0002)sK\u0012,g-\u0003\u0002xq\n\u0001B\u0005\\3tg\u0012\u001aw\u000e\\8oI1,7o\u001d\u0006\u0003k&\u00012!\u0015>n\u0013\tY8LA\bUe\u00064XM]:bE2,wJ\\2f\u0011\u0015i\bA\"\u0001\u007f\u0003!!xNR;ukJ,GcA@\u0002\fA)\u0011\u0011AA\u0004K5\u0011\u00111\u0001\u0006\u0004\u0003\u000bI\u0011AC2p]\u000e,(O]3oi&!\u0011\u0011BA\u0002\u0005\u00191U\u000f^;sK\"9\u0011Q\u0002?A\u0004\u0005=\u0011aB2p]R,\u0007\u0010\u001e\t\u0005\u0003\u0003\t\t\"\u0003\u0003\u0002\u0014\u0005\r!\u0001E#yK\u000e,H/[8o\u0007>tG/\u001a=u\u0011\u001d\t9\u0002\u0001D\u0001\u00033\t1!\u00198e+\u0011\tY\"a\n\u0015\t\u0005u\u0011\u0011\u0006\t\u0005%\u0001\ty\u0002\u0005\u0004\t\u0003C)\u0013QE\u0005\u0004\u0003GI!A\u0002+va2,'\u0007E\u0002\u0017\u0003O!a\u0001GA\u000b\u0005\u0004I\u0002\u0002CA\u0016\u0003+\u0001\r!!\f\u0002\tQD\u0017\r\u001e\t\u0005%\u0001\t)\u0003")
public interface ManagedResource<R> {
    public <B> ExtractableManagedResource<B> map(Function1<R, B> var1);

    public <B> ManagedResource<B> flatMap(Function1<R, ManagedResource<B>> var1);

    public void foreach(Function1<R, BoxedUnit> var1);

    public <B> B acquireAndGet(Function1<R, B> var1);

    public <B> B apply(Function1<R, B> var1);

    public <B> ExtractedEither<List<Throwable>, B> acquireFor(Function1<R, B> var1);

    public <B> Traversable<B> toTraversable(Predef$.less.colon.less<R, TraversableOnce<B>> var1);

    public Future<R> toFuture(ExecutionContext var1);

    public <B> ManagedResource<Tuple2<R, B>> and(ManagedResource<B> var1);
}

