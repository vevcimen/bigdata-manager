/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.input.InputAnalysisProcess;
import shadeio.univocity.parsers.csv.CsvFormat;
import shadeio.univocity.parsers.csv.CsvParserSettings;

public abstract class CsvFormatDetector
implements InputAnalysisProcess {
    private final int MAX_ROW_SAMPLES;
    private final char comment;
    private final char suggestedDelimiter;
    private final char normalizedNewLine;
    private final int whitespaceRangeStart;
    private char[] allowedDelimiters;
    private char[] delimiterPreference;
    private final char suggestedQuote;
    private final char suggestedQuoteEscape;

    public CsvFormatDetector(int maxRowSamples, CsvParserSettings settings, int whitespaceRangeStart) {
        this.MAX_ROW_SAMPLES = maxRowSamples;
        this.whitespaceRangeStart = whitespaceRangeStart;
        this.allowedDelimiters = settings.getDelimitersForDetection();
        if (this.allowedDelimiters != null && this.allowedDelimiters.length > 0) {
            this.suggestedDelimiter = this.allowedDelimiters[0];
            this.delimiterPreference = (char[])this.allowedDelimiters.clone();
            Arrays.sort(this.allowedDelimiters);
        } else {
            String delimiter = ((CsvFormat)settings.getFormat()).getDelimiterString();
            this.suggestedDelimiter = (char)(delimiter.length() > 1 ? 44 : (int)((CsvFormat)settings.getFormat()).getDelimiter());
            this.allowedDelimiters = new char[0];
            this.delimiterPreference = this.allowedDelimiters;
        }
        this.normalizedNewLine = ((CsvFormat)settings.getFormat()).getNormalizedNewline();
        this.comment = ((CsvFormat)settings.getFormat()).getComment();
        this.suggestedQuote = ((CsvFormat)settings.getFormat()).getQuote();
        this.suggestedQuoteEscape = ((CsvFormat)settings.getFormat()).getQuoteEscape();
    }

    protected Map<Character, Integer> calculateTotals(List<Map<Character, Integer>> symbolsPerRow) {
        HashMap<Character, Integer> out = new HashMap<Character, Integer>();
        for (Map<Character, Integer> rowStats : symbolsPerRow) {
            for (Map.Entry<Character, Integer> symbolStats : rowStats.entrySet()) {
                Character symbol = symbolStats.getKey();
                Integer count = symbolStats.getValue();
                Integer total = (Integer)out.get(symbol);
                if (total == null) {
                    total = 0;
                }
                out.put(symbol, total + count);
            }
        }
        return out;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void execute(char[] characters, int length) {
        void var16_31;
        int i;
        HashSet<Character> allSymbols = new HashSet<Character>();
        HashMap<Character, Integer> symbols = new HashMap<Character, Integer>();
        HashMap<Character, Integer> escape = new HashMap<Character, Integer>();
        ArrayList<Map<Character, Integer>> symbolsPerRow = new ArrayList<Map<Character, Integer>>();
        int doubleQuoteCount = 0;
        int singleQuoteCount = 0;
        char inQuote = '\u0000';
        boolean afterNewLine = true;
        block0: for (i = 0; i < length; ++i) {
            char ch = characters[i];
            if (afterNewLine && ch == this.comment) {
                while (++i < length) {
                    ch = characters[i];
                    if (ch != '\r' && ch != '\n' && ch != this.normalizedNewLine) continue;
                    if (ch != '\r' || i + 1 >= characters.length || characters[i + 1] != '\n') continue block0;
                    ++i;
                    continue block0;
                }
                continue;
            }
            if (ch == '\"' || ch == '\'') {
                if (inQuote == ch) {
                    char prev;
                    char next;
                    if (ch == '\"') {
                        ++doubleQuoteCount;
                    } else {
                        ++singleQuoteCount;
                    }
                    if (i + 1 < length && (Character.isLetterOrDigit(next = characters[i + 1]) || next <= ' ' && this.whitespaceRangeStart < next && next != '\n' && next != '\r') && !Character.isLetterOrDigit(prev = characters[i - 1]) && prev != '\n' && prev != '\r') {
                        this.increment(escape, prev);
                    }
                    inQuote = '\u0000';
                    continue;
                }
                if (inQuote != '\u0000') continue;
                char prev = '\u0000';
                int j = i;
                while (prev <= ' ' && --j >= 0) {
                    prev = characters[j];
                }
                if (j >= 0 && Character.isLetterOrDigit(prev)) continue;
                inQuote = ch;
                continue;
            }
            if (inQuote != '\u0000') continue;
            afterNewLine = false;
            if (this.isSymbol(ch)) {
                allSymbols.add(Character.valueOf(ch));
                this.increment(symbols, ch);
                continue;
            }
            if (ch != '\r' && ch != '\n' && ch != this.normalizedNewLine || symbols.size() <= 0) continue;
            afterNewLine = true;
            symbolsPerRow.add(symbols);
            if (symbolsPerRow.size() == this.MAX_ROW_SAMPLES) break;
            symbols = new HashMap();
        }
        if (symbols.size() > 0 && length < characters.length) {
            symbolsPerRow.add(symbols);
        }
        if (length >= characters.length && i >= length && symbolsPerRow.size() > 1) {
            symbolsPerRow.remove(symbolsPerRow.size() - 1);
        }
        Map<Character, Integer> totals = this.calculateTotals(symbolsPerRow);
        HashMap<Character, Integer> sums = new HashMap<Character, Integer>();
        HashSet<Character> toRemove = new HashSet<Character>();
        for (Map map : symbolsPerRow) {
            for (Map map2 : symbolsPerRow) {
                for (Character symbol : allSymbols) {
                    Integer previousCount = (Integer)map.get(symbol);
                    Integer currentCount = (Integer)map2.get(symbol);
                    if (previousCount == null && currentCount == null) {
                        toRemove.add(symbol);
                    }
                    if (previousCount == null || currentCount == null) continue;
                    this.increment(sums, symbol.charValue(), Math.abs(previousCount - currentCount));
                }
            }
        }
        if (toRemove.size() == sums.size()) {
            void var16_24;
            HashMap<Character, Integer> lineCount = new HashMap<Character, Integer>();
            for (i = 0; i < symbolsPerRow.size(); ++i) {
                for (Character symbolInRow : ((Map)symbolsPerRow.get(i)).keySet()) {
                    void var18_39;
                    Integer n = (Integer)lineCount.get(symbolInRow);
                    if (n == null) {
                        Integer n2 = 0;
                    }
                    lineCount.put(symbolInRow, var18_39.intValue() + 1);
                }
            }
            Object var16_23 = null;
            for (Map.Entry entry : lineCount.entrySet()) {
                if (var16_24 != null && var16_24.intValue() >= (Integer)entry.getValue()) continue;
                Integer n = (Integer)entry.getValue();
            }
            Character bestCandidate = null;
            for (Map.Entry e : lineCount.entrySet()) {
                if (!((Integer)e.getValue()).equals(var16_24)) continue;
                if (bestCandidate == null) {
                    bestCandidate = (Character)e.getKey();
                    continue;
                }
                bestCandidate = null;
                break;
            }
            if (bestCandidate != null) {
                toRemove.remove(bestCandidate);
            }
        }
        sums.keySet().removeAll(toRemove);
        if (this.allowedDelimiters.length > 0) {
            void var18_45;
            HashSet<Character> toRetain = new HashSet<Character>();
            char[] cArray = this.allowedDelimiters;
            int len$ = cArray.length;
            boolean bl = false;
            while (var18_45 < len$) {
                char c = cArray[var18_45];
                toRetain.add(Character.valueOf(c));
                ++var18_45;
            }
            sums.keySet().retainAll(toRetain);
        }
        char delimiter = this.pickDelimiter(sums, totals);
        if (doubleQuoteCount == 0 && singleQuoteCount == 0) {
            char c = this.suggestedQuote;
        } else {
            int n = doubleQuoteCount >= singleQuoteCount ? 34 : 39;
        }
        escape.remove(Character.valueOf(delimiter));
        char quoteEscape = doubleQuoteCount == 0 && singleQuoteCount == 0 ? this.suggestedQuoteEscape : this.max(escape, totals, (char)var16_31);
        this.apply(delimiter, (char)var16_31, quoteEscape);
    }

    protected char pickDelimiter(Map<Character, Integer> sums, Map<Character, Integer> totals) {
        char delimiter;
        block10: {
            char delimiterMax = this.max(sums, totals, this.suggestedDelimiter);
            char delimiterMin = this.min(sums, totals, this.suggestedDelimiter);
            if (delimiterMin == ' ' || delimiterMax == ' ') {
                boolean hasOtherDelimiters = false;
                for (Map.Entry<Character, Integer> e : sums.entrySet()) {
                    if (e.getValue() != 0 || e.getKey().charValue() == ' ') continue;
                    hasOtherDelimiters = true;
                    break;
                }
                if (hasOtherDelimiters) {
                    totals.remove(Character.valueOf(' '));
                    delimiterMax = this.max(sums, totals, this.suggestedDelimiter);
                    delimiterMin = this.min(sums, totals, this.suggestedDelimiter);
                }
            }
            if (delimiterMax != delimiterMin) {
                if (sums.get(Character.valueOf(delimiterMin)) == 0 && sums.get(Character.valueOf(delimiterMax)) != 0) {
                    delimiter = delimiterMin;
                } else {
                    for (char c : this.delimiterPreference) {
                        if (c == delimiterMin) {
                            delimiter = delimiterMin;
                        } else {
                            if (c != delimiterMax) continue;
                            delimiter = delimiterMax;
                        }
                        break block10;
                    }
                    delimiter = totals.get(Character.valueOf(delimiterMin)) > totals.get(Character.valueOf(delimiterMax)) ? delimiterMin : delimiterMax;
                }
            } else {
                delimiter = delimiterMax;
            }
        }
        return delimiter;
    }

    protected void increment(Map<Character, Integer> map, char symbol) {
        this.increment(map, symbol, 1);
    }

    protected void increment(Map<Character, Integer> map, char symbol, int incrementSize) {
        Integer count = map.get(Character.valueOf(symbol));
        if (count == null) {
            count = 0;
        }
        map.put(Character.valueOf(symbol), count + incrementSize);
    }

    protected char min(Map<Character, Integer> map, Map<Character, Integer> totals, char defaultChar) {
        return this.getChar(map, totals, defaultChar, true);
    }

    protected char max(Map<Character, Integer> map, Map<Character, Integer> totals, char defaultChar) {
        return this.getChar(map, totals, defaultChar, false);
    }

    protected char getChar(Map<Character, Integer> map, Map<Character, Integer> totals, char defaultChar, boolean min2) {
        int val = min2 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        for (Map.Entry<Character, Integer> e : map.entrySet()) {
            int sum = e.getValue();
            if ((!min2 || sum > val) && (min2 || sum < val)) continue;
            char newChar = e.getKey().charValue();
            if (val == sum) {
                Integer currentTotal = totals.get(Character.valueOf(defaultChar));
                Integer newTotal = totals.get(Character.valueOf(newChar));
                if (currentTotal != null && newTotal != null) {
                    if (currentTotal.equals(newTotal)) {
                        int defIndex = ArgumentUtils.indexOf(this.delimiterPreference, defaultChar, 0);
                        int newIndex = ArgumentUtils.indexOf(this.delimiterPreference, newChar, 0);
                        if (defIndex == -1 || newIndex == -1) continue;
                        defaultChar = defIndex < newIndex ? defaultChar : newChar;
                        continue;
                    }
                    if ((!min2 || newTotal <= currentTotal) && (min2 || newTotal <= currentTotal)) continue;
                    defaultChar = newChar;
                    continue;
                }
                if (!this.isSymbol(newChar)) continue;
                defaultChar = newChar;
                continue;
            }
            val = sum;
            defaultChar = newChar;
        }
        return defaultChar;
    }

    protected boolean isSymbol(char ch) {
        return this.isAllowedDelimiter(ch) || ch != this.comment && !Character.isLetterOrDigit(ch) && (ch == '\t' || ch >= ' ');
    }

    protected boolean isAllowedDelimiter(char ch) {
        return Arrays.binarySearch(this.allowedDelimiters, ch) >= 0;
    }

    protected abstract void apply(char var1, char var2, char var3);
}

