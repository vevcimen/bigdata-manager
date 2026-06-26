/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.eclipse.jetty.util.resource.Resource;

public class ResourceCollection
extends Resource {
    private Resource[] _resources;

    public ResourceCollection() {
        this._resources = new Resource[0];
    }

    public ResourceCollection(Resource ... resources) {
        ArrayList<Resource> list = new ArrayList<Resource>();
        for (Resource r : resources) {
            if (r == null) continue;
            if (r instanceof ResourceCollection) {
                Collections.addAll(list, ((ResourceCollection)r).getResources());
                continue;
            }
            list.add(r);
        }
        this._resources = list.toArray(new Resource[0]);
        for (Resource r : this._resources) {
            this.assertResourceValid(r);
        }
    }

    public ResourceCollection(String[] resources) {
        if (resources == null || resources.length == 0) {
            this._resources = null;
            return;
        }
        ArrayList<Resource> res = new ArrayList<Resource>();
        try {
            for (String strResource : resources) {
                if (strResource == null || strResource.length() == 0) {
                    throw new IllegalArgumentException("empty/null resource path not supported");
                }
                Resource resource = Resource.newResource(strResource);
                this.assertResourceValid(resource);
                res.add(resource);
            }
            if (res.isEmpty()) {
                this._resources = null;
                return;
            }
            this._resources = res.toArray(new Resource[0]);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResourceCollection(String csvResources) {
        this.setResourcesAsCSV(csvResources);
    }

    public Resource[] getResources() {
        return this._resources;
    }

    public void setResources(Resource[] resources) {
        if (resources == null || resources.length == 0) {
            this._resources = null;
            return;
        }
        ArrayList<Resource> res = new ArrayList<Resource>();
        for (Resource resource : resources) {
            this.assertResourceValid(resource);
            res.add(resource);
        }
        if (res.isEmpty()) {
            this._resources = null;
            return;
        }
        this._resources = res.toArray(new Resource[0]);
    }

    public void setResourcesAsCSV(String csvResources) {
        if (csvResources == null) {
            throw new IllegalArgumentException("CSV String is null");
        }
        StringTokenizer tokenizer = new StringTokenizer(csvResources, ",;");
        int len = tokenizer.countTokens();
        if (len == 0) {
            throw new IllegalArgumentException("ResourceCollection@setResourcesAsCSV(String)  argument must be a string containing one or more comma-separated resource strings.");
        }
        ArrayList<Resource> res = new ArrayList<Resource>();
        try {
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (token.length() == 0) continue;
                Resource resource = Resource.newResource(token);
                this.assertResourceValid(resource);
                res.add(resource);
            }
            if (res.isEmpty()) {
                this._resources = null;
                return;
            }
            this._resources = res.toArray(new Resource[0]);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource addPath(String path) throws IOException {
        int i;
        this.assertResourcesSet();
        if (path == null) {
            throw new MalformedURLException();
        }
        if (path.length() == 0 || "/".equals(path)) {
            return this;
        }
        Resource resource = null;
        ArrayList<Resource> resources = null;
        for (i = 0; i < this._resources.length; ++i) {
            resource = this._resources[i].addPath(path);
            if (!resource.exists()) continue;
            if (resource.isDirectory()) break;
            return resource;
        }
        ++i;
        while (i < this._resources.length) {
            Resource r = this._resources[i].addPath(path);
            if (r.exists() && r.isDirectory()) {
                if (resources == null) {
                    resources = new ArrayList<Resource>();
                }
                if (resource != null) {
                    resources.add(resource);
                    resource = null;
                }
                resources.add(r);
            }
            ++i;
        }
        if (resource != null) {
            return resource;
        }
        if (resources != null) {
            return new ResourceCollection(resources.toArray(new Resource[0]));
        }
        return null;
    }

    @Override
    public boolean delete() throws SecurityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists() {
        this.assertResourcesSet();
        return true;
    }

    @Override
    public File getFile() throws IOException {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            File f = r.getFile();
            if (f == null) continue;
            return f;
        }
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            InputStream is = r.getInputStream();
            if (is == null) continue;
            return is;
        }
        return null;
    }

    @Override
    public ReadableByteChannel getReadableByteChannel() throws IOException {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            ReadableByteChannel channel = r.getReadableByteChannel();
            if (channel == null) continue;
            return channel;
        }
        return null;
    }

    @Override
    public String getName() {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            String name = r.getName();
            if (name == null) continue;
            return name;
        }
        return null;
    }

    @Override
    public URL getURL() {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            URL url = r.getURL();
            if (url == null) continue;
            return url;
        }
        return null;
    }

    @Override
    public boolean isDirectory() {
        this.assertResourcesSet();
        return true;
    }

    @Override
    public long lastModified() {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            long lm = r.lastModified();
            if (lm == -1L) continue;
            return lm;
        }
        return -1L;
    }

    @Override
    public long length() {
        return -1L;
    }

    @Override
    public String[] list() {
        this.assertResourcesSet();
        HashSet set = new HashSet();
        for (Resource r : this._resources) {
            String[] list = r.list();
            if (list == null) continue;
            Collections.addAll(set, list);
        }
        Object[] result = set.toArray(new String[0]);
        Arrays.sort(result);
        return result;
    }

    @Override
    public void close() {
        this.assertResourcesSet();
        for (Resource r : this._resources) {
            r.close();
        }
    }

    @Override
    public boolean renameTo(Resource dest) throws SecurityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyTo(File destination) throws IOException {
        this.assertResourcesSet();
        int r = this._resources.length;
        while (r-- > 0) {
            this._resources[r].copyTo(destination);
        }
    }

    public String toString() {
        if (this._resources == null || this._resources.length == 0) {
            return "[]";
        }
        return String.valueOf(Arrays.asList(this._resources));
    }

    @Override
    public boolean isContainedIn(Resource r) {
        return false;
    }

    private void assertResourcesSet() {
        if (this._resources == null || this._resources.length == 0) {
            throw new IllegalStateException("*resources* not set.");
        }
    }

    private void assertResourceValid(Resource resource) {
        if (resource == null) {
            throw new IllegalStateException("Null resource not supported");
        }
        if (!resource.exists() || !resource.isDirectory()) {
            throw new IllegalArgumentException(resource + " is not an existing directory.");
        }
    }
}

