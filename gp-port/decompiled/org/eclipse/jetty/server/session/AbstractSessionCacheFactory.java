/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionCacheFactory;
import org.eclipse.jetty.server.session.SessionHandler;

public abstract class AbstractSessionCacheFactory
implements SessionCacheFactory {
    int _evictionPolicy;
    boolean _saveOnInactiveEvict;
    boolean _saveOnCreate;
    boolean _removeUnloadableSessions;
    boolean _flushOnResponseCommit;
    boolean _invalidateOnShutdown;

    public abstract SessionCache newSessionCache(SessionHandler var1);

    public boolean isInvalidateOnShutdown() {
        return this._invalidateOnShutdown;
    }

    public void setInvalidateOnShutdown(boolean invalidateOnShutdown) {
        this._invalidateOnShutdown = invalidateOnShutdown;
    }

    public boolean isFlushOnResponseCommit() {
        return this._flushOnResponseCommit;
    }

    public void setFlushOnResponseCommit(boolean flushOnResponseCommit) {
        this._flushOnResponseCommit = flushOnResponseCommit;
    }

    public boolean isSaveOnCreate() {
        return this._saveOnCreate;
    }

    public void setSaveOnCreate(boolean saveOnCreate) {
        this._saveOnCreate = saveOnCreate;
    }

    public boolean isRemoveUnloadableSessions() {
        return this._removeUnloadableSessions;
    }

    public void setRemoveUnloadableSessions(boolean removeUnloadableSessions) {
        this._removeUnloadableSessions = removeUnloadableSessions;
    }

    public int getEvictionPolicy() {
        return this._evictionPolicy;
    }

    public void setEvictionPolicy(int evictionPolicy) {
        this._evictionPolicy = evictionPolicy;
    }

    public boolean isSaveOnInactiveEvict() {
        return this._saveOnInactiveEvict;
    }

    public void setSaveOnInactiveEvict(boolean saveOnInactiveEvict) {
        this._saveOnInactiveEvict = saveOnInactiveEvict;
    }

    @Override
    public SessionCache getSessionCache(SessionHandler handler) {
        SessionCache cache = this.newSessionCache(handler);
        cache.setEvictionPolicy(this.getEvictionPolicy());
        cache.setSaveOnInactiveEviction(this.isSaveOnInactiveEvict());
        cache.setSaveOnCreate(this.isSaveOnCreate());
        cache.setRemoveUnloadableSessions(this.isRemoveUnloadableSessions());
        cache.setFlushOnResponseCommit(this.isFlushOnResponseCommit());
        cache.setInvalidateOnShutdown(this.isInvalidateOnShutdown());
        return cache;
    }
}

