/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.ParserOutput;
import shadeio.univocity.parsers.common.fields.FieldSelector;

public class ColumnMap {
    private Map<NormalizedString, Integer> columnMap;
    private int[] enumMap;
    private int[] extractedIndexes = null;
    private final Context context;
    private final ParserOutput output;

    public ColumnMap(Context context, ParserOutput output) {
        this.context = context;
        this.output = output;
    }

    public int indexOf(String header) {
        Integer index;
        if (this.columnMap != null && this.columnMap.isEmpty()) {
            return -1;
        }
        this.validateHeader(header);
        NormalizedString normalizedHeader = NormalizedString.valueOf(header);
        if (this.columnMap == null) {
            NormalizedString[] headers = NormalizedString.toIdentifierGroupArray(this.context.headers());
            if (headers == null) {
                this.columnMap = Collections.emptyMap();
                return -1;
            }
            this.columnMap = new HashMap<NormalizedString, Integer>(headers.length);
            this.extractedIndexes = this.context.extractedFieldIndexes();
            if (this.extractedIndexes != null) {
                if (this.context.columnsReordered()) {
                    int[] selection = ArgumentUtils.removeAll(this.extractedIndexes, -1);
                    for (int i = 0; i < selection.length; ++i) {
                        int originalIndex = selection[i];
                        NormalizedString h2 = headers[originalIndex];
                        this.columnMap.put(h2, i);
                    }
                } else {
                    for (int i = 0; i < this.extractedIndexes.length && i < headers.length; ++i) {
                        this.columnMap.put(headers[i], i);
                    }
                }
            } else {
                for (int i = 0; i < headers.length; ++i) {
                    this.columnMap.put(headers[i], i);
                }
            }
        }
        if ((index = this.columnMap.get(normalizedHeader)) == null) {
            return -1;
        }
        return index;
    }

    private void validateHeader(Object header) {
        if (header == null) {
            if (this.context.headers() == null) {
                throw new IllegalArgumentException("Header name cannot be null.");
            }
            throw new IllegalArgumentException("Header name cannot be null. Use one of the available column names: " + Arrays.asList(this.context.headers()));
        }
    }

    public int indexOf(Enum<?> header) {
        if (this.enumMap != null && this.enumMap.length == 0) {
            return -1;
        }
        this.validateHeader(header);
        if (this.enumMap == null) {
            FieldSelector selector;
            NormalizedString[] headers = NormalizedString.toIdentifierGroupArray(this.context.headers());
            if (headers == null) {
                this.enumMap = new int[0];
                return -1;
            }
            Enum[] constants = (Enum[])header.getClass().getEnumConstants();
            int lastOrdinal = Integer.MIN_VALUE;
            for (int i = 0; i < constants.length; ++i) {
                if (lastOrdinal >= constants[i].ordinal()) continue;
                lastOrdinal = constants[i].ordinal();
            }
            this.enumMap = new int[lastOrdinal + 1];
            FieldSelector fieldSelector = selector = this.output == null ? null : this.output.getFieldSelector();
            if (!this.context.columnsReordered()) {
                selector = null;
            }
            for (int i = 0; i < constants.length; ++i) {
                int index;
                Enum constant = constants[i];
                String name = constant.toString();
                this.enumMap[constant.ordinal()] = index = ArgumentUtils.indexOf(headers, NormalizedString.valueOf(name), selector);
            }
        }
        return this.enumMap[header.ordinal()];
    }

    void reset() {
        this.columnMap = null;
        this.enumMap = null;
        this.extractedIndexes = null;
    }
}

