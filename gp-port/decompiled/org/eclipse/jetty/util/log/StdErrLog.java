/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.log;

import java.io.PrintStream;
import java.security.AccessControlException;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject(value="Jetty StdErr Logging Implementation")
public class StdErrLog
extends AbstractLogger {
    private static final String EOL;
    private static final Object[] EMPTY_ARGS;
    private static int __tagpad;
    private static DateCache _dateCache;
    private static final boolean __source;
    private static final boolean __long;
    private static final boolean __escape;
    private int _level;
    private int _configuredLevel;
    private PrintStream _altStream;
    private boolean _source;
    private boolean _printLongNames = __long;
    private final String _name;
    protected final String _abbrevname;
    private boolean _hideStacks = false;

    public static void setTagPad(int pad) {
        __tagpad = pad;
    }

    public static int getLoggingLevel(Properties props, String name) {
        int level = StdErrLog.lookupLoggingLevel(props, name);
        if (level == -1 && (level = StdErrLog.lookupLoggingLevel(props, "log")) == -1) {
            level = 2;
        }
        return level;
    }

    public static StdErrLog getLogger(Class<?> clazz) {
        Logger log = Log.getLogger(clazz);
        if (log instanceof StdErrLog) {
            return (StdErrLog)log;
        }
        throw new RuntimeException("Logger for " + clazz + " is not of type StdErrLog");
    }

    public StdErrLog() {
        this(null);
    }

    public StdErrLog(String name) {
        this(name, null);
    }

    public StdErrLog(String name, Properties props) {
        boolean sameObject;
        boolean bl = sameObject = props != Log.__props;
        if (props != null && sameObject) {
            Log.__props.putAll((Map<?, ?>)props);
        }
        this._name = name == null ? "" : name;
        this._abbrevname = StdErrLog.condensePackageString(this._name);
        this._configuredLevel = this._level = StdErrLog.getLoggingLevel(Log.__props, this._name);
        try {
            String source = StdErrLog.getLoggingProperty(Log.__props, this._name, "SOURCE");
            this._source = source == null ? __source : Boolean.parseBoolean(source);
        }
        catch (AccessControlException ace) {
            this._source = __source;
        }
        try {
            String stacks = StdErrLog.getLoggingProperty(Log.__props, this._name, "STACKS");
            this._hideStacks = stacks != null && !Boolean.parseBoolean(stacks);
        }
        catch (AccessControlException accessControlException) {
            // empty catch block
        }
    }

    @Override
    public String getName() {
        return this._name;
    }

    public void setPrintLongNames(boolean printLongNames) {
        this._printLongNames = printLongNames;
    }

    public boolean isPrintLongNames() {
        return this._printLongNames;
    }

    public boolean isHideStacks() {
        return this._hideStacks;
    }

    public void setHideStacks(boolean hideStacks) {
        this._hideStacks = hideStacks;
    }

    public boolean isSource() {
        return this._source;
    }

    public void setSource(boolean source) {
        this._source = source;
    }

    @Override
    public void warn(String msg, Object ... args) {
        if (this._level <= 3) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":WARN:", msg, args);
            this.println(builder);
        }
    }

    @Override
    public void warn(Throwable thrown) {
        this.warn("", thrown);
    }

    @Override
    public void warn(String msg, Throwable thrown) {
        if (this._level <= 3) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":WARN:", msg, thrown);
            this.println(builder);
        }
    }

    @Override
    public void info(String msg, Object ... args) {
        if (this._level <= 2) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":INFO:", msg, args);
            this.println(builder);
        }
    }

    @Override
    public void info(Throwable thrown) {
        this.info("", thrown);
    }

    @Override
    public void info(String msg, Throwable thrown) {
        if (this._level <= 2) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":INFO:", msg, thrown);
            this.println(builder);
        }
    }

    @Override
    @ManagedAttribute(value="is debug enabled for root logger Log.LOG")
    public boolean isDebugEnabled() {
        return this._level <= 1;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        int level = enabled ? 1 : this.getConfiguredLevel();
        this.setLevel(level);
        String name = this.getName();
        for (Logger log : Log.getLoggers().values()) {
            if (!log.getName().startsWith(name) || !(log instanceof StdErrLog)) continue;
            StdErrLog logger = (StdErrLog)log;
            level = enabled ? 1 : logger.getConfiguredLevel();
            logger.setLevel(level);
        }
    }

    private int getConfiguredLevel() {
        return this._configuredLevel;
    }

    public int getLevel() {
        return this._level;
    }

    public void setLevel(int level) {
        this._level = level;
    }

    public void setStdErrStream(PrintStream stream) {
        this._altStream = stream;
    }

    @Override
    public void debug(String msg, Object ... args) {
        if (this.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":DBUG:", msg, args);
            this.println(builder);
        }
    }

    @Override
    public void debug(String msg, long arg) {
        if (this.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":DBUG:", msg, arg);
            this.println(builder);
        }
    }

    @Override
    public void debug(Throwable thrown) {
        this.debug("", thrown);
    }

    @Override
    public void debug(String msg, Throwable thrown) {
        if (this.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":DBUG:", msg, thrown);
            this.println(builder);
        }
    }

    private void println(StringBuilder builder) {
        if (this._altStream != null) {
            this._altStream.println(builder);
        } else {
            System.err.println(builder);
        }
    }

    private void format(StringBuilder builder, String level, String msg, Object ... inArgs) {
        long now = System.currentTimeMillis();
        int ms = (int)(now % 1000L);
        String d = _dateCache.formatNow(now);
        this.tag(builder, d, ms, level);
        Object[] msgArgs = EMPTY_ARGS;
        int msgArgsLen = 0;
        Throwable cause = null;
        if (inArgs != null) {
            msgArgs = inArgs;
            msgArgsLen = inArgs.length;
            if (msgArgsLen > 0 && inArgs[msgArgsLen - 1] instanceof Throwable) {
                cause = (Throwable)inArgs[msgArgsLen - 1];
                --msgArgsLen;
            }
        }
        if (msg == null) {
            msg = "";
            for (int i = 0; i < msgArgsLen; ++i) {
                msg = msg + "{} ";
            }
        }
        String braces = "{}";
        int start = 0;
        for (int i = 0; i < msgArgsLen; ++i) {
            Object arg = msgArgs[i];
            int bracesIndex = msg.indexOf(braces, start);
            if (bracesIndex < 0) {
                this.escape(builder, msg.substring(start));
                builder.append(" ");
                if (arg != null) {
                    builder.append(arg);
                }
                start = msg.length();
                continue;
            }
            this.escape(builder, msg.substring(start, bracesIndex));
            if (arg != null) {
                builder.append(arg);
            }
            start = bracesIndex + braces.length();
        }
        this.escape(builder, msg.substring(start));
        if (cause != null) {
            if (this.isHideStacks()) {
                builder.append(": ").append(cause);
            } else {
                this.formatCause(builder, cause, "");
            }
        }
    }

    private void formatCause(StringBuilder builder, Throwable cause, String indent) {
        builder.append(EOL).append(indent);
        this.escape(builder, cause.toString());
        StackTraceElement[] elements = cause.getStackTrace();
        for (int i = 0; elements != null && i < elements.length; ++i) {
            builder.append(EOL).append(indent).append("\tat ");
            this.escape(builder, elements[i].toString());
        }
        for (Throwable suppressed : cause.getSuppressed()) {
            builder.append(EOL).append(indent).append("Suppressed: ");
            this.formatCause(builder, suppressed, "\t|" + indent);
        }
        Throwable by = cause.getCause();
        if (by != null && by != cause) {
            builder.append(EOL).append(indent).append("Caused by: ");
            this.formatCause(builder, by, indent);
        }
    }

    private void escape(StringBuilder builder, String str) {
        if (__escape) {
            for (int i = 0; i < str.length(); ++i) {
                char c = str.charAt(i);
                if (Character.isISOControl(c)) {
                    if (c == '\n') {
                        builder.append('|');
                        continue;
                    }
                    if (c == '\r') {
                        builder.append('<');
                        continue;
                    }
                    builder.append('?');
                    continue;
                }
                builder.append(c);
            }
        } else {
            builder.append(str);
        }
    }

    private void tag(StringBuilder builder, String d, int ms, String tag) {
        int p;
        builder.setLength(0);
        builder.append(d);
        if (ms > 99) {
            builder.append('.');
        } else if (ms > 9) {
            builder.append(".0");
        } else {
            builder.append(".00");
        }
        builder.append(ms).append(tag);
        String name = this._printLongNames ? this._name : this._abbrevname;
        String tname = Thread.currentThread().getName();
        int n = p = __tagpad > 0 ? name.length() + tname.length() - __tagpad : 0;
        if (p < 0) {
            builder.append(name).append(':').append("                                                  ", 0, -p).append(tname);
        } else if (p == 0) {
            builder.append(name).append(':').append(tname);
        }
        builder.append(':');
        if (this._source) {
            StackTraceElement[] frames;
            Throwable source = new Throwable();
            for (StackTraceElement frame : frames = source.getStackTrace()) {
                String clazz = frame.getClassName();
                if (clazz.equals(StdErrLog.class.getName()) || clazz.equals(Log.class.getName())) continue;
                if (!this._printLongNames && clazz.startsWith("org.eclipse.jetty.")) {
                    builder.append(StdErrLog.condensePackageString(clazz));
                } else {
                    builder.append(clazz);
                }
                builder.append('#').append(frame.getMethodName());
                if (frame.getFileName() != null) {
                    builder.append('(').append(frame.getFileName()).append(':').append(frame.getLineNumber()).append(')');
                }
                builder.append(':');
                break;
            }
        }
        builder.append(' ');
    }

    @Override
    protected Logger newLogger(String fullname) {
        StdErrLog logger = new StdErrLog(fullname);
        logger.setPrintLongNames(this._printLongNames);
        logger._altStream = this._altStream;
        if (this._level != this._configuredLevel) {
            logger._level = this._level;
        }
        return logger;
    }

    public String toString() {
        StringBuilder s2 = new StringBuilder();
        s2.append("StdErrLog:");
        s2.append(this._name);
        s2.append(":LEVEL=");
        switch (this._level) {
            case 0: {
                s2.append("ALL");
                break;
            }
            case 1: {
                s2.append("DEBUG");
                break;
            }
            case 2: {
                s2.append("INFO");
                break;
            }
            case 3: {
                s2.append("WARN");
                break;
            }
            default: {
                s2.append("?");
            }
        }
        return s2.toString();
    }

    @Override
    public void ignore(Throwable ignored) {
        if (this._level <= 0) {
            StringBuilder builder = new StringBuilder(64);
            this.format(builder, ":IGNORED:", "", ignored);
            this.println(builder);
        }
    }

    static {
        String[] deprecatedProperties;
        EOL = System.lineSeparator();
        EMPTY_ARGS = new Object[0];
        __tagpad = Integer.parseInt(Log.__props.getProperty("org.eclipse.jetty.util.log.StdErrLog.TAG_PAD", "0"));
        __source = Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.SOURCE", Log.__props.getProperty("org.eclipse.jetty.util.log.stderr.SOURCE", "false")));
        __long = Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.stderr.LONG", "false"));
        __escape = Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.stderr.ESCAPE", "true"));
        for (String deprecatedProp : deprecatedProperties = new String[]{"DEBUG", "org.eclipse.jetty.util.log.DEBUG", "org.eclipse.jetty.util.log.stderr.DEBUG"}) {
            if (System.getProperty(deprecatedProp) == null) continue;
            System.err.printf("System Property [%s] has been deprecated! (Use org.eclipse.jetty.LEVEL=DEBUG instead)%n", deprecatedProp);
        }
        try {
            _dateCache = new DateCache("yyyy-MM-dd HH:mm:ss");
        }
        catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }
}

