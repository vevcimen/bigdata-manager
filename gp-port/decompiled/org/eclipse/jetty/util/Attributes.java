/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

public interface Attributes {
    public void removeAttribute(String var1);

    public void setAttribute(String var1, Object var2);

    public Object getAttribute(String var1);

    public Set<String> getAttributeNameSet();

    default public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.getAttributeNameSet());
    }

    public void clearAttributes();

    public static Attributes unwrap(Attributes attributes) {
        while (attributes instanceof Wrapper) {
            attributes = ((Wrapper)attributes).getAttributes();
        }
        return attributes;
    }

    public static abstract class Wrapper
    implements Attributes {
        protected final Attributes _attributes;

        public Wrapper(Attributes attributes) {
            this._attributes = attributes;
        }

        public Attributes getAttributes() {
            return this._attributes;
        }

        @Override
        public void removeAttribute(String name) {
            this._attributes.removeAttribute(name);
        }

        @Override
        public void setAttribute(String name, Object attribute) {
            this._attributes.setAttribute(name, attribute);
        }

        @Override
        public Object getAttribute(String name) {
            return this._attributes.getAttribute(name);
        }

        @Override
        public Set<String> getAttributeNameSet() {
            return this._attributes.getAttributeNameSet();
        }

        @Override
        public void clearAttributes() {
            this._attributes.clearAttributes();
        }
    }
}

