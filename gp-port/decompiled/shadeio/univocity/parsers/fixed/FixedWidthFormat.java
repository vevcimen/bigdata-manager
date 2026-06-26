/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.util.TreeMap;
import shadeio.univocity.parsers.common.Format;

public class FixedWidthFormat
extends Format {
    private char padding = (char)32;
    private char lookupWildcard = (char)63;

    public char getPadding() {
        return this.padding;
    }

    public void setPadding(char padding) {
        this.padding = padding;
    }

    public boolean isPadding(char padding) {
        return this.padding == padding;
    }

    @Override
    protected TreeMap<String, Object> getConfiguration() {
        TreeMap<String, Object> out = new TreeMap<String, Object>();
        out.put("Padding", Character.valueOf(this.padding));
        return out;
    }

    @Override
    public final FixedWidthFormat clone() {
        return (FixedWidthFormat)super.clone();
    }

    public char getLookupWildcard() {
        return this.lookupWildcard;
    }

    public void setLookupWildcard(char lookupWildcard) {
        this.lookupWildcard = lookupWildcard;
    }
}

