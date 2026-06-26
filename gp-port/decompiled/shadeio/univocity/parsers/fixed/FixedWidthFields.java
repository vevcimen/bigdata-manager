/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.annotations.FixedWidth;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.annotations.helpers.AnnotationRegistry;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.annotations.helpers.TransformedHeader;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.CommonSettings;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.fixed.FieldAlignment;
import shadeio.univocity.parsers.fixed.FixedWidthFormat;

public class FixedWidthFields
implements Cloneable {
    private List<Integer> fieldLengths = new ArrayList<Integer>();
    private List<Boolean> fieldsToIgnore = new ArrayList<Boolean>();
    private List<NormalizedString> fieldNames = new ArrayList<NormalizedString>();
    private List<FieldAlignment> fieldAlignment = new ArrayList<FieldAlignment>();
    private List<Character> fieldPadding = new ArrayList<Character>();
    private List<Boolean> paddingsToKeep = new ArrayList<Boolean>();
    private boolean noNames = true;
    private int totalLength = 0;

    public FixedWidthFields(LinkedHashMap<String, Integer> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Map of fields and their lengths cannot be null/empty");
        }
        for (Map.Entry<String, Integer> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Integer fieldLength = entry.getValue();
            this.addField(fieldName, (int)fieldLength);
        }
    }

    public FixedWidthFields(String[] headers, int[] lengths) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("Headers cannot be null/empty");
        }
        if (lengths == null || lengths.length == 0) {
            throw new IllegalArgumentException("Field lengths cannot be null/empty");
        }
        if (headers.length != lengths.length) {
            throw new IllegalArgumentException("Sequence of headers and their respective lengths must match. Got " + headers.length + " headers but " + lengths.length + " lengths");
        }
        for (int i = 0; i < headers.length; ++i) {
            this.addField(headers[i], lengths[i]);
        }
    }

    public FixedWidthFields(int ... fieldLengths) {
        for (int i = 0; i < fieldLengths.length; ++i) {
            this.addField(fieldLengths[i]);
        }
    }

    @Deprecated
    public FixedWidthFields(Class beanClass) {
        this(beanClass, MethodFilter.ONLY_SETTERS);
    }

    public static FixedWidthFields forParsing(Class beanClass) {
        return new FixedWidthFields(beanClass, MethodFilter.ONLY_SETTERS);
    }

    public static FixedWidthFields forWriting(Class beanClass) {
        return new FixedWidthFields(beanClass, MethodFilter.ONLY_GETTERS);
    }

    private FixedWidthFields(Class beanClass, MethodFilter methodFilter) {
        if (beanClass == null) {
            throw new IllegalArgumentException("Class must not be null.");
        }
        List<TransformedHeader> fieldSequence = AnnotationHelper.getFieldSequence(beanClass, true, null, methodFilter);
        if (fieldSequence.isEmpty()) {
            throw new IllegalArgumentException("Can't derive fixed-width fields from class '" + beanClass.getName() + "'. No @Parsed annotations found.");
        }
        LinkedHashSet<String> fieldNamesWithoutConfig = new LinkedHashSet<String>();
        for (TransformedHeader field : fieldSequence) {
            if (field == null) continue;
            String fieldName = field.getHeaderName();
            FixedWidth fw = AnnotationHelper.findAnnotation(field.getTarget(), FixedWidth.class);
            if (fw == null) {
                fieldNamesWithoutConfig.add(field.getTargetName());
                continue;
            }
            int length = AnnotationRegistry.getValue(field.getTarget(), fw, "value", fw.value());
            int from = AnnotationRegistry.getValue(field.getTarget(), fw, "from", fw.from());
            int to = AnnotationRegistry.getValue(field.getTarget(), fw, "to", fw.to());
            FieldAlignment alignment = AnnotationRegistry.getValue(field.getTarget(), fw, "alignment", fw.alignment());
            char padding = AnnotationRegistry.getValue(field.getTarget(), fw, "padding", Character.valueOf(fw.padding())).charValue();
            if (length != -1) {
                if (from != -1 || to != -1) {
                    throw new IllegalArgumentException("Can't initialize fixed-width field from " + field.describe() + ". " + "Can't have field length (" + length + ") defined along with position from (" + from + ") and to (" + to + ")");
                }
                this.addField(fieldName, length, alignment, padding);
            } else if (from != -1 && to != -1) {
                this.addField(fieldName, from, to, alignment, padding);
            } else {
                throw new IllegalArgumentException("Can't initialize fixed-width field from " + field.describe() + "'. " + "Field length/position undefined defined");
            }
            boolean keepPadding = AnnotationRegistry.getValue(field.getTarget(), fw, "keepPadding", fw.keepPadding());
            this.setKeepPaddingFlag(keepPadding, this.fieldLengths.size() - 1, new int[0]);
        }
        if (fieldNamesWithoutConfig.size() > 0) {
            throw new IllegalArgumentException("Can't derive fixed-width fields from class '" + beanClass.getName() + "'. " + "The following fields don't have a @FixedWidth annotation: " + fieldNamesWithoutConfig);
        }
    }

    public FixedWidthFields addField(int startPosition, int endPosition) {
        return this.addField(null, startPosition, endPosition, FieldAlignment.LEFT, '\u0000');
    }

    public FixedWidthFields addField(String name, int startPosition, int endPosition) {
        return this.addField(name, startPosition, endPosition, FieldAlignment.LEFT, '\u0000');
    }

    public FixedWidthFields addField(String name, int startPosition, int endPosition, char padding) {
        return this.addField(name, startPosition, endPosition, FieldAlignment.LEFT, padding);
    }

    public FixedWidthFields addField(String name, int startPosition, int endPosition, FieldAlignment alignment) {
        return this.addField(name, startPosition, endPosition, alignment, '\u0000');
    }

    public FixedWidthFields addField(int startPosition, int endPosition, FieldAlignment alignment) {
        return this.addField(null, startPosition, endPosition, alignment, '\u0000');
    }

    public FixedWidthFields addField(int startPosition, int endPosition, FieldAlignment alignment, char padding) {
        return this.addField(null, startPosition, endPosition, alignment, padding);
    }

    public FixedWidthFields addField(int startPosition, int endPosition, char padding) {
        return this.addField(null, startPosition, endPosition, FieldAlignment.LEFT, padding);
    }

    public FixedWidthFields addField(String name, int startPosition, int endPosition, FieldAlignment alignment, char padding) {
        int length = endPosition - startPosition;
        if (startPosition < this.totalLength) {
            throw new IllegalArgumentException("Start position '" + startPosition + "' overlaps with one or more fields");
        }
        if (startPosition > this.totalLength) {
            this.addField(null, startPosition - this.totalLength, FieldAlignment.LEFT, '\u0000');
            this.fieldsToIgnore.set(this.fieldsToIgnore.size() - 1, Boolean.TRUE);
        }
        return this.addField(name, length, alignment, padding);
    }

    boolean[] getFieldsToIgnore() {
        boolean[] out = new boolean[this.fieldsToIgnore.size()];
        for (int i = 0; i < this.fieldsToIgnore.size(); ++i) {
            out[i] = this.fieldsToIgnore.get(i);
        }
        return out;
    }

    Boolean[] getKeepPaddingFlags() {
        return this.paddingsToKeep.toArray(new Boolean[0]);
    }

    public FixedWidthFields addField(int length) {
        return this.addField(null, length, FieldAlignment.LEFT, '\u0000');
    }

    public FixedWidthFields addField(int length, FieldAlignment alignment) {
        return this.addField(null, length, alignment, '\u0000');
    }

    public FixedWidthFields addField(String name, int length) {
        return this.addField(name, length, FieldAlignment.LEFT, '\u0000');
    }

    public FixedWidthFields addField(String name, int length, FieldAlignment alignment) {
        return this.addField(name, length, alignment, '\u0000');
    }

    public FixedWidthFields addField(int length, char padding) {
        return this.addField(null, length, FieldAlignment.LEFT, padding);
    }

    public FixedWidthFields addField(int length, FieldAlignment alignment, char padding) {
        return this.addField(null, length, alignment, padding);
    }

    public FixedWidthFields addField(String name, int length, char padding) {
        return this.addField(name, length, FieldAlignment.LEFT, padding);
    }

    public FixedWidthFields addField(String name, int length, FieldAlignment alignment, char padding) {
        this.validateLength(name, length);
        this.fieldLengths.add(length);
        this.fieldsToIgnore.add(Boolean.FALSE);
        this.fieldNames.add(NormalizedString.valueOf(name));
        this.fieldPadding.add(Character.valueOf(padding));
        this.paddingsToKeep.add(null);
        if (name != null) {
            this.noNames = false;
        }
        this.fieldAlignment.add(alignment);
        this.totalLength += length;
        return this;
    }

    private void validateLength(String name, int length) {
        if (length < 1) {
            if (name == null) {
                throw new IllegalArgumentException("Invalid field length: " + length + " for field at index " + this.fieldLengths.size());
            }
            throw new IllegalArgumentException("Invalid field length: " + length + " for field " + name);
        }
    }

    public int getFieldsPerRecord() {
        return this.fieldLengths.size();
    }

    public NormalizedString[] getFieldNames() {
        if (this.noNames) {
            return null;
        }
        return this.getSelectedElements(this.fieldNames).toArray(ArgumentUtils.EMPTY_NORMALIZED_STRING_ARRAY);
    }

    private <T> List<T> getSelectedElements(List<T> elements) {
        ArrayList<T> out = new ArrayList<T>();
        for (int i = 0; i < elements.size(); ++i) {
            if (this.fieldsToIgnore.get(i).booleanValue()) continue;
            out.add(elements.get(i));
        }
        return out;
    }

    public int[] getFieldLengths() {
        return ArgumentUtils.toIntArray(this.getSelectedElements(this.fieldLengths));
    }

    int[] getAllLengths() {
        return ArgumentUtils.toIntArray(this.fieldLengths);
    }

    public void setFieldLength(String name, int newLength) {
        if (name == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        int index = this.fieldNames.indexOf(name);
        if (index == -1) {
            throw new IllegalArgumentException("Cannot find field with name '" + name + '\'');
        }
        this.validateLength(name, newLength);
        this.fieldLengths.set(index, newLength);
    }

    public void setFieldLength(int position, int newLength) {
        this.validateIndex(position);
        this.validateLength("at index " + position, newLength);
        this.fieldLengths.set(position, newLength);
    }

    public void setAlignment(FieldAlignment alignment, int ... positions) {
        for (int position : positions) {
            this.setAlignment(position, alignment);
        }
    }

    public void setAlignment(FieldAlignment alignment, String ... names) {
        for (String name : names) {
            int position = this.indexOf(name);
            this.setAlignment(position, alignment);
        }
    }

    private void validateIndex(int position) {
        if (position < 0 && position >= this.fieldLengths.size()) {
            throw new IllegalArgumentException("No field defined at index " + position);
        }
    }

    public int indexOf(String fieldName) {
        if (this.noNames) {
            throw new IllegalArgumentException("No field names defined");
        }
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null/empty");
        }
        NormalizedString normalizedFieldName = NormalizedString.valueOf(fieldName);
        int i = 0;
        for (NormalizedString name : this.fieldNames) {
            if (name.equals(normalizedFieldName)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    private void setAlignment(int position, FieldAlignment alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException("Alignment cannot be null");
        }
        this.validateIndex(position);
        this.fieldAlignment.set(position, alignment);
    }

    public FieldAlignment getAlignment(int position) {
        this.validateIndex(position);
        return this.fieldAlignment.get(position);
    }

    public FieldAlignment getAlignment(String fieldName) {
        int index = this.indexOf(fieldName);
        if (index == -1) {
            throw new IllegalArgumentException("Field '" + fieldName + "' does not exist. Available field names are: " + this.fieldNames);
        }
        return this.getAlignment(index);
    }

    public FieldAlignment[] getFieldAlignments() {
        return this.fieldAlignment.toArray(new FieldAlignment[this.fieldAlignment.size()]);
    }

    public char[] getFieldPaddings() {
        return ArgumentUtils.toCharArray(this.fieldPadding);
    }

    char[] getFieldPaddings(FixedWidthFormat format) {
        char[] out = this.getFieldPaddings();
        for (int i = 0; i < out.length; ++i) {
            if (out[i] != '\u0000') continue;
            out[i] = format.getPadding();
        }
        return out;
    }

    public void setPadding(char padding, int ... positions) {
        for (int position : positions) {
            this.setPadding(position, padding);
        }
    }

    public void setPadding(char padding, String ... names) {
        for (String name : names) {
            int position = this.indexOf(name);
            this.setPadding(position, padding);
        }
    }

    private void setPadding(int position, char padding) {
        if (padding == '\u0000') {
            throw new IllegalArgumentException("Cannot use the null character as padding");
        }
        this.validateIndex(position);
        this.fieldPadding.set(position, Character.valueOf(padding));
    }

    public void keepPaddingOn(int position, int ... positions) {
        this.setKeepPaddingFlag(true, position, positions);
    }

    public void keepPaddingOn(String name, String ... names) {
        this.setKeepPaddingFlag(true, name, names);
    }

    public void stripPaddingFrom(int position, int ... positions) {
        this.setKeepPaddingFlag(false, position, positions);
    }

    public void stripPaddingFrom(String name, String ... names) {
        this.setKeepPaddingFlag(false, name, names);
    }

    private void setKeepPaddingFlag(boolean keep, int position, int ... positions) {
        this.setPaddingToKeep(position, keep);
        for (int p : positions) {
            this.setPaddingToKeep(p, keep);
        }
    }

    private void setKeepPaddingFlag(boolean keep, String name, String ... names) {
        int position = this.indexOf(name);
        this.setPaddingToKeep(position, keep);
        for (String n : names) {
            position = this.indexOf(n);
            this.setPaddingToKeep(position, keep);
        }
    }

    private void setPaddingToKeep(int position, boolean keepPaddingFlag) {
        this.validateIndex(position);
        this.paddingsToKeep.set(position, keepPaddingFlag);
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        int i = 0;
        for (Integer length : this.fieldLengths) {
            out.append("\n\t\t").append(i + 1).append('\t');
            if (i < this.fieldNames.size()) {
                out.append(this.fieldNames.get(i));
            }
            out.append(", length: ").append(length);
            out.append(", align: ").append((Object)this.fieldAlignment.get(i));
            out.append(", padding: ").append(this.fieldPadding.get(i));
            out.append(", keepPadding: ").append(this.paddingsToKeep.get(i));
            ++i;
        }
        return out.toString();
    }

    static void setHeadersIfPossible(FixedWidthFields fieldLengths, CommonSettings settings) {
        int[] lengths;
        NormalizedString[] headers;
        if (fieldLengths != null && settings.getHeaders() == null && (headers = fieldLengths.getFieldNames()) != null && (lengths = fieldLengths.getFieldLengths()).length == headers.length) {
            settings.setHeaders(NormalizedString.toArray(headers));
        }
    }

    protected FixedWidthFields clone() {
        try {
            FixedWidthFields out = (FixedWidthFields)super.clone();
            out.fieldLengths = new ArrayList<Integer>(this.fieldLengths);
            out.fieldNames = new ArrayList<NormalizedString>(this.fieldNames);
            out.fieldAlignment = new ArrayList<FieldAlignment>(this.fieldAlignment);
            out.fieldPadding = new ArrayList<Character>(this.fieldPadding);
            out.paddingsToKeep = new ArrayList<Boolean>(this.paddingsToKeep);
            return out;
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}

