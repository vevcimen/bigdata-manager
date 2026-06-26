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

public class PGlseg
extends PGobject
implements Serializable,
Cloneable {
    public PGpoint @Nullable [] point;

    public PGlseg(double x1, double y1, double x2, double y2) {
        this(new PGpoint(x1, y1), new PGpoint(x2, y2));
    }

    public PGlseg(PGpoint p1, PGpoint p2) {
        this();
        this.point = new PGpoint[]{p1, p2};
    }

    public PGlseg(String s2) throws SQLException {
        this();
        this.setValue(s2);
    }

    public PGlseg() {
        this.type = "lseg";
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        if (s2 == null) {
            this.point = null;
            return;
        }
        PGtokenizer t = new PGtokenizer(PGtokenizer.removeBox(s2), ',');
        if (t.getSize() != 2) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s2), PSQLState.DATA_TYPE_MISMATCH);
        }
        PGpoint[] point = this.point;
        if (point == null) {
            this.point = point = new PGpoint[2];
        }
        point[0] = new PGpoint(t.getToken(0));
        point[1] = new PGpoint(t.getToken(1));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGlseg) {
            PGlseg p = (PGlseg)obj;
            PGpoint[] point = this.point;
            PGpoint[] pPoint = p.point;
            if (point == null) {
                return pPoint == null;
            }
            if (pPoint == null) {
                return false;
            }
            return pPoint[0].equals(point[0]) && pPoint[1].equals(point[1]) || pPoint[0].equals(point[1]) && pPoint[1].equals(point[0]);
        }
        return false;
    }

    @Override
    public int hashCode() {
        PGpoint[] point = this.point;
        if (point == null) {
            return 0;
        }
        return point[0].hashCode() ^ point[1].hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PGlseg newPGlseg = (PGlseg)super.clone();
        if (newPGlseg.point != null) {
            newPGlseg.point = (PGpoint[])newPGlseg.point.clone();
            for (int i = 0; i < newPGlseg.point.length; ++i) {
                if (newPGlseg.point[i] == null) continue;
                newPGlseg.point[i] = (PGpoint)newPGlseg.point[i].clone();
            }
        }
        return newPGlseg;
    }

    @Override
    public @Nullable String getValue() {
        PGpoint[] point = this.point;
        if (point == null) {
            return null;
        }
        return "[" + point[0] + "," + point[1] + "]";
    }
}

