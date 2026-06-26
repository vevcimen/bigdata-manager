/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.component.Dumpable;

public class AttributesMap
implements Attributes,
Dumpable {
    private final AtomicReference<ConcurrentMap<String, Object>> _map = new AtomicReference();

    public AttributesMap() {
    }

    public AttributesMap(AttributesMap attributes) {
        ConcurrentMap<String, Object> map = attributes.map();
        if (map != null) {
            this._map.set(new ConcurrentHashMap<String, Object>(map));
        }
    }

    private ConcurrentMap<String, Object> map() {
        return this._map.get();
    }

    private ConcurrentMap<String, Object> ensureMap() {
        ConcurrentMap<String, Object> map;
        do {
            if ((map = this.map()) == null) continue;
            return map;
        } while (!this._map.compareAndSet(null, map = new ConcurrentHashMap<String, Object>()));
        return map;
    }

    @Override
    public void removeAttribute(String name) {
        ConcurrentMap<String, Object> map = this.map();
        if (map != null) {
            map.remove(name);
        }
    }

    @Override
    public void setAttribute(String name, Object attribute) {
        if (attribute == null) {
            this.removeAttribute(name);
        } else {
            this.ensureMap().put(name, attribute);
        }
    }

    @Override
    public Object getAttribute(String name) {
        ConcurrentMap<String, Object> map = this.map();
        return map == null ? null : map.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.getAttributeNameSet());
    }

    @Override
    public Set<String> getAttributeNameSet() {
        return this.keySet();
    }

    public Set<Map.Entry<String, Object>> getAttributeEntrySet() {
        ConcurrentMap<String, Object> map = this.map();
        return map == null ? Collections.emptySet() : map.entrySet();
    }

    public static Enumeration<String> getAttributeNamesCopy(Attributes attrs) {
        if (attrs instanceof AttributesMap) {
            return Collections.enumeration(((AttributesMap)attrs).keySet());
        }
        ArrayList<String> names = new ArrayList<String>(Collections.list(attrs.getAttributeNames()));
        return Collections.enumeration(names);
    }

    @Override
    public void clearAttributes() {
        ConcurrentMap<String, Object> map = this.map();
        if (map != null) {
            map.clear();
        }
    }

    public int size() {
        ConcurrentMap<String, Object> map = this.map();
        return map == null ? 0 : map.size();
    }

    public String toString() {
        ConcurrentMap<String, Object> map = this.map();
        return map == null ? "{}" : map.toString();
    }

    private Set<String> keySet() {
        ConcurrentMap<String, Object> map = this.map();
        return map == null ? Collections.emptySet() : map.keySet();
    }

    public void addAll(Attributes attributes) {
        Enumeration<String> e = attributes.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            this.setAttribute(name, attributes.getAttribute(name));
        }
    }

    @Override
    public String dump() {
        return Dumpable.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Dumpable.dumpObjects(out, indent, String.format("%s@%x", this.getClass().getSimpleName(), this.hashCode()), this.map());
    }
}

