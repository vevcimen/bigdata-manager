/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.LastErrorException
 *  com.sun.jna.Platform
 *  com.sun.jna.platform.win32.Sspi$SecBufferDesc
 *  com.sun.jna.platform.win32.Win32Exception
 *  waffle.windows.auth.IWindowsCredentialsHandle
 *  waffle.windows.auth.impl.WindowsCredentialsHandleImpl
 *  waffle.windows.auth.impl.WindowsSecurityContextImpl
 */
package org.postgresql.sspi;

import com.sun.jna.LastErrorException;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Sspi;
import com.sun.jna.platform.win32.Win32Exception;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.PGStream;
import org.postgresql.sspi.ISSPIClient;
import org.postgresql.sspi.NTDSAPIWrapper;
import org.postgresql.util.HostSpec;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class SSPIClient
implements ISSPIClient {
    public static final String SSPI_DEFAULT_SPN_SERVICE_CLASS = "POSTGRES";
    private static final Logger LOGGER = Logger.getLogger(SSPIClient.class.getName());
    private final PGStream pgStream;
    private final String spnServiceClass;
    private final boolean enableNegotiate;
    private @Nullable IWindowsCredentialsHandle clientCredentials;
    private @Nullable WindowsSecurityContextImpl sspiContext;
    private @Nullable String targetName;

    public SSPIClient(PGStream pgStream, String spnServiceClass, boolean enableNegotiate) {
        this.pgStream = pgStream;
        if (spnServiceClass == null || spnServiceClass.isEmpty()) {
            spnServiceClass = SSPI_DEFAULT_SPN_SERVICE_CLASS;
        }
        this.spnServiceClass = spnServiceClass;
        this.enableNegotiate = enableNegotiate;
    }

    @Override
    public boolean isSSPISupported() {
        try {
            if (!Platform.isWindows()) {
                LOGGER.log(Level.FINE, "SSPI not supported: non-Windows host");
                return false;
            }
            Class.forName("waffle.windows.auth.impl.WindowsSecurityContextImpl");
            return true;
        }
        catch (NoClassDefFoundError ex) {
            LOGGER.log(Level.WARNING, "SSPI unavailable (no Waffle/JNA libraries?)", ex);
            return false;
        }
        catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "SSPI unavailable (no Waffle/JNA libraries?)", ex);
            return false;
        }
    }

    private String makeSPN() throws PSQLException {
        HostSpec hs = this.pgStream.getHostSpec();
        try {
            return NTDSAPIWrapper.instance.DsMakeSpn(this.spnServiceClass, hs.getHost(), null, (short)0, null);
        }
        catch (LastErrorException ex) {
            throw new PSQLException("SSPI setup failed to determine SPN", PSQLState.CONNECTION_UNABLE_TO_CONNECT, (Throwable)ex);
        }
    }

    @Override
    public void startSSPI() throws SQLException, IOException {
        String securityPackage = this.enableNegotiate ? "negotiate" : "kerberos";
        LOGGER.log(Level.FINEST, "Beginning SSPI/Kerberos negotiation with SSPI package: {0}", securityPackage);
        try {
            IWindowsCredentialsHandle clientCredentials;
            try {
                this.clientCredentials = clientCredentials = WindowsCredentialsHandleImpl.getCurrent((String)securityPackage);
                clientCredentials.initialize();
            }
            catch (Win32Exception ex) {
                throw new PSQLException("Could not obtain local Windows credentials for SSPI", PSQLState.CONNECTION_UNABLE_TO_CONNECT, (Throwable)ex);
            }
            try {
                String targetName;
                this.targetName = targetName = this.makeSPN();
                LOGGER.log(Level.FINEST, "SSPI target name: {0}", targetName);
                this.sspiContext = new WindowsSecurityContextImpl();
                this.sspiContext.setPrincipalName(targetName);
                this.sspiContext.setCredentialsHandle(clientCredentials);
                this.sspiContext.setSecurityPackage(securityPackage);
                this.sspiContext.initialize(null, null, targetName);
            }
            catch (Win32Exception ex) {
                throw new PSQLException("Could not initialize SSPI security context", PSQLState.CONNECTION_UNABLE_TO_CONNECT, (Throwable)ex);
            }
            this.sendSSPIResponse(this.sspiContext.getToken());
            LOGGER.log(Level.FINEST, "Sent first SSPI negotiation message");
        }
        catch (NoClassDefFoundError ex) {
            throw new PSQLException("SSPI cannot be used, Waffle or its dependencies are missing from the classpath", PSQLState.NOT_IMPLEMENTED, (Throwable)ex);
        }
    }

    @Override
    public void continueSSPI(int msgLength) throws SQLException, IOException {
        WindowsSecurityContextImpl sspiContext = this.sspiContext;
        if (sspiContext == null) {
            throw new IllegalStateException("Cannot continue SSPI authentication that we didn't begin");
        }
        LOGGER.log(Level.FINEST, "Continuing SSPI negotiation");
        byte[] receivedToken = this.pgStream.receive(msgLength);
        Sspi.SecBufferDesc continueToken = new Sspi.SecBufferDesc(2, receivedToken);
        sspiContext.initialize(sspiContext.getHandle(), continueToken, Nullness.castNonNull(this.targetName));
        byte[] responseToken = sspiContext.getToken();
        if (responseToken.length > 0) {
            this.sendSSPIResponse(responseToken);
            LOGGER.log(Level.FINEST, "Sent SSPI negotiation continuation message");
        } else {
            LOGGER.log(Level.FINEST, "SSPI authentication complete, no reply required");
        }
    }

    private void sendSSPIResponse(byte[] outToken) throws IOException {
        this.pgStream.sendChar(112);
        this.pgStream.sendInteger4(4 + outToken.length);
        this.pgStream.send(outToken);
        this.pgStream.flush();
    }

    @Override
    public void dispose() {
        if (this.sspiContext != null) {
            this.sspiContext.dispose();
            this.sspiContext = null;
        }
        if (this.clientCredentials != null) {
            this.clientCredentials.dispose();
            this.clientCredentials = null;
        }
    }
}

