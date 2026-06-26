/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import shadeio.univocity.parsers.annotations.helpers.FieldMapping;

abstract class AbstractColumnMapping<K>
implements Cloneable {
    final String prefix;
    Map<K, Object> mapping;

    AbstractColumnMapping(String prefix, AbstractColumnMapping parent) {
        if (parent != null) {
            this.mapping = parent.mapping;
            this.prefix = parent.prefix.isEmpty() ? prefix : parent.prefix + '.' + prefix;
        } else {
            this.mapping = new LinkedHashMap<K, Object>();
            this.prefix = prefix;
        }
    }

    void mapToColumnName(K key, String columnName) {
        this.mapping.put(key, columnName);
    }

    void mapToColumn(K key, Enum<?> column) {
        this.mapping.put(key, column);
    }

    void mapToColumnIndex(K key, int columnIndex) {
        this.mapping.put(key, columnIndex);
    }

    void mapToColumnNames(Map<K, String> mappings) {
        this.mapping.putAll(mappings);
    }

    void mapToColumns(Map<K, Enum<?>> mappings) {
        this.mapping.putAll(mappings);
    }

    void mapToColumnIndexes(Map<K, Integer> mappings) {
        this.mapping.putAll(mappings);
    }

    boolean isMapped(K key) {
        return this.getMappedColumn(key) != null;
    }

    abstract K prefixKey(String var1, K var2);

    private Object getMappedColumn(K key) {
        if (key == null) {
            return null;
        }
        key = this.prefixKey(this.prefix, key);
        Object out = this.mapping.get(key);
        return out;
    }

    boolean updateFieldMapping(FieldMapping fieldMapping, K key) {
        Object mappedColumn = this.getMappedColumn(key);
        if (mappedColumn != null) {
            if (mappedColumn instanceof Enum) {
                mappedColumn = ((Enum)mappedColumn).name();
            }
            if (mappedColumn instanceof String) {
                fieldMapping.setFieldName((String)mappedColumn);
                fieldMapping.setIndex(-1);
                return true;
            }
            if (mappedColumn instanceof Integer) {
                fieldMapping.setIndex((Integer)mappedColumn);
                return true;
            }
            throw new IllegalStateException("Unexpected mapping of '" + key + "' to " + mappedColumn);
        }
        return false;
    }

    void extractPrefixes(Set<String> out) {
        for (K key : this.mapping.keySet()) {
            String keyPrefix = this.getKeyPrefix(this.prefix, key);
            if (keyPrefix == null) continue;
            out.add(keyPrefix);
        }
    }

    abstract String getKeyPrefix(String var1, K var2);

    public AbstractColumnMapping<K> clone() {
        try {
            AbstractColumnMapping out = (AbstractColumnMapping)super.clone();
            out.mapping = new LinkedHashMap<K, Object>(this.mapping);
            return out;
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    abstract K findKey(String var1);

    void remove(String nameWithPrefix) {
        K key;
        while ((key = this.findKey(nameWithPrefix)) != null) {
            if (this.mapping.remove(key) != null) continue;
            return;
        }
    }
}

