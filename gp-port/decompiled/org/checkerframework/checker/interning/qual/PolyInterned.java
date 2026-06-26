/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.interning.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.interning.qual.UnknownInterned;
import org.checkerframework.framework.qual.PolymorphicQualifier;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(value=UnknownInterned.class)
public @interface PolyInterned {
}

