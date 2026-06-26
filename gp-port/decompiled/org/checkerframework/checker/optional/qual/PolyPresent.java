/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.optional.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.optional.qual.MaybePresent;
import org.checkerframework.framework.qual.PolymorphicQualifier;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(value=MaybePresent.class)
public @interface PolyPresent {
}

