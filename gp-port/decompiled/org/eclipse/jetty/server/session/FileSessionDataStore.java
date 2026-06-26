/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.session;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.eclipse.jetty.server.session.AbstractSessionDataStore;
import org.eclipse.jetty.server.session.SessionContext;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.UnreadableSessionDataException;
import org.eclipse.jetty.server.session.UnwriteableSessionDataException;
import org.eclipse.jetty.util.ClassLoadingObjectInputStream;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject
public class FileSessionDataStore
extends AbstractSessionDataStore {
    private static final Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");
    protected File _storeDir;
    protected boolean _deleteUnrestorableFiles = false;
    protected Map<String, String> _sessionFileMap = new ConcurrentHashMap<String, String>();
    protected String _contextString;
    protected long _lastSweepTime = 0L;

    @Override
    public void initialize(SessionContext context) throws Exception {
        super.initialize(context);
        this._contextString = this._context.getCanonicalContextPath() + "_" + this._context.getVhost();
    }

    @Override
    protected void doStart() throws Exception {
        this.initializeStore();
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        this._sessionFileMap.clear();
        this._lastSweepTime = 0L;
        super.doStop();
    }

    @ManagedAttribute(value="dir where sessions are stored", readonly=true)
    public File getStoreDir() {
        return this._storeDir;
    }

    public void setStoreDir(File storeDir) {
        this.checkStarted();
        this._storeDir = storeDir;
    }

    public boolean isDeleteUnrestorableFiles() {
        return this._deleteUnrestorableFiles;
    }

    public void setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        this.checkStarted();
        this._deleteUnrestorableFiles = deleteUnrestorableFiles;
    }

    @Override
    public boolean delete(String id) throws Exception {
        if (this._storeDir != null) {
            String filename = this._sessionFileMap.remove(this.getIdWithContext(id));
            if (filename == null) {
                return false;
            }
            return this.deleteFile(filename);
        }
        return false;
    }

    public boolean deleteFile(String filename) throws Exception {
        if (filename == null) {
            return false;
        }
        File file = new File(this._storeDir, filename);
        return Files.deleteIfExists(file.toPath());
    }

    @Override
    public Set<String> doGetExpired(Set<String> candidates) {
        long now = System.currentTimeMillis();
        HashSet<String> expired = new HashSet<String>();
        for (String filename : this._sessionFileMap.values()) {
            try {
                long expiry = this.getExpiryFromFilename(filename);
                if (expiry <= 0L || expiry >= now) continue;
                expired.add(this.getIdFromFilename(filename));
            }
            catch (Exception e) {
                LOG.warn(e);
            }
        }
        for (String c : candidates) {
            String filename;
            if (expired.contains(c) || (filename = this._sessionFileMap.get(this.getIdWithContext(c))) != null) continue;
            expired.add(c);
        }
        if (this._gracePeriodSec > 0 && (this._lastSweepTime == 0L || now - this._lastSweepTime >= 5L * TimeUnit.SECONDS.toMillis(this._gracePeriodSec))) {
            this._lastSweepTime = now;
            this.sweepDisk();
        }
        return expired;
    }

    public void sweepDisk() {
        long now = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sweeping {} for old session files", this._storeDir);
        }
        try (Stream<Path> stream = Files.walk(this._storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS);){
            stream.filter(p -> !Files.isDirectory(p, new LinkOption[0])).filter(p -> !this.isOurContextSessionFilename(p.getFileName().toString())).filter(p -> this.isSessionFilename(p.getFileName().toString())).forEach(p -> this.sweepFile(now, (Path)p));
        }
        catch (Exception e) {
            LOG.warn(e);
        }
    }

    public void sweepFile(long now, Path p) {
        block8: {
            if (p != null) {
                try {
                    long expiry = this.getExpiryFromFilename(p.getFileName().toString());
                    if (expiry <= 0L || now - expiry < 5L * TimeUnit.SECONDS.toMillis(this._gracePeriodSec)) break block8;
                    try {
                        if (!Files.deleteIfExists(p)) {
                            LOG.warn("Could not delete {}", p.getFileName());
                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug("Deleted {}", p.getFileName());
                        }
                    }
                    catch (IOException e) {
                        LOG.warn("Could not delete {}", p.getFileName(), e);
                    }
                }
                catch (NumberFormatException e) {
                    LOG.warn("Not valid session filename {}", p.getFileName());
                    LOG.warn(e);
                }
            }
        }
    }

    @Override
    public SessionData doLoad(String id) throws Exception {
        SessionData sessionData;
        String idWithContext = this.getIdWithContext(id);
        String filename = this._sessionFileMap.get(idWithContext);
        if (filename == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unknown file {}", idWithContext);
            }
            return null;
        }
        File file = new File(this._storeDir, filename);
        if (!file.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No such file {}", filename);
            }
            return null;
        }
        FileInputStream in = new FileInputStream(file);
        try {
            SessionData data = this.load(in, id);
            data.setLastSaved(file.lastModified());
            sessionData = data;
        }
        catch (Throwable data) {
            try {
                try {
                    in.close();
                }
                catch (Throwable throwable) {
                    data.addSuppressed(throwable);
                }
                throw data;
            }
            catch (UnreadableSessionDataException e) {
                if (this.isDeleteUnrestorableFiles() && file.exists() && file.getParentFile().equals(this._storeDir)) {
                    try {
                        this.delete(id);
                        LOG.warn("Deleted unrestorable file for session {}", id);
                    }
                    catch (Exception x) {
                        LOG.warn("Unable to delete unrestorable file {} for session {}", filename, id);
                        LOG.warn(x);
                    }
                }
                throw e;
            }
        }
        in.close();
        return sessionData;
    }

    @Override
    public void doStore(String id, SessionData data, long lastSaveTime) throws Exception {
        if (this._storeDir != null) {
            this.delete(id);
            String filename = this.getIdWithContextAndExpiry(data);
            String idWithContext = this.getIdWithContext(id);
            File file = new File(this._storeDir, filename);
            try (FileOutputStream fos = new FileOutputStream(file, false);){
                this.save(fos, id, data);
                this._sessionFileMap.put(idWithContext, filename);
            }
            catch (Exception e) {
                if (!file.delete()) {
                    e.addSuppressed(new IOException("Could not delete " + file));
                }
                throw new UnwriteableSessionDataException(id, this._context, e);
            }
        }
    }

    public void initializeStore() throws Exception {
        if (this._storeDir == null) {
            throw new IllegalStateException("No file store specified");
        }
        if (!this._storeDir.exists()) {
            if (!this._storeDir.mkdirs()) {
                throw new IllegalStateException("Could not create " + this._storeDir);
            }
        } else {
            if (!(this._storeDir.isDirectory() && this._storeDir.canWrite() && this._storeDir.canRead())) {
                throw new IllegalStateException(this._storeDir.getAbsolutePath() + " must be readable/writeable dir");
            }
            MultiException me = new MultiException();
            long now = System.currentTimeMillis();
            try (Stream<Path> stream = Files.walk(this._storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS);){
                stream.filter(p -> !Files.isDirectory(p, new LinkOption[0])).filter(p -> this.isSessionFilename(p.getFileName().toString())).forEach(p -> {
                    String existing;
                    String sessionIdWithContext;
                    this.sweepFile(now, (Path)p);
                    String filename = p.getFileName().toString();
                    String context = this.getContextFromFilename(filename);
                    if (Files.exists(p, new LinkOption[0]) && this._contextString.equals(context) && (sessionIdWithContext = this.getIdWithContextFromFilename(filename)) != null && (existing = this._sessionFileMap.putIfAbsent(sessionIdWithContext, filename)) != null) {
                        try {
                            long existingExpiry = this.getExpiryFromFilename(existing);
                            long thisExpiry = this.getExpiryFromFilename(filename);
                            if (thisExpiry > existingExpiry) {
                                Path existingPath = this._storeDir.toPath().resolve(existing);
                                this._sessionFileMap.put(sessionIdWithContext, filename);
                                Files.delete(existingPath);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Replaced {} with {}", existing, filename);
                                }
                            } else {
                                Files.delete(p);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Deleted expired session file {}", filename);
                                }
                            }
                        }
                        catch (IOException e) {
                            me.add(e);
                        }
                    }
                });
                me.ifExceptionThrow();
            }
        }
    }

    @Override
    @ManagedAttribute(value="are sessions serialized by this store", readonly=true)
    public boolean isPassivating() {
        return true;
    }

    @Override
    public boolean exists(String id) throws Exception {
        String idWithContext = this.getIdWithContext(id);
        String filename = this._sessionFileMap.get(idWithContext);
        if (filename == null) {
            return false;
        }
        long expiry = this.getExpiryFromFilename(filename);
        if (expiry <= 0L) {
            return true;
        }
        return expiry > System.currentTimeMillis();
    }

    protected void save(OutputStream os, String id, SessionData data) throws IOException {
        DataOutputStream out = new DataOutputStream(os);
        out.writeUTF(id);
        out.writeUTF(this._context.getCanonicalContextPath());
        out.writeUTF(this._context.getVhost());
        out.writeUTF(data.getLastNode());
        out.writeLong(data.getCreated());
        out.writeLong(data.getAccessed());
        out.writeLong(data.getLastAccessed());
        out.writeLong(data.getCookieSet());
        out.writeLong(data.getExpiry());
        out.writeLong(data.getMaxInactiveMs());
        ObjectOutputStream oos = new ObjectOutputStream(out);
        SessionData.serializeAttributes(data, oos);
    }

    protected String getIdWithContext(String id) {
        return this._contextString + "_" + id;
    }

    protected String getIdWithContextAndExpiry(SessionData data) {
        return "" + data.getExpiry() + "_" + this.getIdWithContext(data.getId());
    }

    protected String getIdFromFilename(String filename) {
        if (filename == null) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(95) + 1);
    }

    protected long getExpiryFromFilename(String filename) {
        if (StringUtil.isBlank(filename) || !filename.contains("_")) {
            throw new IllegalStateException("Invalid or missing filename");
        }
        String s2 = filename.substring(0, filename.indexOf(95));
        return Long.parseLong(s2);
    }

    protected String getContextFromFilename(String filename) {
        if (StringUtil.isBlank(filename)) {
            return null;
        }
        int start = filename.indexOf(95);
        int end = filename.lastIndexOf(95);
        return filename.substring(start + 1, end);
    }

    protected String getIdWithContextFromFilename(String filename) {
        if (StringUtil.isBlank(filename) || filename.indexOf(95) < 0) {
            return null;
        }
        return filename.substring(filename.indexOf(95) + 1);
    }

    protected boolean isSessionFilename(String filename) {
        if (StringUtil.isBlank(filename)) {
            return false;
        }
        String[] parts = filename.split("_");
        return parts.length >= 4;
    }

    protected boolean isOurContextSessionFilename(String filename) {
        if (StringUtil.isBlank(filename)) {
            return false;
        }
        String[] parts = filename.split("_");
        if (parts.length < 4) {
            return false;
        }
        String context = this.getContextFromFilename(filename);
        if (context == null) {
            return false;
        }
        return this._contextString.equals(context);
    }

    protected SessionData load(InputStream is, String expectedId) throws Exception {
        try {
            DataInputStream di = new DataInputStream(is);
            String id = di.readUTF();
            String contextPath = di.readUTF();
            String vhost = di.readUTF();
            String lastNode = di.readUTF();
            long created = di.readLong();
            long accessed = di.readLong();
            long lastAccessed = di.readLong();
            long cookieSet = di.readLong();
            long expiry = di.readLong();
            long maxIdle = di.readLong();
            SessionData data = this.newSessionData(id, created, accessed, lastAccessed, maxIdle);
            data.setContextPath(contextPath);
            data.setVhost(vhost);
            data.setLastNode(lastNode);
            data.setCookieSet(cookieSet);
            data.setExpiry(expiry);
            data.setMaxInactiveMs(maxIdle);
            ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(is);
            SessionData.deserializeAttributes(data, ois);
            return data;
        }
        catch (Exception e) {
            throw new UnreadableSessionDataException(expectedId, this._context, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[dir=%s,deleteUnrestorableFiles=%b]", super.toString(), this._storeDir, this._deleteUnrestorableFiles);
    }
}

