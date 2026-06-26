/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.initialization.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(value={UnknownInitialization.class})
public @interface UnderInitialization {
    public Class<?> value() default Object.class;
}

