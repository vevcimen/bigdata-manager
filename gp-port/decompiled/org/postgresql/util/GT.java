/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public class GT {
    private static final GT _gt = new GT();
    private static final Object[] noargs = new Object[0];
    private @Nullable ResourceBundle bundle;

    @Pure
    public static String tr(String message, Object ... args) {
        return _gt.translate(message, args);
    }

    private GT() {
        try {
            this.bundle = ResourceBundle.getBundle("org.postgresql.translation.messages", Locale.getDefault(Locale.Category.DISPLAY));
        }
        catch (MissingResourceException mre) {
            this.bundle = null;
        }
    }

    private String translate(String message, @Nullable Object[] args) {
        if (this.bundle != null && message != null) {
            try {
                message = this.bundle.getString(message);
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
        }
        if (args == null) {
            args = noargs;
        }
        if (message != null) {
            message = MessageFormat.format(message, args);
        }
        return message;
    }
}

