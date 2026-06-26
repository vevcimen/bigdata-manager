/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.lang.ref.PhantomReference;
import java.nio.charset.StandardCharsets;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.v3.SimpleQuery;

class Portal
implements ResultCursor {
    private final @Nullable SimpleQuery query;
    private final String portalName;
    private final byte[] encodedName;
    private @Nullable PhantomReference<?> cleanupRef;

    Portal(@Nullable SimpleQuery query, String portalName) {
        this.query = query;
        this.portalName = portalName;
        this.encodedName = portalName.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        if (this.cleanupRef != null) {
            this.cleanupRef.clear();
            this.cleanupRef.enqueue();
            this.cleanupRef = null;
        }
    }

    String getPortalName() {
        return this.portalName;
    }

    byte[] getEncodedPortalName() {
        return this.encodedName;
    }

    @Nullable SimpleQuery getQuery() {
        return this.query;
    }

    void setCleanupRef(PhantomReference<?> cleanupRef) {
        this.cleanupRef = cleanupRef;
    }

    public String toString() {
        return this.portalName;
    }
}

