/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class ByteConverter {
    private static final int NUMERIC_DSCALE_MASK = 16383;
    private static final short NUMERIC_POS = 0;
    private static final short NUMERIC_NEG = 16384;
    private static final short NUMERIC_NAN = -16384;
    private static final int SHORT_BYTES = 2;
    private static final int LONG_BYTES = 4;
    private static final int[] INT_TEN_POWERS;
    private static final long[] LONG_TEN_POWERS;
    private static final BigInteger[] BI_TEN_POWERS;
    private static final BigInteger BI_TEN_THOUSAND;
    private static final BigInteger BI_MAX_LONG;

    private ByteConverter() {
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length == 1) {
            return bytes[0];
        }
        if (bytes.length == 2) {
            return ByteConverter.int2(bytes, 0);
        }
        if (bytes.length == 4) {
            return ByteConverter.int4(bytes, 0);
        }
        throw new IllegalArgumentException("Argument bytes is empty");
    }

    public static Number numeric(byte[] bytes) {
        return ByteConverter.numeric(bytes, 0, bytes.length);
    }

    public static Number numeric(byte[] bytes, int pos, int numBytes) {
        if (numBytes < 8) {
            throw new IllegalArgumentException("number of bytes should be at-least 8");
        }
        int len = ByteConverter.int2(bytes, pos);
        int weight = ByteConverter.int2(bytes, pos + 2);
        short sign = ByteConverter.int2(bytes, pos + 4);
        int scale = ByteConverter.int2(bytes, pos + 6);
        if (numBytes != len * 2 + 8) {
            throw new IllegalArgumentException("invalid length of bytes \"numeric\" value");
        }
        if (sign != 0 && sign != 16384 && sign != -16384) {
            throw new IllegalArgumentException("invalid sign in \"numeric\" value");
        }
        if (sign == -16384) {
            return Double.NaN;
        }
        if ((scale & 0x3FFF) != scale) {
            throw new IllegalArgumentException("invalid scale in \"numeric\" value");
        }
        if (len == 0) {
            return new BigDecimal(BigInteger.ZERO, scale);
        }
        int idx = pos + 8;
        short d = ByteConverter.int2(bytes, idx);
        if (weight < 0) {
            int i;
            assert (scale > 0);
            int effectiveScale = scale;
            if ((weight = (short)(weight + 1)) < 0) {
                effectiveScale += 4 * weight;
            }
            for (i = 1; i < len && d == 0; ++i) {
                effectiveScale -= 4;
                d = ByteConverter.int2(bytes, idx += 2);
            }
            assert (effectiveScale > 0);
            if (effectiveScale >= 4) {
                effectiveScale -= 4;
            } else {
                d = (short)(d / INT_TEN_POWERS[4 - effectiveScale]);
                effectiveScale = 0;
            }
            BigInteger unscaledBI = null;
            long unscaledInt = d;
            while (i < len) {
                if (i == 4 && effectiveScale > 2) {
                    unscaledBI = BigInteger.valueOf(unscaledInt);
                }
                d = ByteConverter.int2(bytes, idx += 2);
                if (effectiveScale >= 4) {
                    if (unscaledBI == null) {
                        unscaledInt *= 10000L;
                    } else {
                        unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                    }
                    effectiveScale -= 4;
                } else {
                    if (unscaledBI == null) {
                        unscaledInt *= (long)INT_TEN_POWERS[effectiveScale];
                    } else {
                        unscaledBI = unscaledBI.multiply(ByteConverter.tenPower(effectiveScale));
                    }
                    d = (short)(d / INT_TEN_POWERS[4 - effectiveScale]);
                    effectiveScale = 0;
                }
                if (unscaledBI == null) {
                    unscaledInt += (long)d;
                } else if (d != 0) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
                ++i;
            }
            if (unscaledBI == null) {
                unscaledBI = BigInteger.valueOf(unscaledInt);
            }
            if (effectiveScale > 0) {
                unscaledBI = unscaledBI.multiply(ByteConverter.tenPower(effectiveScale));
            }
            if (sign == 16384) {
                unscaledBI = unscaledBI.negate();
            }
            return new BigDecimal(unscaledBI, scale);
        }
        if (scale == 0) {
            int bigDecScale;
            BigInteger unscaledBI = null;
            long unscaledInt = d;
            for (int i = 1; i < len; ++i) {
                if (i == 4) {
                    unscaledBI = BigInteger.valueOf(unscaledInt);
                }
                d = ByteConverter.int2(bytes, idx += 2);
                if (unscaledBI == null) {
                    unscaledInt *= 10000L;
                    unscaledInt += (long)d;
                    continue;
                }
                unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                if (d == 0) continue;
                unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
            }
            if (unscaledBI == null) {
                unscaledBI = BigInteger.valueOf(unscaledInt);
            }
            if (sign == 16384) {
                unscaledBI = unscaledBI.negate();
            }
            return (bigDecScale = (len - (weight + 1)) * 4) == 0 ? new BigDecimal(unscaledBI) : new BigDecimal(unscaledBI, bigDecScale).setScale(0);
        }
        BigInteger unscaledBI = null;
        long unscaledInt = d;
        int effectiveWeight = weight;
        int effectiveScale = scale;
        for (int i = 1; i < len; ++i) {
            if (i == 4) {
                unscaledBI = BigInteger.valueOf(unscaledInt);
            }
            d = ByteConverter.int2(bytes, idx += 2);
            if (effectiveWeight > 0) {
                --effectiveWeight;
                if (unscaledBI == null) {
                    unscaledInt *= 10000L;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
            } else if (effectiveScale >= 4) {
                effectiveScale -= 4;
                if (unscaledBI == null) {
                    unscaledInt *= 10000L;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
            } else {
                if (unscaledBI == null) {
                    unscaledInt *= (long)INT_TEN_POWERS[effectiveScale];
                } else {
                    unscaledBI = unscaledBI.multiply(ByteConverter.tenPower(effectiveScale));
                }
                d = (short)(d / INT_TEN_POWERS[4 - effectiveScale]);
                effectiveScale = 0;
            }
            if (unscaledBI == null) {
                unscaledInt += (long)d;
                continue;
            }
            if (d == 0) continue;
            unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
        }
        if (unscaledBI == null) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (effectiveWeight > 0) {
            unscaledBI = unscaledBI.multiply(ByteConverter.tenPower(effectiveWeight * 4));
        }
        if (effectiveScale > 0) {
            unscaledBI = unscaledBI.multiply(ByteConverter.tenPower(effectiveScale));
        }
        if (sign == 16384) {
            unscaledBI = unscaledBI.negate();
        }
        return new BigDecimal(unscaledBI, scale);
    }

    public static byte[] numeric(BigDecimal nbr) {
        short s2;
        PositiveShorts shorts = new PositiveShorts();
        BigInteger unscaled = nbr.unscaledValue().abs();
        int scale = nbr.scale();
        if (unscaled.equals(BigInteger.ZERO)) {
            byte[] bytes = new byte[]{0, 0, -1, -1, 0, 0, 0, 0};
            ByteConverter.int2(bytes, 6, Math.max(0, scale));
            return bytes;
        }
        int weight = -1;
        if (scale <= 0) {
            if (scale < 0) {
                scale = Math.abs(scale);
                weight += scale / 4;
                int mod = scale % 4;
                unscaled = unscaled.multiply(ByteConverter.tenPower(mod));
                scale = 0;
            }
            while (unscaled.compareTo(BI_MAX_LONG) > 0) {
                BigInteger[] pair = unscaled.divideAndRemainder(BI_TEN_THOUSAND);
                unscaled = pair[0];
                short shortValue = pair[1].shortValue();
                if (shortValue != 0 || !shorts.isEmpty()) {
                    shorts.push(shortValue);
                }
                ++weight;
            }
            long unscaledLong = unscaled.longValueExact();
            do {
                short shortValue;
                if ((shortValue = (short)(unscaledLong % 10000L)) != 0 || !shorts.isEmpty()) {
                    shorts.push(shortValue);
                }
                ++weight;
            } while ((unscaledLong /= 10000L) != 0L);
        } else {
            BigInteger[] split = unscaled.divideAndRemainder(ByteConverter.tenPower(scale));
            BigInteger decimal = split[1];
            BigInteger wholes = split[0];
            weight = -1;
            if (!BigInteger.ZERO.equals(decimal)) {
                int mod = scale % 4;
                int segments = scale / 4;
                if (mod != 0) {
                    decimal = decimal.multiply(ByteConverter.tenPower(4 - mod));
                    ++segments;
                }
                do {
                    BigInteger[] pair = decimal.divideAndRemainder(BI_TEN_THOUSAND);
                    decimal = pair[0];
                    short shortValue = pair[1].shortValue();
                    if (shortValue != 0 || !shorts.isEmpty()) {
                        shorts.push(shortValue);
                    }
                    --segments;
                } while (!BigInteger.ZERO.equals(decimal));
                if (BigInteger.ZERO.equals(wholes)) {
                    weight -= segments;
                } else {
                    for (int i = 0; i < segments; ++i) {
                        shorts.push((short)0);
                    }
                }
            }
            while (!BigInteger.ZERO.equals(wholes)) {
                ++weight;
                BigInteger[] pair = wholes.divideAndRemainder(BI_TEN_THOUSAND);
                wholes = pair[0];
                short shortValue = pair[1].shortValue();
                if (shortValue == 0 && shorts.isEmpty()) continue;
                shorts.push(shortValue);
            }
        }
        byte[] bytes = new byte[8 + 2 * shorts.size()];
        int idx = 0;
        ByteConverter.int2(bytes, idx, shorts.size());
        ByteConverter.int2(bytes, idx += 2, weight);
        ByteConverter.int2(bytes, idx += 2, nbr.signum() == -1 ? 16384 : 0);
        ByteConverter.int2(bytes, idx += 2, Math.max(0, scale));
        idx += 2;
        while ((s2 = shorts.pop()) != -1) {
            ByteConverter.int2(bytes, idx, s2);
            idx += 2;
        }
        return bytes;
    }

    private static BigInteger tenPower(int exponent) {
        return BI_TEN_POWERS.length > exponent ? BI_TEN_POWERS[exponent] : BigInteger.TEN.pow(exponent);
    }

    public static long int8(byte[] bytes, int idx) {
        return ((long)(bytes[idx + 0] & 0xFF) << 56) + ((long)(bytes[idx + 1] & 0xFF) << 48) + ((long)(bytes[idx + 2] & 0xFF) << 40) + ((long)(bytes[idx + 3] & 0xFF) << 32) + ((long)(bytes[idx + 4] & 0xFF) << 24) + ((long)(bytes[idx + 5] & 0xFF) << 16) + ((long)(bytes[idx + 6] & 0xFF) << 8) + (long)(bytes[idx + 7] & 0xFF);
    }

    public static int int4(byte[] bytes, int idx) {
        return ((bytes[idx] & 0xFF) << 24) + ((bytes[idx + 1] & 0xFF) << 16) + ((bytes[idx + 2] & 0xFF) << 8) + (bytes[idx + 3] & 0xFF);
    }

    public static short int2(byte[] bytes, int idx) {
        return (short)(((bytes[idx] & 0xFF) << 8) + (bytes[idx + 1] & 0xFF));
    }

    public static boolean bool(byte[] bytes, int idx) {
        return bytes[idx] == 1;
    }

    public static float float4(byte[] bytes, int idx) {
        return Float.intBitsToFloat(ByteConverter.int4(bytes, idx));
    }

    public static double float8(byte[] bytes, int idx) {
        return Double.longBitsToDouble(ByteConverter.int8(bytes, idx));
    }

    public static void int8(byte[] target, int idx, long value) {
        target[idx + 0] = (byte)(value >>> 56);
        target[idx + 1] = (byte)(value >>> 48);
        target[idx + 2] = (byte)(value >>> 40);
        target[idx + 3] = (byte)(value >>> 32);
        target[idx + 4] = (byte)(value >>> 24);
        target[idx + 5] = (byte)(value >>> 16);
        target[idx + 6] = (byte)(value >>> 8);
        target[idx + 7] = (byte)value;
    }

    public static void int4(byte[] target, int idx, int value) {
        target[idx + 0] = (byte)(value >>> 24);
        target[idx + 1] = (byte)(value >>> 16);
        target[idx + 2] = (byte)(value >>> 8);
        target[idx + 3] = (byte)value;
    }

    public static void int2(byte[] target, int idx, int value) {
        target[idx + 0] = (byte)(value >>> 8);
        target[idx + 1] = (byte)value;
    }

    public static void bool(byte[] target, int idx, boolean value) {
        target[idx] = value ? (byte)1 : 0;
    }

    public static void float4(byte[] target, int idx, float value) {
        ByteConverter.int4(target, idx, Float.floatToRawIntBits(value));
    }

    public static void float8(byte[] target, int idx, double value) {
        ByteConverter.int8(target, idx, Double.doubleToRawLongBits(value));
    }

    static {
        int i;
        INT_TEN_POWERS = new int[6];
        LONG_TEN_POWERS = new long[19];
        BI_TEN_POWERS = new BigInteger[32];
        BI_TEN_THOUSAND = BigInteger.valueOf(10000L);
        BI_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
        for (i = 0; i < INT_TEN_POWERS.length; ++i) {
            ByteConverter.INT_TEN_POWERS[i] = (int)Math.pow(10.0, i);
        }
        for (i = 0; i < LONG_TEN_POWERS.length; ++i) {
            ByteConverter.LONG_TEN_POWERS[i] = (long)Math.pow(10.0, i);
        }
        for (i = 0; i < BI_TEN_POWERS.length; ++i) {
            ByteConverter.BI_TEN_POWERS[i] = BigInteger.TEN.pow(i);
        }
    }

    private static final class PositiveShorts {
        private short[] shorts = new short[8];
        private int idx = 0;

        PositiveShorts() {
        }

        public void push(short s2) {
            if (s2 < 0) {
                throw new IllegalArgumentException("only non-negative values accepted: " + s2);
            }
            if (this.idx == this.shorts.length) {
                this.grow();
            }
            this.shorts[this.idx++] = s2;
        }

        public int size() {
            return this.idx;
        }

        public boolean isEmpty() {
            return this.idx == 0;
        }

        public short pop() {
            return this.idx > 0 ? this.shorts[--this.idx] : (short)-1;
        }

        private void grow() {
            int newSize = this.shorts.length <= 1024 ? this.shorts.length << 1 : (int)((double)this.shorts.length * 1.5);
            this.shorts = Arrays.copyOf(this.shorts, newSize);
        }
    }
}

