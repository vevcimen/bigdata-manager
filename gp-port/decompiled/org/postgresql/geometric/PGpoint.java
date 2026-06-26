/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.geometric;

import java.awt.Point;
import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.GT;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGpoint
extends PGobject
implements PGBinaryObject,
Serializable,
Cloneable {
    public double x;
    public double y;
    public boolean isNull;

    public PGpoint(double x, double y) {
        this();
        this.x = x;
        this.y = y;
    }

    public PGpoint(String value) throws SQLException {
        this();
        this.setValue(value);
    }

    public PGpoint() {
        this.type = "point";
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        boolean bl = this.isNull = s2 == null;
        if (s2 == null) {
            return;
        }
        PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(s2), ',');
        try {
            this.x = Double.parseDouble(t.getToken(0));
            this.y = Double.parseDouble(t.getToken(1));
        }
        catch (NumberFormatException e) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s2), PSQLState.DATA_TYPE_MISMATCH, (Throwable)e);
        }
    }

    @Override
    public void setByteValue(byte[] b, int offset) {
        this.isNull = false;
        this.x = ByteConverter.float8(b, offset);
        this.y = ByteConverter.float8(b, offset + 8);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGpoint) {
            PGpoint p = (PGpoint)obj;
            if (this.isNull) {
                return p.isNull;
            }
            if (p.isNull) {
                return false;
            }
            return this.x == p.x && this.y == p.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.isNull) {
            return 0;
        }
        long v1 = Double.doubleToLongBits(this.x);
        long v2 = Double.doubleToLongBits(this.y);
        return (int)(v1 ^ v2 ^ v1 >>> 32 ^ v2 >>> 32);
    }

    @Override
    public @Nullable String getValue() {
        return this.isNull ? null : "(" + this.x + "," + this.y + ")";
    }

    @Override
    public int lengthInBytes() {
        return this.isNull ? 0 : 16;
    }

    @Override
    public void toBytes(byte[] b, int offset) {
        if (this.isNull) {
            return;
        }
        ByteConverter.float8(b, offset, this.x);
        ByteConverter.float8(b, offset + 8, this.y);
    }

    public void translate(int x, int y) {
        this.translate((double)x, (double)y);
    }

    public void translate(double x, double y) {
        this.isNull = false;
        this.x += x;
        this.y += y;
    }

    public void move(int x, int y) {
        this.setLocation(x, y);
    }

    public void move(double x, double y) {
        this.isNull = false;
        this.x = x;
        this.y = y;
    }

    public void setLocation(int x, int y) {
        this.move((double)x, (double)y);
    }

    public void setLocation(Point p) {
        this.setLocation(p.x, p.y);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

