/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.signature.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.signature.qual.BinaryNameWithoutPackage;
import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers;
import org.checkerframework.checker.signature.qual.IdentifierOrPrimitiveType;
import org.checkerframework.framework.qual.SubtypeOf;

@SubtypeOf(value={DotSeparatedIdentifiers.class, BinaryNameWithoutPackage.class, IdentifierOrPrimitiveType.class})
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Identifier {
}

