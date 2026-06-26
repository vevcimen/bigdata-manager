/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.units.qual;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.checkerframework.checker.units.qual.Prefix;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
public @interface UnitsMultiple {
    public Class<? extends Annotation> quantity();

    public Prefix prefix() default Prefix.one;
}

