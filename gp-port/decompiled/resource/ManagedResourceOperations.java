/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.Option
 *  scala.PartialFunction
 *  scala.Predef$
 *  scala.Predef$$less$colon$less
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.Tuple3
 *  scala.collection.GenTraversable
 *  scala.collection.GenTraversableOnce
 *  scala.collection.Iterable
 *  scala.collection.Iterator
 *  scala.collection.Parallel
 *  scala.collection.Parallelizable
 *  scala.collection.Seq
 *  scala.collection.Traversable
 *  scala.collection.TraversableLike
 *  scala.collection.TraversableOnce
 *  scala.collection.TraversableView
 *  scala.collection.generic.CanBuildFrom
 *  scala.collection.generic.FilterMonadic
 *  scala.collection.generic.GenericCompanion
 *  scala.collection.generic.GenericTraversableTemplate
 *  scala.collection.immutable.IndexedSeq
 *  scala.collection.immutable.List
 *  scala.collection.immutable.Map
 *  scala.collection.immutable.Set
 *  scala.collection.immutable.Stream
 *  scala.collection.immutable.Vector
 *  scala.collection.mutable.Buffer
 *  scala.collection.mutable.Builder
 *  scala.collection.mutable.StringBuilder
 *  scala.collection.parallel.Combiner
 *  scala.collection.parallel.ParIterable
 *  scala.concurrent.ExecutionContext
 *  scala.concurrent.Future
 *  scala.concurrent.Future$
 *  scala.math.Numeric
 *  scala.math.Ordering
 *  scala.package$
 *  scala.reflect.ClassTag
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.LambdaDeserialize
 *  scala.runtime.Nothing$
 */
package resource;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.SerializedLambda;
import resource.DeferredExtractableManagedResource;
import resource.ExtractableManagedResource;
import resource.ExtractedEither;
import resource.ManagedResource;
import resource.ManagedTraversable;
import resource.package$;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.Option;
import scala.PartialFunction;
import scala.Predef$;
import scala.Tuple2;
import scala.Tuple3;
import scala.collection.GenTraversable;
import scala.collection.GenTraversableOnce;
import scala.collection.Iterable;
import scala.collection.Iterator;
import scala.collection.Parallel;
import scala.collection.Parallelizable;
import scala.collection.Seq;
import scala.collection.Traversable;
import scala.collection.TraversableLike;
import scala.collection.TraversableOnce;
import scala.collection.TraversableView;
import scala.collection.generic.CanBuildFrom;
import scala.collection.generic.FilterMonadic;
import scala.collection.generic.GenericCompanion;
import scala.collection.generic.GenericTraversableTemplate;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.collection.immutable.Set;
import scala.collection.immutable.Stream;
import scala.collection.immutable.Vector;
import scala.collection.mutable.Buffer;
import scala.collection.mutable.Builder;
import scala.collection.mutable.StringBuilder;
import scala.collection.parallel.Combiner;
import scala.collection.parallel.ParIterable;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Future$;
import scala.math.Numeric;
import scala.math.Ordering;
import scala.reflect.ClassTag;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.LambdaDeserialize;
import scala.runtime.Nothing$;

