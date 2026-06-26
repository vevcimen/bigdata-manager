/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionContext;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.session.UnreadableSessionDataException;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Locker;

@ManagedObject
public abstract class AbstractSessionCache
extends ContainerLifeCycle
implements SessionCache {
    static final Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");
    protected SessionDataStore _sessionDataStore;
    protected final SessionHandler _handler;
    protected SessionContext _context;
    protected int _evictionPolicy = -1;
    protected boolean _saveOnCreate = false;
    protected boolean _saveOnInactiveEviction;
    protected boolean _removeUnloadableSessions;
    protected boolean _flushOnResponseCommit;
    protected boolean _invalidateOnShutdown;

    @Override
    public abstract Session newSession(SessionData var1);

    public abstract Session newSession(HttpServletRequest var1, SessionData var2);

    protected abstract Session doGet(String var1);

    protected abstract Session doPutIfAbsent(String var1, Session var2);

    protected abstract Session doComputeIfAbsent(String var1, Function<String, Session> var2);

    protected abstract boolean doReplace(String var1, Session var2, Session var3);

    public abstract Session doDelete(String var1);

    public AbstractSessionCache(SessionHandler handler) {
        this._handler = handler;
    }

    @Override
    public SessionHandler getSessionHandler() {
        return this._handler;
    }

    @Override
    public void initialize(SessionContext context) {
        if (this.isStarted()) {
            throw new IllegalStateException("Context set after session store started");
        }
        this._context = context;
    }

    @Override
    protected void doStart() throws Exception {
        if (this._sessionDataStore == null) {
            throw new IllegalStateException("No session data store configured");
        }
        if (this._handler == null) {
            throw new IllegalStateException("No session manager");
        }
        if (this._context == null) {
            throw new IllegalStateException("No ContextId");
        }
        this._sessionDataStore.initialize(this._context);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        this._sessionDataStore.stop();
        super.doStop();
    }

    @Override
    public SessionDataStore getSessionDataStore() {
        return this._sessionDataStore;
    }

    @Override
    public void setSessionDataStore(SessionDataStore sessionStore) {
        this.updateBean(this._sessionDataStore, sessionStore);
        this._sessionDataStore = sessionStore;
    }

    @Override
    @ManagedAttribute(value="session eviction policy", readonly=true)
    public int getEvictionPolicy() {
        return this._evictionPolicy;
    }

    @Override
    public void setEvictionPolicy(int evictionTimeout) {
        this._evictionPolicy = evictionTimeout;
    }

    @Override
    @ManagedAttribute(value="immediately save new sessions", readonly=true)
    public boolean isSaveOnCreate() {
        return this._saveOnCreate;
    }

    @Override
    public void setSaveOnCreate(boolean saveOnCreate) {
        this._saveOnCreate = saveOnCreate;
    }

    @Override
    @ManagedAttribute(value="delete unreadable stored sessions", readonly=true)
    public boolean isRemoveUnloadableSessions() {
        return this._removeUnloadableSessions;
    }

    @Override
    public void setRemoveUnloadableSessions(boolean removeUnloadableSessions) {
        this._removeUnloadableSessions = removeUnloadableSessions;
    }

    @Override
    public void setFlushOnResponseCommit(boolean flushOnResponseCommit) {
        this._flushOnResponseCommit = flushOnResponseCommit;
    }

    @Override
    public boolean isFlushOnResponseCommit() {
        return this._flushOnResponseCommit;
    }

    @Override
    public Session get(String id) throws Exception {
        return this.getAndEnter(id, true);
    }

    protected Session getAndEnter(String id, boolean enter) throws Exception {
        Session session = null;
        AtomicReference exception = new AtomicReference();
        session = this.doComputeIfAbsent(id, k -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session {} not found locally in {}, attempting to load", id, this);
            }
            try {
                Session s2 = this.loadSession((String)k);
                if (s2 != null) {
                    try (Locker.Lock lock = s2.lock();){
                        s2.setResident(true);
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Session {} not loaded by store", id);
                }
                return s2;
            }
            catch (Exception e) {
                exception.set(e);
                return null;
            }
        });
        Exception ex = (Exception)exception.get();
        if (ex != null) {
            throw ex;
        }
        if (session != null) {
            try (Locker.Lock lock = session.lock();){
                if (!session.isResident()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Non-resident session {} in cache", id);
                    }
                    Session session2 = null;
                    return session2;
                }
                if (enter) {
                    session.use();
                }
            }
        }
        return session;
    }

    private Session loadSession(String id) throws Exception {
        SessionData data = null;
        Session session = null;
        if (this._sessionDataStore == null) {
            return null;
        }
        try {
            data = this._sessionDataStore.load(id);
            if (data == null) {
                return null;
            }
            data.setLastNode(this._context.getWorkerName());
            session = this.newSession(data);
            return session;
        }
        catch (UnreadableSessionDataException e) {
            if (this.isRemoveUnloadableSessions()) {
                this._sessionDataStore.delete(id);
            }
            throw e;
        }
    }

    @Override
    public void add(String id, Session session) throws Exception {
        block10: {
            if (id == null || session == null) {
                throw new IllegalArgumentException("Add key=" + id + " session=" + (session == null ? "null" : session.getId()));
            }
            try (Locker.Lock lock = session.lock();){
                if (session.getSessionHandler() == null) {
                    throw new IllegalStateException("Session " + id + " is not managed");
                }
                if (!session.isValid()) {
                    throw new IllegalStateException("Session " + id + " is not valid");
                }
                if (this.doPutIfAbsent(id, session) == null) {
                    session.setResident(true);
                    session.use();
                    break block10;
                }
                throw new IllegalStateException("Session " + id + " already in cache");
            }
        }
    }

    @Override
    public void commit(Session session) throws Exception {
        if (session == null) {
            return;
        }
        try (Locker.Lock lock = session.lock();){
            if (session.isValid() && session.getSessionData().isDirty() && this._flushOnResponseCommit) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flush session {} on response commit", session);
                }
                if (!this._sessionDataStore.isPassivating()) {
                    this._sessionDataStore.store(session.getId(), session.getSessionData());
                } else {
                    session.willPassivate();
                    this._sessionDataStore.store(session.getId(), session.getSessionData());
                    session.didActivate();
                }
            }
        }
    }

    @Override
    @Deprecated
    public void put(String id, Session session) throws Exception {
        this.release(id, session);
    }

    @Override
    public void release(String id, Session session) throws Exception {
        if (id == null || session == null) {
            throw new IllegalArgumentException("Put key=" + id + " session=" + (session == null ? "null" : session.getId()));
        }
        try (Locker.Lock lock = session.lock();){
            if (session.getSessionHandler() == null) {
                throw new IllegalStateException("Session " + id + " is not managed");
            }
            if (session.isInvalid()) {
                return;
            }
            session.complete();
            if (session.getRequests() <= 0L) {
                if (!this._sessionDataStore.isPassivating()) {
                    this._sessionDataStore.store(id, session.getSessionData());
                    if (this.getEvictionPolicy() == 0) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Eviction on request exit id={}", id);
                        }
                        this.doDelete(session.getId());
                        session.setResident(false);
                    } else {
                        session.setResident(true);
                        this.doPutIfAbsent(id, session);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Non passivating SessionDataStore, session in SessionCache only id={}", id);
                        }
                    }
                } else {
                    session.willPassivate();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Session passivating id={}", id);
                    }
                    this._sessionDataStore.store(id, session.getSessionData());
                    if (this.getEvictionPolicy() == 0) {
                        this.doDelete(id);
                        session.setResident(false);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Evicted on request exit id={}", id);
                        }
                    } else {
                        session.didActivate();
                        session.setResident(true);
                        this.doPutIfAbsent(id, session);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Session reactivated id={}", id);
                        }
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Req count={} for id={}", session.getRequests(), id);
                }
                session.setResident(true);
                this.doPutIfAbsent(id, session);
            }
        }
    }

    @Override
    public boolean exists(String id) throws Exception {
        Session s2 = this.doGet(id);
        if (s2 != null) {
            try (Locker.Lock lock = s2.lock();){
                boolean bl = s2.isValid();
                return bl;
            }
        }
        return this._sessionDataStore.exists(id);
    }

    @Override
    public boolean contains(String id) throws Exception {
        return this.doGet(id) != null;
    }

    @Override
    public Session delete(String id) throws Exception {
        Session session = this.getAndEnter(id, false);
        if (this._sessionDataStore != null) {
            boolean dsdel = this._sessionDataStore.delete(id);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session {} deleted in session data store {}", id, dsdel);
            }
        }
        if (session != null) {
            session.setResident(false);
        }
        return this.doDelete(id);
    }

    @Override
    public Set<String> checkExpiration(Set<String> candidates) {
        if (!this.isStarted()) {
            return Collections.emptySet();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} checking expiration on {}", this, candidates);
        }
        Set<String> allCandidates = this._sessionDataStore.getExpired(candidates);
        HashSet<String> sessionsInUse = new HashSet<String>();
        if (allCandidates != null) {
            for (String c : allCandidates) {
                Session s2 = this.doGet(c);
                if (s2 == null || s2.getRequests() <= 0L) continue;
                sessionsInUse.add(c);
            }
            try {
                allCandidates.removeAll(sessionsInUse);
            }
            catch (UnsupportedOperationException e) {
                HashSet<String> tmp = new HashSet<String>(allCandidates);
                tmp.removeAll(sessionsInUse);
                allCandidates = tmp;
            }
        }
        return allCandidates;
    }

    @Override
    public void checkInactiveSession(Session session) {
        if (session == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking for idle {}", session.getId());
        }
        try (Locker.Lock s2 = session.lock();){
            if (this.getEvictionPolicy() > 0 && session.isIdleLongerThan(this.getEvictionPolicy()) && session.isValid() && session.isResident() && session.getRequests() <= 0L) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Evicting idle session {}", session.getId());
                    }
                    if (this.isSaveOnInactiveEviction() && this._sessionDataStore != null) {
                        if (this._sessionDataStore.isPassivating()) {
                            session.willPassivate();
                        }
                        session.getSessionData().setDirty(true);
                        this._sessionDataStore.store(session.getId(), session.getSessionData());
                    }
                    this.doDelete(session.getId());
                    session.setResident(false);
                }
                catch (Exception e) {
                    LOG.warn("Passivation of idle session {} failed", session.getId());
                    LOG.warn(e);
                }
            }
        }
    }

    @Override
    public Session renewSessionId(String oldId, String newId, String oldExtendedId, String newExtendedId) throws Exception {
        if (StringUtil.isBlank(oldId)) {
            throw new IllegalArgumentException("Old session id is null");
        }
        if (StringUtil.isBlank(newId)) {
            throw new IllegalArgumentException("New session id is null");
        }
        Session session = this.getAndEnter(oldId, true);
        this.renewSessionId(session, newId, newExtendedId);
        return session;
    }

    protected void renewSessionId(Session session, String newId, String newExtendedId) throws Exception {
        if (session == null) {
            return;
        }
        try (Locker.Lock lock = session.lock();){
            String oldId = session.getId();
            session.checkValidForWrite();
            session.getSessionData().setId(newId);
            session.getSessionData().setLastSaved(0L);
            session.getSessionData().setDirty(true);
            session.setExtendedId(newExtendedId);
            session.setIdChanged(true);
            this.doPutIfAbsent(newId, session);
            this.doDelete(oldId);
            if (this._sessionDataStore != null) {
                this._sessionDataStore.delete(oldId);
                this._sessionDataStore.store(newId, session.getSessionData());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session id {} swapped for new id {}", oldId, newId);
            }
        }
    }

    @Override
    public void setSaveOnInactiveEviction(boolean saveOnEvict) {
        this._saveOnInactiveEviction = saveOnEvict;
    }

    @Override
    public void setInvalidateOnShutdown(boolean invalidateOnShutdown) {
        this._invalidateOnShutdown = invalidateOnShutdown;
    }

    @Override
    public boolean isInvalidateOnShutdown() {
        return this._invalidateOnShutdown;
    }

    @Override
    @ManagedAttribute(value="save sessions before evicting from cache", readonly=true)
    public boolean isSaveOnInactiveEviction() {
        return this._saveOnInactiveEviction;
    }

    @Override
    public Session newSession(HttpServletRequest request, String id, long time, long maxInactiveMs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new session id=" + id, new Object[0]);
        }
        Session session = this.newSession(request, this._sessionDataStore.newSessionData(id, time, time, time, maxInactiveMs));
        session.getSessionData().setLastNode(this._context.getWorkerName());
        try {
            if (this.isSaveOnCreate() && this._sessionDataStore != null) {
                this._sessionDataStore.store(id, session.getSessionData());
            }
        }
        catch (Exception e) {
            LOG.warn("Save of new session {} failed", id);
            LOG.warn(e);
        }
        return session;
    }

    @Override
    public String toString() {
        return String.format("%s@%x[evict=%d,removeUnloadable=%b,saveOnCreate=%b,saveOnInactiveEvict=%b]", this.getClass().getName(), this.hashCode(), this._evictionPolicy, this._removeUnloadableSessions, this._saveOnCreate, this._saveOnInactiveEviction);
    }
}

