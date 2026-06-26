/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.tainting.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(value={Tainted.class})
@QualifierForLiterals(value={LiteralKind.STRING})
@DefaultFor(value={TypeUseLocation.LOWER_BOUND})
public @interface Untainted {
}

