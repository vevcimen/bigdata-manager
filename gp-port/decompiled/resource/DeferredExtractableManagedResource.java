/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.Option
 *  scala.Predef$$less$colon$less
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.Traversable
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.List
 *  scala.concurrent.ExecutionContext
 *  scala.concurrent.Future
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.util.Try
 *  scala.util.Try$
 */
package resource;

import java.io.Serializable;
import resource.ExtractableManagedResource;
import resource.ExtractedEither;
import resource.ManagedResource;
import resource.ManagedResourceOperations;
import resource.package$;
import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.Traversable;
import scala.collection.TraversableOnce;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.util.Try;
import scala.util.Try$;

@ScalaSignature(bytes="\u0006\u0001}4Q!\u0001\u0002\u0001\u0005\u0011\u0011!\u0005R3gKJ\u0014X\rZ#yiJ\f7\r^1cY\u0016l\u0015M\\1hK\u0012\u0014Vm]8ve\u000e,'\"A\u0002\u0002\u0011I,7o\\;sG\u0016,2!\u0002\n&'\u0011\u0001a\u0001\u0004\u000f\u0011\u0005\u001dQQ\"\u0001\u0005\u000b\u0003%\tQa]2bY\u0006L!a\u0003\u0005\u0003\r\u0005s\u0017PU3g!\ria\u0002E\u0007\u0002\u0005%\u0011qB\u0001\u0002\u001b\u000bb$(/Y2uC\ndW-T1oC\u001e,GMU3t_V\u00148-\u001a\t\u0003#Ia\u0001\u0001\u0002\u0004\u0014\u0001\u0011\u0015\r!\u0006\u0002\u0002\u0003\u000e\u0001\u0011C\u0001\f\u001a!\t9q#\u0003\u0002\u0019\u0011\t9aj\u001c;iS:<\u0007CA\u0004\u001b\u0013\tY\u0002BA\u0002B]f\u00042!D\u000f\u0011\u0013\tq\"AA\rNC:\fw-\u001a3SKN|WO]2f\u001fB,'/\u0019;j_:\u001c\b\u0002C\u0002\u0001\u0005\u000b\u0007I\u0011\u0001\u0011\u0016\u0003\u0005\u00022!\u0004\u0012%\u0013\t\u0019#AA\bNC:\fw-\u001a3SKN|WO]2f!\t\tR\u0005B\u0003'\u0001\t\u0007QCA\u0001S\u0011!A\u0003A!A!\u0002\u0013\t\u0013!\u0003:fg>,(oY3!\u0011!Q\u0003A!b\u0001\n\u0003Y\u0013!\u0003;sC:\u001cH.\u0019;f+\u0005a\u0003\u0003B\u0004.IAI!A\f\u0005\u0003\u0013\u0019+hn\u0019;j_:\f\u0004\u0002\u0003\u0019\u0001\u0005\u0003\u0005\u000b\u0011\u0002\u0017\u0002\u0015Q\u0014\u0018M\\:mCR,\u0007\u0005C\u00033\u0001\u0011\u00051'\u0001\u0004=S:LGO\u0010\u000b\u0004iU2\u0004\u0003B\u0007\u0001!\u0011BQaA\u0019A\u0002\u0005BQAK\u0019A\u00021BQ\u0001\u000f\u0001\u0005Be\n!\"Y2rk&\u0014XMR8s+\tQd\n\u0006\u0002<!B!Q\u0002\u0010 N\u0013\ti$AA\bFqR\u0014\u0018m\u0019;fI\u0016KG\u000f[3s!\rytI\u0013\b\u0003\u0001\u0016s!!\u0011#\u000e\u0003\tS!a\u0011\u000b\u0002\rq\u0012xn\u001c;?\u0013\u0005I\u0011B\u0001$\t\u0003\u001d\u0001\u0018mY6bO\u0016L!\u0001S%\u0003\t1K7\u000f\u001e\u0006\u0003\r\"\u0001\"aP&\n\u00051K%!\u0003+ie><\u0018M\u00197f!\t\tb\nB\u0003Po\t\u0007QCA\u0001C\u0011\u0015\tv\u00071\u0001S\u0003\u00051\u0007\u0003B\u0004.!5CQ\u0001\u0016\u0001\u0005BU\u000ba!Z5uQ\u0016\u0014X#\u0001,\u0011\t5ad\b\u0005\u0005\u00061\u0002!\t%W\u0001\u0004_B$X#\u0001.\u0011\u0007\u001dY\u0006#\u0003\u0002]\u0011\t1q\n\u001d;j_:DQA\u0018\u0001\u0005B}\u000bQ\u0001\u001e:jK\u0012,\u0012\u0001\u0019\t\u0004C\u0012\u0004R\"\u00012\u000b\u0005\rD\u0011\u0001B;uS2L!!\u001a2\u0003\u0007Q\u0013\u0018\u0010C\u0003h\u0001\u0011\u0005\u0003.\u0001\u0004fcV\fGn\u001d\u000b\u0003S2\u0004\"a\u00026\n\u0005-D!a\u0002\"p_2,\u0017M\u001c\u0005\u0006[\u001a\u0004\r!G\u0001\u0005i\"\fG\u000fC\u0003p\u0001\u0011\u0005\u0003/\u0001\u0005iCND7i\u001c3f)\u0005\t\bCA\u0004s\u0013\t\u0019\bBA\u0002J]RDQ!\u001e\u0001\u0005BY\f\u0001\u0002^8TiJLgn\u001a\u000b\u0002oB\u0011\u00010`\u0007\u0002s*\u0011!p_\u0001\u0005Y\u0006twMC\u0001}\u0003\u0011Q\u0017M^1\n\u0005yL(AB*ue&tw\r")
public class DeferredExtractableManagedResource<A, R>
implements ExtractableManagedResource<A>,
ManagedResourceOperations<A> {
    private final ManagedResource<R> resource;
    private final Function1<R, A> translate;

    @Override
    public <B> B acquireAndGet(Function1<A, B> f) {
        return (B)ManagedResourceOperations.acquireAndGet$(this, f);
    }

    @Override
    public <B> B apply(Function1<A, B> f) {
        return (B)ManagedResourceOperations.apply$(this, f);
    }

    @Override
    public <B> Traversable<B> toTraversable(Predef$.less.colon.less<A, TraversableOnce<B>> ev) {
        return ManagedResourceOperations.toTraversable$(this, ev);
    }

    @Override
    public Future<A> toFuture(ExecutionContext context) {
        return ManagedResourceOperations.toFuture$(this, context);
    }

    @Override
    public <B> ExtractableManagedResource<B> map(Function1<A, B> f) {
        return ManagedResourceOperations.map$(this, f);
    }

    @Override
    public <B> ManagedResource<B> flatMap(Function1<A, ManagedResource<B>> f) {
        return ManagedResourceOperations.flatMap$(this, f);
    }

    @Override
    public void foreach(Function1<A, BoxedUnit> f) {
        ManagedResourceOperations.foreach$(this, f);
    }

    @Override
    public <B> ManagedResource<Tuple2<A, B>> and(ManagedResource<B> that) {
        return ManagedResourceOperations.and$(this, that);
    }

    public ManagedResource<R> resource() {
        return this.resource;
    }

    public Function1<R, A> translate() {
        return this.translate;
    }

    @Override
    public <B> ExtractedEither<List<Throwable>, B> acquireFor(Function1<A, B> f) {
        return this.resource().acquireFor(this.translate().andThen(f));
    }

    @Override
    public ExtractedEither<List<Throwable>, A> either() {
        return new ExtractedEither<List<Throwable>, A>(package$.MODULE$.extractedEitherToEither(this.resource().acquireFor(this.translate())));
    }

    @Override
    public Option<A> opt() {
        return this.either().either().right().toOption();
    }

    @Override
    public Try<A> tried() {
        return Try$.MODULE$.apply((Function0 & Serializable & scala.Serializable)() -> this.resource().apply(this.translate()));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object that) {
        Object object = that;
        if (!(object instanceof DeferredExtractableManagedResource)) return false;
        DeferredExtractableManagedResource deferredExtractableManagedResource = (DeferredExtractableManagedResource)object;
        ManagedResource<R> managedResource = deferredExtractableManagedResource.resource();
        ManagedResource<R> managedResource2 = this.resource();
        if (managedResource == null) {
            if (managedResource2 != null) {
                return false;
            }
        } else if (!managedResource.equals(managedResource2)) return false;
        Function1<R, A> function1 = deferredExtractableManagedResource.translate();
        Function1<R, A> function12 = this.translate();
        if (function1 == null) {
            if (function12 == null) return true;
            return false;
        } else {
            if (!function1.equals(function12)) return false;
            return true;
        }
    }

    public int hashCode() {
        return (this.resource().hashCode() << 7) + this.translate().hashCode() + 13;
    }

    public String toString() {
        return "DeferredExtractableManagedResource(" + this.resource() + ", " + this.translate() + ")";
    }

    public DeferredExtractableManagedResource(ManagedResource<R> resource, Function1<R, A> translate) {
        this.resource = resource;
        this.translate = translate;
        ManagedResourceOperations.$init$(this);
    }
}

