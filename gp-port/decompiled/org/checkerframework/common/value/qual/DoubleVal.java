/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.common.value.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@SubtypeOf(value={UnknownVal.class})
public @interface DoubleVal {
    public double[] value();
}

