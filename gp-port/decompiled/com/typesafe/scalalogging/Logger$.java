/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ClassTag
 */
package com.typesafe.scalalogging;

import com.typesafe.scalalogging.CanLog;
import com.typesafe.scalalogging.Logger;
import com.typesafe.scalalogging.LoggerTakingImplicit;
import org.slf4j.LoggerFactory;
import scala.Predef$;
import scala.Serializable;
import scala.collection.immutable.StringOps;
import scala.reflect.ClassTag;

public final class Logger$
implements Serializable {
    public static Logger$ MODULE$;

    static {
        new Logger$();
    }

    public Logger apply(org.slf4j.Logger underlying) {
        return new Logger(underlying);
    }

    public <A> LoggerTakingImplicit<A> takingImplicit(org.slf4j.Logger underlying, CanLog<A> ev) {
        return new LoggerTakingImplicit<A>(underlying, ev);
    }

    public Logger apply(String name) {
        return new Logger(LoggerFactory.getLogger(name));
    }

    public <A> LoggerTakingImplicit<A> takingImplicit(String name, CanLog<A> ev) {
        return new LoggerTakingImplicit<A>(LoggerFactory.getLogger(name), ev);
    }

    public Logger apply(Class<?> clazz) {
        return new Logger(LoggerFactory.getLogger(clazz.getName()));
    }

    public <A> LoggerTakingImplicit<A> takingImplicit(Class<?> clazz, CanLog<A> ev) {
        return new LoggerTakingImplicit<A>(LoggerFactory.getLogger(clazz.getName()), ev);
    }

    public <T> Logger apply(ClassTag<T> ct) {
        return new Logger(LoggerFactory.getLogger(new StringOps(Predef$.MODULE$.augmentString(ct.runtimeClass().getName())).stripSuffix("$")));
    }

    public <T, A> LoggerTakingImplicit<A> takingImplicit(ClassTag<T> ct, CanLog<A> ev) {
        return new LoggerTakingImplicit<A>(LoggerFactory.getLogger(new StringOps(Predef$.MODULE$.augmentString(ct.runtimeClass().getName())).stripSuffix("$")), ev);
    }

    private Object readResolve() {
        return MODULE$;
    }

    private Logger$() {
        MODULE$ = this;
    }
}

