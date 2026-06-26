/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.QuotedCSV;
import org.eclipse.jetty.http.pathmap.PathMappings;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject(value="Custom format request log")
public class CustomRequestLog
extends ContainerLifeCycle
implements RequestLog {
    protected static final Logger LOG = Log.getLogger(CustomRequestLog.class);
    public static final String DEFAULT_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss ZZZ";
    public static final String NCSA_FORMAT = "%{client}a - %u %t \"%r\" %s %O";
    public static final String EXTENDED_NCSA_FORMAT = "%{client}a - %u %t \"%r\" %s %O \"%{Referer}i\" \"%{User-Agent}i\"";
    private static final ThreadLocal<StringBuilder> _buffers = ThreadLocal.withInitial(() -> new StringBuilder(256));
    private final RequestLog.Writer _requestLogWriter;
    private final MethodHandle _logHandle;
    private final String _formatString;
    private transient PathMappings<String> _ignorePathMap;
    private String[] _ignorePaths;

    public CustomRequestLog(RequestLog.Writer writer, String formatString) {
        this._formatString = formatString;
        this._requestLogWriter = writer;
        this.addBean(this._requestLogWriter);
        try {
            this._logHandle = this.getLogHandle(formatString);
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public CustomRequestLog(String file) {
        this(file, EXTENDED_NCSA_FORMAT);
    }

    public CustomRequestLog(String file, String format) {
        this(new RequestLogWriter(file), format);
    }

    @ManagedAttribute(value="The RequestLogWriter")
    public RequestLog.Writer getWriter() {
        return this._requestLogWriter;
    }

    @Override
    public void log(Request request, Response response) {
        try {
            if (this._ignorePathMap != null && this._ignorePathMap.getMatch(request.getRequestURI()) != null) {
                return;
            }
            StringBuilder sb = _buffers.get();
            sb.setLength(0);
            this._logHandle.invoke(sb, request, response);
            String log = sb.toString();
            this._requestLogWriter.write(log);
        }
        catch (Throwable e) {
            LOG.warn(e);
        }
    }

    protected static String getAuthentication(Request request, boolean checkDeferred) {
        Authentication authentication = request.getAuthentication();
        if (checkDeferred && authentication instanceof Authentication.Deferred) {
            authentication = ((Authentication.Deferred)authentication).authenticate(request);
        }
        String name = null;
        if (authentication instanceof Authentication.User) {
            name = ((Authentication.User)authentication).getUserIdentity().getUserPrincipal().getName();
        }
        return name;
    }

    public void setIgnorePaths(String[] ignorePaths) {
        this._ignorePaths = ignorePaths;
    }

    public String[] getIgnorePaths() {
        return this._ignorePaths;
    }

    @ManagedAttribute(value="format string")
    public String getFormatString() {
        return this._formatString;
    }

    @Override
    protected synchronized void doStart() throws Exception {
        if (this._ignorePaths != null && this._ignorePaths.length > 0) {
            this._ignorePathMap = new PathMappings();
            for (String ignorePath : this._ignorePaths) {
                this._ignorePathMap.put(ignorePath, ignorePath);
            }
        } else {
            this._ignorePathMap = null;
        }
        super.doStart();
    }

    private static void append(StringBuilder buf, String s2) {
        if (s2 == null || s2.length() == 0) {
            buf.append('-');
        } else {
            buf.append(s2);
        }
    }

    private static void append(String s2, StringBuilder buf) {
        CustomRequestLog.append(buf, s2);
    }

    private MethodHandle getLogHandle(String formatString) throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle append = lookup.findStatic(CustomRequestLog.class, "append", MethodType.methodType(Void.TYPE, String.class, StringBuilder.class));
        MethodHandle logHandle = lookup.findStatic(CustomRequestLog.class, "logNothing", MethodType.methodType(Void.TYPE, StringBuilder.class, Request.class, Response.class));
        List<Token> tokens = CustomRequestLog.getTokens(formatString);
        Collections.reverse(tokens);
        for (Token t : tokens) {
            if (t.isLiteralString()) {
                logHandle = this.updateLogHandle(logHandle, append, t.literal);
                continue;
            }
            if (t.isPercentCode()) {
                logHandle = this.updateLogHandle(logHandle, append, lookup, t.code, t.arg, t.modifiers, t.negated);
                continue;
            }
            throw new IllegalStateException("bad token " + t);
        }
        return logHandle;
    }

    private static List<Token> getTokens(String formatString) {
        Pattern PATTERN = Pattern.compile("^(?:%(?<MOD>!?[0-9,]+)?(?:\\{(?<ARG>[^}]+)})?(?<CODE>(?:(?:ti)|(?:to)|[a-zA-Z%]))|(?<LITERAL>[^%]+))(?<REMAINING>.*)", 40);
        ArrayList<Token> tokens = new ArrayList<Token>();
        String remaining = formatString;
        while (remaining.length() > 0) {
            Matcher m3 = PATTERN.matcher(remaining);
            if (m3.matches()) {
                if (m3.group("CODE") != null) {
                    String code = m3.group("CODE");
                    String arg = m3.group("ARG");
                    String modifierString = m3.group("MOD");
                    List modifiers = null;
                    boolean negated = false;
                    if (modifierString != null) {
                        if (modifierString.startsWith("!")) {
                            modifierString = modifierString.substring(1);
                            negated = true;
                        }
                        modifiers = new QuotedCSV(modifierString).getValues().stream().map(Integer::parseInt).collect(Collectors.toList());
                    }
                    tokens.add(new Token(code, arg, modifiers, negated));
                } else if (m3.group("LITERAL") != null) {
                    String literal = m3.group("LITERAL");
                    tokens.add(new Token(literal));
                } else {
                    throw new IllegalStateException("formatString parsing error");
                }
                remaining = m3.group("REMAINING");
                continue;
            }
            throw new IllegalArgumentException("Invalid format string");
        }
        return tokens;
    }

    private static boolean modify(List<Integer> modifiers, Boolean negated, StringBuilder b, Request request, Response response) {
        if (negated.booleanValue()) {
            return !modifiers.contains(response.getStatus());
        }
        return modifiers.contains(response.getStatus());
    }

    private MethodHandle updateLogHandle(MethodHandle logHandle, MethodHandle append, String literal) {
        return MethodHandles.foldArguments(logHandle, MethodHandles.dropArguments(MethodHandles.dropArguments(append.bindTo(literal), 1, Request.class), 2, Response.class));
    }

    private MethodHandle updateLogHandle(MethodHandle logHandle, MethodHandle append, MethodHandles.Lookup lookup, String code, String arg, List<Integer> modifiers, boolean negated) throws NoSuchMethodException, IllegalAccessException {
        MethodHandle specificHandle;
        MethodType logType = MethodType.methodType(Void.TYPE, StringBuilder.class, Request.class, Response.class);
        MethodType logTypeArg = MethodType.methodType(Void.TYPE, String.class, StringBuilder.class, Request.class, Response.class);
        switch (code) {
            case "%": {
                specificHandle = MethodHandles.dropArguments(MethodHandles.dropArguments(append.bindTo("%"), 1, Request.class), 2, Response.class);
                break;
            }
            case "a": {
                String method;
                if (StringUtil.isEmpty(arg)) {
                    arg = "server";
                }
                switch (arg) {
                    case "server": {
                        method = "logServerHost";
                        break;
                    }
                    case "client": {
                        method = "logClientHost";
                        break;
                    }
                    case "local": {
                        method = "logLocalHost";
                        break;
                    }
                    case "remote": {
                        method = "logRemoteHost";
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Invalid arg for %a");
                    }
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "p": {
                String method;
                if (StringUtil.isEmpty(arg)) {
                    arg = "server";
                }
                switch (arg) {
                    case "server": {
                        method = "logServerPort";
                        break;
                    }
                    case "client": {
                        method = "logClientPort";
                        break;
                    }
                    case "local": {
                        method = "logLocalPort";
                        break;
                    }
                    case "remote": {
                        method = "logRemotePort";
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Invalid arg for %p");
                    }
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "I": {
                String method;
                if (StringUtil.isEmpty(arg)) {
                    method = "logBytesReceived";
                } else if (arg.equalsIgnoreCase("clf")) {
                    method = "logBytesReceivedCLF";
                } else {
                    throw new IllegalArgumentException("Invalid argument for %I");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "O": {
                String method;
                if (StringUtil.isEmpty(arg)) {
                    method = "logBytesSent";
                } else if (arg.equalsIgnoreCase("clf")) {
                    method = "logBytesSentCLF";
                } else {
                    throw new IllegalArgumentException("Invalid argument for %O");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "S": {
                String method;
                if (StringUtil.isEmpty(arg)) {
                    method = "logBytesTransferred";
                } else if (arg.equalsIgnoreCase("clf")) {
                    method = "logBytesTransferredCLF";
                } else {
                    throw new IllegalArgumentException("Invalid argument for %S");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "C": {
                if (StringUtil.isEmpty(arg)) {
                    specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestCookies", logType);
                    break;
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestCookie", logTypeArg);
                specificHandle = specificHandle.bindTo(arg);
                break;
            }
            case "D": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logLatencyMicroseconds", logType);
                break;
            }
            case "e": {
                if (StringUtil.isEmpty(arg)) {
                    throw new IllegalArgumentException("No arg for %e");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logEnvironmentVar", logTypeArg);
                specificHandle = specificHandle.bindTo(arg);
                break;
            }
            case "f": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logFilename", logType);
                break;
            }
            case "H": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestProtocol", logType);
                break;
            }
            case "i": {
                if (StringUtil.isEmpty(arg)) {
                    throw new IllegalArgumentException("No arg for %i");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestHeader", logTypeArg);
                specificHandle = specificHandle.bindTo(arg);
                break;
            }
            case "k": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logKeepAliveRequests", logType);
                break;
            }
            case "m": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestMethod", logType);
                break;
            }
            case "o": {
                if (StringUtil.isEmpty(arg)) {
                    throw new IllegalArgumentException("No arg for %o");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logResponseHeader", logTypeArg);
                specificHandle = specificHandle.bindTo(arg);
                break;
            }
            case "q": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logQueryString", logType);
                break;
            }
            case "r": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestFirstLine", logType);
                break;
            }
            case "R": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestHandler", logType);
                break;
            }
            case "s": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logResponseStatus", logType);
                break;
            }
            case "t": {
                String format = DEFAULT_DATE_FORMAT;
                TimeZone timeZone = TimeZone.getTimeZone("GMT");
                Locale locale = Locale.getDefault();
                if (arg != null && !arg.isEmpty()) {
                    String[] args = arg.split("\\|");
                    switch (args.length) {
                        case 1: {
                            format = args[0];
                            break;
                        }
                        case 2: {
                            format = args[0];
                            timeZone = TimeZone.getTimeZone(args[1]);
                            break;
                        }
                        case 3: {
                            format = args[0];
                            timeZone = TimeZone.getTimeZone(args[1]);
                            locale = Locale.forLanguageTag(args[2]);
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("Too many \"|\" characters in %t");
                        }
                    }
                }
                DateCache logDateCache = new DateCache(format, locale, timeZone);
                MethodType logTypeDateCache = MethodType.methodType(Void.TYPE, DateCache.class, StringBuilder.class, Request.class, Response.class);
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestTime", logTypeDateCache);
                specificHandle = specificHandle.bindTo(logDateCache);
                break;
            }
            case "T": {
                String method;
                if (arg == null) {
                    arg = "s";
                }
                switch (arg) {
                    case "s": {
                        method = "logLatencySeconds";
                        break;
                    }
                    case "us": {
                        method = "logLatencyMicroseconds";
                        break;
                    }
                    case "ms": {
                        method = "logLatencyMilliseconds";
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Invalid arg for %T");
                    }
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "u": {
                String method;
                if (StringUtil.isEmpty(arg)) {
                    method = "logRequestAuthentication";
                } else if ("d".equals(arg)) {
                    method = "logRequestAuthenticationWithDeferred";
                } else {
                    throw new IllegalArgumentException("Invalid arg for %u: " + arg);
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, method, logType);
                break;
            }
            case "U": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logUrlRequestPath", logType);
                break;
            }
            case "X": {
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logConnectionStatus", logType);
                break;
            }
            case "ti": {
                if (StringUtil.isEmpty(arg)) {
                    throw new IllegalArgumentException("No arg for %ti");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logRequestTrailer", logTypeArg);
                specificHandle = specificHandle.bindTo(arg);
                break;
            }
            case "to": {
                if (StringUtil.isEmpty(arg)) {
                    throw new IllegalArgumentException("No arg for %to");
                }
                specificHandle = lookup.findStatic(CustomRequestLog.class, "logResponseTrailer", logTypeArg);
                specificHandle = specificHandle.bindTo(arg);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported code %" + code);
            }
        }
        if (modifiers != null && !modifiers.isEmpty()) {
            MethodHandle dash = this.updateLogHandle(logHandle, append, "-");
            MethodHandle log = MethodHandles.foldArguments(logHandle, specificHandle);
            MethodHandle modifierTest = lookup.findStatic(CustomRequestLog.class, "modify", MethodType.methodType(Boolean.TYPE, List.class, Boolean.class, StringBuilder.class, Request.class, Response.class));
            modifierTest = modifierTest.bindTo(modifiers).bindTo(negated);
            return MethodHandles.guardWithTest(modifierTest, log, dash);
        }
        return MethodHandles.foldArguments(logHandle, specificHandle);
    }

    private static void logNothing(StringBuilder b, Request request, Response response) {
    }

    private static void logServerHost(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getServerName());
    }

    private static void logClientHost(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getRemoteHost());
    }

    private static void logLocalHost(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getHttpChannel().getEndPoint().getLocalAddress().getAddress().getHostAddress());
    }

    private static void logRemoteHost(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getHttpChannel().getEndPoint().getRemoteAddress().getAddress().getHostAddress());
    }

    private static void logServerPort(StringBuilder b, Request request, Response response) {
        b.append(request.getServerPort());
    }

    private static void logClientPort(StringBuilder b, Request request, Response response) {
        b.append(request.getRemotePort());
    }

    private static void logLocalPort(StringBuilder b, Request request, Response response) {
        b.append(request.getHttpChannel().getEndPoint().getLocalAddress().getPort());
    }

    private static void logRemotePort(StringBuilder b, Request request, Response response) {
        b.append(request.getHttpChannel().getEndPoint().getRemoteAddress().getPort());
    }

    private static void logResponseSize(StringBuilder b, Request request, Response response) {
        long written = response.getHttpChannel().getBytesWritten();
        b.append(written);
    }

    private static void logResponseSizeCLF(StringBuilder b, Request request, Response response) {
        long written = response.getHttpChannel().getBytesWritten();
        if (written == 0L) {
            b.append('-');
        } else {
            b.append(written);
        }
    }

    private static void logBytesSent(StringBuilder b, Request request, Response response) {
        b.append(response.getHttpChannel().getBytesWritten());
    }

    private static void logBytesSentCLF(StringBuilder b, Request request, Response response) {
        long sent = response.getHttpChannel().getBytesWritten();
        if (sent == 0L) {
            b.append('-');
        } else {
            b.append(sent);
        }
    }

    private static void logBytesReceived(StringBuilder b, Request request, Response response) {
        b.append(request.getHttpInput().getContentReceived());
    }

    private static void logBytesReceivedCLF(StringBuilder b, Request request, Response response) {
        long received = request.getHttpInput().getContentReceived();
        if (received == 0L) {
            b.append('-');
        } else {
            b.append(received);
        }
    }

    private static void logBytesTransferred(StringBuilder b, Request request, Response response) {
        b.append(request.getHttpInput().getContentReceived() + response.getHttpOutput().getWritten());
    }

    private static void logBytesTransferredCLF(StringBuilder b, Request request, Response response) {
        long transferred = request.getHttpInput().getContentReceived() + response.getHttpOutput().getWritten();
        if (transferred == 0L) {
            b.append('-');
        } else {
            b.append(transferred);
        }
    }

    private static void logRequestCookie(String arg, StringBuilder b, Request request, Response response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (!arg.equals(c.getName())) continue;
                b.append(c.getValue());
                return;
            }
        }
        b.append('-');
    }

    private static void logRequestCookies(StringBuilder b, Request request, Response response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            b.append("-");
        } else {
            for (int i = 0; i < cookies.length; ++i) {
                if (i != 0) {
                    b.append(';');
                }
                b.append(cookies[i].getName());
                b.append('=');
                b.append(cookies[i].getValue());
            }
        }
    }

    private static void logEnvironmentVar(String arg, StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, System.getenv(arg));
    }

    private static void logFilename(StringBuilder b, Request request, Response response) {
        UserIdentity.Scope scope = request.getUserIdentityScope();
        if (scope == null || scope.getContextHandler() == null) {
            b.append('-');
        } else {
            ContextHandler context = scope.getContextHandler();
            int lengthToStrip = scope.getContextPath().length() > 1 ? scope.getContextPath().length() : 0;
            String filename = context.getServletContext().getRealPath(request.getPathInfo().substring(lengthToStrip));
            CustomRequestLog.append(b, filename);
        }
    }

    private static void logRequestProtocol(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getProtocol());
    }

    private static void logRequestHeader(String arg, StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getHeader(arg));
    }

    private static void logKeepAliveRequests(StringBuilder b, Request request, Response response) {
        long requests = request.getHttpChannel().getConnection().getMessagesIn();
        if (requests >= 0L) {
            b.append(requests);
        } else {
            b.append('-');
        }
    }

    private static void logRequestMethod(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getMethod());
    }

    private static void logResponseHeader(String arg, StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, response.getHeader(arg));
    }

    private static void logQueryString(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, "?" + request.getQueryString());
    }

    private static void logRequestFirstLine(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getMethod());
        b.append(" ");
        CustomRequestLog.append(b, request.getOriginalURI());
        b.append(" ");
        CustomRequestLog.append(b, request.getProtocol());
    }

    private static void logRequestHandler(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getServletName());
    }

    private static void logResponseStatus(StringBuilder b, Request request, Response response) {
        b.append(response.getCommittedMetaData().getStatus());
    }

    private static void logRequestTime(DateCache dateCache, StringBuilder b, Request request, Response response) {
        b.append('[');
        CustomRequestLog.append(b, dateCache.format(request.getTimeStamp()));
        b.append(']');
    }

    private static void logLatencyMicroseconds(StringBuilder b, Request request, Response response) {
        long currentTime = System.currentTimeMillis();
        long requestTime = request.getTimeStamp();
        long latencyMs = currentTime - requestTime;
        long latencyUs = TimeUnit.MILLISECONDS.toMicros(latencyMs);
        b.append(latencyUs);
    }

    private static void logLatencyMilliseconds(StringBuilder b, Request request, Response response) {
        long latency = System.currentTimeMillis() - request.getTimeStamp();
        b.append(latency);
    }

    private static void logLatencySeconds(StringBuilder b, Request request, Response response) {
        long latency = System.currentTimeMillis() - request.getTimeStamp();
        b.append(TimeUnit.MILLISECONDS.toSeconds(latency));
    }

    private static void logRequestAuthentication(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, CustomRequestLog.getAuthentication(request, false));
    }

    private static void logRequestAuthenticationWithDeferred(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, CustomRequestLog.getAuthentication(request, true));
    }

    private static void logUrlRequestPath(StringBuilder b, Request request, Response response) {
        CustomRequestLog.append(b, request.getRequestURI());
    }

    private static void logConnectionStatus(StringBuilder b, Request request, Response response) {
        b.append((char)(request.getHttpChannel().isResponseCompleted() ? (request.getHttpChannel().isPersistent() ? 43 : 45) : 88));
    }

    private static void logRequestTrailer(String arg, StringBuilder b, Request request, Response response) {
        HttpFields trailers = request.getTrailers();
        if (trailers != null) {
            CustomRequestLog.append(b, trailers.get(arg));
        } else {
            b.append('-');
        }
    }

    private static void logResponseTrailer(String arg, StringBuilder b, Request request, Response response) {
        Supplier<HttpFields> supplier = response.getTrailers();
        if (supplier != null) {
            HttpFields trailers = supplier.get();
            if (trailers != null) {
                CustomRequestLog.append(b, trailers.get(arg));
            } else {
                b.append('-');
            }
        } else {
            b.append("-");
        }
    }

    private static class Token {
        public final String code;
        public final String arg;
        public final List<Integer> modifiers;
        public final boolean negated;
        public final String literal;

        public Token(String code, String arg, List<Integer> modifiers, boolean negated) {
            this.code = code;
            this.arg = arg;
            this.modifiers = modifiers;
            this.negated = negated;
            this.literal = null;
        }

        public Token(String literal) {
            this.code = null;
            this.arg = null;
            this.modifiers = null;
            this.negated = false;
            this.literal = literal;
        }

        public boolean isLiteralString() {
            return this.literal != null;
        }

        public boolean isPercentCode() {
            return this.code != null;
        }
    }
}

