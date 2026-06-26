/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public interface RangeWriter
extends Closeable {
    public void writeTo(OutputStream var1, long var2, long var4) throws IOException;
}

