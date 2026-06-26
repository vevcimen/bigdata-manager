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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGEnvironment;
import org.postgresql.util.OSUtil;

public class PGPropertyPasswordParser {
    private static final Logger LOGGER = Logger.getLogger(PGPropertyPasswordParser.class.getName());
    private static final char SEPARATOR = ':';
    private final String hostname;
    private final String port;
    private final String database;
    private final String user;

    private PGPropertyPasswordParser(String hostname, String port, String database, String user) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = user;
    }

    public static @Nullable String getPassword(@Nullable String hostname, @Nullable String port, @Nullable String database, @Nullable String user) {
        if (hostname == null || hostname.isEmpty()) {
            return null;
        }
        if (port == null || port.isEmpty()) {
            return null;
        }
        if (database == null || database.isEmpty()) {
            return null;
        }
        if (user == null || user.isEmpty()) {
            return null;
        }
        PGPropertyPasswordParser pgPropertyPasswordParser = new PGPropertyPasswordParser(hostname, port, database, user);
        return pgPropertyPasswordParser.findPassword();
    }

    private @Nullable String findPassword() {
        String resourceName = this.findPgPasswordResourceName();
        if (resourceName == null) {
            return null;
        }
        String result = null;
        try (InputStream inputStream = this.openInputStream(resourceName);){
            result = this.parseInputStream(inputStream);
        }
        catch (IOException e) {
            LOGGER.log(Level.FINE, "Failed to handle resource [{0}] with error [{1}]", new Object[]{resourceName, e.getMessage()});
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

    private @Nullable String findPgPasswordResourceName() {
        File resourceFile;
        String pgPassFileDefaultName = PGEnvironment.PGPASSFILE.getDefaultValue();
        String propertyName = PGEnvironment.ORG_POSTGRESQL_PGPASSFILE.getName();
        String resourceName = System.getProperty(propertyName);
        if (resourceName != null && !resourceName.trim().isEmpty()) {
            LOGGER.log(Level.FINE, "Value [{0}] selected from property [{1}]", new Object[]{resourceName, propertyName});
            return resourceName;
        }
        String envVariableName = PGEnvironment.PGPASSFILE.getName();
        resourceName = System.getenv().get(envVariableName);
        if (resourceName != null && !resourceName.trim().isEmpty()) {
            LOGGER.log(Level.FINE, "Value [{0}] selected from environment variable [{1}]", new Object[]{resourceName, envVariableName});
            return resourceName;
        }
        String resourceName2 = "";
        if (!OSUtil.isWindows()) {
            resourceName2 = resourceName2 + ".";
        }
        resourceName2 = resourceName2 + pgPassFileDefaultName;
        if (OSUtil.isWindows()) {
            resourceName2 = resourceName2 + ".conf";
        }
        if ((resourceFile = new File(OSUtil.getUserConfigRootDirectory(), resourceName2)).canRead()) {
            LOGGER.log(Level.FINE, "Value [{0}] selected because file exist in user home directory", new Object[]{resourceFile.getAbsolutePath()});
            return resourceFile.getAbsolutePath();
        }
        LOGGER.log(Level.FINE, "Value for resource [{0}] not found", pgPassFileDefaultName);
        return null;
    }

    private @Nullable String parseInputStream(InputStream inputStream) throws IOException {
        String result = null;
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader);){
            String line;
            int currentLine = 0;
            while ((line = br.readLine()) != null && (line.trim().isEmpty() || line.startsWith("#") || (result = this.evaluateLine(line, ++currentLine)) == null)) {
            }
        }
        return result;
    }

    private @Nullable String evaluateLine(String fullLine, int currentLine) {
        String line = fullLine;
        String result = null;
        if ((line = this.checkForPattern(line, this.hostname)) != null && (line = this.checkForPattern(line, this.port)) != null && (line = this.checkForPattern(line, this.database)) != null && (line = this.checkForPattern(line, this.user)) != null) {
            result = this.extractPassword(line);
            String lineWithoutPassword = fullLine.substring(0, fullLine.length() - line.length());
            LOGGER.log(Level.FINE, "Matching line number [{0}] with value prefix [{1}] found for input [{2}:{3}:{4}:{5}]", new Object[]{currentLine, lineWithoutPassword, this.hostname, this.port, this.database, this.user});
        }
        return result;
    }

    private String extractPassword(String line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); ++i) {
            char chr = line.charAt(i);
            if (chr == '\\' && i + 1 < line.length()) {
                char nextChr = line.charAt(i + 1);
                if (nextChr == '\\' || nextChr == ':') {
                    chr = nextChr;
                    ++i;
                }
            } else if (chr == ':') break;
            sb.append(chr);
        }
        return sb.toString();
    }

    private @Nullable String checkForPattern(String line, String value) {
        String result = null;
        if (line.startsWith("*:")) {
            result = line.substring(2);
        } else {
            int lPos = 0;
            for (int vPos = 0; vPos < value.length(); ++vPos) {
                if (lPos >= line.length()) {
                    return null;
                }
                char l = line.charAt(lPos);
                if (l == '\\') {
                    if (lPos + 1 >= line.length()) {
                        return null;
                    }
                    char next = line.charAt(lPos + 1);
                    if (next == '\\' || next == ':') {
                        l = next;
                        ++lPos;
                    }
                }
                ++lPos;
                char v = value.charAt(vPos);
                if (l == v) continue;
                return null;
            }
            if (line.charAt(lPos) == ':') {
                result = line.substring(lPos + 1);
            }
        }
        return result;
    }
}

