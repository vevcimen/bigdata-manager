/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.geometric;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

public class PGpolygon
extends PGobject
implements Serializable,
Cloneable {
    public PGpoint @Nullable [] points;

    public PGpolygon(PGpoint[] points) {
        this();
        this.points = points;
    }

    public PGpolygon(String s2) throws SQLException {
        this();
        this.setValue(s2);
    }

    public PGpolygon() {
        this.type = "polygon";
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        if (s2 == null) {
            this.points = null;
            return;
        }
        PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(s2), ',');
        int npoints = t.getSize();
        PGpoint[] points = this.points;
        if (points == null || points.length != npoints) {
            this.points = points = new PGpoint[npoints];
        }
        for (int p = 0; p < npoints; ++p) {
            points[p] = new PGpoint(t.getToken(p));
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGpolygon) {
            PGpolygon p = (PGpolygon)obj;
            PGpoint[] points = this.points;
            PGpoint[] pPoints = p.points;
            if (points == null) {
                return pPoints == null;
            }
            if (pPoints == null) {
                return false;
            }
            if (pPoints.length != points.length) {
                return false;
            }
            for (int i = 0; i < points.length; ++i) {
                if (points[i].equals(pPoints[i])) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        PGpoint[] points = this.points;
        if (points == null) {
            return hash;
        }
        for (int i = 0; i < points.length && i < 5; ++i) {
            hash = hash * 31 + points[i].hashCode();
        }
        return hash;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PGpolygon newPGpolygon = (PGpolygon)super.clone();
        if (newPGpolygon.points != null) {
            PGpoint[] newPoints = (PGpoint[])newPGpolygon.points.clone();
            newPGpolygon.points = newPoints;
            for (int i = 0; i < newPGpolygon.points.length; ++i) {
                if (newPGpolygon.points[i] == null) continue;
                newPoints[i] = (PGpoint)newPGpolygon.points[i].clone();
            }
        }
        return newPGpolygon;
    }

    @Override
    public @Nullable String getValue() {
        PGpoint[] points = this.points;
        if (points == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        b.append("(");
        for (int p = 0; p < points.length; ++p) {
            if (p > 0) {
                b.append(",");
            }
            b.append(points[p].toString());
        }
        b.append(")");
        return b.toString();
    }
}

