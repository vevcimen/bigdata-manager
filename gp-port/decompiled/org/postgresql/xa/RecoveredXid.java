/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.xa;

import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.transaction.xa.Xid;
import org.checkerframework.checker.nullness.qual.Nullable;

class RecoveredXid
implements Xid {
    int formatId;
    byte[] globalTransactionId;
    byte[] branchQualifier;

    RecoveredXid(int formatId, byte[] globalTransactionId, byte[] branchQualifier) {
        this.formatId = formatId;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }

    @Override
    public int getFormatId() {
        return this.formatId;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return this.globalTransactionId;
    }

    @Override
    public byte[] getBranchQualifier() {
        return this.branchQualifier;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.branchQualifier);
        result = 31 * result + this.formatId;
        result = 31 * result + Arrays.hashCode(this.globalTransactionId);
        return result;
    }

    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Xid)) {
            return false;
        }
        Xid other = (Xid)o;
        return this.formatId == other.getFormatId() && Arrays.equals(this.globalTransactionId, other.getGlobalTransactionId()) && Arrays.equals(this.branchQualifier, other.getBranchQualifier());
    }

    public String toString() {
        return RecoveredXid.xidToString(this);
    }

    static String xidToString(Xid xid) {
        byte[] globalTransactionId = xid.getGlobalTransactionId();
        byte[] branchQualifier = xid.getBranchQualifier();
        StringBuilder sb = new StringBuilder((int)(16.0 + (double)globalTransactionId.length * 1.5 + (double)branchQualifier.length * 1.5));
        sb.append(xid.getFormatId()).append('_').append(Base64.getEncoder().encodeToString(globalTransactionId)).append('_').append(Base64.getEncoder().encodeToString(branchQualifier));
        return sb.toString();
    }

    static @Nullable Xid stringToXid(String s2) {
        int b;
        int a = s2.indexOf(95);
        if (a == (b = s2.lastIndexOf(95))) {
            return null;
        }
        try {
            int formatId = Integer.parseInt(s2.substring(0, a));
            byte[] globalTransactionId = Base64.getMimeDecoder().decode(s2.substring(a + 1, b));
            byte[] branchQualifier = Base64.getMimeDecoder().decode(s2.substring(b + 1));
            return new RecoveredXid(formatId, globalTransactionId, branchQualifier);
        }
        catch (Exception ex) {
            LogRecord logRecord = new LogRecord(Level.FINE, "XID String is invalid: [{0}]");
            logRecord.setParameters(new Object[]{s2});
            logRecord.setThrown(ex);
            Logger.getLogger(RecoveredXid.class.getName()).log(logRecord);
            return null;
        }
    }
}

