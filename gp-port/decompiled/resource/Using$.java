/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.Traversable
 *  scala.collection.TraversableOnce
 *  scala.reflect.ClassManifestFactory$
 *  scala.reflect.OptManifest
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.LazyRef
 */
package resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import resource.ManagedResource;
import resource.Resource;
import resource.Resource$;
import resource.Using$traverser$2$;
import resource.package$;
import scala.Function0;
import scala.Function1;
import scala.Predef$;
import scala.collection.Traversable;
import scala.collection.TraversableOnce;
import scala.reflect.ClassManifestFactory$;
import scala.reflect.OptManifest;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.LazyRef;

public final class Using$ {
    public static Using$ MODULE$;
    private final Charset utf8;

    static {
        new Using$();
    }

    public ManagedResource<BufferedOutputStream> bufferedOutputStream(Function0<OutputStream> out) {
        return package$.MODULE$.managed(out, Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(OutputStream.class)).map((Function1 & Serializable & scala.Serializable)x$1 -> new BufferedOutputStream((OutputStream)x$1));
    }

    public ManagedResource<BufferedInputStream> bufferedInputStream(Function0<InputStream> in) {
        return package$.MODULE$.managed(in, Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(InputStream.class)).map((Function1 & Serializable & scala.Serializable)x$2 -> new BufferedInputStream((InputStream)x$2));
    }

    public <T> ManagedResource<T> file(Function1<File, T> in, File source, Resource<T> evidence$1, OptManifest<T> evidence$2) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> Using$.open$1(in, source), evidence$1, evidence$2);
    }

    public ManagedResource<BufferedInputStream> fileInputStream(File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new BufferedInputStream(new FileInputStream((File)f)), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(BufferedInputStream.class));
    }

    public ManagedResource<BufferedOutputStream> fileOutputStream(File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new BufferedOutputStream(new FileOutputStream((File)f)), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(BufferedOutputStream.class));
    }

    public ManagedResource<BufferedInputStream> urlInputStream(URL url) {
        return this.bufferedInputStream((Function0<InputStream>)(Function0 & Serializable & scala.Serializable)() -> url.openStream());
    }

    public ManagedResource<FileChannel> fileOuputChannel(File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new FileOutputStream((File)f).getChannel(), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(FileChannel.class));
    }

    public ManagedResource<FileChannel> fileInputChannel(File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new FileInputStream((File)f).getChannel(), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(FileChannel.class));
    }

    private Charset utf8() {
        return this.utf8;
    }

    public ManagedResource<BufferedWriter> fileWriter(Charset charset, boolean append, File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream((File)f, append), charset)), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(BufferedWriter.class));
    }

    public Charset fileWriter$default$1() {
        return this.utf8();
    }

    public boolean fileWriter$default$2() {
        return false;
    }

    public ManagedResource<BufferedReader> fileReader(Charset charset, File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new BufferedReader(new InputStreamReader((InputStream)new FileInputStream((File)f), charset)), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(BufferedReader.class));
    }

    public Traversable<String> fileLines(Charset charset, File source) {
        return this.fileReader(charset, source).map((Function1 & Serializable & scala.Serializable)reader -> this.makeBufferedReaderLineTraverser((BufferedReader)reader)).toTraversable(Predef$.MODULE$.$conforms());
    }

    public ManagedResource<BufferedReader> urlReader(Charset charset, URL u) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new BufferedReader(new InputStreamReader(u.openStream(), charset)), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(BufferedReader.class));
    }

    public ManagedResource<JarFile> jarFile(boolean verify, File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new JarFile((File)f, verify), source, Resource$.MODULE$.jarFileResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(JarFile.class));
    }

    public ManagedResource<ZipFile> zipFile(File source) {
        return this.file((Function1)(Function1 & Serializable & scala.Serializable)f -> new ZipFile((File)f), source, Resource$.MODULE$.closeableResource(), (OptManifest)ClassManifestFactory$.MODULE$.classType(ZipFile.class));
    }

    public ManagedResource<Reader> streamReader(InputStream in, Charset charset) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new InputStreamReader(in, charset), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(InputStreamReader.class));
    }

    public ManagedResource<GZIPInputStream> gzipInputStream(Function0<InputStream> in) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new GZIPInputStream((InputStream)in.apply(), 8192), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(GZIPInputStream.class));
    }

    public ManagedResource<ZipInputStream> zipInputStream(Function0<InputStream> in) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new ZipInputStream((InputStream)in.apply()), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(ZipInputStream.class));
    }

    public ManagedResource<ZipOutputStream> zipOutputStream(Function0<OutputStream> out) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new ZipOutputStream((OutputStream)out.apply()), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(ZipOutputStream.class));
    }

    public ManagedResource<GZIPOutputStream> gzipOutputStream(Function0<OutputStream> out) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new GZIPOutputStream((OutputStream)out.apply(), 8192), Resource$.MODULE$.gzipOuputStraemResource(), ClassManifestFactory$.MODULE$.classType(GZIPOutputStream.class));
    }

    public ManagedResource<JarOutputStream> jarOutputStream(Function0<OutputStream> out) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new JarOutputStream((OutputStream)out.apply()), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(JarOutputStream.class));
    }

    public ManagedResource<JarInputStream> jarInputStream(Function0<InputStream> in) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> new JarInputStream((InputStream)in.apply()), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(JarInputStream.class));
    }

    public ManagedResource<InputStream> zipEntry(ZipFile zip, ZipEntry entry) {
        return package$.MODULE$.managed((Function0 & Serializable & scala.Serializable)() -> zip.getInputStream(entry), Resource$.MODULE$.closeableResource(), ClassManifestFactory$.MODULE$.classType(InputStream.class));
    }

    private TraversableOnce<String> makeBufferedReaderLineTraverser(BufferedReader reader) {
        LazyRef traverser$module = new LazyRef();
        return this.traverser$1(reader, traverser$module);
    }

    private static final Object open$1(Function1 in$1, File source$1) {
        File parent = source$1.getParentFile();
        Object object = parent != null ? BoxesRunTime.boxToBoolean((boolean)parent.mkdirs()) : BoxedUnit.UNIT;
        return in$1.apply((Object)source$1);
    }

    private static final /* synthetic */ Using$traverser$2$ traverser$lzycompute$1(BufferedReader reader$1, LazyRef traverser$module$1) {
        Using$traverser$2$ using$traverser$2$;
        LazyRef lazyRef = traverser$module$1;
        synchronized (lazyRef) {
            using$traverser$2$ = traverser$module$1.initialized() ? (Using$traverser$2$)traverser$module$1.value() : (Using$traverser$2$)traverser$module$1.initialize((Object)new Using$traverser$2$(reader$1));
        }
        return using$traverser$2$;
    }

    private final Using$traverser$2$ traverser$1(BufferedReader reader$1, LazyRef traverser$module$1) {
        return traverser$module$1.initialized() ? (Using$traverser$2$)traverser$module$1.value() : Using$.traverser$lzycompute$1(reader$1, traverser$module$1);
    }

    private Using$() {
        MODULE$ = this;
        this.utf8 = Charset.forName("UTF-8");
    }
}

