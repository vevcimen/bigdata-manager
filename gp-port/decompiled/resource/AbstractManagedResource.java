/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Predef$$less$colon$less
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.Traversable
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.List
 *  scala.collection.immutable.List$
 *  scala.collection.immutable.Nil$
 *  scala.concurrent.ExecutionContext
 *  scala.concurrent.Future
 *  scala.package$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.Nothing$
 *  scala.runtime.java8.JFunction0$mcV$sp
 *  scala.util.Either
 *  scala.util.Left
 *  scala.util.Right
 *  scala.util.control.ControlThrowable
 *  scala.util.control.Exception$
 *  scala.util.control.Exception$Catch
 */
package resource;

import java.io.Serializable;
import java.lang.invoke.LambdaMetafactory;
import resource.ExtractableManagedResource;
import resource.ExtractedEither;
import resource.ManagedResource;
import resource.ManagedResourceOperations;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.Traversable;
import scala.collection.TraversableOnce;
import scala.collection.immutable.List;
import scala.collection.immutable.List$;
import scala.collection.immutable.Nil$;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.package$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.Nothing$;
import scala.runtime.java8.JFunction0;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;
import scala.util.control.ControlThrowable;
import scala.util.control.Exception;
import scala.util.control.Exception$;

@ScalaSignature(bytes="\u0006\u000194Q!\u0001\u0002\u0002\u0002\u0015\u0011q#\u00112tiJ\f7\r^'b]\u0006<W\r\u001a*fg>,(oY3\u000b\u0003\r\t\u0001B]3t_V\u00148-Z\u0002\u0001+\t11c\u0005\u0003\u0001\u000f5a\u0002C\u0001\u0005\f\u001b\u0005I!\"\u0001\u0006\u0002\u000bM\u001c\u0017\r\\1\n\u00051I!AB!osJ+g\rE\u0002\u000f\u001fEi\u0011AA\u0005\u0003!\t\u0011q\"T1oC\u001e,GMU3t_V\u00148-\u001a\t\u0003%Ma\u0001\u0001B\u0003\u0015\u0001\t\u0007QCA\u0001S#\t1\u0012\u0004\u0005\u0002\t/%\u0011\u0001$\u0003\u0002\b\u001d>$\b.\u001b8h!\tA!$\u0003\u0002\u001c\u0013\t\u0019\u0011I\\=\u0011\u00079i\u0012#\u0003\u0002\u001f\u0005\tIR*\u00198bO\u0016$'+Z:pkJ\u001cWm\u00149fe\u0006$\u0018n\u001c8t\u0011\u0015\u0001\u0003\u0001\"\u0001\"\u0003\u0019a\u0014N\\5u}Q\t!\u0005E\u0002\u000f\u0001EAQ\u0001\n\u0001\u0007\u0012\u0015\nAa\u001c9f]V\t\u0011\u0003C\u0003(\u0001\u0019E\u0001&A\u0006v]N\fg-Z\"m_N,GcA\u0015-]A\u0011\u0001BK\u0005\u0003W%\u0011A!\u00168ji\")QF\na\u0001#\u00051\u0001.\u00198eY\u0016DQa\f\u0014A\u0002A\na!\u001a:s_J\u001c\bc\u0001\u00052g%\u0011!'\u0003\u0002\u0007\u001fB$\u0018n\u001c8\u0011\u0005QbdBA\u001b;\u001d\t1\u0014(D\u00018\u0015\tAD!\u0001\u0004=e>|GOP\u0005\u0002\u0015%\u00111(C\u0001\ba\u0006\u001c7.Y4f\u0013\tidHA\u0005UQJ|w/\u00192mK*\u00111(\u0003\u0005\u0006\u0001\u0002!\t\"Q\u0001\u000bSN\u0014V\r\u001e5s_^tGC\u0001\"F!\tA1)\u0003\u0002E\u0013\t9!i\\8mK\u0006t\u0007\"\u0002$@\u0001\u0004\u0019\u0014!\u0001;\t\u000b!\u0003A\u0011C%\u0002\u000f%\u001ch)\u0019;bYR\u0011!I\u0013\u0005\u0006\r\u001e\u0003\ra\r\u0005\b\u0019\u0002\u0011\r\u0011\"\u0004N\u0003A\u0019\u0017\r^2iS:<gj\u001c8GCR\fG.F\u0001O!\ryuK\u0006\b\u0003!Vk\u0011!\u0015\u0006\u0003%N\u000bqaY8oiJ|GN\u0003\u0002U\u0013\u0005!Q\u000f^5m\u0013\t1\u0016+A\u0005Fq\u000e,\u0007\u000f^5p]&\u0011\u0001,\u0017\u0002\u0006\u0007\u0006$8\r\u001b\u0006\u0003-FCaa\u0017\u0001!\u0002\u001bq\u0015!E2bi\u000eD\u0017N\\4O_:4\u0015\r^1mA!)Q\f\u0001C!=\u0006Q\u0011mY9vSJ,gi\u001c:\u0016\u0005};GC\u00011j!\u0011q\u0011m\u00194\n\u0005\t\u0014!aD#yiJ\f7\r^3e\u000b&$\b.\u001a:\u0011\u0007Q\"7'\u0003\u0002f}\t!A*[:u!\t\u0011r\rB\u0003i9\n\u0007QCA\u0001C\u0011\u0015QG\f1\u0001l\u0003\u00051\u0007\u0003\u0002\u0005m#\u0019L!!\\\u0005\u0003\u0013\u0019+hn\u0019;j_:\f\u0004")
public abstract class AbstractManagedResource<R>
implements ManagedResourceOperations<R> {
    private final Exception.Catch<Nothing$> catchingNonFatal;

    @Override
    public <B> B acquireAndGet(Function1<R, B> f) {
        return (B)ManagedResourceOperations.acquireAndGet$(this, f);
    }

    @Override
    public <B> B apply(Function1<R, B> f) {
        return (B)ManagedResourceOperations.apply$(this, f);
    }

    @Override
    public <B> Traversable<B> toTraversable(Predef$.less.colon.less<R, TraversableOnce<B>> ev) {
        return ManagedResourceOperations.toTraversable$(this, ev);
    }

    @Override
    public Future<R> toFuture(ExecutionContext context) {
        return ManagedResourceOperations.toFuture$(this, context);
    }

    @Override
    public <B> ExtractableManagedResource<B> map(Function1<R, B> f) {
        return ManagedResourceOperations.map$(this, f);
    }

    @Override
    public <B> ManagedResource<B> flatMap(Function1<R, ManagedResource<B>> f) {
        return ManagedResourceOperations.flatMap$(this, f);
    }

    @Override
    public void foreach(Function1<R, BoxedUnit> f) {
        ManagedResourceOperations.foreach$(this, f);
    }

    @Override
    public <B> ManagedResource<Tuple2<R, B>> and(ManagedResource<B> that) {
        return ManagedResourceOperations.and$(this, that);
    }

    public abstract R open();

    public abstract void unsafeClose(R var1, Option<Throwable> var2);

    public boolean isRethrown(Throwable t) {
        Throwable throwable = t;
        boolean bl = throwable instanceof ControlThrowable ? true : throwable instanceof InterruptedException;
        return bl;
    }

    public boolean isFatal(Throwable t) {
        Throwable throwable = t;
        boolean bl = throwable instanceof VirtualMachineError;
        return bl;
    }

    private final Exception.Catch<Nothing$> catchingNonFatal() {
        return this.catchingNonFatal;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public <B> ExtractedEither<List<Throwable>, B> acquireFor(Function1<R, B> f) {
        block8: {
            block9: {
                block7: {
                    handle = this.open();
                    result = this.catchingNonFatal().either((Function0)(Function0 & Serializable & scala.Serializable)LambdaMetafactory.altMetafactory(null, null, null, ()Ljava/lang/Object;, $anonfun$acquireFor$1(scala.Function1 java.lang.Object ), ()Ljava/lang/Object;)(f, handle));
                    var7_5 = new Tuple2((Object)result, (Object)(close = this.catchingNonFatal().either((Function0)(JFunction0.mcV.sp & Serializable & scala.Serializable)LambdaMetafactory.altMetafactory(null, null, null, ()V, $anonfun$acquireFor$2(resource.AbstractManagedResource java.lang.Object scala.util.Either ), ()V)((AbstractManagedResource)this, handle, (Either)result))));
                    if (var7_5 != null && (var8_6 = (Either)var7_5._1()) instanceof Left && this.isRethrown(t1 = (Throwable)(var9_7 = (Left)var8_6).value())) {
                        throw t1;
                    }
                    if (var7_5 == null) break block7;
                    var11_9 = (Either)var7_5._1();
                    var12_10 = (Either)var7_5._2();
                    if (!(var11_9 instanceof Left)) break block7;
                    var13_11 = (Left)var11_9;
                    t1 = (Throwable)var13_11.value();
                    if (!(var12_10 instanceof Left)) break block7;
                    var15_13 = (Left)var12_10;
                    t2 = (Throwable)var15_13.value();
                    var17_15 = t1;
                    var18_16 = t2;
                    var2_17 = package$.MODULE$.Left().apply((Object)Nil$.MODULE$.$colon$colon((Object)var18_16).$colon$colon((Object)var17_15));
                    break block8;
                }
                if (var7_5 == null || !((var19_18 = (Either)var7_5._1()) instanceof Left)) break block9;
                var20_19 = (Left)var19_18;
                var22_21 = t1 = (Throwable)var20_19.value();
                var2_17 = package$.MODULE$.Left().apply((Object)Nil$.MODULE$.$colon$colon((Object)var22_21));
                break block8;
            }
            if (var7_5 == null) ** GOTO lbl-1000
            var23_22 = (Either)var7_5._1();
            var24_23 = (Either)var7_5._2();
            if (!(var23_22 instanceof Right) || !((var26_25 = (var25_24 = (Right)var23_22).value()) instanceof ExtractedEither) || !((var28_27 = (var27_26 = (ExtractedEither)var26_25).either()) instanceof Left)) ** GOTO lbl-1000
            var29_28 = (Left)var28_27;
            ts = var29_28.value();
            if (var24_23 instanceof Left) {
                var31_30 = (Left)var24_23;
                t2 = (Throwable)var31_30.value();
                var2_17 = package$.MODULE$.Left().apply(((List)ts).$colon$plus((Object)t2, List$.MODULE$.canBuildFrom()));
            } else if (var7_5 != null && (var33_32 = (Either)var7_5._2()) instanceof Left) {
                var34_33 = (Left)var33_32;
                var36_35 = t2 = (Throwable)var34_33.value();
                var2_17 = package$.MODULE$.Left().apply((Object)Nil$.MODULE$.$colon$colon((Object)var36_35));
            } else if (var7_5 != null && (var37_36 = (Either)var7_5._1()) instanceof Right) {
                var38_37 = (Right)var37_36;
                r = var38_37.value();
                var2_17 = package$.MODULE$.Right().apply(r);
            } else {
                throw new MatchError((Object)var7_5);
            }
        }
        either = var2_17;
        return new ExtractedEither<A, B>(either);
    }

    public static final /* synthetic */ boolean $anonfun$catchingNonFatal$1(AbstractManagedResource $this, Throwable e) {
        return !$this.isFatal(e);
    }

    public static final /* synthetic */ boolean $anonfun$catchingNonFatal$3(Throwable x$2) {
        return false;
    }

    public static final /* synthetic */ Object $anonfun$acquireFor$1(Function1 f$1, Object handle$1) {
        return f$1.apply(handle$1);
    }

    public static final /* synthetic */ void $anonfun$acquireFor$2(AbstractManagedResource $this, Object handle$1, Either result$1) {
        $this.unsafeClose(handle$1, (Option<Throwable>)result$1.left().toOption());
    }

    public AbstractManagedResource() {
        ManagedResourceOperations.$init$(this);
        this.catchingNonFatal = (Exception.Catch)new Exception.Catch(Exception$.MODULE$.mkThrowableCatcher((Function1 & Serializable & scala.Serializable)e -> BoxesRunTime.boxToBoolean((boolean)AbstractManagedResource.$anonfun$catchingNonFatal$1(this, e)), (Function1 & Serializable & scala.Serializable)x$1 -> {
            throw x$1;
        }), (Option)None$.MODULE$, (Function1 & Serializable & scala.Serializable)x$2 -> BoxesRunTime.boxToBoolean((boolean)AbstractManagedResource.$anonfun$catchingNonFatal$3(x$2))).withDesc("<non-fatal>");
    }
}

