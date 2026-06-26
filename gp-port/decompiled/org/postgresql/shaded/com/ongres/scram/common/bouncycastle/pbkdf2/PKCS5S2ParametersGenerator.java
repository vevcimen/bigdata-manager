/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Arrays;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.CipherParameters;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Digest;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.HMac;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.KeyParameter;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Mac;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.PBEParametersGenerator;

public class PKCS5S2ParametersGenerator
extends PBEParametersGenerator {
    private Mac hMac;
    private byte[] state;

    public PKCS5S2ParametersGenerator(Digest digest) {
        this.hMac = new HMac(digest);
        this.state = new byte[this.hMac.getMacSize()];
    }

    private void F(byte[] S, int c, byte[] iBuf, byte[] out, int outOff) {
        if (c == 0) {
            throw new IllegalArgumentException("iteration count must be at least 1.");
        }
        if (S != null) {
            this.hMac.update(S, 0, S.length);
        }
        this.hMac.update(iBuf, 0, iBuf.length);
        this.hMac.doFinal(this.state, 0);
        System.arraycopy(this.state, 0, out, outOff, this.state.length);
        for (int count = 1; count < c; ++count) {
            this.hMac.update(this.state, 0, this.state.length);
            this.hMac.doFinal(this.state, 0);
            for (int j = 0; j != this.state.length; ++j) {
                int n = outOff + j;
                out[n] = (byte)(out[n] ^ this.state[j]);
            }
        }
    }

    private byte[] generateDerivedKey(int dkLen) {
        int hLen = this.hMac.getMacSize();
        int l = (dkLen + hLen - 1) / hLen;
        byte[] iBuf = new byte[4];
        byte[] outBytes = new byte[l * hLen];
        int outPos = 0;
        KeyParameter param = new KeyParameter(this.password);
        this.hMac.init(param);
        for (int i = 1; i <= l; ++i) {
            int n;
            int pos = 3;
            do {
                n = pos--;
            } while ((iBuf[n] = (byte)(iBuf[n] + 1)) == 0);
            this.F(this.salt, this.iterationCount, iBuf, outBytes, outPos);
            outPos += hLen;
        }
        return outBytes;
    }

    @Override
    public CipherParameters generateDerivedParameters(int keySize) {
        byte[] dKey = Arrays.copyOfRange(this.generateDerivedKey(keySize /= 8), 0, keySize);
        return new KeyParameter(dKey, 0, keySize);
    }
}

