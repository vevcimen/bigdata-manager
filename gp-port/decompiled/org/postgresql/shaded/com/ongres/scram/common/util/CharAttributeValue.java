/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.util;

import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;
import org.postgresql.shaded.com.ongres.scram.common.util.StringWritable;

public interface CharAttributeValue
extends CharAttribute,
StringWritable {
    public String getValue();
}

