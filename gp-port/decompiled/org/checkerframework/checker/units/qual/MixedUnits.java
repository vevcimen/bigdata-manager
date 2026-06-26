/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={})
@SubtypeOf(value={UnknownUnits.class})
public @interface MixedUnits {
}

