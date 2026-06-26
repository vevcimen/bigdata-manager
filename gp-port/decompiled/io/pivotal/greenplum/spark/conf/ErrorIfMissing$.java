/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Product
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark.conf;

import io.pivotal.greenplum.spark.conf.WhatIfMissing;
import scala.Product;
import scala.Serializable;
import scala.collection.Iterator;
import scala.runtime.ScalaRunTime$;

public final class ErrorIfMissing$
implements WhatIfMissing,
Product,
Serializable {
    public static ErrorIfMissing$ MODULE$;

    static {
        new ErrorIfMissing$();
    }

    public String productPrefix() {
        return "ErrorIfMissing";
    }

    public int productArity() {
        return 0;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof ErrorIfMissing$;
    }

    public int hashCode() {
        return 709649537;
    }

    public String toString() {
        return "ErrorIfMissing";
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ErrorIfMissing$() {
        MODULE$ = this;
        Product.$init$((Product)this);
    }
}

