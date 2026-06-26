/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500Principal;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.ssl.LibPQFactory;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PKCS12KeyManager
implements X509KeyManager {
    private final CallbackHandler cbh;
    private @Nullable PSQLException error = null;
    private final String keyfile;
    private final KeyStore keyStore;
    boolean keystoreLoaded = false;

    public PKCS12KeyManager(String pkcsFile, CallbackHandler cbh) throws PSQLException {
        try {
            this.keyStore = KeyStore.getInstance("pkcs12");
            this.keyfile = pkcsFile;
            this.cbh = cbh;
        }
        catch (KeyStoreException kse) {
            throw new PSQLException(GT.tr("Unable to find pkcs12 keystore.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)kse);
        }
    }

    public void throwKeyManagerException() throws PSQLException {
        if (this.error != null) {
            throw this.error;
        }
    }

    @Override
    public String @Nullable [] getClientAliases(String keyType, Principal @Nullable [] principals) {
        String[] stringArray;
        String alias = this.chooseClientAlias(new String[]{keyType}, principals, null);
        if (alias == null) {
            stringArray = null;
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = alias;
        }
        return stringArray;
    }

    @Override
    public @Nullable String chooseClientAlias(String[] keyType, Principal @Nullable [] principals, @Nullable Socket socket) {
        if (principals == null || principals.length == 0) {
            return "user";
        }
        X509Certificate[] certchain = this.getCertificateChain("user");
        if (certchain == null) {
            return null;
        }
        X509Certificate cert = certchain[certchain.length - 1];
        X500Principal ourissuer = cert.getIssuerX500Principal();
        String certKeyType = cert.getPublicKey().getAlgorithm();
        boolean keyTypeFound = false;
        boolean found = false;
        if (keyType != null && keyType.length > 0) {
            for (String kt : keyType) {
                if (!kt.equalsIgnoreCase(certKeyType)) continue;
                keyTypeFound = true;
            }
        } else {
            keyTypeFound = true;
        }
        if (keyTypeFound) {
            for (Principal issuer : principals) {
                if (!ourissuer.equals(issuer)) continue;
                found = keyTypeFound;
            }
        }
        return found ? "user" : null;
    }

    @Override
    public String @Nullable [] getServerAliases(String s2, Principal @Nullable [] principals) {
        return new String[0];
    }

    @Override
    public @Nullable String chooseServerAlias(String s2, Principal @Nullable [] principals, @Nullable Socket socket) {
        return null;
    }

    @Override
    public X509Certificate @Nullable [] getCertificateChain(String alias) {
        try {
            this.loadKeyStore();
            Certificate[] certs = this.keyStore.getCertificateChain(alias);
            if (certs == null) {
                return null;
            }
            X509Certificate[] x509Certificates = new X509Certificate[certs.length];
            int i = 0;
            for (Certificate cert : certs) {
                x509Certificates[i++] = (X509Certificate)cert;
            }
            return x509Certificates;
        }
        catch (Exception kse) {
            this.error = new PSQLException(GT.tr("Could not find a java cryptographic algorithm: X.509 CertificateFactory not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)kse);
            return null;
        }
    }

    @Override
    public @Nullable PrivateKey getPrivateKey(String s2) {
        try {
            this.loadKeyStore();
            PasswordCallback pwdcb = new PasswordCallback(GT.tr("Enter SSL password: ", new Object[0]), false);
            this.cbh.handle(new Callback[]{pwdcb});
            KeyStore.PasswordProtection protParam = new KeyStore.PasswordProtection(pwdcb.getPassword());
            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)this.keyStore.getEntry("user", protParam);
            if (pkEntry == null) {
                return null;
            }
            PrivateKey myPrivateKey = pkEntry.getPrivateKey();
            return myPrivateKey;
        }
        catch (Exception ioex) {
            this.error = new PSQLException(GT.tr("Could not read SSL key file {0}.", this.keyfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex);
            return null;
        }
    }

    private synchronized void loadKeyStore() throws Exception {
        if (this.keystoreLoaded) {
            return;
        }
        PasswordCallback pwdcb = new PasswordCallback(GT.tr("Enter SSL password: ", new Object[0]), false);
        try {
            this.cbh.handle(new Callback[]{pwdcb});
        }
        catch (UnsupportedCallbackException ucex) {
            this.error = this.cbh instanceof LibPQFactory.ConsoleCallbackHandler && "Console is not available".equals(ucex.getMessage()) ? new PSQLException(GT.tr("Could not read password for SSL key file, console is not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ucex) : new PSQLException(GT.tr("Could not read password for SSL key file by callbackhandler {0}.", this.cbh.getClass().getName()), PSQLState.CONNECTION_FAILURE, (Throwable)ucex);
        }
        this.keyStore.load(new FileInputStream(new File(this.keyfile)), pwdcb.getPassword());
        this.keystoreLoaded = true;
    }
}

