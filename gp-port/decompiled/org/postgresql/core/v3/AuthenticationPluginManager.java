/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGProperty;
import org.postgresql.plugin.AuthenticationPlugin;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.GT;
import org.postgresql.util.ObjectFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

class AuthenticationPluginManager {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationPluginManager.class.getName());

    private AuthenticationPluginManager() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static <T> T withPassword(AuthenticationRequestType type, Properties info, PasswordAction<char @Nullable [], T> action) throws PSQLException, IOException {
        char[] password = null;
        String authPluginClassName = PGProperty.AUTHENTICATION_PLUGIN_CLASS_NAME.get(info);
        if (authPluginClassName == null || authPluginClassName.equals("")) {
            String passwordText = PGProperty.PASSWORD.get(info);
            if (passwordText != null) {
                password = passwordText.toCharArray();
            }
        } else {
            AuthenticationPlugin authPlugin;
            try {
                authPlugin = ObjectFactory.instantiate(AuthenticationPlugin.class, authPluginClassName, info, false, null);
            }
            catch (Exception ex) {
                String msg = GT.tr("Unable to load Authentication Plugin {0}", authPluginClassName);
                LOGGER.log(Level.FINE, msg, ex);
                throw new PSQLException(msg, PSQLState.INVALID_PARAMETER_VALUE, (Throwable)ex);
            }
            password = authPlugin.getPassword(type);
        }
        try {
            T t = action.apply(password);
            return t;
        }
        finally {
            if (password != null) {
                Arrays.fill(password, '\u0000');
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static <T> T withEncodedPassword(AuthenticationRequestType type, Properties info, PasswordAction<byte[], T> action) throws PSQLException, IOException {
        byte[] encodedPassword = AuthenticationPluginManager.withPassword(type, info, password -> {
            if (password == null) {
                throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided by plugin {0}", PGProperty.AUTHENTICATION_PLUGIN_CLASS_NAME.get(info)), PSQLState.CONNECTION_REJECTED);
            }
            ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
            byte[] bytes = new byte[buf.limit()];
            buf.get(bytes);
            return bytes;
        });
        try {
            T t = action.apply(encodedPassword);
            return t;
        }
        finally {
            Arrays.fill(encodedPassword, (byte)0);
        }
    }

    @FunctionalInterface
    public static interface PasswordAction<T, R> {
        public R apply(T var1) throws PSQLException, IOException;
    }
}

