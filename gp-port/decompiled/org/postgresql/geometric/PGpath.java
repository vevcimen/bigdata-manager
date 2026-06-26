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

public class PGpath
extends PGobject
implements Serializable,
Cloneable {
    public boolean open;
    public PGpoint @Nullable [] points;

    public PGpath(PGpoint @Nullable [] points, boolean open) {
        this();
        this.points = points;
        this.open = open;
    }

    public PGpath() {
        this.type = "path";
    }

    public PGpath(String s2) throws SQLException {
        this();
        this.setValue(s2);
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        if (s2 == null) {
            this.points = null;
            return;
        }
        if (s2.startsWith("[") && s2.endsWith("]")) {
            this.open = true;
            s2 = PGtokenizer.removeBox(s2);
        } else if (s2.startsWith("(") && s2.endsWith(")")) {
            this.open = false;
            s2 = PGtokenizer.removePara(s2);
        } else {
            throw new PSQLException(GT.tr("Cannot tell if path is open or closed: {0}.", s2), PSQLState.DATA_TYPE_MISMATCH);
        }
        PGtokenizer t = new PGtokenizer(s2, ',');
        int npoints = t.getSize();
        PGpoint[] points = new PGpoint[npoints];
        this.points = points;
        for (int p = 0; p < npoints; ++p) {
            points[p] = new PGpoint(t.getToken(p));
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGpath) {
            PGpath p = (PGpath)obj;
            PGpoint[] points = this.points;
            PGpoint[] pPoints = p.points;
            if (points == null) {
                return pPoints == null;
            }
            if (pPoints == null) {
                return false;
            }
            if (p.open != this.open) {
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
        PGpoint[] points = this.points;
        if (points == null) {
            return 0;
        }
        int hash = this.open ? 1231 : 1237;
        for (int i = 0; i < points.length && i < 5; ++i) {
            hash = hash * 31 + points[i].hashCode();
        }
        return hash;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PGpath newPGpath = (PGpath)super.clone();
        if (newPGpath.points != null) {
            PGpoint[] newPoints = (PGpoint[])newPGpath.points.clone();
            newPGpath.points = newPoints;
            for (int i = 0; i < newPGpath.points.length; ++i) {
                newPoints[i] = (PGpoint)newPGpath.points[i].clone();
            }
        }
        return newPGpath;
    }

    @Override
    public @Nullable String getValue() {
        PGpoint[] points = this.points;
        if (points == null) {
            return null;
        }
        StringBuilder b = new StringBuilder(this.open ? "[" : "(");
        for (int p = 0; p < points.length; ++p) {
            if (p > 0) {
                b.append(",");
            }
            b.append(points[p].toString());
        }
        b.append(this.open ? "]" : ")");
        return b.toString();
    }

    public boolean isOpen() {
        return this.open && this.points != null;
    }

    public boolean isClosed() {
        return !this.open && this.points != null;
    }

    public void closePath() {
        this.open = false;
    }

    public void openPath() {
        this.open = true;
    }
}

