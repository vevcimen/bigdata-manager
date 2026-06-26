/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.Arrays;
import java.util.Comparator;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.processor.CustomMatcher;
import shadeio.univocity.parsers.common.processor.core.AbstractProcessorSwitch;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractInputValueSwitch<T extends Context>
extends AbstractProcessorSwitch<T> {
    private int columnIndex = -1;
    private NormalizedString columnName = null;
    private Switch[] switches = new Switch[0];
    private Switch defaultSwitch = null;
    private String[] headers;
    private int[] indexes;
    private static final Comparator<String> caseSensitiveComparator = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return o1 == o2 || o1 != null && o1.equals(o2) ? 0 : 1;
        }
    };
    private static final Comparator<String> caseInsensitiveComparator = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return o1 == o2 || o1 != null && o1.equalsIgnoreCase(o2) ? 0 : 1;
        }
    };
    private Comparator<String> comparator = caseInsensitiveComparator;

    public AbstractInputValueSwitch() {
        this(0);
    }

    public AbstractInputValueSwitch(int columnIndex) {
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Column index must be positive");
        }
        this.columnIndex = columnIndex;
    }

    public AbstractInputValueSwitch(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be blank");
        }
        this.columnName = NormalizedString.valueOf(columnName);
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.comparator = caseSensitive ? caseSensitiveComparator : caseInsensitiveComparator;
    }

    public void setComparator(Comparator<String> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        this.comparator = comparator;
    }

    public void setDefaultSwitch(Processor<T> processor, String ... headersToUse) {
        this.defaultSwitch = new Switch<T>(processor, headersToUse, null, null, null);
    }

    public void setDefaultSwitch(Processor<T> processor) {
        this.defaultSwitch = new Switch<T>(processor, null, null, null, null);
    }

    public void setDefaultSwitch(Processor<T> processor, int ... indexesToUse) {
        this.defaultSwitch = new Switch<T>(processor, null, indexesToUse, null, null);
    }

    public boolean hasDefaultSwitch() {
        return this.defaultSwitch != null;
    }

    public void addSwitchForValue(String value, Processor<T> processor) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = new Switch<T>(processor, null, null, value, null);
    }

    public void addSwitchForValue(String value, Processor<T> processor, String ... headersToUse) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = new Switch<T>(processor, headersToUse, null, value, null);
    }

    public void addSwitchForValue(CustomMatcher matcher, Processor<T> processor) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = new Switch<T>(processor, null, null, null, matcher);
    }

    public void addSwitchForValue(CustomMatcher matcher, Processor<T> processor, String ... headersToUse) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = new Switch<T>(processor, headersToUse, null, null, matcher);
    }

    public void addSwitchForValue(String value, Processor<T> processor, int ... indexesToUse) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = new Switch<T>(processor, null, indexesToUse, value, null);
    }

    public void addSwitchForValue(CustomMatcher matcher, Processor<T> processor, int ... indexesToUse) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = new Switch<T>(processor, null, indexesToUse, null, matcher);
    }

    @Override
    public String[] getHeaders() {
        return this.headers;
    }

    @Override
    public int[] getIndexes() {
        return this.indexes;
    }

    @Override
    protected final Processor<T> switchRowProcessor(String[] row, T context) {
        if (this.columnIndex == -1) {
            Object[] headers = NormalizedString.toIdentifierGroupArray(context.headers());
            if (headers == null) {
                throw new DataProcessingException("Unable to determine position of column named '" + this.columnName + "' as no headers have been defined nor extracted from the input");
            }
            this.columnIndex = ArgumentUtils.indexOf(headers, this.columnName);
            if (this.columnIndex == -1) {
                throw new DataProcessingException("Unable to determine position of column named '" + this.columnName + "' as it does not exist in the headers. Available headers are " + Arrays.toString(headers));
            }
        }
        if (this.columnIndex < row.length) {
            String valueToMatch = row[this.columnIndex];
            for (int i = 0; i < this.switches.length; ++i) {
                Switch s2 = this.switches[i];
                if ((s2.matcher == null || !s2.matcher.matches(valueToMatch)) && this.comparator.compare(valueToMatch, s2.value) != 0) continue;
                this.headers = s2.headers;
                this.indexes = s2.indexes;
                return s2.processor;
            }
        }
        if (this.defaultSwitch != null) {
            this.headers = this.defaultSwitch.headers;
            this.indexes = this.defaultSwitch.indexes;
            return this.defaultSwitch.processor;
        }
        this.headers = null;
        this.indexes = null;
        throw new DataProcessingException("Unable to process input row. No switches activated and no default switch defined.", this.columnIndex, row, null);
    }

    private static class Switch<T extends Context> {
        final Processor<T> processor;
        final String[] headers;
        final int[] indexes;
        final String value;
        final CustomMatcher matcher;

        Switch(Processor<T> processor, String[] headers, int[] indexes, String value, CustomMatcher matcher) {
            this.processor = processor;
            this.headers = headers == null || headers.length == 0 ? null : headers;
            this.indexes = indexes == null || indexes.length == 0 ? null : indexes;
            this.value = value == null ? null : value.intern();
            this.matcher = matcher;
        }

        public String toString() {
            return "Switch{processor=" + this.processor + ", headers=" + Arrays.toString(this.headers) + ", indexes=" + Arrays.toString(this.indexes) + ", value='" + this.value + '\'' + ", matcher=" + this.matcher + '}';
        }
    }
}

