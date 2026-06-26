/*
 * Decompiled with CFR 0.152.
 */
package org.checkerframework.framework.qual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum LiteralKind {
    NULL,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    CHAR,
    STRING,
    ALL,
    PRIMITIVE;


    public static List<LiteralKind> allLiteralKinds() {
        ArrayList<LiteralKind> list = new ArrayList<LiteralKind>(Arrays.asList(LiteralKind.values()));
        list.remove((Object)ALL);
        list.remove((Object)PRIMITIVE);
        return list;
    }

    public static List<LiteralKind> primitiveLiteralKinds() {
        return new ArrayList<LiteralKind>(Arrays.asList(INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR));
    }
}

