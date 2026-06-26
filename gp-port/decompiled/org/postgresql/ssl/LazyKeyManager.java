/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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

public class LazyKeyManager
implements X509KeyManager {
    private X509Certificate @Nullable [] cert = null;
    private @Nullable PrivateKey key = null;
    private final @Nullable String certfile;
    private final @Nullable String keyfile;
    private final CallbackHandler cbh;
    private final boolean defaultfile;
    private @Nullable PSQLException error = null;

    public LazyKeyManager(@Nullable String certfile, @Nullable String keyfile, CallbackHandler cbh, boolean defaultfile) {
        this.certfile = certfile;
        this.keyfile = keyfile;
        this.cbh = cbh;
        this.defaultfile = defaultfile;
    }

    public void throwKeyManagerException() throws PSQLException {
        if (this.error != null) {
            throw this.error;
        }
    }

    @Override
    public @Nullable String chooseClientAlias(String[] keyType, Principal @Nullable [] issuers, @Nullable Socket socket) {
        if (this.certfile == null) {
            return null;
        }
        if (issuers == null || issuers.length == 0) {
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
            for (Principal issuer : issuers) {
                if (!ourissuer.equals(issuer)) continue;
                found = keyTypeFound;
            }
        }
        return found ? "user" : null;
    }

    @Override
    public @Nullable String chooseServerAlias(String keyType, Principal @Nullable [] issuers, @Nullable Socket socket) {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public X509Certificate @Nullable [] getCertificateChain(String alias) {
        Collection<? extends Certificate> certs;
        block18: {
            CertificateFactory cf;
            if (this.cert != null) return this.cert;
            if (this.certfile == null) return this.cert;
            try {
                cf = CertificateFactory.getInstance("X.509");
            }
            catch (CertificateException ex) {
                this.error = new PSQLException(GT.tr("Could not find a java cryptographic algorithm: X.509 CertificateFactory not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ex);
                return null;
            }
            FileInputStream certfileStream = null;
            certfileStream = new FileInputStream(this.certfile);
            certs = cf.generateCertificates(certfileStream);
            if (certfileStream == null) break block18;
            try {
                certfileStream.close();
            }
            catch (IOException ioex) {
                if (!this.defaultfile) {
                    this.error = new PSQLException(GT.tr("Could not close SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex);
                }
                break block18;
            }
            catch (FileNotFoundException ioex) {
                if (!this.defaultfile) {
                    this.error = new PSQLException(GT.tr("Could not open SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex);
                }
                X509Certificate[] x509CertificateArray = null;
                if (certfileStream == null) return x509CertificateArray;
                {
                    catch (Throwable throwable) {
                        if (certfileStream == null) throw throwable;
                        try {
                            certfileStream.close();
                            throw throwable;
                        }
                        catch (IOException ioex2) {
                            if (this.defaultfile) throw throwable;
                            this.error = new PSQLException(GT.tr("Could not close SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex2);
                        }
                        throw throwable;
                    }
                }
                try {
                    certfileStream.close();
                    return x509CertificateArray;
                }
                catch (IOException ioex3) {
                    if (this.defaultfile) return x509CertificateArray;
                    this.error = new PSQLException(GT.tr("Could not close SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex3);
                }
                return x509CertificateArray;
                catch (CertificateException gsex) {
                    this.error = new PSQLException(GT.tr("Loading the SSL certificate {0} into a KeyManager failed.", this.certfile), PSQLState.CONNECTION_FAILURE, (Throwable)gsex);
                    X509Certificate[] x509CertificateArray2 = null;
                    if (certfileStream == null) return x509CertificateArray2;
                    try {
                        certfileStream.close();
                        return x509CertificateArray2;
                    }
                    catch (IOException ioex4) {
                        if (this.defaultfile) return x509CertificateArray2;
                        this.error = new PSQLException(GT.tr("Could not close SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex4);
                    }
                    return x509CertificateArray2;
                }
            }
        }
        this.cert = certs.toArray(new X509Certificate[0]);
        return this.cert;
    }

    @Override
    public String @Nullable [] getClientAliases(String keyType, Principal @Nullable [] issuers) {
        String[] stringArray;
        String alias = this.chooseClientAlias(new String[]{keyType}, issuers, null);
        if (alias == null) {
            stringArray = new String[]{};
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = alias;
        }
        return stringArray;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static byte[] readFileFully(String path) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r");){
            byte[] ret = new byte[(int)raf.length()];
            raf.readFully(ret);
            byte[] byArray = ret;
            return byArray;
        }
    }

    @Override
    public @Nullable PrivateKey getPrivateKey(String alias) {
        block15: {
            try {
                byte[] keydata;
                if (this.key != null || this.keyfile == null) break block15;
                X509Certificate[] cert = this.getCertificateChain("user");
                if (cert == null || cert.length == 0) {
                    return null;
                }
                try {
                    keydata = LazyKeyManager.readFileFully(this.keyfile);
                }
                catch (FileNotFoundException ex) {
                    if (!this.defaultfile) {
                        throw ex;
                    }
                    return null;
                }
                KeyFactory kf = KeyFactory.getInstance(cert[0].getPublicKey().getAlgorithm());
                try {
                    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keydata);
                    this.key = kf.generatePrivate(pkcs8KeySpec);
                }
                catch (InvalidKeySpecException ex) {
                    Cipher cipher;
                    EncryptedPrivateKeyInfo ePKInfo = new EncryptedPrivateKeyInfo(keydata);
                    try {
                        cipher = Cipher.getInstance(ePKInfo.getAlgName());
                    }
                    catch (NoSuchPaddingException npex) {
                        throw new NoSuchAlgorithmException(npex.getMessage(), npex);
                    }
                    PasswordCallback pwdcb = new PasswordCallback(GT.tr("Enter SSL password: ", new Object[0]), false);
                    try {
                        this.cbh.handle(new Callback[]{pwdcb});
                    }
                    catch (UnsupportedCallbackException ucex) {
                        this.error = this.cbh instanceof LibPQFactory.ConsoleCallbackHandler && "Console is not available".equals(ucex.getMessage()) ? new PSQLException(GT.tr("Could not read password for SSL key file, console is not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)ucex) : new PSQLException(GT.tr("Could not read password for SSL key file by callbackhandler {0}.", this.cbh.getClass().getName()), PSQLState.CONNECTION_FAILURE, (Throwable)ucex);
                        return null;
                    }
                    try {
                        PBEKeySpec pbeKeySpec = new PBEKeySpec(pwdcb.getPassword());
                        pwdcb.clearPassword();
                        SecretKeyFactory skFac = SecretKeyFactory.getInstance(ePKInfo.getAlgName());
                        SecretKey pbeKey = skFac.generateSecret(pbeKeySpec);
                        AlgorithmParameters algParams = ePKInfo.getAlgParameters();
                        cipher.init(2, (Key)pbeKey, algParams);
                        PKCS8EncodedKeySpec pkcs8KeySpec = ePKInfo.getKeySpec(cipher);
                        this.key = kf.generatePrivate(pkcs8KeySpec);
                    }
                    catch (GeneralSecurityException ikex) {
                        this.error = new PSQLException(GT.tr("Could not decrypt SSL key file {0}.", this.keyfile), PSQLState.CONNECTION_FAILURE, (Throwable)ikex);
                        return null;
                    }
                }
            }
            catch (IOException ioex) {
                this.error = new PSQLException(GT.tr("Could not read SSL key file {0}.", this.keyfile), PSQLState.CONNECTION_FAILURE, (Throwable)ioex);
            }
            catch (NoSuchAlgorithmException ex) {
                this.error = new PSQLException(GT.tr("Could not find a java cryptographic algorithm: {0}.", ex.getMessage()), PSQLState.CONNECTION_FAILURE, (Throwable)ex);
                return null;
            }
        }
        return this.key;
    }

    @Override
    public String @Nullable [] getServerAliases(String keyType, Principal @Nullable [] issuers) {
        return new String[0];
    }
}

