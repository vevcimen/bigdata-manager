/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.tsv;

import java.util.Map;
import shadeio.univocity.parsers.common.CommonParserSettings;
import shadeio.univocity.parsers.tsv.TsvFormat;

public class TsvParserSettings
extends CommonParserSettings<TsvFormat> {
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
    public final TsvParserSettings clone() {
        return (TsvParserSettings)super.clone();
    }

    @Override
    public final TsvParserSettings clone(boolean clearInputSpecificSettings) {
        return (TsvParserSettings)super.clone(clearInputSpecificSettings);
    }
}

