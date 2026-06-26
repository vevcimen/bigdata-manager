/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Part;
import org.eclipse.jetty.http.MultiPartParser;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.ByteArrayOutputStream2;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MultiPartFormInputStream {
    private static final Logger LOG = Log.getLogger(MultiPartFormInputStream.class);
    private static final MultiMap<Part> EMPTY_MAP = new MultiMap(Collections.emptyMap());
    private final MultiMap<Part> _parts;
    private InputStream _in;
    private MultipartConfigElement _config;
    private String _contentType;
    private Throwable _err;
    private File _tmpDir;
    private File _contextTmpDir;
    private boolean _writeFilesWithFilenames;
    private boolean _parsed;
    private int _bufferSize = 16384;

    public MultiPartFormInputStream(InputStream in, String contentType, MultipartConfigElement config, File contextTmpDir) {
        this._contentType = contentType;
        this._config = config;
        this._contextTmpDir = contextTmpDir;
        if (this._contextTmpDir == null) {
            this._contextTmpDir = new File(System.getProperty("java.io.tmpdir"));
        }
        if (this._config == null) {
            this._config = new MultipartConfigElement(this._contextTmpDir.getAbsolutePath());
        }
        MultiMap<Object> parts = new MultiMap();
        if (in instanceof ServletInputStream && ((ServletInputStream)in).isFinished()) {
            parts = EMPTY_MAP;
            this._parsed = true;
        }
        if (!this._parsed) {
            this._in = new BufferedInputStream(in);
        }
        this._parts = parts;
    }

    public boolean isEmpty() {
        if (this._parts == null) {
            return true;
        }
        Collection values = this._parts.values();
        for (List partList : values) {
            if (partList.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Deprecated
    public Collection<Part> getParsedParts() {
        if (this._parts == null) {
            return Collections.emptyList();
        }
        Collection values = this._parts.values();
        ArrayList<Part> parts = new ArrayList<Part>();
        for (List o : values) {
            List asList = LazyList.getList(o, false);
            parts.addAll(asList);
        }
        return parts;
    }

    public void deleteParts() {
        MultiException err = null;
        for (List parts : this._parts.values()) {
            for (Part p : parts) {
                try {
                    ((MultiPart)p).cleanUp();
                }
                catch (Exception e) {
                    if (err == null) {
                        err = new MultiException();
                    }
                    err.add(e);
                }
            }
        }
        this._parts.clear();
        if (err != null) {
            err.ifExceptionThrowRuntime();
        }
    }

    public Collection<Part> getParts() throws IOException {
        if (!this._parsed) {
            this.parse();
        }
        this.throwIfError();
        if (this._parts.isEmpty()) {
            return Collections.emptyList();
        }
        Collection values = this._parts.values();
        ArrayList<Part> parts = new ArrayList<Part>();
        for (List o : values) {
            List asList = LazyList.getList(o, false);
            parts.addAll(asList);
        }
        return parts;
    }

    public Part getPart(String name) throws IOException {
        if (!this._parsed) {
            this.parse();
        }
        this.throwIfError();
        return this._parts.getValue(name, 0);
    }

    protected void throwIfError() throws IOException {
        if (this._err != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("MultiPart parsing failure ", this._err);
            }
            this._err.addSuppressed(new Throwable());
            if (this._err instanceof IOException) {
                throw (IOException)this._err;
            }
            if (this._err instanceof IllegalStateException) {
                throw (IllegalStateException)this._err;
            }
            throw new IllegalStateException(this._err);
        }
    }

    protected void parse() {
        block14: {
            if (this._parsed) {
                return;
            }
            this._parsed = true;
            MultiPartParser parser = null;
            Handler handler = new Handler();
            try {
                block13: {
                    File f;
                    if (this._contentType == null || !this._contentType.startsWith("multipart/form-data")) {
                        return;
                    }
                    this._tmpDir = this._config.getLocation() == null ? this._contextTmpDir : ("".equals(this._config.getLocation()) ? this._contextTmpDir : ((f = new File(this._config.getLocation())).isAbsolute() ? f : new File(this._contextTmpDir, this._config.getLocation())));
                    if (!this._tmpDir.exists()) {
                        this._tmpDir.mkdirs();
                    }
                    String contentTypeBoundary = "";
                    int bstart = this._contentType.indexOf("boundary=");
                    if (bstart >= 0) {
                        int bend = this._contentType.indexOf(";", bstart);
                        bend = bend < 0 ? this._contentType.length() : bend;
                        contentTypeBoundary = QuotedStringTokenizer.unquote(MultiPartFormInputStream.value(this._contentType.substring(bstart, bend)).trim());
                    }
                    parser = new MultiPartParser(handler, contentTypeBoundary);
                    byte[] data = new byte[this._bufferSize];
                    long total = 0L;
                    while (true) {
                        int len;
                        if ((len = this._in.read(data)) > 0) {
                            if (this._config.getMaxRequestSize() > 0L && (total += (long)len) > this._config.getMaxRequestSize()) {
                                this._err = new IllegalStateException("Request exceeds maxRequestSize (" + this._config.getMaxRequestSize() + ")");
                                return;
                            }
                            ByteBuffer buffer = BufferUtil.toBuffer(data);
                            buffer.limit(len);
                            if (!parser.parse(buffer, false)) {
                                if (!buffer.hasRemaining()) continue;
                                throw new IllegalStateException("Buffer did not fully consume");
                            }
                            break block13;
                        }
                        if (len == -1) break;
                    }
                    parser.parse(BufferUtil.EMPTY_BUFFER, true);
                }
                if (this._err != null) {
                    return;
                }
                if (parser.getState() != MultiPartParser.State.END) {
                    this._err = parser.getState() == MultiPartParser.State.PREAMBLE ? new IOException("Missing initial multi part boundary") : new IOException("Incomplete Multipart");
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Parsing Complete {} err={}", parser, this._err);
                }
            }
            catch (Throwable e) {
                this._err = e;
                if (parser == null) break block14;
                parser.parse(BufferUtil.EMPTY_BUFFER, true);
            }
        }
    }

    @Deprecated
    public void setDeleteOnExit(boolean deleteOnExit) {
    }

    public void setWriteFilesWithFilenames(boolean writeFilesWithFilenames) {
        this._writeFilesWithFilenames = writeFilesWithFilenames;
    }

    public boolean isWriteFilesWithFilenames() {
        return this._writeFilesWithFilenames;
    }

    @Deprecated
    public boolean isDeleteOnExit() {
        return false;
    }

    private static String value(String nameEqualsValue) {
        int idx = nameEqualsValue.indexOf(61);
        String value = nameEqualsValue.substring(idx + 1).trim();
        return QuotedStringTokenizer.unquoteOnly(value);
    }

    private static String filenameValue(String nameEqualsValue) {
        int idx = nameEqualsValue.indexOf(61);
        String value = nameEqualsValue.substring(idx + 1).trim();
        if (value.matches(".??[a-z,A-Z]\\:\\\\[^\\\\].*")) {
            char last;
            char first = value.charAt(0);
            if (first == '\"' || first == '\'') {
                value = value.substring(1);
            }
            if ((last = value.charAt(value.length() - 1)) == '\"' || last == '\'') {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }
        return QuotedStringTokenizer.unquoteOnly(value, true);
    }

    public int getBufferSize() {
        return this._bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this._bufferSize = bufferSize;
    }

    class Handler
    implements MultiPartParser.Handler {
        private MultiPart _part = null;
        private String contentDisposition = null;
        private String contentType = null;
        private MultiMap<String> headers = new MultiMap();

        Handler() {
        }

        @Override
        public boolean messageComplete() {
            return true;
        }

        @Override
        public void parsedField(String key, String value) {
            this.headers.put(StringUtil.asciiToLowerCase(key), value);
            if (key.equalsIgnoreCase("content-disposition")) {
                this.contentDisposition = value;
            } else if (key.equalsIgnoreCase("content-type")) {
                this.contentType = value;
            }
        }

        @Override
        public boolean headerComplete() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("headerComplete {}", this);
            }
            try {
                boolean formData = false;
                if (this.contentDisposition == null) {
                    throw new IOException("Missing content-disposition");
                }
                QuotedStringTokenizer tok = new QuotedStringTokenizer(this.contentDisposition, ";", false, true);
                String name = null;
                String filename = null;
                while (tok.hasMoreTokens()) {
                    String t = tok.nextToken().trim();
                    String tl = StringUtil.asciiToLowerCase(t);
                    if (tl.startsWith("form-data")) {
                        formData = true;
                        continue;
                    }
                    if (tl.startsWith("name=")) {
                        name = MultiPartFormInputStream.value(t);
                        continue;
                    }
                    if (!tl.startsWith("filename=")) continue;
                    filename = MultiPartFormInputStream.filenameValue(t);
                }
                if (!formData) {
                    throw new IOException("Part not form-data");
                }
                if (name == null) {
                    throw new IOException("No name in part");
                }
                this._part = new MultiPart(name, filename);
                this._part.setHeaders(this.headers);
                this._part.setContentType(this.contentType);
                MultiPartFormInputStream.this._parts.add(name, this._part);
                try {
                    this._part.open();
                }
                catch (IOException e) {
                    MultiPartFormInputStream.this._err = e;
                    return true;
                }
            }
            catch (Exception e) {
                MultiPartFormInputStream.this._err = e;
                return true;
            }
            return false;
        }

        @Override
        public boolean content(ByteBuffer buffer, boolean last) {
            if (this._part == null) {
                return false;
            }
            if (BufferUtil.hasContent(buffer)) {
                try {
                    this._part.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
                }
                catch (IOException e) {
                    MultiPartFormInputStream.this._err = e;
                    return true;
                }
            }
            if (last) {
                try {
                    this._part.close();
                }
                catch (IOException e) {
                    MultiPartFormInputStream.this._err = e;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void startPart() {
            this.reset();
        }

        @Override
        public void earlyEOF() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Early EOF {}", MultiPartFormInputStream.this);
            }
            try {
                if (this._part != null) {
                    this._part.close();
                }
            }
            catch (IOException e) {
                LOG.warn("part could not be closed", e);
            }
        }

        public void reset() {
            this._part = null;
            this.contentDisposition = null;
            this.contentType = null;
            this.headers = new MultiMap();
        }
    }

    public class MultiPart
    implements Part {
        protected String _name;
        protected String _filename;
        protected File _file;
        protected OutputStream _out;
        protected ByteArrayOutputStream2 _bout;
        protected String _contentType;
        protected MultiMap<String> _headers;
        protected long _size = 0L;
        protected boolean _temporary = true;

        public MultiPart(String name, String filename) {
            this._name = name;
            this._filename = filename;
        }

        public String toString() {
            return String.format("Part{n=%s,fn=%s,ct=%s,s=%d,tmp=%b,file=%s}", this._name, this._filename, this._contentType, this._size, this._temporary, this._file);
        }

        protected void setContentType(String contentType) {
            this._contentType = contentType;
        }

        protected void open() throws IOException {
            if (MultiPartFormInputStream.this.isWriteFilesWithFilenames() && this._filename != null && !this._filename.trim().isEmpty()) {
                this.createFile();
            } else {
                this._bout = new ByteArrayOutputStream2();
                this._out = this._bout;
            }
        }

        protected void close() throws IOException {
            this._out.close();
        }

        protected void write(int b) throws IOException {
            if (MultiPartFormInputStream.this._config.getMaxFileSize() > 0L && this._size + 1L > MultiPartFormInputStream.this._config.getMaxFileSize()) {
                throw new IllegalStateException("Multipart Mime part " + this._name + " exceeds max filesize");
            }
            if (MultiPartFormInputStream.this._config.getFileSizeThreshold() > 0 && this._size + 1L > (long)MultiPartFormInputStream.this._config.getFileSizeThreshold() && this._file == null) {
                this.createFile();
            }
            this._out.write(b);
            ++this._size;
        }

        protected void write(byte[] bytes, int offset, int length) throws IOException {
            if (MultiPartFormInputStream.this._config.getMaxFileSize() > 0L && this._size + (long)length > MultiPartFormInputStream.this._config.getMaxFileSize()) {
                throw new IllegalStateException("Multipart Mime part " + this._name + " exceeds max filesize");
            }
            if (MultiPartFormInputStream.this._config.getFileSizeThreshold() > 0 && this._size + (long)length > (long)MultiPartFormInputStream.this._config.getFileSizeThreshold() && this._file == null) {
                this.createFile();
            }
            this._out.write(bytes, offset, length);
            this._size += (long)length;
        }

        protected void createFile() throws IOException {
            Path parent = MultiPartFormInputStream.this._tmpDir.toPath();
            Path tempFile = Files.createTempFile(parent, "MultiPart", "", new FileAttribute[0]);
            this._file = tempFile.toFile();
            OutputStream fos = Files.newOutputStream(tempFile, StandardOpenOption.WRITE);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            if (this._size > 0L && this._out != null) {
                this._out.flush();
                this._bout.writeTo(bos);
                this._out.close();
            }
            this._bout = null;
            this._out = bos;
        }

        protected void setHeaders(MultiMap<String> headers) {
            this._headers = headers;
        }

        @Override
        public String getContentType() {
            return this._contentType;
        }

        @Override
        public String getHeader(String name) {
            if (name == null) {
                return null;
            }
            return this._headers.getValue(StringUtil.asciiToLowerCase(name), 0);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return this._headers.keySet();
        }

        @Override
        public Collection<String> getHeaders(String name) {
            List<String> headers = this._headers.getValues(name);
            return headers == null ? Collections.emptyList() : headers;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (this._file != null) {
                return new BufferedInputStream(new FileInputStream(this._file));
            }
            return new ByteArrayInputStream(this._bout.getBuf(), 0, this._bout.size());
        }

        @Override
        public String getSubmittedFileName() {
            return this.getContentDispositionFilename();
        }

        public byte[] getBytes() {
            if (this._bout != null) {
                return this._bout.toByteArray();
            }
            return null;
        }

        @Override
        public String getName() {
            return this._name;
        }

        @Override
        public long getSize() {
            return this._size;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(String fileName) throws IOException {
            block9: {
                if (this._file == null) {
                    this._temporary = false;
                    this._file = new File(MultiPartFormInputStream.this._tmpDir, fileName);
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this._file));){
                        this._bout.writeTo(bos);
                        bos.flush();
                        break block9;
                    }
                    finally {
                        this._bout = null;
                    }
                }
                this._temporary = false;
                Path src = this._file.toPath();
                Path target = src.resolveSibling(fileName);
                Files.move(src, target, StandardCopyOption.REPLACE_EXISTING);
                this._file = target.toFile();
            }
        }

        @Override
        public void delete() throws IOException {
            if (this._file != null && this._file.exists() && !this._file.delete()) {
                throw new IOException("Could Not Delete File");
            }
        }

        public void cleanUp() throws IOException {
            if (this._temporary) {
                this.delete();
            }
        }

        public File getFile() {
            return this._file;
        }

        public String getContentDispositionFilename() {
            return this._filename;
        }
    }
}

