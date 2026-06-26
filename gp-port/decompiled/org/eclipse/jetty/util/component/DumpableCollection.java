/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.jetty.util.component.Dumpable;

public class DumpableCollection
implements Dumpable {
    private final String _name;
    private final Collection<?> _collection;

    public DumpableCollection(String name, Collection<?> collection) {
        this._name = name;
        this._collection = collection;
    }

    public static DumpableCollection fromArray(String name, Object[] array) {
        return new DumpableCollection(name, array == null ? Collections.emptyList() : Arrays.asList(array));
    }

    public static DumpableCollection from(String name, Object ... items) {
        return new DumpableCollection(name, items == null ? Collections.emptyList() : Arrays.asList(items));
    }

    @Override
    public String dump() {
        return Dumpable.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Object[] array = this._collection == null ? null : this._collection.toArray();
        Dumpable.dumpObjects(out, indent, this._name + " size=" + (array == null ? 0 : array.length), array);
    }
}

