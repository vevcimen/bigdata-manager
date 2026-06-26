/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import org.eclipse.jetty.util.StringUtil;

public class HostPort {
    private final String _host;
    private final int _port;

    public HostPort(String host, int port) {
        this._host = HostPort.normalizeHost(host);
        this._port = port;
    }

    public HostPort(String authority) throws IllegalArgumentException {
        if (authority == null) {
            throw new IllegalArgumentException("No Authority");
        }
        try {
            if (authority.isEmpty()) {
                this._host = authority;
                this._port = 0;
            } else if (authority.charAt(0) == '[') {
                int close = authority.lastIndexOf(93);
                if (close < 0) {
                    throw new IllegalArgumentException("Bad IPv6 host");
                }
                this._host = authority.substring(0, close + 1);
                if (authority.length() > close + 1) {
                    if (authority.charAt(close + 1) != ':') {
                        throw new IllegalArgumentException("Bad IPv6 port");
                    }
                    this._port = HostPort.parsePort(authority.substring(close + 2));
                } else {
                    this._port = 0;
                }
            } else {
                int c = authority.lastIndexOf(58);
                if (c >= 0) {
                    if (c != authority.indexOf(58)) {
                        this._host = "[" + authority + "]";
                        this._port = 0;
                    } else {
                        this._host = authority.substring(0, c);
                        this._port = HostPort.parsePort(authority.substring(c + 1));
                    }
                } else {
                    this._host = authority;
                    this._port = 0;
                }
            }
        }
        catch (IllegalArgumentException iae) {
            throw iae;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Bad HostPort", ex);
        }
    }

    public String getHost() {
        return this._host;
    }

    public int getPort() {
        return this._port;
    }

    public int getPort(int defaultPort) {
        return this._port > 0 ? this._port : defaultPort;
    }

    public String toString() {
        if (this._port > 0) {
            return this._host + ":" + this._port;
        }
        return this._host;
    }

    public static String normalizeHost(String host) {
        if (host.isEmpty() || host.charAt(0) == '[' || host.indexOf(58) < 0) {
            return host;
        }
        return "[" + host + "]";
    }

    public static int parsePort(String rawPort) throws IllegalArgumentException {
        if (StringUtil.isEmpty(rawPort)) {
            throw new IllegalArgumentException("Bad port");
        }
        int port = Integer.parseInt(rawPort);
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Bad port");
        }
        return port;
    }
}

