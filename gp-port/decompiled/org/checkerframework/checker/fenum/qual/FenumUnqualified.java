/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.fenum.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.fenum.qual.FenumTop;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={})
@SubtypeOf(value={FenumTop.class})
@DefaultQualifierInHierarchy
@DefaultFor(value={TypeUseLocation.EXCEPTION_PARAMETER})
public @interface FenumUnqualified {
}

