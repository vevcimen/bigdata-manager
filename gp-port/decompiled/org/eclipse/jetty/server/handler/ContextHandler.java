/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ClassLoaderDump;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.ManagedAttributeListener;
import org.eclipse.jetty.server.handler.ScopedHandler;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.FutureCallback;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.component.Graceful;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;

@ManagedObject(value="URI Context")
public class ContextHandler
extends ScopedHandler
implements Attributes,
Graceful {
    public static final int SERVLET_MAJOR_VERSION = 3;
    public static final int SERVLET_MINOR_VERSION = 1;
    public static final Class<?>[] SERVLET_LISTENER_TYPES = new Class[]{ServletContextListener.class, ServletContextAttributeListener.class, ServletRequestListener.class, ServletRequestAttributeListener.class, HttpSessionIdListener.class, HttpSessionListener.class, HttpSessionAttributeListener.class};
    public static final int DEFAULT_LISTENER_TYPE_INDEX = 1;
    public static final int EXTENDED_LISTENER_TYPE_INDEX = 0;
    private static final String UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER = "Unimplemented {} - use org.eclipse.jetty.servlet.ServletContextHandler";
    private static final Logger LOG = Log.getLogger(ContextHandler.class);
    private static final ThreadLocal<Context> __context = new ThreadLocal();
    private static String __serverInfo = "jetty/" + Server.getVersion();
    public static final String MANAGED_ATTRIBUTES = "org.eclipse.jetty.server.context.ManagedAttributes";
    public static final String MAX_FORM_KEYS_KEY = "org.eclipse.jetty.server.Request.maxFormKeys";
    public static final String MAX_FORM_CONTENT_SIZE_KEY = "org.eclipse.jetty.server.Request.maxFormContentSize";
    public static final int DEFAULT_MAX_FORM_KEYS = 1000;
    public static final int DEFAULT_MAX_FORM_CONTENT_SIZE = 200000;
    protected ContextStatus _contextStatus = ContextStatus.NOTSET;
    protected Context _scontext;
    private final AttributesMap _attributes;
    private final Map<String, String> _initParams;
    private ClassLoader _classLoader;
    private String _contextPath = "/";
    private String _contextPathEncoded = "/";
    private String _displayName;
    private Resource _baseResource;
    private MimeTypes _mimeTypes;
    private Map<String, String> _localeEncodingMap;
    private String[] _welcomeFiles;
    private ErrorHandler _errorHandler;
    private String[] _vhosts;
    private boolean[] _vhostswildcard;
    private String[] _vconnectors;
    private Logger _logger;
    private boolean _allowNullPathInfo;
    private int _maxFormKeys = Integer.getInteger("org.eclipse.jetty.server.Request.maxFormKeys", 1000);
    private int _maxFormContentSize = Integer.getInteger("org.eclipse.jetty.server.Request.maxFormContentSize", 200000);
    private boolean _compactPath = false;
    private boolean _usingSecurityManager = System.getSecurityManager() != null;
    private final List<EventListener> _eventListeners = new CopyOnWriteArrayList<EventListener>();
    private final List<EventListener> _programmaticListeners = new CopyOnWriteArrayList<EventListener>();
    private final List<ServletContextListener> _servletContextListeners = new CopyOnWriteArrayList<ServletContextListener>();
    private final List<ServletContextListener> _destroyServletContextListeners = new ArrayList<ServletContextListener>();
    private final List<ServletContextAttributeListener> _servletContextAttributeListeners = new CopyOnWriteArrayList<ServletContextAttributeListener>();
    private final List<ServletRequestListener> _servletRequestListeners = new CopyOnWriteArrayList<ServletRequestListener>();
    private final List<ServletRequestAttributeListener> _servletRequestAttributeListeners = new CopyOnWriteArrayList<ServletRequestAttributeListener>();
    private final List<ContextScopeListener> _contextListeners = new CopyOnWriteArrayList<ContextScopeListener>();
    private final List<EventListener> _durableListeners = new CopyOnWriteArrayList<EventListener>();
    private String[] _protectedTargets;
    private final CopyOnWriteArrayList<AliasCheck> _aliasChecks = new CopyOnWriteArrayList();
    private final AtomicReference<Availability> _availability = new AtomicReference<Availability>(Availability.STOPPED);

    public static Context getCurrentContext() {
        return __context.get();
    }

    public static ContextHandler getContextHandler(ServletContext context) {
        if (context instanceof Context) {
            return ((Context)context).getContextHandler();
        }
        Context c = ContextHandler.getCurrentContext();
        if (c != null) {
            return c.getContextHandler();
        }
        return null;
    }

    public static String getServerInfo() {
        return __serverInfo;
    }

    public static void setServerInfo(String serverInfo) {
        __serverInfo = serverInfo;
    }

    public ContextHandler() {
        this(null, null, null);
    }

    protected ContextHandler(Context context) {
        this(context, null, null);
    }

    public ContextHandler(String contextPath) {
        this(null, null, contextPath);
    }

    public ContextHandler(HandlerContainer parent, String contextPath) {
        this(null, parent, contextPath);
    }

    private ContextHandler(Context context, HandlerContainer parent, String contextPath) {
        this._scontext = context == null ? new Context() : context;
        this._attributes = new AttributesMap();
        this._initParams = new HashMap<String, String>();
        this.addAliasCheck(new ApproveNonExistentDirectoryAliases());
        if (File.separatorChar == '/') {
            this.addAliasCheck(new AllowSymLinkAliasChecker());
        }
        if (contextPath != null) {
            this.setContextPath(contextPath);
        }
        if (parent instanceof HandlerWrapper) {
            ((HandlerWrapper)parent).setHandler(this);
        } else if (parent instanceof HandlerCollection) {
            ((HandlerCollection)parent).addHandler(this);
        }
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        this.dumpObjects(out, indent, new ClassLoaderDump(this.getClassLoader()), new DumpableCollection("eventListeners " + this, this._eventListeners), new DumpableCollection("handler attributes " + this, ((AttributesMap)this.getAttributes()).getAttributeEntrySet()), new DumpableCollection("context attributes " + this, this.getServletContext().getAttributeEntrySet()), new DumpableCollection("initparams " + this, this.getInitParams().entrySet()));
    }

    public Context getServletContext() {
        return this._scontext;
    }

    @ManagedAttribute(value="Checks if the /context is not redirected to /context/")
    public boolean getAllowNullPathInfo() {
        return this._allowNullPathInfo;
    }

    public void setAllowNullPathInfo(boolean allowNullPathInfo) {
        this._allowNullPathInfo = allowNullPathInfo;
    }

    @Override
    public void setServer(Server server) {
        super.setServer(server);
        if (this._errorHandler != null) {
            this._errorHandler.setServer(server);
        }
    }

    public boolean isUsingSecurityManager() {
        return this._usingSecurityManager;
    }

    public void setUsingSecurityManager(boolean usingSecurityManager) {
        if (usingSecurityManager && System.getSecurityManager() == null) {
            throw new IllegalStateException("No security manager");
        }
        this._usingSecurityManager = usingSecurityManager;
    }

    public void setVirtualHosts(String[] vhosts) {
        if (vhosts == null) {
            this._vhosts = vhosts;
        } else {
            boolean hostMatch = false;
            boolean connectorHostMatch = false;
            this._vhosts = new String[vhosts.length];
            this._vconnectors = new String[vhosts.length];
            this._vhostswildcard = new boolean[vhosts.length];
            ArrayList<Integer> connectorOnlyIndexes = null;
            for (int i = 0; i < vhosts.length; ++i) {
                boolean connectorMatch = false;
                this._vhosts[i] = vhosts[i];
                if (vhosts[i] == null) continue;
                int connectorIndex = this._vhosts[i].indexOf(64);
                if (connectorIndex >= 0) {
                    connectorMatch = true;
                    this._vconnectors[i] = this._vhosts[i].substring(connectorIndex + 1);
                    this._vhosts[i] = this._vhosts[i].substring(0, connectorIndex);
                    if (connectorIndex == 0) {
                        if (connectorOnlyIndexes == null) {
                            connectorOnlyIndexes = new ArrayList<Integer>();
                        }
                        connectorOnlyIndexes.add(i);
                    }
                }
                if (this._vhosts[i].startsWith("*.")) {
                    this._vhosts[i] = this._vhosts[i].substring(1);
                    this._vhostswildcard[i] = true;
                }
                if (this._vhosts[i].isEmpty()) {
                    this._vhosts[i] = null;
                } else {
                    hostMatch = true;
                    connectorHostMatch = connectorHostMatch || connectorMatch;
                }
                this._vhosts[i] = this.normalizeHostname(this._vhosts[i]);
            }
            if (connectorOnlyIndexes != null && hostMatch && !connectorHostMatch) {
                LOG.warn("ContextHandler {} has a connector only entry e.g. \"@connector\" and one or more host only entries. \nThe host entries will be ignored to match legacy behavior.  To clear this warning remove the host entries or update to use at least one host@connector syntax entry that will match a host for an specific connector", Arrays.asList(vhosts));
                String[] filteredHosts = new String[connectorOnlyIndexes.size()];
                for (int i = 0; i < connectorOnlyIndexes.size(); ++i) {
                    filteredHosts[i] = vhosts[(Integer)connectorOnlyIndexes.get(i)];
                }
                this.setVirtualHosts(filteredHosts);
            }
        }
    }

    public void addVirtualHosts(String[] virtualHosts) {
        if (virtualHosts == null || virtualHosts.length == 0) {
            return;
        }
        if (this._vhosts == null) {
            this.setVirtualHosts(virtualHosts);
        } else {
            HashSet<String> currentVirtualHosts = new HashSet<String>(Arrays.asList(this.getVirtualHosts()));
            for (String vh : virtualHosts) {
                currentVirtualHosts.add(this.normalizeHostname(vh));
            }
            this.setVirtualHosts(currentVirtualHosts.toArray(new String[0]));
        }
    }

    public void removeVirtualHosts(String[] virtualHosts) {
        if (virtualHosts == null || virtualHosts.length == 0 || this._vhosts == null || this._vhosts.length == 0) {
            return;
        }
        HashSet<String> existingVirtualHosts = new HashSet<String>(Arrays.asList(this.getVirtualHosts()));
        for (String vh : virtualHosts) {
            existingVirtualHosts.remove(this.normalizeHostname(vh));
        }
        if (existingVirtualHosts.isEmpty()) {
            this.setVirtualHosts(null);
        } else {
            this.setVirtualHosts(existingVirtualHosts.toArray(new String[0]));
        }
    }

    @ManagedAttribute(value="Virtual hosts accepted by the context", readonly=true)
    public String[] getVirtualHosts() {
        if (this._vhosts == null) {
            return null;
        }
        String[] vhosts = new String[this._vhosts.length];
        for (int i = 0; i < this._vhosts.length; ++i) {
            StringBuilder sb = new StringBuilder();
            if (this._vhostswildcard[i]) {
                sb.append("*");
            }
            if (this._vhosts[i] != null) {
                sb.append(this._vhosts[i]);
            }
            if (this._vconnectors[i] != null) {
                sb.append("@").append(this._vconnectors[i]);
            }
            vhosts[i] = sb.toString();
        }
        return vhosts;
    }

    @Override
    public Object getAttribute(String name) {
        return this._attributes.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return AttributesMap.getAttributeNamesCopy(this._attributes);
    }

    @Override
    public Set<String> getAttributeNameSet() {
        return this._attributes.getAttributeNameSet();
    }

    public Attributes getAttributes() {
        return this._attributes;
    }

    public ClassLoader getClassLoader() {
        return this._classLoader;
    }

    @ManagedAttribute(value="The file classpath")
    public String getClassPath() {
        if (this._classLoader == null || !(this._classLoader instanceof URLClassLoader)) {
            return null;
        }
        URLClassLoader loader = (URLClassLoader)this._classLoader;
        URL[] urls = loader.getURLs();
        StringBuilder classpath = new StringBuilder();
        for (int i = 0; i < urls.length; ++i) {
            try {
                Resource resource = this.newResource(urls[i]);
                File file = resource.getFile();
                if (file == null || !file.exists()) continue;
                if (classpath.length() > 0) {
                    classpath.append(File.pathSeparatorChar);
                }
                classpath.append(file.getAbsolutePath());
                continue;
            }
            catch (IOException e) {
                LOG.debug(e);
            }
        }
        if (classpath.length() == 0) {
            return null;
        }
        return classpath.toString();
    }

    @ManagedAttribute(value="True if URLs are compacted to replace the multiple '/'s with a single '/'")
    public String getContextPath() {
        return this._contextPath;
    }

    public String getContextPathEncoded() {
        return this._contextPathEncoded;
    }

    public String getRequestContextPath() {
        String contextPathEncoded = this.getContextPathEncoded();
        return "/".equals(contextPathEncoded) ? "" : contextPathEncoded;
    }

    public String getInitParameter(String name) {
        return this._initParams.get(name);
    }

    public String setInitParameter(String name, String value) {
        return this._initParams.put(name, value);
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this._initParams.keySet());
    }

    @ManagedAttribute(value="Initial Parameter map for the context")
    public Map<String, String> getInitParams() {
        return this._initParams;
    }

    @ManagedAttribute(value="Display name of the Context", readonly=true)
    public String getDisplayName() {
        return this._displayName;
    }

    public EventListener[] getEventListeners() {
        return this._eventListeners.toArray(new EventListener[0]);
    }

    public void setEventListeners(EventListener[] eventListeners) {
        this._contextListeners.clear();
        this._servletContextListeners.clear();
        this._servletContextAttributeListeners.clear();
        this._servletRequestListeners.clear();
        this._servletRequestAttributeListeners.clear();
        this._eventListeners.clear();
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                this.addEventListener(listener);
            }
        }
    }

    public void addEventListener(EventListener listener) {
        this._eventListeners.add(listener);
        if (!this.isStarted() && !this.isStarting()) {
            this._durableListeners.add(listener);
        }
        if (listener instanceof ContextScopeListener) {
            this._contextListeners.add((ContextScopeListener)listener);
            if (__context.get() != null) {
                ((ContextScopeListener)listener).enterScope(__context.get(), null, "Listener registered");
            }
        }
        if (listener instanceof ServletContextListener) {
            this._servletContextListeners.add((ServletContextListener)listener);
        }
        if (listener instanceof ServletContextAttributeListener) {
            this._servletContextAttributeListeners.add((ServletContextAttributeListener)listener);
        }
        if (listener instanceof ServletRequestListener) {
            this._servletRequestListeners.add((ServletRequestListener)listener);
        }
        if (listener instanceof ServletRequestAttributeListener) {
            this._servletRequestAttributeListeners.add((ServletRequestAttributeListener)listener);
        }
    }

    public void removeEventListener(EventListener listener) {
        this._eventListeners.remove(listener);
        if (listener instanceof ContextScopeListener) {
            this._contextListeners.remove(listener);
        }
        if (listener instanceof ServletContextListener) {
            this._servletContextListeners.remove(listener);
            this._destroyServletContextListeners.remove(listener);
        }
        if (listener instanceof ServletContextAttributeListener) {
            this._servletContextAttributeListeners.remove(listener);
        }
        if (listener instanceof ServletRequestListener) {
            this._servletRequestListeners.remove(listener);
        }
        if (listener instanceof ServletRequestAttributeListener) {
            this._servletRequestAttributeListeners.remove(listener);
        }
    }

    protected void addProgrammaticListener(EventListener listener) {
        this._programmaticListeners.add(listener);
    }

    public boolean isProgrammaticListener(EventListener listener) {
        return this._programmaticListeners.contains(listener);
    }

    public boolean isDurableListener(EventListener listener) {
        return this._durableListeners.contains(listener);
    }

    @Override
    @ManagedAttribute(value="true for graceful shutdown, which allows existing requests to complete")
    public boolean isShutdown() {
        return this._availability.get() == Availability.SHUTDOWN;
    }

    @Override
    public Future<Void> shutdown() {
        block4: while (true) {
            Availability availability = this._availability.get();
            switch (availability) {
                case STOPPED: {
                    return new FutureCallback(new IllegalStateException(this.getState()));
                }
                case STARTING: 
                case AVAILABLE: 
                case UNAVAILABLE: {
                    if (this._availability.compareAndSet(availability, Availability.SHUTDOWN)) break block4;
                    continue block4;
                }
            }
            break;
        }
        return new FutureCallback(true);
    }

    public boolean isAvailable() {
        return this._availability.get() == Availability.AVAILABLE;
    }

    public void setAvailable(boolean available) {
        block11: {
            if (available) {
                block7: while (true) {
                    Availability availability = this._availability.get();
                    switch (availability) {
                        case AVAILABLE: {
                            break block11;
                        }
                        case UNAVAILABLE: {
                            if (!this._availability.compareAndSet(availability, Availability.AVAILABLE)) {
                                continue block7;
                            }
                            break block11;
                        }
                        default: {
                            throw new IllegalStateException(availability.toString());
                        }
                    }
                    break;
                }
            }
            block8: while (true) {
                Availability availability = this._availability.get();
                switch (availability) {
                    case STARTING: 
                    case AVAILABLE: {
                        if (this._availability.compareAndSet(availability, Availability.UNAVAILABLE)) break block8;
                        continue block8;
                    }
                }
                break;
            }
        }
    }

    public Logger getLogger() {
        return this._logger;
    }

    public void setLogger(Logger logger) {
        this._logger = logger;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStart() throws Exception {
        this._availability.set(Availability.STARTING);
        if (this._contextPath == null) {
            throw new IllegalStateException("Null contextPath");
        }
        if (this._logger == null) {
            this._logger = Log.getLogger(ContextHandler.class.getName() + this.getLogNameSuffix());
        }
        ClassLoader oldClassloader = null;
        Thread currentThread = null;
        Context oldContext = null;
        this._attributes.setAttribute("org.eclipse.jetty.server.Executor", this.getServer().getThreadPool());
        if (this._mimeTypes == null) {
            this._mimeTypes = new MimeTypes();
        }
        try {
            if (this._classLoader != null) {
                currentThread = Thread.currentThread();
                oldClassloader = currentThread.getContextClassLoader();
                currentThread.setContextClassLoader(this._classLoader);
            }
            oldContext = __context.get();
            __context.set(this._scontext);
            this.enterScope(null, this.getState());
            this.startContext();
            this.contextInitialized();
            this._availability.compareAndSet(Availability.STARTING, Availability.AVAILABLE);
            LOG.info("Started {}", this);
        }
        finally {
            this._availability.compareAndSet(Availability.STARTING, Availability.UNAVAILABLE);
            this.exitScope(null);
            __context.set(oldContext);
            if (this._classLoader != null && currentThread != null) {
                currentThread.setContextClassLoader(oldClassloader);
            }
        }
    }

    private String getLogNameSuffix() {
        String logName = this.getDisplayName();
        if (StringUtil.isBlank(logName)) {
            logName = this.getContextPath();
            if (logName != null && logName.startsWith("/")) {
                logName = logName.substring(1);
            }
            if (StringUtil.isBlank(logName)) {
                logName = "ROOT";
            }
        }
        return '.' + logName.replaceAll("\\W", "_");
    }

    protected void startContext() throws Exception {
        String managedAttributes = this._initParams.get(MANAGED_ATTRIBUTES);
        if (managedAttributes != null) {
            this.addEventListener(new ManagedAttributeListener(this, StringUtil.csvSplit(managedAttributes)));
        }
        super.doStart();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void contextInitialized() throws Exception {
        switch (this._contextStatus) {
            case NOTSET: {
                try {
                    this._destroyServletContextListeners.clear();
                    if (this._servletContextListeners.isEmpty()) break;
                    ServletContextEvent event = new ServletContextEvent(this._scontext);
                    for (ServletContextListener listener : this._servletContextListeners) {
                        this.callContextInitialized(listener, event);
                        this._destroyServletContextListeners.add(listener);
                    }
                    break;
                }
                finally {
                    this._contextStatus = ContextStatus.INITIALIZED;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void contextDestroyed() throws Exception {
        switch (this._contextStatus) {
            case INITIALIZED: {
                try {
                    MultiException ex = new MultiException();
                    ServletContextEvent event = new ServletContextEvent(this._scontext);
                    Collections.reverse(this._destroyServletContextListeners);
                    for (ServletContextListener listener : this._destroyServletContextListeners) {
                        try {
                            this.callContextDestroyed(listener, event);
                        }
                        catch (Exception x) {
                            ex.add(x);
                        }
                    }
                    ex.ifExceptionThrow();
                    break;
                }
                finally {
                    this._contextStatus = ContextStatus.DESTROYED;
                }
            }
        }
    }

    protected void stopContext() throws Exception {
        super.doStop();
    }

    protected void callContextInitialized(ServletContextListener l, ServletContextEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("contextInitialized: {}->{}", e, l);
        }
        l.contextInitialized(e);
    }

    protected void callContextDestroyed(ServletContextListener l, ServletContextEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("contextDestroyed: {}->{}", e, l);
        }
        l.contextDestroyed(e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        MultiException mex = null;
        if (this.getStopTimeout() > 0L) {
            try {
                this.doShutdown(null);
            }
            catch (MultiException e) {
                mex = e;
            }
        }
        this._availability.set(Availability.STOPPED);
        ClassLoader oldClassloader = null;
        ClassLoader oldWebapploader = null;
        Thread currentThread = null;
        Context oldContext = __context.get();
        this.enterScope(null, "doStop");
        __context.set(this._scontext);
        try {
            if (this._classLoader != null) {
                oldWebapploader = this._classLoader;
                currentThread = Thread.currentThread();
                oldClassloader = currentThread.getContextClassLoader();
                currentThread.setContextClassLoader(this._classLoader);
            }
            this.stopContext();
            this.contextDestroyed();
            this.setEventListeners(this._durableListeners.toArray(new EventListener[0]));
            this._durableListeners.clear();
            if (this._errorHandler != null) {
                this._errorHandler.stop();
            }
            for (EventListener l : this._programmaticListeners) {
                this.removeEventListener(l);
                if (!(l instanceof ContextScopeListener)) continue;
                try {
                    ((ContextScopeListener)l).exitScope(this._scontext, null);
                }
                catch (Throwable e) {
                    LOG.warn(e);
                }
            }
            this._programmaticListeners.clear();
            this._contextStatus = ContextStatus.NOTSET;
            __context.set(oldContext);
            this.exitScope(null);
        }
        catch (Throwable x) {
            try {
                if (mex == null) {
                    mex = new MultiException();
                }
                mex.add(x);
                this._contextStatus = ContextStatus.NOTSET;
                __context.set(oldContext);
                this.exitScope(null);
            }
            catch (Throwable throwable) {
                this._contextStatus = ContextStatus.NOTSET;
                __context.set(oldContext);
                this.exitScope(null);
                LOG.info("Stopped {}", this);
                if ((oldClassloader == null || oldClassloader != oldWebapploader) && currentThread != null) {
                    currentThread.setContextClassLoader(oldClassloader);
                }
                this._scontext.clearAttributes();
                throw throwable;
            }
            LOG.info("Stopped {}", this);
            if ((oldClassloader == null || oldClassloader != oldWebapploader) && currentThread != null) {
                currentThread.setContextClassLoader(oldClassloader);
            }
            this._scontext.clearAttributes();
        }
        LOG.info("Stopped {}", this);
        if ((oldClassloader == null || oldClassloader != oldWebapploader) && currentThread != null) {
            currentThread.setContextClassLoader(oldClassloader);
        }
        this._scontext.clearAttributes();
        if (mex != null) {
            mex.ifExceptionThrow();
        }
    }

    public boolean checkVirtualHost(Request baseRequest) {
        if (this._vhosts == null || this._vhosts.length == 0) {
            return true;
        }
        String vhost = this.normalizeHostname(baseRequest.getServerName());
        String connectorName = baseRequest.getHttpChannel().getConnector().getName();
        for (int i = 0; i < this._vhosts.length; ++i) {
            int index;
            String contextVhost = this._vhosts[i];
            String contextVConnector = this._vconnectors[i];
            if (contextVConnector != null) {
                if (!contextVConnector.equalsIgnoreCase(connectorName)) continue;
                if (contextVhost == null) {
                    return true;
                }
            }
            if (contextVhost == null || !(this._vhostswildcard[i] ? (index = vhost.indexOf(".")) >= 0 && vhost.substring(index).equalsIgnoreCase(contextVhost) : vhost.equalsIgnoreCase(contextVhost))) continue;
            return true;
        }
        return false;
    }

    public boolean checkContextPath(String uri) {
        if (this._contextPath.length() > 1) {
            if (!uri.startsWith(this._contextPath)) {
                return false;
            }
            return uri.length() <= this._contextPath.length() || uri.charAt(this._contextPath.length()) == '/';
        }
        return true;
    }

    public boolean checkContext(String target, Request baseRequest, HttpServletResponse response) throws IOException {
        DispatcherType dispatch = baseRequest.getDispatcherType();
        if (!this.checkVirtualHost(baseRequest)) {
            return false;
        }
        if (!this.checkContextPath(target)) {
            return false;
        }
        if (!this._allowNullPathInfo && this._contextPath.length() == target.length() && this._contextPath.length() > 1) {
            baseRequest.setHandled(true);
            String queryString = baseRequest.getQueryString();
            baseRequest.getResponse().sendRedirect(302, baseRequest.getRequestURI() + (queryString == null ? "/" : "/?" + queryString), true);
            return false;
        }
        switch (this._availability.get()) {
            case STOPPED: {
                return false;
            }
            case UNAVAILABLE: 
            case SHUTDOWN: {
                baseRequest.setHandled(true);
                response.sendError(503);
                return false;
            }
        }
        return !DispatcherType.REQUEST.equals((Object)dispatch) || !baseRequest.isHandled();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("scope {}|{}|{} @ {}", baseRequest.getContextPath(), baseRequest.getServletPath(), baseRequest.getPathInfo(), this);
        }
        String oldContextPath = null;
        String oldServletPath = null;
        String oldPathInfo = null;
        ClassLoader oldClassloader = null;
        Thread currentThread = null;
        String pathInfo = target;
        DispatcherType dispatch = baseRequest.getDispatcherType();
        Context oldContext = baseRequest.getContext();
        if (oldContext != this._scontext) {
            if (DispatcherType.REQUEST.equals((Object)dispatch) || DispatcherType.ASYNC.equals((Object)dispatch)) {
                if (this.isCompactPath()) {
                    target = URIUtil.compactPath(target);
                }
                if (!this.checkContext(target, baseRequest, response)) {
                    return;
                }
                if (target.length() > this._contextPath.length()) {
                    if (this._contextPath.length() > 1) {
                        target = target.substring(this._contextPath.length());
                    }
                    pathInfo = target;
                } else if (this._contextPath.length() == 1) {
                    target = "/";
                    pathInfo = "/";
                } else {
                    target = "/";
                    pathInfo = null;
                }
            }
            if (this._classLoader != null) {
                currentThread = Thread.currentThread();
                oldClassloader = currentThread.getContextClassLoader();
                currentThread.setContextClassLoader(this._classLoader);
            }
        }
        try {
            oldContextPath = baseRequest.getContextPath();
            oldServletPath = baseRequest.getServletPath();
            oldPathInfo = baseRequest.getPathInfo();
            baseRequest.setContext(this._scontext);
            __context.set(this._scontext);
            if (!DispatcherType.INCLUDE.equals((Object)dispatch) && target.startsWith("/")) {
                baseRequest.setContextPath(this.getRequestContextPath());
                baseRequest.setServletPath(null);
                baseRequest.setPathInfo(pathInfo);
            }
            if (oldContext != this._scontext) {
                this.enterScope(baseRequest, (Object)dispatch);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("context={}|{}|{} @ {}", baseRequest.getContextPath(), baseRequest.getServletPath(), baseRequest.getPathInfo(), this);
            }
            this.nextScope(target, baseRequest, request, response);
        }
        finally {
            if (oldContext != this._scontext) {
                this.exitScope(baseRequest);
                if (this._classLoader != null && currentThread != null) {
                    currentThread.setContextClassLoader(oldClassloader);
                }
                baseRequest.setContext(oldContext);
                __context.set(oldContext);
                baseRequest.setContextPath(oldContextPath);
                baseRequest.setServletPath(oldServletPath);
                baseRequest.setPathInfo(oldPathInfo);
            }
        }
    }

    protected void requestInitialized(Request baseRequest, HttpServletRequest request) {
        if (!this._servletRequestAttributeListeners.isEmpty()) {
            for (ServletRequestAttributeListener l : this._servletRequestAttributeListeners) {
                baseRequest.addEventListener(l);
            }
        }
        if (!this._servletRequestListeners.isEmpty()) {
            ServletRequestEvent sre = new ServletRequestEvent(this._scontext, request);
            for (ServletRequestListener l : this._servletRequestListeners) {
                l.requestInitialized(sre);
            }
        }
    }

    protected void requestDestroyed(Request baseRequest, HttpServletRequest request) {
        if (!this._servletRequestListeners.isEmpty()) {
            ServletRequestEvent sre = new ServletRequestEvent(this._scontext, request);
            int i = this._servletRequestListeners.size();
            while (i-- > 0) {
                this._servletRequestListeners.get(i).requestDestroyed(sre);
            }
        }
        if (!this._servletRequestAttributeListeners.isEmpty()) {
            int i = this._servletRequestAttributeListeners.size();
            while (i-- > 0) {
                baseRequest.removeEventListener(this._servletRequestAttributeListeners.get(i));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DispatcherType dispatch = baseRequest.getDispatcherType();
        boolean new_context = baseRequest.takeNewContext();
        try {
            if (new_context) {
                this.requestInitialized(baseRequest, request);
            }
            if (dispatch == DispatcherType.REQUEST && this.isProtectedTarget(target)) {
                baseRequest.setHandled(true);
                response.sendError(404);
                return;
            }
            this.nextHandle(target, baseRequest, request, response);
        }
        finally {
            if (new_context) {
                this.requestDestroyed(baseRequest, request);
            }
        }
    }

    protected void enterScope(Request request, Object reason) {
        if (!this._contextListeners.isEmpty()) {
            for (ContextScopeListener listener : this._contextListeners) {
                try {
                    listener.enterScope(this._scontext, request, reason);
                }
                catch (Throwable e) {
                    LOG.warn(e);
                }
            }
        }
    }

    protected void exitScope(Request request) {
        if (!this._contextListeners.isEmpty()) {
            int i = this._contextListeners.size();
            while (i-- > 0) {
                try {
                    this._contextListeners.get(i).exitScope(this._scontext, request);
                }
                catch (Throwable e) {
                    LOG.warn(e);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handle(Request request, Runnable runnable) {
        ClassLoader oldClassloader = null;
        Thread currentThread = null;
        Context oldContext = __context.get();
        if (oldContext == this._scontext) {
            runnable.run();
            return;
        }
        try {
            __context.set(this._scontext);
            if (this._classLoader != null) {
                currentThread = Thread.currentThread();
                oldClassloader = currentThread.getContextClassLoader();
                currentThread.setContextClassLoader(this._classLoader);
            }
            this.enterScope(request, runnable);
            runnable.run();
        }
        finally {
            this.exitScope(request);
            __context.set(oldContext);
            if (oldClassloader != null) {
                currentThread.setContextClassLoader(oldClassloader);
            }
        }
    }

    public void handle(Runnable runnable) {
        this.handle(null, runnable);
    }

    public boolean isProtectedTarget(String target) {
        if (target == null || this._protectedTargets == null) {
            return false;
        }
        while (target.startsWith("//")) {
            target = URIUtil.compactPath(target);
        }
        for (int i = 0; i < this._protectedTargets.length; ++i) {
            String t = this._protectedTargets[i];
            if (!StringUtil.startsWithIgnoreCase(target, t)) continue;
            if (target.length() == t.length()) {
                return true;
            }
            char c = target.charAt(t.length());
            if (c != '/' && c != '?' && c != '#' && c != ';') continue;
            return true;
        }
        return false;
    }

    public void setProtectedTargets(String[] targets) {
        if (targets == null) {
            this._protectedTargets = null;
            return;
        }
        this._protectedTargets = Arrays.copyOf(targets, targets.length);
    }

    public String[] getProtectedTargets() {
        if (this._protectedTargets == null) {
            return null;
        }
        return Arrays.copyOf(this._protectedTargets, this._protectedTargets.length);
    }

    @Override
    public void removeAttribute(String name) {
        this._attributes.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        this._attributes.setAttribute(name, value);
    }

    public void setAttributes(Attributes attributes) {
        this._attributes.clearAttributes();
        this._attributes.addAll(attributes);
    }

    @Override
    public void clearAttributes() {
        this._attributes.clearAttributes();
    }

    @Deprecated
    public void setManagedAttribute(String name, Object value) {
    }

    public void setClassLoader(ClassLoader classLoader) {
        if (this.isStarted()) {
            throw new IllegalStateException(this.getState());
        }
        this._classLoader = classLoader;
    }

    public void setContextPath(String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("null contextPath");
        }
        if (contextPath.endsWith("/*")) {
            LOG.warn(this + " contextPath ends with /*", new Object[0]);
            contextPath = contextPath.substring(0, contextPath.length() - 2);
        } else if (contextPath.length() > 1 && contextPath.endsWith("/")) {
            LOG.warn(this + " contextPath ends with /", new Object[0]);
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        if (contextPath.length() == 0) {
            LOG.warn("Empty contextPath", new Object[0]);
            contextPath = "/";
        }
        this._contextPath = contextPath;
        this._contextPathEncoded = URIUtil.encodePath(contextPath);
        if (this.getServer() != null && (this.getServer().isStarting() || this.getServer().isStarted())) {
            Class<ContextHandlerCollection> handlerClass = ContextHandlerCollection.class;
            Handler[] contextCollections = this.getServer().getChildHandlersByClass(handlerClass);
            if (contextCollections != null) {
                for (Handler contextCollection : contextCollections) {
                    ((ContextHandlerCollection)handlerClass.cast(contextCollection)).mapContexts();
                }
            }
        }
    }

    public void setDisplayName(String servletContextName) {
        this._displayName = servletContextName;
    }

    public Resource getBaseResource() {
        if (this._baseResource == null) {
            return null;
        }
        return this._baseResource;
    }

    @ManagedAttribute(value="document root for context")
    public String getResourceBase() {
        if (this._baseResource == null) {
            return null;
        }
        return this._baseResource.toString();
    }

    public void setBaseResource(Resource base) {
        this._baseResource = base;
    }

    public void setResourceBase(String resourceBase) {
        try {
            this.setBaseResource(this.newResource(resourceBase));
        }
        catch (Exception e) {
            LOG.warn(e.toString(), new Object[0]);
            LOG.debug(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }

    public MimeTypes getMimeTypes() {
        if (this._mimeTypes == null) {
            this._mimeTypes = new MimeTypes();
        }
        return this._mimeTypes;
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        this._mimeTypes = mimeTypes;
    }

    public void setWelcomeFiles(String[] files) {
        this._welcomeFiles = files;
    }

    @ManagedAttribute(value="Partial URIs of directory welcome files", readonly=true)
    public String[] getWelcomeFiles() {
        return this._welcomeFiles;
    }

    @ManagedAttribute(value="The error handler to use for the context")
    public ErrorHandler getErrorHandler() {
        return this._errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler != null) {
            errorHandler.setServer(this.getServer());
        }
        this.updateBean(this._errorHandler, errorHandler, true);
        this._errorHandler = errorHandler;
    }

    @ManagedAttribute(value="The maximum content size")
    public int getMaxFormContentSize() {
        return this._maxFormContentSize;
    }

    public void setMaxFormContentSize(int maxSize) {
        this._maxFormContentSize = maxSize;
    }

    public int getMaxFormKeys() {
        return this._maxFormKeys;
    }

    public void setMaxFormKeys(int max) {
        this._maxFormKeys = max;
    }

    public boolean isCompactPath() {
        return this._compactPath;
    }

    public void setCompactPath(boolean compactPath) {
        this._compactPath = compactPath;
    }

    @Override
    public String toString() {
        String p;
        String[] vhosts = this.getVirtualHosts();
        StringBuilder b = new StringBuilder();
        Package pkg = this.getClass().getPackage();
        if (pkg != null && (p = pkg.getName()) != null && p.length() > 0) {
            String[] ss;
            for (String s2 : ss = p.split("\\.")) {
                b.append(s2.charAt(0)).append('.');
            }
        }
        b.append(this.getClass().getSimpleName()).append('@').append(Integer.toString(this.hashCode(), 16));
        b.append('{');
        if (this.getDisplayName() != null) {
            b.append(this.getDisplayName()).append(',');
        }
        b.append(this.getContextPath()).append(',').append(this.getBaseResource()).append(',').append((Object)this._availability.get());
        if (vhosts != null && vhosts.length > 0) {
            b.append(',').append(vhosts[0]);
        }
        b.append('}');
        return b.toString();
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (className == null) {
            return null;
        }
        if (this._classLoader == null) {
            return Loader.loadClass(className);
        }
        return this._classLoader.loadClass(className);
    }

    public void addLocaleEncoding(String locale, String encoding) {
        if (this._localeEncodingMap == null) {
            this._localeEncodingMap = new HashMap<String, String>();
        }
        this._localeEncodingMap.put(locale, encoding);
    }

    public String getLocaleEncoding(String locale) {
        if (this._localeEncodingMap == null) {
            return null;
        }
        String encoding = this._localeEncodingMap.get(locale);
        return encoding;
    }

    public String getLocaleEncoding(Locale locale) {
        if (this._localeEncodingMap == null) {
            return null;
        }
        String encoding = this._localeEncodingMap.get(locale.toString());
        if (encoding == null) {
            encoding = this._localeEncodingMap.get(locale.getLanguage());
        }
        return encoding;
    }

    public Map<String, String> getLocaleEncodings() {
        if (this._localeEncodingMap == null) {
            return null;
        }
        return Collections.unmodifiableMap(this._localeEncodingMap);
    }

    public Resource getResource(String path) throws MalformedURLException {
        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException(path);
        }
        if (this._baseResource == null) {
            return null;
        }
        try {
            path = URIUtil.canonicalPath(path);
            Resource resource = this._baseResource.addPath(path);
            if (this.checkAlias(path, resource)) {
                return resource;
            }
            return null;
        }
        catch (Exception e) {
            LOG.ignore(e);
            return null;
        }
    }

    public boolean checkAlias(String path, Resource resource) {
        if (resource.isAlias()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Aliased resource: " + resource + "~=" + resource.getAlias(), new Object[0]);
            }
            for (AliasCheck check : this.getAliasChecks()) {
                if (!check.check(path, resource)) continue;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Aliased resource: " + resource + " approved by " + check, new Object[0]);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    public Resource newResource(URL url) throws IOException {
        return Resource.newResource(url);
    }

    public Resource newResource(URI uri) throws IOException {
        return Resource.newResource(uri);
    }

    public Resource newResource(String urlOrPath) throws IOException {
        return Resource.newResource(urlOrPath);
    }

    public Set<String> getResourcePaths(String path) {
        try {
            path = URIUtil.canonicalPath(path);
            Resource resource = this.getResource(path);
            if (resource != null && resource.exists()) {
                String[] l;
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                if ((l = resource.list()) != null) {
                    HashSet<String> set = new HashSet<String>();
                    for (int i = 0; i < l.length; ++i) {
                        set.add(path + l[i]);
                    }
                    return set;
                }
            }
        }
        catch (Exception e) {
            LOG.ignore(e);
        }
        return Collections.emptySet();
    }

    private String normalizeHostname(String host) {
        if (host == null) {
            return null;
        }
        int connectorIndex = host.indexOf(64);
        String connector = null;
        if (connectorIndex > 0) {
            host = host.substring(0, connectorIndex);
            connector = host.substring(connectorIndex);
        }
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        if (connector != null) {
            host = host + connector;
        }
        return host;
    }

    public void addAliasCheck(AliasCheck check) {
        this.getAliasChecks().add(check);
    }

    public List<AliasCheck> getAliasChecks() {
        return this._aliasChecks;
    }

    public void setAliasChecks(List<AliasCheck> checks) {
        this.getAliasChecks().clear();
        this.getAliasChecks().addAll(checks);
    }

    public void clearAliasChecks() {
        this.getAliasChecks().clear();
    }

    private static class Caller
    extends SecurityManager {
        private Caller() {
        }

        public ClassLoader getCallerClassLoader(int depth) {
            if (depth < 0) {
                return null;
            }
            Class<?>[] classContext = this.getClassContext();
            if (classContext.length <= depth) {
                return null;
            }
            return classContext[depth].getClassLoader();
        }
    }

    public static interface ContextScopeListener
    extends EventListener {
        public void enterScope(Context var1, Request var2, Object var3);

        public void exitScope(Context var1, Request var2);
    }

    public static class ApproveNonExistentDirectoryAliases
    implements AliasCheck {
        @Override
        public boolean check(String path, Resource resource) {
            if (resource.exists()) {
                return false;
            }
            String a = resource.getAlias().toString();
            String r = resource.getURI().toString();
            if (a.length() > r.length()) {
                return a.startsWith(r) && a.length() == r.length() + 1 && a.endsWith("/");
            }
            if (a.length() < r.length()) {
                return r.startsWith(a) && r.length() == a.length() + 1 && r.endsWith("/");
            }
            return a.equals(r);
        }
    }

    public static class ApproveAliases
    implements AliasCheck {
        @Override
        public boolean check(String path, Resource resource) {
            return true;
        }
    }

    public static interface AliasCheck {
        public boolean check(String var1, Resource var2);
    }

    public static class StaticContext
    extends AttributesMap
    implements ServletContext {
        private int _effectiveMajorVersion = 3;
        private int _effectiveMinorVersion = 1;

        @Override
        public ServletContext getContext(String uripath) {
            return null;
        }

        @Override
        public int getMajorVersion() {
            return 3;
        }

        @Override
        public String getMimeType(String file) {
            return null;
        }

        @Override
        public int getMinorVersion() {
            return 1;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String uriInContext) {
            return null;
        }

        @Override
        public String getRealPath(String path) {
            return null;
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            return null;
        }

        @Override
        public Set<String> getResourcePaths(String path) {
            return null;
        }

        @Override
        public String getServerInfo() {
            return ContextHandler.getServerInfo();
        }

        @Override
        @Deprecated
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        @Override
        @Deprecated
        public Enumeration<String> getServletNames() {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        @Override
        @Deprecated
        public Enumeration<Servlet> getServlets() {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        @Override
        public void log(Exception exception, String msg) {
            LOG.warn(msg, exception);
        }

        @Override
        public void log(String msg) {
            LOG.info(msg, new Object[0]);
        }

        @Override
        public void log(String message, Throwable throwable) {
            LOG.warn(message, throwable);
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        @Override
        public String getServletContextName() {
            return "No Context";
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addFilter(String, Class)");
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addFilter(String, Filter)");
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, String className) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addFilter(String, String)");
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addServlet(String, Class)");
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addServlet(String, Servlet)");
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addServlet(String, String)");
            return null;
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getDefaultSessionTrackingModes()");
            return null;
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getEffectiveSessionTrackingModes()");
            return null;
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getFilterRegistration(String)");
            return null;
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getFilterRegistrations()");
            return null;
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getServletRegistration(String)");
            return null;
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getServletRegistrations()");
            return null;
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getSessionCookieConfig()");
            return null;
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "setSessionTrackingModes(Set<SessionTrackingMode>)");
        }

        @Override
        public void addListener(String className) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addListener(String)");
        }

        @Override
        public <T extends EventListener> void addListener(T t) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addListener(T)");
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "addListener(Class)");
        }

        protected <T> T createInstance(Class<T> clazz) throws ServletException {
            try {
                return clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (Exception e) {
                throw new ServletException(e);
            }
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
            return (T)((EventListener)this.createInstance(clazz));
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
            return (T)((Servlet)this.createInstance(clazz));
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
            return (T)((Filter)this.createInstance(clazz));
        }

        @Override
        public ClassLoader getClassLoader() {
            return ContextHandler.class.getClassLoader();
        }

        @Override
        public int getEffectiveMajorVersion() {
            return this._effectiveMajorVersion;
        }

        @Override
        public int getEffectiveMinorVersion() {
            return this._effectiveMinorVersion;
        }

        public void setEffectiveMajorVersion(int v) {
            this._effectiveMajorVersion = v;
        }

        public void setEffectiveMinorVersion(int v) {
            this._effectiveMinorVersion = v;
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getJspConfigDescriptor()");
            return null;
        }

        @Override
        public void declareRoles(String ... roleNames) {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "declareRoles(String...)");
        }

        @Override
        public String getVirtualServerName() {
            return null;
        }
    }

    public class Context
    extends StaticContext {
        protected boolean _enabled = true;
        protected boolean _extendedListenerTypes = false;

        protected Context() {
        }

        public ContextHandler getContextHandler() {
            return ContextHandler.this;
        }

        @Override
        public ServletContext getContext(String uripath) {
            ContextHandler ch;
            String contextPath;
            ArrayList<ContextHandler> contexts = new ArrayList<ContextHandler>();
            Handler[] handlers = ContextHandler.this.getServer().getChildHandlersByClass(ContextHandler.class);
            String matchedPath = null;
            for (Handler handler : handlers) {
                if (handler == null || !uripath.equals(contextPath = (ch = (ContextHandler)handler).getContextPath()) && (!uripath.startsWith(contextPath) || uripath.charAt(contextPath.length()) != '/') && !"/".equals(contextPath)) continue;
                if (ContextHandler.this.getVirtualHosts() != null && ContextHandler.this.getVirtualHosts().length > 0) {
                    if (ch.getVirtualHosts() == null || ch.getVirtualHosts().length <= 0) continue;
                    for (String h1 : ContextHandler.this.getVirtualHosts()) {
                        for (String h2 : ch.getVirtualHosts()) {
                            if (!h1.equals(h2)) continue;
                            if (matchedPath == null || contextPath.length() > matchedPath.length()) {
                                contexts.clear();
                                matchedPath = contextPath;
                            }
                            if (!matchedPath.equals(contextPath)) continue;
                            contexts.add(ch);
                        }
                    }
                    continue;
                }
                if (matchedPath == null || contextPath.length() > matchedPath.length()) {
                    contexts.clear();
                    matchedPath = contextPath;
                }
                if (!matchedPath.equals(contextPath)) continue;
                contexts.add(ch);
            }
            if (contexts.size() > 0) {
                return ((ContextHandler)contexts.get((int)0))._scontext;
            }
            matchedPath = null;
            for (Handler handler : handlers) {
                if (handler == null || !uripath.equals(contextPath = (ch = (ContextHandler)handler).getContextPath()) && (!uripath.startsWith(contextPath) || uripath.charAt(contextPath.length()) != '/') && !"/".equals(contextPath)) continue;
                if (matchedPath == null || contextPath.length() > matchedPath.length()) {
                    contexts.clear();
                    matchedPath = contextPath;
                }
                if (!matchedPath.equals(contextPath)) continue;
                contexts.add(ch);
            }
            if (contexts.size() > 0) {
                return ((ContextHandler)contexts.get((int)0))._scontext;
            }
            return null;
        }

        @Override
        public String getMimeType(String file) {
            if (ContextHandler.this._mimeTypes == null) {
                return null;
            }
            return ContextHandler.this._mimeTypes.getMimeByExtension(file);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String uriInContext) {
            if (uriInContext == null) {
                return null;
            }
            if (!uriInContext.startsWith("/")) {
                return null;
            }
            try {
                HttpURI uri = new HttpURI(null, null, 0, uriInContext);
                String pathInfo = URIUtil.canonicalPath(uri.getDecodedPath());
                if (pathInfo == null) {
                    return null;
                }
                String contextPath = this.getContextPath();
                if (contextPath != null && contextPath.length() > 0) {
                    uri.setPath(URIUtil.addPaths(contextPath, uri.getPath()));
                }
                return new Dispatcher(ContextHandler.this, uri, pathInfo);
            }
            catch (Exception e) {
                LOG.ignore(e);
                return null;
            }
        }

        @Override
        public String getRealPath(String path) {
            if (path == null) {
                return null;
            }
            if (path.length() == 0) {
                path = "/";
            } else if (path.charAt(0) != '/') {
                path = "/" + path;
            }
            try {
                File file;
                Resource resource = ContextHandler.this.getResource(path);
                if (resource != null && (file = resource.getFile()) != null) {
                    return file.getCanonicalPath();
                }
            }
            catch (Exception e) {
                LOG.ignore(e);
            }
            return null;
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            Resource resource = ContextHandler.this.getResource(path);
            if (resource != null && resource.exists()) {
                return resource.getURI().toURL();
            }
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            try {
                URL url = this.getResource(path);
                if (url == null) {
                    return null;
                }
                Resource r = Resource.newResource(url);
                if (r.isDirectory()) {
                    return null;
                }
                return r.getInputStream();
            }
            catch (Exception e) {
                LOG.ignore(e);
                return null;
            }
        }

        @Override
        public Set<String> getResourcePaths(String path) {
            return ContextHandler.this.getResourcePaths(path);
        }

        @Override
        public void log(Exception exception, String msg) {
            ContextHandler.this._logger.warn(msg, exception);
        }

        @Override
        public void log(String msg) {
            ContextHandler.this._logger.info(msg, new Object[0]);
        }

        @Override
        public void log(String message, Throwable throwable) {
            ContextHandler.this._logger.warn(message, throwable);
        }

        @Override
        public String getInitParameter(String name) {
            return ContextHandler.this.getInitParameter(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return ContextHandler.this.getInitParameterNames();
        }

        @Override
        public Object getAttribute(String name) {
            Object o = ContextHandler.this.getAttribute(name);
            if (o == null) {
                o = super.getAttribute(name);
            }
            return o;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            HashSet<String> set = new HashSet<String>();
            Enumeration<String> e = super.getAttributeNames();
            while (e.hasMoreElements()) {
                set.add(e.nextElement());
            }
            e = ContextHandler.this._attributes.getAttributeNames();
            while (e.hasMoreElements()) {
                set.add(e.nextElement());
            }
            return Collections.enumeration(set);
        }

        @Override
        public void setAttribute(String name, Object value) {
            Object oldValue = super.getAttribute(name);
            if (value == null) {
                super.removeAttribute(name);
            } else {
                super.setAttribute(name, value);
            }
            if (!ContextHandler.this._servletContextAttributeListeners.isEmpty()) {
                ServletContextAttributeEvent event = new ServletContextAttributeEvent(ContextHandler.this._scontext, name, oldValue == null ? value : oldValue);
                for (ServletContextAttributeListener l : ContextHandler.this._servletContextAttributeListeners) {
                    if (oldValue == null) {
                        l.attributeAdded(event);
                        continue;
                    }
                    if (value == null) {
                        l.attributeRemoved(event);
                        continue;
                    }
                    l.attributeReplaced(event);
                }
            }
        }

        @Override
        public void removeAttribute(String name) {
            Object oldValue = super.getAttribute(name);
            super.removeAttribute(name);
            if (oldValue != null && !ContextHandler.this._servletContextAttributeListeners.isEmpty()) {
                ServletContextAttributeEvent event = new ServletContextAttributeEvent(ContextHandler.this._scontext, name, oldValue);
                for (ServletContextAttributeListener l : ContextHandler.this._servletContextAttributeListeners) {
                    l.attributeRemoved(event);
                }
            }
        }

        @Override
        public String getServletContextName() {
            String name = ContextHandler.this.getDisplayName();
            if (name == null) {
                name = ContextHandler.this.getContextPath();
            }
            return name;
        }

        @Override
        public String getContextPath() {
            return ContextHandler.this.getRequestContextPath();
        }

        @Override
        public String toString() {
            return "ServletContext@" + ContextHandler.this.toString();
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            if (ContextHandler.this.getInitParameter(name) != null) {
                return false;
            }
            ContextHandler.this.getInitParams().put(name, value);
            return true;
        }

        @Override
        public void addListener(String className) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            try {
                Class<?> clazz = ContextHandler.this._classLoader == null ? Loader.loadClass(className) : ContextHandler.this._classLoader.loadClass(className);
                this.addListener(clazz);
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public <T extends EventListener> void addListener(T t) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            this.checkListener(t.getClass());
            ContextHandler.this.addEventListener(t);
            ContextHandler.this.addProgrammaticListener(t);
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            try {
                EventListener e = this.createListener(listenerClass);
                this.addListener(e);
            }
            catch (ServletException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public void checkListener(Class<? extends EventListener> listener) throws IllegalStateException {
            int startIndex;
            boolean ok = false;
            for (int i = startIndex = this.isExtendedListenerTypes() ? 0 : 1; i < SERVLET_LISTENER_TYPES.length; ++i) {
                if (!SERVLET_LISTENER_TYPES[i].isAssignableFrom(listener)) continue;
                ok = true;
                break;
            }
            if (!ok) {
                throw new IllegalArgumentException("Inappropriate listener class " + listener.getName());
            }
        }

        public void setExtendedListenerTypes(boolean extended) {
            this._extendedListenerTypes = extended;
        }

        public boolean isExtendedListenerTypes() {
            return this._extendedListenerTypes;
        }

        @Override
        public ClassLoader getClassLoader() {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            if (!ContextHandler.this.isUsingSecurityManager()) {
                return ContextHandler.this._classLoader;
            }
            Caller caller = AccessController.doPrivileged(() -> new Caller());
            for (ClassLoader callerLoader = caller.getCallerClassLoader(2); callerLoader != null; callerLoader = callerLoader.getParent()) {
                if (callerLoader != ContextHandler.this._classLoader) continue;
                return ContextHandler.this._classLoader;
            }
            System.getSecurityManager().checkPermission(new RuntimePermission("getClassLoader"));
            return ContextHandler.this._classLoader;
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            LOG.warn(ContextHandler.UNIMPLEMENTED_USE_SERVLET_CONTEXT_HANDLER, "getJspConfigDescriptor()");
            return null;
        }

        public void setJspConfigDescriptor(JspConfigDescriptor d) {
        }

        @Override
        public void declareRoles(String ... roleNames) {
            if (!ContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
        }

        public void setEnabled(boolean enabled) {
            this._enabled = enabled;
        }

        public boolean isEnabled() {
            return this._enabled;
        }

        @Override
        public String getVirtualServerName() {
            String[] hosts = ContextHandler.this.getVirtualHosts();
            if (hosts != null && hosts.length > 0) {
                return hosts[0];
            }
            return null;
        }
    }

    public static enum Availability {
        STOPPED,
        STARTING,
        AVAILABLE,
        UNAVAILABLE,
        SHUTDOWN;

    }

    public static enum ContextStatus {
        NOTSET,
        INITIALIZED,
        DESTROYED;

    }
}

