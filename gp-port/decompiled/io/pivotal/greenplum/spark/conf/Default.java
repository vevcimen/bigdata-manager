/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.reflect.ScalaSignature
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark.conf;

import io.pivotal.greenplum.spark.conf.Default$;
import io.pivotal.greenplum.spark.conf.WhatIfMissing;
import scala.Function1;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u00055b\u0001B\u000b\u0017\u0001\u0006B\u0001B\r\u0001\u0003\u0016\u0004%\ta\r\u0005\t\u007f\u0001\u0011\t\u0012)A\u0005i!)\u0001\t\u0001C\u0001\u0003\"9A\tAA\u0001\n\u0003)\u0005bB$\u0001#\u0003%\t\u0001\u0013\u0005\b'\u0002\t\t\u0011\"\u0011U\u0011\u001da\u0006!!A\u0005\u0002uCq!\u0019\u0001\u0002\u0002\u0013\u0005!\rC\u0004i\u0001\u0005\u0005I\u0011I5\t\u000fA\u0004\u0011\u0011!C\u0001c\"9a\u000fAA\u0001\n\u0003:\bb\u0002=\u0001\u0003\u0003%\t%\u001f\u0005\bu\u0002\t\t\u0011\"\u0011|\u000f\u001dih#!A\t\u0002y4q!\u0006\f\u0002\u0002#\u0005q\u0010\u0003\u0004A\u001f\u0011\u0005\u0011Q\u0002\u0005\bq>\t\t\u0011\"\u0012z\u0011%\tyaDA\u0001\n\u0003\u000b\t\u0002C\u0005\u0002\u0016=\t\t\u0011\"!\u0002\u0018!I\u00111E\b\u0002\u0002\u0013%\u0011Q\u0005\u0002\b\t\u00164\u0017-\u001e7u\u0015\t9\u0002$\u0001\u0003d_:4'BA\r\u001b\u0003\u0015\u0019\b/\u0019:l\u0015\tYB$A\u0005he\u0016,g\u000e\u001d7v[*\u0011QDH\u0001\ba&4x\u000e^1m\u0015\u0005y\u0012AA5p\u0007\u0001\u0019R\u0001\u0001\u0012)Y=\u0002\"a\t\u0014\u000e\u0003\u0011R\u0011!J\u0001\u0006g\u000e\fG.Y\u0005\u0003O\u0011\u0012a!\u00118z%\u00164\u0007CA\u0015+\u001b\u00051\u0012BA\u0016\u0017\u000559\u0006.\u0019;JM6K7o]5oOB\u00111%L\u0005\u0003]\u0011\u0012q\u0001\u0015:pIV\u001cG\u000f\u0005\u0002$a%\u0011\u0011\u0007\n\u0002\r'\u0016\u0014\u0018.\u00197ju\u0006\u0014G.Z\u0001\u0006m\u0006dW/Z\u000b\u0002iA\u0011Q\u0007\u0010\b\u0003mi\u0002\"a\u000e\u0013\u000e\u0003aR!!\u000f\u0011\u0002\rq\u0012xn\u001c;?\u0013\tYD%\u0001\u0004Qe\u0016$WMZ\u0005\u0003{y\u0012aa\u0015;sS:<'BA\u001e%\u0003\u00191\u0018\r\\;fA\u00051A(\u001b8jiz\"\"AQ\"\u0011\u0005%\u0002\u0001\"\u0002\u001a\u0004\u0001\u0004!\u0014\u0001B2paf$\"A\u0011$\t\u000fI\"\u0001\u0013!a\u0001i\u0005q1m\u001c9zI\u0011,g-Y;mi\u0012\nT#A%+\u0005QR5&A&\u0011\u00051\u000bV\"A'\u000b\u00059{\u0015!C;oG\",7m[3e\u0015\t\u0001F%\u0001\u0006b]:|G/\u0019;j_:L!AU'\u0003#Ut7\r[3dW\u0016$g+\u0019:jC:\u001cW-A\u0007qe>$Wo\u0019;Qe\u00164\u0017\u000e_\u000b\u0002+B\u0011akW\u0007\u0002/*\u0011\u0001,W\u0001\u0005Y\u0006twMC\u0001[\u0003\u0011Q\u0017M^1\n\u0005u:\u0016\u0001\u00049s_\u0012,8\r^!sSRLX#\u00010\u0011\u0005\rz\u0016B\u00011%\u0005\rIe\u000e^\u0001\u000faJ|G-^2u\u000b2,W.\u001a8u)\t\u0019g\r\u0005\u0002$I&\u0011Q\r\n\u0002\u0004\u0003:L\bbB4\t\u0003\u0003\u0005\rAX\u0001\u0004q\u0012\n\u0014a\u00049s_\u0012,8\r^%uKJ\fGo\u001c:\u0016\u0003)\u00042a\u001b8d\u001b\u0005a'BA7%\u0003)\u0019w\u000e\u001c7fGRLwN\\\u0005\u0003_2\u0014\u0001\"\u0013;fe\u0006$xN]\u0001\tG\u0006tW)];bYR\u0011!/\u001e\t\u0003GML!\u0001\u001e\u0013\u0003\u000f\t{w\u000e\\3b]\"9qMCA\u0001\u0002\u0004\u0019\u0017\u0001\u00035bg\"\u001cu\u000eZ3\u0015\u0003y\u000b\u0001\u0002^8TiJLgn\u001a\u000b\u0002+\u00061Q-];bYN$\"A\u001d?\t\u000f\u001dl\u0011\u0011!a\u0001G\u00069A)\u001a4bk2$\bCA\u0015\u0010'\u0011y\u0011\u0011A\u0018\u0011\r\u0005\r\u0011\u0011\u0002\u001bC\u001b\t\t)AC\u0002\u0002\b\u0011\nqA];oi&lW-\u0003\u0003\u0002\f\u0005\u0015!!E!cgR\u0014\u0018m\u0019;Gk:\u001cG/[8ocQ\ta0A\u0003baBd\u0017\u0010F\u0002C\u0003'AQA\r\nA\u0002Q\nq!\u001e8baBd\u0017\u0010\u0006\u0003\u0002\u001a\u0005}\u0001\u0003B\u0012\u0002\u001cQJ1!!\b%\u0005\u0019y\u0005\u000f^5p]\"A\u0011\u0011E\n\u0002\u0002\u0003\u0007!)A\u0002yIA\n1B]3bIJ+7o\u001c7wKR\u0011\u0011q\u0005\t\u0004-\u0006%\u0012bAA\u0016/\n1qJ\u00196fGR\u0004")
public class Default
implements WhatIfMissing,
Product,
Serializable {
    private final String value;

    public static Option<String> unapply(Default default_) {
        return Default$.MODULE$.unapply(default_);
    }

    public static Default apply(String string) {
        return Default$.MODULE$.apply(string);
    }

    public static <A> Function1<String, A> andThen(Function1<Default, A> function1) {
        return Default$.MODULE$.andThen(function1);
    }

    public static <A> Function1<A, Default> compose(Function1<A, String> function1) {
        return Default$.MODULE$.compose(function1);
    }

    public String value() {
        return this.value;
    }

    public Default copy(String value) {
        return new Default(value);
    }

    public String copy$default$1() {
        return this.value();
    }

    public String productPrefix() {
        return "Default";
    }

    public int productArity() {
        return 1;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.value();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof Default;
    }

    public int hashCode() {
        return ScalaRunTime$.MODULE$._hashCode((Product)this);
    }

    public String toString() {
        return ScalaRunTime$.MODULE$._toString((Product)this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object x$1) {
        if (this == x$1) return true;
        Object object = x$1;
        if (!(object instanceof Default)) return false;
        boolean bl = true;
        if (!bl) return false;
        Default default_ = (Default)x$1;
        String string = this.value();
        String string2 = default_.value();
        if (string == null) {
            if (string2 != null) {
                return false;
            }
        } else if (!string.equals(string2)) return false;
        if (!default_.canEqual(this)) return false;
        return true;
    }

    public Default(String value) {
        this.value = value;
        Product.$init$((Product)this);
    }
}

