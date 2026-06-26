/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jetty.http.QuotedCSVParser;

public class QuotedCSV
extends QuotedCSVParser
implements Iterable<String> {
    protected final List<String> _values = new ArrayList<String>();

    public QuotedCSV(String ... values) {
        this(true, values);
    }

    public QuotedCSV(boolean keepQuotes, String ... values) {
        super(keepQuotes);
        for (String v : values) {
            this.addValue(v);
        }
    }

    @Override
    protected void parsedValueAndParams(StringBuffer buffer) {
        this._values.add(buffer.toString());
    }

    public int size() {
        return this._values.size();
    }

    public boolean isEmpty() {
        return this._values.isEmpty();
    }

    public List<String> getValues() {
        return this._values;
    }

    @Override
    public Iterator<String> iterator() {
        return this._values.iterator();
    }

    public String toString() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s2 : this) {
            list.add(s2);
        }
        return ((Object)list).toString();
    }
}

