/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.http.HttpComplianceSection;

public enum HttpCompliance {
    LEGACY(HttpCompliance.sectionsBySpec("0,METHOD_CASE_SENSITIVE")),
    RFC2616_LEGACY(HttpCompliance.sectionsBySpec("RFC2616,-FIELD_COLON,-METHOD_CASE_SENSITIVE,-TRANSFER_ENCODING_WITH_CONTENT_LENGTH,-MULTIPLE_CONTENT_LENGTHS")),
    RFC2616(HttpCompliance.sectionsBySpec("RFC2616")),
    RFC7230_LEGACY(HttpCompliance.sectionsBySpec("RFC7230,-METHOD_CASE_SENSITIVE")),
    RFC7230(HttpCompliance.sectionsBySpec("RFC7230")),
    RFC7230_NO_AMBIGUOUS_URIS(HttpCompliance.sectionsBySpec("RFC7230,NO_AMBIGUOUS_PATH_SEGMENTS,NO_AMBIGUOUS_PATH_SEPARATORS")),
    CUSTOM0(HttpCompliance.sectionsByProperty("CUSTOM0")),
    CUSTOM1(HttpCompliance.sectionsByProperty("CUSTOM1")),
    CUSTOM2(HttpCompliance.sectionsByProperty("CUSTOM2")),
    CUSTOM3(HttpCompliance.sectionsByProperty("CUSTOM3"));

    public static final String VIOLATIONS_ATTR = "org.eclipse.jetty.http.compliance.violations";
    private static final Map<HttpComplianceSection, HttpCompliance> __required;
    private final EnumSet<HttpComplianceSection> _sections;

    private static EnumSet<HttpComplianceSection> sectionsByProperty(String property) {
        String s2 = System.getProperty(HttpCompliance.class.getName() + property);
        return HttpCompliance.sectionsBySpec(s2 == null ? "*" : s2);
    }

    static EnumSet<HttpComplianceSection> sectionsBySpec(String spec) {
        EnumSet<HttpComplianceSection> sections;
        String[] elements = spec.split("\\s*,\\s*");
        int i = 0;
        switch (elements[i]) {
            case "0": {
                sections = EnumSet.noneOf(HttpComplianceSection.class);
                ++i;
                break;
            }
            case "RFC2616": {
                ++i;
                sections = EnumSet.complementOf(EnumSet.of(HttpComplianceSection.NO_FIELD_FOLDING, HttpComplianceSection.NO_HTTP_0_9, HttpComplianceSection.NO_AMBIGUOUS_PATH_SEGMENTS, HttpComplianceSection.NO_AMBIGUOUS_PATH_SEPARATORS));
                break;
            }
            case "*": 
            case "RFC7230": {
                ++i;
                sections = EnumSet.complementOf(EnumSet.of(HttpComplianceSection.NO_AMBIGUOUS_PATH_SEGMENTS, HttpComplianceSection.NO_AMBIGUOUS_PATH_SEPARATORS));
                break;
            }
            default: {
                sections = EnumSet.noneOf(HttpComplianceSection.class);
            }
        }
        while (i < elements.length) {
            String element;
            boolean exclude;
            if (exclude = (element = elements[i++]).startsWith("-")) {
                element = element.substring(1);
            }
            HttpComplianceSection section = HttpComplianceSection.valueOf(element);
            if (exclude) {
                sections.remove((Object)section);
                continue;
            }
            sections.add(section);
        }
        return sections;
    }

    public static HttpCompliance requiredCompliance(HttpComplianceSection section) {
        return __required.get((Object)section);
    }

    private HttpCompliance(EnumSet<HttpComplianceSection> sections) {
        this._sections = sections;
    }

    public EnumSet<HttpComplianceSection> sections() {
        return this._sections;
    }

    static {
        __required = new HashMap<HttpComplianceSection, HttpCompliance>();
        block0: for (HttpComplianceSection section : HttpComplianceSection.values()) {
            for (HttpCompliance compliance : HttpCompliance.values()) {
                if (!compliance.sections().contains((Object)section)) continue;
                __required.put(section, compliance);
                continue block0;
            }
        }
    }
}

