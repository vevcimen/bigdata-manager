/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import org.eclipse.jetty.http.MultiPartFormInputStream;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

public interface MultiParts
extends Closeable {
    public Collection<Part> getParts() throws IOException;

    public Part getPart(String var1) throws IOException;

    public boolean isEmpty();

    public ContextHandler.Context getContext();

    public static class MultiPartsUtilParser
    implements MultiParts {
        private final MultiPartInputStreamParser _utilParser;
        private final ContextHandler.Context _context;
        private final Request _request;

        public MultiPartsUtilParser(InputStream in, String contentType, MultipartConfigElement config, File contextTmpDir, Request request) throws IOException {
            this._utilParser = new MultiPartInputStreamParser(in, contentType, config, contextTmpDir);
            this._context = request.getContext();
            this._request = request;
        }

        @Override
        public Collection<Part> getParts() throws IOException {
            Collection<Part> parts = this._utilParser.getParts();
            this.setNonComplianceViolationsOnRequest();
            return parts;
        }

        @Override
        public Part getPart(String name) throws IOException {
            Part part = this._utilParser.getPart(name);
            this.setNonComplianceViolationsOnRequest();
            return part;
        }

        @Override
        public void close() {
            this._utilParser.deleteParts();
        }

        @Override
        public boolean isEmpty() {
            return this._utilParser.getParsedParts().isEmpty();
        }

        @Override
        public ContextHandler.Context getContext() {
            return this._context;
        }

        private void setNonComplianceViolationsOnRequest() {
            ArrayList<String> violations = (ArrayList<String>)this._request.getAttribute("org.eclipse.jetty.http.compliance.violations");
            if (violations != null) {
                return;
            }
            EnumSet<MultiPartInputStreamParser.NonCompliance> nonComplianceWarnings = this._utilParser.getNonComplianceWarnings();
            violations = new ArrayList<String>();
            for (MultiPartInputStreamParser.NonCompliance nc : nonComplianceWarnings) {
                violations.add(nc.name() + ": " + nc.getURL());
            }
            this._request.setAttribute("org.eclipse.jetty.http.compliance.violations", violations);
        }
    }

    public static class MultiPartsHttpParser
    implements MultiParts {
        private final MultiPartFormInputStream _httpParser;
        private final ContextHandler.Context _context;

        public MultiPartsHttpParser(InputStream in, String contentType, MultipartConfigElement config, File contextTmpDir, Request request) throws IOException {
            this._httpParser = new MultiPartFormInputStream(in, contentType, config, contextTmpDir);
            this._context = request.getContext();
        }

        @Override
        public Collection<Part> getParts() throws IOException {
            return this._httpParser.getParts();
        }

        @Override
        public Part getPart(String name) throws IOException {
            return this._httpParser.getPart(name);
        }

        @Override
        public void close() {
            this._httpParser.deleteParts();
        }

        @Override
        public boolean isEmpty() {
            return this._httpParser.isEmpty();
        }

        @Override
        public ContextHandler.Context getContext() {
            return this._context;
        }
    }
}

