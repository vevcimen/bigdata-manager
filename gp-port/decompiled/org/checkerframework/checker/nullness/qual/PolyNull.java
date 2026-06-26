/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.PolymorphicQualifier;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(value=Nullable.class)
public @interface PolyNull {
}

