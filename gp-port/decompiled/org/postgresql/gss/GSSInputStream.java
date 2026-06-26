/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.gss;

import java.io.IOException;
import java.io.InputStream;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import org.postgresql.util.internal.Nullness;

public class GSSInputStream
extends InputStream {
    private GSSContext gssContext;
    private MessageProp messageProp;
    private InputStream wrapped;
    byte @Nullable [] unencrypted;
    int unencryptedPos;
    int unencryptedLength;

    public GSSInputStream(InputStream wrapped, GSSContext gssContext, MessageProp messageProp) {
        this.wrapped = wrapped;
        this.gssContext = gssContext;
        this.messageProp = messageProp;
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] buffer, int pos, int len) throws IOException {
        byte[] int4Buf = new byte[4];
        int copyLength = 0;
        if (this.unencryptedLength > 0) {
            copyLength = Math.min(len, this.unencryptedLength);
            System.arraycopy(Nullness.castNonNull(this.unencrypted), this.unencryptedPos, buffer, pos, copyLength);
            this.unencryptedLength -= copyLength;
            this.unencryptedPos += copyLength;
        } else if (this.wrapped.read(int4Buf, 0, 4) == 4) {
            int encryptedLength = (int4Buf[0] & 0xFF) << 24 | (int4Buf[1] & 0xFF) << 16 | (int4Buf[2] & 0xFF) << 8 | int4Buf[3] & 0xFF;
            byte[] encryptedBuffer = new byte[encryptedLength];
            this.wrapped.read(encryptedBuffer, 0, encryptedLength);
            try {
                byte[] unencrypted = this.gssContext.unwrap(encryptedBuffer, 0, encryptedLength, this.messageProp);
                this.unencrypted = unencrypted;
                this.unencryptedLength = unencrypted.length;
                this.unencryptedPos = 0;
                copyLength = Math.min(len, unencrypted.length);
                System.arraycopy(unencrypted, this.unencryptedPos, buffer, pos, copyLength);
                this.unencryptedLength -= copyLength;
                this.unencryptedPos += copyLength;
            }
            catch (GSSException e) {
                throw new IOException(e);
            }
            return copyLength;
        }
        return copyLength;
    }
}

