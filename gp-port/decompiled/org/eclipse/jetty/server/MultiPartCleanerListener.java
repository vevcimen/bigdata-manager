/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import org.eclipse.jetty.server.MultiParts;
import org.eclipse.jetty.server.handler.ContextHandler;

public class MultiPartCleanerListener
implements ServletRequestListener {
    public static final MultiPartCleanerListener INSTANCE = new MultiPartCleanerListener();

    protected MultiPartCleanerListener() {
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ContextHandler.Context context;
        MultiParts parts = (MultiParts)sre.getServletRequest().getAttribute("org.eclipse.jetty.multiParts");
        if (parts != null && (context = parts.getContext()) == sre.getServletContext()) {
            try {
                parts.close();
            }
            catch (Throwable e) {
                sre.getServletContext().log("Errors deleting multipart tmp files", e);
            }
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
    }
}