@ScalaSignature(bytes="\u0006\u0001\u00055aaB\u0001\u0003!\u0003\r\t!\u0002\u0002\u001a\u001b\u0006t\u0017mZ3e%\u0016\u001cx.\u001e:dK>\u0003XM]1uS>t7OC\u0001\u0004\u0003!\u0011Xm]8ve\u000e,7\u0001A\u000b\u0003\rM\u00192\u0001A\u0004\u000e!\tA1\"D\u0001\n\u0015\u0005Q\u0011!B:dC2\f\u0017B\u0001\u0007\n\u0005\u0019\te.\u001f*fMB\u0019abD\t\u000e\u0003\tI!\u0001\u0005\u0002\u0003\u001f5\u000bg.Y4fIJ+7o\\;sG\u0016\u0004\"AE\n\r\u0001\u00111A\u0003\u0001CC\u0002U\u0011\u0011AU\t\u0003-e\u0001\"\u0001C\f\n\u0005aI!a\u0002(pi\"Lgn\u001a\t\u0003\u0011iI!aG\u0005\u0003\u0007\u0005s\u0017\u0010C\u0003\u001e\u0001\u0011\u0005a$\u0001\u0004%S:LG\u000f\n\u000b\u0002?A\u0011\u0001\u0002I\u0005\u0003C%\u0011A!\u00168ji\")1\u0005\u0001C!I\u0005i\u0011mY9vSJ,\u0017I\u001c3HKR,\"!J\u0014\u0015\u0005\u0019J\u0003C\u0001\n(\t\u0015A#E1\u0001\u0016\u0005\u0005\u0011\u0005\"\u0002\u0016#\u0001\u0004Y\u0013!\u00014\u0011\t!a\u0013CJ\u0005\u0003[%\u0011\u0011BR;oGRLwN\\\u0019\t\u000b=\u0002A\u0011\t\u0019\u0002\u000b\u0005\u0004\b\u000f\\=\u0016\u0005E\u001aDC\u0001\u001a5!\t\u00112\u0007B\u0003)]\t\u0007Q\u0003C\u0003+]\u0001\u0007Q\u0007\u0005\u0003\tYE\u0011\u0004\"B\u001c\u0001\t\u0003B\u0014!\u0004;p)J\fg/\u001a:tC\ndW-\u0006\u0002:\u0003R\u0011!H\u0011\t\u0004wy\u0002U\"\u0001\u001f\u000b\u0005uJ\u0011AC2pY2,7\r^5p]&\u0011q\b\u0010\u0002\f)J\fg/\u001a:tC\ndW\r\u0005\u0002\u0013\u0003\u0012)\u0001F\u000eb\u0001+!)1I\u000ea\u0002\t\u0006\u0011QM\u001e\t\u0005\u000b2\u000brJ\u0004\u0002G\u0015B\u0011q)C\u0007\u0002\u0011*\u0011\u0011\nB\u0001\u0007yI|w\u000e\u001e \n\u0005-K\u0011A\u0002)sK\u0012,g-\u0003\u0002N\u001d\n\u0001B\u0005\\3tg\u0012\u001aw\u000e\\8oI1,7o\u001d\u0006\u0003\u0017&\u00012a\u000f)A\u0013\t\tFHA\bUe\u00064XM]:bE2,wJ\\2f\u0011\u0015\u0019\u0006\u0001\"\u0011U\u0003!!xNR;ukJ,GCA+\\!\r1\u0016,E\u0007\u0002/*\u0011\u0001,C\u0001\u000bG>t7-\u001e:sK:$\u0018B\u0001.X\u0005\u00191U\u000f^;sK\")AL\u0015a\u0002;\u000691m\u001c8uKb$\bC\u0001,_\u0013\tyvK\u0001\tFq\u0016\u001cW\u000f^5p]\u000e{g\u000e^3yi\")\u0011\r\u0001C!E\u0006\u0019Q.\u00199\u0016\u0005\rDGC\u00013j!\rqQmZ\u0005\u0003M\n\u0011!$\u0012=ue\u0006\u001cG/\u00192mK6\u000bg.Y4fIJ+7o\\;sG\u0016\u0004\"A\u00055\u0005\u000b!\u0002'\u0019A\u000b\t\u000b)\u0002\u0007\u0019\u00016\u0011\t!a\u0013c\u001a\u0005\u0006Y\u0002!\t%\\\u0001\bM2\fG/T1q+\tq\u0017\u000f\u0006\u0002peB\u0019ab\u00049\u0011\u0005I\tH!\u0002\u0015l\u0005\u0004)\u0002\"\u0002\u0016l\u0001\u0004\u0019\b\u0003\u0002\u0005-#=DQ!\u001e\u0001\u0005BY\fqAZ8sK\u0006\u001c\u0007\u000e\u0006\u0002 o\")!\u0006\u001ea\u0001qB!\u0001\u0002L\t \u0011\u0015Q\b\u0001\"\u0011|\u0003\r\tg\u000eZ\u000b\u0004y\u0006\u0015AcA?\u0002\bA\u0019ab\u0004@\u0011\u000b!y\u0018#a\u0001\n\u0007\u0005\u0005\u0011B\u0001\u0004UkBdWM\r\t\u0004%\u0005\u0015A!\u0002\u0015z\u0005\u0004)\u0002bBA\u0005s\u0002\u0007\u00111B\u0001\u0005i\"\fG\u000f\u0005\u0003\u000f\u001f\u0005\r\u0001")
public interface ManagedResourceOperations<R>
extends ManagedResource<R> {
    public static /* synthetic */ Object acquireAndGet$(ManagedResourceOperations $this, Function1 f) {
        return $this.acquireAndGet(f);
    }

    @Override
    default public <B> B acquireAndGet(Function1<R, B> f) {
        return this.apply(f);
    }

    public static /* synthetic */ Object apply$(ManagedResourceOperations $this, Function1 f) {
        return $this.apply(f);
    }

    @Override
    default public <B> B apply(Function1<R, B> f) {
        return (B)package$.MODULE$.extractedEitherToEither(this.acquireFor(f)).fold((Function1 & Serializable & scala.Serializable)liste -> {
            throw (Throwable)liste.reduce((Function2 & Serializable & scala.Serializable)(prev, next) -> {
                prev.addSuppressed((Throwable)next);
                return prev;
            });
        }, (Function1 & Serializable & scala.Serializable)x -> x);
    }

    public static /* synthetic */ Traversable toTraversable$(ManagedResourceOperations $this, Predef$.less.colon.less ev) {
        return $this.toTraversable(ev);
    }

    @Override
    default public <B> Traversable<B> toTraversable(Predef$.less.colon.less<R, TraversableOnce<B>> ev) {
        return new ManagedTraversable<B, R>(this, ev){
            private final ManagedResourceOperations<R> resource;
            private final Predef$.less.colon.less ev$1;

            public boolean ignoreError(Exception error) {
                return ManagedTraversable.ignoreError$(this, error);
            }

            public void handleErrorsDuringTraversal(List<Throwable> ex) {
                ManagedTraversable.handleErrorsDuringTraversal$(this, ex);
            }

            public <U> void foreach(Function1<B, U> f) {
                ManagedTraversable.foreach$(this, f);
            }

            public String toString() {
                return ManagedTraversable.toString$(this);
            }

            public GenericCompanion<Traversable> companion() {
                return Traversable.companion$((Traversable)this);
            }

            public Traversable<B> seq() {
                return Traversable.seq$((Traversable)this);
            }

            public Builder<B, Traversable<B>> newBuilder() {
                return GenericTraversableTemplate.newBuilder$((GenericTraversableTemplate)this);
            }

            public <B> Builder<B, Traversable<B>> genericBuilder() {
                return GenericTraversableTemplate.genericBuilder$((GenericTraversableTemplate)this);
            }

            public <A1, A2> Tuple2<Traversable<A1>, Traversable<A2>> unzip(Function1<B, Tuple2<A1, A2>> asPair) {
                return GenericTraversableTemplate.unzip$((GenericTraversableTemplate)this, asPair);
            }

            public <A1, A2, A3> Tuple3<Traversable<A1>, Traversable<A2>, Traversable<A3>> unzip3(Function1<B, Tuple3<A1, A2, A3>> asTriple) {
                return GenericTraversableTemplate.unzip3$((GenericTraversableTemplate)this, asTriple);
            }

            public GenTraversable flatten(Function1 asTraversable) {
                return GenericTraversableTemplate.flatten$((GenericTraversableTemplate)this, (Function1)asTraversable);
            }

            public GenTraversable transpose(Function1 asTraversable) {
                return GenericTraversableTemplate.transpose$((GenericTraversableTemplate)this, (Function1)asTraversable);
            }

            public Object repr() {
                return TraversableLike.repr$((TraversableLike)this);
            }

            public final boolean isTraversableAgain() {
                return TraversableLike.isTraversableAgain$((TraversableLike)this);
            }

            public Traversable<B> thisCollection() {
                return TraversableLike.thisCollection$((TraversableLike)this);
            }

            public Traversable toCollection(Object repr) {
                return TraversableLike.toCollection$((TraversableLike)this, (Object)repr);
            }

            public Combiner<B, ParIterable<B>> parCombiner() {
                return TraversableLike.parCombiner$((TraversableLike)this);
            }

            public boolean isEmpty() {
                return TraversableLike.isEmpty$((TraversableLike)this);
            }

            public boolean hasDefiniteSize() {
                return TraversableLike.hasDefiniteSize$((TraversableLike)this);
            }

            public <B, That> That $plus$plus(GenTraversableOnce<B> that, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.$plus$plus$((TraversableLike)this, that, bf);
            }

            public <B, That> That $plus$plus$colon(TraversableOnce<B> that, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.$plus$plus$colon$((TraversableLike)this, that, bf);
            }

            public <B, That> That $plus$plus$colon(Traversable<B> that, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.$plus$plus$colon$((TraversableLike)this, that, bf);
            }

            public <B, That> That map(Function1<B, B> f, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.map$((TraversableLike)this, f, bf);
            }

            public <B, That> That flatMap(Function1<B, GenTraversableOnce<B>> f, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.flatMap$((TraversableLike)this, f, bf);
            }

            public Object filterImpl(Function1 p, boolean isFlipped) {
                return TraversableLike.filterImpl$((TraversableLike)this, (Function1)p, (boolean)isFlipped);
            }

            public Object filter(Function1 p) {
                return TraversableLike.filter$((TraversableLike)this, (Function1)p);
            }

            public Object filterNot(Function1 p) {
                return TraversableLike.filterNot$((TraversableLike)this, (Function1)p);
            }

            public <B, That> That collect(PartialFunction<B, B> pf, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.collect$((TraversableLike)this, pf, bf);
            }

            public Tuple2<Traversable<B>, Traversable<B>> partition(Function1<B, Object> p) {
                return TraversableLike.partition$((TraversableLike)this, p);
            }

            public <K> Map<K, Traversable<B>> groupBy(Function1<B, K> f) {
                return TraversableLike.groupBy$((TraversableLike)this, f);
            }

            public boolean forall(Function1<B, Object> p) {
                return TraversableLike.forall$((TraversableLike)this, p);
            }

            public boolean exists(Function1<B, Object> p) {
                return TraversableLike.exists$((TraversableLike)this, p);
            }

            public Option<B> find(Function1<B, Object> p) {
                return TraversableLike.find$((TraversableLike)this, p);
            }

            public <B, That> That scan(B z, Function2<B, B, B> op, CanBuildFrom<Traversable<B>, B, That> cbf) {
                return (That)TraversableLike.scan$((TraversableLike)this, z, op, cbf);
            }

            public <B, That> That scanLeft(B z, Function2<B, B, B> op, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.scanLeft$((TraversableLike)this, z, op, bf);
            }

            public <B, That> That scanRight(B z, Function2<B, B, B> op, CanBuildFrom<Traversable<B>, B, That> bf) {
                return (That)TraversableLike.scanRight$((TraversableLike)this, z, op, bf);
            }

            public B head() {
                return (B)TraversableLike.head$((TraversableLike)this);
            }

            public Option<B> headOption() {
                return TraversableLike.headOption$((TraversableLike)this);
            }

            public Object tail() {
                return TraversableLike.tail$((TraversableLike)this);
            }

            public B last() {
                return (B)TraversableLike.last$((TraversableLike)this);
            }

            public Option<B> lastOption() {
                return TraversableLike.lastOption$((TraversableLike)this);
            }

            public Object init() {
                return TraversableLike.init$((TraversableLike)this);
            }

            public Object take(int n) {
                return TraversableLike.take$((TraversableLike)this, (int)n);
            }

            public Object drop(int n) {
                return TraversableLike.drop$((TraversableLike)this, (int)n);
            }

            public Object slice(int from, int until) {
                return TraversableLike.slice$((TraversableLike)this, (int)from, (int)until);
            }

            public Object sliceWithKnownDelta(int from, int until, int delta) {
                return TraversableLike.sliceWithKnownDelta$((TraversableLike)this, (int)from, (int)until, (int)delta);
            }

            public Object sliceWithKnownBound(int from, int until) {
                return TraversableLike.sliceWithKnownBound$((TraversableLike)this, (int)from, (int)until);
            }

            public Object takeWhile(Function1 p) {
                return TraversableLike.takeWhile$((TraversableLike)this, (Function1)p);
            }

            public Object dropWhile(Function1 p) {
                return TraversableLike.dropWhile$((TraversableLike)this, (Function1)p);
            }

            public Tuple2<Traversable<B>, Traversable<B>> span(Function1<B, Object> p) {
                return TraversableLike.span$((TraversableLike)this, p);
            }

            public Tuple2<Traversable<B>, Traversable<B>> splitAt(int n) {
                return TraversableLike.splitAt$((TraversableLike)this, (int)n);
            }

            public Iterator<Traversable<B>> tails() {
                return TraversableLike.tails$((TraversableLike)this);
            }

            public Iterator<Traversable<B>> inits() {
                return TraversableLike.inits$((TraversableLike)this);
            }

            public <B> void copyToArray(Object xs, int start, int len) {
                TraversableLike.copyToArray$((TraversableLike)this, (Object)xs, (int)start, (int)len);
            }

            public Traversable<B> toTraversable() {
                return TraversableLike.toTraversable$((TraversableLike)this);
            }

            public Iterator<B> toIterator() {
                return TraversableLike.toIterator$((TraversableLike)this);
            }

            public Stream<B> toStream() {
                return TraversableLike.toStream$((TraversableLike)this);
            }

            public <Col> Col to(CanBuildFrom<Nothing$, B, Col> cbf) {
                return (Col)TraversableLike.to$((TraversableLike)this, cbf);
            }

            public String stringPrefix() {
                return TraversableLike.stringPrefix$((TraversableLike)this);
            }

            public TraversableView<B, Traversable<B>> view() {
                return TraversableLike.view$((TraversableLike)this);
            }

            public TraversableView<B, Traversable<B>> view(int from, int until) {
                return TraversableLike.view$((TraversableLike)this, (int)from, (int)until);
            }

            public FilterMonadic<B, Traversable<B>> withFilter(Function1<B, Object> p) {
                return TraversableLike.withFilter$((TraversableLike)this, p);
            }

            public Parallel par() {
                return Parallelizable.par$((Parallelizable)this);
            }

            public List<B> reversed() {
                return TraversableOnce.reversed$((TraversableOnce)this);
            }

            public int size() {
                return TraversableOnce.size$((TraversableOnce)this);
            }

            public boolean nonEmpty() {
                return TraversableOnce.nonEmpty$((TraversableOnce)this);
            }

            public int count(Function1<B, Object> p) {
                return TraversableOnce.count$((TraversableOnce)this, p);
            }

            public <B> Option<B> collectFirst(PartialFunction<B, B> pf) {
                return TraversableOnce.collectFirst$((TraversableOnce)this, pf);
            }

            public <B> B $div$colon(B z, Function2<B, B, B> op) {
                return (B)TraversableOnce.$div$colon$((TraversableOnce)this, z, op);
            }

            public <B> B $colon$bslash(B z, Function2<B, B, B> op) {
                return (B)TraversableOnce.$colon$bslash$((TraversableOnce)this, z, op);
            }

            public <B> B foldLeft(B z, Function2<B, B, B> op) {
                return (B)TraversableOnce.foldLeft$((TraversableOnce)this, z, op);
            }

            public <B> B foldRight(B z, Function2<B, B, B> op) {
                return (B)TraversableOnce.foldRight$((TraversableOnce)this, z, op);
            }

            public <B> B reduceLeft(Function2<B, B, B> op) {
                return (B)TraversableOnce.reduceLeft$((TraversableOnce)this, op);
            }

            public <B> B reduceRight(Function2<B, B, B> op) {
                return (B)TraversableOnce.reduceRight$((TraversableOnce)this, op);
            }

            public <B> Option<B> reduceLeftOption(Function2<B, B, B> op) {
                return TraversableOnce.reduceLeftOption$((TraversableOnce)this, op);
            }

            public <B> Option<B> reduceRightOption(Function2<B, B, B> op) {
                return TraversableOnce.reduceRightOption$((TraversableOnce)this, op);
            }

            public <A1> A1 reduce(Function2<A1, A1, A1> op) {
                return (A1)TraversableOnce.reduce$((TraversableOnce)this, op);
            }

            public <A1> Option<A1> reduceOption(Function2<A1, A1, A1> op) {
                return TraversableOnce.reduceOption$((TraversableOnce)this, op);
            }

            public <A1> A1 fold(A1 z, Function2<A1, A1, A1> op) {
                return (A1)TraversableOnce.fold$((TraversableOnce)this, z, op);
            }

            public <B> B aggregate(Function0<B> z, Function2<B, B, B> seqop, Function2<B, B, B> combop) {
                return (B)TraversableOnce.aggregate$((TraversableOnce)this, z, seqop, combop);
            }

            public <B> B sum(Numeric<B> num) {
                return (B)TraversableOnce.sum$((TraversableOnce)this, num);
            }

            public <B> B product(Numeric<B> num) {
                return (B)TraversableOnce.product$((TraversableOnce)this, num);
            }

            public <B> B min(Ordering<B> cmp) {
                return (B)TraversableOnce.min$((TraversableOnce)this, cmp);
            }

            public <B> B max(Ordering<B> cmp) {
                return (B)TraversableOnce.max$((TraversableOnce)this, cmp);
            }

            public <B> B maxBy(Function1<B, B> f, Ordering<B> cmp) {
                return (B)TraversableOnce.maxBy$((TraversableOnce)this, f, cmp);
            }

            public <B> B minBy(Function1<B, B> f, Ordering<B> cmp) {
                return (B)TraversableOnce.minBy$((TraversableOnce)this, f, cmp);
            }

            public <B> void copyToBuffer(Buffer<B> dest) {
                TraversableOnce.copyToBuffer$((TraversableOnce)this, dest);
            }

            public <B> void copyToArray(Object xs, int start) {
                TraversableOnce.copyToArray$((TraversableOnce)this, (Object)xs, (int)start);
            }

            public <B> void copyToArray(Object xs) {
                TraversableOnce.copyToArray$((TraversableOnce)this, (Object)xs);
            }

            public <B> Object toArray(ClassTag<B> evidence$1) {
                return TraversableOnce.toArray$((TraversableOnce)this, evidence$1);
            }

            public List<B> toList() {
                return TraversableOnce.toList$((TraversableOnce)this);
            }

            public Iterable<B> toIterable() {
                return TraversableOnce.toIterable$((TraversableOnce)this);
            }

            public Seq<B> toSeq() {
                return TraversableOnce.toSeq$((TraversableOnce)this);
            }

            public IndexedSeq<B> toIndexedSeq() {
                return TraversableOnce.toIndexedSeq$((TraversableOnce)this);
            }

            public <B> Buffer<B> toBuffer() {
                return TraversableOnce.toBuffer$((TraversableOnce)this);
            }

            public <B> Set<B> toSet() {
                return TraversableOnce.toSet$((TraversableOnce)this);
            }

            public Vector<B> toVector() {
                return TraversableOnce.toVector$((TraversableOnce)this);
            }

            public <T, U> Map<T, U> toMap(Predef$.less.colon.less<B, Tuple2<T, U>> ev) {
                return TraversableOnce.toMap$((TraversableOnce)this, ev);
            }

            public String mkString(String start, String sep, String end) {
                return TraversableOnce.mkString$((TraversableOnce)this, (String)start, (String)sep, (String)end);
            }

            public String mkString(String sep) {
                return TraversableOnce.mkString$((TraversableOnce)this, (String)sep);
            }

            public String mkString() {
                return TraversableOnce.mkString$((TraversableOnce)this);
            }

            public StringBuilder addString(StringBuilder b, String start, String sep, String end) {
                return TraversableOnce.addString$((TraversableOnce)this, (StringBuilder)b, (String)start, (String)sep, (String)end);
            }

            public StringBuilder addString(StringBuilder b, String sep) {
                return TraversableOnce.addString$((TraversableOnce)this, (StringBuilder)b, (String)sep);
            }

            public StringBuilder addString(StringBuilder b) {
                return TraversableOnce.addString$((TraversableOnce)this, (StringBuilder)b);
            }

            public int sizeHintIfCheap() {
                return GenTraversableOnce.sizeHintIfCheap$((GenTraversableOnce)this);
            }

            public ManagedResourceOperations<R> resource() {
                return this.resource;
            }

            public <U> void internalForeach(R resource, Function1<B, U> g2) {
                ((TraversableOnce)this.ev$1.apply(resource)).foreach(g2);
            }
            {
                this.ev$1 = ev$1;
                GenTraversableOnce.$init$((GenTraversableOnce)this);
                TraversableOnce.$init$((TraversableOnce)this);
                Parallelizable.$init$((Parallelizable)this);
                TraversableLike.$init$((TraversableLike)this);
                GenericTraversableTemplate.$init$((GenericTraversableTemplate)this);
                GenTraversable.$init$((GenTraversable)this);
                Traversable.$init$((Traversable)this);
                ManagedTraversable.$init$(this);
                this.resource = $outer;
            }
        };
    }

    public static /* synthetic */ Future toFuture$(ManagedResourceOperations $this, ExecutionContext context) {
        return $this.toFuture(context);
    }

    @Override
    default public Future<R> toFuture(ExecutionContext context) {
        return Future$.MODULE$.apply((Function0 & Serializable & scala.Serializable)() -> this.acquireAndGet((Function1 & Serializable & scala.Serializable)x -> Predef$.MODULE$.identity(x)), context);
    }

    public static /* synthetic */ ExtractableManagedResource map$(ManagedResourceOperations $this, Function1 f) {
        return $this.map(f);
    }

    @Override
    default public <B> ExtractableManagedResource<B> map(Function1<R, B> f) {
        return new DeferredExtractableManagedResource<B, R>(this, f);
    }

    public static /* synthetic */ ManagedResource flatMap$(ManagedResourceOperations $this, Function1 f) {
        return $this.flatMap(f);
    }

    @Override
    default public <B> ManagedResource<B> flatMap(Function1<R, ManagedResource<B>> f) {
        return new ManagedResourceOperations<B>(this, f){
            private final /* synthetic */ ManagedResourceOperations $outer;
            private final Function1 f$1;

            public <B> B acquireAndGet(Function1<B, B> f) {
                return (B)ManagedResourceOperations.acquireAndGet$(this, f);
            }

            public <B> B apply(Function1<B, B> f) {
                return (B)ManagedResourceOperations.apply$(this, f);
            }

            public <B> Traversable<B> toTraversable(Predef$.less.colon.less<B, TraversableOnce<B>> ev) {
                return ManagedResourceOperations.toTraversable$(this, ev);
            }

            public Future<B> toFuture(ExecutionContext context) {
                return ManagedResourceOperations.toFuture$(this, context);
            }

            public <B> ExtractableManagedResource<B> map(Function1<B, B> f) {
                return ManagedResourceOperations.map$(this, f);
            }

            public <B> ManagedResource<B> flatMap(Function1<B, ManagedResource<B>> f) {
                return ManagedResourceOperations.flatMap$(this, f);
            }

            public void foreach(Function1<B, BoxedUnit> f) {
                ManagedResourceOperations.foreach$(this, f);
            }

            public <B> ManagedResource<Tuple2<B, B>> and(ManagedResource<B> that) {
                return ManagedResourceOperations.and$(this, that);
            }

            public <C> ExtractedEither<List<Throwable>, C> acquireFor(Function1<B, C> f2) {
                return (ExtractedEither)package$.MODULE$.extractedEitherToEither(this.$outer.acquireFor((Function1 & Serializable & scala.Serializable)r -> ((ManagedResource)$this.f$1.apply(r)).acquireFor(f2))).fold((Function1 & Serializable & scala.Serializable)x -> new ExtractedEither<A, B>(scala.package$.MODULE$.Left().apply(x)), (Function1 & Serializable & scala.Serializable)x -> x);
            }

            public String toString() {
                return "FlattenedManagedResource[?](...)";
            }
            {
                if ($outer == null) {
                    throw null;
                }
                this.$outer = $outer;
                this.f$1 = f$1;
                ManagedResourceOperations.$init$(this);
            }

            private static /* synthetic */ Object $deserializeLambda$(SerializedLambda serializedLambda) {
                return LambdaDeserialize.bootstrap("lambdaDeserialize", new MethodHandle[]{$anonfun$acquireFor$1(resource.ManagedResourceOperations$$anon$2 scala.Function1 java.lang.Object ), $anonfun$acquireFor$2(scala.collection.immutable.List ), $anonfun$acquireFor$3(resource.ExtractedEither )}, serializedLambda);
            }
        };
    }

    public static /* synthetic */ void foreach$(ManagedResourceOperations $this, Function1 f) {
        $this.foreach(f);
    }

    @Override
    default public void foreach(Function1<R, BoxedUnit> f) {
        this.acquireAndGet(f);
    }

    public static /* synthetic */ ManagedResource and$(ManagedResourceOperations $this, ManagedResource that) {
        return $this.and(that);
    }

    @Override
    default public <B> ManagedResource<Tuple2<R, B>> and(ManagedResource<B> that) {
        return package$.MODULE$.and(this, that);
    }

    public static void $init$(ManagedResourceOperations $this) {
    }
}

