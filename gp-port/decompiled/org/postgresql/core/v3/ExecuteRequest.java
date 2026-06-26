/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.v3.Portal;
import org.postgresql.core.v3.SimpleQuery;

class ExecuteRequest {
    public final SimpleQuery query;
    public final @Nullable Portal portal;
    public final boolean asSimple;

    ExecuteRequest(SimpleQuery query, @Nullable Portal portal, boolean asSimple) {
        this.query = query;
        this.portal = portal;
        this.asSimple = asSimple;
    }
}

