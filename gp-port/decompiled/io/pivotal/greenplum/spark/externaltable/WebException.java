/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.Tuple3
 *  scala.collection.Iterator
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 *  scala.runtime.Statics
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.WebException$;
import scala.Function1;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.Tuple3;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;
import scala.runtime.Statics;

@ScalaSignature(bytes="\u0006\u0001\u0005ud\u0001\u0002\u0010 \u0001*B\u0001\u0002\u0011\u0001\u0003\u0016\u0004%\t!\u0011\u0005\t\u000b\u0002\u0011\t\u0012)A\u0005\u0005\"Aa\t\u0001BK\u0002\u0013\u0005q\t\u0003\u0005Q\u0001\tE\t\u0015!\u0003I\u0011!\t\u0006A!f\u0001\n\u0003\u0011\u0006\u0002C-\u0001\u0005#\u0005\u000b\u0011B*\t\u000bi\u0003A\u0011A.\t\u000f\u0005\u0004\u0011\u0011!C\u0001E\"9a\rAI\u0001\n\u00039\u0007b\u0002:\u0001#\u0003%\ta\u001d\u0005\bk\u0002\t\n\u0011\"\u0001w\u0011\u001dA\b!!A\u0005BeD\u0001\"a\u0001\u0001\u0003\u0003%\t!\u0011\u0005\n\u0003\u000b\u0001\u0011\u0011!C\u0001\u0003\u000fA\u0011\"a\u0005\u0001\u0003\u0003%\t%!\u0006\t\u0013\u0005\r\u0002!!A\u0005\u0002\u0005\u0015\u0002\"CA\u0018\u0001\u0005\u0005I\u0011IA\u0019\u0011%\t\u0019\u0004AA\u0001\n\u0003\n)dB\u0005\u0002:}\t\t\u0011#\u0001\u0002<\u0019AadHA\u0001\u0012\u0003\ti\u0004\u0003\u0004[)\u0011\u0005\u00111\n\u0005\n\u0003\u001b\"\u0012\u0011!C#\u0003\u001fB\u0011\"!\u0015\u0015\u0003\u0003%\t)a\u0015\t\u0011\u0005mC#%A\u0005\u0002MD\u0001\"!\u0018\u0015#\u0003%\tA\u001e\u0005\n\u0003?\"\u0012\u0011!CA\u0003CB\u0001\"a\u001c\u0015#\u0003%\ta\u001d\u0005\t\u0003c\"\u0012\u0013!C\u0001m\"I\u00111\u000f\u000b\u0002\u0002\u0013%\u0011Q\u000f\u0002\r/\u0016\u0014W\t_2faRLwN\u001c\u0006\u0003A\u0005\nQ\"\u001a=uKJt\u0017\r\u001c;bE2,'B\u0001\u0012$\u0003\u0015\u0019\b/\u0019:l\u0015\t!S%A\u0005he\u0016,g\u000e\u001d7v[*\u0011aeJ\u0001\ba&4x\u000e^1m\u0015\u0005A\u0013AA5p\u0007\u0001\u0019B\u0001A\u0016:{A\u0011AF\u000e\b\u0003[Mr!AL\u0019\u000e\u0003=R!\u0001M\u0015\u0002\rq\u0012xn\u001c;?\u0013\u0005\u0011\u0014!B:dC2\f\u0017B\u0001\u001b6\u0003\u001d\u0001\u0018mY6bO\u0016T\u0011AM\u0005\u0003oa\u0012\u0011\"\u0012=dKB$\u0018n\u001c8\u000b\u0005Q*\u0004C\u0001\u001e<\u001b\u0005)\u0014B\u0001\u001f6\u0005\u001d\u0001&o\u001c3vGR\u0004\"A\u000f \n\u0005}*$\u0001D*fe&\fG.\u001b>bE2,\u0017\u0001B2pI\u0016,\u0012A\u0011\t\u0003u\rK!\u0001R\u001b\u0003\u0007%sG/A\u0003d_\u0012,\u0007%A\u0004nKN\u001c\u0018mZ3\u0016\u0003!\u0003\"!S'\u000f\u0005)[\u0005C\u0001\u00186\u0013\taU'\u0001\u0004Qe\u0016$WMZ\u0005\u0003\u001d>\u0013aa\u0015;sS:<'B\u0001'6\u0003!iWm]:bO\u0016\u0004\u0013!B2bkN,W#A*\u0011\u0007i\"f+\u0003\u0002Vk\t1q\n\u001d;j_:\u0004\"\u0001L,\n\u0005aC$!\u0003+ie><\u0018M\u00197f\u0003\u0019\u0019\u0017-^:fA\u00051A(\u001b8jiz\"B\u0001\u00180`AB\u0011Q\fA\u0007\u0002?!)\u0001i\u0002a\u0001\u0005\"9ai\u0002I\u0001\u0002\u0004A\u0005bB)\b!\u0003\u0005\raU\u0001\u0005G>\u0004\u0018\u0010\u0006\u0003]G\u0012,\u0007b\u0002!\t!\u0003\u0005\rA\u0011\u0005\b\r\"\u0001\n\u00111\u0001I\u0011\u001d\t\u0006\u0002%AA\u0002M\u000babY8qs\u0012\"WMZ1vYR$\u0013'F\u0001iU\t\u0011\u0015nK\u0001k!\tY\u0007/D\u0001m\u0015\tig.A\u0005v]\u000eDWmY6fI*\u0011q.N\u0001\u000bC:tw\u000e^1uS>t\u0017BA9m\u0005E)hn\u00195fG.,GMV1sS\u0006t7-Z\u0001\u000fG>\u0004\u0018\u0010\n3fM\u0006,H\u000e\u001e\u00133+\u0005!(F\u0001%j\u00039\u0019w\u000e]=%I\u00164\u0017-\u001e7uIM*\u0012a\u001e\u0016\u0003'&\fQ\u0002\u001d:pIV\u001cG\u000f\u0015:fM&DX#\u0001>\u0011\u0007m\f\t!D\u0001}\u0015\tih0\u0001\u0003mC:<'\"A@\u0002\t)\fg/Y\u0005\u0003\u001dr\fA\u0002\u001d:pIV\u001cG/\u0011:jif\fa\u0002\u001d:pIV\u001cG/\u00127f[\u0016tG\u000f\u0006\u0003\u0002\n\u0005=\u0001c\u0001\u001e\u0002\f%\u0019\u0011QB\u001b\u0003\u0007\u0005s\u0017\u0010\u0003\u0005\u0002\u00129\t\t\u00111\u0001C\u0003\rAH%M\u0001\u0010aJ|G-^2u\u0013R,'/\u0019;peV\u0011\u0011q\u0003\t\u0007\u00033\ty\"!\u0003\u000e\u0005\u0005m!bAA\u000fk\u0005Q1m\u001c7mK\u000e$\u0018n\u001c8\n\t\u0005\u0005\u00121\u0004\u0002\t\u0013R,'/\u0019;pe\u0006A1-\u00198FcV\fG\u000e\u0006\u0003\u0002(\u00055\u0002c\u0001\u001e\u0002*%\u0019\u00111F\u001b\u0003\u000f\t{w\u000e\\3b]\"I\u0011\u0011\u0003\t\u0002\u0002\u0003\u0007\u0011\u0011B\u0001\tQ\u0006\u001c\bnQ8eKR\t!)\u0001\u0004fcV\fGn\u001d\u000b\u0005\u0003O\t9\u0004C\u0005\u0002\u0012I\t\t\u00111\u0001\u0002\n\u0005aq+\u001a2Fq\u000e,\u0007\u000f^5p]B\u0011Q\fF\n\u0005)\u0005}R\b\u0005\u0005\u0002B\u0005\u001d#\tS*]\u001b\t\t\u0019EC\u0002\u0002FU\nqA];oi&lW-\u0003\u0003\u0002J\u0005\r#!E!cgR\u0014\u0018m\u0019;Gk:\u001cG/[8ogQ\u0011\u00111H\u0001\ti>\u001cFO]5oOR\t!0A\u0003baBd\u0017\u0010F\u0004]\u0003+\n9&!\u0017\t\u000b\u0001;\u0002\u0019\u0001\"\t\u000f\u0019;\u0002\u0013!a\u0001\u0011\"9\u0011k\u0006I\u0001\u0002\u0004\u0019\u0016aD1qa2LH\u0005Z3gCVdG\u000f\n\u001a\u0002\u001f\u0005\u0004\b\u000f\\=%I\u00164\u0017-\u001e7uIM\nq!\u001e8baBd\u0017\u0010\u0006\u0003\u0002d\u0005-\u0004\u0003\u0002\u001eU\u0003K\u0002bAOA4\u0005\"\u001b\u0016bAA5k\t1A+\u001e9mKNB\u0001\"!\u001c\u001b\u0003\u0003\u0005\r\u0001X\u0001\u0004q\u0012\u0002\u0014a\u0007\u0013mKN\u001c\u0018N\\5uI\u001d\u0014X-\u0019;fe\u0012\"WMZ1vYR$#'A\u000e%Y\u0016\u001c8/\u001b8ji\u0012:'/Z1uKJ$C-\u001a4bk2$HeM\u0001\fe\u0016\fGMU3t_24X\r\u0006\u0002\u0002xA\u001910!\u001f\n\u0007\u0005mDP\u0001\u0004PE*,7\r\u001e")
public class WebException
extends Exception
implements Product,
Serializable {
    private final int code;
    private final String message;
    private final Option<Throwable> cause;

    public static Option<Throwable> $lessinit$greater$default$3() {
        return WebException$.MODULE$.$lessinit$greater$default$3();
    }

    public static String $lessinit$greater$default$2() {
        return WebException$.MODULE$.$lessinit$greater$default$2();
    }

    public static Option<Tuple3<Object, String, Option<Throwable>>> unapply(WebException webException) {
        return WebException$.MODULE$.unapply(webException);
    }

    public static Option<Throwable> apply$default$3() {
        return WebException$.MODULE$.apply$default$3();
    }

    public static String apply$default$2() {
        return WebException$.MODULE$.apply$default$2();
    }

    public static WebException apply(int n, String string, Option<Throwable> option) {
        return WebException$.MODULE$.apply(n, string, option);
    }

    public static Function1<Tuple3<Object, String, Option<Throwable>>, WebException> tupled() {
        return WebException$.MODULE$.tupled();
    }

    public static Function1<Object, Function1<String, Function1<Option<Throwable>, WebException>>> curried() {
        return WebException$.MODULE$.curried();
    }

    public int code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    public Option<Throwable> cause() {
        return this.cause;
    }

    public WebException copy(int code, String message, Option<Throwable> cause) {
        return new WebException(code, message, cause);
    }

    public int copy$default$1() {
        return this.code();
    }

    public String copy$default$2() {
        return this.message();
    }

    public Option<Throwable> copy$default$3() {
        return this.cause();
    }

    public String productPrefix() {
        return "WebException";
    }

    public int productArity() {
        return 3;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return BoxesRunTime.boxToInteger((int)this.code());
            }
            case 1: {
                return this.message();
            }
            case 2: {
                return this.cause();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof WebException;
    }

    public int hashCode() {
        int n = -889275714;
        n = Statics.mix((int)n, (int)this.code());
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.message()));
        n = Statics.mix((int)n, (int)Statics.anyHash(this.cause()));
        return Statics.finalizeHash((int)n, (int)3);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object x$1) {
        if (this == x$1) return true;
        Object object = x$1;
        if (!(object instanceof WebException)) return false;
        boolean bl = true;
        if (!bl) return false;
        WebException webException = (WebException)x$1;
        if (this.code() != webException.code()) return false;
        String string = this.message();
        String string2 = webException.message();
        if (string == null) {
            if (string2 != null) {
                return false;
            }
        } else if (!string.equals(string2)) return false;
        Option<Throwable> option = this.cause();
        Option<Throwable> option2 = webException.cause();
        if (option == null) {
            if (option2 != null) {
                return false;
            }
        } else if (!option.equals(option2)) return false;
        if (!webException.canEqual(this)) return false;
        return true;
    }

    public WebException(int code, String message, Option<Throwable> cause) {
        this.code = code;
        this.message = message;
        this.cause = cause;
        super(message);
        Product.$init$((Product)this);
    }
}

