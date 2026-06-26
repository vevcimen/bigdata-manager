/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.index.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@SubtypeOf(value={LessThan.class})
public @interface LessThanBottom {
}

