/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.common.reflection.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.common.reflection.qual.UnknownClass;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(value={UnknownClass.class})
public @interface ClassVal {
    public String[] value();
}

