/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Predef$
 *  scala.Serializable
 *  scala.StringContext
 *  scala.collection.Seq
 *  scala.collection.Traversable
 *  scala.collection.immutable.List
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 */
package resource;

import resource.ExtractedEither;
import resource.ManagedResource;
import resource.package$;
import scala.Function1;
import scala.Predef$;
import scala.Serializable;
import scala.StringContext;
import scala.collection.Seq;
import scala.collection.Traversable;
import scala.collection.immutable.List;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;

@ScalaSignature(bytes="\u0006\u000114q!\u0001\u0002\u0011\u0002\u0007\u0005QA\u0001\nNC:\fw-\u001a3Ue\u00064XM]:bE2,'\"A\u0002\u0002\u0011I,7o\\;sG\u0016\u001c\u0001!F\u0002\u0007+-\u001a2\u0001A\u0004\u000e!\tA1\"D\u0001\n\u0015\u0005Q\u0011!B:dC2\f\u0017B\u0001\u0007\n\u0005\u0019\te.\u001f*fMB\u0019a\"E\n\u000e\u0003=Q!\u0001E\u0005\u0002\u0015\r|G\u000e\\3di&|g.\u0003\u0002\u0013\u001f\tYAK]1wKJ\u001c\u0018M\u00197f!\t!R\u0003\u0004\u0001\u0005\rY\u0001AQ1\u0001\u0018\u0005\u0005\u0011\u0015C\u0001\r\u001c!\tA\u0011$\u0003\u0002\u001b\u0013\t9aj\u001c;iS:<\u0007C\u0001\u0005\u001d\u0013\ti\u0012BA\u0002B]fDQa\b\u0001\u0005\u0002\u0001\na\u0001J5oSR$C#A\u0011\u0011\u0005!\u0011\u0013BA\u0012\n\u0005\u0011)f.\u001b;\t\u000f\r\u0001!\u0019!D\u0001KU\ta\u0005E\u0002(Q)j\u0011AA\u0005\u0003S\t\u0011q\"T1oC\u001e,GMU3t_V\u00148-\u001a\t\u0003)-\"Q\u0001\f\u0001C\u0002]\u0011\u0011!\u0011\u0005\u0006]\u0001!\taL\u0001\fS\u001etwN]3FeJ|'\u000f\u0006\u00021gA\u0011\u0001\"M\u0005\u0003e%\u0011qAQ8pY\u0016\fg\u000eC\u00035[\u0001\u0007Q'A\u0003feJ|'\u000f\u0005\u00027}9\u0011q\u0007\u0010\b\u0003qmj\u0011!\u000f\u0006\u0003u\u0011\ta\u0001\u0010:p_Rt\u0014\"\u0001\u0006\n\u0005uJ\u0011a\u00029bG.\fw-Z\u0005\u0003\u007f\u0001\u0013\u0011\"\u0012=dKB$\u0018n\u001c8\u000b\u0005uJ\u0001\"\u0002\"\u0001\t\u0003\u0019\u0015a\u00075b]\u0012dW-\u0012:s_J\u001cH)\u001e:j]\u001e$&/\u0019<feN\fG\u000e\u0006\u0002\"\t\")Q)\u0011a\u0001\r\u0006\u0011Q\r\u001f\t\u0004m\u001dK\u0015B\u0001%A\u0005\u0011a\u0015n\u001d;\u0011\u0005YR\u0015BA&A\u0005%!\u0006N]8xC\ndW\rC\u0003N\u0001\u0019Ea*A\bj]R,'O\\1m\r>\u0014X-Y2i+\tyu\u000bF\u0002\"!FCQa\u0001'A\u0002)BQA\u0015'A\u0002M\u000b\u0011A\u001a\t\u0005\u0011Q\u001bb+\u0003\u0002V\u0013\tIa)\u001e8di&|g.\r\t\u0003)]#Q\u0001\u0017'C\u0002]\u0011\u0011!\u0016\u0005\u00065\u0002!\taW\u0001\bM>\u0014X-Y2i+\ta\u0006\r\u0006\u0002\";\")!+\u0017a\u0001=B!\u0001\u0002V\n`!\t!\u0002\rB\u0003Y3\n\u0007q\u0003C\u0003c\u0001\u0011\u00053-\u0001\u0005u_N#(/\u001b8h)\u0005!\u0007CA3j\u001d\t1w\r\u0005\u00029\u0013%\u0011\u0001.C\u0001\u0007!J,G-\u001a4\n\u0005)\\'AB*ue&twM\u0003\u0002i\u0013\u0001")
public interface ManagedTraversable<B, A>
extends Traversable<B> {
    public ManagedResource<A> resource();

    public static /* synthetic */ boolean ignoreError$(ManagedTraversable $this, Exception error) {
        return $this.ignoreError(error);
    }

    default public boolean ignoreError(Exception error) {
        return false;
    }

    public static /* synthetic */ void handleErrorsDuringTraversal$(ManagedTraversable $this, List ex) {
        $this.handleErrorsDuringTraversal((List<Throwable>)ex);
    }

    default public void handleErrorsDuringTraversal(List<Throwable> ex) {
        ex.headOption().foreach((Function1 & java.io.Serializable & Serializable)x$1 -> {
            throw x$1;
        });
    }

    public <U> void internalForeach(A var1, Function1<B, U> var2);

    public static /* synthetic */ void foreach$(ManagedTraversable $this, Function1 f) {
        $this.foreach(f);
    }

    default public <U> void foreach(Function1<B, U> f) {
        ExtractedEither result = this.resource().acquireFor((Function1 & java.io.Serializable & Serializable)r -> {
            ManagedTraversable.$anonfun$foreach$1(this, f, r);
            return BoxedUnit.UNIT;
        });
        package$.MODULE$.extractedEitherToEither(result).left().foreach((Function1 & java.io.Serializable & Serializable)ex -> {
            this.handleErrorsDuringTraversal((List<Throwable>)ex);
            return BoxedUnit.UNIT;
        });
    }

    public static /* synthetic */ String toString$(ManagedTraversable $this) {
        return $this.toString();
    }

    default public String toString() {
        return new StringContext((Seq)Predef$.MODULE$.wrapRefArray((Object[])new String[]{"ManagedTraversable(", ")"})).s((Seq)Predef$.MODULE$.genericWrapArray((Object)new Object[]{this.resource()}));
    }

    public static /* synthetic */ void $anonfun$foreach$1(ManagedTraversable $this, Function1 f$1, Object r) {
        try {
            $this.internalForeach(r, f$1);
        }
        catch (Throwable throwable) {
            Exception exception;
            Throwable throwable2 = throwable;
            if (throwable2 instanceof Exception && $this.ignoreError(exception = (Exception)throwable2)) {
                BoxedUnit boxedUnit = BoxedUnit.UNIT;
            }
            throw throwable;
        }
    }

    public static void $init$(ManagedTraversable $this) {
    }
}

