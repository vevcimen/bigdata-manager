/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.sql.Timestamp;
import java.util.Calendar;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PGTimestamp
extends Timestamp {
    private static final long serialVersionUID = -6245623465210738466L;
    private @Nullable Calendar calendar;

    public PGTimestamp(long time) {
        this(time, null);
    }

    public PGTimestamp(long time, @Nullable Calendar calendar) {
        super(time);
        this.calendar = calendar;
    }

    public void setCalendar(@Nullable Calendar calendar) {
        this.calendar = calendar;
    }

    public @Nullable Calendar getCalendar() {
        return this.calendar;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.calendar == null ? 0 : this.calendar.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PGTimestamp that = (PGTimestamp)o;
        return this.calendar != null ? this.calendar.equals(that.calendar) : that.calendar == null;
    }

    @Override
    public Object clone() {
        PGTimestamp clone = (PGTimestamp)super.clone();
        Calendar calendar = this.getCalendar();
        if (calendar != null) {
            clone.setCalendar((Calendar)calendar.clone());
        }
        return clone;
    }
}

