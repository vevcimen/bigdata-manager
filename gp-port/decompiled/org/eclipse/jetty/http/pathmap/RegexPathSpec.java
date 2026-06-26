/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http.pathmap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.http.pathmap.AbstractPathSpec;
import org.eclipse.jetty.http.pathmap.PathSpecGroup;

public class RegexPathSpec
extends AbstractPathSpec {
    private final String _declaration;
    private final PathSpecGroup _group;
    private final int _pathDepth;
    private final int _specLength;
    private final Pattern _pattern;

    public RegexPathSpec(String regex) {
        String declaration = regex.startsWith("regex|") ? regex.substring("regex|".length()) : regex;
        int specLength = declaration.length();
        boolean inGrouping = false;
        StringBuilder signature = new StringBuilder();
        int pathDepth = 0;
        block6: for (int i = 0; i < declaration.length(); ++i) {
            char c = declaration.charAt(i);
            switch (c) {
                case '[': {
                    inGrouping = true;
                    continue block6;
                }
                case ']': {
                    inGrouping = false;
                    signature.append('g');
                    continue block6;
                }
                case '*': {
                    signature.append('g');
                    continue block6;
                }
                case '/': {
                    if (inGrouping) continue block6;
                    ++pathDepth;
                    continue block6;
                }
                default: {
                    if (inGrouping || !Character.isLetterOrDigit(c)) continue block6;
                    signature.append('l');
                }
            }
        }
        Pattern pattern = Pattern.compile(declaration);
        String sig = signature.toString();
        PathSpecGroup group = Pattern.matches("^l*$", sig) ? PathSpecGroup.EXACT : (Pattern.matches("^l*g+", sig) ? PathSpecGroup.PREFIX_GLOB : (Pattern.matches("^g+l+$", sig) ? PathSpecGroup.SUFFIX_GLOB : PathSpecGroup.MIDDLE_GLOB));
        this._declaration = declaration;
        this._group = group;
        this._pathDepth = pathDepth;
        this._specLength = specLength;
        this._pattern = pattern;
    }

    protected Matcher getMatcher(String path) {
        return this._pattern.matcher(path);
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
        Matcher matcher;
        if (this._group == PathSpecGroup.PREFIX_GLOB && (matcher = this.getMatcher(path)).matches() && matcher.groupCount() >= 1) {
            String pathInfo = matcher.group(1);
            if ("".equals(pathInfo)) {
                return "/";
            }
            return pathInfo;
        }
        return null;
    }

    @Override
    public String getPathMatch(String path) {
        Matcher matcher = this.getMatcher(path);
        if (matcher.matches()) {
            int idx;
            if (matcher.groupCount() >= 1 && (idx = matcher.start(1)) > 0) {
                if (path.charAt(idx - 1) == '/') {
                    --idx;
                }
                return path.substring(0, idx);
            }
            return path;
        }
        return null;
    }

    @Override
    public String getDeclaration() {
        return this._declaration;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public String getSuffix() {
        return null;
    }

    public Pattern getPattern() {
        return this._pattern;
    }

    @Override
    public boolean matches(String path) {
        int idx = path.indexOf(63);
        if (idx >= 0) {
            return this.getMatcher(path.substring(0, idx)).matches();
        }
        return this.getMatcher(path).matches();
    }
}

