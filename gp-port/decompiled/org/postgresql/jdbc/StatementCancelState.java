/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

enum StatementCancelState {
    IDLE,
    IN_QUERY,
    CANCELING,
    CANCELLED;

}

