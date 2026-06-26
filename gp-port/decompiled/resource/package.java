/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.Predef$$less$colon$less
 *  scala.Tuple2
 *  scala.collection.Seq
 *  scala.collection.immutable.List
 *  scala.reflect.OptManifest
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.util.Either
 */
package resource;

import resource.DefaultManagedResource;
import resource.ExtractedEither;
import resource.ManagedResource;
import resource.Resource;
import resource.package$;
import scala.Function0;
import scala.Function1;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.Seq;
import scala.collection.immutable.List;
import scala.reflect.OptManifest;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.util.Either;

@ScalaSignature(bytes="\u0006\u0001\u0005mu!B\u0001\u0003\u0011\u0003)\u0011a\u00029bG.\fw-\u001a\u0006\u0002\u0007\u0005A!/Z:pkJ\u001cWm\u0001\u0001\u0011\u0005\u00199Q\"\u0001\u0002\u0007\u000b!\u0011\u0001\u0012A\u0005\u0003\u000fA\f7m[1hKN\u0011qA\u0003\t\u0003\u00179i\u0011\u0001\u0004\u0006\u0002\u001b\u0005)1oY1mC&\u0011q\u0002\u0004\u0002\u0007\u0003:L(+\u001a4\t\u000bE9A\u0011\u0001\n\u0002\rqJg.\u001b;?)\u0005)Q\u0001\u0002\u000b\b\u0001U\u00111\"\u0012:s_JDu\u000e\u001c3feV\u0011a#\u000b\t\u0005/y\tsE\u0004\u0002\u0019;9\u0011\u0011\u0004H\u0007\u00025)\u00111\u0004B\u0001\u0007yI|w\u000e\u001e \n\u00035I!!\u0001\u0007\n\u0005}\u0001#AB#ji\",'O\u0003\u0002\u0002\u0019A\u0019qC\t\u0013\n\u0005\r\u0002#\u0001\u0002'jgR\u0004\"aF\u0013\n\u0005\u0019\u0002#!\u0003+ie><\u0018M\u00197f!\tA\u0013\u0006\u0004\u0001\u0005\u000b)\u001a\"\u0019A\u0016\u0003\u0003\u0005\u000b\"\u0001L\u0018\u0011\u0005-i\u0013B\u0001\u0018\r\u0005\u001dqu\u000e\u001e5j]\u001e\u0004\"a\u0003\u0019\n\u0005Eb!aA!os\")1g\u0002C\u0001i\u00059Q.\u00198bO\u0016$WCA\u001b<)\t14\nF\u00028y\u0005\u00032A\u0002\u001d;\u0013\tI$AA\bNC:\fw-\u001a3SKN|WO]2f!\tA3\bB\u0003+e\t\u00071\u0006C\u0004>e\u0005\u0005\t9\u0001 \u0002\u0015\u00154\u0018\u000eZ3oG\u0016$\u0013\u0007E\u0002\u0007\u007fiJ!\u0001\u0011\u0002\u0003\u0011I+7o\\;sG\u0016DqA\u0011\u001a\u0002\u0002\u0003\u000f1)\u0001\u0006fm&$WM\\2fII\u00022\u0001\u0012%;\u001d\t)e\t\u0005\u0002\u001a\u0019%\u0011q\tD\u0001\u0007!J,G-\u001a4\n\u0005%S%aC(qi6\u000bg.\u001b4fgRT!a\u0012\u0007\t\r1\u0013D\u00111\u0001N\u0003\u0019y\u0007/\u001a8feB\u00191B\u0014\u001e\n\u0005=c!\u0001\u0003\u001fcs:\fW.\u001a \t\u000bE;A\u0011\u0001*\u0002\u0011\r|gn\u001d;b]R,\"a\u0015,\u0015\u0005QC\u0006c\u0001\u00049+B\u0011\u0001F\u0016\u0003\u0006/B\u0013\ra\u000b\u0002\u0002-\")\u0011\f\u0015a\u0001+\u0006)a/\u00197vK\")1l\u0002C\u00019\u0006\u0019R.Y6f\u001b\u0006t\u0017mZ3e%\u0016\u001cx.\u001e:dKV\u0011Q,\u001a\u000b\u0003=v$\"aX;\u0015\u0005\u0001TGCA1h!\r1!\rZ\u0005\u0003G\n\u0011a\u0003R3gCVdG/T1oC\u001e,GMU3t_V\u00148-\u001a\t\u0003Q\u0015$QA\u001a.C\u0002-\u0012\u0011A\u0015\u0005\bQj\u000b\t\u0011q\u0001j\u0003))g/\u001b3f]\u000e,Ge\r\t\u0004\t\"#\u0007\"B6[\u0001\u0004a\u0017AC3yG\u0016\u0004H/[8ogB\u0019qCI71\u00059\u0014\bc\u0001#pc&\u0011\u0001O\u0013\u0002\u0006\u00072\f7o\u001d\t\u0003QI$\u0011b\u001d6\u0002\u0002\u0003\u0005)\u0011\u0001;\u0003\u0007}#\u0013'\u0005\u0002-I!)aO\u0017a\u0001o\u000611\r\\8tKJ\u0004Ba\u0003=eu&\u0011\u0011\u0010\u0004\u0002\n\rVt7\r^5p]F\u0002\"aC>\n\u0005qd!\u0001B+oSRDa\u0001\u0014.\u0005\u0002\u0004q\bcA\u0006OI\"9\u0011\u0011A\u0004\u0005\u0002\u0005\r\u0011aA1oIV1\u0011QAA\t\u0003+!b!a\u0002\u0002\u001a\u0005}\u0001\u0003\u0002\u00049\u0003\u0013\u0001raCA\u0006\u0003\u001f\t\u0019\"C\u0002\u0002\u000e1\u0011a\u0001V;qY\u0016\u0014\u0004c\u0001\u0015\u0002\u0012\u0011)!f b\u0001WA\u0019\u0001&!\u0006\u0005\r\u0005]qP1\u0001,\u0005\u0005\u0011\u0005bBA\u000e\u007f\u0002\u0007\u0011QD\u0001\u0003eF\u0002BA\u0002\u001d\u0002\u0010!9\u0011\u0011E@A\u0002\u0005\r\u0012A\u0001:3!\u00111\u0001(a\u0005\t\u000f\u0005\u001dr\u0001\"\u0001\u0002*\u0005!!n\\5o+!\tY#!\u000f\u0002P\u0005\u001dC\u0003BA\u0017\u00037\"b!a\f\u0002<\u0005M\u0003\u0003\u0002\u00049\u0003c\u0001RaFA\u001a\u0003oI1!!\u000e!\u0005\r\u0019V-\u001d\t\u0004Q\u0005eBA\u0002\u0016\u0002&\t\u00071\u0006\u0003\u0005\u0002>\u0005\u0015\u00029AA \u0003\r)g\u000f\r\t\b\t\u0006\u0005\u0013QIA&\u0013\r\t\u0019E\u0013\u0002\u0011I1,7o\u001d\u0013d_2|g\u000e\n7fgN\u00042\u0001KA$\t\u001d\tI%!\nC\u0002-\u0012!aQ\"\u0011\u000b]\t\u0019$!\u0014\u0011\u0007!\ny\u0005B\u0004\u0002R\u0005\u0015\"\u0019A\u0016\u0003\u00055\u0013\u0006\u0002CA+\u0003K\u0001\u001d!a\u0016\u0002\u0007\u00154\u0018\u0007E\u0004E\u0003\u0003\ni%!\u0017\u0011\t\u0019A\u0014q\u0007\u0005\t\u0003;\n)\u00031\u0001\u0002F\u0005I!/Z:pkJ\u001cWm\u001d\u0005\b\u0003C:A\u0011AA2\u0003\u0019\u0019\b.\u0019:fIV!\u0011QMA7)\u0011\t9'a\u001f\u0015\r\u0005%\u0014qNA;!\u00111\u0001(a\u001b\u0011\u0007!\ni\u0007\u0002\u0004+\u0003?\u0012\ra\u000b\u0005\u000b\u0003c\ny&!AA\u0004\u0005M\u0014AC3wS\u0012,gnY3%iA!aaPA6\u0011)\t9(a\u0018\u0002\u0002\u0003\u000f\u0011\u0011P\u0001\u000bKZLG-\u001a8dK\u0012*\u0004\u0003\u0002#I\u0003WB\u0001\u0002TA0\t\u0003\u0007\u0011Q\u0010\t\u0005\u00179\u000bY\u0007C\u0004\u0002\u0002\u001e!\u0019!a!\u0002/\u0015DHO]1di\u0016$W)\u001b;iKJ$v.R5uQ\u0016\u0014XCBAC\u0003\u0017\u000by\t\u0006\u0003\u0002\b\u0006E\u0005CB\f\u001f\u0003\u0013\u000bi\tE\u0002)\u0003\u0017#aAKA@\u0005\u0004Y\u0003c\u0001\u0015\u0002\u0010\u00129\u0011qCA@\u0005\u0004Y\u0003\u0002CAJ\u0003\u007f\u0002\r!!&\u0002\u0013\u0015DHO]1di\u0016$\u0007c\u0002\u0004\u0002\u0018\u0006%\u0015QR\u0005\u0004\u00033\u0013!aD#yiJ\f7\r^3e\u000b&$\b.\u001a:")
public final class package {
    public static <A, B> Either<A, B> extractedEitherToEither(ExtractedEither<A, B> extractedEither) {
        return package$.MODULE$.extractedEitherToEither(extractedEither);
    }

