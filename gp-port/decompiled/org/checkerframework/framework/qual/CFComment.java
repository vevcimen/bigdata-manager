/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.framework.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(value=RetentionPolicy.SOURCE)
public @interface CFComment {
    public String[] value();
}

