/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

public abstract class QuotedCSVParser {
    protected final boolean _keepQuotes;

    public QuotedCSVParser(boolean keepQuotes) {
        this._keepQuotes = keepQuotes;
    }

    public static String unquote(String s2) {
        char c;
        int i;
        int l = s2.length();
        if (s2 == null || l == 0) {
            return s2;
        }
        for (i = 0; i < l && (c = s2.charAt(i)) != '\"'; ++i) {
        }
        if (i == l) {
            return s2;
        }
        boolean quoted = true;
        boolean sloshed = false;
        StringBuffer buffer = new StringBuffer();
        buffer.append(s2, 0, i);
        ++i;
        while (i < l) {
            char c2 = s2.charAt(i);
            if (quoted) {
                if (sloshed) {
                    buffer.append(c2);
                    sloshed = false;
                } else if (c2 == '\"') {
                    quoted = false;
                } else if (c2 == '\\') {
                    sloshed = true;
                } else {
                    buffer.append(c2);
                }
            } else if (c2 == '\"') {
                quoted = true;
            } else {
                buffer.append(c2);
            }
            ++i;
        }
        return buffer.toString();
    }

    /*
     * Unable to fully structure code
     */
    public void addValue(String value) {
        if (value == null) {
            return;
        }
        buffer = new StringBuffer();
        l = value.length();
        state = State.VALUE;
        quoted = false;
        sloshed = false;
        nwsLength = 0;
        lastLength = 0;
        valueLength = -1;
        paramName = -1;
        paramValue = -1;
        block25: for (i = 0; i <= l; ++i) {
            block38: {
                block39: {
                    v0 = c = i == l ? '\u0000' : value.charAt(i);
                    if (!quoted || c == '\u0000') break block38;
                    if (!sloshed) break block39;
                    sloshed = false;
                    ** GOTO lbl-1000
                }
                switch (c) {
                    case '\\': {
                        sloshed = true;
                        if (!this._keepQuotes) {
                            break;
                        }
                        ** GOTO lbl29
                    }
                    case '\"': {
                        quoted = false;
                        if (!this._keepQuotes) break;
                    }
lbl29:
                    // 3 sources

                    default: lbl-1000:
                    // 2 sources

                    {
                        buffer.append(c);
                        nwsLength = buffer.length();
                        break;
                    }
                }
                continue;
            }
            switch (c) {
                case '\t': 
                case ' ': {
                    if (buffer.length() <= lastLength) continue block25;
                    buffer.append(c);
                    continue block25;
                }
                case '\"': {
                    quoted = true;
                    if (this._keepQuotes) {
                        if (state == State.PARAM_VALUE && paramValue < 0) {
                            paramValue = nwsLength;
                        }
                        buffer.append(c);
                    } else if (state == State.PARAM_VALUE && paramValue < 0) {
                        paramValue = nwsLength;
                    }
                    nwsLength = buffer.length();
                    continue block25;
                }
                case ';': {
                    buffer.setLength(nwsLength);
                    if (state == State.VALUE) {
                        this.parsedValue(buffer);
                        valueLength = buffer.length();
                    } else {
                        this.parsedParam(buffer, valueLength, paramName, paramValue);
                    }
                    nwsLength = buffer.length();
                    paramValue = -1;
                    paramName = -1;
                    buffer.append(c);
                    lastLength = ++nwsLength;
                    state = State.PARAM_NAME;
                    continue block25;
                }
                case '\u0000': 
                case ',': {
                    if (nwsLength > 0) {
                        buffer.setLength(nwsLength);
                        switch (1.$SwitchMap$org$eclipse$jetty$http$QuotedCSVParser$State[state.ordinal()]) {
                            case 1: {
                                this.parsedValue(buffer);
                                valueLength = buffer.length();
                                break;
                            }
                            case 2: 
                            case 3: {
                                this.parsedParam(buffer, valueLength, paramName, paramValue);
                            }
                        }
                        this.parsedValueAndParams(buffer);
                    }
                    buffer.setLength(0);
                    lastLength = 0;
                    nwsLength = 0;
                    paramValue = -1;
                    paramName = -1;
                    valueLength = -1;
                    state = State.VALUE;
                    continue block25;
                }
                case '=': {
                    switch (1.$SwitchMap$org$eclipse$jetty$http$QuotedCSVParser$State[state.ordinal()]) {
                        case 1: {
                            paramName = 0;
                            valueLength = 0;
                            buffer.setLength(nwsLength);
                            param = buffer.toString();
                            buffer.setLength(0);
                            this.parsedValue(buffer);
                            valueLength = buffer.length();
                            buffer.append(param);
                            buffer.append(c);
                            lastLength = ++nwsLength;
                            state = State.PARAM_VALUE;
                            continue block25;
                        }
                        case 2: {
                            buffer.setLength(nwsLength);
                            buffer.append(c);
                            lastLength = ++nwsLength;
                            state = State.PARAM_VALUE;
                            continue block25;
                        }
                        case 3: {
                            if (paramValue < 0) {
                                paramValue = nwsLength;
                            }
                            buffer.append(c);
                            nwsLength = buffer.length();
                            continue block25;
                        }
                    }
                    continue block25;
                }
                default: {
                    switch (1.$SwitchMap$org$eclipse$jetty$http$QuotedCSVParser$State[state.ordinal()]) {
                        case 1: {
                            buffer.append(c);
                            nwsLength = buffer.length();
                            continue block25;
                        }
                        case 2: {
                            if (paramName < 0) {
                                paramName = nwsLength;
                            }
                            buffer.append(c);
                            nwsLength = buffer.length();
                            continue block25;
                        }
                        case 3: {
                            if (paramValue < 0) {
                                paramValue = nwsLength;
                            }
                            buffer.append(c);
                            nwsLength = buffer.length();
                            continue block25;
                        }
                    }
                }
            }
        }
    }

    protected void parsedValueAndParams(StringBuffer buffer) {
    }

    protected void parsedValue(StringBuffer buffer) {
    }

    protected void parsedParam(StringBuffer buffer, int valueLength, int paramName, int paramValue) {
    }

    private static enum State {
        VALUE,
        PARAM_NAME,
        PARAM_VALUE;

    }
}

