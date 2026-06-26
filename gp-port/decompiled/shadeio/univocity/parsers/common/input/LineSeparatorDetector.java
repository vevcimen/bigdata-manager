/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import shadeio.univocity.parsers.common.input.InputAnalysisProcess;

public abstract class LineSeparatorDetector
implements InputAnalysisProcess {
    @Override
    public void execute(char[] characters, int length) {
        int separator1 = 0;
        int separator2 = 0;
        for (int c = 0; c < length; ++c) {
            int ch = characters[c];
            if (ch == 10 || ch == 13) {
                if (separator1 == 0) {
                    separator1 = ch;
                    continue;
                }
                separator2 = ch;
                break;
            }
            if (separator1 != 0) break;
        }
        int lineSeparator1 = separator1;
        int lineSeparator2 = separator2;
        if (separator1 != 0) {
            if (separator1 == 10) {
                lineSeparator1 = 10;
                lineSeparator2 = 0;
            } else {
                lineSeparator1 = 13;
                lineSeparator2 = separator2 == 10 ? 10 : 0;
            }
        }
        this.apply((char)lineSeparator1, (char)lineSeparator2);
    }

    protected abstract void apply(char var1, char var2);
}

