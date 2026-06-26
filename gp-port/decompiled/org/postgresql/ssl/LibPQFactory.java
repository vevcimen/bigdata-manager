/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Locale;
import java.util.Properties;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.SslMode;
import org.postgresql.ssl.LazyKeyManager;
import org.postgresql.ssl.NonValidatingFactory;
import org.postgresql.ssl.PKCS12KeyManager;
import org.postgresql.ssl.WrappedFactory;
import org.postgresql.util.GT;
import org.postgresql.util.ObjectFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class LibPQFactory
extends WrappedFactory {
    @Nullable KeyManager km;
    boolean defaultfile;

    private CallbackHandler getCallbackHandler(@UnderInitialization(value=WrappedFactory.class) LibPQFactory this, Properties info) throws PSQLException {
        CallbackHandler cbh;
        String sslpasswordcallback = PGProperty.SSL_PASSWORD_CALLBACK.get(info);
        if (sslpasswordcallback != null) {
            try {
                cbh = ObjectFactory.instantiate(CallbackHandler.class, sslpasswordcallback, info, false, null);
            }
            catch (Exception e) {
                throw new PSQLException(GT.tr("The password callback class provided {0} could not be instantiated.", sslpasswordcallback), PSQLState.CONNECTION_FAILURE, (Throwable)e);
            }
        } else {
            cbh = new ConsoleCallbackHandler(PGProperty.SSL_PASSWORD.get(info));
        }
        return cbh;
    }

    private void initPk8(@UnderInitialization(value=WrappedFactory.class) LibPQFactory this, String sslkeyfile, String defaultdir, Properties info) throws PSQLException {
        String sslcertfile = PGProperty.SSL_CERT.get(info);
        if (sslcertfile == null) {
            this.defaultfile = true;
            sslcertfile = defaultdir + "postgresql.crt";
        }
        this.km = new LazyKeyManager("".equals(sslcertfile) ? null : sslcertfile, "".equals(sslkeyfile) ? null : sslkeyfile, this.getCallbackHandler(info), this.defaultfile);
    }

    private void initP12(@UnderInitialization(value=WrappedFactory.class) LibPQFactory this, String sslkeyfile, Properties info) throws PSQLException {
        this.km = new PKCS12KeyManager(sslkeyfile, this.getCallbackHandler(info));
    }

    public LibPQFactory(Properties info) throws PSQLException {
        try {
            TrustManager[] tm;
            SSLContext ctx = SSLContext.getInstance("TLS");
            String pathsep = System.getProperty("file.separator");
            String defaultdir = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows") ? System.getenv("APPDATA") + pathsep + "postgresql" + pathsep : System.getProperty("user.home") + pathsep + ".postgresql" + pathsep;
            String sslkeyfile = PGProperty.SSL_KEY.get(info);
            if (sslkeyfile == null) {
                this.defaultfile = true;
                sslkeyfile = defaultdir + "postgresql.pk8";
            }
            if (sslkeyfile.endsWith(".p12") || sslkeyfile.endsWith(".pfx")) {
                this.initP12(sslkeyfile, info);
            } else {
                this.initPk8(sslkeyfile, defaultdir, info);
            }
            SslMode sslMode = SslMode.of(info);
            if (!sslMode.verifyCertificate()) {
                tm = new TrustManager[]{new NonValidatingFactory.NonValidatingTM()};
            } else {
                FileInputStream fis;
                KeyStore ks;
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
                try {
                    ks = KeyStore.getInstance("jks");
                }
                catch (KeyStoreException e) {
                    throw new NoSuchAlgorithmException("jks KeyStore not available");
                }
                String sslrootcertfile = PGProperty.SSL_ROOT_CERT.get(info);
                if (sslrootcertfile == null) {
                    sslrootcertfile = defaultdir + "root.crt";
                }
                try {
                    fis = new FileInputStream(sslrootcertfile);
                }
                catch (FileNotFoundException ex) {
                    throw new PSQLException(GT.tr("Could not open SSL root certificate file {0}.", sslrootcertfile), PSQLState.CONNECTION_FAILURE, (Throwable)ex);
                }
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate[] certs = cf.generateCertificates(fis).toArray(new Certificate[0]);
                    ks.load(null, null);
                    for (int i = 0; i < certs.length; ++i) {
                        ks.setCertificateEntry("cert" + i, certs[i]);
                    }
                    tmf.init(ks);
                }
                catch (IOException ioex) {
                    throw new PSQLException(GT.tr("Could not read SSL root certificate file {0}.", sslrootcertfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex);
                }
                catch (GeneralSecurityException gsex) {
                    throw new PSQLException(GT.tr("Loading the SSL root certificate {0} into a TrustManager failed.", sslrootcertfile), PSQLState.CONNECTION_FAILURE, (Throwable)gsex);
                }
                finally {
                    try {
                        fis.close();
                    }
                    catch (IOException iOException) {}
                }
                tm = tmf.getTrustManagers();
            }
            try {
                KeyManager[] keyManagerArray;
                KeyManager km3 = this.km;
                if (km3 == null) {
                    keyManagerArray = null;
                } else {
                    KeyManager[] keyManagerArray2 = new KeyManager[1];
                    keyManagerArray = keyManagerArray2;
                    keyManagerArray2[0] = km3;
                }
                ctx.init(keyManagerArray, tm, null);
            }
            catch (KeyManagementException ex) {
                throw new PSQLException(GT.tr("Could not initialize SSL context.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ex);
            }
            this.factory = ctx.getSocketFactory();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new PSQLException(GT.tr("Could not find a java cryptographic algorithm: {0}.", ex.getMessage()), PSQLState.CONNECTION_FAILURE, (Throwable)ex);
        }
    }

    public void throwKeyManagerException() throws PSQLException {
        if (this.km != null) {
            if (this.km instanceof LazyKeyManager) {
                ((LazyKeyManager)this.km).throwKeyManagerException();
            }
            if (this.km instanceof PKCS12KeyManager) {
                ((PKCS12KeyManager)this.km).throwKeyManagerException();
            }
        }
    }

    public static class ConsoleCallbackHandler
    implements CallbackHandler {
        private char @Nullable [] password = null;

        ConsoleCallbackHandler(@Nullable String password) {
            if (password != null) {
                this.password = password.toCharArray();
            }
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            Console cons = System.console();
            char[] password = this.password;
            if (cons == null && password == null) {
                throw new UnsupportedCallbackException(callbacks[0], "Console is not available");
            }
            for (Callback callback : callbacks) {
                if (!(callback instanceof PasswordCallback)) {
                    throw new UnsupportedCallbackException(callback);
                }
                PasswordCallback pwdCallback = (PasswordCallback)callback;
                if (password != null) {
                    pwdCallback.setPassword(password);
                    continue;
                }
                pwdCallback.setPassword(Nullness.castNonNull(cons, "System.console()").readPassword("%s", pwdCallback.getPrompt()));
            }
        }
    }
}

