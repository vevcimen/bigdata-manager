/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.processor.BeanWriterProcessor;
import shadeio.univocity.parsers.common.processor.RowWriterProcessor;
import shadeio.univocity.parsers.common.processor.RowWriterProcessorSwitch;

public class OutputValueSwitch
extends RowWriterProcessorSwitch {
    private Switch defaultSwitch;
    private Switch[] switches = new Switch[0];
    private Switch selectedSwitch;
    private Class[] types = new Class[0];
    private final int columnIndex;
    private final String headerName;
    private Comparator comparator = new Comparator<Object>(){

        @Override
        public int compare(Object o1, Object o2) {
            return o1 == o2 ? 0 : (o1 != null && o1.equals(o2) ? 0 : 1);
        }
    };

    public OutputValueSwitch() {
        this(0);
    }

    public OutputValueSwitch(int columnIndex) {
        this.columnIndex = this.getValidatedIndex(columnIndex);
        this.headerName = null;
    }

    public OutputValueSwitch(String headerName) {
        this.headerName = this.getValidatedHeaderName(headerName);
        this.columnIndex = 0;
    }

    public OutputValueSwitch(String headerName, int columnIndex) {
        this.columnIndex = this.getValidatedIndex(columnIndex);
        this.headerName = this.getValidatedHeaderName(headerName);
    }

    private int getValidatedIndex(int columnIndex) {
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Column index must be positive");
        }
        return columnIndex;
    }

    private String getValidatedHeaderName(String headerName) {
        if (headerName == null || headerName.trim().length() == 0) {
            throw new IllegalArgumentException("Header name cannot be blank");
        }
        return headerName;
    }

    public void setComparator(Comparator<?> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        this.comparator = comparator;
    }

    public void setDefaultSwitch(RowWriterProcessor<Object[]> rowProcessor, String ... headersToUse) {
        this.defaultSwitch = new Switch(rowProcessor, headersToUse, null, null);
    }

    public void setDefaultSwitch(RowWriterProcessor<Object[]> rowProcessor, int ... indexesToUse) {
        this.defaultSwitch = new Switch(rowProcessor, null, indexesToUse, null);
    }

    @Override
    protected NormalizedString[] getHeaders() {
        return this.selectedSwitch != null ? this.selectedSwitch.headers : null;
    }

    @Override
    protected int[] getIndexes() {
        return this.selectedSwitch != null ? this.selectedSwitch.indexes : null;
    }

    private Switch getSwitch(Object value) {
        if (value instanceof Object[]) {
            Object[] row = (Object[])value;
            if (row.length < this.columnIndex) {
                return this.defaultSwitch;
            }
            value = row[this.columnIndex];
        }
        for (int i = 0; i < this.switches.length; ++i) {
            Switch s2 = this.switches[i];
            Class type = this.types[i];
            if (!(type != null ? type.isAssignableFrom(value.getClass()) : this.comparator.compare(value, s2.value) == 0)) continue;
            return s2;
        }
        return this.defaultSwitch;
    }

    @Override
    protected RowWriterProcessor<?> switchRowProcessor(Object row) {
        this.selectedSwitch = this.getSwitch(row);
        return this.selectedSwitch != null ? this.selectedSwitch.processor : null;
    }

    public void addSwitchForValue(Object value, RowWriterProcessor<Object[]> rowProcessor, String ... headersToUse) {
        this.addSwitch(new Switch(rowProcessor, headersToUse, null, value));
    }

    public void addSwitchForValue(Object value, RowWriterProcessor<Object[]> rowProcessor) {
        this.addSwitch(new Switch(rowProcessor, null, null, value));
    }

    public void addSwitchForValue(Object value, RowWriterProcessor<Object[]> rowProcessor, int ... indexesToUse) {
        this.addSwitch(new Switch(rowProcessor, null, indexesToUse, value));
    }

    public <T> void addSwitchForType(Class<T> beanType, String ... headersToUse) {
        this.addSwitch(new Switch(headersToUse, null, beanType));
    }

    public <T> void addSwitchForType(Class<T> beanType, int ... indexesToUse) {
        this.addSwitch(new Switch(null, indexesToUse, beanType));
    }

    public <T> void addSwitchForType(Class<T> beanType) {
        this.addSwitch(new Switch(null, null, beanType));
    }

    private void addSwitch(Switch newSwitch) {
        this.switches = Arrays.copyOf(this.switches, this.switches.length + 1);
        this.switches[this.switches.length - 1] = newSwitch;
        this.types = Arrays.copyOf(this.types, this.types.length + 1);
        if (newSwitch.value != null && newSwitch.value.getClass() == Class.class) {
            this.types[this.types.length - 1] = (Class)newSwitch.value;
        }
    }

    private <V> V getValue(Map<?, V> map, int index) {
        int i = 0;
        for (Map.Entry<?, V> e : map.entrySet()) {
            if (i != index) continue;
            return e.getValue();
        }
        return null;
    }

    private NormalizedString[] getHeadersFromSwitch(Object value) {
        for (int i = 0; i < this.switches.length; ++i) {
            Switch s2 = this.getSwitch(value);
            if (s2 == null) continue;
            return s2.headers;
        }
        return null;
    }

    @Override
    public NormalizedString[] getHeaders(Object input) {
        if (input instanceof Object[]) {
            Object[] row = (Object[])input;
            if (this.columnIndex < row.length) {
                return this.getHeadersFromSwitch(row[this.columnIndex]);
            }
            return null;
        }
        return this.getHeadersFromSwitch(input);
    }

    @Override
    public NormalizedString[] getHeaders(Map headerMapping, Map mapInput) {
        Object mapValue = null;
        if (mapInput != null && !mapInput.isEmpty()) {
            String headerToUse = this.headerName;
            if (headerMapping != null) {
                if (this.headerName != null) {
                    Object value = headerMapping.get(this.headerName);
                    headerToUse = value == null ? null : value.toString();
                } else if (this.columnIndex != -1) {
                    Object value = this.getValue(headerMapping, this.columnIndex);
                    headerToUse = value == null ? null : value.toString();
                }
            }
            mapValue = headerToUse != null ? mapInput.get(headerToUse) : this.getValue(mapInput, this.columnIndex);
        }
        return this.getHeadersFromSwitch(mapValue);
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    private List<Object> getSwitchValues() {
        ArrayList<Object> values = new ArrayList<Object>(this.switches.length);
        for (Switch s2 : this.switches) {
            values.add(s2.value);
        }
        return values;
    }

    @Override
    protected String describeSwitch() {
        return "Expecting one of values: " + this.getSwitchValues() + " at column index " + this.getColumnIndex();
    }

    private static class Switch {
        final RowWriterProcessor<Object[]> processor;
        final NormalizedString[] headers;
        final int[] indexes;
        final Object value;

        Switch(RowWriterProcessor<Object[]> processor, String[] headers, int[] indexes, Object value) {
            this(processor, headers, indexes, value, null);
        }

        Switch(String[] headers, int[] indexes, Class<?> type) {
            this(null, headers, indexes, type, type);
        }

        private Switch(RowWriterProcessor<Object[]> processor, String[] headers, int[] indexes, Object value, Class<?> type) {
            if (type != null) {
                processor = new BeanWriterProcessor(type);
                if (headers == null && indexes == null) {
                    headers = AnnotationHelper.deriveHeaderNamesFromFields(type, MethodFilter.ONLY_GETTERS);
                    indexes = ArgumentUtils.toIntArray(Arrays.asList(AnnotationHelper.getSelectedIndexes(type, MethodFilter.ONLY_GETTERS)));
                }
            }
            this.processor = processor;
            this.headers = headers == null || headers.length == 0 ? null : NormalizedString.toIdentifierGroupArray(headers);
            this.indexes = indexes == null || indexes.length == 0 ? null : indexes;
            this.value = value;
        }
    }
}

