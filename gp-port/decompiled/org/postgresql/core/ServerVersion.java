/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.text.NumberFormat;
import java.text.ParsePosition;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Version;

public enum ServerVersion implements Version
{
    INVALID("0.0.0"),
    v8_2("8.2.0"),
    v8_3("8.3.0"),
    v8_4("8.4.0"),
    v9_0("9.0.0"),
    v9_1("9.1.0"),
    v9_2("9.2.0"),
    v9_3("9.3.0"),
    v9_4("9.4.0"),
    v9_5("9.5.0"),
    v9_6("9.6.0"),
    v10("10"),
    v11("11"),
    v12("12"),
    v13("13"),
    v14("14"),
    v15("15"),
    v16("16");

    private final int version;

    private ServerVersion(String version) {
        this.version = ServerVersion.parseServerVersionStr(version);
    }

    @Override
    public int getVersionNum() {
        return this.version;
    }

    public static Version from(@Nullable String version) {
        final int versionNum = ServerVersion.parseServerVersionStr(version);
        return new Version(){

            @Override
            public int getVersionNum() {
                return versionNum;
            }

            public boolean equals(@Nullable Object obj) {
                if (obj instanceof Version) {
                    return this.getVersionNum() == ((Version)obj).getVersionNum();
                }
                return false;
            }

            public int hashCode() {
                return this.getVersionNum();
            }

            public String toString() {
                return Integer.toString(versionNum);
            }
        };
    }

    static int parseServerVersionStr(@Nullable String serverVersion) throws NumberFormatException {
        Number part;
        int versionParts;
        if (serverVersion == null) {
            return 0;
        }
        NumberFormat numformat = NumberFormat.getIntegerInstance();
        numformat.setGroupingUsed(false);
        ParsePosition parsepos = new ParsePosition(0);
        int[] parts = new int[3];
        for (versionParts = 0; versionParts < 3 && (part = (Number)numformat.parseObject(serverVersion, parsepos)) != null; ++versionParts) {
            parts[versionParts] = part.intValue();
            if (parsepos.getIndex() == serverVersion.length() || serverVersion.charAt(parsepos.getIndex()) != '.') break;
            parsepos.setIndex(parsepos.getIndex() + 1);
        }
        ++versionParts;
        if (parts[0] >= 10000) {
            if (parsepos.getIndex() == serverVersion.length() && versionParts == 1) {
                return parts[0];
            }
            throw new NumberFormatException("First major-version part equal to or greater than 10000 in invalid version string: " + serverVersion);
        }
        if (versionParts >= 3) {
            if (parts[1] > 99) {
                throw new NumberFormatException("Unsupported second part of major version > 99 in invalid version string: " + serverVersion);
            }
            if (parts[2] > 99) {
                throw new NumberFormatException("Unsupported second part of minor version > 99 in invalid version string: " + serverVersion);
            }
            return (parts[0] * 100 + parts[1]) * 100 + parts[2];
        }
        if (versionParts == 2) {
            if (parts[0] >= 10) {
                return parts[0] * 100 * 100 + parts[1];
            }
            if (parts[1] > 99) {
                throw new NumberFormatException("Unsupported second part of major version > 99 in invalid version string: " + serverVersion);
            }
            return (parts[0] * 100 + parts[1]) * 100;
        }
        if (versionParts == 1 && parts[0] >= 10) {
            return parts[0] * 100 * 100;
        }
        return 0;
    }
}

