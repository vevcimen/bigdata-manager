/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.checker.lock.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.lock.qual.GuardedByUnknown;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE_USE})
@TargetLocations(value={TypeUseLocation.RECEIVER, TypeUseLocation.PARAMETER, TypeUseLocation.RETURN})
@SubtypeOf(value={GuardedByUnknown.class})
public @interface GuardSatisfied {
    public int value() default -1;
}

