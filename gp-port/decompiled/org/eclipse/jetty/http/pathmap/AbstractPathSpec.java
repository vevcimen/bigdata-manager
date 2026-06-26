/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http.pathmap;

import java.util.Objects;
import org.eclipse.jetty.http.pathmap.PathSpec;

public abstract class AbstractPathSpec
implements PathSpec {
    @Override
    public int compareTo(PathSpec other) {
        int diff = this.getGroup().ordinal() - other.getGroup().ordinal();
        if (diff != 0) {
            return diff;
        }
        diff = other.getSpecLength() - this.getSpecLength();
        if (diff != 0) {
            return diff;
        }
        return this.getDeclaration().compareTo(other.getDeclaration());
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return this.compareTo((AbstractPathSpec)obj) == 0;
    }

    public final int hashCode() {
        return Objects.hash(this.getDeclaration());
    }

    public String toString() {
        return String.format("%s@%s{%s}", this.getClass().getSimpleName(), Integer.toHexString(this.hashCode()), this.getDeclaration());
    }
}

