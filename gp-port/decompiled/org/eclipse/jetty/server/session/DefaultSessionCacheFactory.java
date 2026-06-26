/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import org.eclipse.jetty.server.session.AbstractSessionCacheFactory;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;

public class DefaultSessionCacheFactory
extends AbstractSessionCacheFactory {
    @Override
    public SessionCache newSessionCache(SessionHandler handler) {
        return new DefaultSessionCache(handler);
    }
}

