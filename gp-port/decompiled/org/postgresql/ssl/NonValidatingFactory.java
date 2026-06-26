/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.postgresql.ssl.WrappedFactory;

public class NonValidatingFactory
extends WrappedFactory {
    public NonValidatingFactory(String arg) throws GeneralSecurityException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new NonValidatingTM()}, null);
        this.factory = ctx.getSocketFactory();
    }

    public static class NonValidatingTM
    implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }
}

