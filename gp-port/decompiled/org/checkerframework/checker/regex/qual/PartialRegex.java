/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.regex.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.regex.qual.UnknownRegex;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={})
@InvisibleQualifier
@SubtypeOf(value={UnknownRegex.class})
public @interface PartialRegex {
    public String value() default "";
}

