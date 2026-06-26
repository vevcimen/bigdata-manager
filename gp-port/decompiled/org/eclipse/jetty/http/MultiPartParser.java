/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.SearchPattern;
import org.eclipse.jetty.util.Utf8StringBuilder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MultiPartParser {
    public static final Logger LOG = Log.getLogger(MultiPartParser.class);
    private static final EnumSet<State> __delimiterStates = EnumSet.of(State.DELIMITER, State.DELIMITER_CLOSE, State.DELIMITER_PADDING);
    private static final int MAX_HEADER_LINE_LENGTH = 998;
    private final boolean debug = LOG.isDebugEnabled();
    private final Handler _handler;
    private final SearchPattern _delimiterSearch;
    private String _fieldName;
    private String _fieldValue;
    private State _state = State.PREAMBLE;
    private FieldState _fieldState = FieldState.FIELD;
    private int _partialBoundary = 2;
    private boolean _cr;
    private ByteBuffer _patternBuffer;
    private final Utf8StringBuilder _string = new Utf8StringBuilder();
    private int _length;
    private int _totalHeaderLineLength = -1;

    public MultiPartParser(Handler handler, String boundary) {
        this._handler = handler;
        String delimiter = "\r\n--" + boundary;
        this._patternBuffer = ByteBuffer.wrap(delimiter.getBytes(StandardCharsets.US_ASCII));
        this._delimiterSearch = SearchPattern.compile(this._patternBuffer.array());
    }

    public void reset() {
        this._state = State.PREAMBLE;
        this._fieldState = FieldState.FIELD;
        this._partialBoundary = 2;
    }

    public Handler getHandler() {
        return this._handler;
    }

    public State getState() {
        return this._state;
    }

    public boolean isState(State state) {
        return this._state == state;
    }

    private static boolean hasNextByte(ByteBuffer buffer) {
        return BufferUtil.hasContent(buffer);
    }

    private HttpTokens.Token next(ByteBuffer buffer) {
        byte ch = buffer.get();
        HttpTokens.Token t = HttpTokens.TOKENS[0xFF & ch];
        switch (t.getType()) {
            case CNTL: {
                throw new IllegalCharacterException(this._state, t, buffer);
            }
            case LF: {
                this._cr = false;
                break;
            }
            case CR: {
                if (this._cr) {
                    throw new BadMessageException("Bad EOL");
                }
                this._cr = true;
                return null;
            }
            case ALPHA: 
            case DIGIT: 
            case TCHAR: 
            case VCHAR: 
            case HTAB: 
            case SPACE: 
            case OTEXT: 
            case COLON: {
                if (!this._cr) break;
                throw new BadMessageException("Bad EOL");
            }
        }
        return t;
    }

    private void setString(String s2) {
        this._string.reset();
        this._string.append(s2);
        this._length = s2.length();
    }

    private String takeString() {
        String s2 = this._string.toString();
        if (s2.length() > this._length) {
            s2 = s2.substring(0, this._length);
        }
        this._string.reset();
        this._length = -1;
        return s2;
    }

    public boolean parse(ByteBuffer buffer, boolean last) {
        boolean handle = false;
        block8: while (!handle && BufferUtil.hasContent(buffer)) {
            switch (this._state) {
                case PREAMBLE: {
                    this.parsePreamble(buffer);
                    continue block8;
                }
                case DELIMITER: 
                case DELIMITER_PADDING: 
                case DELIMITER_CLOSE: {
                    this.parseDelimiter(buffer);
                    continue block8;
                }
                case BODY_PART: {
                    handle = this.parseMimePartHeaders(buffer);
                    continue block8;
                }
                case FIRST_OCTETS: 
                case OCTETS: {
                    handle = this.parseOctetContent(buffer);
                    continue block8;
                }
                case EPILOGUE: {
                    BufferUtil.clear(buffer);
                    continue block8;
                }
                case END: {
                    handle = true;
                    continue block8;
                }
            }
            throw new IllegalStateException();
        }
        if (last && BufferUtil.isEmpty(buffer)) {
            if (this._state == State.EPILOGUE) {
                this._state = State.END;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("messageComplete {}", this);
                }
                return this._handler.messageComplete();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("earlyEOF {}", this);
            }
            this._handler.earlyEOF();
            return true;
        }
        return handle;
    }

    private void parsePreamble(ByteBuffer buffer) {
        int delimiter;
        if (LOG.isDebugEnabled()) {
            LOG.debug("parsePreamble({})", BufferUtil.toDetailString(buffer));
        }
        if (this._partialBoundary > 0) {
            int partial = this._delimiterSearch.startsWith(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining(), this._partialBoundary);
            if (partial > 0) {
                if (partial == this._delimiterSearch.getLength()) {
                    buffer.position(buffer.position() + partial - this._partialBoundary);
                    this._partialBoundary = 0;
                    this.setState(State.DELIMITER);
                    return;
                }
                this._partialBoundary = partial;
                BufferUtil.clear(buffer);
                return;
            }
            this._partialBoundary = 0;
        }
        if ((delimiter = this._delimiterSearch.match(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())) >= 0) {
            buffer.position(delimiter - buffer.arrayOffset() + this._delimiterSearch.getLength());
            this.setState(State.DELIMITER);
            return;
        }
        this._partialBoundary = this._delimiterSearch.endsWith(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        BufferUtil.clear(buffer);
    }

    private void parseDelimiter(ByteBuffer buffer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("parseDelimiter({})", BufferUtil.toDetailString(buffer));
        }
        block4: while (__delimiterStates.contains((Object)this._state) && MultiPartParser.hasNextByte(buffer)) {
            HttpTokens.Token t = this.next(buffer);
            if (t == null) {
                return;
            }
            if (t.getType() == HttpTokens.Type.LF) {
                this.setState(State.BODY_PART);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("startPart {}", this);
                }
                this._handler.startPart();
                return;
            }
            switch (this._state) {
                case DELIMITER: {
                    if (t.getChar() == '-') {
                        this.setState(State.DELIMITER_CLOSE);
                        continue block4;
                    }
                    this.setState(State.DELIMITER_PADDING);
                    continue block4;
                }
                case DELIMITER_CLOSE: {
                    if (t.getChar() == '-') {
                        this.setState(State.EPILOGUE);
                        return;
                    }
                    this.setState(State.DELIMITER_PADDING);
                    continue block4;
                }
            }
        }
    }

    protected boolean parseMimePartHeaders(ByteBuffer buffer) {
        HttpTokens.Token t;
        if (LOG.isDebugEnabled()) {
            LOG.debug("parseMimePartHeaders({})", BufferUtil.toDetailString(buffer));
        }
        block33: while (this._state == State.BODY_PART && MultiPartParser.hasNextByte(buffer) && (t = this.next(buffer)) != null) {
            if (t.getType() != HttpTokens.Type.LF) {
                ++this._totalHeaderLineLength;
            }
            if (this._totalHeaderLineLength > 998) {
                throw new IllegalStateException("Header Line Exceeded Max Length");
            }
            switch (this._fieldState) {
                case FIELD: {
                    switch (t.getType()) {
                        case HTAB: 
                        case SPACE: {
                            if (this._fieldName == null) {
                                throw new IllegalStateException("First field folded");
                            }
                            if (this._fieldValue == null) {
                                this._string.reset();
                                this._length = 0;
                            } else {
                                this.setString(this._fieldValue);
                                this._string.append(' ');
                                ++this._length;
                                this._fieldValue = null;
                            }
                            this.setState(FieldState.VALUE);
                            continue block33;
                        }
                        case LF: {
                            this.handleField();
                            this.setState(State.FIRST_OCTETS);
                            this._partialBoundary = 2;
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("headerComplete {}", this);
                            }
                            if (!this._handler.headerComplete()) continue block33;
                            return true;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: {
                            this.handleField();
                            this.setState(FieldState.IN_NAME);
                            this._string.reset();
                            this._string.append(t.getChar());
                            this._length = 1;
                            continue block33;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case IN_NAME: {
                    switch (t.getType()) {
                        case COLON: {
                            this._fieldName = this.takeString();
                            this._length = -1;
                            this.setState(FieldState.VALUE);
                            continue block33;
                        }
                        case SPACE: {
                            this.setState(FieldState.AFTER_NAME);
                            continue block33;
                        }
                        case LF: {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Line Feed in Name {}", this);
                            }
                            this.handleField();
                            this.setState(FieldState.FIELD);
                            continue block33;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: {
                            this._string.append(t.getChar());
                            this._length = this._string.length();
                            continue block33;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case AFTER_NAME: {
                    switch (t.getType()) {
                        case COLON: {
                            this._fieldName = this.takeString();
                            this._length = -1;
                            this.setState(FieldState.VALUE);
                            continue block33;
                        }
                        case LF: {
                            this._fieldName = this.takeString();
                            this._string.reset();
                            this._fieldValue = "";
                            this._length = -1;
                            continue block33;
                        }
                        case SPACE: {
                            continue block33;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case VALUE: {
                    switch (t.getType()) {
                        case LF: {
                            this._string.reset();
                            this._fieldValue = "";
                            this._length = -1;
                            this.setState(FieldState.FIELD);
                            continue block33;
                        }
                        case HTAB: 
                        case SPACE: {
                            continue block33;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case OTEXT: 
                        case COLON: {
                            this._string.append(t.getByte());
                            this._length = this._string.length();
                            this.setState(FieldState.IN_VALUE);
                            continue block33;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case IN_VALUE: {
                    switch (t.getType()) {
                        case HTAB: 
                        case SPACE: {
                            this._string.append(' ');
                            continue block33;
                        }
                        case LF: {
                            if (this._length > 0) {
                                this._fieldValue = this.takeString();
                                this._length = -1;
                                this._totalHeaderLineLength = -1;
                            }
                            this.setState(FieldState.FIELD);
                            continue block33;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case OTEXT: 
                        case COLON: {
                            this._string.append(t.getByte());
                            this._length = this._string.length();
                            continue block33;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
            }
            throw new IllegalStateException(this._state.toString());
        }
        return false;
    }

    private void handleField() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("parsedField:  _fieldName={} _fieldValue={} {}", this._fieldName, this._fieldValue, this);
        }
        if (this._fieldName != null && this._fieldValue != null) {
            this._handler.parsedField(this._fieldName, this._fieldValue);
        }
        this._fieldValue = null;
        this._fieldName = null;
    }

    protected boolean parseOctetContent(ByteBuffer buffer) {
        int delimiter;
        ByteBuffer content;
        if (LOG.isDebugEnabled()) {
            LOG.debug("parseOctetContent({})", BufferUtil.toDetailString(buffer));
        }
        if (this._partialBoundary > 0) {
            int partial = this._delimiterSearch.startsWith(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining(), this._partialBoundary);
            if (partial > 0) {
                if (partial == this._delimiterSearch.getLength()) {
                    buffer.position(buffer.position() + this._delimiterSearch.getLength() - this._partialBoundary);
                    this.setState(State.DELIMITER);
                    this._partialBoundary = 0;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Content={}, Last={} {}", BufferUtil.toDetailString(BufferUtil.EMPTY_BUFFER), true, this);
                    }
                    return this._handler.content(BufferUtil.EMPTY_BUFFER, true);
                }
                this._partialBoundary = partial;
                BufferUtil.clear(buffer);
                return false;
            }
            content = this._patternBuffer.slice();
            if (this._state == State.FIRST_OCTETS) {
                this.setState(State.OCTETS);
                content.position(2);
            }
            content.limit(this._partialBoundary);
            this._partialBoundary = 0;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Content={}, Last={} {}", BufferUtil.toDetailString(content), false, this);
            }
            if (this._handler.content(content, false)) {
                return true;
            }
        }
        if ((delimiter = this._delimiterSearch.match(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())) >= 0) {
            content = buffer.slice();
            content.limit(delimiter - buffer.arrayOffset() - buffer.position());
            buffer.position(delimiter - buffer.arrayOffset() + this._delimiterSearch.getLength());
            this.setState(State.DELIMITER);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Content={}, Last={} {}", BufferUtil.toDetailString(content), true, this);
            }
            return this._handler.content(content, true);
        }
        this._partialBoundary = this._delimiterSearch.endsWith(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        if (this._partialBoundary > 0) {
            content = buffer.slice();
            content.limit(content.limit() - this._partialBoundary);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Content={}, Last={} {}", BufferUtil.toDetailString(content), false, this);
            }
            BufferUtil.clear(buffer);
            return this._handler.content(content, false);
        }
        content = buffer.slice();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Content={}, Last={} {}", BufferUtil.toDetailString(content), false, this);
        }
        BufferUtil.clear(buffer);
        return this._handler.content(content, false);
    }

    private void setState(State state) {
        if (this.debug) {
            LOG.debug("{} --> {}", new Object[]{this._state, state});
        }
        this._state = state;
    }

    private void setState(FieldState state) {
        if (this.debug) {
            LOG.debug("{}:{} --> {}", new Object[]{this._state, this._fieldState, state});
        }
        this._fieldState = state;
    }

    public String toString() {
        return String.format("%s{s=%s}", new Object[]{this.getClass().getSimpleName(), this._state});
    }

    private static class IllegalCharacterException
    extends BadMessageException {
        private IllegalCharacterException(State state, HttpTokens.Token token, ByteBuffer buffer) {
            super(400, String.format("Illegal character %s", token));
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Illegal character %s in state=%s for buffer %s", new Object[]{token, state, BufferUtil.toDetailString(buffer)}), new Object[0]);
            }
        }
    }

    public static interface Handler {
        default public void startPart() {
        }

        default public void parsedField(String name, String value) {
        }

        default public boolean headerComplete() {
            return false;
        }

        default public boolean content(ByteBuffer item, boolean last) {
            return false;
        }

        default public boolean messageComplete() {
            return false;
        }

        default public void earlyEOF() {
        }
    }

    public static enum State {
        PREAMBLE,
        DELIMITER,
        DELIMITER_PADDING,
        DELIMITER_CLOSE,
        BODY_PART,
        FIRST_OCTETS,
        OCTETS,
        EPILOGUE,
        END;

    }

    public static enum FieldState {
        FIELD,
        IN_NAME,
        AFTER_NAME,
        VALUE,
        IN_VALUE;

    }
}

