/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FieldSet<T>
implements Cloneable {
    private List<T> fields = new ArrayList<T>();
    private List<FieldSet<T>> wrappedFieldSets;

    public FieldSet() {
        this.wrappedFieldSets = Collections.emptyList();
    }

    public FieldSet(List<FieldSet<T>> wrappedFieldSets) {
        this.wrappedFieldSets = wrappedFieldSets;
        if (this.wrappedFieldSets.contains(this)) {
            this.wrappedFieldSets.remove(this);
        }
    }

    public List<T> get() {
        return new ArrayList<T>(this.fields);
    }

    public FieldSet<T> set(T ... fields) {
        this.fields.clear();
        this.add(fields);
        for (FieldSet<T> wrapped : this.wrappedFieldSets) {
            wrapped.set(fields);
        }
        return this;
    }

    public FieldSet<T> add(T ... fields) {
        for (T field : fields) {
            this.addElement(field);
        }
        for (FieldSet<T> wrapped : this.wrappedFieldSets) {
            wrapped.add(fields);
        }
        return this;
    }

    private void addElement(T field) {
        this.fields.add(field);
    }

    public FieldSet<T> set(Collection<T> fields) {
        this.fields.clear();
        this.add(fields);
        for (FieldSet<T> wrapped : this.wrappedFieldSets) {
            wrapped.set(fields);
        }
        return this;
    }

    public FieldSet<T> add(Collection<T> fields) {
        for (T t : fields) {
            this.addElement(t);
        }
        for (FieldSet fieldSet : this.wrappedFieldSets) {
            fieldSet.add(fields);
        }
        return this;
    }

    public FieldSet<T> remove(T ... fields) {
        for (T field : fields) {
            this.fields.remove(field);
        }
        for (FieldSet<T> wrapped : this.wrappedFieldSets) {
            wrapped.remove(fields);
        }
        return this;
    }

    public FieldSet<T> remove(Collection<T> fields) {
        this.fields.removeAll(fields);
        for (FieldSet<T> wrapped : this.wrappedFieldSets) {
            wrapped.remove(fields);
        }
        return this;
    }

    public String describe() {
        return "field selection: " + this.fields.toString();
    }

    public String toString() {
        return this.fields.toString();
    }

    public FieldSet<T> clone() {
        try {
            FieldSet out = (FieldSet)super.clone();
            out.fields = new ArrayList<T>(this.fields);
            if (this.wrappedFieldSets != null) {
                out.wrappedFieldSets = new ArrayList<FieldSet<T>>();
                for (FieldSet<T> fieldSet : this.wrappedFieldSets) {
                    out.wrappedFieldSets.add((FieldSet<T>)fieldSet.clone());
                }
            }
            return out;
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}

