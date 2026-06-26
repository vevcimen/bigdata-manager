/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.message;

import org.postgresql.shaded.com.ongres.scram.common.ScramAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.ScramStringFormatting;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2Header;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;

public class ClientFinalMessage
implements StringWritable {
    private final String cbind;
    private final String nonce;
    private final byte[] proof;

    private static String generateCBind(Gs2Header gs2Header, byte[] cbindData) {
        StringBuffer sb = new StringBuffer();
        gs2Header.writeTo(sb).append(',');
        if (null != cbindData) {
            new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, ScramStringFormatting.base64Encode(cbindData)).writeTo(sb);
        }
        return sb.toString();
    }

    public ClientFinalMessage(Gs2Header gs2Header, byte[] cbindData, String nonce, byte[] proof) {
        this.cbind = ClientFinalMessage.generateCBind(Preconditions.checkNotNull(gs2Header, "gs2Header"), cbindData);
        this.nonce = Preconditions.checkNotEmpty(nonce, "nonce");
        this.proof = Preconditions.checkNotNull(proof, "proof");
    }

    private static StringBuffer writeToWithoutProof(StringBuffer sb, String cbind, String nonce) {
        return StringWritableCsv.writeTo(sb, new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, ScramStringFormatting.base64Encode(cbind)), new ScramAttributeValue(ScramAttributes.NONCE, nonce));
    }

    private static StringBuffer writeToWithoutProof(StringBuffer sb, Gs2Header gs2Header, byte[] cbindData, String nonce) {
        return ClientFinalMessage.writeToWithoutProof(sb, ClientFinalMessage.generateCBind(Preconditions.checkNotNull(gs2Header, "gs2Header"), cbindData), nonce);
    }

    public static StringBuffer writeToWithoutProof(Gs2Header gs2Header, byte[] cbindData, String nonce) {
        return ClientFinalMessage.writeToWithoutProof(new StringBuffer(), gs2Header, cbindData, nonce);
    }

    @Override
    public StringBuffer writeTo(StringBuffer sb) {
        ClientFinalMessage.writeToWithoutProof(sb, this.cbind, this.nonce);
        return StringWritableCsv.writeTo(sb, null, new ScramAttributeValue(ScramAttributes.CLIENT_PROOF, ScramStringFormatting.base64Encode(this.proof)));
    }

    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}

