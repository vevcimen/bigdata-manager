/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.geometric;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.GT;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PGbox
extends PGobject
implements PGBinaryObject,
Serializable,
Cloneable {
    public PGpoint @Nullable [] point;

    public PGbox(double x1, double y1, double x2, double y2) {
        this(new PGpoint(x1, y1), new PGpoint(x2, y2));
    }

    public PGbox(PGpoint p1, PGpoint p2) {
        this();
        this.point = new PGpoint[]{p1, p2};
    }

    public PGbox(String s2) throws SQLException {
        this();
        this.setValue(s2);
    }

    public PGbox() {
        this.type = "box";
    }

    @Override
    public void setValue(@Nullable String value) throws SQLException {
        if (value == null) {
            this.point = null;
            return;
        }
        PGtokenizer t = new PGtokenizer(value, ',');
        if (t.getSize() != 2) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, value), PSQLState.DATA_TYPE_MISMATCH);
        }
        PGpoint[] point = this.point;
        if (point == null) {
            this.point = point = new PGpoint[2];
        }
        point[0] = new PGpoint(t.getToken(0));
        point[1] = new PGpoint(t.getToken(1));
    }

    @Override
    public void setByteValue(byte[] b, int offset) {
        PGpoint[] point = this.point;
        if (point == null) {
            this.point = point = new PGpoint[2];
        }
        point[0] = new PGpoint();
        point[0].setByteValue(b, offset);
        point[1] = new PGpoint();
        point[1].setByteValue(b, offset + point[0].lengthInBytes());
        this.point = point;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGbox) {
            PGbox p = (PGbox)obj;
            PGpoint[] point = this.point;
            PGpoint[] pPoint = p.point;
            if (point == null) {
                return pPoint == null;
            }
            if (pPoint == null) {
                return false;
            }
            if (pPoint[0].equals(point[0]) && pPoint[1].equals(point[1])) {
                return true;
            }
            if (pPoint[0].equals(point[1]) && pPoint[1].equals(point[0])) {
                return true;
            }
            if (pPoint[0].x == point[0].x && pPoint[0].y == point[1].y && pPoint[1].x == point[1].x && pPoint[1].y == point[0].y) {
                return true;
            }
            if (pPoint[0].x == point[1].x && pPoint[0].y == point[0].y && pPoint[1].x == point[0].x && pPoint[1].y == point[1].y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        PGpoint[] point = this.point;
        return point == null ? 0 : point[0].hashCode() ^ point[1].hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PGbox newPGbox = (PGbox)super.clone();
        if (newPGbox.point != null) {
            newPGbox.point = (PGpoint[])newPGbox.point.clone();
            for (int i = 0; i < newPGbox.point.length; ++i) {
                if (newPGbox.point[i] == null) continue;
                newPGbox.point[i] = (PGpoint)newPGbox.point[i].clone();
            }
        }
        return newPGbox;
    }

    @Override
    public @Nullable String getValue() {
        PGpoint[] point = this.point;
        return point == null ? null : point[0].toString() + "," + point[1].toString();
    }

    @Override
    public int lengthInBytes() {
        PGpoint[] point = this.point;
        if (point == null) {
            return 0;
        }
        return point[0].lengthInBytes() + point[1].lengthInBytes();
    }

    @Override
    public void toBytes(byte[] bytes, int offset) {
        PGpoint[] point = Nullness.castNonNull(this.point);
        point[0].toBytes(bytes, offset);
        point[1].toBytes(bytes, offset + point[0].lengthInBytes());
    }
}

