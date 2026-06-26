/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Target(value={ElementType.METHOD})
public @interface Copy {
    public Class to();

    public String property() default "";
}

