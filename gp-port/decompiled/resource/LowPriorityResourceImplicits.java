/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 *  scala.runtime.ScalaRunTime$
 *  scala.runtime.StructuralCallSite
 */
package resource;

import java.lang.invoke.CallSite;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import resource.LowPriorityResourceImplicits$;
import resource.Resource;
import scala.reflect.ScalaSignature;
import scala.runtime.ScalaRunTime$;
import scala.runtime.StructuralCallSite;

@ScalaSignature(bytes="\u0006\u0001\u00053q!\u0001\u0002\u0011\u0002\u0007\u0005RA\u0001\u000fM_^\u0004&/[8sSRL(+Z:pkJ\u001cW-S7qY&\u001c\u0017\u000e^:\u000b\u0003\r\t\u0001B]3t_V\u00148-Z\u0002\u0001'\t\u0001a\u0001\u0005\u0002\b\u00155\t\u0001BC\u0001\n\u0003\u0015\u00198-\u00197b\u0013\tY\u0001B\u0001\u0004B]f\u0014VM\u001a\u0005\u0006\u001b\u0001!\tAD\u0001\u0007I%t\u0017\u000e\u001e\u0013\u0015\u0003=\u0001\"a\u0002\t\n\u0005EA!\u0001B+oSR,Aa\u0005\u0001\u0001)\t\u0019\"+\u001a4mK\u000e$\u0018N^3DY>\u001cX-\u00192mKJ\u0011QC\u0002\u0004\u0005-\u0001\u0001AC\u0001\u0007=e\u00164\u0017N\\3nK:$h\bC\u0003\u0019+\u0019\u0005a\"A\u0003dY>\u001cX\rC\u0003\u001b\u0001\u0011\r1$A\u000esK\u001adWm\u0019;jm\u0016\u001cEn\\:fC\ndWMU3t_V\u00148-Z\u000b\u00039\u0015*\u0012!\b\n\u0004=\u0019yb\u0001\u0002\f\u001a\u0001u\u00012\u0001I\u0011$\u001b\u0005\u0011\u0011B\u0001\u0012\u0003\u0005!\u0011Vm]8ve\u000e,\u0007C\u0001\u0013&\u0019\u0001!QAJ\rC\u0002\u001d\u0012\u0011!Q\t\u0003Q-\u0002\"aB\u0015\n\u0005)B!a\u0002(pi\"Lgn\u001a\t\u0003YIi\u0011\u0001A\u0003\u0005]\u0001\u0001qF\u0001\u000bSK\u001adWm\u0019;jm\u0016$\u0015n\u001d9pg\u0006\u0014G.\u001a\n\u0003a\u00191AA\u0006\u0001\u0001_!)!\u0007\rD\u0001\u001d\u00059A-[:q_N,\u0007\"\u0002\u001b\u0001\t\u0007)\u0014\u0001\b:fM2,7\r^5wK\u0012K7\u000f]8tC\ndWMU3t_V\u00148-Z\u000b\u0003mm*\u0012a\u000e\n\u0004q\u0019Id\u0001\u0002\f4\u0001]\u00022\u0001I\u0011;!\t!3\bB\u0003'g\t\u0007A(\u0005\u0002){A\u0011A&L\u0015\u0003\u0001}J!\u0001\u0011\u0002\u0003?5+G-[;n!JLwN]5usJ+7o\\;sG\u0016LU\u000e\u001d7jG&$8\u000f")
public interface LowPriorityResourceImplicits {
    public static /* synthetic */ Resource reflectiveCloseableResource$(LowPriorityResourceImplicits $this) {
        return $this.reflectiveCloseableResource();
    }

    default public <A> Resource<A> reflectiveCloseableResource() {
        return new Resource<A>(null){

            public static Method reflMethod$Method1(Class x$1) {
                CallSite methodCache1 = StructuralCallSite.bootstrap("apply", ()Ljava/lang/Object;);
                Method method1 = methodCache1.find(x$1);
                if (method1 != null) {
                    return method1;
                }
                method1 = ScalaRunTime$.MODULE$.ensureAccessible(x$1.getMethod("close", methodCache1.parameterTypes()));
                methodCache1.add(x$1, method1);
                return method1;
            }

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
                A qual1 = r;
                try {
                    $anon$1.reflMethod$Method1(qual1.getClass()).invoke(qual1, new Object[0]);
                }
                catch (InvocationTargetException invocationTargetException) {
                    throw invocationTargetException.getCause();
                }
            }

            public String toString() {
                return "Resource[{ def close() : Unit }]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static /* synthetic */ Resource reflectiveDisposableResource$(LowPriorityResourceImplicits $this) {
        return $this.reflectiveDisposableResource();
    }

    default public <A> Resource<A> reflectiveDisposableResource() {
        return new Resource<A>(null){

            public static Method reflMethod$Method2(Class x$1) {
                CallSite methodCache2 = StructuralCallSite.bootstrap("apply", ()Ljava/lang/Object;);
                Method method2 = methodCache2.find(x$1);
                if (method2 != null) {
                    return method2;
                }
                method2 = ScalaRunTime$.MODULE$.ensureAccessible(x$1.getMethod("dispose", methodCache2.parameterTypes()));
                methodCache2.add(x$1, method2);
                return method2;
            }

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
                A qual2 = r;
                try {
                    $anon$2.reflMethod$Method2(qual2.getClass()).invoke(qual2, new Object[0]);
                }
                catch (InvocationTargetException invocationTargetException) {
                    throw invocationTargetException.getCause();
                }
            }

            public String toString() {
                return "Resource[{ def dispose() : Unit }]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static void $init$(LowPriorityResourceImplicits $this) {
    }
}

