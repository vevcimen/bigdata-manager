/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.iterators;

import shadeio.univocity.parsers.common.AbstractParser;
import shadeio.univocity.parsers.common.iterators.ParserIterator;

public abstract class RowIterator
extends ParserIterator<String[]> {
    public RowIterator(AbstractParser parser) {
        super(parser);
    }

    @Override
    protected final String[] nextResult() {
        return this.parser.parseNext();
    }
}

