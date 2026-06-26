/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class KerberosTicket {
    private static final String CONFIG_ITEM_NAME = "ticketCache";
    private static final String KRBLOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

    public static boolean credentialCacheExists(Properties info) {
        Subject sub;
        LoginContext lc = null;
        Configuration existingConfiguration = Configuration.getConfiguration();
        Configuration.setConfiguration(new CustomKrbConfig());
        try {
            lc = new LoginContext(CONFIG_ITEM_NAME, new CallbackHandler(){

                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    throw new RuntimeException("This is an error, you should set doNotPrompt to false in jaas.config");
                }
            });
            lc.login();
        }
        catch (LoginException e) {
            if (existingConfiguration != null) {
                Configuration.setConfiguration(existingConfiguration);
            }
            return false;
        }
        if (existingConfiguration != null) {
            Configuration.setConfiguration(existingConfiguration);
        }
        return (sub = lc.getSubject()) != null;
    }

    static class CustomKrbConfig
    extends Configuration {
        CustomKrbConfig() {
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if (KerberosTicket.CONFIG_ITEM_NAME.equals(name)) {
                HashMap<String, String> options = new HashMap<String, String>();
                options.put("refreshKrb5Config", Boolean.FALSE.toString());
                options.put("useTicketCache", Boolean.TRUE.toString());
                options.put("doNotPrompt", Boolean.TRUE.toString());
                options.put("useKeyTab", Boolean.TRUE.toString());
                options.put("isInitiator", Boolean.FALSE.toString());
                options.put("renewTGT", Boolean.FALSE.toString());
                options.put("debug", Boolean.FALSE.toString());
                return new AppConfigurationEntry[]{new AppConfigurationEntry(KerberosTicket.KRBLOGIN_MODULE, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
            }
            return null;
        }
    }
}

