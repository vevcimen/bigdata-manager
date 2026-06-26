/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.geometric;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGcircle
extends PGobject
implements Serializable,
Cloneable {
    public @Nullable PGpoint center;
    public double radius;

    public PGcircle(double x, double y, double r) {
        this(new PGpoint(x, y), r);
    }

    public PGcircle(PGpoint c, double r) {
        this();
        this.center = c;
        this.radius = r;
    }

    public PGcircle(String s2) throws SQLException {
        this();
        this.setValue(s2);
    }

    public PGcircle() {
        this.type = "circle";
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        if (s2 == null) {
            this.center = null;
            return;
        }
        PGtokenizer t = new PGtokenizer(PGtokenizer.removeAngle(s2), ',');
        if (t.getSize() != 2) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s2), PSQLState.DATA_TYPE_MISMATCH);
        }
        try {
            this.center = new PGpoint(t.getToken(0));
            this.radius = Double.parseDouble(t.getToken(1));
        }
        catch (NumberFormatException e) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s2), PSQLState.DATA_TYPE_MISMATCH, (Throwable)e);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGcircle) {
            PGcircle p = (PGcircle)obj;
            PGpoint center = this.center;
            PGpoint pCenter = p.center;
            if (center == null) {
                return pCenter == null;
            }
            if (pCenter == null) {
                return false;
            }
            return p.radius == this.radius && PGcircle.equals(pCenter, center);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.center == null) {
            return 0;
        }
        long bits = Double.doubleToLongBits(this.radius);
        int v = (int)(bits ^ bits >>> 32);
        v = v * 31 + this.center.hashCode();
        return v;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PGcircle newPGcircle = (PGcircle)super.clone();
        if (newPGcircle.center != null) {
            newPGcircle.center = (PGpoint)newPGcircle.center.clone();
        }
        return newPGcircle;
    }

    @Override
    public @Nullable String getValue() {
        return this.center == null ? null : "<" + this.center + "," + this.radius + ">";
    }
}

