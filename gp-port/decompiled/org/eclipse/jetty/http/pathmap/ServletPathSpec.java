/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http.pathmap;

import org.eclipse.jetty.http.pathmap.AbstractPathSpec;
import org.eclipse.jetty.http.pathmap.PathSpecGroup;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ServletPathSpec
extends AbstractPathSpec {
    private static final Logger LOG = Log.getLogger(ServletPathSpec.class);
    private final String _declaration;
    private final PathSpecGroup _group;
    private final int _pathDepth;
    private final int _specLength;
    private final String _prefix;
    private final String _suffix;

    public static String normalize(String pathSpec) {
        if (StringUtil.isNotBlank(pathSpec) && !pathSpec.startsWith("/") && !pathSpec.startsWith("*")) {
            return "/" + pathSpec;
        }
        return pathSpec;
    }

    public ServletPathSpec(String servletPathSpec) {
        String suffix;
        String prefix;
        PathSpecGroup group;
        if (servletPathSpec == null) {
            servletPathSpec = "";
        }
        if (servletPathSpec.startsWith("servlet|")) {
            servletPathSpec = servletPathSpec.substring("servlet|".length());
        }
        ServletPathSpec.assertValidServletPathSpec(servletPathSpec);
        if (servletPathSpec.isEmpty()) {
            this._declaration = "";
            this._group = PathSpecGroup.ROOT;
            this._pathDepth = -1;
            this._specLength = 1;
            this._prefix = null;
            this._suffix = null;
            return;
        }
        if ("/".equals(servletPathSpec)) {
            this._declaration = "/";
            this._group = PathSpecGroup.DEFAULT;
            this._pathDepth = -1;
            this._specLength = 1;
            this._prefix = null;
            this._suffix = null;
            return;
        }
        int specLength = servletPathSpec.length();
        if (servletPathSpec.charAt(0) == '/' && servletPathSpec.endsWith("/*")) {
            group = PathSpecGroup.PREFIX_GLOB;
            prefix = servletPathSpec.substring(0, specLength - 2);
            suffix = null;
        } else if (servletPathSpec.charAt(0) == '*' && servletPathSpec.length() > 1) {
            group = PathSpecGroup.SUFFIX_GLOB;
            prefix = null;
            suffix = servletPathSpec.substring(2, specLength);
        } else {
            group = PathSpecGroup.EXACT;
            prefix = servletPathSpec;
            suffix = null;
            if (servletPathSpec.endsWith("*")) {
                LOG.warn("Suspicious URL pattern: '{}'; see sections 12.1 and 12.2 of the Servlet specification", servletPathSpec);
            }
        }
        int pathDepth = 0;
        for (int i = 0; i < specLength; ++i) {
            char c;
            int cp = servletPathSpec.codePointAt(i);
            if (cp >= 128 || (c = (char)cp) != '/') continue;
            ++pathDepth;
        }
        this._declaration = servletPathSpec;
        this._group = group;
        this._pathDepth = pathDepth;
        this._specLength = specLength;
        this._prefix = prefix;
        this._suffix = suffix;
    }

    private static void assertValidServletPathSpec(String servletPathSpec) {
        if (servletPathSpec == null || servletPathSpec.equals("")) {
            return;
        }
        int len = servletPathSpec.length();
        if (servletPathSpec.charAt(0) == '/') {
            if (len == 1) {
                return;
            }
            int idx = servletPathSpec.indexOf(42);
            if (idx < 0) {
                return;
            }
            if (idx != len - 1) {
                throw new IllegalArgumentException("Servlet Spec 12.2 violation: glob '*' can only exist at end of prefix based matches: bad spec \"" + servletPathSpec + "\"");
            }
        } else if (servletPathSpec.startsWith("*.")) {
            int idx = servletPathSpec.indexOf(47);
            if (idx >= 0) {
                throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have path separators: bad spec \"" + servletPathSpec + "\"");
            }
            idx = servletPathSpec.indexOf(42, 2);
            if (idx >= 1) {
                throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have multiple glob '*': bad spec \"" + servletPathSpec + "\"");
            }
        } else {
            throw new IllegalArgumentException("Servlet Spec 12.2 violation: path spec must start with \"/\" or \"*.\": bad spec \"" + servletPathSpec + "\"");
        }
    }

    @Override
    public int getSpecLength() {
        return this._specLength;
    }

    @Override
    public PathSpecGroup getGroup() {
        return this._group;
    }

    @Override
    public int getPathDepth() {
        return this._pathDepth;
    }

    @Override
    public String getPathInfo(String path) {
        switch (this._group) {
            case ROOT: {
                return path;
            }
            case PREFIX_GLOB: {
                if (path.length() == this._specLength - 2) {
                    return null;
                }
                return path.substring(this._specLength - 2);
            }
        }
        return null;
    }

    @Override
    public String getPathMatch(String path) {
        switch (this._group) {
            case ROOT: {
                return "";
            }
            case EXACT: {
                if (this._declaration.equals(path)) {
                    return path;
                }
                return null;
            }
            case PREFIX_GLOB: {
                if (this.isWildcardMatch(path)) {
                    return path.substring(0, this._specLength - 2);
                }
                return null;
            }
            case SUFFIX_GLOB: {
                if (path.regionMatches(path.length() - (this._specLength - 1), this._declaration, 1, this._specLength - 1)) {
                    return path;
                }
                return null;
            }
            case DEFAULT: {
                return path;
            }
        }
        return null;
    }

    @Override
    public String getDeclaration() {
        return this._declaration;
    }

    @Override
    public String getPrefix() {
        return this._prefix;
    }

    @Override
    public String getSuffix() {
        return this._suffix;
    }

    private boolean isWildcardMatch(String path) {
        int cpl = this._specLength - 2;
        if (this._group == PathSpecGroup.PREFIX_GLOB && path.regionMatches(0, this._declaration, 0, cpl)) {
            return path.length() == cpl || '/' == path.charAt(cpl);
        }
        return false;
    }

    @Override
    public boolean matches(String path) {
        switch (this._group) {
            case EXACT: {
                return this._declaration.equals(path);
            }
            case PREFIX_GLOB: {
                return this.isWildcardMatch(path);
            }
            case SUFFIX_GLOB: {
                return path.regionMatches(path.length() - this._specLength + 1, this._declaration, 1, this._specLength - 1);
            }
            case ROOT: {
                return "/".equals(path);
            }
            case DEFAULT: {
                return true;
            }
        }
        return false;
    }
}

