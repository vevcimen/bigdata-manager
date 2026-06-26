/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.util.Arrays;
import shadeio.univocity.parsers.common.TextParsingException;
import shadeio.univocity.parsers.common.input.CharInput;
import shadeio.univocity.parsers.common.input.DefaultCharAppender;

public class ExpandingCharAppender
extends DefaultCharAppender {
    private static final int MAX_ARRAY_LENGTH = 0x7FFFFFF7;

    public ExpandingCharAppender(String emptyValue, int whitespaceRangeStart) {
        this(8192, emptyValue, whitespaceRangeStart);
    }

    public ExpandingCharAppender(int initialBufferLength, String emptyValue, int whitespaceRangeStart) {
        super(initialBufferLength, emptyValue, whitespaceRangeStart);
    }

    @Override
    public void appendIgnoringWhitespace(char ch) {
        try {
            super.appendIgnoringWhitespace(ch);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            super.appendIgnoringWhitespace(ch);
        }
    }

    @Override
    public void appendIgnoringPadding(char ch, char padding) {
        try {
            super.appendIgnoringPadding(ch, padding);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            super.appendIgnoringPadding(ch, padding);
        }
    }

    @Override
    public void appendIgnoringWhitespaceAndPadding(char ch, char padding) {
        try {
            super.appendIgnoringWhitespaceAndPadding(ch, padding);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            super.appendIgnoringWhitespaceAndPadding(ch, padding);
        }
    }

    @Override
    public void append(char ch) {
        try {
            super.append(ch);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            super.append(ch);
        }
    }

    @Override
    public final void fill(char ch, int length) {
        try {
            super.fill(ch, length);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            super.fill(ch, length);
        }
    }

    final void expandAndRetry() {
        this.expand();
        --this.index;
    }

    private void expand(int additionalLength, double factor) {
        if (this.chars.length == 0x7FFFFFF7) {
            throw new TextParsingException(null, "Can't expand internal appender array to over 2147483639 characters in length.");
        }
        this.chars = Arrays.copyOf(this.chars, (int)Math.min((double)(this.index + additionalLength) * factor, 2.147483639E9));
    }

    final void expand() {
        this.expand(0, 2.0);
    }

    final void expand(int additionalLength) {
        this.expand(additionalLength, 1.5);
    }

    @Override
    public final void prepend(char ch) {
        try {
            super.prepend(ch);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expand();
            super.prepend(ch);
        }
    }

    @Override
    public final void prepend(char ch1, char ch2) {
        try {
            super.prepend(ch1, ch2);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expand(2);
            super.prepend(ch1, ch2);
        }
    }

    @Override
    public final void prepend(char[] chars) {
        try {
            super.prepend(chars);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expand(chars.length);
            super.prepend(chars);
        }
    }

    @Override
    public final void append(DefaultCharAppender appender) {
        try {
            super.append(appender);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expand(appender.index);
            this.append(appender);
        }
    }

    @Override
    public final char appendUntil(char ch, CharInput input, char stop) {
        try {
            return super.appendUntil(ch, input, stop);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            return this.appendUntil(input.getChar(), input, stop);
        }
    }

    @Override
    public final char appendUntil(char ch, CharInput input, char stop1, char stop2) {
        try {
            return super.appendUntil(ch, input, stop1, stop2);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            return this.appendUntil(input.getChar(), input, stop1, stop2);
        }
    }

    @Override
    public final char appendUntil(char ch, CharInput input, char stop1, char stop2, char stop3) {
        try {
            return super.appendUntil(ch, input, stop1, stop2, stop3);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.expandAndRetry();
            return this.appendUntil(input.getChar(), input, stop1, stop2, stop3);
        }
    }

    @Override
    public final void append(char[] ch, int from, int length) {
        if (this.index + length <= this.chars.length) {
            super.append(ch, from, length);
        } else {
            this.chars = Arrays.copyOf(this.chars, Math.min(this.chars.length + length + this.index, 0x7FFFFFF7));
            super.append(ch, from, length);
        }
    }

    @Override
    public final void append(String string, int from, int to) {
        try {
            super.append(string, from, to);
        }
        catch (IndexOutOfBoundsException e) {
            this.expand(to - from);
            super.append(string, from, to);
        }
    }
}

