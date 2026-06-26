/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Predef$
 *  scala.Predef$$less$colon$less
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple2
 *  scala.collection.Iterator
 *  scala.collection.Seq
 *  scala.collection.Seq$
 *  scala.collection.SeqLike
 *  scala.collection.Traversable
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.List
 *  scala.concurrent.ExecutionContext
 *  scala.concurrent.Future
 *  scala.reflect.OptManifest
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.LambdaDeserialize
 *  scala.runtime.VolatileObjectRef
 *  scala.util.Either
 */
package resource;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.SerializedLambda;
import resource.ConstantManagedResource;
import resource.DefaultManagedResource;
import resource.ExtractableManagedResource;
import resource.ExtractedEither;
import resource.ManagedResource;
import resource.ManagedResourceOperations;
import resource.Resource;
import resource.package$;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Some;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.Seq$;
import scala.collection.SeqLike;
import scala.collection.Traversable;
import scala.collection.TraversableOnce;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.reflect.OptManifest;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.LambdaDeserialize;
import scala.runtime.VolatileObjectRef;
import scala.util.Either;

public final class package$ {
    public static package$ MODULE$;

    static {
        new package$();
    }

    public <A> ManagedResource<A> managed(Function0<A> opener, Resource<A> evidence$1, OptManifest<A> evidence$2) {
        return new DefaultManagedResource<A>(opener, evidence$1, evidence$2);
    }

    public <V> ManagedResource<V> constant(V value) {
        return new ConstantManagedResource<V>(value);
    }

