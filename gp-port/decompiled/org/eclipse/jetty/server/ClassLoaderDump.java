/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.net.URLClassLoader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.DumpableCollection;

public class ClassLoaderDump
implements Dumpable {
    final ClassLoader _loader;

    public ClassLoaderDump(ClassLoader loader) {
        this._loader = loader;
    }

    @Override
    public String dump() {
        return Dumpable.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        if (this._loader == null) {
            out.append("No ClassLoader\n");
        } else if (this._loader instanceof Dumpable) {
            ((Dumpable)((Object)this._loader)).dump(out, indent);
        } else if (this._loader instanceof URLClassLoader) {
            String loader = this._loader.toString();
            DumpableCollection urls = DumpableCollection.fromArray("URLs", ((URLClassLoader)this._loader).getURLs());
            ClassLoader parent = this._loader.getParent();
            if (parent == null) {
                Dumpable.dumpObjects(out, indent, loader, urls);
            } else if (parent == Server.class.getClassLoader()) {
                Dumpable.dumpObjects(out, indent, loader, urls, parent.toString());
            } else if (parent instanceof Dumpable) {
                Dumpable.dumpObjects(out, indent, loader, urls, parent);
            } else {
                Dumpable.dumpObjects(out, indent, loader, urls, new ClassLoaderDump(parent));
            }
        } else {
            String loader = this._loader.toString();
            ClassLoader parent = this._loader.getParent();
            if (parent == null) {
                Dumpable.dumpObject(out, loader);
            }
            if (parent == Server.class.getClassLoader()) {
                Dumpable.dumpObjects(out, indent, loader, parent.toString());
            } else if (parent instanceof Dumpable) {
                Dumpable.dumpObjects(out, indent, loader, parent);
            } else if (parent != null) {
                Dumpable.dumpObjects(out, indent, loader, new ClassLoaderDump(parent));
            }
        }
    }
}

