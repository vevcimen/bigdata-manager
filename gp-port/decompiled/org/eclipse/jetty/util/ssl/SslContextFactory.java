/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.CertificateUtils;
import org.eclipse.jetty.util.security.CertificateValidator;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.AliasedX509ExtendedKeyManager;
import org.eclipse.jetty.util.ssl.SniX509ExtendedKeyManager;
import org.eclipse.jetty.util.ssl.SslSelectionDump;
import org.eclipse.jetty.util.ssl.X509;

@ManagedObject
public class SslContextFactory
extends AbstractLifeCycle
implements Dumpable {
    public static final TrustManager[] TRUST_ALL_CERTS = new X509TrustManager[]{new X509ExtendedTrustManagerWrapper(null)};
    public static final String DEFAULT_KEYMANAGERFACTORY_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();
    public static final String DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM = TrustManagerFactory.getDefaultAlgorithm();
    public static final String KEYPASSWORD_PROPERTY = "org.eclipse.jetty.ssl.keypassword";
    public static final String PASSWORD_PROPERTY = "org.eclipse.jetty.ssl.password";
    private static final Logger LOG = Log.getLogger(SslContextFactory.class);
    private static final Logger LOG_CONFIG = LOG.getLogger("config");
    private static final String[] DEFAULT_EXCLUDED_PROTOCOLS = new String[]{"SSL", "SSLv2", "SSLv2Hello", "SSLv3"};
    private static final String[] DEFAULT_EXCLUDED_CIPHER_SUITES = new String[]{"^.*_(MD5|SHA|SHA1)$", "^TLS_RSA_.*$", "^SSL_.*$", "^.*_NULL_.*$", "^.*_anon_.*$"};
    private final Set<String> _excludeProtocols = new LinkedHashSet<String>();
    private final Set<String> _includeProtocols = new LinkedHashSet<String>();
    private final Set<String> _excludeCipherSuites = new LinkedHashSet<String>();
    private final Set<String> _includeCipherSuites = new LinkedHashSet<String>();
    private final Map<String, X509> _aliasX509 = new HashMap<String, X509>();
    private final Map<String, X509> _certHosts = new HashMap<String, X509>();
    private final Map<String, X509> _certWilds = new HashMap<String, X509>();
    private String[] _selectedProtocols;
    private boolean _useCipherSuitesOrder = true;
    private Comparator<String> _cipherComparator;
    private String[] _selectedCipherSuites;
    private Resource _keyStoreResource;
    private String _keyStoreProvider;
    private String _keyStoreType = "JKS";
    private String _certAlias;
    private Resource _trustStoreResource;
    private String _trustStoreProvider;
    private String _trustStoreType;
    private boolean _needClientAuth = false;
    private boolean _wantClientAuth = false;
    private Password _keyStorePassword;
    private Password _keyManagerPassword;
    private Password _trustStorePassword;
    private String _sslProvider;
    private String _sslProtocol = "TLS";
    private String _secureRandomAlgorithm;
    private String _keyManagerFactoryAlgorithm = DEFAULT_KEYMANAGERFACTORY_ALGORITHM;
    private String _trustManagerFactoryAlgorithm = DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM;
    private boolean _validateCerts;
    private boolean _validatePeerCerts;
    private int _maxCertPathLength = -1;
    private String _crlPath;
    private boolean _enableCRLDP = false;
    private boolean _enableOCSP = false;
    private String _ocspResponderURL;
    private KeyStore _setKeyStore;
    private KeyStore _setTrustStore;
    private boolean _sessionCachingEnabled = true;
    private int _sslSessionCacheSize = -1;
    private int _sslSessionTimeout = -1;
    private SSLContext _setContext;
    private String _endpointIdentificationAlgorithm = "HTTPS";
    private boolean _trustAll;
    private boolean _renegotiationAllowed = true;
    private int _renegotiationLimit = 5;
    private Factory _factory;
    private PKIXCertPathChecker _pkixCertPathChecker;
    private HostnameVerifier _hostnameVerifier;

    @Deprecated
    public SslContextFactory() {
        this(false);
    }

    @Deprecated
    public SslContextFactory(boolean trustAll) {
        this(trustAll, null);
    }

    @Deprecated
    public SslContextFactory(String keyStorePath) {
        this(false, keyStorePath);
    }

    private SslContextFactory(boolean trustAll, String keyStorePath) {
        this.setTrustAll(trustAll);
        this.setExcludeProtocols(DEFAULT_EXCLUDED_PROTOCOLS);
        this.setExcludeCipherSuites(DEFAULT_EXCLUDED_CIPHER_SUITES);
        if (keyStorePath != null) {
            this.setKeyStorePath(keyStorePath);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStart() throws Exception {
        super.doStart();
        SslContextFactory sslContextFactory = this;
        synchronized (sslContextFactory) {
            this.load();
        }
        this.checkConfiguration();
    }

    protected void checkConfiguration() {
        SSLEngine engine = this._factory._context.createSSLEngine();
        this.customize(engine);
        SSLParameters supported = engine.getSSLParameters();
        this.checkProtocols(supported);
        this.checkCiphers(supported);
    }

    protected void checkTrustAll() {
        if (this.isTrustAll()) {
            LOG_CONFIG.warn("Trusting all certificates configured for {}", this);
        }
    }

    protected void checkEndPointIdentificationAlgorithm() {
        if (this.getEndpointIdentificationAlgorithm() == null) {
            LOG_CONFIG.warn("No Client EndPointIdentificationAlgorithm configured for {}", this);
        }
    }

    protected void checkProtocols(SSLParameters supported) {
        for (String protocol : supported.getProtocols()) {
            for (String excluded : DEFAULT_EXCLUDED_PROTOCOLS) {
                if (!excluded.equals(protocol)) continue;
                LOG_CONFIG.warn("Protocol {} not excluded for {}", protocol, this);
            }
        }
    }

    protected void checkCiphers(SSLParameters supported) {
        for (String suite : supported.getCipherSuites()) {
            for (String excludedSuiteRegex : DEFAULT_EXCLUDED_CIPHER_SUITES) {
                if (!suite.matches(excludedSuiteRegex)) continue;
                LOG_CONFIG.warn("Weak cipher suite {} enabled for {}", suite, this);
            }
        }
    }

    private void load() throws Exception {
        SSLSessionContext serverContext;
        SSLContext context = this._setContext;
        KeyStore keyStore = this._setKeyStore;
        KeyStore trustStore = this._setTrustStore;
        if (context == null) {
            if (keyStore == null && this._keyStoreResource == null && trustStore == null && this._trustStoreResource == null) {
                TrustManager[] trustManagers = null;
                if (this.isTrustAll()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No keystore or trust store configured.  ACCEPTING UNTRUSTED CERTIFICATES!!!!!", new Object[0]);
                    }
                    trustManagers = TRUST_ALL_CERTS;
                }
                context = this.getSSLContextInstance();
                context.init(null, trustManagers, this.getSecureRandomInstance());
            } else {
                if (keyStore == null) {
                    keyStore = this.loadKeyStore(this._keyStoreResource);
                }
                if (trustStore == null) {
                    trustStore = this.loadTrustStore(this._trustStoreResource);
                }
                Collection<? extends CRL> crls = this.loadCRL(this.getCrlPath());
                if (keyStore != null) {
                    for (String alias : Collections.list(keyStore.aliases())) {
                        Certificate certificate = keyStore.getCertificate(alias);
                        if (certificate == null || !"X.509".equals(certificate.getType())) continue;
                        X509Certificate x509C = (X509Certificate)certificate;
                        if (X509.isCertSign(x509C)) {
                            if (!LOG.isDebugEnabled()) continue;
                            LOG.debug("Skipping " + x509C, new Object[0]);
                            continue;
                        }
                        X509 x509 = new X509(alias, x509C);
                        this._aliasX509.put(alias, x509);
                        if (this.isValidateCerts()) {
                            CertificateValidator validator = new CertificateValidator(trustStore, crls);
                            validator.setMaxCertPathLength(this.getMaxCertPathLength());
                            validator.setEnableCRLDP(this.isEnableCRLDP());
                            validator.setEnableOCSP(this.isEnableOCSP());
                            validator.setOcspResponderURL(this.getOcspResponderURL());
                            validator.validate(keyStore, x509C);
                        }
                        LOG.info("x509={} for {}", x509, this);
                        for (String h2 : x509.getHosts()) {
                            this._certHosts.put(h2, x509);
                        }
                        for (String w : x509.getWilds()) {
                            this._certWilds.put(w, x509);
                        }
                    }
                }
                KeyManager[] keyManagers = this.getKeyManagers(keyStore);
                TrustManager[] trustManagers = this.getTrustManagers(trustStore, crls);
                context = this.getSSLContextInstance();
                context.init(keyManagers, trustManagers, this.getSecureRandomInstance());
            }
        }
        if ((serverContext = context.getServerSessionContext()) != null) {
            if (this.getSslSessionCacheSize() > -1) {
                serverContext.setSessionCacheSize(this.getSslSessionCacheSize());
            }
            if (this.getSslSessionTimeout() > -1) {
                serverContext.setSessionTimeout(this.getSslSessionTimeout());
            }
        }
        SSLParameters enabled = context.getDefaultSSLParameters();
        SSLParameters supported = context.getSupportedSSLParameters();
        this.selectCipherSuites(enabled.getCipherSuites(), supported.getCipherSuites());
        this.selectProtocols(enabled.getProtocols(), supported.getProtocols());
        this._factory = new Factory(keyStore, trustStore, context);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Selected Protocols {} of {}", Arrays.asList(this._selectedProtocols), Arrays.asList(supported.getProtocols()));
            LOG.debug("Selected Ciphers   {} of {}", Arrays.asList(this._selectedCipherSuites), Arrays.asList(supported.getCipherSuites()));
        }
    }

    @Override
    public String dump() {
        return Dumpable.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        try {
            SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
            Dumpable.dumpObjects(out, indent, this, "trustAll=" + this._trustAll, new SslSelectionDump("Protocol", sslEngine.getSupportedProtocols(), sslEngine.getEnabledProtocols(), this.getExcludeProtocols(), this.getIncludeProtocols()), new SslSelectionDump("Cipher Suite", sslEngine.getSupportedCipherSuites(), sslEngine.getEnabledCipherSuites(), this.getExcludeCipherSuites(), this.getIncludeCipherSuites()));
        }
        catch (NoSuchAlgorithmException x) {
            LOG.ignore(x);
        }
    }

    List<SslSelectionDump> selectionDump() throws NoSuchAlgorithmException {
        SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
        ArrayList<SslSelectionDump> selections = new ArrayList<SslSelectionDump>();
        selections.add(new SslSelectionDump("Protocol", sslEngine.getSupportedProtocols(), sslEngine.getEnabledProtocols(), this.getExcludeProtocols(), this.getIncludeProtocols()));
        selections.add(new SslSelectionDump("Cipher Suite", sslEngine.getSupportedCipherSuites(), sslEngine.getEnabledCipherSuites(), this.getExcludeCipherSuites(), this.getIncludeCipherSuites()));
        return selections;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        SslContextFactory sslContextFactory = this;
        synchronized (sslContextFactory) {
            this.unload();
        }
        super.doStop();
    }

    private void unload() {
        this._factory = null;
        this._selectedProtocols = null;
        this._selectedCipherSuites = null;
        this._aliasX509.clear();
        this._certHosts.clear();
        this._certWilds.clear();
    }

    @ManagedAttribute(value="The selected TLS protocol versions", readonly=true)
    public String[] getSelectedProtocols() {
        return Arrays.copyOf(this._selectedProtocols, this._selectedProtocols.length);
    }

    @ManagedAttribute(value="The selected cipher suites", readonly=true)
    public String[] getSelectedCipherSuites() {
        return Arrays.copyOf(this._selectedCipherSuites, this._selectedCipherSuites.length);
    }

    public Comparator<String> getCipherComparator() {
        return this._cipherComparator;
    }

    public void setCipherComparator(Comparator<String> cipherComparator) {
        if (cipherComparator != null) {
            this.setUseCipherSuitesOrder(true);
        }
        this._cipherComparator = cipherComparator;
    }

    public Set<String> getAliases() {
        return Collections.unmodifiableSet(this._aliasX509.keySet());
    }

    public X509 getX509(String alias) {
        return this._aliasX509.get(alias);
    }

    @ManagedAttribute(value="The excluded TLS protocols")
    public String[] getExcludeProtocols() {
        return this._excludeProtocols.toArray(new String[0]);
    }

    public void setExcludeProtocols(String ... protocols) {
        this._excludeProtocols.clear();
        this._excludeProtocols.addAll(Arrays.asList(protocols));
    }

    public void addExcludeProtocols(String ... protocol) {
        this._excludeProtocols.addAll(Arrays.asList(protocol));
    }

    @ManagedAttribute(value="The included TLS protocols")
    public String[] getIncludeProtocols() {
        return this._includeProtocols.toArray(new String[0]);
    }

    public void setIncludeProtocols(String ... protocols) {
        this._includeProtocols.clear();
        this._includeProtocols.addAll(Arrays.asList(protocols));
    }

    @ManagedAttribute(value="The excluded cipher suites")
    public String[] getExcludeCipherSuites() {
        return this._excludeCipherSuites.toArray(new String[0]);
    }

    public void setExcludeCipherSuites(String ... cipherSuites) {
        this._excludeCipherSuites.clear();
        this._excludeCipherSuites.addAll(Arrays.asList(cipherSuites));
    }

    public void addExcludeCipherSuites(String ... cipher) {
        this._excludeCipherSuites.addAll(Arrays.asList(cipher));
    }

    @ManagedAttribute(value="The included cipher suites")
    public String[] getIncludeCipherSuites() {
        return this._includeCipherSuites.toArray(new String[0]);
    }

    public void setIncludeCipherSuites(String ... cipherSuites) {
        this._includeCipherSuites.clear();
        this._includeCipherSuites.addAll(Arrays.asList(cipherSuites));
    }

    @ManagedAttribute(value="Whether to respect the cipher suites order")
    public boolean isUseCipherSuitesOrder() {
        return this._useCipherSuitesOrder;
    }

    public void setUseCipherSuitesOrder(boolean useCipherSuitesOrder) {
        this._useCipherSuitesOrder = useCipherSuitesOrder;
    }

    @ManagedAttribute(value="The keyStore path")
    public String getKeyStorePath() {
        return Objects.toString(this._keyStoreResource, null);
    }

    public void setKeyStorePath(String keyStorePath) {
        try {
            this._keyStoreResource = Resource.newResource(keyStorePath);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @ManagedAttribute(value="The keyStore provider name")
    public String getKeyStoreProvider() {
        return this._keyStoreProvider;
    }

    public void setKeyStoreProvider(String keyStoreProvider) {
        this._keyStoreProvider = keyStoreProvider;
    }

    @ManagedAttribute(value="The keyStore type")
    public String getKeyStoreType() {
        return this._keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this._keyStoreType = keyStoreType;
    }

    @ManagedAttribute(value="The certificate alias")
    public String getCertAlias() {
        return this._certAlias;
    }

    public void setCertAlias(String certAlias) {
        this._certAlias = certAlias;
    }

    @ManagedAttribute(value="The trustStore path")
    public String getTrustStorePath() {
        return Objects.toString(this._trustStoreResource, null);
    }

    public void setTrustStorePath(String trustStorePath) {
        try {
            this._trustStoreResource = Resource.newResource(trustStorePath);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @ManagedAttribute(value="The trustStore provider name")
    public String getTrustStoreProvider() {
        return this._trustStoreProvider;
    }

    public void setTrustStoreProvider(String trustStoreProvider) {
        this._trustStoreProvider = trustStoreProvider;
    }

    @ManagedAttribute(value="The trustStore type")
    public String getTrustStoreType() {
        return this._trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this._trustStoreType = trustStoreType;
    }

    @ManagedAttribute(value="Whether client authentication is needed")
    @Deprecated
    public boolean getNeedClientAuth() {
        return this._needClientAuth;
    }

    @Deprecated
    public void setNeedClientAuth(boolean needClientAuth) {
        this._needClientAuth = needClientAuth;
    }

    @ManagedAttribute(value="Whether client authentication is wanted")
    @Deprecated
    public boolean getWantClientAuth() {
        return this._wantClientAuth;
    }

    @Deprecated
    public void setWantClientAuth(boolean wantClientAuth) {
        this._wantClientAuth = wantClientAuth;
    }

    @ManagedAttribute(value="Whether certificates are validated")
    public boolean isValidateCerts() {
        return this._validateCerts;
    }

    public void setValidateCerts(boolean validateCerts) {
        this._validateCerts = validateCerts;
    }

    @ManagedAttribute(value="Whether peer certificates are validated")
    public boolean isValidatePeerCerts() {
        return this._validatePeerCerts;
    }

    public void setValidatePeerCerts(boolean validatePeerCerts) {
        this._validatePeerCerts = validatePeerCerts;
    }

    public void setKeyStorePassword(String password) {
        this._keyStorePassword = password == null ? (this._keyStoreResource != null ? this.getPassword(PASSWORD_PROPERTY) : null) : this.newPassword(password);
    }

    public void setKeyManagerPassword(String password) {
        this._keyManagerPassword = password == null ? (System.getProperty(KEYPASSWORD_PROPERTY) != null ? this.getPassword(KEYPASSWORD_PROPERTY) : null) : this.newPassword(password);
    }

    public void setTrustStorePassword(String password) {
        this._trustStorePassword = password == null ? (this._trustStoreResource != null && !this._trustStoreResource.equals(this._keyStoreResource) ? this.getPassword(PASSWORD_PROPERTY) : null) : this.newPassword(password);
    }

    @ManagedAttribute(value="The provider name")
    public String getProvider() {
        return this._sslProvider;
    }

    public void setProvider(String provider) {
        this._sslProvider = provider;
    }

    @ManagedAttribute(value="The TLS protocol")
    public String getProtocol() {
        return this._sslProtocol;
    }

    public void setProtocol(String protocol) {
        this._sslProtocol = protocol;
    }

    @ManagedAttribute(value="The SecureRandom algorithm")
    public String getSecureRandomAlgorithm() {
        return this._secureRandomAlgorithm;
    }

    public void setSecureRandomAlgorithm(String algorithm) {
        this._secureRandomAlgorithm = algorithm;
    }

    @ManagedAttribute(value="The KeyManagerFactory algorithm")
    public String getKeyManagerFactoryAlgorithm() {
        return this._keyManagerFactoryAlgorithm;
    }

    public void setKeyManagerFactoryAlgorithm(String algorithm) {
        this._keyManagerFactoryAlgorithm = algorithm;
    }

    @ManagedAttribute(value="The TrustManagerFactory algorithm")
    public String getTrustManagerFactoryAlgorithm() {
        return this._trustManagerFactoryAlgorithm;
    }

    @ManagedAttribute(value="Whether certificates should be trusted even if they are invalid")
    public boolean isTrustAll() {
        return this._trustAll;
    }

    public void setTrustAll(boolean trustAll) {
        this._trustAll = trustAll;
        if (trustAll) {
            this.setEndpointIdentificationAlgorithm(null);
        }
    }

    public void setTrustManagerFactoryAlgorithm(String algorithm) {
        this._trustManagerFactoryAlgorithm = algorithm;
    }

    @ManagedAttribute(value="Whether renegotiation is allowed")
    public boolean isRenegotiationAllowed() {
        return this._renegotiationAllowed;
    }

    public void setRenegotiationAllowed(boolean renegotiationAllowed) {
        this._renegotiationAllowed = renegotiationAllowed;
    }

    @ManagedAttribute(value="The max number of renegotiations allowed")
    public int getRenegotiationLimit() {
        return this._renegotiationLimit;
    }

    public void setRenegotiationLimit(int renegotiationLimit) {
        this._renegotiationLimit = renegotiationLimit;
    }

    @ManagedAttribute(value="The path to the certificate revocation list file")
    public String getCrlPath() {
        return this._crlPath;
    }

    public void setCrlPath(String crlPath) {
        this._crlPath = crlPath;
    }

    @ManagedAttribute(value="The maximum number of intermediate certificates")
    public int getMaxCertPathLength() {
        return this._maxCertPathLength;
    }

    public void setMaxCertPathLength(int maxCertPathLength) {
        this._maxCertPathLength = maxCertPathLength;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SSLContext getSslContext() {
        if (!this.isStarted()) {
            return this._setContext;
        }
        SslContextFactory sslContextFactory = this;
        synchronized (sslContextFactory) {
            if (this._factory == null) {
                throw new IllegalStateException("SslContextFactory reload failed");
            }
            return this._factory._context;
        }
    }

    public void setSslContext(SSLContext sslContext) {
        this._setContext = sslContext;
    }

    @ManagedAttribute(value="The endpoint identification algorithm")
    public String getEndpointIdentificationAlgorithm() {
        return this._endpointIdentificationAlgorithm;
    }

    public void setEndpointIdentificationAlgorithm(String endpointIdentificationAlgorithm) {
        this._endpointIdentificationAlgorithm = endpointIdentificationAlgorithm;
    }

    public PKIXCertPathChecker getPkixCertPathChecker() {
        return this._pkixCertPathChecker;
    }

    public void setPkixCertPathChecker(PKIXCertPathChecker pkixCertPatchChecker) {
        this._pkixCertPathChecker = pkixCertPatchChecker;
    }

    protected KeyStore loadKeyStore(Resource resource) throws Exception {
        String storePassword = Objects.toString(this._keyStorePassword, null);
        return CertificateUtils.getKeyStore(resource, this.getKeyStoreType(), this.getKeyStoreProvider(), storePassword);
    }

    protected KeyStore loadTrustStore(Resource resource) throws Exception {
        String type = Objects.toString(this.getTrustStoreType(), this.getKeyStoreType());
        String provider = Objects.toString(this.getTrustStoreProvider(), this.getKeyStoreProvider());
        Password passwd = this._trustStorePassword;
        if (resource == null || resource.equals(this._keyStoreResource)) {
            resource = this._keyStoreResource;
            if (passwd == null) {
                passwd = this._keyStorePassword;
            }
        }
        return CertificateUtils.getKeyStore(resource, type, provider, Objects.toString(passwd, null));
    }

    protected Collection<? extends CRL> loadCRL(String crlPath) throws Exception {
        return CertificateUtils.loadCRL(crlPath);
    }

    protected KeyManager[] getKeyManagers(KeyStore keyStore) throws Exception {
        KeyManager[] managers = null;
        if (keyStore != null) {
            KeyManagerFactory keyManagerFactory = this.getKeyManagerFactoryInstance();
            keyManagerFactory.init(keyStore, this._keyManagerPassword == null ? (this._keyStorePassword == null ? null : this._keyStorePassword.toString().toCharArray()) : this._keyManagerPassword.toString().toCharArray());
            managers = keyManagerFactory.getKeyManagers();
            if (managers != null) {
                boolean sniRequired;
                String alias = this.getCertAlias();
                if (alias != null) {
                    for (int idx = 0; idx < managers.length; ++idx) {
                        if (!(managers[idx] instanceof X509ExtendedKeyManager)) continue;
                        managers[idx] = new AliasedX509ExtendedKeyManager((X509ExtendedKeyManager)managers[idx], alias);
                    }
                }
                boolean bl = sniRequired = this instanceof Server && ((Server)this).isSniRequired();
                if (sniRequired || !this._certWilds.isEmpty() || this._certHosts.size() > 1 || this._certHosts.size() == 1 && this._aliasX509.size() > 1) {
                    for (int idx = 0; idx < managers.length; ++idx) {
                        if (!(managers[idx] instanceof X509ExtendedKeyManager)) continue;
                        managers[idx] = this.newSniX509ExtendedKeyManager((X509ExtendedKeyManager)managers[idx]);
                    }
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("managers={} for {}", managers, this);
        }
        return managers;
    }

    @Deprecated
    protected X509ExtendedKeyManager newSniX509ExtendedKeyManager(X509ExtendedKeyManager keyManager) {
        throw new IllegalStateException(String.format("KeyStores with multiple certificates are not supported on the base class %s. (Use %s or %s instead)", SslContextFactory.class.getName(), Server.class.getName(), Client.class.getName()));
    }

    protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception {
        TrustManager[] managers = null;
        if (trustStore != null) {
            if (this.isValidatePeerCerts() && "PKIX".equalsIgnoreCase(this.getTrustManagerFactoryAlgorithm())) {
                PKIXBuilderParameters pbParams = this.newPKIXBuilderParameters(trustStore, crls);
                TrustManagerFactory trustManagerFactory = this.getTrustManagerFactoryInstance();
                trustManagerFactory.init(new CertPathTrustManagerParameters(pbParams));
                managers = trustManagerFactory.getTrustManagers();
            } else {
                TrustManagerFactory trustManagerFactory = this.getTrustManagerFactoryInstance();
                trustManagerFactory.init(trustStore);
                managers = trustManagerFactory.getTrustManagers();
            }
        }
        return managers;
    }

    protected PKIXBuilderParameters newPKIXBuilderParameters(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception {
        PKIXBuilderParameters pbParams = new PKIXBuilderParameters(trustStore, (CertSelector)new X509CertSelector());
        pbParams.setMaxPathLength(this._maxCertPathLength);
        pbParams.setRevocationEnabled(true);
        if (this._pkixCertPathChecker != null) {
            pbParams.addCertPathChecker(this._pkixCertPathChecker);
        }
        if (crls != null && !crls.isEmpty()) {
            pbParams.addCertStore(this.getCertStoreInstance(crls));
        }
        if (this._enableCRLDP) {
            System.setProperty("com.sun.security.enableCRLDP", "true");
        }
        if (this._enableOCSP) {
            Security.setProperty("ocsp.enable", "true");
            if (this._ocspResponderURL != null) {
                Security.setProperty("ocsp.responderURL", this._ocspResponderURL);
            }
        }
        return pbParams;
    }

    public void selectProtocols(String[] enabledProtocols, String[] supportedProtocols) {
        List<String> selectedProtocols = this.processIncludeExcludePatterns("Protocols", enabledProtocols, supportedProtocols, this._includeProtocols, this._excludeProtocols);
        if (selectedProtocols.isEmpty()) {
            LOG.warn("No selected Protocols from {}", Arrays.asList(supportedProtocols));
        }
        this._selectedProtocols = selectedProtocols.toArray(new String[0]);
    }

    protected void selectCipherSuites(String[] enabledCipherSuites, String[] supportedCipherSuites) {
        Comparator<String> comparator;
        List<String> selectedCiphers = this.processIncludeExcludePatterns("Cipher Suite", enabledCipherSuites, supportedCipherSuites, this._includeCipherSuites, this._excludeCipherSuites);
        if (selectedCiphers.isEmpty()) {
            LOG.warn("No supported Cipher Suite from {}", Arrays.asList(supportedCipherSuites));
        }
        if ((comparator = this.getCipherComparator()) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sorting selected ciphers with {}", comparator);
            }
            selectedCiphers.sort(comparator);
        }
        this._selectedCipherSuites = selectedCiphers.toArray(new String[0]);
    }

    private List<String> processIncludeExcludePatterns(String type, String[] enabled, String[] supported, Set<String> included, Set<String> excluded) {
        Pattern pattern;
        ArrayList<String> selected = new ArrayList<String>();
        if (included.isEmpty()) {
            selected.addAll(Arrays.asList(enabled));
        } else {
            for (String includedItem : included) {
                pattern = Pattern.compile(includedItem);
                boolean added = false;
                for (String supportedItem : supported) {
                    if (!pattern.matcher(supportedItem).matches()) continue;
                    added = true;
                    selected.add(supportedItem);
                }
                if (added) continue;
                LOG.info("No {} matching '{}' is supported", type, includedItem);
            }
        }
        for (String excludedItem : excluded) {
            pattern = Pattern.compile(excludedItem);
            selected.removeIf(selectedItem -> pattern.matcher((CharSequence)selectedItem).matches());
        }
        return selected;
    }

    @Deprecated
    protected void processIncludeCipherSuites(String[] supportedCipherSuites, List<String> selectedCiphers) {
    }

    @Deprecated
    protected void removeExcludedCipherSuites(List<String> selectedCiphers) {
    }

    private void checkIsStarted() {
        if (!this.isStarted()) {
            throw new IllegalStateException("!STARTED: " + this);
        }
    }

    @ManagedAttribute(value="Whether certificate revocation list distribution points is enabled")
    public boolean isEnableCRLDP() {
        return this._enableCRLDP;
    }

    public void setEnableCRLDP(boolean enableCRLDP) {
        this._enableCRLDP = enableCRLDP;
    }

    @ManagedAttribute(value="Whether online certificate status protocol support is enabled")
    public boolean isEnableOCSP() {
        return this._enableOCSP;
    }

    public void setEnableOCSP(boolean enableOCSP) {
        this._enableOCSP = enableOCSP;
    }

    @ManagedAttribute(value="The online certificate status protocol URL")
    public String getOcspResponderURL() {
        return this._ocspResponderURL;
    }

    public void setOcspResponderURL(String ocspResponderURL) {
        this._ocspResponderURL = ocspResponderURL;
    }

    public void setKeyStore(KeyStore keyStore) {
        this._setKeyStore = keyStore;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyStore getKeyStore() {
        if (!this.isStarted()) {
            return this._setKeyStore;
        }
        SslContextFactory sslContextFactory = this;
        synchronized (sslContextFactory) {
            if (this._factory == null) {
                throw new IllegalStateException("SslContextFactory reload failed");
            }
            return this._factory._keyStore;
        }
    }

    public void setTrustStore(KeyStore trustStore) {
        this._setTrustStore = trustStore;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyStore getTrustStore() {
        if (!this.isStarted()) {
            return this._setTrustStore;
        }
        SslContextFactory sslContextFactory = this;
        synchronized (sslContextFactory) {
            if (this._factory == null) {
                throw new IllegalStateException("SslContextFactory reload failed");
            }
            return this._factory._trustStore;
        }
    }

    public void setKeyStoreResource(Resource resource) {
        this._keyStoreResource = resource;
    }

    public Resource getKeyStoreResource() {
        return this._keyStoreResource;
    }

    public void setTrustStoreResource(Resource resource) {
        this._trustStoreResource = resource;
    }

    public Resource getTrustStoreResource() {
        return this._trustStoreResource;
    }

    @ManagedAttribute(value="Whether TLS session caching is enabled")
    public boolean isSessionCachingEnabled() {
        return this._sessionCachingEnabled;
    }

    public void setSessionCachingEnabled(boolean enableSessionCaching) {
        this._sessionCachingEnabled = enableSessionCaching;
    }

    @ManagedAttribute(value="The maximum TLS session cache size")
    public int getSslSessionCacheSize() {
        return this._sslSessionCacheSize;
    }

    public void setSslSessionCacheSize(int sslSessionCacheSize) {
        this._sslSessionCacheSize = sslSessionCacheSize;
    }

    @ManagedAttribute(value="The TLS session cache timeout, in seconds")
    public int getSslSessionTimeout() {
        return this._sslSessionTimeout;
    }

    public void setSslSessionTimeout(int sslSessionTimeout) {
        this._sslSessionTimeout = sslSessionTimeout;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this._hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this._hostnameVerifier = hostnameVerifier;
    }

    protected Password getPassword(String realm) {
        return Password.getPassword(realm, null, null);
    }

    public Password newPassword(String password) {
        return new Password(password);
    }

    public SSLServerSocket newSslServerSocket(String host, int port, int backlog) throws IOException {
        this.checkIsStarted();
        SSLContext context = this.getSslContext();
        SSLServerSocketFactory factory = context.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket)(host == null ? factory.createServerSocket(port, backlog) : factory.createServerSocket(port, backlog, InetAddress.getByName(host)));
        socket.setSSLParameters(this.customize(socket.getSSLParameters()));
        return socket;
    }

    public SSLSocket newSslSocket() throws IOException {
        this.checkIsStarted();
        SSLContext context = this.getSslContext();
        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket)factory.createSocket();
        socket.setSSLParameters(this.customize(socket.getSSLParameters()));
        return socket;
    }

    protected CertificateFactory getCertificateFactoryInstance(String type) throws CertificateException {
        block3: {
            String provider = this.getProvider();
            try {
                if (provider != null) {
                    return CertificateFactory.getInstance(type, provider);
                }
            }
            catch (Throwable cause) {
                LOG.info("Unable to get CertificateFactory instance for type [{}] on provider [{}], using default", type, provider);
                if (!LOG.isDebugEnabled()) break block3;
                LOG.debug(cause);
            }
        }
        return CertificateFactory.getInstance(type);
    }

    protected CertStore getCertStoreInstance(Collection<? extends CRL> crls) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        String type;
        block3: {
            type = "Collection";
            String provider = this.getProvider();
            try {
                if (provider != null) {
                    return CertStore.getInstance(type, (CertStoreParameters)new CollectionCertStoreParameters(crls), provider);
                }
            }
            catch (Throwable cause) {
                LOG.info("Unable to get CertStore instance for type [{}] on provider [{}], using default", type, provider);
                if (!LOG.isDebugEnabled()) break block3;
                LOG.debug(cause);
            }
        }
        return CertStore.getInstance(type, new CollectionCertStoreParameters(crls));
    }

    protected KeyManagerFactory getKeyManagerFactoryInstance() throws NoSuchAlgorithmException {
        String algorithm;
        block3: {
            algorithm = this.getKeyManagerFactoryAlgorithm();
            String provider = this.getProvider();
            try {
                if (provider != null) {
                    return KeyManagerFactory.getInstance(algorithm, provider);
                }
            }
            catch (Throwable cause) {
                LOG.info("Unable to get KeyManagerFactory instance for algorithm [{}] on provider [{}], using default", algorithm, provider);
                if (!LOG.isDebugEnabled()) break block3;
                LOG.debug(cause);
            }
        }
        return KeyManagerFactory.getInstance(algorithm);
    }

    protected SecureRandom getSecureRandomInstance() throws NoSuchAlgorithmException {
        String algorithm = this.getSecureRandomAlgorithm();
        if (algorithm != null) {
            block4: {
                String provider = this.getProvider();
                try {
                    if (provider != null) {
                        return SecureRandom.getInstance(algorithm, provider);
                    }
                }
                catch (Throwable cause) {
                    LOG.info("Unable to get SecureRandom instance for algorithm [{}] on provider [{}], using default", algorithm, provider);
                    if (!LOG.isDebugEnabled()) break block4;
                    LOG.debug(cause);
                }
            }
            return SecureRandom.getInstance(algorithm);
        }
        return null;
    }

    protected SSLContext getSSLContextInstance() throws NoSuchAlgorithmException {
        String protocol;
        block3: {
            protocol = this.getProtocol();
            String provider = this.getProvider();
            try {
                if (provider != null) {
                    return SSLContext.getInstance(protocol, provider);
                }
            }
            catch (Throwable cause) {
                LOG.info("Unable to get SSLContext instance for protocol [{}] on provider [{}], using default", protocol, provider);
                if (!LOG.isDebugEnabled()) break block3;
                LOG.debug(cause);
            }
        }
        return SSLContext.getInstance(protocol);
    }

    protected TrustManagerFactory getTrustManagerFactoryInstance() throws NoSuchAlgorithmException {
        String algorithm;
        block3: {
            algorithm = this.getTrustManagerFactoryAlgorithm();
            String provider = this.getProvider();
            try {
                if (provider != null) {
                    return TrustManagerFactory.getInstance(algorithm, provider);
                }
            }
            catch (Throwable cause) {
                LOG.info("Unable to get TrustManagerFactory instance for algorithm [{}] on provider [{}], using default", algorithm, provider);
                if (!LOG.isDebugEnabled()) break block3;
                LOG.debug(cause);
            }
        }
        return TrustManagerFactory.getInstance(algorithm);
    }

    public SSLEngine newSSLEngine() {
        this.checkIsStarted();
        SSLContext context = this.getSslContext();
        SSLEngine sslEngine = context.createSSLEngine();
        this.customize(sslEngine);
        return sslEngine;
    }

    public SSLEngine newSSLEngine(String host, int port) {
        this.checkIsStarted();
        SSLContext context = this.getSslContext();
        SSLEngine sslEngine = this.isSessionCachingEnabled() ? context.createSSLEngine(host, port) : context.createSSLEngine();
        this.customize(sslEngine);
        return sslEngine;
    }

    public SSLEngine newSSLEngine(InetSocketAddress address) {
        if (address == null) {
            return this.newSSLEngine();
        }
        return this.newSSLEngine(address.getHostString(), address.getPort());
    }

    public void customize(SSLEngine sslEngine) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Customize {}", sslEngine);
        }
        sslEngine.setSSLParameters(this.customize(sslEngine.getSSLParameters()));
    }

    public SSLParameters customize(SSLParameters sslParams) {
        sslParams.setEndpointIdentificationAlgorithm(this.getEndpointIdentificationAlgorithm());
        sslParams.setUseCipherSuitesOrder(this.isUseCipherSuitesOrder());
        if (!this._certHosts.isEmpty() || !this._certWilds.isEmpty()) {
            sslParams.setSNIMatchers(Collections.singletonList(new AliasSNIMatcher()));
        }
        if (this._selectedCipherSuites != null) {
            sslParams.setCipherSuites(this._selectedCipherSuites);
        }
        if (this._selectedProtocols != null) {
            sslParams.setProtocols(this._selectedProtocols);
        }
        if (!(this instanceof Client)) {
            if (this.getWantClientAuth()) {
                sslParams.setWantClientAuth(true);
            }
            if (this.getNeedClientAuth()) {
                sslParams.setNeedClientAuth(true);
            }
        }
        return sslParams;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reload(Consumer<SslContextFactory> consumer) throws Exception {
        SslContextFactory sslContextFactory = this;
        synchronized (sslContextFactory) {
            consumer.accept(this);
            this.unload();
            this.load();
        }
    }

    public X509Certificate[] getX509CertChain(SSLSession sslSession) {
        return SslContextFactory.getX509CertChain(this, sslSession);
    }

    public static X509Certificate[] getCertChain(SSLSession sslSession) {
        return SslContextFactory.getX509CertChain(null, sslSession);
    }

    private static X509Certificate[] getX509CertChain(SslContextFactory sslContextFactory, SSLSession sslSession) {
        try {
            Certificate[] javaxCerts = sslSession.getPeerCertificates();
            if (javaxCerts == null || javaxCerts.length == 0) {
                return null;
            }
            int length = javaxCerts.length;
            X509Certificate[] javaCerts = new X509Certificate[length];
            String type = "X.509";
            CertificateFactory cf = sslContextFactory != null ? sslContextFactory.getCertificateFactoryInstance(type) : CertificateFactory.getInstance(type);
            for (int i = 0; i < length; ++i) {
                byte[] bytes = javaxCerts[i].getEncoded();
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                javaCerts[i] = (X509Certificate)cf.generateCertificate(stream);
            }
            return javaCerts;
        }
        catch (SSLPeerUnverifiedException pue) {
            return null;
        }
        catch (Exception e) {
            LOG.warn("EXCEPTION ", e);
            return null;
        }
    }

    public static int deduceKeyLength(String cipherSuite) {
        if (cipherSuite == null) {
            return 0;
        }
        if (cipherSuite.contains("WITH_AES_256_")) {
            return 256;
        }
        if (cipherSuite.contains("WITH_RC4_128_")) {
            return 128;
        }
        if (cipherSuite.contains("WITH_AES_128_")) {
            return 128;
        }
        if (cipherSuite.contains("WITH_RC4_40_")) {
            return 40;
        }
        if (cipherSuite.contains("WITH_3DES_EDE_CBC_")) {
            return 168;
        }
        if (cipherSuite.contains("WITH_IDEA_CBC_")) {
            return 128;
        }
        if (cipherSuite.contains("WITH_RC2_CBC_40_")) {
            return 40;
        }
        if (cipherSuite.contains("WITH_DES40_CBC_")) {
            return 40;
        }
        if (cipherSuite.contains("WITH_DES_CBC_")) {
            return 56;
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("%s@%x[provider=%s,keyStore=%s,trustStore=%s]", this.getClass().getSimpleName(), this.hashCode(), this._sslProvider, this._keyStoreResource, this._trustStoreResource);
    }

    public static class X509ExtendedTrustManagerWrapper
    extends X509ExtendedTrustManager {
        private final X509ExtendedTrustManager trustManager;

        public X509ExtendedTrustManagerWrapper(X509ExtendedTrustManager trustManager) {
            this.trustManager = trustManager;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.trustManager == null ? new X509Certificate[]{} : this.trustManager.getAcceptedIssuers();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            if (this.trustManager != null) {
                this.trustManager.checkClientTrusted(certs, authType);
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            if (this.trustManager != null) {
                this.trustManager.checkClientTrusted(chain, authType, socket);
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            if (this.trustManager != null) {
                this.trustManager.checkClientTrusted(chain, authType, engine);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            if (this.trustManager != null) {
                this.trustManager.checkServerTrusted(certs, authType);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            if (this.trustManager != null) {
                this.trustManager.checkServerTrusted(chain, authType, socket);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            if (this.trustManager != null) {
                this.trustManager.checkServerTrusted(chain, authType, engine);
            }
        }
    }

    public static class X509ExtendedKeyManagerWrapper
    extends X509ExtendedKeyManager {
        private final X509ExtendedKeyManager keyManager;

        public X509ExtendedKeyManagerWrapper(X509ExtendedKeyManager keyManager) {
            this.keyManager = keyManager;
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return this.keyManager == null ? null : this.keyManager.getClientAliases(keyType, issuers);
        }

        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return this.keyManager == null ? null : this.keyManager.chooseClientAlias(keyType, issuers, socket);
        }

        @Override
        public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
            return this.keyManager == null ? null : this.keyManager.chooseEngineClientAlias(keyType, issuers, engine);
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return this.keyManager == null ? null : this.keyManager.getServerAliases(keyType, issuers);
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return this.keyManager == null ? null : this.keyManager.chooseServerAlias(keyType, issuers, socket);
        }

        @Override
        public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
            return this.keyManager == null ? null : this.keyManager.chooseEngineServerAlias(keyType, issuers, engine);
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            return this.keyManager == null ? null : this.keyManager.getCertificateChain(alias);
        }

        @Override
        public PrivateKey getPrivateKey(String alias) {
            return this.keyManager == null ? null : this.keyManager.getPrivateKey(alias);
        }
    }

    @ManagedObject
    public static class Server
    extends SslContextFactory
    implements SniX509ExtendedKeyManager.SniSelector {
        private boolean _sniRequired;
        private SniX509ExtendedKeyManager.SniSelector _sniSelector;

        public Server() {
            this.setEndpointIdentificationAlgorithm(null);
        }

        @Override
        public boolean getWantClientAuth() {
            return super.getWantClientAuth();
        }

        @Override
        public void setWantClientAuth(boolean wantClientAuth) {
            super.setWantClientAuth(wantClientAuth);
        }

        @Override
        public boolean getNeedClientAuth() {
            return super.getNeedClientAuth();
        }

        @Override
        public void setNeedClientAuth(boolean needClientAuth) {
            super.setNeedClientAuth(needClientAuth);
        }

        @ManagedAttribute(value="Whether the TLS handshake is rejected if there is no SNI host match")
        public boolean isSniRequired() {
            return this._sniRequired;
        }

        public void setSniRequired(boolean sniRequired) {
            this._sniRequired = sniRequired;
        }

        @Override
        protected KeyManager[] getKeyManagers(KeyStore keyStore) throws Exception {
            KeyManager[] managers;
            block2: {
                block3: {
                    managers = super.getKeyManagers(keyStore);
                    if (!this.isSniRequired()) break block2;
                    if (managers == null) break block3;
                    if (!Arrays.stream(managers).noneMatch(SniX509ExtendedKeyManager.class::isInstance)) break block2;
                }
                throw new IllegalStateException("No SNI Key managers when SNI is required");
            }
            return managers;
        }

        public SniX509ExtendedKeyManager.SniSelector getSNISelector() {
            return this._sniSelector;
        }

        public void setSNISelector(SniX509ExtendedKeyManager.SniSelector sniSelector) {
            this._sniSelector = sniSelector;
        }

        @Override
        public String sniSelect(String keyType, Principal[] issuers, SSLSession session, String sniHost, Collection<X509> certificates) {
            if (sniHost == null) {
                return this._sniRequired ? null : "delegate_no_sni_match";
            }
            List matching = certificates.stream().filter(x509 -> x509.matches(sniHost)).collect(Collectors.toList());
            if (matching.isEmpty()) {
                return this.isSniRequired() ? null : "delegate_no_sni_match";
            }
            String alias = ((X509)matching.get(0)).getAlias();
            if (matching.size() == 1) {
                return alias;
            }
            return matching.stream().min(Comparator.comparingInt(cert -> cert.getWilds().size())).map(X509::getAlias).orElse(alias);
        }

        @Override
        protected X509ExtendedKeyManager newSniX509ExtendedKeyManager(X509ExtendedKeyManager keyManager) {
            return new SniX509ExtendedKeyManager(keyManager, this);
        }
    }

    public static class Client
    extends SslContextFactory {
        public Client() {
            this(false);
        }

        public Client(boolean trustAll) {
            super(trustAll);
        }

        @Override
        protected void checkConfiguration() {
            this.checkTrustAll();
            this.checkEndPointIdentificationAlgorithm();
            super.checkConfiguration();
        }

        @Override
        protected X509ExtendedKeyManager newSniX509ExtendedKeyManager(X509ExtendedKeyManager keyManager) {
            return keyManager;
        }
    }

    class AliasSNIMatcher
    extends SNIMatcher {
        private String _host;

        AliasSNIMatcher() {
            super(0);
        }

        @Override
        public boolean matches(SNIServerName serverName) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("SNI matching for {}", serverName);
            }
            if (serverName instanceof SNIHostName) {
                this._host = StringUtil.asciiToLowerCase(((SNIHostName)serverName).getAsciiName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SNI host name {}", this._host);
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("No SNI host name for {}", serverName);
            }
            return true;
        }

        public String getHost() {
            return this._host;
        }
    }

    class Factory {
        private final KeyStore _keyStore;
        private final KeyStore _trustStore;
        private final SSLContext _context;

        Factory(KeyStore keyStore, KeyStore trustStore, SSLContext context) {
            this._keyStore = keyStore;
            this._trustStore = trustStore;
            this._context = context;
        }
    }
}

