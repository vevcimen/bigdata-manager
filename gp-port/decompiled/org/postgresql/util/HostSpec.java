/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HostSpec {
    public static final String DEFAULT_NON_PROXY_HOSTS = "localhost|127.*|[::1]|0.0.0.0|[::0]";
    protected final @Nullable String localSocketAddress;
    protected final String host;
    protected final int port;

    public HostSpec(String host, int port) {
        this(host, port, null);
    }

    public HostSpec(String host, int port, @Nullable String localSocketAddress) {
        this.host = host;
        this.port = port;
        this.localSocketAddress = localSocketAddress;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return this.host + ":" + this.port;
    }

    public boolean equals(@Nullable Object obj) {
        return obj instanceof HostSpec && this.port == ((HostSpec)obj).port && this.host.equals(((HostSpec)obj).host) && Objects.equals(this.localSocketAddress, ((HostSpec)obj).localSocketAddress);
    }

    public int hashCode() {
        return Objects.hash(this.localSocketAddress, this.host, this.port);
    }

    public @Nullable String getLocalSocketAddress() {
        return this.localSocketAddress;
    }

    public Boolean shouldResolve() {
        String socksProxy = System.getProperty("socksProxyHost");
        if (socksProxy == null || socksProxy.trim().isEmpty()) {
            return true;
        }
        return this.matchesNonProxyHosts();
    }

    private Boolean matchesNonProxyHosts() {
        String nonProxyHosts = System.getProperty("socksNonProxyHosts", DEFAULT_NON_PROXY_HOSTS);
        if (nonProxyHosts == null || this.host.isEmpty()) {
            return false;
        }
        Pattern pattern = this.toPattern(nonProxyHosts);
        Matcher matcher = pattern == null ? null : pattern.matcher(this.host);
        return matcher != null && matcher.matches();
    }

    private @Nullable Pattern toPattern(String mask) {
        StringBuilder joiner = new StringBuilder();
        String separator = "";
        for (String disjunct : mask.split("\\|")) {
            if (disjunct.isEmpty()) continue;
            String regex = this.disjunctToRegex(disjunct.toLowerCase(Locale.ROOT));
            joiner.append(separator).append(regex);
            separator = "|";
        }
        return joiner.length() == 0 ? null : Pattern.compile(joiner.toString());
    }

    private String disjunctToRegex(String disjunct) {
        String regex = disjunct.startsWith("*") ? ".*" + Pattern.quote(disjunct.substring(1)) : (disjunct.endsWith("*") ? Pattern.quote(disjunct.substring(0, disjunct.length() - 1)) + ".*" : Pattern.quote(disjunct));
        return regex;
    }
}

