/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.tsv;

import java.util.Map;
import shadeio.univocity.parsers.common.CommonWriterSettings;
import shadeio.univocity.parsers.tsv.TsvFormat;

public class TsvWriterSettings
extends CommonWriterSettings<TsvFormat> {
    private boolean lineJoiningEnabled = false;

    public boolean isLineJoiningEnabled() {
        return this.lineJoiningEnabled;
    }

    public void setLineJoiningEnabled(boolean lineJoiningEnabled) {
        this.lineJoiningEnabled = lineJoiningEnabled;
    }

    @Override
    protected TsvFormat createDefaultFormat() {
        return new TsvFormat();
    }

    @Override
    protected void addConfiguration(Map<String, Object> out) {
        super.addConfiguration(out);
    }

    @Override
    public final TsvWriterSettings clone() {
        return (TsvWriterSettings)super.clone();
    }

    @Override
    public final TsvWriterSettings clone(boolean clearInputSpecificSettings) {
        return (TsvWriterSettings)super.clone(clearInputSpecificSettings);
    }
}

