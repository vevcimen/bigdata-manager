/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import shadeio.univocity.parsers.annotations.helpers.FieldMapping;
import shadeio.univocity.parsers.annotations.helpers.MethodDescriptor;
import shadeio.univocity.parsers.common.fields.AbstractColumnMapping;
import shadeio.univocity.parsers.common.fields.ColumnMapper;

public final class ColumnMapping
implements ColumnMapper {
    private NameMapping attributeMapping;
    private NameMapping methodNameMapping;
    private MethodMapping methodMapping;

    private static String getCurrentAttributePrefix(String prefix, String name) {
        if (!name.startsWith(prefix)) {
            return null;
        }
        int off = prefix.isEmpty() ? 0 : 1;
        int dot = name.indexOf(46, prefix.length() + off);
        if (dot != -1) {
            String attributePrefix = name.substring(prefix.length() + off, dot);
            return attributePrefix;
        }
        return null;
    }

    public ColumnMapping() {
        this("", null);
    }

    public ColumnMapping(String prefix, ColumnMapping parent) {
        this.attributeMapping = new NameMapping(prefix, parent == null ? null : parent.attributeMapping);
        this.methodNameMapping = new NameMapping(prefix, parent == null ? null : parent.methodNameMapping);
        this.methodMapping = new MethodMapping(prefix, parent == null ? null : parent.methodMapping);
    }

    @Override
    public void attributeToColumnName(String attributeName, String columnName) {
        this.attributeMapping.mapToColumnName(attributeName, columnName);
    }

    @Override
    public void attributeToColumn(String attributeName, Enum<?> column) {
        this.attributeMapping.mapToColumn(attributeName, column);
    }

    @Override
    public void attributeToIndex(String attributeName, int columnIndex) {
        this.attributeMapping.mapToColumnIndex(attributeName, columnIndex);
    }

    @Override
    public void attributesToColumnNames(Map<String, String> mappings) {
        this.attributeMapping.mapToColumnNames(mappings);
    }

    @Override
    public void attributesToColumns(Map<String, Enum<?>> mappings) {
        this.attributeMapping.mapToColumns(mappings);
    }

    @Override
    public void attributesToIndexes(Map<String, Integer> mappings) {
        this.attributeMapping.mapToColumnIndexes(mappings);
    }

    private void methodToColumnName(MethodDescriptor method, String columnName) {
        this.methodMapping.mapToColumnName(method, columnName);
    }

    private void methodToColumn(MethodDescriptor method, Enum<?> column) {
        this.methodMapping.mapToColumn(method, column);
    }

    private void methodToIndex(MethodDescriptor method, int columnIndex) {
        this.methodMapping.mapToColumnIndex(method, columnIndex);
    }

    public boolean isMapped(MethodDescriptor method, String targetName) {
        return this.methodMapping.isMapped(method) || this.attributeMapping.isMapped(targetName) || this.methodNameMapping.isMapped(targetName);
    }

    public boolean updateMapping(FieldMapping fieldMapping, String targetName, MethodDescriptor method) {
        if (this.methodMapping.isMapped(method)) {
            return this.methodMapping.updateFieldMapping(fieldMapping, method);
        }
        if (this.attributeMapping.isMapped(targetName)) {
            return this.attributeMapping.updateFieldMapping(fieldMapping, targetName);
        }
        if (this.methodNameMapping.isMapped(targetName)) {
            return this.methodNameMapping.updateFieldMapping(fieldMapping, targetName);
        }
        return false;
    }

    public String getPrefix() {
        return this.methodMapping.prefix;
    }

    @Override
    public void methodToColumnName(String methodName, String columnName) {
        this.methodNameMapping.mapToColumnName(methodName, columnName);
    }

    @Override
    public void methodToColumn(String methodName, Enum<?> column) {
        this.methodNameMapping.mapToColumn(methodName, column);
    }

    @Override
    public void methodToIndex(String methodName, int columnIndex) {
        this.methodNameMapping.mapToColumnIndex(methodName, columnIndex);
    }

    @Override
    public void methodsToColumnNames(Map<String, String> mappings) {
        this.methodNameMapping.mapToColumnNames(mappings);
    }

    @Override
    public void methodsToColumns(Map<String, Enum<?>> mappings) {
        this.methodNameMapping.mapToColumns(mappings);
    }

    @Override
    public void methodsToIndexes(Map<String, Integer> mappings) {
        this.methodNameMapping.mapToColumnIndexes(mappings);
    }

    @Override
    public void remove(String methodOrAttributeName) {
        this.attributeMapping.remove(methodOrAttributeName);
        this.methodNameMapping.remove(methodOrAttributeName);
        this.methodMapping.remove(methodOrAttributeName);
    }

    @Override
    public void methodToColumnName(String setterName, Class<?> parameterType, String columnName) {
        this.methodToColumnName(MethodDescriptor.setter(setterName, parameterType), columnName);
    }

    @Override
    public void methodToColumn(String setterName, Class<?> parameterType, Enum<?> column) {
        this.methodToColumn(MethodDescriptor.setter(setterName, parameterType), column);
    }

    @Override
    public void methodToIndex(String setterName, Class<?> parameterType, int columnIndex) {
        this.methodToIndex(MethodDescriptor.setter(setterName, parameterType), columnIndex);
    }

    public Set<String> getNestedAttributeNames() {
        HashSet<String> out = new HashSet<String>();
        this.attributeMapping.extractPrefixes(out);
        this.methodNameMapping.extractPrefixes(out);
        this.methodMapping.extractPrefixes(out);
        return out;
    }

    @Override
    public ColumnMapper clone() {
        try {
            ColumnMapping out = (ColumnMapping)super.clone();
            out.attributeMapping = (NameMapping)this.attributeMapping.clone();
            out.methodNameMapping = (NameMapping)this.methodNameMapping.clone();
            out.methodMapping = (MethodMapping)this.methodMapping.clone();
            return out;
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    private class MethodMapping
    extends AbstractColumnMapping<MethodDescriptor> {
        public MethodMapping(String prefix, MethodMapping parent) {
            super(prefix, parent);
        }

        @Override
        MethodDescriptor prefixKey(String prefix, MethodDescriptor key) {
            if (key.getPrefix().equals(prefix)) {
                return key;
            }
            return null;
        }

        @Override
        String getKeyPrefix(String prefix, MethodDescriptor key) {
            return ColumnMapping.getCurrentAttributePrefix(prefix, key.getPrefixedName());
        }

        @Override
        MethodDescriptor findKey(String nameWithPrefix) {
            for (MethodDescriptor k : this.mapping.keySet()) {
                if (!k.getPrefixedName().equals(nameWithPrefix)) continue;
                return k;
            }
            return null;
        }
    }

    private class NameMapping
    extends AbstractColumnMapping<String> {
        public NameMapping(String prefix, NameMapping parent) {
            super(prefix, parent);
        }

        @Override
        String prefixKey(String prefix, String key) {
            if (prefix.isEmpty()) {
                return key;
            }
            return prefix + '.' + key;
        }

        @Override
        String getKeyPrefix(String prefix, String key) {
            return ColumnMapping.getCurrentAttributePrefix(prefix, key);
        }

        @Override
        String findKey(String nameWithPrefix) {
            return nameWithPrefix;
        }
    }
}

