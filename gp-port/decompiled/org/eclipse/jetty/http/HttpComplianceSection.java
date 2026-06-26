/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

public enum HttpComplianceSection {
    CASE_INSENSITIVE_FIELD_VALUE_CACHE("", "Use case insensitive field value cache"),
    METHOD_CASE_SENSITIVE("https://tools.ietf.org/html/rfc7230#section-3.1.1", "Method is case-sensitive"),
    FIELD_COLON("https://tools.ietf.org/html/rfc7230#section-3.2", "Fields must have a Colon"),
    FIELD_NAME_CASE_INSENSITIVE("https://tools.ietf.org/html/rfc7230#section-3.2", "Field name is case-insensitive"),
    NO_WS_AFTER_FIELD_NAME("https://tools.ietf.org/html/rfc7230#section-3.2.4", "Whitespace not allowed after field name"),
    NO_FIELD_FOLDING("https://tools.ietf.org/html/rfc7230#section-3.2.4", "No line Folding"),
    NO_HTTP_0_9("https://tools.ietf.org/html/rfc7230#appendix-A.2", "No HTTP/0.9"),
    TRANSFER_ENCODING_WITH_CONTENT_LENGTH("https://tools.ietf.org/html/rfc7230#section-3.3.1", "Transfer-Encoding and Content-Length"),
    MULTIPLE_CONTENT_LENGTHS("https://tools.ietf.org/html/rfc7230#section-3.3.1", "Multiple Content-Lengths"),
    NO_AMBIGUOUS_PATH_SEGMENTS("https://tools.ietf.org/html/rfc3986#section-3.3", "No ambiguous URI path segments"),
    NO_AMBIGUOUS_PATH_SEPARATORS("https://tools.ietf.org/html/rfc3986#section-3.3", "No ambiguous URI path separators"),
    NO_AMBIGUOUS_PATH_PARAMETERS("https://tools.ietf.org/html/rfc3986#section-3.3", "No ambiguous URI path parameters");

    final String url;
    final String description;

    private HttpComplianceSection(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public String getURL() {
        return this.url;
    }

    public String getDescription() {
        return this.description;
    }
}

