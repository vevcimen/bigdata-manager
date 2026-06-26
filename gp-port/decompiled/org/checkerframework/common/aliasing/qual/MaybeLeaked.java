/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.common.aliasing.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.common.aliasing.qual.LeakedToResult;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={})
@DefaultQualifierInHierarchy
@SubtypeOf(value={LeakedToResult.class})
@InvisibleQualifier
public @interface MaybeLeaked {
}

