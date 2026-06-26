/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.jetty.util.IncludeExcludeSet;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class PathWatcher
extends AbstractLifeCycle
implements Runnable {
    private static final boolean IS_WINDOWS;
    static final Logger LOG;
    private static final WatchEvent.Kind<?>[] WATCH_EVENT_KINDS;
    private static final WatchEvent.Kind<?>[] WATCH_DIR_KINDS;
    private WatchService watchService;
    private final List<Config> configs = new ArrayList<Config>();
    private final Map<WatchKey, Config> keys = new ConcurrentHashMap<WatchKey, Config>();
    private final List<EventListener> listeners = new CopyOnWriteArrayList<EventListener>();
    private final Map<Path, PathWatchEvent> pending = new LinkedHashMap<Path, PathWatchEvent>(32, 0.75f, false);
    private final List<PathWatchEvent> events = new ArrayList<PathWatchEvent>();
    private long updateQuietTimeDuration = 1000L;
    private TimeUnit updateQuietTimeUnit = TimeUnit.MILLISECONDS;
    private Thread thread;
    private boolean _notifyExistingOnStart = true;

    protected static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return event;
    }

    public Collection<Config> getConfigs() {
        return this.configs;
    }

    public void watch(Path file) {
        Path abs = file;
        if (!abs.isAbsolute()) {
            abs = file.toAbsolutePath();
        }
        Config config = null;
        Path parent = abs.getParent();
        for (Config c : this.configs) {
            if (!c.getPath().equals(parent)) continue;
            config = c;
            break;
        }
        if (config == null) {
            config = new Config(abs.getParent());
            config.addIncludeGlobRelative("");
            config.addIncludeGlobRelative(file.getFileName().toString());
            this.watch(config);
        } else {
            config.addIncludeGlobRelative(file.getFileName().toString());
        }
    }

    public void watch(Config config) {
        this.configs.add(config);
    }

    public void addListener(EventListener listener) {
        this.listeners.add(listener);
    }

    private void appendConfigId(StringBuilder s2) {
        ArrayList<Path> dirs = new ArrayList<Path>();
        for (Config config : this.keys.values()) {
            dirs.add(config.path);
        }
        Collections.sort(dirs);
        s2.append("[");
        if (dirs.size() > 0) {
            s2.append(dirs.get(0));
            if (dirs.size() > 1) {
                s2.append(" (+").append(dirs.size() - 1).append(")");
            }
        } else {
            s2.append("<null>");
        }
        s2.append("]");
    }

    @Override
    protected void doStart() throws Exception {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.setUpdateQuietTime(this.getUpdateQuietTimeMillis(), TimeUnit.MILLISECONDS);
        for (Config c : this.configs) {
            this.registerTree(c.getPath(), c, this.isNotifyExistingOnStart());
        }
        StringBuilder threadId = new StringBuilder();
        threadId.append("PathWatcher@");
        threadId.append(Integer.toHexString(this.hashCode()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} -> {}", this, threadId);
        }
        this.thread = new Thread((Runnable)this, threadId.toString());
        this.thread.setDaemon(true);
        this.thread.start();
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        if (this.watchService != null) {
            this.watchService.close();
        }
        this.watchService = null;
        this.thread = null;
        this.keys.clear();
        this.pending.clear();
        this.events.clear();
        super.doStop();
    }

    public void reset() {
        if (!this.isStopped()) {
            throw new IllegalStateException("PathWatcher must be stopped before reset.");
        }
        this.configs.clear();
        this.listeners.clear();
    }

    protected boolean isNotifiable() {
        return this.isStarted() || !this.isStarted() && this.isNotifyExistingOnStart();
    }

    public Iterator<EventListener> getListeners() {
        return this.listeners.iterator();
    }

    public long getUpdateQuietTimeMillis() {
        return TimeUnit.MILLISECONDS.convert(this.updateQuietTimeDuration, this.updateQuietTimeUnit);
    }

    private void registerTree(Path dir, Config config, boolean notify) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerTree {} {} {}", dir, config, notify);
        }
        if (!Files.isDirectory(dir, new LinkOption[0])) {
            throw new IllegalArgumentException(dir.toString());
        }
        this.register(dir, config);
        MultiException me = new MultiException();
        try (Stream<Path> stream = Files.list(dir);){
            stream.forEach(p -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("registerTree? {}", p);
                }
                try {
                    if (notify && config.test((Path)p)) {
                        this.pending.put((Path)p, new PathWatchEvent((Path)p, PathWatchEventType.ADDED, config));
                    }
                    switch (config.handleDir((Path)p)) {
                        case ENTER: {
                            this.registerTree((Path)p, config.asSubConfig((Path)p), notify);
                            break;
                        }
                        case WATCH: {
                            this.registerDir((Path)p, config);
                            break;
                        }
                    }
                }
                catch (IOException e) {
                    me.add(e);
                }
            });
        }
        try {
            me.ifExceptionThrow();
        }
        catch (IOException e) {
            throw e;
        }
        catch (Throwable ex) {
            throw new IOException(ex);
        }
    }

    private void registerDir(Path path, Config config) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerDir {} {}", path, config);
        }
        if (!Files.isDirectory(path, new LinkOption[0])) {
            throw new IllegalArgumentException(path.toString());
        }
        this.register(path, config.asSubConfig(path), WATCH_DIR_KINDS);
    }

    protected void register(Path path, Config config) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering watch on {}", path);
        }
        this.register(path, config, WATCH_EVENT_KINDS);
    }

    private void register(Path path, Config config, WatchEvent.Kind<?>[] kinds) throws IOException {
        WatchKey key = path.register(this.watchService, kinds);
        this.keys.put(key, config);
    }

    public boolean removeListener(Listener listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting java.nio file watching with {}", this.watchService);
        }
        long waitTime = this.getUpdateQuietTimeMillis();
        WatchService watch = this.watchService;
        while (this.isRunning() && this.thread == Thread.currentThread()) {
            try {
                WatchKey key;
                long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                for (Map.Entry<WatchKey, Config> e : this.keys.entrySet()) {
                    WatchKey k = e.getKey();
                    Config c = e.getValue();
                    if (c.isPaused(now) || k.reset()) continue;
                    this.keys.remove(k);
                    if (!this.keys.isEmpty()) continue;
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Waiting for poll({})", waitTime);
                }
                WatchKey watchKey = waitTime < 0L ? watch.take() : (key = waitTime > 0L ? watch.poll(waitTime, this.updateQuietTimeUnit) : watch.poll());
                while (key != null) {
                    this.handleKey(key);
                    key = watch.poll();
                }
                waitTime = this.processPending();
                this.notifyEvents();
            }
            catch (ClosedWatchServiceException e) {
                return;
            }
            catch (InterruptedException e) {
                if (this.isRunning()) {
                    LOG.warn(e);
                    continue;
                }
                LOG.ignore(e);
            }
        }
    }

    private void handleKey(WatchKey key) {
        Config config = this.keys.get(key);
        if (config == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("WatchKey not recognized: {}", key);
            }
            return;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent<Path> ev = PathWatcher.cast(event);
            Path name = (Path)ev.context();
            Path path = config.resolve(name);
            if (LOG.isDebugEnabled()) {
                LOG.debug("handleKey? {} {} {}", ev.kind(), config.toShortPath(path), config);
            }
            if (ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY && Files.exists(path, new LinkOption[0]) && Files.isDirectory(path, new LinkOption[0])) continue;
            if (config.test(path)) {
                this.handleWatchEvent(path, new PathWatchEvent(path, ev, config));
            } else if (config.getRecurseDepth() == -1) {
                Path parent = path.getParent();
                Config parentConfig = config.getParent();
                this.handleWatchEvent(parent, new PathWatchEvent(parent, PathWatchEventType.MODIFIED, parentConfig));
                continue;
            }
            if (ev.kind() != StandardWatchEventKinds.ENTRY_CREATE) continue;
            try {
                switch (config.handleDir(path)) {
                    case ENTER: {
                        this.registerTree(path, config.asSubConfig(path), true);
                        break;
                    }
                    case WATCH: {
                        this.registerDir(path, config);
                        break;
                    }
                }
            }
            catch (IOException e) {
                LOG.warn(e);
            }
        }
    }

    public void handleWatchEvent(Path path, PathWatchEvent event) {
        PathWatchEvent existing = this.pending.get(path);
        if (LOG.isDebugEnabled()) {
            LOG.debug("handleWatchEvent {} {} <= {}", path, event, existing);
        }
        switch (event.getType()) {
            case ADDED: {
                if (existing != null && existing.getType() == PathWatchEventType.MODIFIED) {
                    this.events.add(new PathWatchEvent(path, PathWatchEventType.DELETED, existing.getConfig()));
                }
                this.pending.put(path, event);
                break;
            }
            case MODIFIED: {
                if (existing == null) {
                    this.pending.put(path, event);
                    break;
                }
                existing.modified();
                break;
            }
            case DELETED: 
            case UNKNOWN: {
                if (existing != null) {
                    this.pending.remove(path);
                }
                this.events.add(event);
            }
        }
    }

    private long processPending() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("processPending> {}", this.pending.values());
        }
        long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        long wait = Long.MAX_VALUE;
        for (PathWatchEvent event : new ArrayList<PathWatchEvent>(this.pending.values())) {
            Path path = event.getPath();
            if (this.pending.containsKey(path.getParent())) continue;
            if (event.isQuiet(now, this.getUpdateQuietTimeMillis())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("isQuiet {}", event);
                }
                this.pending.remove(path);
                this.events.add(event);
                continue;
            }
            long msToCheck = event.toQuietCheck(now, this.getUpdateQuietTimeMillis());
            if (LOG.isDebugEnabled()) {
                LOG.debug("pending {} {}", event, msToCheck);
            }
            if (msToCheck >= wait) continue;
            wait = msToCheck;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("processPending< {}", this.pending.values());
        }
        return wait == Long.MAX_VALUE ? -1L : wait;
    }

    private void notifyEvents() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("notifyEvents {}", this.events.size());
        }
        if (this.events.isEmpty()) {
            return;
        }
        boolean eventListeners = false;
        for (EventListener listener : this.listeners) {
            if (listener instanceof EventListListener) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("notifyEvents {} {}", listener, this.events);
                    }
                    ((EventListListener)listener).onPathWatchEvents(this.events);
                }
                catch (Throwable t) {
                    LOG.warn(t);
                }
                continue;
            }
            eventListeners = true;
        }
        if (eventListeners) {
            for (PathWatchEvent event : this.events) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("notifyEvent {} {}", event, this.listeners);
                }
                for (EventListener listener : this.listeners) {
                    if (!(listener instanceof Listener)) continue;
                    try {
                        ((Listener)listener).onPathWatchEvent(event);
                    }
                    catch (Throwable t) {
                        LOG.warn(t);
                    }
                }
            }
        }
        this.events.clear();
    }

    public void setNotifyExistingOnStart(boolean notify) {
        this._notifyExistingOnStart = notify;
    }

    public boolean isNotifyExistingOnStart() {
        return this._notifyExistingOnStart;
    }

    public void setUpdateQuietTime(long duration, TimeUnit unit) {
        long desiredMillis = unit.toMillis(duration);
        if (IS_WINDOWS && desiredMillis < 1000L) {
            LOG.warn("Quiet Time is too low for Microsoft Windows: {} < 1000 ms (defaulting to 1000 ms)", desiredMillis);
            this.updateQuietTimeDuration = 1000L;
            this.updateQuietTimeUnit = TimeUnit.MILLISECONDS;
            return;
        }
        this.updateQuietTimeDuration = duration;
        this.updateQuietTimeUnit = unit;
    }

    @Override
    public String toString() {
        StringBuilder s2 = new StringBuilder(this.getClass().getName());
        this.appendConfigId(s2);
        return s2.toString();
    }

    static {
        String os = System.getProperty("os.name");
        if (os == null) {
            IS_WINDOWS = false;
        } else {
            String osl = os.toLowerCase(Locale.ENGLISH);
            IS_WINDOWS = osl.contains("windows");
        }
        LOG = Log.getLogger(PathWatcher.class);
        WATCH_EVENT_KINDS = new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
        WATCH_DIR_KINDS = new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE};
    }

    public static class PathMatcherSet
    extends HashSet<PathMatcher>
    implements Predicate<Path> {
        @Override
        public boolean test(Path path) {
            for (PathMatcher pm : this) {
                if (!pm.matches(path)) continue;
                return true;
            }
            return false;
        }
    }

    private static class ExactPathMatcher
    implements PathMatcher {
        private final Path path;

        ExactPathMatcher(Path path) {
            this.path = path;
        }

        @Override
        public boolean matches(Path path) {
            return this.path.equals(path);
        }
    }

    public static enum PathWatchEventType {
        ADDED,
        DELETED,
        MODIFIED,
        UNKNOWN;

    }

    public class PathWatchEvent {
        private final Path path;
        private final PathWatchEventType type;
        private final Config config;
        long checked;
        long modified;
        long length;

        public PathWatchEvent(Path path, PathWatchEventType type, Config config) {
            this.path = path;
            this.type = type;
            this.config = config;
            this.checked = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            this.check();
        }

        public Config getConfig() {
            return this.config;
        }

        public PathWatchEvent(Path path, WatchEvent<Path> event, Config config) {
            this.path = path;
            this.type = event.kind() == StandardWatchEventKinds.ENTRY_CREATE ? PathWatchEventType.ADDED : (event.kind() == StandardWatchEventKinds.ENTRY_DELETE ? PathWatchEventType.DELETED : (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY ? PathWatchEventType.MODIFIED : PathWatchEventType.UNKNOWN));
            this.config = config;
            this.checked = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            this.check();
        }

        private void check() {
            if (Files.exists(this.path, new LinkOption[0])) {
                try {
                    this.modified = Files.getLastModifiedTime(this.path, new LinkOption[0]).toMillis();
                    this.length = Files.size(this.path);
                }
                catch (IOException e) {
                    this.modified = -1L;
                    this.length = -1L;
                }
            } else {
                this.modified = -1L;
                this.length = -1L;
            }
        }

        public boolean isQuiet(long now, long quietTime) {
            long lastModified = this.modified;
            long lastLength = this.length;
            this.check();
            if (lastModified == this.modified && lastLength == this.length) {
                return now - this.checked >= quietTime;
            }
            this.checked = now;
            return false;
        }

        public long toQuietCheck(long now, long quietTime) {
            long check = quietTime - (now - this.checked);
            if (check <= 0L) {
                return quietTime;
            }
            return check;
        }

        public void modified() {
            long now;
            this.checked = now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            this.check();
            this.config.setPauseUntil(now + PathWatcher.this.getUpdateQuietTimeMillis());
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
            PathWatchEvent other = (PathWatchEvent)obj;
            if (this.path == null ? other.path != null : !this.path.equals(other.path)) {
                return false;
            }
            return this.type == other.type;
        }

        public Path getPath() {
            return this.path;
        }

        public PathWatchEventType getType() {
            return this.type;
        }

        @Deprecated
        public int getCount() {
            return 1;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + (this.path == null ? 0 : this.path.hashCode());
            result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
            return result;
        }

        public String toString() {
            return String.format("PathWatchEvent[%8s|%s]", new Object[]{this.type, this.path});
        }
    }

    public static interface EventListListener
    extends EventListener {
        public void onPathWatchEvents(List<PathWatchEvent> var1);
    }

    public static interface Listener
    extends EventListener {
        public void onPathWatchEvent(PathWatchEvent var1);
    }

    public static enum DirAction {
        IGNORE,
        WATCH,
        ENTER;

    }

    public static class Config
    implements Predicate<Path> {
        public static final int UNLIMITED_DEPTH = -9999;
        private static final String PATTERN_SEP;
        protected final Config parent;
        protected final Path path;
        protected final IncludeExcludeSet<PathMatcher, Path> includeExclude;
        protected int recurseDepth = 0;
        protected boolean excludeHidden = false;
        protected long pauseUntil;

        public Config(Path path) {
            this(path, null);
        }

        public Config(Path path, Config parent) {
            IncludeExcludeSet<PathMatcher, Path> includeExcludeSet;
            this.parent = parent;
            if (parent == null) {
                IncludeExcludeSet<PathMatcher, Path> includeExcludeSet2;
                includeExcludeSet = includeExcludeSet2;
                super(PathMatcherSet.class);
            } else {
                includeExcludeSet = parent.includeExclude;
            }
            this.includeExclude = includeExcludeSet;
            Path dir = path;
            if (!Files.exists(path, new LinkOption[0])) {
                throw new IllegalStateException("Path does not exist: " + path);
            }
            if (!Files.isDirectory(path, new LinkOption[0])) {
                dir = path.getParent();
                this.includeExclude.include(new ExactPathMatcher(path));
                this.setRecurseDepth(0);
            }
            this.path = dir;
        }

        public Config getParent() {
            return this.parent;
        }

        public void setPauseUntil(long time) {
            if (time > this.pauseUntil) {
                this.pauseUntil = time;
            }
        }

        public boolean isPaused(long now) {
            if (this.pauseUntil == 0L) {
                return false;
            }
            if (this.pauseUntil > now) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("PAUSED {}", this);
                }
                return true;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("unpaused {}", this);
            }
            this.pauseUntil = 0L;
            return false;
        }

        public void addExclude(PathMatcher matcher) {
            this.includeExclude.exclude(matcher);
        }

        public void addExclude(String syntaxAndPattern) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding exclude: [{}]", syntaxAndPattern);
            }
            this.addExclude(this.path.getFileSystem().getPathMatcher(syntaxAndPattern));
        }

        public void addExcludeGlobRelative(String pattern) {
            this.addExclude(this.toGlobPattern(this.path, pattern));
        }

        public void addExcludeHidden() {
            if (!this.excludeHidden) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding hidden files and directories to exclusions", new Object[0]);
                }
                this.excludeHidden = true;
            }
        }

        public void addExcludes(List<String> syntaxAndPatterns) {
            for (String syntaxAndPattern : syntaxAndPatterns) {
                this.addExclude(syntaxAndPattern);
            }
        }

        public void addInclude(PathMatcher matcher) {
            this.includeExclude.include(matcher);
        }

        public void addInclude(String syntaxAndPattern) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding include: [{}]", syntaxAndPattern);
            }
            this.addInclude(this.path.getFileSystem().getPathMatcher(syntaxAndPattern));
        }

        public void addIncludeGlobRelative(String pattern) {
            this.addInclude(this.toGlobPattern(this.path, pattern));
        }

        public void addIncludes(List<String> syntaxAndPatterns) {
            for (String syntaxAndPattern : syntaxAndPatterns) {
                this.addInclude(syntaxAndPattern);
            }
        }

        public Config asSubConfig(Path dir) {
            Config subconfig = new Config(dir, this);
            if (dir == this.path) {
                throw new IllegalStateException("sub " + dir.toString() + " of " + this);
            }
            subconfig.recurseDepth = this.recurseDepth == -9999 ? -9999 : this.recurseDepth - (dir.getNameCount() - this.path.getNameCount());
            if (LOG.isDebugEnabled()) {
                LOG.debug("subconfig {} of {}", subconfig, this.path);
            }
            return subconfig;
        }

        public int getRecurseDepth() {
            return this.recurseDepth;
        }

        public boolean isRecurseDepthUnlimited() {
            return this.recurseDepth == -9999;
        }

        public Path getPath() {
            return this.path;
        }

        public Path resolve(Path path) {
            if (Files.isDirectory(this.path, new LinkOption[0])) {
                return this.path.resolve(path);
            }
            if (Files.exists(this.path, new LinkOption[0])) {
                return this.path;
            }
            return path;
        }

        @Override
        public boolean test(Path path) {
            int depth;
            if (this.excludeHidden && this.isHidden(path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("test({}) -> [Hidden]", this.toShortPath(path));
                }
                return false;
            }
            if (!path.startsWith(this.path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("test({}) -> [!child {}]", this.toShortPath(path), this.path);
                }
                return false;
            }
            if (this.recurseDepth != -9999 && (depth = path.getNameCount() - this.path.getNameCount() - 1) > this.recurseDepth) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("test({}) -> [depth {}>{}]", this.toShortPath(path), depth, this.recurseDepth);
                }
                return false;
            }
            boolean matched = this.includeExclude.test(path);
            if (LOG.isDebugEnabled()) {
                LOG.debug("test({}) -> {}", this.toShortPath(path), matched);
            }
            return matched;
        }

        public void setRecurseDepth(int depth) {
            this.recurseDepth = depth;
        }

        private String toGlobPattern(Path path, String subPattern) {
            StringBuilder s2 = new StringBuilder();
            s2.append("glob:");
            boolean needDelim = false;
            Path root = path.getRoot();
            if (root != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Path: {} -> Root: {}", path, root);
                }
                for (Object c : (Object)root.toString().toCharArray()) {
                    if (c == 92) {
                        s2.append(PATTERN_SEP);
                        continue;
                    }
                    s2.append((char)c);
                }
            } else {
                needDelim = true;
            }
            for (Path segment : path) {
                if (needDelim) {
                    s2.append(PATTERN_SEP);
                }
                s2.append(segment);
                needDelim = true;
            }
            if (subPattern != null && subPattern.length() > 0) {
                if (needDelim) {
                    s2.append(PATTERN_SEP);
                }
                for (Object c : (Object)subPattern.toCharArray()) {
                    if (c == 47) {
                        s2.append(PATTERN_SEP);
                        continue;
                    }
                    s2.append((char)c);
                }
            }
            return s2.toString();
        }

        DirAction handleDir(Path path) {
            try {
                if (!Files.isDirectory(path, new LinkOption[0])) {
                    return DirAction.IGNORE;
                }
                if (this.excludeHidden && this.isHidden(path)) {
                    return DirAction.IGNORE;
                }
                if (this.getRecurseDepth() == 0) {
                    return DirAction.WATCH;
                }
                return DirAction.ENTER;
            }
            catch (Exception e) {
                LOG.ignore(e);
                return DirAction.IGNORE;
            }
        }

        public boolean isHidden(Path path) {
            try {
                if (!path.startsWith(this.path)) {
                    return true;
                }
                for (int i = this.path.getNameCount(); i < path.getNameCount(); ++i) {
                    if (!path.getName(i).toString().startsWith(".")) continue;
                    return true;
                }
                return Files.exists(path, new LinkOption[0]) && Files.isHidden(path);
            }
            catch (IOException e) {
                LOG.ignore(e);
                return false;
            }
        }

        public String toShortPath(Path path) {
            if (!path.startsWith(this.path)) {
                return path.toString();
            }
            return this.path.relativize(path).toString();
        }

        public String toString() {
            StringBuilder s2 = new StringBuilder();
            s2.append(this.path).append(" [depth=");
            if (this.recurseDepth == -9999) {
                s2.append("UNLIMITED");
            } else {
                s2.append(this.recurseDepth);
            }
            s2.append(']');
            return s2.toString();
        }

        static {
            String sep = File.separator;
            if (File.separatorChar == '\\') {
                sep = "\\\\";
            }
            PATTERN_SEP = sep;
        }
    }
}

