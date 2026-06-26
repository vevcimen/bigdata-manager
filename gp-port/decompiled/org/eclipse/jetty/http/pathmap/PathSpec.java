/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http.pathmap;

import org.eclipse.jetty.http.pathmap.PathSpecGroup;

public interface PathSpec
extends Comparable<PathSpec> {
    public int getSpecLength();

    public PathSpecGroup getGroup();

    public int getPathDepth();

    public String getPathInfo(String var1);

    public String getPathMatch(String var1);

    public String getDeclaration();

    public String getPrefix();

    public String getSuffix();

    public boolean matches(String var1);
}

