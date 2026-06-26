/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.gss;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.checkerframework.checker.nullness.qual.Nullable;

class GSSCallbackHandler
implements CallbackHandler {
    private final String user;
    private final char @Nullable [] password;

    GSSCallbackHandler(String user, char @Nullable [] password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof TextOutputCallback) {
                TextOutputCallback toc = (TextOutputCallback)callback;
                switch (toc.getMessageType()) {
                    case 0: {
                        System.out.println("INFO: " + toc.getMessage());
                        break;
                    }
                    case 2: {
                        System.out.println("ERROR: " + toc.getMessage());
                        break;
                    }
                    case 1: {
                        System.out.println("WARNING: " + toc.getMessage());
                        break;
                    }
                    default: {
                        throw new IOException("Unsupported message type: " + toc.getMessageType());
                    }
                }
                continue;
            }
            if (callback instanceof NameCallback) {
                NameCallback nc = (NameCallback)callback;
                nc.setName(this.user);
                continue;
            }
            if (callback instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback)callback;
                if (this.password == null) {
                    throw new IOException("No cached kerberos ticket found and no password supplied.");
                }
                pc.setPassword(this.password);
                continue;
            }
            throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
        }
    }
}

