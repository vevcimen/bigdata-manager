/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.internal.Nullness;

public class PGobject
implements Serializable,
Cloneable {
    protected @Nullable String type;
    protected @Nullable String value;

    public final void setType(String type) {
        this.type = type;
    }

    public void setValue(@Nullable String value) throws SQLException {
        this.value = value;
    }

    public final String getType() {
        return Nullness.castNonNull(this.type, "PGobject#type is uninitialized. Please call setType(String)");
    }

    public @Nullable String getValue() {
        return this.value;
    }

    public boolean isNull() {
        return this.getValue() == null;
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGobject) {
            String otherValue = ((PGobject)obj).getValue();
            if (otherValue == null) {
                return this.getValue() == null;
            }
            return otherValue.equals(this.getValue());
        }
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        return this.getValue();
    }

    public int hashCode() {
        String value = this.getValue();
        return value != null ? value.hashCode() : 0;
    }

    protected static boolean equals(@Nullable Object a, @Nullable Object b) {
        return a == b || a != null && a.equals(b);
    }
}

