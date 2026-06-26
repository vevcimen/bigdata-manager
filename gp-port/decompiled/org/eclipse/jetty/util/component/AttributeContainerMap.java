/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;

public class AttributeContainerMap
extends ContainerLifeCycle
implements Attributes {
    private final Map<String, Object> _map = new HashMap<String, Object>();

    @Override
    public synchronized void setAttribute(String name, Object attribute) {
        Object old = this._map.put(name, attribute);
        this.updateBean(old, attribute);
    }

    @Override
    public synchronized void removeAttribute(String name) {
        Object removed = this._map.remove(name);
        if (removed != null) {
            this.removeBean(removed);
        }
    }

    @Override
    public synchronized Object getAttribute(String name) {
        return this._map.get(name);
    }

    @Override
    public synchronized Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this._map.keySet());
    }

    @Override
    public Set<String> getAttributeNameSet() {
        return this._map.keySet();
    }

    @Override
    public synchronized void clearAttributes() {
        this._map.clear();
        this.removeBeans();
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Dumpable.dumpObject(out, this);
        Dumpable.dumpMapEntries(out, indent, this._map, true);
    }

    @Override
    public String toString() {
        return String.format("%s@%x{size=%d}", this.getClass().getSimpleName(), this.hashCode(), this._map.size());
    }
}

