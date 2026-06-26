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
import org.checkerframework.framework.qual.TypeUseLocation;

@Documented
@Retention(value=RetentionPolicy.SOURCE)
@Target(value={ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Repeatable(value=List.class)
public @interface DefaultQualifier {
    public Class<? extends Annotation> value();

    public TypeUseLocation[] locations() default {TypeUseLocation.ALL};

    @Documented
    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
    public static @interface List {
        public DefaultQualifier[] value();
    }
}

