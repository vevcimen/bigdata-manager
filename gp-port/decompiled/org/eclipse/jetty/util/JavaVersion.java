/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

public class JavaVersion {
    public static final String JAVA_TARGET_PLATFORM = "org.eclipse.jetty.javaTargetPlatform";
    public static final JavaVersion VERSION = JavaVersion.parse(System.getProperty("java.version"));
    private final String version;
    private final int platform;
    private final int major;
    private final int minor;
    private final int micro;

    public static JavaVersion parse(String v) {
        String[] split = v.split("[^0-9]");
        int len = Math.min(split.length, 3);
        int[] version = new int[len];
        for (int i = 0; i < len; ++i) {
            try {
                version[i] = Integer.parseInt(split[i]);
                continue;
            }
            catch (Throwable e) {
                len = i - 1;
                break;
            }
        }
        return new JavaVersion(v, version[0] >= 9 || len == 1 ? version[0] : version[1], version[0], len > 1 ? version[1] : 0, len > 2 ? version[2] : 0);
    }

    private JavaVersion(String version, int platform, int major, int minor, int micro) {
        this.version = version;
        this.platform = platform;
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    public String getVersion() {
        return this.version;
    }

    public int getPlatform() {
        return this.platform;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getMicro() {
        return this.micro;
    }

    @Deprecated
    public int getUpdate() {
        return 0;
    }

    @Deprecated
    public String getSuffix() {
        return null;
    }

    public String toString() {
        return this.version;
    }
}

