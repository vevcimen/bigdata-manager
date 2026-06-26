/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.i18n.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.i18n.qual.UnknownLocalized;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(value={UnknownLocalized.class})
@QualifierForLiterals(value={LiteralKind.INT, LiteralKind.LONG, LiteralKind.FLOAT, LiteralKind.DOUBLE, LiteralKind.BOOLEAN})
public @interface Localized {
}

