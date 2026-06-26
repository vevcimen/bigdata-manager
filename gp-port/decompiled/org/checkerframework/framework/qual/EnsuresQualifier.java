/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.framework.qual;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.InheritedAnnotation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
@InheritedAnnotation
@Repeatable(value=List.class)
public @interface EnsuresQualifier {
    public String[] expression();

    public Class<? extends Annotation> qualifier();

    @Documented
    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
    @InheritedAnnotation
    public static @interface List {
        public EnsuresQualifier[] value();
    }
}

