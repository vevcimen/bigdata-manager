/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Predef$
 *  scala.Some
 *  scala.reflect.OptManifest
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 */
package resource;

import resource.AbstractManagedResource;
import resource.Resource;
import scala.Function0;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Some;
import scala.reflect.OptManifest;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;

@ScalaSignature(bytes="\u0006\u0001I4A!\u0001\u0002\u0003\u000b\t1B)\u001a4bk2$X*\u00198bO\u0016$'+Z:pkJ\u001cWMC\u0001\u0004\u0003!\u0011Xm]8ve\u000e,7\u0001A\u000b\u0003\r5\u0019\"\u0001A\u0004\u0011\u0007!I1\"D\u0001\u0003\u0013\tQ!AA\fBEN$(/Y2u\u001b\u0006t\u0017mZ3e%\u0016\u001cx.\u001e:dKB\u0011A\"\u0004\u0007\u0001\t\u0015q\u0001A1\u0001\u0010\u0005\u0005\u0011\u0016C\u0001\t\u0017!\t\tB#D\u0001\u0013\u0015\u0005\u0019\u0012!B:dC2\f\u0017BA\u000b\u0013\u0005\u001dqu\u000e\u001e5j]\u001e\u0004\"!E\f\n\u0005a\u0011\"aA!os\"A!\u0004\u0001B\u0001J\u0003%1$A\u0001s!\r\tBdC\u0005\u0003;I\u0011\u0001\u0002\u00102z]\u0006lWM\u0010\u0005\t?\u0001\u0011\u0019\u0011)A\u0006A\u0005QQM^5eK:\u001cW\rJ\u0019\u0011\u0007!\t3\"\u0003\u0002#\u0005\tA!+Z:pkJ\u001cW\r\u0003\u0005%\u0001\t\r\t\u0015a\u0003&\u0003))g/\u001b3f]\u000e,GE\r\t\u0004M5ZaBA\u0014,!\tA##D\u0001*\u0015\tQC!\u0001\u0004=e>|GOP\u0005\u0003YI\ta\u0001\u0015:fI\u00164\u0017B\u0001\u00180\u0005-y\u0005\u000f^'b]&4Wm\u001d;\u000b\u00051\u0012\u0002\"B\u0019\u0001\t\u0003\u0011\u0014A\u0002\u001fj]&$h\b\u0006\u00024oQ\u0019A'\u000e\u001c\u0011\u0007!\u00011\u0002C\u0003 a\u0001\u000f\u0001\u0005C\u0003%a\u0001\u000fQ\u0005\u0003\u0004\u001ba\u0011\u0005\ra\u0007\u0005\bs\u0001\u0011\r\u0011\"\u0005;\u0003%!\u0018\u0010]3Ue\u0006LG/F\u0001!\u0011\u0019a\u0004\u0001)A\u0005A\u0005QA/\u001f9f)J\f\u0017\u000e\u001e\u0011\t\u000by\u0002A\u0011K \u0002\t=\u0004XM\\\u000b\u0002\u0017!)\u0011\t\u0001C)\u0005\u0006YQO\\:bM\u0016\u001cEn\\:f)\r\u0019ei\u0012\t\u0003#\u0011K!!\u0012\n\u0003\tUs\u0017\u000e\u001e\u0005\u00065\u0001\u0003\ra\u0003\u0005\u0006\u0011\u0002\u0003\r!S\u0001\u0006KJ\u0014xN\u001d\t\u0004#)c\u0015BA&\u0013\u0005\u0019y\u0005\u000f^5p]B\u0011QJ\u0015\b\u0003\u001dBs!\u0001K(\n\u0003MI!!\u0015\n\u0002\u000fA\f7m[1hK&\u00111\u000b\u0016\u0002\n)\"\u0014xn^1cY\u0016T!!\u0015\n\t\u000bY\u0003A\u0011K,\u0002\u000f%\u001ch)\u0019;bYR\u0011\u0001l\u0017\t\u0003#eK!A\u0017\n\u0003\u000f\t{w\u000e\\3b]\")A,\u0016a\u0001\u0019\u0006\tA\u000fC\u0003_\u0001\u0011Es,\u0001\u0006jgJ+G\u000f\u001b:po:$\"\u0001\u00171\t\u000bqk\u0006\u0019\u0001'\t\u000b\t\u0004A\u0011I2\u0002\u0011!\f7\u000f[\"pI\u0016$\u0012\u0001\u001a\t\u0003#\u0015L!A\u001a\n\u0003\u0007%sG\u000fC\u0003i\u0001\u0011\u0005\u0013.\u0001\u0005u_N#(/\u001b8h)\u0005Q\u0007CA6q\u001b\u0005a'BA7o\u0003\u0011a\u0017M\\4\u000b\u0003=\fAA[1wC&\u0011\u0011\u000f\u001c\u0002\u0007'R\u0014\u0018N\\4")
public final class DefaultManagedResource<R>
extends AbstractManagedResource<R> {
    private final Function0<R> r;
    private final OptManifest<R> evidence$2;
    private final Resource<R> typeTrait;

    public Resource<R> typeTrait() {
        return this.typeTrait;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public R open() {
        void var1_1;
        Object resource = this.r.apply();
        this.typeTrait().open(resource);
        return var1_1;
    }

    @Override
    public void unsafeClose(R r, Option<Throwable> error) {
        Option<Throwable> option = error;
        if (None$.MODULE$.equals(option)) {
            this.typeTrait().close(r);
            BoxedUnit boxedUnit = BoxedUnit.UNIT;
        } else if (option instanceof Some) {
            Some some = (Some)option;
            Throwable t = (Throwable)some.value();
            this.typeTrait().closeAfterException(r, t);
            BoxedUnit boxedUnit = BoxedUnit.UNIT;
        } else {
            throw new MatchError(option);
        }
    }

    @Override
    public boolean isFatal(Throwable t) {
        return this.typeTrait().isFatalException(t);
    }

    @Override
    public boolean isRethrown(Throwable t) {
        return this.typeTrait().isRethrownException(t);
    }

    public int hashCode() {
        return (this.typeTrait().hashCode() << 7) + super.hashCode() + 13;
    }

    public String toString() {
        return "Default[" + Predef$.MODULE$.implicitly(this.evidence$2) + " : " + this.typeTrait() + "](...)";
    }

    public DefaultManagedResource(Function0<R> r, Resource<R> evidence$1, OptManifest<R> evidence$2) {
        this.r = r;
        this.evidence$2 = evidence$2;
        this.typeTrait = (Resource)Predef$.MODULE$.implicitly(evidence$1);
    }
}

