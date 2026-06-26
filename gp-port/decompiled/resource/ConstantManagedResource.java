/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.reflect.ScalaSignature
 */
package resource;

import resource.AbstractManagedResource;
import scala.Option;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001q2A!\u0001\u0002\u0003\u000b\t92i\u001c8ti\u0006tG/T1oC\u001e,GMU3t_V\u00148-\u001a\u0006\u0002\u0007\u0005A!/Z:pkJ\u001cWm\u0001\u0001\u0016\u0005\u0019i1C\u0001\u0001\b!\rA\u0011bC\u0007\u0002\u0005%\u0011!B\u0001\u0002\u0018\u0003\n\u001cHO]1di6\u000bg.Y4fIJ+7o\\;sG\u0016\u0004\"\u0001D\u0007\r\u0001\u0011)a\u0002\u0001b\u0001\u001f\t\ta+\u0005\u0002\u0011-A\u0011\u0011\u0003F\u0007\u0002%)\t1#A\u0003tG\u0006d\u0017-\u0003\u0002\u0016%\t9aj\u001c;iS:<\u0007CA\t\u0018\u0013\tA\"CA\u0002B]fD\u0001B\u0007\u0001\u0003\u0002\u0003\u0006IaC\u0001\u0006m\u0006dW/\u001a\u0005\u00069\u0001!\t!H\u0001\u0007y%t\u0017\u000e\u001e \u0015\u0005yy\u0002c\u0001\u0005\u0001\u0017!)!d\u0007a\u0001\u0017!)\u0011\u0005\u0001C)E\u0005!q\u000e]3o+\u0005Y\u0001\"\u0002\u0013\u0001\t#*\u0013aC;og\u00064Wm\u00117pg\u0016$2AJ\u0015,!\t\tr%\u0003\u0002)%\t!QK\\5u\u0011\u0015Q3\u00051\u0001\f\u0003\u0019A\u0017M\u001c3mK\")Af\ta\u0001[\u00051QM\u001d:peN\u00042!\u0005\u00181\u0013\ty#C\u0001\u0004PaRLwN\u001c\t\u0003cer!AM\u001c\u000f\u0005M2T\"\u0001\u001b\u000b\u0005U\"\u0011A\u0002\u001fs_>$h(C\u0001\u0014\u0013\tA$#A\u0004qC\u000e\\\u0017mZ3\n\u0005iZ$!\u0003+ie><\u0018M\u00197f\u0015\tA$\u0003")
public final class ConstantManagedResource<V>
extends AbstractManagedResource<V> {
    private final V value;

    @Override
    public V open() {
        return this.value;
    }

    @Override
    public void unsafeClose(V handle, Option<Throwable> errors) {
    }

    public ConstantManagedResource(V value) {
        this.value = value;
    }
}

