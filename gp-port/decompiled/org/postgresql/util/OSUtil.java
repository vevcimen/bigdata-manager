/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.File;
import java.util.Locale;

public class OSUtil {
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows");
    }

    public static String getUserConfigRootDirectory() {
        if (OSUtil.isWindows()) {
            return System.getenv("APPDATA") + File.separator + "postgresql";
        }
        return System.getProperty("user.home");
    }
}

