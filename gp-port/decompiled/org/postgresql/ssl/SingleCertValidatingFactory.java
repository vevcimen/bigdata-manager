/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.postgresql.ssl.WrappedFactory;
import org.postgresql.util.GT;

public class SingleCertValidatingFactory
extends WrappedFactory {
    private static final String FILE_PREFIX = "file:";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String ENV_PREFIX = "env:";
    private static final String SYS_PROP_PREFIX = "sys:";

    public SingleCertValidatingFactory(String sslFactoryArg) throws GeneralSecurityException {
        if (sslFactoryArg == null || sslFactoryArg.equals("")) {
            throw new GeneralSecurityException(GT.tr("The sslfactoryarg property may not be empty.", new Object[0]));
        }
        InputStream in = null;
        try {
            String name;
            String path;
            if (sslFactoryArg.startsWith(FILE_PREFIX)) {
                path = sslFactoryArg.substring(FILE_PREFIX.length());
                in = new BufferedInputStream(new FileInputStream(path));
            } else if (sslFactoryArg.startsWith(CLASSPATH_PREFIX)) {
                InputStream inputStream;
                path = sslFactoryArg.substring(CLASSPATH_PREFIX.length());
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader != null) {
                    inputStream = classLoader.getResourceAsStream(path);
                    if (inputStream == null) {
                        throw new IllegalArgumentException(GT.tr("Unable to find resource {0} via Thread contextClassLoader {1}", path, classLoader));
                    }
                } else {
                    inputStream = this.getClass().getResourceAsStream(path);
                    if (inputStream == null) {
                        throw new IllegalArgumentException(GT.tr("Unable to find resource {0} via class {1} ClassLoader {2}", path, this.getClass(), this.getClass().getClassLoader()));
                    }
                }
                in = new BufferedInputStream(inputStream);
            } else if (sslFactoryArg.startsWith(ENV_PREFIX)) {
                name = sslFactoryArg.substring(ENV_PREFIX.length());
                String cert = System.getenv(name);
                if (cert == null || "".equals(cert)) {
                    throw new GeneralSecurityException(GT.tr("The environment variable containing the server's SSL certificate must not be empty.", new Object[0]));
                }
                in = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
            } else if (sslFactoryArg.startsWith(SYS_PROP_PREFIX)) {
                name = sslFactoryArg.substring(SYS_PROP_PREFIX.length());
                String cert = System.getProperty(name);
                if (cert == null || "".equals(cert)) {
                    throw new GeneralSecurityException(GT.tr("The system property containing the server's SSL certificate must not be empty.", new Object[0]));
                }
                in = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
            } else if (sslFactoryArg.startsWith("-----BEGIN CERTIFICATE-----")) {
                in = new ByteArrayInputStream(sslFactoryArg.getBytes(StandardCharsets.UTF_8));
            } else {
                throw new GeneralSecurityException(GT.tr("The sslfactoryarg property must start with the prefix file:, classpath:, env:, sys:, or -----BEGIN CERTIFICATE-----.", new Object[0]));
            }
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new SingleCertTrustManager(in)}, null);
            this.factory = ctx.getSocketFactory();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            if (e instanceof GeneralSecurityException) {
                throw (GeneralSecurityException)e;
            }
            throw new GeneralSecurityException(GT.tr("An error occurred reading the certificate", new Object[0]), e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception exception) {}
            }
        }
    }

    public static class SingleCertTrustManager
    implements X509TrustManager {
        X509Certificate cert;
        X509TrustManager trustManager;

        public SingleCertTrustManager(InputStream in) throws IOException, GeneralSecurityException {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            this.cert = (X509Certificate)cf.generateCertificate(in);
            ks.setCertificateEntry(UUID.randomUUID().toString(), this.cert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (!(tm instanceof X509TrustManager)) continue;
                this.trustManager = (X509TrustManager)tm;
                break;
            }
            if (this.trustManager == null) {
                throw new GeneralSecurityException(GT.tr("No X509TrustManager found", new Object[0]));
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.trustManager.checkServerTrusted(chain, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{this.cert};
        }
    }
}

