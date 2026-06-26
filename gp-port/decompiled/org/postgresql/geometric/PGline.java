/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.geometric;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.geometric.PGlseg;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGline
extends PGobject
implements Serializable,
Cloneable {
    public double a;
    public double b;
    public double c;
    private boolean isNull;

    public PGline(double a, double b, double c) {
        this();
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public PGline(double x1, double y1, double x2, double y2) {
        this();
        this.setValue(x1, y1, x2, y2);
    }

    public PGline(@Nullable PGpoint p1, @Nullable PGpoint p2) {
        this();
        this.setValue(p1, p2);
    }

    public PGline(@Nullable PGlseg lseg) {
        this();
        if (lseg == null) {
            this.isNull = true;
            return;
        }
        PGpoint[] point = lseg.point;
        if (point == null) {
            this.isNull = true;
            return;
        }
        this.setValue(point[0], point[1]);
    }

    private void setValue(@Nullable PGpoint p1, @Nullable PGpoint p2) {
        if (p1 == null || p2 == null) {
            this.isNull = true;
        } else {
            this.setValue(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void setValue(double x1, double y1, double x2, double y2) {
        if (x1 == x2) {
            this.a = -1.0;
            this.b = 0.0;
        } else {
            this.a = (y2 - y1) / (x2 - x1);
            this.b = -1.0;
        }
        this.c = y1 - this.a * x1;
    }

    public PGline(String s2) throws SQLException {
        this();
        this.setValue(s2);
    }

    public PGline() {
        this.type = "line";
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        boolean bl = this.isNull = s2 == null;
        if (s2 == null) {
            return;
        }
        if (s2.trim().startsWith("{")) {
            PGtokenizer t = new PGtokenizer(PGtokenizer.removeCurlyBrace(s2), ',');
            if (t.getSize() != 3) {
                throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s2), PSQLState.DATA_TYPE_MISMATCH);
            }
            this.a = Double.parseDouble(t.getToken(0));
            this.b = Double.parseDouble(t.getToken(1));
            this.c = Double.parseDouble(t.getToken(2));
        } else if (s2.trim().startsWith("[")) {
            PGtokenizer t = new PGtokenizer(PGtokenizer.removeBox(s2), ',');
            if (t.getSize() != 2) {
                throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s2), PSQLState.DATA_TYPE_MISMATCH);
            }
            PGpoint point1 = new PGpoint(t.getToken(0));
            PGpoint point2 = new PGpoint(t.getToken(1));
            this.a = point2.x - point1.x;
            this.b = point2.y - point1.y;
            this.c = point1.y;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        PGline pGline = (PGline)obj;
        if (this.isNull) {
            return pGline.isNull;
        }
        if (pGline.isNull) {
            return false;
        }
        return Double.compare(pGline.a, this.a) == 0 && Double.compare(pGline.b, this.b) == 0 && Double.compare(pGline.c, this.c) == 0;
    }

    @Override
    public int hashCode() {
        if (this.isNull) {
            return 0;
        }
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(this.a);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.b);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.c);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public @Nullable String getValue() {
        return this.isNull ? null : "{" + this.a + "," + this.b + "," + this.c + "}";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

