/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.StringCache;

public final class NormalizedString
implements Serializable,
Comparable<NormalizedString>,
CharSequence {
    private static final long serialVersionUID = -3904288692735859811L;
    private static final StringCache<NormalizedString> stringCache = new StringCache<NormalizedString>(){

        @Override
        protected NormalizedString process(String input) {
            if (input == null) {
                return null;
            }
            return new NormalizedString(input);
        }
    };
    private final String original;
    private final String normalized;
    private final boolean literal;
    private final int hashCode;

    private NormalizedString(String string) {
        String trimmed = string.trim();
        if (trimmed.length() > 2 && trimmed.charAt(0) == '\'' && trimmed.charAt(trimmed.length() - 1) == '\'') {
            this.normalized = this.original = string.substring(1, string.length() - 1);
            this.hashCode = this.normalize(this.original).hashCode();
            this.literal = true;
        } else {
            this.original = string;
            this.normalized = this.normalize(this.original);
            this.hashCode = this.normalized.hashCode();
            this.literal = false;
        }
    }

    private String normalize(Object value) {
        String str = String.valueOf(value);
        str = str.trim().toLowerCase();
        return str;
    }

    public boolean isLiteral() {
        return this.literal;
    }

    public boolean equals(Object anObject) {
        if (anObject == this) {
            return true;
        }
        if (anObject == null) {
            return false;
        }
        if (anObject instanceof NormalizedString) {
            NormalizedString other = (NormalizedString)anObject;
            if (this.literal || other.literal) {
                return this.original.equals(other.original);
            }
            return this.normalized.equals(other.normalized);
        }
        if (this.literal) {
            return this.original.equals(String.valueOf(anObject));
        }
        return this.normalized.equals(this.normalize(anObject));
    }

    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public int length() {
        return this.original.length();
    }

    @Override
    public char charAt(int index) {
        return this.original.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.original.subSequence(start, end);
    }

    @Override
    public int compareTo(NormalizedString o) {
        if (o == this) {
            return 0;
        }
        if (this.literal || o.literal) {
            return this.original.compareTo(o.original);
        }
        return this.normalized.compareTo(o.normalized);
    }

    @Override
    public int compareTo(String o) {
        return this.compareTo(NormalizedString.valueOf(o));
    }

    @Override
    public String toString() {
        return this.original;
    }

    public static NormalizedString literalValueOf(String string) {
        if (string == null) {
            return null;
        }
        return stringCache.get('\'' + string + "'");
    }

    public static NormalizedString valueOf(Object o) {
        if (o == null) {
            return null;
        }
        return stringCache.get(o.toString());
    }

    public static NormalizedString valueOf(String string) {
        if (string == null) {
            return null;
        }
        return stringCache.get(string);
    }

    public static String valueOf(NormalizedString string) {
        if (string == null) {
            return null;
        }
        return string.original;
    }

    public static NormalizedString[] toArray(Collection<String> args) {
        if (args == null) {
            throw new IllegalArgumentException("String collection cannot be null");
        }
        NormalizedString[] out = new NormalizedString[args.size()];
        Iterator<String> it = args.iterator();
        for (int i = 0; i < out.length; ++i) {
            out[i] = NormalizedString.valueOf(it.next());
        }
        return out;
    }

    public static String[] toStringArray(Collection<NormalizedString> args) {
        if (args == null) {
            throw new IllegalArgumentException("String collection cannot be null");
        }
        String[] out = new String[args.size()];
        Iterator<NormalizedString> it = args.iterator();
        for (int i = 0; i < out.length; ++i) {
            out[i] = NormalizedString.valueOf(it.next());
        }
        return out;
    }

    public static NormalizedString[] toUniqueArray(String ... args) {
        ArgumentUtils.notEmpty("Element array", args);
        NormalizedString[] out = NormalizedString.toArray(args);
        Object[] duplicates = ArgumentUtils.findDuplicates(out);
        if (duplicates.length > 0) {
            throw new IllegalArgumentException("Duplicate elements found: " + Arrays.toString(duplicates));
        }
        return out;
    }

    public static NormalizedString[] toArray(String ... args) {
        if (args == null) {
            return null;
        }
        if (args.length == 0) {
            return ArgumentUtils.EMPTY_NORMALIZED_STRING_ARRAY;
        }
        NormalizedString[] out = new NormalizedString[args.length];
        for (int i = 0; i < args.length; ++i) {
            out[i] = NormalizedString.valueOf(args[i]);
        }
        return out;
    }

    public static String[] toArray(NormalizedString ... args) {
        if (args == null) {
            return null;
        }
        if (args.length == 0) {
            return ArgumentUtils.EMPTY_STRING_ARRAY;
        }
        String[] out = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            out[i] = NormalizedString.valueOf(args[i]);
        }
        return out;
    }

    private static <T extends Collection<NormalizedString>> T getCollection(T out, String ... args) {
        Collections.addAll(out, NormalizedString.toArray(args));
        return out;
    }

    private static <T extends Collection<NormalizedString>> T getCollection(T out, Collection<String> args) {
        Collections.addAll(out, NormalizedString.toArray(args));
        return out;
    }

    private static <T extends Collection<String>> T getCollection(T out, NormalizedString ... args) {
        Collections.addAll(out, NormalizedString.toArray(args));
        return out;
    }

    private static <T extends Collection<String>> T getStringCollection(T out, Collection<NormalizedString> args) {
        Collections.addAll(out, NormalizedString.toStringArray(args));
        return out;
    }

    public static ArrayList<NormalizedString> toArrayList(String ... args) {
        return NormalizedString.getCollection(new ArrayList(), args);
    }

    public static ArrayList<NormalizedString> toArrayList(Collection<String> args) {
        return NormalizedString.getCollection(new ArrayList(), args);
    }

    public static ArrayList<String> toArrayListOfStrings(NormalizedString ... args) {
        return NormalizedString.getCollection(new ArrayList(), args);
    }

    public static ArrayList<String> toArrayListOfStrings(Collection<NormalizedString> args) {
        return NormalizedString.getStringCollection(new ArrayList(), args);
    }

    public static TreeSet<NormalizedString> toTreeSet(String ... args) {
        return NormalizedString.getCollection(new TreeSet(), args);
    }

    public static TreeSet<NormalizedString> toTreeSet(Collection<String> args) {
        return NormalizedString.getCollection(new TreeSet(), args);
    }

    public static TreeSet<String> toTreeSetOfStrings(NormalizedString ... args) {
        return NormalizedString.getCollection(new TreeSet(), args);
    }

    public static TreeSet<String> toTreeSetOfStrings(Collection<NormalizedString> args) {
        return NormalizedString.getStringCollection(new TreeSet(), args);
    }

    public static HashSet<NormalizedString> toHashSet(String ... args) {
        return NormalizedString.getCollection(new HashSet(), args);
    }

    public static HashSet<NormalizedString> toHashSet(Collection<String> args) {
        return NormalizedString.getCollection(new HashSet(), args);
    }

    public static HashSet<String> toHashSetOfStrings(NormalizedString ... args) {
        return NormalizedString.getCollection(new HashSet(), args);
    }

    public static HashSet<String> toHashSetOfStrings(Collection<NormalizedString> args) {
        return NormalizedString.getStringCollection(new HashSet(), args);
    }

    public static LinkedHashSet<NormalizedString> toLinkedHashSet(String ... args) {
        return NormalizedString.getCollection(new LinkedHashSet(), args);
    }

    public static LinkedHashSet<NormalizedString> toLinkedHashSet(Collection<String> args) {
        return NormalizedString.getCollection(new LinkedHashSet(), args);
    }

    public static LinkedHashSet<String> toLinkedHashSetOfStrings(NormalizedString ... args) {
        return NormalizedString.getCollection(new LinkedHashSet(), args);
    }

    public static LinkedHashSet<String> toLinkedHashSetOfStrings(Collection<NormalizedString> args) {
        return NormalizedString.getStringCollection(new LinkedHashSet(), args);
    }

    public NormalizedString toLiteral() {
        if (this.literal) {
            return this;
        }
        return NormalizedString.literalValueOf(this.original);
    }

    public static NormalizedString[] toIdentifierGroupArray(NormalizedString[] strings) {
        NormalizedString.identifyLiterals(strings);
        return strings;
    }

    public static NormalizedString[] toIdentifierGroupArray(String[] strings) {
        NormalizedString[] out = NormalizedString.toArray(strings);
        NormalizedString.identifyLiterals(out, false, false);
        return out;
    }

    public static boolean identifyLiterals(NormalizedString[] strings) {
        return NormalizedString.identifyLiterals(strings, false, false);
    }

    public static boolean identifyLiterals(NormalizedString[] strings, boolean lowercaseIdentifiers, boolean uppercaseIdentifiers) {
        if (strings == null) {
            return false;
        }
        TreeMap<NormalizedString, Object[]> normalizedMap = new TreeMap<NormalizedString, Object[]>();
        boolean modified = false;
        for (int i = 0; i < strings.length; ++i) {
            NormalizedString string = strings[i];
            if (string == null || string.isLiteral()) continue;
            if (NormalizedString.shouldBeLiteral(string.original, lowercaseIdentifiers, uppercaseIdentifiers)) {
                strings[i] = NormalizedString.literalValueOf(string.original);
                continue;
            }
            Object[] clashing = (Object[])normalizedMap.get(string);
            if (clashing != null && !string.original.equals(((NormalizedString)clashing[0]).original)) {
                strings[i] = NormalizedString.literalValueOf(string.original);
                strings[((Integer)clashing[1]).intValue()] = ((NormalizedString)clashing[0]).toLiteral();
                modified = true;
                continue;
            }
            normalizedMap.put(string, new Object[]{string, i});
        }
        return modified;
    }

    private static boolean shouldBeLiteral(String string, boolean lowercaseIdentifiers, boolean uppercaseIdentifiers) {
        if (lowercaseIdentifiers || uppercaseIdentifiers) {
            for (int i = 0; i < string.length(); ++i) {
                char ch = string.charAt(i);
                if ((!uppercaseIdentifiers || Character.isUpperCase(ch)) && (!lowercaseIdentifiers || Character.isLowerCase(ch))) continue;
                return true;
            }
        }
        return false;
    }

    public static StringCache<NormalizedString> getCache() {
        return stringCache;
    }
}

