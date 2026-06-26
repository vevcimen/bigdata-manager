/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.TreeTrie;
import org.eclipse.jetty.util.Trie;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject(value="HTTP Configuration")
public class HttpConfiguration
implements Dumpable {
    private static final Logger LOG = Log.getLogger(HttpConfiguration.class);
    public static final String SERVER_VERSION = "Jetty(" + Jetty.VERSION + ")";
    private final List<Customizer> _customizers = new CopyOnWriteArrayList<Customizer>();
    private final Trie<Boolean> _formEncodedMethods = new TreeTrie<Boolean>();
    private int _outputBufferSize = 32768;
    private int _outputAggregationSize = this._outputBufferSize / 4;
    private int _requestHeaderSize = 8192;
    private int _responseHeaderSize = 8192;
    private int _headerCacheSize = 1024;
    private int _securePort;
    private long _idleTimeout = -1L;
    private long _blockingTimeout = -1L;
    private String _secureScheme = HttpScheme.HTTPS.asString();
    private boolean _sendServerVersion = true;
    private boolean _sendXPoweredBy = false;
    private boolean _sendDateHeader = true;
    private boolean _delayDispatchUntilContent = true;
    private boolean _persistentConnectionsEnabled = true;
    private int _maxErrorDispatches = 10;
    private long _minRequestDataRate;
    private long _minResponseDataRate;
    private CookieCompliance _requestCookieCompliance = CookieCompliance.RFC6265;
    private CookieCompliance _responseCookieCompliance = CookieCompliance.RFC6265;
    private MultiPartFormDataCompliance _multiPartCompliance = MultiPartFormDataCompliance.LEGACY;
    private boolean _notifyRemoteAsyncErrors = true;
    private boolean _relativeRedirectAllowed;

    public HttpConfiguration() {
        this._formEncodedMethods.put(HttpMethod.POST.asString(), Boolean.TRUE);
        this._formEncodedMethods.put(HttpMethod.PUT.asString(), Boolean.TRUE);
    }

    public HttpConfiguration(HttpConfiguration config) {
        this._customizers.addAll(config._customizers);
        for (String s2 : config._formEncodedMethods.keySet()) {
            this._formEncodedMethods.put(s2, Boolean.TRUE);
        }
        this._outputBufferSize = config._outputBufferSize;
        this._outputAggregationSize = config._outputAggregationSize;
        this._requestHeaderSize = config._requestHeaderSize;
        this._responseHeaderSize = config._responseHeaderSize;
        this._headerCacheSize = config._headerCacheSize;
        this._secureScheme = config._secureScheme;
        this._securePort = config._securePort;
        this._idleTimeout = config._idleTimeout;
        this._blockingTimeout = config._blockingTimeout;
        this._sendDateHeader = config._sendDateHeader;
        this._sendServerVersion = config._sendServerVersion;
        this._sendXPoweredBy = config._sendXPoweredBy;
        this._delayDispatchUntilContent = config._delayDispatchUntilContent;
        this._persistentConnectionsEnabled = config._persistentConnectionsEnabled;
        this._maxErrorDispatches = config._maxErrorDispatches;
        this._minRequestDataRate = config._minRequestDataRate;
        this._minResponseDataRate = config._minResponseDataRate;
        this._requestCookieCompliance = config._requestCookieCompliance;
        this._responseCookieCompliance = config._responseCookieCompliance;
        this._multiPartCompliance = config._multiPartCompliance;
        this._notifyRemoteAsyncErrors = config._notifyRemoteAsyncErrors;
        this._relativeRedirectAllowed = config._relativeRedirectAllowed;
    }

    public void addCustomizer(Customizer customizer) {
        this._customizers.add(customizer);
    }

    public List<Customizer> getCustomizers() {
        return this._customizers;
    }

    public <T> T getCustomizer(Class<T> type) {
        for (Customizer c : this._customizers) {
            if (!type.isAssignableFrom(c.getClass())) continue;
            return (T)c;
        }
        return null;
    }

    @ManagedAttribute(value="The size in bytes of the output buffer used to aggregate HTTP output")
    public int getOutputBufferSize() {
        return this._outputBufferSize;
    }

    @ManagedAttribute(value="The maximum size in bytes for HTTP output to be aggregated")
    public int getOutputAggregationSize() {
        return this._outputAggregationSize;
    }

    @ManagedAttribute(value="The maximum allowed size in bytes for an HTTP request header")
    public int getRequestHeaderSize() {
        return this._requestHeaderSize;
    }

    @ManagedAttribute(value="The maximum allowed size in bytes for an HTTP response header")
    public int getResponseHeaderSize() {
        return this._responseHeaderSize;
    }

    @ManagedAttribute(value="The maximum allowed size in Trie nodes for an HTTP header field cache")
    public int getHeaderCacheSize() {
        return this._headerCacheSize;
    }

    @ManagedAttribute(value="The port to which Integral or Confidential security constraints are redirected")
    public int getSecurePort() {
        return this._securePort;
    }

    @ManagedAttribute(value="The scheme with which Integral or Confidential security constraints are redirected")
    public String getSecureScheme() {
        return this._secureScheme;
    }

    @ManagedAttribute(value="Whether persistent connections are enabled")
    public boolean isPersistentConnectionsEnabled() {
        return this._persistentConnectionsEnabled;
    }

    @ManagedAttribute(value="The idle timeout in ms for I/O operations during the handling of an HTTP request")
    public long getIdleTimeout() {
        return this._idleTimeout;
    }

    public void setIdleTimeout(long timeoutMs) {
        this._idleTimeout = timeoutMs;
    }

    @ManagedAttribute(value="Total timeout in ms for blocking I/O operations. DEPRECATED!", readonly=true)
    @Deprecated
    public long getBlockingTimeout() {
        return this._blockingTimeout;
    }

    @Deprecated
    public void setBlockingTimeout(long blockingTimeout) {
        if (blockingTimeout > 0L) {
            LOG.warn("HttpConfiguration.setBlockingTimeout is deprecated!", new Object[0]);
        }
        this._blockingTimeout = blockingTimeout;
    }

    public void setPersistentConnectionsEnabled(boolean persistentConnectionsEnabled) {
        this._persistentConnectionsEnabled = persistentConnectionsEnabled;
    }

    public void setSendServerVersion(boolean sendServerVersion) {
        this._sendServerVersion = sendServerVersion;
    }

    @ManagedAttribute(value="Whether to send the Server header in responses")
    public boolean getSendServerVersion() {
        return this._sendServerVersion;
    }

    public void writePoweredBy(Appendable out, String preamble, String postamble) throws IOException {
        if (this.getSendServerVersion()) {
            if (preamble != null) {
                out.append(preamble);
            }
            out.append(Jetty.POWERED_BY);
            if (postamble != null) {
                out.append(postamble);
            }
        }
    }

    public void setSendXPoweredBy(boolean sendXPoweredBy) {
        this._sendXPoweredBy = sendXPoweredBy;
    }

    @ManagedAttribute(value="Whether to send the X-Powered-By header in responses")
    public boolean getSendXPoweredBy() {
        return this._sendXPoweredBy;
    }

    public void setSendDateHeader(boolean sendDateHeader) {
        this._sendDateHeader = sendDateHeader;
    }

    @ManagedAttribute(value="Whether to send the Date header in responses")
    public boolean getSendDateHeader() {
        return this._sendDateHeader;
    }

    public void setDelayDispatchUntilContent(boolean delay) {
        this._delayDispatchUntilContent = delay;
    }

    @ManagedAttribute(value="Whether to delay the application dispatch until content is available")
    public boolean isDelayDispatchUntilContent() {
        return this._delayDispatchUntilContent;
    }

    public void setCustomizers(List<Customizer> customizers) {
        this._customizers.clear();
        this._customizers.addAll(customizers);
    }

    public void setOutputBufferSize(int outputBufferSize) {
        this._outputBufferSize = outputBufferSize;
        this.setOutputAggregationSize(outputBufferSize / 4);
    }

    public void setOutputAggregationSize(int outputAggregationSize) {
        this._outputAggregationSize = outputAggregationSize;
    }

    public void setRequestHeaderSize(int requestHeaderSize) {
        this._requestHeaderSize = requestHeaderSize;
    }

    public void setResponseHeaderSize(int responseHeaderSize) {
        this._responseHeaderSize = responseHeaderSize;
    }

    public void setHeaderCacheSize(int headerCacheSize) {
        this._headerCacheSize = headerCacheSize;
    }

    public void setSecurePort(int securePort) {
        this._securePort = securePort;
    }

    public void setSecureScheme(String secureScheme) {
        this._secureScheme = secureScheme;
    }

    public void setFormEncodedMethods(String ... methods) {
        this._formEncodedMethods.clear();
        for (String method : methods) {
            this.addFormEncodedMethod(method);
        }
    }

    public Set<String> getFormEncodedMethods() {
        return this._formEncodedMethods.keySet();
    }

    public void addFormEncodedMethod(String method) {
        this._formEncodedMethods.put(method, Boolean.TRUE);
    }

    public boolean isFormEncodedMethod(String method) {
        return Boolean.TRUE.equals(this._formEncodedMethods.get(method));
    }

    @ManagedAttribute(value="The maximum ERROR dispatches for a request for loop prevention (default 10)")
    public int getMaxErrorDispatches() {
        return this._maxErrorDispatches;
    }

    public void setMaxErrorDispatches(int max) {
        this._maxErrorDispatches = max;
    }

    @ManagedAttribute(value="The minimum request content data rate in bytes per second")
    public long getMinRequestDataRate() {
        return this._minRequestDataRate;
    }

    public void setMinRequestDataRate(long bytesPerSecond) {
        this._minRequestDataRate = bytesPerSecond;
    }

    @ManagedAttribute(value="The minimum response content data rate in bytes per second")
    public long getMinResponseDataRate() {
        return this._minResponseDataRate;
    }

    public void setMinResponseDataRate(long bytesPerSecond) {
        this._minResponseDataRate = bytesPerSecond;
    }

    public CookieCompliance getRequestCookieCompliance() {
        return this._requestCookieCompliance;
    }

    public CookieCompliance getResponseCookieCompliance() {
        return this._responseCookieCompliance;
    }

    public void setRequestCookieCompliance(CookieCompliance cookieCompliance) {
        this._requestCookieCompliance = cookieCompliance == null ? CookieCompliance.RFC6265 : cookieCompliance;
    }

    public void setResponseCookieCompliance(CookieCompliance cookieCompliance) {
        this._responseCookieCompliance = cookieCompliance == null ? CookieCompliance.RFC6265 : cookieCompliance;
    }

    @Deprecated
    public void setCookieCompliance(CookieCompliance compliance) {
        this.setRequestCookieCompliance(compliance);
    }

    @Deprecated
    public CookieCompliance getCookieCompliance() {
        return this.getRequestCookieCompliance();
    }

    @Deprecated
    public boolean isCookieCompliance(CookieCompliance compliance) {
        return this._requestCookieCompliance.equals((Object)compliance);
    }

    public void setMultiPartFormDataCompliance(MultiPartFormDataCompliance multiPartCompliance) {
        this._multiPartCompliance = multiPartCompliance == null ? MultiPartFormDataCompliance.LEGACY : multiPartCompliance;
    }

    public MultiPartFormDataCompliance getMultipartFormDataCompliance() {
        return this._multiPartCompliance;
    }

    public void setNotifyRemoteAsyncErrors(boolean notifyRemoteAsyncErrors) {
        this._notifyRemoteAsyncErrors = notifyRemoteAsyncErrors;
    }

    @ManagedAttribute(value="Whether remote errors, when detected, are notified to async applications")
    public boolean isNotifyRemoteAsyncErrors() {
        return this._notifyRemoteAsyncErrors;
    }

    public void setRelativeRedirectAllowed(boolean allowed) {
        this._relativeRedirectAllowed = allowed;
    }

    @ManagedAttribute(value="Whether relative redirection locations are allowed")
    public boolean isRelativeRedirectAllowed() {
        return this._relativeRedirectAllowed;
    }

    @Override
    public String dump() {
        return Dumpable.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Dumpable.dumpObjects(out, indent, this, new DumpableCollection("customizers", this._customizers), new DumpableCollection("formEncodedMethods", this._formEncodedMethods.keySet()), "outputBufferSize=" + this._outputBufferSize, "outputAggregationSize=" + this._outputAggregationSize, "requestHeaderSize=" + this._requestHeaderSize, "responseHeaderSize=" + this._responseHeaderSize, "headerCacheSize=" + this._headerCacheSize, "secureScheme=" + this._secureScheme, "securePort=" + this._securePort, "idleTimeout=" + this._idleTimeout, "blockingTimeout=" + this._blockingTimeout, "sendDateHeader=" + this._sendDateHeader, "sendServerVersion=" + this._sendServerVersion, "sendXPoweredBy=" + this._sendXPoweredBy, "delayDispatchUntilContent=" + this._delayDispatchUntilContent, "persistentConnectionsEnabled=" + this._persistentConnectionsEnabled, "maxErrorDispatches=" + this._maxErrorDispatches, "minRequestDataRate=" + this._minRequestDataRate, "minResponseDataRate=" + this._minResponseDataRate, "cookieCompliance=" + (Object)((Object)this._requestCookieCompliance), "setRequestCookieCompliance=" + (Object)((Object)this._responseCookieCompliance), "notifyRemoteAsyncErrors=" + this._notifyRemoteAsyncErrors, "relativeRedirectAllowed=" + this._relativeRedirectAllowed);
    }

    public String toString() {
        return String.format("%s@%x{%d/%d,%d/%d,%s://:%d,%s}", this.getClass().getSimpleName(), this.hashCode(), this._outputBufferSize, this._outputAggregationSize, this._requestHeaderSize, this._responseHeaderSize, this._secureScheme, this._securePort, this._customizers);
    }

    public static interface ConnectionFactory {
        public HttpConfiguration getHttpConfiguration();
    }

    public static interface Customizer {
        public void customize(Connector var1, HttpConfiguration var2, Request var3);
    }
}

