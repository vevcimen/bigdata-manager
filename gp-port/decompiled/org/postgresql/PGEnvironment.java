/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum PGEnvironment {
    ORG_POSTGRESQL_PGPASSFILE("org.postgresql.pgpassfile", null, "Specified location of password file."),
    PGPASSFILE("PGPASSFILE", "pgpass", "Specified location of password file."),
    ORG_POSTGRESQL_PGSERVICEFILE("org.postgresql.pgservicefile", null, "Specifies the service resource to resolve connection properties."),
    PGSERVICEFILE("PGSERVICEFILE", "pg_service.conf", "Specifies the service resource to resolve connection properties."),
    PGSYSCONFDIR("PGSYSCONFDIR", null, "Specifies the directory containing the PGSERVICEFILE file");

    private final String name;
    private final @Nullable String defaultValue;
    private final String description;
    private static final Map<String, PGEnvironment> PROPS_BY_NAME;

    private PGEnvironment(String name, String defaultValue, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public @Nullable String getDefaultValue() {
        return this.defaultValue;
    }

    public String getDescription() {
        return this.description;
    }

    static {
        PROPS_BY_NAME = new HashMap<String, PGEnvironment>();
        for (PGEnvironment prop : PGEnvironment.values()) {
            if (PROPS_BY_NAME.put(prop.getName(), prop) == null) continue;
            throw new IllegalStateException("Duplicate PGProperty name: " + prop.getName());
        }
    }
}

