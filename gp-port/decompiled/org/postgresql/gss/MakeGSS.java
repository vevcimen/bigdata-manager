/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.gss;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ietf.jgss.GSSCredential;
import org.postgresql.PGProperty;
import org.postgresql.core.PGStream;
import org.postgresql.gss.GSSCallbackHandler;
import org.postgresql.gss.GssAction;
import org.postgresql.gss.GssEncAction;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class MakeGSS {
    private static final Logger LOGGER = Logger.getLogger(MakeGSS.class.getName());
    private static final @Nullable MethodHandle SUBJECT_CURRENT;
    private static final @Nullable MethodHandle ACCESS_CONTROLLER_GET_CONTEXT;
    private static final @Nullable MethodHandle SUBJECT_GET_SUBJECT;
    private static final @Nullable MethodHandle SUBJECT_DO_AS;
    private static final @Nullable MethodHandle SUBJECT_CALL_AS;

    private static @Nullable Subject getCurrentSubject() {
        try {
            if (SUBJECT_CURRENT != null) {
                return SUBJECT_CURRENT.invokeExact();
            }
            if (SUBJECT_GET_SUBJECT == null || ACCESS_CONTROLLER_GET_CONTEXT == null) {
                return null;
            }
            return SUBJECT_GET_SUBJECT.invoke(ACCESS_CONTROLLER_GET_CONTEXT.invoke());
        }
        catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            if (e instanceof Error) {
                throw (Error)e;
            }
            throw new RuntimeException(e);
        }
    }

    public static void authenticate(boolean encrypted, PGStream pgStream, String host, String user, char @Nullable [] password, @Nullable String jaasApplicationName, @Nullable String kerberosServerName, boolean useSpnego, boolean jaasLogin, boolean logServerErrorDetail) throws IOException, PSQLException {
        Exception result;
        block11: {
            LOGGER.log(Level.FINEST, " <=BE AuthenticationReqGSS");
            if (jaasApplicationName == null) {
                jaasApplicationName = PGProperty.JAAS_APPLICATION_NAME.getDefaultValue();
            }
            if (kerberosServerName == null) {
                kerberosServerName = "postgres";
            }
            try {
                Set<GSSCredential> gssCreds;
                boolean performAuthentication = jaasLogin;
                Subject sub = MakeGSS.getCurrentSubject();
                if (sub != null && (gssCreds = sub.getPrivateCredentials(GSSCredential.class)) != null && !gssCreds.isEmpty()) {
                    performAuthentication = false;
                }
                if (performAuthentication) {
                    LoginContext lc = new LoginContext(Nullness.castNonNull(jaasApplicationName), new GSSCallbackHandler(user, password));
                    lc.login();
                    sub = lc.getSubject();
                }
                Callable<Exception> action = encrypted ? new GssEncAction(pgStream, sub, host, user, kerberosServerName, useSpnego, logServerErrorDetail) : new GssAction(pgStream, sub, host, user, kerberosServerName, useSpnego, logServerErrorDetail);
                @NonNull Subject subject = sub;
                if (SUBJECT_DO_AS != null) {
                    result = SUBJECT_DO_AS.invoke(subject, (PrivilegedAction)((Object)action));
                    break block11;
                }
                if (SUBJECT_CALL_AS != null) {
                    result = SUBJECT_CALL_AS.invoke(subject, (PrivilegedAction)((Object)action));
                    break block11;
                }
                throw new PSQLException(GT.tr("Neither Subject.doAs (Java before 18) nor Subject.callAs (Java 18+) method found", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
            }
            catch (Throwable e) {
                throw new PSQLException(GT.tr("GSS Authentication failed", new Object[0]), PSQLState.CONNECTION_FAILURE, e);
            }
        }
        if (result instanceof IOException) {
            throw (IOException)result;
        }
        if (result instanceof PSQLException) {
            throw (PSQLException)result;
        }
        if (result != null) {
            throw new PSQLException(GT.tr("GSS Authentication failed", new Object[0]), PSQLState.CONNECTION_FAILURE, (Throwable)result);
        }
    }

    static {
        MethodHandle subjectCurrent = null;
        try {
            subjectCurrent = MethodHandles.lookup().findStatic(Subject.class, "current", MethodType.methodType(Subject.class));
        }
        catch (IllegalAccessException | NoSuchMethodException reflectiveOperationException) {
            // empty catch block
        }
        SUBJECT_CURRENT = subjectCurrent;
        MethodHandle accessControllerGetContext = null;
        MethodHandle subjectGetSubject = null;
        try {
            Class<?> accessControllerClass = Class.forName("java.security.AccessController");
            Class<?> accessControlContextClass = Class.forName("java.security.AccessControlContext");
            accessControllerGetContext = MethodHandles.lookup().findStatic(accessControllerClass, "getContext", MethodType.methodType(accessControlContextClass));
            subjectGetSubject = MethodHandles.lookup().findStatic(Subject.class, "getSubject", MethodType.methodType(Subject.class, accessControlContextClass));
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException accessControllerClass) {
            // empty catch block
        }
        ACCESS_CONTROLLER_GET_CONTEXT = accessControllerGetContext;
        SUBJECT_GET_SUBJECT = subjectGetSubject;
        MethodHandle subjectDoAs = null;
        try {
            subjectDoAs = MethodHandles.lookup().findStatic(Subject.class, "doAs", MethodType.methodType(Object.class, Subject.class, PrivilegedAction.class));
        }
        catch (IllegalAccessException | NoSuchMethodException accessControlContextClass) {
            // empty catch block
        }
        SUBJECT_DO_AS = subjectDoAs;
        MethodHandle subjectCallAs = null;
        try {
            subjectCallAs = MethodHandles.lookup().findStatic(Subject.class, "callAs", MethodType.methodType(Object.class, Subject.class, Callable.class));
        }
        catch (IllegalAccessException | NoSuchMethodException reflectiveOperationException) {
            // empty catch block
        }
        SUBJECT_CALL_AS = subjectCallAs;
    }
}

