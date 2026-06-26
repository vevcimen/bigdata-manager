/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.PreconditionAnnotation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
@PreconditionAnnotation(qualifier=NonNull.class)
public @interface RequiresNonNull {
    public String[] value();
}

