/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

class ModuleLocation
implements Function<Class<?>, URI> {
    private static final Logger LOG = Log.getLogger(ModuleLocation.class);
    private final Class<?> classModule;
    private final MethodHandle handleGetModule;
    private final MethodHandle handleGetLayer;
    private final MethodHandle handleConfiguration;
    private final MethodHandle handleGetName;
    private final MethodHandle handleOptionalResolvedModule;
    private final MethodHandle handleReference;
    private final MethodHandle handleLocation;

    public ModuleLocation() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        try {
            this.classModule = loader.loadClass("java.lang.Module");
            this.handleGetModule = lookup.findVirtual(Class.class, "getModule", MethodType.methodType(this.classModule));
            Class<?> classLayer = loader.loadClass("java.lang.ModuleLayer");
            this.handleGetLayer = lookup.findVirtual(this.classModule, "getLayer", MethodType.methodType(classLayer));
            Class<?> classConfiguration = loader.loadClass("java.lang.module.Configuration");
            this.handleConfiguration = lookup.findVirtual(classLayer, "configuration", MethodType.methodType(classConfiguration));
            this.handleGetName = lookup.findVirtual(this.classModule, "getName", MethodType.methodType(String.class));
            Method findModuleMethod = classConfiguration.getMethod("findModule", String.class);
            this.handleOptionalResolvedModule = lookup.findVirtual(classConfiguration, "findModule", MethodType.methodType(findModuleMethod.getReturnType(), String.class));
            Class<?> classResolvedModule = loader.loadClass("java.lang.module.ResolvedModule");
            Class<?> classReference = loader.loadClass("java.lang.module.ModuleReference");
            this.handleReference = lookup.findVirtual(classResolvedModule, "reference", MethodType.methodType(classReference));
            Method locationMethod = classReference.getMethod("location", new Class[0]);
            this.handleLocation = lookup.findVirtual(classReference, "location", MethodType.methodType(locationMethod.getReturnType()));
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
            throw new UnsupportedOperationException("Not supported on this runtime", e);
        }
    }

    @Override
    public URI apply(Class<?> clazz) {
        block8: {
            try {
                Object module = this.handleGetModule.invoke(clazz);
                if (module == null) {
                    return null;
                }
                Object layer = this.handleGetLayer.invoke(module);
                if (layer == null) {
                    return null;
                }
                Object configuration = this.handleConfiguration.invoke(layer);
                if (configuration == null) {
                    return null;
                }
                String moduleName = this.handleGetName.invoke(module);
                if (moduleName == null) {
                    return null;
                }
                Optional optionalResolvedModule = this.handleOptionalResolvedModule.invoke(configuration, moduleName);
                if (!optionalResolvedModule.isPresent()) {
                    return null;
                }
                Object resolved = optionalResolvedModule.get();
                Object moduleReference = this.handleReference.invoke(resolved);
                Optional location = this.handleLocation.invoke(moduleReference);
                if (location != null || location.isPresent()) {
                    return (URI)location.get();
                }
            }
            catch (Throwable ignored) {
                if (!LOG.isDebugEnabled()) break block8;
                LOG.ignore(ignored);
            }
        }
        return null;
    }
}

