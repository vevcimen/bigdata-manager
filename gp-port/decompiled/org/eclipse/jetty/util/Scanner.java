/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import org.eclipse.jetty.util.IncludeExcludeSet;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class Scanner
extends AbstractLifeCycle {
    public static final int DEFAULT_SCAN_DEPTH = 1;
    public static final int MAX_SCAN_DEPTH = Integer.MAX_VALUE;
    private static final Logger LOG = Log.getLogger(Scanner.class);
    private static int __scannerId = 0;
    private int _scanInterval;
    private int _scanCount = 0;
    private final List<Listener> _listeners = new ArrayList<Listener>();
    private final Map<String, TimeNSize> _prevScan = new HashMap<String, TimeNSize>();
    private final Map<String, TimeNSize> _currentScan = new HashMap<String, TimeNSize>();
    private FilenameFilter _filter;
    private final Map<Path, IncludeExcludeSet<PathMatcher, Path>> _scannables = new HashMap<Path, IncludeExcludeSet<PathMatcher, Path>>();
    private volatile boolean _running = false;
    private boolean _reportExisting = true;
    private boolean _reportDirs = true;
    private Timer _timer;
    private TimerTask _task;
    private int _scanDepth = 1;
    private final Map<String, Notification> _notifications = new HashMap<String, Notification>();

    public synchronized int getScanInterval() {
        return this._scanInterval;
    }

    public synchronized void setScanInterval(int scanInterval) {
        this._scanInterval = scanInterval;
        this.schedule();
    }

    public void setScanDirs(List<File> dirs) {
        this._scannables.clear();
        if (dirs == null) {
            return;
        }
        for (File f : dirs) {
            this.addScanDir(f);
        }
    }

    @Deprecated
    public synchronized void addScanDir(File dir) {
        if (dir == null) {
            return;
        }
        try {
            if (dir.isDirectory()) {
                this.addDirectory(dir.toPath());
            } else {
                this.addFile(dir.toPath());
            }
        }
        catch (Exception e) {
            LOG.warn(e);
        }
    }

    public synchronized void addFile(Path p) throws IOException {
        if (p == null) {
            throw new IllegalStateException("Null path");
        }
        File f = p.toFile();
        if (!f.exists() || f.isDirectory()) {
            throw new IllegalStateException("Not file or doesn't exist: " + f.getCanonicalPath());
        }
        this._scannables.put(p, null);
    }

    public synchronized IncludeExcludeSet<PathMatcher, Path> addDirectory(Path p) throws IOException {
        if (p == null) {
            throw new IllegalStateException("Null path");
        }
        File f = p.toFile();
        if (!f.exists() || !f.isDirectory()) {
            throw new IllegalStateException("Not directory or doesn't exist: " + f.getCanonicalPath());
        }
        IncludeExcludeSet<PathMatcher, Path> includesExcludes = this._scannables.get(p);
        if (includesExcludes == null) {
            includesExcludes = new IncludeExcludeSet(PathMatcherSet.class);
            this._scannables.put(p.toRealPath(new LinkOption[0]), includesExcludes);
        }
        return includesExcludes;
    }

    @Deprecated
    public List<File> getScanDirs() {
        ArrayList<File> files = new ArrayList<File>();
        for (Path p : this._scannables.keySet()) {
            files.add(p.toFile());
        }
        return Collections.unmodifiableList(files);
    }

    public Set<Path> getScannables() {
        return this._scannables.keySet();
    }

    @Deprecated
    public void setRecursive(boolean recursive) {
        this._scanDepth = recursive ? Integer.MAX_VALUE : 1;
    }

    @Deprecated
    public boolean getRecursive() {
        return this._scanDepth > 1;
    }

    public int getScanDepth() {
        return this._scanDepth;
    }

    public void setScanDepth(int scanDepth) {
        this._scanDepth = scanDepth;
    }

    @Deprecated
    public void setFilenameFilter(FilenameFilter filter) {
        this._filter = filter;
    }

    @Deprecated
    public FilenameFilter getFilenameFilter() {
        return this._filter;
    }

    public void setReportExistingFilesOnStartup(boolean reportExisting) {
        this._reportExisting = reportExisting;
    }

    public boolean getReportExistingFilesOnStartup() {
        return this._reportExisting;
    }

    public void setReportDirs(boolean dirs) {
        this._reportDirs = dirs;
    }

    public boolean getReportDirs() {
        return this._reportDirs;
    }

    public synchronized void addListener(Listener listener) {
        if (listener == null) {
            return;
        }
        this._listeners.add(listener);
    }

    public synchronized void removeListener(Listener listener) {
        if (listener == null) {
            return;
        }
        this._listeners.remove(listener);
    }

    @Override
    public synchronized void doStart() {
        if (this._running) {
            return;
        }
        this._running = true;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Scanner start: rprtExists={}, depth={}, rprtDirs={}, interval={}, filter={}, scannables={}", this._reportExisting, this._scanDepth, this._reportDirs, this._scanInterval, this._filter, this._scannables);
        }
        if (this._reportExisting) {
            this.scan();
            this.scan();
        } else {
            this.scanFiles();
            this._prevScan.putAll(this._currentScan);
        }
        this.schedule();
    }

    public TimerTask newTimerTask() {
        return new TimerTask(){

            @Override
            public void run() {
                Scanner.this.scan();
            }
        };
    }

    public Timer newTimer() {
        return new Timer("Scanner-" + __scannerId++, true);
    }

    public void schedule() {
        if (this._running) {
            if (this._timer != null) {
                this._timer.cancel();
            }
            if (this._task != null) {
                this._task.cancel();
            }
            if (this.getScanInterval() > 0) {
                this._timer = this.newTimer();
                this._task = this.newTimerTask();
                this._timer.schedule(this._task, 1010L * (long)this.getScanInterval(), 1010L * (long)this.getScanInterval());
            }
        }
    }

    @Override
    public synchronized void doStop() {
        if (this._running) {
            this._running = false;
            if (this._timer != null) {
                this._timer.cancel();
            }
            if (this._task != null) {
                this._task.cancel();
            }
            this._task = null;
            this._timer = null;
        }
    }

    public void reset() {
        if (!this.isStopped()) {
            throw new IllegalStateException("Not stopped");
        }
        this._scannables.clear();
        this._currentScan.clear();
        this._prevScan.clear();
    }

    public boolean exists(String path) {
        for (Path p : this._scannables.keySet()) {
            if (!p.resolve(path).toFile().exists()) continue;
            return true;
        }
        return false;
    }

    public synchronized void scan() {
        this.reportScanStart(++this._scanCount);
        this.scanFiles();
        this.reportDifferences(this._currentScan, this._prevScan);
        this._prevScan.clear();
        this._prevScan.putAll(this._currentScan);
        this.reportScanEnd(this._scanCount);
        for (Listener l : this._listeners) {
            try {
                if (!(l instanceof ScanListener)) continue;
                ((ScanListener)l).scan();
            }
            catch (Throwable e) {
                LOG.warn(e);
            }
        }
    }

    public synchronized void scanFiles() {
        this._currentScan.clear();
        for (Map.Entry<Path, IncludeExcludeSet<PathMatcher, Path>> entry : this._scannables.entrySet()) {
            Path p = entry.getKey();
            try {
                Files.walkFileTree(p, EnumSet.allOf(FileVisitOption.class), this._scanDepth, new Visitor(p, entry.getValue(), this._currentScan));
            }
            catch (IOException e) {
                LOG.warn("Error scanning files.", e);
            }
        }
    }

    private synchronized void reportDifferences(Map<String, TimeNSize> currentScan, Map<String, TimeNSize> oldScan) {
        HashSet<String> oldScanKeys = new HashSet<String>(oldScan.keySet());
        for (Map.Entry<String, TimeNSize> entry : currentScan.entrySet()) {
            Notification old;
            String file = entry.getKey();
            if (!oldScanKeys.contains(file)) {
                old = this._notifications.put(file, Notification.ADDED);
                if (old == null) continue;
                switch (old) {
                    case REMOVED: 
                    case CHANGED: {
                        this._notifications.put(file, Notification.CHANGED);
                    }
                }
                continue;
            }
            if (oldScan.get(file).equals(currentScan.get(file)) || (old = this._notifications.put(file, Notification.CHANGED)) != Notification.ADDED) continue;
            this._notifications.put(file, Notification.ADDED);
        }
        for (String file : oldScan.keySet()) {
            Notification old;
            if (currentScan.containsKey(file) || (old = this._notifications.put(file, Notification.REMOVED)) != Notification.ADDED) continue;
            this._notifications.remove(file);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("scanned " + this._scannables.keySet() + ": " + this._notifications, new Object[0]);
        }
        ArrayList<String> bulkChanges = new ArrayList<String>();
        Iterator<Map.Entry<String, Notification>> iter = this._notifications.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Notification> entry = iter.next();
            String file = entry.getKey();
            if (oldScan.containsKey(file) ? !oldScan.get(file).equals(currentScan.get(file)) : currentScan.containsKey(file)) continue;
            Notification notification = entry.getValue();
            iter.remove();
            bulkChanges.add(file);
            switch (notification) {
                case ADDED: {
                    this.reportAddition(file);
                    break;
                }
                case CHANGED: {
                    this.reportChange(file);
                    break;
                }
                case REMOVED: {
                    this.reportRemoval(file);
                }
            }
        }
        if (!bulkChanges.isEmpty()) {
            this.reportBulkChanges(bulkChanges);
        }
    }

    private void warn(Object listener, String filename, Throwable th) {
        LOG.warn(listener + " failed on '" + filename, th);
    }

    private void reportAddition(String filename) {
        for (Listener l : this._listeners) {
            try {
                if (!(l instanceof DiscreteListener)) continue;
                ((DiscreteListener)l).fileAdded(filename);
            }
            catch (Throwable e) {
                this.warn(l, filename, e);
            }
        }
    }

    private void reportRemoval(String filename) {
        for (Listener l : this._listeners) {
            try {
                if (!(l instanceof DiscreteListener)) continue;
                ((DiscreteListener)l).fileRemoved(filename);
            }
            catch (Throwable e) {
                this.warn(l, filename, e);
            }
        }
    }

    private void reportChange(String filename) {
        for (Listener l : this._listeners) {
            try {
                if (!(l instanceof DiscreteListener)) continue;
                ((DiscreteListener)l).fileChanged(filename);
            }
            catch (Throwable e) {
                this.warn(l, filename, e);
            }
        }
    }

    private void reportBulkChanges(List<String> filenames) {
        for (Listener l : this._listeners) {
            try {
                if (!(l instanceof BulkListener)) continue;
                ((BulkListener)l).filesChanged(filenames);
            }
            catch (Throwable e) {
                this.warn(l, filenames.toString(), e);
            }
        }
    }

    private void reportScanStart(int cycle) {
        for (Listener listener : this._listeners) {
            try {
                if (!(listener instanceof ScanCycleListener)) continue;
                ((ScanCycleListener)listener).scanStarted(cycle);
            }
            catch (Exception e) {
                LOG.warn(listener + " failed on scan start for cycle " + cycle, e);
            }
        }
    }

    private void reportScanEnd(int cycle) {
        for (Listener listener : this._listeners) {
            try {
                if (!(listener instanceof ScanCycleListener)) continue;
                ((ScanCycleListener)listener).scanEnded(cycle);
            }
            catch (Exception e) {
                LOG.warn(listener + " failed on scan end for cycle " + cycle, e);
            }
        }
    }

    public static interface ScanCycleListener
    extends Listener {
        public void scanStarted(int var1) throws Exception;

        public void scanEnded(int var1) throws Exception;
    }

    public static interface BulkListener
    extends Listener {
        public void filesChanged(List<String> var1) throws Exception;
    }

    public static interface DiscreteListener
    extends Listener {
        public void fileChanged(String var1) throws Exception;

        public void fileAdded(String var1) throws Exception;

        public void fileRemoved(String var1) throws Exception;
    }

    public static interface ScanListener
    extends Listener {
        public void scan();
    }

    public static interface Listener {
    }

    class Visitor
    implements FileVisitor<Path> {
        Map<String, TimeNSize> scanInfoMap;
        IncludeExcludeSet<PathMatcher, Path> rootIncludesExcludes;
        Path root;

        public Visitor(Path root, IncludeExcludeSet<PathMatcher, Path> rootIncludesExcludes, Map<String, TimeNSize> scanInfoMap) {
            this.root = root;
            this.rootIncludesExcludes = rootIncludesExcludes;
            this.scanInfoMap = scanInfoMap;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!Files.exists(dir, new LinkOption[0])) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            File f = dir.toFile();
            if (Scanner.this._reportDirs && !this.scanInfoMap.containsKey(f.getCanonicalPath())) {
                boolean accepted = false;
                if (this.rootIncludesExcludes != null && !this.rootIncludesExcludes.isEmpty()) {
                    boolean result = this.rootIncludesExcludes.test(dir);
                    if (result) {
                        accepted = true;
                    }
                } else if (Scanner.this._filter == null || Scanner.this._filter.accept(f.getParentFile(), f.getName())) {
                    accepted = true;
                }
                if (accepted) {
                    this.scanInfoMap.put(f.getCanonicalPath(), new TimeNSize(f.lastModified(), f.isDirectory() ? 0L : f.length()));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("scan accepted dir {} mod={}", f, f.lastModified());
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.exists(file, new LinkOption[0])) {
                return FileVisitResult.CONTINUE;
            }
            File f = file.toFile();
            boolean accepted = false;
            if (f.isFile() || f.isDirectory() && Scanner.this._reportDirs && !this.scanInfoMap.containsKey(f.getCanonicalPath())) {
                if (this.rootIncludesExcludes != null && !this.rootIncludesExcludes.isEmpty()) {
                    boolean result = this.rootIncludesExcludes.test(file);
                    if (result) {
                        accepted = true;
                    }
                } else if (Scanner.this._filter == null || Scanner.this._filter.accept(f.getParentFile(), f.getName())) {
                    accepted = true;
                }
            }
            if (accepted) {
                this.scanInfoMap.put(f.getCanonicalPath(), new TimeNSize(f.lastModified(), f.isDirectory() ? 0L : f.length()));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("scan accepted {} mod={}", f, f.lastModified());
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            LOG.warn(exc);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }

    static class TimeNSize {
        final long _lastModified;
        final long _size;

        public TimeNSize(long lastModified, long size) {
            this._lastModified = lastModified;
            this._size = size;
        }

        public int hashCode() {
            return (int)this._lastModified ^ (int)this._size;
        }

        public boolean equals(Object o) {
            if (o instanceof TimeNSize) {
                TimeNSize tns = (TimeNSize)o;
                return tns._lastModified == this._lastModified && tns._size == this._size;
            }
            return false;
        }

        public String toString() {
            return "[lm=" + this._lastModified + ",s=" + this._size + "]";
        }
    }

    static class PathMatcherSet
    extends HashSet<PathMatcher>
    implements Predicate<Path> {
        PathMatcherSet() {
        }

        @Override
        public boolean test(Path p) {
            for (PathMatcher pm : this) {
                if (!pm.matches(p)) continue;
                return true;
            }
            return false;
        }
    }

    public static enum Notification {
        ADDED,
        CHANGED,
        REMOVED;

    }
}

