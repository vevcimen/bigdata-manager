/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.input.BomInput;

public class ArgumentUtils {
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final NormalizedString[] EMPTY_NORMALIZED_STRING_ARRAY = new NormalizedString[0];

    public static <T> void notEmpty(String argDescription, T ... args) {
        if (args == null) {
            throw new IllegalArgumentException(argDescription + " must not be null");
        }
        if (args.length == 0) {
            throw new IllegalArgumentException(argDescription + " must not be empty");
        }
    }

    public static <T> void noNulls(String argDescription, T ... args) {
        ArgumentUtils.notEmpty(argDescription, args);
        for (T arg : args) {
            if (arg != null) continue;
            if (args.length > 0) {
                throw new IllegalArgumentException(argDescription + " must not contain nulls");
            }
            throw new IllegalArgumentException(argDescription + " must not be null");
        }
    }

    public static int indexOf(NormalizedString[] array, NormalizedString element, FieldSelector fieldSelector) {
        int index = ArgumentUtils.indexOf(array, element);
        if (fieldSelector == null || index == -1) {
            return index;
        }
        int[] indexes = fieldSelector.getFieldIndexes(array);
        for (int i = 0; i < indexes.length; ++i) {
            if (indexes[i] != index) continue;
            return i;
        }
        return -1;
    }

    public static int[] indexesOf(Object[] array, Object element) {
        int[] tmp = new int[]{};
        int i = 0;
        int o = 0;
        while (i < array.length && (i = ArgumentUtils.indexOf(array, element, i)) != -1) {
            tmp = Arrays.copyOf(tmp, tmp.length + 1);
            tmp[o++] = i++;
        }
        return tmp;
    }

    public static int indexOf(Object[] array, Object element) {
        return ArgumentUtils.indexOf(array, element, 0);
    }

    public static int indexOf(char[] array, char element, int from) {
        for (int i = from; i < array.length; ++i) {
            if (array[i] != element) continue;
            return i;
        }
        return -1;
    }

    private static int indexOf(Object[] array, Object element, int from) {
        if (array == null) {
            throw new NullPointerException("Null array");
        }
        if (element == null) {
            for (int i = from; i < array.length; ++i) {
                if (array[i] != null) continue;
                return i;
            }
        } else {
            if (element.getClass() != array.getClass().getComponentType()) {
                throw new IllegalStateException("a");
            }
            if (element instanceof String && array instanceof String[]) {
                for (int i = from; i < array.length; ++i) {
                    String e = String.valueOf(array[i]);
                    if (!element.toString().equalsIgnoreCase(e)) continue;
                    return i;
                }
            } else {
                for (int i = from; i < array.length; ++i) {
                    if (!element.equals(array[i])) continue;
                    return i;
                }
            }
        }
        return -1;
    }

    public static Object[] findMissingElements(Object[] array, Collection<?> elements) {
        return ArgumentUtils.findMissingElements(array, elements.toArray());
    }

    public static Object[] findMissingElements(Object[] array, Object[] elements) {
        ArrayList<Object> out = new ArrayList<Object>();
        for (Object element : elements) {
            if (ArgumentUtils.indexOf(array, element) != -1) continue;
            out.add(element);
        }
        return out.toArray();
    }

    public static Writer newWriter(OutputStream output) {
        return ArgumentUtils.newWriter(output, (Charset)null);
    }

    public static Writer newWriter(OutputStream output, String encoding) {
        return ArgumentUtils.newWriter(output, Charset.forName(encoding));
    }

    public static Writer newWriter(OutputStream output, Charset encoding) {
        if (encoding != null) {
            return new OutputStreamWriter(output, encoding);
        }
        return new OutputStreamWriter(output);
    }

    public static Writer newWriter(File file) {
        return ArgumentUtils.newWriter(file, (Charset)null);
    }

    public static Writer newWriter(File file, String encoding) {
        return ArgumentUtils.newWriter(file, Charset.forName(encoding));
    }

