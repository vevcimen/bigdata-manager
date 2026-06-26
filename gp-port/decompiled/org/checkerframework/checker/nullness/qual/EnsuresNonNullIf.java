/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.ConditionalPostconditionAnnotation;
import org.checkerframework.framework.qual.InheritedAnnotation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
@ConditionalPostconditionAnnotation(qualifier=NonNull.class)
@InheritedAnnotation
@Repeatable(value=List.class)
public @interface EnsuresNonNullIf {
    public String[] expression();

    public boolean result();

    @Documented
    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
    @ConditionalPostconditionAnnotation(qualifier=NonNull.class)
    @InheritedAnnotation
    public static @interface List {
        public EnsuresNonNullIf[] value();
    }
}

