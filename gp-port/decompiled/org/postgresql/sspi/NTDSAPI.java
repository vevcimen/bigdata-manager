/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.LastErrorException
 *  com.sun.jna.Native
 *  com.sun.jna.WString
 *  com.sun.jna.ptr.IntByReference
 *  com.sun.jna.win32.StdCallLibrary
 */
package org.postgresql.sspi;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.checkerframework.checker.nullness.qual.Nullable;

interface NTDSAPI
extends StdCallLibrary {
    public static final NTDSAPI instance = (NTDSAPI)Native.loadLibrary((String)"NTDSAPI", NTDSAPI.class);
    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_INVALID_PARAMETER = 87;
    public static final int ERROR_BUFFER_OVERFLOW = 111;

    public int DsMakeSpnW(WString var1, WString var2, @Nullable WString var3, short var4, @Nullable WString var5, IntByReference var6, char[] var7) throws LastErrorException;
}

