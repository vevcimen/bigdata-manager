/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

public class ProcessorUtils {
    public static final String AVAILABLE_PROCESSORS = "JETTY_AVAILABLE_PROCESSORS";
    private static int __availableProcessors = ProcessorUtils.init();

    static int init() {
        String processors = System.getProperty(AVAILABLE_PROCESSORS, System.getenv(AVAILABLE_PROCESSORS));
        if (processors != null) {
            try {
                return Integer.parseInt(processors);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return Runtime.getRuntime().availableProcessors();
    }

    public static int availableProcessors() {
        return __availableProcessors;
    }

    public static void setAvailableProcessors(int processors) {
        if (processors < 1) {
            throw new IllegalArgumentException("Invalid number of processors: " + processors);
        }
        __availableProcessors = processors;
    }
}

