/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.LastErrorException
 *  com.sun.jna.WString
 *  com.sun.jna.ptr.IntByReference
 */
package org.postgresql.sspi;

import com.sun.jna.LastErrorException;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.sspi.NTDSAPI;

public class NTDSAPIWrapper {
    static final NTDSAPIWrapper instance = new NTDSAPIWrapper();

    public String DsMakeSpn(String serviceClass, String serviceName, @Nullable String instanceName, short instancePort, @Nullable String referrer) throws LastErrorException {
        char[] spn;
        IntByReference spnLength;
        int ret = NTDSAPI.instance.DsMakeSpnW(new WString(serviceClass), new WString(serviceName), instanceName == null ? null : new WString(instanceName), instancePort, referrer == null ? null : new WString(referrer), spnLength = new IntByReference(2048), spn = new char[spnLength.getValue()]);
        if (ret != 0) {
            throw new RuntimeException("NTDSAPI DsMakeSpn call failed with " + ret);
        }
        return new String(spn, 0, spnLength.getValue());
    }
}

