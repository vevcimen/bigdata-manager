/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.message;

import org.postgresql.shaded.com.ongres.scram.common.ScramAttributeValue;
import org.postgresql.shaded.com.ongres.scram.common.ScramAttributes;
import org.postgresql.shaded.com.ongres.scram.common.ScramStringFormatting;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2Header;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritableCsv;

public class ClientFirstMessage
implements StringWritable {
    private final Gs2Header gs2Header;
    private final String user;
    private final String nonce;

    public ClientFirstMessage(Gs2Header gs2Header, String user, String nonce) throws IllegalArgumentException {
        this.gs2Header = Preconditions.checkNotNull(gs2Header, "gs2Header");
        this.user = Preconditions.checkNotEmpty(user, "user");
        this.nonce = Preconditions.checkNotEmpty(nonce, "nonce");
    }

    private static Gs2Header gs2Header(Gs2CbindFlag gs2CbindFlag, String authzid, String cbindName) {
        Preconditions.checkNotNull(gs2CbindFlag, "gs2CbindFlag");
        if (Gs2CbindFlag.CHANNEL_BINDING_REQUIRED == gs2CbindFlag && null == cbindName) {
            throw new IllegalArgumentException("Channel binding name is required if channel binding is specified");
        }
        return new Gs2Header(gs2CbindFlag, cbindName, authzid);
    }

    public ClientFirstMessage(Gs2CbindFlag gs2CbindFlag, String authzid, String cbindName, String user, String nonce) {
        this(ClientFirstMessage.gs2Header(gs2CbindFlag, authzid, cbindName), user, nonce);
    }

    public ClientFirstMessage(String user, String nonce) {
        this(ClientFirstMessage.gs2Header(Gs2CbindFlag.CLIENT_NOT, null, null), user, nonce);
    }

    public Gs2CbindFlag getChannelBindingFlag() {
        return this.gs2Header.getChannelBindingFlag();
    }

    public boolean isChannelBinding() {
        return this.gs2Header.getChannelBindingFlag() == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED;
    }

    public String getChannelBindingName() {
        return this.gs2Header.getChannelBindingName();
    }

    public String getAuthzid() {
        return this.gs2Header.getAuthzid();
    }

    public Gs2Header getGs2Header() {
        return this.gs2Header;
    }

    public String getUser() {
        return this.user;
    }

    public String getNonce() {
        return this.nonce;
    }

    public StringBuffer writeToWithoutGs2Header(StringBuffer sb) {
        return StringWritableCsv.writeTo(sb, new ScramAttributeValue(ScramAttributes.USERNAME, ScramStringFormatting.toSaslName(this.user)), new ScramAttributeValue(ScramAttributes.NONCE, this.nonce));
    }

    @Override
    public StringBuffer writeTo(StringBuffer sb) {
        StringWritableCsv.writeTo(sb, this.gs2Header, null);
        return this.writeToWithoutGs2Header(sb);
    }

    public static ClientFirstMessage parseFrom(String clientFirstMessage) throws ScramParseException, IllegalArgumentException {
        String[] userNonceString;
        Preconditions.checkNotEmpty(clientFirstMessage, "clientFirstMessage");
        Gs2Header gs2Header = Gs2Header.parseFrom(clientFirstMessage);
        try {
            userNonceString = StringWritableCsv.parseFrom(clientFirstMessage, 2, 2);
        }
        catch (IllegalArgumentException e) {
            throw new ScramParseException("Illegal series of attributes in client-first-message", e);
        }
        ScramAttributeValue user = ScramAttributeValue.parse(userNonceString[0]);
        if (ScramAttributes.USERNAME.getChar() != user.getChar()) {
            throw new ScramParseException("user must be the 3rd element of the client-first-message");
        }
        ScramAttributeValue nonce = ScramAttributeValue.parse(userNonceString[1]);
        if (ScramAttributes.NONCE.getChar() != nonce.getChar()) {
            throw new ScramParseException("nonce must be the 4th element of the client-first-message");
        }
        return new ClientFirstMessage(gs2Header, user.getValue(), nonce.getValue());
    }

    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}

