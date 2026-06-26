/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGmoney
extends PGobject
implements Serializable,
Cloneable {
    public double val;
    public boolean isNull;

    public PGmoney(double value) {
        this();
        this.val = value;
    }

    public PGmoney(String value) throws SQLException {
        this();
        this.setValue(value);
    }

    public PGmoney() {
        this.type = "money";
    }

    @Override
    public void setValue(@Nullable String s2) throws SQLException {
        boolean bl = this.isNull = s2 == null;
        if (s2 == null) {
            return;
        }
        try {
            boolean negative = s2.charAt(0) == '(';
            String s1 = PGtokenizer.removePara(s2).substring(1);
            int pos = s1.indexOf(44);
            while (pos != -1) {
                s1 = s1.substring(0, pos) + s1.substring(pos + 1);
                pos = s1.indexOf(44);
            }
            this.val = Double.parseDouble(s1);
            this.val = negative ? -this.val : this.val;
        }
        catch (NumberFormatException e) {
            throw new PSQLException(GT.tr("Conversion of money failed.", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE, (Throwable)e);
        }
    }

    @Override
    public int hashCode() {
        if (this.isNull) {
            return 0;
        }
        int prime = 31;
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(this.val);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PGmoney) {
            PGmoney p = (PGmoney)obj;
            if (this.isNull) {
                return p.isNull;
            }
            if (p.isNull) {
                return false;
            }
            return this.val == p.val;
        }
        return false;
    }

    @Override
    public @Nullable String getValue() {
        if (this.isNull) {
            return null;
        }
        if (this.val < 0.0) {
            return "-$" + -this.val;
        }
        return "$" + this.val;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

