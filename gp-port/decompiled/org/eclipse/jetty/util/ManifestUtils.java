/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestUtils {
    private ManifestUtils() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Optional<Manifest> getManifest(Class<?> klass) {
        try {
            CodeSource codeSource = klass.getProtectionDomain().getCodeSource();
            if (codeSource == null) return Optional.empty();
            URL location = codeSource.getLocation();
            if (location == null) return Optional.empty();
            try (JarFile jarFile = new JarFile(new File(location.toURI()));){
                Optional<Manifest> optional = Optional.of(jarFile.getManifest());
                return optional;
            }
        }
        catch (Throwable x) {
            return Optional.empty();
        }
    }

    public static Optional<String> getVersion(Class<?> klass) {
        Optional<String> version = ManifestUtils.getManifest(klass).map(Manifest::getMainAttributes).map(attributes -> attributes.getValue("Implementation-Version"));
        if (version.isPresent()) {
            return version;
        }
        try {
            Object module = klass.getClass().getMethod("getModule", new Class[0]).invoke(klass, new Object[0]);
            Object descriptor = module.getClass().getMethod("getDescriptor", new Class[0]).invoke(module, new Object[0]);
            return (Optional)descriptor.getClass().getMethod("rawVersion", new Class[0]).invoke(descriptor, new Object[0]);
        }
        catch (Throwable x) {
            return Optional.empty();
        }
    }
}

