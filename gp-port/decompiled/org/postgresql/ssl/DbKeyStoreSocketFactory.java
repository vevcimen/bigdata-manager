/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.postgresql.ssl.WrappedFactory;

public abstract class DbKeyStoreSocketFactory
extends WrappedFactory {
    public DbKeyStoreSocketFactory() throws DbKeyStoreSocketException {
        char[] password;
        KeyStore keys;
        try {
            keys = KeyStore.getInstance("JKS");
            password = this.getKeyStorePassword();
            keys.load(this.getKeyStoreStream(), password);
        }
        catch (GeneralSecurityException gse) {
            throw new DbKeyStoreSocketException("Failed to load keystore: " + gse.getMessage());
        }
        catch (FileNotFoundException fnfe) {
            throw new DbKeyStoreSocketException("Failed to find keystore file." + fnfe.getMessage());
        }
        catch (IOException ioe) {
            throw new DbKeyStoreSocketException("Failed to read keystore file: " + ioe.getMessage());
        }
        try {
            KeyManagerFactory keyfact = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyfact.init(keys, password);
            TrustManagerFactory trustfact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustfact.init(keys);
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(keyfact.getKeyManagers(), trustfact.getTrustManagers(), null);
            this.factory = ctx.getSocketFactory();
        }
        catch (GeneralSecurityException gse) {
            throw new DbKeyStoreSocketException("Failed to set up database socket factory: " + gse.getMessage());
        }
    }

    public abstract char[] getKeyStorePassword();

    public abstract InputStream getKeyStoreStream();

    public static class DbKeyStoreSocketException
    extends Exception {
        public DbKeyStoreSocketException(String message) {
            super(message);
        }
    }
}