    public static <A> ManagedResource<A> shared(Function0<A> function0, Resource<A> resource, OptManifest<A> optManifest) {
        return package$.MODULE$.shared(function0, resource, optManifest);
    }

    public static <A, MR, CC> ManagedResource<Seq<A>> join(CC CC, Predef$.less.colon.less<CC, Seq<MR>> less2, Predef$.less.colon.less<MR, ManagedResource<A>> less3) {
        return package$.MODULE$.join(CC, less2, less3);
    }

    public static <A, B> ManagedResource<Tuple2<A, B>> and(ManagedResource<A> managedResource, ManagedResource<B> managedResource2) {
        return package$.MODULE$.and(managedResource, managedResource2);
    }

    public static <R> DefaultManagedResource<R> makeManagedResource(Function0<R> function0, Function1<R, BoxedUnit> function1, List<Class<? extends Throwable>> list, OptManifest<R> optManifest) {
        return package$.MODULE$.makeManagedResource(function0, function1, list, optManifest);
    }

    public static <V> ManagedResource<V> constant(V v) {
        return package$.MODULE$.constant(v);
    }

    public static <A> ManagedResource<A> managed(Function0<A> function0, Resource<A> resource, OptManifest<A> optManifest) {
        return package$.MODULE$.managed(function0, resource, optManifest);
    }
}

