/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import org.eclipse.jetty.io.CyclicTimeout;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Locker;

public class Session
implements SessionHandler.SessionIf {
    private static final Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");
    public static final String SESSION_CREATED_SECURE = "org.eclipse.jetty.security.sessionCreatedSecure";
    protected final SessionData _sessionData;
    protected final SessionHandler _handler;
    protected String _extendedId;
    protected long _requests;
    protected boolean _idChanged;
    protected boolean _newSession;
    protected State _state = State.VALID;
    protected Locker _lock = new Locker();
    protected Condition _stateChangeCompleted = this._lock.newCondition();
    protected boolean _resident = false;
    protected final SessionInactivityTimer _sessionInactivityTimer;

    public Session(SessionHandler handler, HttpServletRequest request, SessionData data) {
        this._handler = handler;
        this._sessionData = data;
        this._newSession = true;
        this._sessionData.setDirty(true);
        this._sessionInactivityTimer = new SessionInactivityTimer();
    }

    public Session(SessionHandler handler, SessionData data) {
        this._handler = handler;
        this._sessionData = data;
        this._sessionInactivityTimer = new SessionInactivityTimer();
    }

    public long getRequests() {
        try (Locker.Lock lock = this._lock.lock();){
            long l = this._requests;
            return l;
        }
    }

    public void setExtendedId(String extendedId) {
        this._extendedId = extendedId;
    }

    protected void cookieSet() {
        try (Locker.Lock lock = this._lock.lock();){
            this._sessionData.setCookieSet(this._sessionData.getAccessed());
        }
    }

    protected void use() {
        try (Locker.Lock lock = this._lock.lock();){
            ++this._requests;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session {} in use, stopping timer, active requests={}", this.getId(), this._requests);
            }
            this._sessionInactivityTimer.cancel();
        }
    }

    protected boolean access(long time) {
        try (Locker.Lock lock = this._lock.lock();){
            if (!this.isValid() || !this.isResident()) {
                boolean bl = false;
                return bl;
            }
            this._newSession = false;
            long lastAccessed = this._sessionData.getAccessed();
            this._sessionData.setAccessed(time);
            this._sessionData.setLastAccessed(lastAccessed);
            this._sessionData.calcAndSetExpiry(time);
            if (this.isExpiredAt(time)) {
                this.invalidate();
                boolean bl = false;
                return bl;
            }
            boolean bl = true;
            return bl;
        }
    }

    protected void complete() {
        try (Locker.Lock lock = this._lock.lock();){
            --this._requests;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session {} complete, active requests={}", this.getId(), this._requests);
            }
            if (this._requests == 0L) {
                long now = System.currentTimeMillis();
                this._sessionData.calcAndSetExpiry(now);
                this._sessionInactivityTimer.schedule(this.calculateInactivityTimeout(now));
            }
        }
    }

    protected boolean isExpiredAt(long time) {
        try (Locker.Lock lock = this._lock.lock();){
            boolean bl = this._sessionData.isExpiredAt(time);
            return bl;
        }
    }

    protected boolean isIdleLongerThan(int sec) {
        long now = System.currentTimeMillis();
        try (Locker.Lock lock = this._lock.lock();){
            boolean bl = this._sessionData.getAccessed() + (long)(sec * 1000) <= now;
            return bl;
        }
    }

    protected void callSessionAttributeListeners(String name, Object newValue, Object oldValue) {
        if (newValue == null || !newValue.equals(oldValue)) {
            if (oldValue != null) {
                this.unbindValue(name, oldValue);
            }
            if (newValue != null) {
                this.bindValue(name, newValue);
            }
            if (this._handler == null) {
                throw new IllegalStateException("No session manager for session " + this._sessionData.getId());
            }
            this._handler.doSessionAttributeListeners(this, name, oldValue, newValue);
        }
    }

    public void unbindValue(String name, Object value) {
        if (value != null && value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener)value).valueUnbound(new HttpSessionBindingEvent(this, name));
        }
    }

    public void bindValue(String name, Object value) {
        if (value != null && value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener)value).valueBound(new HttpSessionBindingEvent(this, name));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void didActivate() {
        boolean dirty = this.getSessionData().isDirty();
        try {
            HttpSessionEvent event = new HttpSessionEvent(this);
            Iterator<String> iter = this._sessionData.getKeys().iterator();
            while (iter.hasNext()) {
                Object value = this._sessionData.getAttribute(iter.next());
                if (!(value instanceof HttpSessionActivationListener)) continue;
                HttpSessionActivationListener listener = (HttpSessionActivationListener)value;
                listener.sessionDidActivate(event);
            }
        }
        finally {
            this.getSessionData().setDirty(dirty);
        }
    }

    public void willPassivate() {
        HttpSessionEvent event = new HttpSessionEvent(this);
        Iterator<String> iter = this._sessionData.getKeys().iterator();
        while (iter.hasNext()) {
            Object value = this._sessionData.getAttribute(iter.next());
            if (!(value instanceof HttpSessionActivationListener)) continue;
            HttpSessionActivationListener listener = (HttpSessionActivationListener)value;
            listener.sessionWillPassivate(event);
        }
    }

    public boolean isValid() {
        try (Locker.Lock lock = this._lock.lock();){
            boolean bl = this._state == State.VALID;
            return bl;
        }
    }

    public boolean isInvalid() {
        try (Locker.Lock lock = this._lock.lock();){
            boolean bl = this._state == State.INVALID || this._state == State.INVALIDATING;
            return bl;
        }
    }

    public boolean isChanging() {
        this.checkLocked();
        return this._state == State.CHANGING;
    }

    public long getCookieSetTime() {
        try (Locker.Lock lock = this._lock.lock();){
            long l = this._sessionData.getCookieSet();
            return l;
        }
    }

    @Override
    public long getCreationTime() throws IllegalStateException {
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForRead();
            long l = this._sessionData.getCreated();
            return l;
        }
    }

    @Override
    public String getId() {
        try (Locker.Lock lock = this._lock.lock();){
            String string = this._sessionData.getId();
            return string;
        }
    }

    public String getExtendedId() {
        return this._extendedId;
    }

    public String getContextPath() {
        return this._sessionData.getContextPath();
    }

    public String getVHost() {
        return this._sessionData.getVhost();
    }

    @Override
    public long getLastAccessedTime() {
        try (Locker.Lock lock = this._lock.lock();){
            if (this.isInvalid()) {
                throw new IllegalStateException("Session not valid");
            }
            long l = this._sessionData.getLastAccessed();
            return l;
        }
    }

    @Override
    public ServletContext getServletContext() {
        if (this._handler == null) {
            throw new IllegalStateException("No session manager for session " + this._sessionData.getId());
        }
        return this._handler._context;
    }

    @Override
    public void setMaxInactiveInterval(int secs) {
        try (Locker.Lock lock = this._lock.lock();){
            this._sessionData.setMaxInactiveMs((long)secs * 1000L);
            this._sessionData.calcAndSetExpiry();
            this._sessionData.setDirty(true);
            if (LOG.isDebugEnabled()) {
                if (secs <= 0) {
                    LOG.debug("Session {} is now immortal (maxInactiveInterval={})", this._sessionData.getId(), secs);
                } else {
                    LOG.debug("Session {} maxInactiveInterval={}", this._sessionData.getId(), secs);
                }
            }
        }
    }

    @Deprecated
    public void updateInactivityTimer() {
    }

    public long calculateInactivityTimeout(long now) {
        long time = 0L;
        try (Locker.Lock lock = this._lock.lock();){
            long remaining = this._sessionData.getExpiry() - now;
            long maxInactive = this._sessionData.getMaxInactiveMs();
            int evictionPolicy = this.getSessionHandler().getSessionCache().getEvictionPolicy();
            if (maxInactive <= 0L) {
                if (evictionPolicy < 1) {
                    time = -1L;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Session {} is immortal && no inactivity eviction", this.getId());
                    }
                } else {
                    time = TimeUnit.SECONDS.toMillis(evictionPolicy);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Session {} is immortal; evict after {} sec inactivity", this.getId(), evictionPolicy);
                    }
                }
            } else if (evictionPolicy == -1) {
                long l = time = remaining > 0L ? remaining : 0L;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Session {} no eviction", this.getId());
                }
            } else if (evictionPolicy == 0) {
                time = -1L;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Session {} evict on exit", this.getId());
                }
            } else {
                long l = time = remaining > 0L ? Math.min(maxInactive, TimeUnit.SECONDS.toMillis(evictionPolicy)) : 0L;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Session {} timer set to lesser of maxInactive={} and inactivityEvict={}", this.getId(), maxInactive, evictionPolicy);
                }
            }
        }
        return time;
    }

    @Override
    public int getMaxInactiveInterval() {
        try (Locker.Lock lock = this._lock.lock();){
            long maxInactiveMs = this._sessionData.getMaxInactiveMs();
            int n = (int)(maxInactiveMs < 0L ? -1L : maxInactiveMs / 1000L);
            return n;
        }
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        this.checkValidForRead();
        return SessionHandler.__nullSessionContext;
    }

    public SessionHandler getSessionHandler() {
        return this._handler;
    }

    protected void checkValidForWrite() throws IllegalStateException {
        this.checkLocked();
        if (this._state == State.INVALID) {
            throw new IllegalStateException("Not valid for write: id=" + this._sessionData.getId() + " created=" + this._sessionData.getCreated() + " accessed=" + this._sessionData.getAccessed() + " lastaccessed=" + this._sessionData.getLastAccessed() + " maxInactiveMs=" + this._sessionData.getMaxInactiveMs() + " expiry=" + this._sessionData.getExpiry());
        }
        if (this._state == State.INVALIDATING) {
            return;
        }
        if (!this.isResident()) {
            throw new IllegalStateException("Not valid for write: id=" + this._sessionData.getId() + " not resident");
        }
    }

    protected void checkValidForRead() throws IllegalStateException {
        this.checkLocked();
        if (this._state == State.INVALID) {
            throw new IllegalStateException("Invalid for read: id=" + this._sessionData.getId() + " created=" + this._sessionData.getCreated() + " accessed=" + this._sessionData.getAccessed() + " lastaccessed=" + this._sessionData.getLastAccessed() + " maxInactiveMs=" + this._sessionData.getMaxInactiveMs() + " expiry=" + this._sessionData.getExpiry());
        }
        if (this._state == State.INVALIDATING) {
            return;
        }
        if (!this.isResident()) {
            throw new IllegalStateException("Invalid for read: id=" + this._sessionData.getId() + " not resident");
        }
    }

    protected void checkLocked() throws IllegalStateException {
        if (!this._lock.isLocked()) {
            throw new IllegalStateException("Session not locked");
        }
    }

    @Override
    public Object getAttribute(String name) {
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForRead();
            Object object = this._sessionData.getAttribute(name);
            return object;
        }
    }

    @Override
    @Deprecated
    public Object getValue(String name) {
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForRead();
            Object object = this._sessionData.getAttribute(name);
            return object;
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForRead();
            final Iterator<String> itor = this._sessionData.getKeys().iterator();
            Enumeration<String> enumeration = new Enumeration<String>(){

                @Override
                public boolean hasMoreElements() {
                    return itor.hasNext();
                }

                @Override
                public String nextElement() {
                    return (String)itor.next();
                }
            };
            return enumeration;
        }
    }

    public int getAttributes() {
        return this._sessionData.getKeys().size();
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(this._sessionData.getKeys());
    }

    @Override
    @Deprecated
    public String[] getValueNames() throws IllegalStateException {
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForRead();
            Iterator<String> itor = this._sessionData.getKeys().iterator();
            if (!itor.hasNext()) {
                String[] stringArray = new String[]{};
                return stringArray;
            }
            ArrayList<String> names = new ArrayList<String>();
            while (itor.hasNext()) {
                names.add(itor.next());
            }
            String[] stringArray = names.toArray(new String[names.size()]);
            return stringArray;
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        Object old = null;
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForWrite();
            old = this._sessionData.setAttribute(name, value);
        }
        if (value == null && old == null) {
            return;
        }
        this.callSessionAttributeListeners(name, value, old);
    }

    @Override
    @Deprecated
    public void putValue(String name, Object value) {
        this.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        this.setAttribute(name, null);
    }

    @Override
    @Deprecated
    public void removeValue(String name) {
        this.setAttribute(name, null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void renewId(HttpServletRequest request) {
        if (this._handler == null) {
            throw new IllegalStateException("No session manager for session " + this._sessionData.getId());
        }
        String id = null;
        String extendedId = null;
        try (Locker.Lock lock = this._lock.lock();){
            block21: while (true) {
                switch (2.$SwitchMap$org$eclipse$jetty$server$session$Session$State[this._state.ordinal()]) {
                    case 1: 
                    case 2: {
                        throw new IllegalStateException();
                    }
                    case 3: {
                        try {
                            this._stateChangeCompleted.await();
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    continue block21;
                    case 4: {
                        this._state = State.CHANGING;
                        break block21;
                    }
                    default: {
                        throw new IllegalStateException();
                    }
                }
                break;
            }
            id = this._sessionData.getId();
            extendedId = this.getExtendedId();
        }
        String newId = this._handler._sessionIdManager.renewSessionId(id, extendedId, request);
        try (Locker.Lock lock = this._lock.lock();){
            switch (this._state) {
                case CHANGING: {
                    if (id.equals(newId)) {
                        throw new IllegalStateException("Unable to change session id");
                    }
                    this._sessionData.setId(newId);
                    this.setExtendedId(this._handler._sessionIdManager.getExtendedId(newId, request));
                    this.setIdChanged(true);
                    this._state = State.VALID;
                    this._stateChangeCompleted.signalAll();
                    return;
                }
                case INVALID: 
                case INVALIDATING: {
                    throw new IllegalStateException("Session invalid");
                }
                default: {
                    throw new IllegalStateException();
                }
            }
        }
    }

    @Override
    public void invalidate() {
        block6: {
            if (this._handler == null) {
                throw new IllegalStateException("No session manager for session " + this._sessionData.getId());
            }
            boolean result = this.beginInvalidate();
            try {
                if (!result) break block6;
                try {
                    this._handler.callSessionDestroyedListeners(this);
                }
                finally {
                    this.finishInvalidate();
                }
                this._handler.getSessionIdManager().invalidateAll(this._sessionData.getId());
            }
            catch (Exception e) {
                LOG.warn(e);
            }
        }
    }

    public Locker.Lock lock() {
        return this._lock.lock();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected boolean beginInvalidate() {
        boolean result = false;
        try (Locker.Lock lock = this._lock.lock();){
            block13: while (true) {
                switch (2.$SwitchMap$org$eclipse$jetty$server$session$Session$State[this._state.ordinal()]) {
                    case 1: {
                        throw new IllegalStateException();
                    }
                    case 2: {
                        if (!LOG.isDebugEnabled()) return result;
                        LOG.debug("Session {} already being invalidated", this._sessionData.getId());
                        return result;
                    }
                    case 3: {
                        try {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Session {} waiting for id change to complete", this._sessionData.getId());
                            }
                            this._stateChangeCompleted.await();
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    continue block13;
                    case 4: {
                        result = true;
                        this._state = State.INVALIDATING;
                        return result;
                    }
                    default: {
                        throw new IllegalStateException();
                    }
                }
                break;
            }
        }
    }

    @Deprecated
    protected void doInvalidate() throws IllegalStateException {
        this.finishInvalidate();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void finishInvalidate() throws IllegalStateException {
        try (Locker.Lock lock = this._lock.lock();){
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("invalidate {}", this._sessionData.getId());
                }
                if (this._state == State.VALID || this._state == State.INVALIDATING) {
                    Set<String> keys = null;
                    do {
                        keys = this._sessionData.getKeys();
                        for (String key : keys) {
                            Object old = this._sessionData.setAttribute(key, null);
                            if (old == null) continue;
                            this.callSessionAttributeListeners(key, null, old);
                        }
                    } while (!keys.isEmpty());
                }
            }
            finally {
                this._state = State.INVALID;
                this._handler.recordSessionTime(this);
                this._stateChangeCompleted.signalAll();
            }
        }
    }

    @Override
    public boolean isNew() throws IllegalStateException {
        try (Locker.Lock lock = this._lock.lock();){
            this.checkValidForRead();
            boolean bl = this._newSession;
            return bl;
        }
    }

    public void setIdChanged(boolean changed) {
        try (Locker.Lock lock = this._lock.lock();){
            this._idChanged = changed;
        }
    }

    public boolean isIdChanged() {
        try (Locker.Lock lock = this._lock.lock();){
            boolean bl = this._idChanged;
            return bl;
        }
    }

    @Override
    public Session getSession() {
        return this;
    }

    protected SessionData getSessionData() {
        return this._sessionData;
    }

    public void setResident(boolean resident) {
        this._resident = resident;
        if (!this._resident) {
            this._sessionInactivityTimer.destroy();
        }
    }

    public boolean isResident() {
        return this._resident;
    }

    public String toString() {
        try (Locker.Lock lock = this._lock.lock();){
            String string = String.format("%s@%x{id=%s,x=%s,req=%d,res=%b}", this.getClass().getSimpleName(), this.hashCode(), this._sessionData.getId(), this._extendedId, this._requests, this._resident);
            return string;
        }
    }

    public class SessionInactivityTimer {
        protected final CyclicTimeout _timer;

        public SessionInactivityTimer() {
            this._timer = new CyclicTimeout(Session.this.getSessionHandler().getScheduler()){

                @Override
                public void onTimeoutExpired() {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Timer expired for session {}", Session.this.getId());
                    }
                    long now = System.currentTimeMillis();
                    Session.this.getSessionHandler().sessionInactivityTimerExpired(Session.this, now);
                    try (Locker.Lock lock = Session.this.lock();){
                        if (Session.this.isResident() && Session.this.getRequests() <= 0L && Session.this.isValid() && !Session.this.isExpiredAt(now)) {
                            SessionInactivityTimer.this.schedule(Session.this.calculateInactivityTimeout(now));
                        }
                    }
                }
            };
        }

        @Deprecated
        public void schedule() {
            this.schedule(Session.this.calculateInactivityTimeout(System.currentTimeMillis()));
        }

        public void schedule(long time) {
            if (time >= 0L) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("(Re)starting timer for session {} at {}ms", Session.this.getId(), time);
                }
                this._timer.schedule(time, TimeUnit.MILLISECONDS);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Not starting timer for session {}", Session.this.getId());
            }
        }

        public void cancel() {
            this._timer.cancel();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cancelled timer for session {}", Session.this.getId());
            }
        }

        public void destroy() {
            this._timer.destroy();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Destroyed timer for session {}", Session.this.getId());
            }
        }
    }

    public static enum IdState {
        SET,
        CHANGING;

    }

    public static enum State {
        VALID,
        INVALID,
        INVALIDATING,
        CHANGING;

    }
}

