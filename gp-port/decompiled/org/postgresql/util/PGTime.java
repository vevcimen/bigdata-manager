/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.sql.Time;
import java.util.Calendar;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PGTime
extends Time {
    private static final long serialVersionUID = 3592492258676494276L;
    private @Nullable Calendar calendar;

    public PGTime(long time) {
        this(time, null);
    }

    public PGTime(long time, @Nullable Calendar calendar) {
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
        PGTime pgTime = (PGTime)o;
        return this.calendar != null ? this.calendar.equals(pgTime.calendar) : pgTime.calendar == null;
    }

    @Override
    public Object clone() {
        PGTime clone = (PGTime)super.clone();
        Calendar calendar = this.getCalendar();
        if (calendar != null) {
            clone.setCalendar((Calendar)calendar.clone());
        }
        return clone;
    }
}

