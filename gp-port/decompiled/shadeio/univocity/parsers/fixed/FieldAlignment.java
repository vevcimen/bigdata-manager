/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

public enum FieldAlignment {
    LEFT,
    CENTER,
    RIGHT;


    public int calculatePadding(int totalLength, int lengthToWrite) {
        if (this == LEFT || totalLength <= lengthToWrite) {
            return 0;
        }
        if (this == RIGHT) {
            return totalLength - lengthToWrite;
        }
        int padding = totalLength / 2 - lengthToWrite / 2;
        if (lengthToWrite + padding > totalLength) {
            --padding;
        }
        return padding;
    }
}

