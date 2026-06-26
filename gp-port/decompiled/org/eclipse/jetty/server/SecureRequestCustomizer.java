/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.ssl.X509;

public class SecureRequestCustomizer
implements HttpConfiguration.Customizer {
    private static final Logger LOG = Log.getLogger(SecureRequestCustomizer.class);
    public static final String JAVAX_SERVLET_REQUEST_X_509_CERTIFICATE = "javax.servlet.request.X509Certificate";
    public static final String JAVAX_SERVLET_REQUEST_CIPHER_SUITE = "javax.servlet.request.cipher_suite";
    public static final String JAVAX_SERVLET_REQUEST_KEY_SIZE = "javax.servlet.request.key_size";
    public static final String JAVAX_SERVLET_REQUEST_SSL_SESSION_ID = "javax.servlet.request.ssl_session_id";
    private String sslSessionAttribute = "org.eclipse.jetty.servlet.request.ssl_session";
    private boolean _sniRequired;
    private boolean _sniHostCheck;
    private long _stsMaxAge = -1L;
    private boolean _stsIncludeSubDomains;
    private HttpField _stsField;

    public SecureRequestCustomizer() {
        this(true);
    }

    public SecureRequestCustomizer(@Name(value="sniHostCheck") boolean sniHostCheck) {
        this(sniHostCheck, -1L, false);
    }

    public SecureRequestCustomizer(@Name(value="sniHostCheck") boolean sniHostCheck, @Name(value="stsMaxAgeSeconds") long stsMaxAgeSeconds, @Name(value="stsIncludeSubdomains") boolean stsIncludeSubdomains) {
        this(false, sniHostCheck, stsMaxAgeSeconds, stsIncludeSubdomains);
    }

    public SecureRequestCustomizer(@Name(value="sniRequired") boolean sniRequired, @Name(value="sniHostCheck") boolean sniHostCheck, @Name(value="stsMaxAgeSeconds") long stsMaxAgeSeconds, @Name(value="stsIncludeSubdomains") boolean stsIncludeSubdomains) {
        this._sniRequired = sniRequired;
        this._sniHostCheck = sniHostCheck;
        this._stsMaxAge = stsMaxAgeSeconds;
        this._stsIncludeSubDomains = stsIncludeSubdomains;
        this.formatSTS();
    }

    public boolean isSniHostCheck() {
        return this._sniHostCheck;
    }

    public void setSniHostCheck(boolean sniHostCheck) {
        this._sniHostCheck = sniHostCheck;
    }

    public boolean isSniRequired() {
        return this._sniRequired;
    }

    public void setSniRequired(boolean sniRequired) {
        this._sniRequired = sniRequired;
    }

    public long getStsMaxAge() {
        return this._stsMaxAge;
    }

    public void setStsMaxAge(long stsMaxAgeSeconds) {
        this._stsMaxAge = stsMaxAgeSeconds;
        this.formatSTS();
    }

    public void setStsMaxAge(long period, TimeUnit units) {
        this._stsMaxAge = units.toSeconds(period);
        this.formatSTS();
    }

    public boolean isStsIncludeSubDomains() {
        return this._stsIncludeSubDomains;
    }

    public void setStsIncludeSubDomains(boolean stsIncludeSubDomains) {
        this._stsIncludeSubDomains = stsIncludeSubDomains;
        this.formatSTS();
    }

    private void formatSTS() {
        this._stsField = this._stsMaxAge < 0L ? null : new PreEncodedHttpField(HttpHeader.STRICT_TRANSPORT_SECURITY, String.format("max-age=%d%s", this._stsMaxAge, this._stsIncludeSubDomains ? "; includeSubDomains" : ""));
    }

    @Override
    public void customize(Connector connector, HttpConfiguration channelConfig, Request request) {
        EndPoint endp = request.getHttpChannel().getEndPoint();
        if (endp instanceof SslConnection.DecryptedEndPoint) {
            SslConnection.DecryptedEndPoint sslEndp = (SslConnection.DecryptedEndPoint)endp;
            SslConnection sslConnection = sslEndp.getSslConnection();
            SSLEngine sslEngine = sslConnection.getSSLEngine();
            this.customize(sslEngine, request);
            if (request.getHttpURI().getScheme() == null) {
                request.setScheme(HttpScheme.HTTPS.asString());
            }
        } else if (endp instanceof ProxyConnectionFactory.ProxyEndPoint) {
            ProxyConnectionFactory.ProxyEndPoint proxy = (ProxyConnectionFactory.ProxyEndPoint)endp;
            if (request.getHttpURI().getScheme() == null && proxy.getAttribute("TLS_VERSION") != null) {
                request.setScheme(HttpScheme.HTTPS.asString());
            }
        }
        if (HttpScheme.HTTPS.is(request.getScheme())) {
            this.customizeSecure(request);
        }
    }

    protected void customizeSecure(Request request) {
        request.setSecure(true);
        if (this._stsField != null) {
            request.getResponse().getHttpFields().add(this._stsField);
        }
    }

    protected void customize(SSLEngine sslEngine, Request request) {
        SSLSession sslSession = sslEngine.getSession();
        if (this._sniHostCheck || this._sniRequired) {
            X509 x509 = (X509)sslSession.getValue("org.eclipse.jetty.util.ssl.snix509");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Host {} with SNI {}", request.getServerName(), x509);
            }
            if (x509 == null) {
                if (this._sniRequired) {
                    throw new BadMessageException(400, "SNI required");
                }
            } else if (this._sniHostCheck && !x509.matches(request.getServerName())) {
                throw new BadMessageException(400, "Host does not match SNI");
            }
        }
        request.setAttributes(new SslAttributes(request, sslSession, request.getAttributes()));
    }

    private X509Certificate[] getCertChain(Connector connector, SSLSession sslSession) {
        SslContextFactory sslContextFactory;
        SslConnectionFactory sslConnectionFactory = connector.getConnectionFactory(SslConnectionFactory.class);
        if (sslConnectionFactory != null && (sslContextFactory = sslConnectionFactory.getSslContextFactory()) != null) {
            return sslContextFactory.getX509CertChain(sslSession);
        }
        return SslContextFactory.getCertChain(sslSession);
    }

    public void setSslSessionAttribute(String attribute) {
        this.sslSessionAttribute = attribute;
    }

    public String getSslSessionAttribute() {
        return this.sslSessionAttribute;
    }

    public String toString() {
        return String.format("%s@%x", this.getClass().getSimpleName(), this.hashCode());
    }

    private static class SslSessionData {
        private final Integer _keySize;
        private final X509Certificate[] _certs;
        private final String _idStr;

        private SslSessionData(Integer keySize, X509Certificate[] certs, String idStr) {
            this._keySize = keySize;
            this._certs = certs;
            this._idStr = idStr;
        }

        private Integer getKeySize() {
            return this._keySize;
        }

        private X509Certificate[] getCerts() {
            return this._certs;
        }

        private String getIdStr() {
            return this._idStr;
        }
    }

    private class SslAttributes
    extends Attributes.Wrapper {
        private final Request _request;
        private final SSLSession _session;
        private X509Certificate[] _certs;
        private String _cipherSuite;
        private Integer _keySize;
        private String _sessionId;
        private String _sessionAttribute;

        public SslAttributes(Request request, SSLSession sslSession, Attributes attributes) {
            super(attributes);
            this._request = request;
            this._session = sslSession;
            try {
                SslSessionData sslSessionData = this.getSslSessionData();
                this._certs = sslSessionData.getCerts();
                this._cipherSuite = this._session.getCipherSuite();
                this._keySize = sslSessionData.getKeySize();
                this._sessionId = sslSessionData.getIdStr();
                this._sessionAttribute = SecureRequestCustomizer.this.getSslSessionAttribute();
            }
            catch (Exception e) {
                LOG.warn("Unable to get secure details ", e);
            }
        }

        @Override
        public Object getAttribute(String name) {
            switch (name) {
                case "javax.servlet.request.X509Certificate": {
                    return this._certs;
                }
                case "javax.servlet.request.cipher_suite": {
                    return this._cipherSuite;
                }
                case "javax.servlet.request.key_size": {
                    return this._keySize;
                }
                case "javax.servlet.request.ssl_session_id": {
                    return this._sessionId;
                }
            }
            if (!StringUtil.isEmpty(this._sessionAttribute) && this._sessionAttribute.equals(name)) {
                return this._session;
            }
            return this._attributes.getAttribute(name);
        }

        private SslSessionData getSslSessionData() {
            String key = SslSessionData.class.getName();
            SslSessionData sslSessionData = (SslSessionData)this._session.getValue(key);
            if (sslSessionData == null) {
                String cipherSuite = this._session.getCipherSuite();
                int keySize = SslContextFactory.deduceKeyLength(cipherSuite);
                X509Certificate[] certs = SecureRequestCustomizer.this.getCertChain(this._request.getHttpChannel().getConnector(), this._session);
                byte[] bytes = this._session.getId();
                String idStr = TypeUtil.toHexString(bytes);
                sslSessionData = new SslSessionData(keySize, certs, idStr);
                this._session.putValue(key, sslSessionData);
            }
            return sslSessionData;
        }

        @Override
        public Set<String> getAttributeNameSet() {
            HashSet<String> names = new HashSet<String>(this._attributes.getAttributeNameSet());
            names.remove(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_X_509_CERTIFICATE);
            names.remove(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_CIPHER_SUITE);
            names.remove(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_KEY_SIZE);
            names.remove(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_SSL_SESSION_ID);
            if (this._certs != null) {
                names.add(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_X_509_CERTIFICATE);
            }
            if (this._cipherSuite != null) {
                names.add(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_CIPHER_SUITE);
            }
            if (this._keySize != null) {
                names.add(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_KEY_SIZE);
            }
            if (this._sessionId != null) {
                names.add(SecureRequestCustomizer.JAVAX_SERVLET_REQUEST_SSL_SESSION_ID);
            }
            if (!StringUtil.isEmpty(this._sessionAttribute)) {
                names.add(this._sessionAttribute);
            }
            return names;
        }
    }
}

