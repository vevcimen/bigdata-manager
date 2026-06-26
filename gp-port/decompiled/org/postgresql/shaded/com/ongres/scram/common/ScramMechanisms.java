/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.scram.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import org.postgresql.shaded.com.ongres.scram.common.ScramMechanism;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.DigestFactory;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.KeyParameter;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.PBEParametersGenerator;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.PKCS5S2ParametersGenerator;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;
import org.postgresql.shaded.com.ongres.scram.common.util.CryptoUtil;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public enum ScramMechanisms implements ScramMechanism
{
    SCRAM_SHA_1("SHA-1", "SHA-1", 160, "HmacSHA1", false, 1),
    SCRAM_SHA_1_PLUS("SHA-1", "SHA-1", 160, "HmacSHA1", true, 1),
    SCRAM_SHA_256("SHA-256", "SHA-256", 256, "HmacSHA256", false, 10),
    SCRAM_SHA_256_PLUS("SHA-256", "SHA-256", 256, "HmacSHA256", true, 10);

    private static final String SCRAM_MECHANISM_NAME_PREFIX = "SCRAM-";
    private static final String CHANNEL_BINDING_SUFFIX = "-PLUS";
    private static final String PBKDF2_PREFIX_ALGORITHM_NAME = "PBKDF2With";
    private static final Map<String, ScramMechanisms> BY_NAME_MAPPING;
    private final String mechanismName;
    private final String hashAlgorithmName;
    private final int keyLength;
    private final String hmacAlgorithmName;
    private final boolean channelBinding;
    private final int priority;

    private ScramMechanisms(String name, String hashAlgorithmName, int keyLength, String hmacAlgorithmName, boolean channelBinding, int priority) {
        this.mechanismName = SCRAM_MECHANISM_NAME_PREFIX + Preconditions.checkNotNull(name, "name") + (channelBinding ? CHANNEL_BINDING_SUFFIX : "");
        this.hashAlgorithmName = Preconditions.checkNotNull(hashAlgorithmName, "hashAlgorithmName");
        this.keyLength = Preconditions.gt0(keyLength, "keyLength");
        this.hmacAlgorithmName = Preconditions.checkNotNull(hmacAlgorithmName, "hmacAlgorithmName");
        this.channelBinding = channelBinding;
        this.priority = Preconditions.gt0(priority, "priority");
    }

    protected String getHashAlgorithmName() {
        return this.hashAlgorithmName;
    }

    protected String getHmacAlgorithmName() {
        return this.hmacAlgorithmName;
    }

    @Override
    public String getName() {
        return this.mechanismName;
    }

    @Override
    public boolean supportsChannelBinding() {
        return this.channelBinding;
    }

    @Override
    public int algorithmKeyLength() {
        return this.keyLength;
    }

    @Override
    public byte[] digest(byte[] message) {
        try {
            return MessageDigest.getInstance(this.hashAlgorithmName).digest(message);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + this.hashAlgorithmName + " not present in current JVM");
        }
    }

    @Override
    public byte[] hmac(byte[] key, byte[] message) {
        try {
            return CryptoUtil.hmac(new SecretKeySpec(key, this.hmacAlgorithmName), Mac.getInstance(this.hmacAlgorithmName), message);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MAC Algorithm " + this.hmacAlgorithmName + " not present in current JVM");
        }
    }

    @Override
    public byte[] saltedPassword(StringPreparation stringPreparation, String password, byte[] salt, int iterations) {
        char[] normalizedString = stringPreparation.normalize(password).toCharArray();
        try {
            return CryptoUtil.hi(SecretKeyFactory.getInstance(PBKDF2_PREFIX_ALGORITHM_NAME + this.hmacAlgorithmName), this.algorithmKeyLength(), normalizedString, salt, iterations);
        }
        catch (NoSuchAlgorithmException e) {
            if (!SCRAM_SHA_256.getHmacAlgorithmName().equals(this.getHmacAlgorithmName())) {
                throw new RuntimeException("Unsupported PBKDF2 for " + this.mechanismName);
            }
            PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(DigestFactory.createSHA256());
            generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(normalizedString), salt, iterations);
            KeyParameter params = (KeyParameter)((PBEParametersGenerator)generator).generateDerivedParameters(this.algorithmKeyLength());
            return params.getKey();
        }
    }

    public static ScramMechanisms byName(String name) {
        Preconditions.checkNotNull(name, "name");
        return BY_NAME_MAPPING.get(name);
    }

    public static ScramMechanism selectMatchingMechanism(boolean channelBinding, String ... peerMechanisms) {
        ScramMechanisms selectedScramMechanisms = null;
        for (String peerMechanism : peerMechanisms) {
            ScramMechanisms matchedScramMechanisms = BY_NAME_MAPPING.get(peerMechanism);
            if (matchedScramMechanisms == null) continue;
            for (ScramMechanisms candidateScramMechanisms : ScramMechanisms.values()) {
                if (channelBinding != candidateScramMechanisms.channelBinding || !candidateScramMechanisms.mechanismName.equals(matchedScramMechanisms.mechanismName) || selectedScramMechanisms != null && selectedScramMechanisms.priority >= candidateScramMechanisms.priority) continue;
                selectedScramMechanisms = candidateScramMechanisms;
            }
        }
        return selectedScramMechanisms;
    }

    private static Map<String, ScramMechanisms> valuesAsMap() {
        HashMap<String, ScramMechanisms> mapScramMechanisms = new HashMap<String, ScramMechanisms>(ScramMechanisms.values().length);
        for (ScramMechanisms scramMechanisms : ScramMechanisms.values()) {
            mapScramMechanisms.put(scramMechanisms.getName(), scramMechanisms);
        }
        return mapScramMechanisms;
    }

    static {
        BY_NAME_MAPPING = ScramMechanisms.valuesAsMap();
    }
}

