/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.processor.AbstractRowProcessor;
import shadeio.univocity.parsers.common.processor.RowProcessor;

public final class NoopRowProcessor
extends AbstractRowProcessor {
    public static final RowProcessor instance = new NoopRowProcessor();

    private NoopRowProcessor() {
    }
}

