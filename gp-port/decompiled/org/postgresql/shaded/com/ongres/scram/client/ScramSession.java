/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.client;

import org.postgresql.shaded.com.ongres.scram.common.ScramFunctions;
import org.postgresql.shaded.com.ongres.scram.common.ScramMechanism;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramServerErrorException;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.postgresql.shaded.com.ongres.scram.common.message.ClientFinalMessage;
import org.postgresql.shaded.com.ongres.scram.common.message.ClientFirstMessage;
import org.postgresql.shaded.com.ongres.scram.common.message.ServerFinalMessage;
import org.postgresql.shaded.com.ongres.scram.common.message.ServerFirstMessage;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class ScramSession {
    private final ScramMechanism scramMechanism;
    private final StringPreparation stringPreparation;
    private final String user;
    private final String nonce;
    private ClientFirstMessage clientFirstMessage;
    private String serverFirstMessageString;

    public ScramSession(ScramMechanism scramMechanism, StringPreparation stringPreparation, String user, String nonce) {
        this.scramMechanism = Preconditions.checkNotNull(scramMechanism, "scramMechanism");
        this.stringPreparation = Preconditions.checkNotNull(stringPreparation, "stringPreparation");
        this.user = Preconditions.checkNotEmpty(user, "user");
        this.nonce = Preconditions.checkNotEmpty(nonce, "nonce");
    }

    private String setAndReturnClientFirstMessage(ClientFirstMessage clientFirstMessage) {
        this.clientFirstMessage = clientFirstMessage;
        return clientFirstMessage.toString();
    }

    public String clientFirstMessage(Gs2CbindFlag gs2CbindFlag, String cbindName, String authzid) {
        return this.setAndReturnClientFirstMessage(new ClientFirstMessage(gs2CbindFlag, authzid, cbindName, this.user, this.nonce));
    }

    public String clientFirstMessage() {
        return this.setAndReturnClientFirstMessage(new ClientFirstMessage(this.user, this.nonce));
    }

    public ServerFirstProcessor receiveServerFirstMessage(String serverFirstMessage) throws ScramParseException, IllegalArgumentException {
        return new ServerFirstProcessor(Preconditions.checkNotEmpty(serverFirstMessage, "serverFirstMessage"));
    }

    public class ClientFinalProcessor {
        private final String nonce;
        private final byte[] clientKey;
        private final byte[] storedKey;
        private final byte[] serverKey;
        private String authMessage;

        private ClientFinalProcessor(String nonce, byte[] clientKey, byte[] storedKey, byte[] serverKey) {
            assert (null != clientKey) : "clientKey";
            assert (null != storedKey) : "storedKey";
            assert (null != serverKey) : "serverKey";
            this.nonce = nonce;
            this.clientKey = clientKey;
            this.storedKey = storedKey;
            this.serverKey = serverKey;
        }

        private ClientFinalProcessor(String nonce, byte[] clientKey, byte[] serverKey) {
            this(nonce, clientKey, ScramFunctions.storedKey(this$0.scramMechanism, clientKey), serverKey);
        }

        private ClientFinalProcessor(String nonce, byte[] saltedPassword) {
            this(nonce, ScramFunctions.clientKey(this$0.scramMechanism, saltedPassword), ScramFunctions.serverKey(this$0.scramMechanism, saltedPassword));
        }

        private ClientFinalProcessor(String nonce, String password, String salt, int iteration) {
            this(nonce, ScramFunctions.saltedPassword(this$0.scramMechanism, this$0.stringPreparation, password, Base64.decode(salt), iteration));
        }

        private synchronized void generateAndCacheAuthMessage(byte[] cbindData) {
            if (null != this.authMessage) {
                return;
            }
            this.authMessage = ScramSession.this.clientFirstMessage.writeToWithoutGs2Header(new StringBuffer()).append(",").append(ScramSession.this.serverFirstMessageString).append(",").append(ClientFinalMessage.writeToWithoutProof(ScramSession.this.clientFirstMessage.getGs2Header(), cbindData, this.nonce)).toString();
        }

        private String clientFinalMessage(byte[] cbindData) throws IllegalArgumentException {
            if (null == this.authMessage) {
                this.generateAndCacheAuthMessage(cbindData);
            }
            ClientFinalMessage clientFinalMessage = new ClientFinalMessage(ScramSession.this.clientFirstMessage.getGs2Header(), cbindData, this.nonce, ScramFunctions.clientProof(this.clientKey, ScramFunctions.clientSignature(ScramSession.this.scramMechanism, this.storedKey, this.authMessage)));
            return clientFinalMessage.toString();
        }

        public String clientFinalMessage() {
            return this.clientFinalMessage(null);
        }

        public void receiveServerFinalMessage(String serverFinalMessage) throws ScramParseException, ScramServerErrorException, ScramInvalidServerSignatureException, IllegalArgumentException {
            Preconditions.checkNotEmpty(serverFinalMessage, "serverFinalMessage");
            ServerFinalMessage message = ServerFinalMessage.parseFrom(serverFinalMessage);
            if (message.isError()) {
                throw new ScramServerErrorException(message.getError());
            }
            if (!ScramFunctions.verifyServerSignature(ScramSession.this.scramMechanism, this.serverKey, this.authMessage, message.getVerifier())) {
                throw new ScramInvalidServerSignatureException("Invalid server SCRAM signature");
            }
        }
    }

    public class ServerFirstProcessor {
        private final ServerFirstMessage serverFirstMessage;

        private ServerFirstProcessor(String receivedServerFirstMessage) throws ScramParseException {
            ScramSession.this.serverFirstMessageString = receivedServerFirstMessage;
            this.serverFirstMessage = ServerFirstMessage.parseFrom(receivedServerFirstMessage, ScramSession.this.nonce);
        }

        public String getSalt() {
            return this.serverFirstMessage.getSalt();
        }

        public int getIteration() {
            return this.serverFirstMessage.getIteration();
        }

        public ClientFinalProcessor clientFinalProcessor(String password) throws IllegalArgumentException {
            return new ClientFinalProcessor(this.serverFirstMessage.getNonce(), Preconditions.checkNotEmpty(password, "password"), this.getSalt(), this.getIteration());
        }

        public ClientFinalProcessor clientFinalProcessor(byte[] clientKey, byte[] storedKey) throws IllegalArgumentException {
            return new ClientFinalProcessor(this.serverFirstMessage.getNonce(), Preconditions.checkNotNull(clientKey, "clientKey"), Preconditions.checkNotNull(storedKey, "storedKey"));
        }
    }
}

