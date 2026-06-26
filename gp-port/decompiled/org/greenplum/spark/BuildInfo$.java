/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Predef$
 *  scala.Product
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.collection.Seq
 *  scala.collection.immutable.StringOps
 *  scala.runtime.ScalaRunTime$
 */
package org.greenplum.spark;

import scala.Predef$;
import scala.Product;
import scala.Serializable;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.immutable.StringOps;
import scala.runtime.ScalaRunTime$;

public final class BuildInfo$
implements Product,
Serializable {
    public static BuildInfo$ MODULE$;
    private final String name;
    private final String version;
    private final String scalaVersion;
    private final String sbtVersion;
    private final String sparkVersion;
    private final String toString;

    static {
        new BuildInfo$();
    }

    public String name() {
        return this.name;
    }

    public String version() {
        return this.version;
    }

    public String scalaVersion() {
        return this.scalaVersion;
    }

    public String sbtVersion() {
        return this.sbtVersion;
    }

    public String sparkVersion() {
        return this.sparkVersion;
    }

    public String toString() {
        return this.toString;
    }

    public String productPrefix() {
        return "BuildInfo";
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
        return x$1 instanceof BuildInfo$;
    }

    public int hashCode() {
        return 602658844;
    }

    private Object readResolve() {
        return MODULE$;
    }

    private BuildInfo$() {
        MODULE$ = this;
        Product.$init$((Product)this);
        this.name = "greenplum-spark";
        this.version = "2.2.0";
        this.scalaVersion = "2.12.17";
        this.sbtVersion = "1.8.2";
        this.sparkVersion = "3.0.3";
        this.toString = new StringOps(Predef$.MODULE$.augmentString("name: %s, version: %s, scalaVersion: %s, sbtVersion: %s, sparkVersion: %s")).format((Seq)Predef$.MODULE$.genericWrapArray((Object)new Object[]{this.name(), this.version(), this.scalaVersion(), this.sbtVersion(), this.sparkVersion()}));
    }
}

