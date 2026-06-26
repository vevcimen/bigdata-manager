/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.gssapi;

import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;

public enum Gs2CbindFlag implements CharAttribute
{
    CLIENT_NOT('n'),
    CLIENT_YES_SERVER_NOT('y'),
    CHANNEL_BINDING_REQUIRED('p');

    private final char flag;

    private Gs2CbindFlag(char flag) {
        this.flag = flag;
    }

    @Override
    public char getChar() {
        return this.flag;
    }

    public static Gs2CbindFlag byChar(char c) {
        switch (c) {
            case 'n': {
                return CLIENT_NOT;
            }
            case 'y': {
                return CLIENT_YES_SERVER_NOT;
            }
            case 'p': {
                return CHANNEL_BINDING_REQUIRED;
            }
        }
        throw new IllegalArgumentException("Invalid Gs2CbindFlag character '" + c + "'");
    }
}

