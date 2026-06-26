/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import org.eclipse.jetty.server.session.AbstractSessionCacheFactory;
import org.eclipse.jetty.server.session.NullSessionCache;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class NullSessionCacheFactory
extends AbstractSessionCacheFactory {
    private static final Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");

    @Override
    public int getEvictionPolicy() {
        return 0;
    }

    @Override
    public void setEvictionPolicy(int evictionPolicy) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ignoring eviction policy setting for NullSessionCaches", new Object[0]);
        }
    }

    @Override
    public boolean isSaveOnInactiveEvict() {
        return false;
    }

    @Override
    public void setSaveOnInactiveEvict(boolean saveOnInactiveEvict) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ignoring eviction policy setting for NullSessionCaches", new Object[0]);
        }
    }

    @Override
    public boolean isInvalidateOnShutdown() {
        return false;
    }

    @Override
    public void setInvalidateOnShutdown(boolean invalidateOnShutdown) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ignoring invalidateOnShutdown setting for NullSessionCaches", new Object[0]);
        }
    }

    @Override
    public SessionCache newSessionCache(SessionHandler handler) {
        return new NullSessionCache(handler);
    }
}

