/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;

public class PathResource
extends Resource {
    private static final Logger LOG = Log.getLogger(PathResource.class);
    private static final LinkOption[] NO_FOLLOW_LINKS = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
    private static final LinkOption[] FOLLOW_LINKS = new LinkOption[0];
    private final Path path;
    private final Path alias;
    private final URI uri;
    private final boolean belongsToDefaultFileSystem;

    private final Path checkAliasPath() {
        Path abs = this.path;
        if (!URIUtil.equalsIgnoreEncodings(this.uri, this.path.toUri())) {
            try {
                return Paths.get(this.uri).toRealPath(FOLLOW_LINKS);
            }
            catch (IOException ignored) {
                LOG.ignore(ignored);
            }
        }
        if (!abs.isAbsolute()) {
            abs = this.path.toAbsolutePath();
        }
        try {
            Path real;
            if (Files.isSymbolicLink(this.path)) {
                return this.path.getParent().resolve(Files.readSymbolicLink(this.path));
            }
            if (Files.exists(this.path, new LinkOption[0]) && !PathResource.isSameName(abs, real = abs.toRealPath(FOLLOW_LINKS))) {
                return real;
            }
        }
        catch (IOException e) {
            LOG.ignore(e);
        }
        catch (Exception e) {
            LOG.warn("bad alias ({} {}) for {}", e.getClass().getName(), e.getMessage(), this.path);
        }
        return null;
    }

    public static boolean isSameName(Path pathA, Path pathB) {
        int bCount;
        int aCount = pathA.getNameCount();
        if (aCount != (bCount = pathB.getNameCount())) {
            return false;
        }
        int i = bCount;
        while (i-- > 0) {
            if (pathA.getName(i).toString().equals(pathB.getName(i).toString())) continue;
            return false;
        }
        return true;
    }

    public PathResource(File file) {
        this(file.toPath());
    }

    public PathResource(Path path) {
        Path absPath;
        block2: {
            absPath = path;
            try {
                absPath = path.toRealPath(NO_FOLLOW_LINKS);
            }
            catch (IOError | IOException e) {
                if (!LOG.isDebugEnabled()) break block2;
                LOG.debug("Unable to get real/canonical path for {}", path, e);
            }
        }
        this.path = absPath.normalize();
        this.assertValidPath(path);
        this.uri = this.path.toUri();
        this.alias = this.checkAliasPath();
        this.belongsToDefaultFileSystem = this.path.getFileSystem() == FileSystems.getDefault();
    }

    private PathResource(PathResource parent, String childPath) {
        this.path = parent.path.getFileSystem().getPath(parent.path.toString(), childPath);
        if (this.isDirectory() && !childPath.endsWith("/")) {
            childPath = childPath + "/";
        }
        this.uri = URIUtil.addPath(parent.uri, childPath);
        this.alias = this.checkAliasPath();
        this.belongsToDefaultFileSystem = this.path.getFileSystem() == FileSystems.getDefault();
    }

    public PathResource(URI uri) throws IOException {
        Path path;
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("not an absolute uri");
        }
        if (!uri.getScheme().equalsIgnoreCase("file")) {
            throw new IllegalArgumentException("not file: scheme");
        }
        try {
            path = Paths.get(uri);
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.ignore(e);
            throw new IOException("Unable to build Path from: " + uri, e);
        }
        this.path = path.toAbsolutePath();
        this.uri = path.toUri();
        this.alias = this.checkAliasPath();
        this.belongsToDefaultFileSystem = this.path.getFileSystem() == FileSystems.getDefault();
    }

    public PathResource(URL url) throws IOException, URISyntaxException {
        this(url.toURI());
    }

    @Override
    public boolean isSame(Resource resource) {
        block4: {
            try {
                if (resource instanceof PathResource) {
                    Path path = ((PathResource)resource).getPath();
                    return Files.isSameFile(this.getPath(), path);
                }
                if (resource instanceof FileResource) {
                    Path path = ((FileResource)resource).getFile().toPath();
                    return Files.isSameFile(this.getPath(), path);
                }
            }
            catch (IOException e) {
                if (!LOG.isDebugEnabled()) break block4;
                LOG.debug("ignored", e);
            }
        }
        return false;
    }

    @Override
    public Resource addPath(String subpath) throws IOException {
        String cpath = URIUtil.canonicalPath(subpath);
        if (cpath == null || cpath.length() == 0) {
            throw new MalformedURLException(subpath);
        }
        if ("/".equals(cpath)) {
            return this;
        }
        return new PathResource(this, subpath);
    }

    private void assertValidPath(Path path) {
        String str = path.toString();
        int idx = StringUtil.indexOfControlChars(str);
        if (idx >= 0) {
            throw new InvalidPathException(str, "Invalid Character at index " + idx);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean delete() throws SecurityException {
        try {
            return Files.deleteIfExists(this.path);
        }
        catch (IOException e) {
            LOG.ignore(e);
            return false;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PathResource other = (PathResource)obj;
        if (this.path == null) {
            return other.path == null;
        }
        return this.path.equals(other.path);
    }

    @Override
    public boolean exists() {
        return Files.exists(this.path, NO_FOLLOW_LINKS);
    }

    @Override
    public File getFile() throws IOException {
        if (!this.belongsToDefaultFileSystem) {
            return null;
        }
        return this.path.toFile();
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.path, StandardOpenOption.READ);
    }

    @Override
    public String getName() {
        return this.path.toAbsolutePath().toString();
    }

    @Override
    public ReadableByteChannel getReadableByteChannel() throws IOException {
        return this.newSeekableByteChannel();
    }

    public SeekableByteChannel newSeekableByteChannel() throws IOException {
        return Files.newByteChannel(this.path, StandardOpenOption.READ);
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public URL getURL() {
        try {
            return this.path.toUri().toURL();
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.path == null ? 0 : this.path.hashCode());
        return result;
    }

    @Override
    public boolean isContainedIn(Resource r) throws MalformedURLException {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(this.path, FOLLOW_LINKS);
    }

    @Override
    public long lastModified() {
        try {
            FileTime ft = Files.getLastModifiedTime(this.path, FOLLOW_LINKS);
            return ft.toMillis();
        }
        catch (IOException e) {
            LOG.ignore(e);
            return 0L;
        }
    }

    @Override
    public long length() {
        try {
            return Files.size(this.path);
        }
        catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public boolean isAlias() {
        return this.alias != null;
    }

    public Path getAliasPath() {
        return this.alias;
    }

    @Override
    public URI getAlias() {
        return this.alias == null ? null : this.alias.toUri();
    }

    @Override
    public String[] list() {
        String[] stringArray;
        block10: {
            DirectoryStream<Path> dir = Files.newDirectoryStream(this.path);
            try {
                ArrayList<String> entries = new ArrayList<String>();
                for (Path entry : dir) {
                    String name = entry.getFileName().toString();
                    if (Files.isDirectory(entry, new LinkOption[0])) {
                        name = name + "/";
                    }
                    entries.add(name);
                }
                int size = entries.size();
                stringArray = entries.toArray(new String[size]);
                if (dir == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (dir != null) {
                        try {
                            dir.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException | DirectoryIteratorException e) {
                    LOG.debug(e);
                    return null;
                }
            }
            dir.close();
        }
        return stringArray;
    }

    @Override
    public boolean renameTo(Resource dest) throws SecurityException {
        if (dest instanceof PathResource) {
            PathResource destRes = (PathResource)dest;
            try {
                Path result = Files.move(this.path, destRes.path, new CopyOption[0]);
                return Files.exists(result, NO_FOLLOW_LINKS);
            }
            catch (IOException e) {
                LOG.ignore(e);
                return false;
            }
        }
        return false;
    }

    @Override
    public void copyTo(File destination) throws IOException {
        if (this.isDirectory()) {
            IO.copyDir(this.path.toFile(), destination);
        } else {
            Files.copy(this.path, destination.toPath(), new CopyOption[0]);
        }
    }

    @Override
    public void writeTo(OutputStream outputStream, long start, long count) throws IOException {
        long length = count;
        if (count < 0L) {
            length = Files.size(this.path) - start;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(this.path, StandardOpenOption.READ);){
            int readLen;
            ByteBuffer buffer = BufferUtil.allocate(65536);
            this.skipTo(channel, buffer, start);
            for (long readTotal = 0L; readTotal < length; readTotal += (long)readLen) {
                BufferUtil.clearToFill(buffer);
                int size = (int)Math.min(65536L, length - readTotal);
                buffer.limit(size);
                readLen = channel.read(buffer);
                BufferUtil.flipToFlush(buffer, 0);
                BufferUtil.writeTo(buffer, outputStream);
            }
        }
    }

    private void skipTo(SeekableByteChannel channel, ByteBuffer buffer, long skipTo) throws IOException {
        block6: {
            try {
                if (channel.position() != skipTo) {
                    channel.position(skipTo);
                }
            }
            catch (UnsupportedOperationException e) {
                int NO_PROGRESS_LIMIT = 3;
                if (skipTo <= 0L) break block6;
                long pos = 0L;
                int noProgressLoopLimit = 3;
                while (noProgressLoopLimit > 0 && pos < skipTo) {
                    BufferUtil.clearToFill(buffer);
                    int len = (int)Math.min(65536L, skipTo - pos);
                    buffer.limit(len);
                    long readLen = channel.read(buffer);
                    if (readLen == 0L) {
                        --noProgressLoopLimit;
                        continue;
                    }
                    if (readLen > 0L) {
                        pos += readLen;
                        noProgressLoopLimit = 3;
                        continue;
                    }
                    throw new IOException("EOF reached before SeekableByteChannel skip destination");
                }
                if (noProgressLoopLimit > 0) break block6;
                throw new IOException("No progress made to reach SeekableByteChannel skip position " + skipTo);
            }
        }
    }

    public String toString() {
        return this.uri.toASCIIString();
    }
}

