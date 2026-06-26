/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.QuotedCSV;
import org.eclipse.jetty.util.log.Log;

public class QuotedQualityCSV
extends QuotedCSV
implements Iterable<String> {
    public static ToIntFunction<String> MOST_SPECIFIC_MIME_ORDERING = s2 -> {
        if ("*/*".equals(s2)) {
            return 0;
        }
        if (s2.endsWith("/*")) {
            return 1;
        }
        if (s2.indexOf(59) < 0) {
            return 2;
        }
        return 3;
    };
    private final List<QualityValue> _qualities = new ArrayList<QualityValue>();
    private QualityValue _lastQuality;
    private boolean _sorted = false;
    private final ToIntFunction<String> _secondaryOrdering;

    public QuotedQualityCSV() {
        this((ToIntFunction<String>)null);
    }

    public QuotedQualityCSV(String[] preferredOrder) {
        this(s2 -> {
            for (int i = 0; i < preferredOrder.length; ++i) {
                if (!preferredOrder[i].equals(s2)) continue;
                return preferredOrder.length - i;
            }
            if ("*".equals(s2)) {
                return preferredOrder.length;
            }
            return 0;
        });
    }

    public QuotedQualityCSV(ToIntFunction<String> secondaryOrdering) {
        super(new String[0]);
        this._secondaryOrdering = secondaryOrdering == null ? s2 -> 0 : secondaryOrdering;
    }

    @Override
    protected void parsedValueAndParams(StringBuffer buffer) {
        super.parsedValueAndParams(buffer);
        this._lastQuality = new QualityValue(this._lastQuality._quality, buffer.toString(), this._lastQuality._index);
        this._qualities.set(this._lastQuality._index, this._lastQuality);
    }

    @Override
    protected void parsedValue(StringBuffer buffer) {
        super.parsedValue(buffer);
        this._sorted = false;
        this._lastQuality = new QualityValue(1.0, buffer.toString(), this._qualities.size());
        this._qualities.add(this._lastQuality);
    }

    @Override
    protected void parsedParam(StringBuffer buffer, int valueLength, int paramName, int paramValue) {
        this._sorted = false;
        if (paramName < 0) {
            if (buffer.charAt(buffer.length() - 1) == ';') {
                buffer.setLength(buffer.length() - 1);
            }
        } else if (paramValue >= 0 && buffer.charAt(paramName) == 'q' && paramValue > paramName && buffer.length() >= paramName && buffer.charAt(paramName + 1) == '=') {
            double q;
            try {
                q = this._keepQuotes && buffer.charAt(paramValue) == '\"' ? Double.valueOf(buffer.substring(paramValue + 1, buffer.length() - 1)) : Double.valueOf(buffer.substring(paramValue));
            }
            catch (Exception e) {
                Log.getLogger(QuotedQualityCSV.class).ignore(e);
                q = 0.0;
            }
            buffer.setLength(Math.max(0, paramName - 1));
            if (q != 1.0) {
                this._lastQuality = new QualityValue(q, buffer.toString(), this._lastQuality._index);
                this._qualities.set(this._lastQuality._index, this._lastQuality);
            }
        }
    }

    @Override
    public List<String> getValues() {
        if (!this._sorted) {
            this.sort();
        }
        return this._values;
    }

    @Override
    public Iterator<String> iterator() {
        if (!this._sorted) {
            this.sort();
        }
        return this._values.iterator();
    }

    protected void sort() {
        this._values.clear();
        this._qualities.stream().filter(qv -> ((QualityValue)qv)._quality != 0.0).sorted().map(rec$ -> ((QualityValue)rec$).getValue()).collect(Collectors.toCollection(() -> this._values));
        this._sorted = true;
    }

    private class QualityValue
    implements Comparable<QualityValue> {
        private final double _quality;
        private final String _value;
        private final int _index;

        private QualityValue(double quality, String value, int index) {
            this._quality = quality;
            this._value = value;
            this._index = index;
        }

        public int hashCode() {
            return Double.hashCode(this._quality) ^ Objects.hash(this._value, this._index);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof QualityValue)) {
                return false;
            }
            QualityValue qv = (QualityValue)obj;
            return this._quality == qv._quality && Objects.equals(this._value, qv._value) && Objects.equals(this._index, qv._index);
        }

        private String getValue() {
            return this._value;
        }

        @Override
        public int compareTo(QualityValue o) {
            int compare = Double.compare(o._quality, this._quality);
            if (compare == 0 && (compare = Integer.compare(QuotedQualityCSV.this._secondaryOrdering.applyAsInt(o._value), QuotedQualityCSV.this._secondaryOrdering.applyAsInt(this._value))) == 0) {
                compare = -Integer.compare(o._index, this._index);
            }
            return compare;
        }

        public String toString() {
            return String.format("%s@%x[%s,q=%f,i=%d]", this.getClass().getSimpleName(), this.hashCode(), this._value, this._quality, this._index);
        }
    }
}

