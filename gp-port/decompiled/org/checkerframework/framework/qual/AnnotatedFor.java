/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.framework.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.SOURCE)
@Target(value={ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE})
public @interface AnnotatedFor {
    public String[] value();
}

