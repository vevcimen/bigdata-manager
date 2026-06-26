/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import shadeio.univocity.parsers.common.processor.core.AbstractProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;

public final class NoopProcessor
extends AbstractProcessor {
    public static final Processor instance = new NoopProcessor();

    private NoopProcessor() {
    }
}

