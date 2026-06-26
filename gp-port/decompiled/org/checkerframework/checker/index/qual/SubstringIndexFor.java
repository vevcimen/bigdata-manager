/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.index.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.index.qual.SubstringIndexUnknown;
import org.checkerframework.framework.qual.JavaExpression;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(value={SubstringIndexUnknown.class})
public @interface SubstringIndexFor {
    @JavaExpression
    public String[] value();

    @JavaExpression
    public String[] offset();
}