    public static Writer newWriter(File file, Charset encoding) {
        FileOutputStream os;
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                throw new IllegalArgumentException("Unable to create file '" + file.getAbsolutePath() + "', please ensure your application has permission to create files in that path", e);
            }
        }
        try {
            os = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return ArgumentUtils.newWriter((OutputStream)os, encoding);
    }

    public static Reader newReader(InputStream input) {
        return ArgumentUtils.newReader(input, (Charset)null);
    }

    public static Reader newReader(InputStream input, String encoding) {
        return ArgumentUtils.newReader(input, encoding == null ? (Charset)null : Charset.forName(encoding));
    }

    public static Reader newReader(InputStream input, Charset encoding) {
        if (encoding == null) {
            BomInput bomInput = new BomInput(input);
            if (bomInput.getEncoding() != null) {
                encoding = bomInput.getCharset();
            }
            if (bomInput.hasBytesStored()) {
                input = bomInput;
            }
        }
        if (encoding != null) {
            return new InputStreamReader(input, encoding);
        }
        return new InputStreamReader(input);
    }

    public static Reader newReader(File file) {
        return ArgumentUtils.newReader(file, (Charset)null);
    }

    public static Reader newReader(File file, String encoding) {
        return ArgumentUtils.newReader(file, Charset.forName(encoding));
    }

    public static Reader newReader(File file, Charset encoding) {
        FileInputStream input;
        try {
            input = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return ArgumentUtils.newReader((InputStream)input, encoding);
    }

    public static String[] toArray(List<Enum> enums) {
        String[] out = new String[enums.size()];
        for (int i = 0; i < out.length; ++i) {
            out[i] = enums.get(i).toString();
        }
        return out;
    }

    public static int[] toIntArray(Collection<Integer> ints) {
        int[] out = new int[ints.size()];
        int i = 0;
        for (Integer boxed : ints) {
            out[i++] = boxed;
        }
        return out;
    }

    public static char[] toCharArray(Collection<Character> characters) {
        char[] out = new char[characters.size()];
        int i = 0;
        for (Character boxed : characters) {
            out[i++] = boxed.charValue();
        }
        return out;
    }

    public static String restrictContent(int length, CharSequence content) {
        if (content == null) {
            return null;
        }
        if (length == 0) {
            return "<omitted>";
        }
        if (length == -1) {
            return ((Object)content).toString();
        }
        int errorMessageStart = content.length() - length;
        if (length > 0 && errorMessageStart > 0) {
            return "..." + ((Object)content.subSequence(errorMessageStart, content.length())).toString();
        }
        return ((Object)content).toString();
    }

    public static String restrictContent(int length, Object content) {
        if (content == null) {
            return null;
        }
        if (content instanceof Object[]) {
            return ArgumentUtils.restrictContent(length, Arrays.toString((Object[])content));
        }
        return ArgumentUtils.restrictContent(length, String.valueOf(content));
    }

    public static void throwUnchecked(Throwable error) {
        ArgumentUtils.throwsUnchecked(error);
    }

    private static <T extends Exception> void throwsUnchecked(Throwable toThrow) throws T {
        throw (Exception)toThrow;
    }

    public static byte[] toByteArray(int ... ints) {
        byte[] out = new byte[ints.length];
        for (int i = 0; i < ints.length; ++i) {
            out[i] = (byte)ints[i];
        }
        return out;
    }

    public static <T> T[] findDuplicates(T[] array) {
        if (array == null || array.length == 0) {
            return array;
        }
        HashSet<T> elements = new HashSet<T>(array.length);
        ArrayList<T> duplicates = new ArrayList<T>(1);
        for (T element : array) {
            if (!elements.contains(element)) {
                elements.add(element);
                continue;
            }
            duplicates.add(element);
        }
        return duplicates.toArray((Object[])Array.newInstance(array.getClass().getComponentType(), duplicates.size()));
    }

    public static String trim(String input, boolean left, boolean right) {
        int begin;
        if (input.length() == 0 || !left && !right) {
            return input;
        }
        for (begin = 0; left && begin < input.length() && input.charAt(begin) <= ' '; ++begin) {
        }
        if (begin == input.length()) {
            return "";
        }
        int end = begin + input.length() - 1;
        if (end >= input.length()) {
            end = input.length() - 1;
        }
        while (right && input.charAt(end) <= ' ') {
            --end;
        }
        if (begin == end) {
            return "";
        }
        if (begin == 0 && end == input.length() - 1) {
            return input;
        }
        return input.substring(begin, end + 1);
    }

    public static String displayLineSeparators(String str, boolean addNewLine) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            if (ch == '\r' || ch == '\n') {
                out.append('[');
                out.append(ch == '\r' ? "cr" : "lf");
                char next = '\u0000';
                if (i + 1 < str.length()) {
                    next = str.charAt(i + 1);
                    if (next != ch && (next == '\r' || next == '\n')) {
                        out.append(next == '\r' ? "cr" : "lf");
                        ++i;
                    } else {
                        next = '\u0000';
                    }
                }
                out.append(']');
                if (!addNewLine) continue;
                out.append(ch);
                if (next == '\u0000') continue;
                out.append(next);
                continue;
            }
            out.append(ch);
        }
        return out.toString();
    }

    public static int[] removeAll(int[] array, int e) {
        if (array == null || array.length == 0) {
            return array;
        }
        int removeCount = 0;
        for (int i = 0; i < array.length; ++i) {
            if (array[i] != e) continue;
            ++removeCount;
        }
        if (removeCount == 0) {
            return array;
        }
        int[] tmp = new int[array.length - removeCount];
        int j = 0;
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == e) continue;
            tmp[j++] = array[i];
        }
        return tmp;
    }
}

