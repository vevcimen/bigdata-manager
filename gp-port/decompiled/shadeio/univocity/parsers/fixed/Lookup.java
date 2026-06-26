/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import shadeio.univocity.parsers.common.ColumnMap;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.ParsingContextWrapper;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordFactory;
import shadeio.univocity.parsers.common.record.RecordMetaData;
import shadeio.univocity.parsers.fixed.FieldAlignment;
import shadeio.univocity.parsers.fixed.FixedWidthFields;
import shadeio.univocity.parsers.fixed.FixedWidthFormat;

class Lookup {
    final char[] value;
    final int[] lengths;
    final FieldAlignment[] alignments;
    final boolean[] ignore;
    final Boolean[] keepPaddingFlags;
    final char[] paddings;
    final NormalizedString[] fieldNames;
    final char wildcard;
    Context context;

    Lookup(String value, FixedWidthFields config, FixedWidthFormat format) {
        this.value = value.toCharArray();
        this.lengths = config.getAllLengths();
        this.alignments = config.getFieldAlignments();
        this.fieldNames = config.getFieldNames();
        this.paddings = config.getFieldPaddings(format);
        this.wildcard = format.getLookupWildcard();
        this.ignore = config.getFieldsToIgnore();
        this.keepPaddingFlags = config.getKeepPaddingFlags();
    }

    void initializeLookupContext(ParsingContext context, NormalizedString[] headersToUse) {
        final String[] headers = NormalizedString.toArray(headersToUse);
        this.context = new ParsingContextWrapper(context){
            RecordFactory recordFactory;
            final ColumnMap columnMap;
            {
                super(x0);
                this.columnMap = new ColumnMap(this, null);
            }

            @Override
            public String[] headers() {
                return headers;
            }

            @Override
            public int indexOf(String header) {
                return this.columnMap.indexOf(header);
            }

            @Override
            public int indexOf(Enum<?> header) {
                return this.columnMap.indexOf(header);
            }

            @Override
            public Record toRecord(String[] row) {
                if (this.recordFactory == null) {
                    this.recordFactory = new RecordFactory(this);
                }
                return this.recordFactory.newRecord(row);
            }

            @Override
            public RecordMetaData recordMetaData() {
                if (this.recordFactory == null) {
                    this.recordFactory = new RecordFactory(this);
                }
                return this.recordFactory.getRecordMetaData();
            }
        };
    }

    boolean matches(char[] lookup) {
        if (this.value.length > lookup.length) {
            return false;
        }
        for (int i = 0; i < this.value.length; ++i) {
            char ch = this.value[i];
            if (ch == this.wildcard || ch == lookup[i]) continue;
            return false;
        }
        return true;
    }

    static void registerLookahead(String lookup, FixedWidthFields lengths, Map<String, FixedWidthFields> map) {
        Lookup.registerLookup("ahead", lookup, lengths, map);
    }

    static void registerLookbehind(String lookup, FixedWidthFields lengths, Map<String, FixedWidthFields> map) {
        Lookup.registerLookup("behind", lookup, lengths, map);
    }

    private static void registerLookup(String direction, String lookup, FixedWidthFields lengths, Map<String, FixedWidthFields> map) {
        if (lookup == null) {
            throw new IllegalArgumentException("Look" + direction + " value cannot be null");
        }
        if (lengths == null) {
            throw new IllegalArgumentException("Lengths of fields associated to look" + direction + " value '" + lookup + "' cannot be null");
        }
        map.put(lookup, lengths);
    }

    static Lookup[] getLookupFormats(Map<String, FixedWidthFields> map, FixedWidthFormat format) {
        if (map.isEmpty()) {
            return null;
        }
        Lookup[] out = new Lookup[map.size()];
        int i = 0;
        for (Map.Entry<String, FixedWidthFields> e : map.entrySet()) {
            out[i++] = new Lookup(e.getKey(), e.getValue(), format);
        }
        Arrays.sort(out, new Comparator<Lookup>(){

            @Override
            public int compare(Lookup o1, Lookup o2) {
                return o1.value.length < o2.value.length ? 1 : (o1.value.length == o2.value.length ? 0 : -1);
            }
        });
        return out;
    }

    static int calculateMaxLookupLength(Lookup[] ... lookupArrays) {
        int max = 0;
        for (Lookup[] lookups : lookupArrays) {
            if (lookups == null) continue;
            for (Lookup lookup : lookups) {
                if (max >= lookup.value.length) continue;
                max = lookup.value.length;
            }
        }
        return max;
    }

    static int[] calculateMaxFieldLengths(FixedWidthFields fieldLengths, Map<String, FixedWidthFields> lookaheadFormats, Map<String, FixedWidthFields> lookbehindFormats) {
        ArrayList<int[]> allLengths = new ArrayList<int[]>();
        if (fieldLengths != null) {
            allLengths.add(fieldLengths.getFieldLengths());
        }
        for (FixedWidthFields lengths : lookaheadFormats.values()) {
            allLengths.add(lengths.getFieldLengths());
        }
        for (FixedWidthFields lengths : lookbehindFormats.values()) {
            allLengths.add(lengths.getFieldLengths());
        }
        if (allLengths.isEmpty()) {
            throw new IllegalStateException("Cannot determine field lengths to use.");
        }
        int lastColumn = -1;
        for (int[] lengths : allLengths) {
            if (lastColumn >= lengths.length) continue;
            lastColumn = lengths.length;
        }
        int[] out = new int[lastColumn];
        Arrays.fill(out, 0);
        for (int[] lengths : allLengths) {
            for (int i = 0; i < lastColumn; ++i) {
                int length;
                if (i >= lengths.length || out[i] >= (length = lengths[i])) continue;
                out[i] = length;
            }
        }
        return out;
    }
}