    public <R> DefaultManagedResource<R> makeManagedResource(Function0<R> opener, Function1<R, BoxedUnit> closer, List<Class<? extends Throwable>> exceptions, OptManifest<R> evidence$3) {
        Resource typeTrait = new Resource<R>(closer, exceptions){
            private final Function1 closer$1;
            private final List exceptions$1;

            public void open(R r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(R r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(R r) {
                this.closer$1.apply(r);
            }

            public boolean isFatalException(Throwable t) {
                return this.exceptions$1.exists((Function1 & Serializable & scala.Serializable)cls -> BoxesRunTime.boxToBoolean((boolean)anon.2.$anonfun$isFatalException$1(t, cls)));
            }

            public static final /* synthetic */ boolean $anonfun$isFatalException$1(Throwable t$1, Class cls) {
                return cls.isAssignableFrom(t$1.getClass());
            }
            {
                this.closer$1 = closer$1;
                this.exceptions$1 = exceptions$1;
                Resource.$init$(this);
            }

            private static /* synthetic */ Object $deserializeLambda$(SerializedLambda serializedLambda) {
                return LambdaDeserialize.bootstrap("lambdaDeserialize", new MethodHandle[]{$anonfun$isFatalException$1$adapted(java.lang.Throwable java.lang.Class )}, serializedLambda);
            }
        };
        return new DefaultManagedResource<R>(opener, typeTrait, evidence$3);
    }

    public <A, B> ManagedResource<Tuple2<A, B>> and(ManagedResource<A> r1, ManagedResource<B> r2) {
        return r1.flatMap((Function1 & Serializable & scala.Serializable)ther1 -> r2.map((Function1 & Serializable & scala.Serializable)ther2 -> new Tuple2(ther1, ther2)));
    }

    public <A, MR, CC> ManagedResource<Seq<A>> join(CC resources, Predef$.less.colon.less<CC, Seq<MR>> ev0, Predef$.less.colon.less<MR, ManagedResource<A>> ev1) {
        Iterator itr = ((SeqLike)ev0.apply(resources)).reverseIterator();
        ManagedResource first = (ManagedResource)ev1.apply(itr.next());
        ManagedResource<Seq<A>> toReturn = first.map((Function1 & Serializable & scala.Serializable)x -> (Seq)Seq$.MODULE$.apply((Seq)Predef$.MODULE$.genericWrapArray((Object)new Object[]{x})));
        while (itr.hasNext()) {
            ExtractableManagedResource<Seq<A>> r1 = toReturn;
            ManagedResource r2 = (ManagedResource)ev1.apply(itr.next());
            toReturn = new ManagedResourceOperations<Seq<A>>(r1, r2){
                private final ManagedResource r1$1;
                private final ManagedResource r2$2;

                public <B> B acquireAndGet(Function1<Seq<A>, B> f) {
                    return (B)ManagedResourceOperations.acquireAndGet$(this, f);
                }

                public <B> B apply(Function1<Seq<A>, B> f) {
                    return (B)ManagedResourceOperations.apply$(this, f);
                }

                public <B> Traversable<B> toTraversable(Predef$.less.colon.less<Seq<A>, TraversableOnce<B>> ev) {
                    return ManagedResourceOperations.toTraversable$(this, ev);
                }

                public Future<Seq<A>> toFuture(ExecutionContext context) {
                    return ManagedResourceOperations.toFuture$(this, context);
                }

                public <B> ExtractableManagedResource<B> map(Function1<Seq<A>, B> f) {
                    return ManagedResourceOperations.map$(this, f);
                }

                public <B> ManagedResource<B> flatMap(Function1<Seq<A>, ManagedResource<B>> f) {
                    return ManagedResourceOperations.flatMap$(this, f);
                }

                public void foreach(Function1<Seq<A>, BoxedUnit> f) {
                    ManagedResourceOperations.foreach$(this, f);
                }

                public <B> ManagedResource<Tuple2<Seq<A>, B>> and(ManagedResource<B> that) {
                    return ManagedResourceOperations.and$(this, that);
                }

                public <B> ExtractedEither<List<Throwable>, B> acquireFor(Function1<Seq<A>, B> f) {
                    return this.r1$1.acquireFor((Function1 & Serializable & scala.Serializable)r1seq -> $this.r2$2.acquireAndGet((Function1 & Serializable & scala.Serializable)r2item -> {
                        Object object = r2item;
                        return f.apply((Object)r1seq.toList().$colon$colon(object));
                    }));
                }
                {
                    this.r1$1 = r1$1;
                    this.r2$2 = r2$2;
                    ManagedResourceOperations.$init$(this);
                }

                private static /* synthetic */ Object $deserializeLambda$(SerializedLambda serializedLambda) {
                    return LambdaDeserialize.bootstrap("lambdaDeserialize", new MethodHandle[]{$anonfun$acquireFor$1(resource.package$$anon$1 scala.Function1 scala.collection.Seq ), $anonfun$acquireFor$2(scala.Function1 scala.collection.Seq java.lang.Object )}, serializedLambda);
                }
            };
        }
        return toReturn;
    }

    public <A> ManagedResource<A> shared(Function0<A> opener, Resource<A> evidence$4, OptManifest<A> evidence$5) {
        VolatileObjectRef sharedReference = VolatileObjectRef.create((Object)None$.MODULE$);
        Object lock = new Object();
        Resource resource = new Resource<A>(evidence$4, sharedReference, lock){
            private final Resource evidence$4$1;
            private final VolatileObjectRef sharedReference$1;
            private final Object lock$1;

            public void open(A r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(A r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                Object object = this.lock$1;
                synchronized (object) {
                    BoxedUnit boxedUnit;
                    Some some;
                    Tuple2 tuple2;
                    Option option = (Option)this.sharedReference$1.elem;
                    if (option instanceof Some && (tuple2 = (Tuple2)(some = (Some)option).value()) != null) {
                        int oldReferenceCount = tuple2._1$mcI$sp();
                        Object sc = tuple2._2();
                        if (!BoxesRunTime.equals(r, (Object)sc)) {
                            throw new IllegalArgumentException();
                        }
                        if (oldReferenceCount == 1) {
                            ((Resource)Predef$.MODULE$.implicitly((Object)this.evidence$4$1)).close(sc);
                            this.sharedReference$1.elem = None$.MODULE$;
                            boxedUnit = BoxedUnit.UNIT;
                        } else {
                            this.sharedReference$1.elem = new Some((Object)new Tuple2((Object)BoxesRunTime.boxToInteger((int)(oldReferenceCount - 1)), sc));
                            boxedUnit = BoxedUnit.UNIT;
                        }
                    } else {
                        if (None$.MODULE$.equals(option)) {
                            throw new IllegalStateException();
                        }
                        throw new MatchError((Object)option);
                    }
                    BoxedUnit boxedUnit2 = boxedUnit;
                }
            }
            {
                this.evidence$4$1 = evidence$4$1;
                this.sharedReference$1 = sharedReference$1;
                this.lock$1 = lock$1;
                Resource.$init$(this);
            }
        };
        return new DefaultManagedResource((Function0 & Serializable & scala.Serializable)() -> package$.acquire$1(opener, evidence$4, sharedReference, lock), resource, (OptManifest)Predef$.MODULE$.implicitly(evidence$5));
    }

    public <A, B> Either<A, B> extractedEitherToEither(ExtractedEither<A, B> extracted) {
        return extracted.either();
    }

    private static final Object acquire$1(Function0 opener$1, Resource evidence$4$1, VolatileObjectRef sharedReference$1, Object lock$1) {
        Object object;
        Object object2 = lock$1;
        synchronized (object2) {
            Some some;
            Tuple2 tuple2;
            Tuple2 tuple22;
            Option option = (Option)sharedReference$1.elem;
            if (None$.MODULE$.equals(option)) {
                Object r = opener$1.apply();
                ((Resource)Predef$.MODULE$.implicitly((Object)evidence$4$1)).open(r);
                tuple22 = new Tuple2((Object)BoxesRunTime.boxToInteger((int)1), r);
            } else if (option instanceof Some && (tuple2 = (Tuple2)(some = (Some)option).value()) != null) {
                int oldReferenceCount = tuple2._1$mcI$sp();
                Object sc = tuple2._2();
                tuple22 = new Tuple2((Object)BoxesRunTime.boxToInteger((int)(oldReferenceCount + 1)), sc);
            } else {
                throw new MatchError((Object)option);
            }
            Tuple2 tuple23 = tuple22;
            if (tuple23 == null) {
                throw new MatchError((Object)tuple23);
            }
            int referenceCount = tuple23._1$mcI$sp();
            Object sc = tuple23._2();
            Tuple2 tuple24 = new Tuple2((Object)BoxesRunTime.boxToInteger((int)referenceCount), sc);
            Tuple2 tuple25 = tuple24;
            int referenceCount2 = tuple25._1$mcI$sp();
            Object sc2 = tuple25._2();
            sharedReference$1.elem = new Some((Object)new Tuple2((Object)BoxesRunTime.boxToInteger((int)referenceCount2), sc2));
            object = sc2;
        }
        return object;
    }

    private package$() {
        MODULE$ = this;
    }
}

