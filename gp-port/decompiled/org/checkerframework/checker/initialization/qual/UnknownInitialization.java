/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.initialization.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(value={})
@DefaultFor(value={TypeUseLocation.LOCAL_VARIABLE, TypeUseLocation.RESOURCE_VARIABLE})
public @interface UnknownInitialization {
    public Class<?> value() default Object.class;
}

