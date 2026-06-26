/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class RolloverFileOutputStream
extends OutputStream {
    private static Timer __rollover;
    static final String YYYY_MM_DD = "yyyy_mm_dd";
    static final String ROLLOVER_FILE_DATE_FORMAT = "yyyy_MM_dd";
    static final String ROLLOVER_FILE_BACKUP_FORMAT = "HHmmssSSS";
    static final int ROLLOVER_FILE_RETAIN_DAYS = 31;
    private OutputStream _out;
    private RollTask _rollTask;
    private SimpleDateFormat _fileBackupFormat;
    private SimpleDateFormat _fileDateFormat;
    private String _filename;
    private File _file;
    private boolean _append;
    private int _retainDays;

    public RolloverFileOutputStream(String filename) throws IOException {
        this(filename, true, 31);
    }

    public RolloverFileOutputStream(String filename, boolean append) throws IOException {
        this(filename, append, 31);
    }

    public RolloverFileOutputStream(String filename, boolean append, int retainDays) throws IOException {
        this(filename, append, retainDays, TimeZone.getDefault());
    }

    public RolloverFileOutputStream(String filename, boolean append, int retainDays, TimeZone zone) throws IOException {
        this(filename, append, retainDays, zone, null, null, ZonedDateTime.now(zone.toZoneId()));
    }

    public RolloverFileOutputStream(String filename, boolean append, int retainDays, TimeZone zone, String dateFormat, String backupFormat) throws IOException {
        this(filename, append, retainDays, zone, dateFormat, backupFormat, ZonedDateTime.now(zone.toZoneId()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    RolloverFileOutputStream(String filename, boolean append, int retainDays, TimeZone zone, String dateFormat, String backupFormat, ZonedDateTime now) throws IOException {
        if (dateFormat == null) {
            dateFormat = ROLLOVER_FILE_DATE_FORMAT;
        }
        this._fileDateFormat = new SimpleDateFormat(dateFormat);
        if (backupFormat == null) {
            backupFormat = ROLLOVER_FILE_BACKUP_FORMAT;
        }
        this._fileBackupFormat = new SimpleDateFormat(backupFormat);
        this._fileBackupFormat.setTimeZone(zone);
        this._fileDateFormat.setTimeZone(zone);
        if (filename != null && (filename = filename.trim()).length() == 0) {
            filename = null;
        }
        if (filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }
        this._filename = filename;
        this._append = append;
        this._retainDays = retainDays;
        this.setFile(now);
        Class<RolloverFileOutputStream> clazz = RolloverFileOutputStream.class;
        synchronized (RolloverFileOutputStream.class) {
            if (__rollover == null) {
                __rollover = new Timer(RolloverFileOutputStream.class.getName(), true);
            }
            // ** MonitorExit[var8_8] (shouldn't be in output)
            this.scheduleNextRollover(now);
            return;
        }
    }

    public static ZonedDateTime toMidnight(ZonedDateTime now) {
        return now.toLocalDate().atStartOfDay(now.getZone()).plus(1L, ChronoUnit.DAYS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void scheduleNextRollover(ZonedDateTime now) {
        this._rollTask = new RollTask();
        ZonedDateTime midnight = RolloverFileOutputStream.toMidnight(now);
        long delay = midnight.toInstant().toEpochMilli() - now.toInstant().toEpochMilli();
        Class<RolloverFileOutputStream> clazz = RolloverFileOutputStream.class;
        synchronized (RolloverFileOutputStream.class) {
            __rollover.schedule((TimerTask)this._rollTask, delay);
            // ** MonitorExit[var5_4] (shouldn't be in output)
            return;
        }
    }

    public String getFilename() {
        return this._filename;
    }

    public String getDatedFilename() {
        if (this._file == null) {
            return null;
        }
        return this._file.toString();
    }

    public int getRetainDays() {
        return this._retainDays;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setFile(ZonedDateTime now) throws IOException {
        File oldFile = null;
        File newFile = null;
        File backupFile = null;
        RolloverFileOutputStream rolloverFileOutputStream = this;
        synchronized (rolloverFileOutputStream) {
            File file = new File(this._filename);
            this._filename = file.getCanonicalPath();
            file = new File(this._filename);
            File dir = new File(file.getParent());
            if (!dir.isDirectory() || !dir.canWrite()) {
                throw new IOException("Cannot write log directory " + dir);
            }
            String filename = file.getName();
            int datePattern = filename.toLowerCase(Locale.ENGLISH).indexOf(YYYY_MM_DD);
            if (datePattern >= 0) {
                file = new File(dir, filename.substring(0, datePattern) + this._fileDateFormat.format(new Date(now.toInstant().toEpochMilli())) + filename.substring(datePattern + YYYY_MM_DD.length()));
            }
            if (file.exists() && !file.canWrite()) {
                throw new IOException("Cannot write log file " + file);
            }
            if (this._out == null || datePattern >= 0) {
                oldFile = this._file;
                newFile = this._file = file;
                OutputStream oldOut = this._out;
                if (oldOut != null) {
                    oldOut.close();
                }
                if (!this._append && file.exists()) {
                    backupFile = new File(file.toString() + "." + this._fileBackupFormat.format(new Date(now.toInstant().toEpochMilli())));
                    this.renameFile(file, backupFile);
                }
                this._out = new FileOutputStream(file.toString(), this._append);
            }
        }
        if (newFile != null) {
            this.rollover(oldFile, backupFile, newFile);
        }
    }

    private void renameFile(File src, File dest) throws IOException {
        if (!src.renameTo(dest)) {
            try {
                Files.move(src.toPath(), dest.toPath(), new CopyOption[0]);
            }
            catch (IOException e) {
                Files.copy(src.toPath(), dest.toPath(), new CopyOption[0]);
                Files.deleteIfExists(src.toPath());
            }
        }
    }

    protected void rollover(File oldFile, File backupFile, File newFile) {
    }

    void removeOldFiles(ZonedDateTime now) {
        if (this._retainDays > 0) {
            long expired = now.minus(this._retainDays, ChronoUnit.DAYS).toInstant().toEpochMilli();
            File file = new File(this._filename);
            File dir = new File(file.getParent());
            String fn = file.getName();
            int s2 = fn.toLowerCase(Locale.ENGLISH).indexOf(YYYY_MM_DD);
            if (s2 < 0) {
                return;
            }
            String prefix = fn.substring(0, s2);
            String suffix = fn.substring(s2 + YYYY_MM_DD.length());
            String[] logList = dir.list();
            for (int i = 0; i < logList.length; ++i) {
                File f;
                fn = logList[i];
                if (!fn.startsWith(prefix) || fn.indexOf(suffix, prefix.length()) < 0 || (f = new File(dir, fn)).lastModified() >= expired) continue;
                f.delete();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(int b) throws IOException {
        RolloverFileOutputStream rolloverFileOutputStream = this;
        synchronized (rolloverFileOutputStream) {
            this._out.write(b);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] buf) throws IOException {
        RolloverFileOutputStream rolloverFileOutputStream = this;
        synchronized (rolloverFileOutputStream) {
            this._out.write(buf);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        RolloverFileOutputStream rolloverFileOutputStream = this;
        synchronized (rolloverFileOutputStream) {
            this._out.write(buf, off, len);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void flush() throws IOException {
        RolloverFileOutputStream rolloverFileOutputStream = this;
        synchronized (rolloverFileOutputStream) {
            this._out.flush();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        Object object = this;
        synchronized (object) {
            try {
                this._out.close();
            }
            finally {
                this._out = null;
                this._file = null;
            }
        }
        object = RolloverFileOutputStream.class;
        synchronized (RolloverFileOutputStream.class) {
            if (this._rollTask != null) {
                this._rollTask.cancel();
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    private class RollTask
    extends TimerTask {
        private RollTask() {
        }

        @Override
        public void run() {
            try {
                ZonedDateTime now = ZonedDateTime.now(RolloverFileOutputStream.this._fileDateFormat.getTimeZone().toZoneId());
                RolloverFileOutputStream.this.setFile(now);
                RolloverFileOutputStream.this.removeOldFiles(now);
                RolloverFileOutputStream.this.scheduleNextRollover(now);
            }
            catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }
}

