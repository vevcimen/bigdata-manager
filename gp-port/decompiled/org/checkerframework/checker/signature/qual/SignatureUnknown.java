/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.signature.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={})
@DefaultQualifierInHierarchy
@SubtypeOf(value={})
public @interface SignatureUnknown {
}

