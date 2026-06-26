/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGEnvironment;
import org.postgresql.PGProperty;
import org.postgresql.util.OSUtil;
import org.postgresql.util.PGPropertyUtil;

public class PGPropertyServiceParser {
    private static final Logger LOGGER = Logger.getLogger(PGPropertyServiceParser.class.getName());
    private final String serviceName;
    private boolean ignoreIfOpenFails = true;

    private PGPropertyServiceParser(String serviceName) {
        this.serviceName = serviceName;
    }

    public static @Nullable Properties getServiceProperties(String serviceName) {
        PGPropertyServiceParser pgPropertyServiceParser = new PGPropertyServiceParser(serviceName);
        return pgPropertyServiceParser.findServiceDescription();
    }

    private @Nullable Properties findServiceDescription() {
        String resourceName = this.findPgServiceConfResourceName();
        if (resourceName == null) {
            return null;
        }
        Properties result = null;
        try (InputStream inputStream = this.openInputStream(resourceName);){
            result = this.parseInputStream(inputStream);
        }
        catch (IOException e) {
            Level level = this.ignoreIfOpenFails ? Level.FINE : Level.WARNING;
            LOGGER.log(level, "Failed to handle resource [{0}] with error [{1}]", new Object[]{resourceName, e.getMessage()});
        }
        return result;
    }

    private InputStream openInputStream(String resourceName) throws IOException {
        try {
            URL url = new URL(resourceName);
            return url.openStream();
        }
        catch (MalformedURLException ex) {
            File file = new File(resourceName);
            return new FileInputStream(file);
        }
    }

    private @Nullable String findPgServiceConfResourceName() {
        String pgServceConfFileDefaultName = PGEnvironment.PGSERVICEFILE.getDefaultValue();
        String propertyName = PGEnvironment.ORG_POSTGRESQL_PGSERVICEFILE.getName();
        String resourceName = System.getProperty(propertyName);
        if (resourceName != null && !resourceName.trim().isEmpty()) {
            this.ignoreIfOpenFails = false;
            LOGGER.log(Level.FINE, "Value [{0}] selected from property [{1}]", new Object[]{resourceName, propertyName});
            return resourceName;
        }
        String envVariableName = PGEnvironment.PGSERVICEFILE.getName();
        resourceName = System.getenv().get(envVariableName);
        if (resourceName != null && !resourceName.trim().isEmpty()) {
            this.ignoreIfOpenFails = false;
            LOGGER.log(Level.FINE, "Value [{0}] selected from environment variable [{1}]", new Object[]{resourceName, envVariableName});
            return resourceName;
        }
        String resourceName2 = "." + pgServceConfFileDefaultName;
        File resourceFile = new File(OSUtil.getUserConfigRootDirectory(), resourceName2);
        if (resourceFile.canRead()) {
            LOGGER.log(Level.FINE, "Value [{0}] selected because file exist in user home directory", new Object[]{resourceFile.getAbsolutePath()});
            return resourceFile.getAbsolutePath();
        }
        envVariableName = PGEnvironment.PGSYSCONFDIR.getName();
        String pgSysconfDir = System.getenv().get(envVariableName);
        if (pgSysconfDir != null && !pgSysconfDir.trim().isEmpty()) {
            String resourceName3 = pgSysconfDir + File.separator + pgServceConfFileDefaultName;
            LOGGER.log(Level.FINE, "Value [{0}] selected using environment variable [{1}]", new Object[]{resourceName3, envVariableName});
            return resourceName3;
        }
        LOGGER.log(Level.FINE, "Value for resource [{0}] not found", pgServceConfFileDefaultName);
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private @Nullable Properties parseInputStream(InputStream inputStream) throws IOException {
        Set allowedServiceKeys = Arrays.stream(PGProperty.values()).map(PGProperty::getName).map(PGPropertyUtil::translatePGPropertyToPGService).collect(Collectors.toSet());
        Properties result = new Properties();
        boolean isFound = false;
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader);){
            String originalLine;
            int lineNumber = 0;
            while ((originalLine = br.readLine()) != null) {
                ++lineNumber;
                String line = originalLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int indexOfEqualSign = line.indexOf("=");
                if (line.startsWith("[") && line.endsWith("]")) {
                    if (!isFound) {
                        String sectionName = line.substring(1, line.length() - 1);
                        if (!this.serviceName.equals(sectionName)) continue;
                        isFound = true;
                        continue;
                    }
                    break;
                }
                if (!isFound) continue;
                if (indexOfEqualSign <= 1) {
                    LOGGER.log(Level.WARNING, "Not valid line: line number [{0}], value [{1}]", new Object[]{lineNumber, originalLine});
                    Properties properties = null;
                    return properties;
                }
                String key = line.substring(0, indexOfEqualSign);
                String value = line.substring(indexOfEqualSign + 1);
                if (!allowedServiceKeys.contains(key)) {
                    String allowedValuesCommaSeparated = allowedServiceKeys.stream().sorted().collect(Collectors.joining(","));
                    LOGGER.log(Level.SEVERE, "Got invalid key: line number [{0}], value [{1}], allowed values [{2}]", new Object[]{lineNumber, originalLine, allowedValuesCommaSeparated});
                    Properties properties = null;
                    return properties;
                }
                if (value.isEmpty()) continue;
                result.putIfAbsent(PGPropertyUtil.translatePGServiceToPGProperty(key), value);
            }
        }
        if (!isFound) return null;
        Properties properties = result;
        return properties;
    }
}

