/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
public @interface UnitsRelations {
    public Class<?> value();
}

