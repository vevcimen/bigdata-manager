/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.common.value.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.common.value.qual.MinLen;
import org.checkerframework.framework.qual.ConditionalPostconditionAnnotation;
import org.checkerframework.framework.qual.InheritedAnnotation;
import org.checkerframework.framework.qual.QualifierArgument;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
@ConditionalPostconditionAnnotation(qualifier=MinLen.class)
@InheritedAnnotation
@Repeatable(value=List.class)
public @interface EnsuresMinLenIf {
    public String[] expression();

    public boolean result();

    @QualifierArgument(value="value")
    public int targetValue() default 0;

    @Documented
    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
    @ConditionalPostconditionAnnotation(qualifier=MinLen.class)
    @InheritedAnnotation
    public static @interface List {
        public EnsuresMinLenIf[] value();
    }
}

