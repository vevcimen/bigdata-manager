/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.util;

import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public abstract class AbstractStringWritable
implements StringWritable {
    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}

