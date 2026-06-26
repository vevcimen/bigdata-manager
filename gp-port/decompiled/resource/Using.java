/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.collection.Traversable
 *  scala.reflect.OptManifest
 *  scala.reflect.ScalaSignature
 */
package resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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
import resource.Using$;
import scala.Function0;
import scala.Function1;
import scala.collection.Traversable;
import scala.reflect.OptManifest;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001\tet!B\u0001\u0003\u0011\u0003)\u0011!B+tS:<'\"A\u0002\u0002\u0011I,7o\\;sG\u0016\u001c\u0001\u0001\u0005\u0002\u0007\u000f5\t!AB\u0003\t\u0005!\u0005\u0011BA\u0003Vg&twm\u0005\u0002\b\u0015A\u00111BD\u0007\u0002\u0019)\tQ\"A\u0003tG\u0006d\u0017-\u0003\u0002\u0010\u0019\t1\u0011I\\=SK\u001aDQ!E\u0004\u0005\u0002I\ta\u0001P5oSRtD#A\u0003\t\u000bQ9A\u0011A\u000b\u0002)\t,hMZ3sK\u0012|U\u000f\u001e9viN#(/Z1n)\t1\u0012\u0005E\u0002\u0007/eI!\u0001\u0007\u0002\u0003\u001f5\u000bg.Y4fIJ+7o\\;sG\u0016\u0004\"AG\u0010\u000e\u0003mQ!\u0001H\u000f\u0002\u0005%|'\"\u0001\u0010\u0002\t)\fg/Y\u0005\u0003Am\u0011ACQ;gM\u0016\u0014X\rZ(viB,Ho\u0015;sK\u0006l\u0007B\u0002\u0012\u0014\t\u0003\u00071%A\u0002pkR\u00042a\u0003\u0013'\u0013\t)CB\u0001\u0005=Eft\u0017-\\3?!\tQr%\u0003\u0002)7\taq*\u001e;qkR\u001cFO]3b[\")!f\u0002C\u0001W\u0005\u0019\"-\u001e4gKJ,G-\u00138qkR\u001cFO]3b[R\u0011A\u0006\r\t\u0004\r]i\u0003C\u0001\u000e/\u0013\ty3DA\nCk\u001a4WM]3e\u0013:\u0004X\u000f^*ue\u0016\fW\u000e\u0003\u00042S\u0011\u0005\rAM\u0001\u0003S:\u00042a\u0003\u00134!\tQB'\u0003\u000267\tY\u0011J\u001c9viN#(/Z1n\u0011\u00159t\u0001\"\u00019\u0003\u00111\u0017\u000e\\3\u0016\u0005ezDC\u0001\u001e`)\tY$\fF\u0002=\u00116\u00032AB\f>!\tqt\b\u0004\u0001\u0005\u000b\u00013$\u0019A!\u0003\u0003Q\u000b\"AQ#\u0011\u0005-\u0019\u0015B\u0001#\r\u0005\u001dqu\u000e\u001e5j]\u001e\u0004\"a\u0003$\n\u0005\u001dc!aA!os\"9\u0011JNA\u0001\u0002\bQ\u0015AC3wS\u0012,gnY3%cA\u0019aaS\u001f\n\u00051\u0013!\u0001\u0003*fg>,(oY3\t\u000f93\u0014\u0011!a\u0002\u001f\u0006QQM^5eK:\u001cW\r\n\u001a\u0011\u0007A;VH\u0004\u0002R+B\u0011!\u000bD\u0007\u0002'*\u0011A\u000bB\u0001\u0007yI|w\u000e\u001e \n\u0005Yc\u0011A\u0002)sK\u0012,g-\u0003\u0002Y3\nYq\n\u001d;NC:Lg-Z:u\u0015\t1F\u0002C\u0003\\m\u0001\u0007A,\u0001\u0004t_V\u00148-\u001a\t\u00035uK!AX\u000e\u0003\t\u0019KG.\u001a\u0005\u0006cY\u0002\r\u0001\u0019\t\u0005\u0017\u0005dV(\u0003\u0002c\u0019\tIa)\u001e8di&|g.\r\u0005\u0006I\u001e!\t!Z\u0001\u0010M&dW-\u00138qkR\u001cFO]3b[R\u0011AF\u001a\u0005\u00067\u000e\u0004\r\u0001\u0018\u0005\u0006Q\u001e!\t![\u0001\u0011M&dWmT;uaV$8\u000b\u001e:fC6$\"A\u00066\t\u000bm;\u0007\u0019\u0001/\t\u000b1<A\u0011A7\u0002\u001dU\u0014H.\u00138qkR\u001cFO]3b[R\u0011AF\u001c\u0005\u0006_.\u0004\r\u0001]\u0001\u0004kJd\u0007CA9u\u001b\u0005\u0011(BA:\u001e\u0003\rqW\r^\u0005\u0003kJ\u00141!\u0016*M\u0011\u00159x\u0001\"\u0001y\u0003A1\u0017\u000e\\3PkB,Ho\u00115b]:,G\u000eF\u0002z\u0003\u000b\u00012AB\f{!\rY\u0018\u0011A\u0007\u0002y*\u0011QP`\u0001\tG\"\fgN\\3mg*\u0011q0H\u0001\u0004]&|\u0017bAA\u0002y\nYa)\u001b7f\u0007\"\fgN\\3m\u0011\u0015Yf\u000f1\u0001]\u0011\u001d\tIa\u0002C\u0001\u0003\u0017\t\u0001CZ5mK&s\u0007/\u001e;DQ\u0006tg.\u001a7\u0015\u0007e\fi\u0001\u0003\u0004\\\u0003\u000f\u0001\r\u0001\u0018\u0005\n\u0003#9!\u0019!C\u0005\u0003'\tA!\u001e;gqU\u0011\u0011Q\u0003\t\u0005\u0003/\ti\"\u0004\u0002\u0002\u001a)\u0019\u00111\u0004@\u0002\u000f\rD\u0017M]:fi&!\u0011qDA\r\u0005\u001d\u0019\u0005.\u0019:tKRD\u0001\"a\t\bA\u0003%\u0011QC\u0001\u0006kR4\u0007\b\t\u0005\b\u0003O9A\u0011AA\u0015\u0003)1\u0017\u000e\\3Xe&$XM\u001d\u000b\u0007\u0003W\t9$!\u000f\u0015\t\u00055\u0012Q\u0007\t\u0005\r]\ty\u0003E\u0002\u001b\u0003cI1!a\r\u001c\u00059\u0011UO\u001a4fe\u0016$wK]5uKJDaaWA\u0013\u0001\u0004a\u0006BCA\u000e\u0003K\u0001\n\u00111\u0001\u0002\u0016!Q\u00111HA\u0013!\u0003\u0005\r!!\u0010\u0002\r\u0005\u0004\b/\u001a8e!\rY\u0011qH\u0005\u0004\u0003\u0003b!a\u0002\"p_2,\u0017M\u001c\u0005\b\u0003\u000b:A\u0011AA$\u0003)1\u0017\u000e\\3SK\u0006$WM\u001d\u000b\u0005\u0003\u0013\n)\u0006\u0006\u0003\u0002L\u0005M\u0003\u0003\u0002\u0004\u0018\u0003\u001b\u00022AGA(\u0013\r\t\tf\u0007\u0002\u000f\u0005V4g-\u001a:fIJ+\u0017\rZ3s\u0011\u0019Y\u00161\ta\u00019\"A\u00111DA\"\u0001\u0004\t)\u0002C\u0004\u0002Z\u001d!\t!a\u0017\u0002\u0013\u0019LG.\u001a'j]\u0016\u001cH\u0003BA/\u0003s\"B!a\u0018\u0002xA1\u0011\u0011MA6\u0003crA!a\u0019\u0002h9\u0019!+!\u001a\n\u00035I1!!\u001b\r\u0003\u001d\u0001\u0018mY6bO\u0016LA!!\u001c\u0002p\tYAK]1wKJ\u001c\u0018M\u00197f\u0015\r\tI\u0007\u0004\t\u0004!\u0006M\u0014bAA;3\n11\u000b\u001e:j]\u001eDaaWA,\u0001\u0004a\u0006\u0002CA\u000e\u0003/\u0002\r!!\u0006\t\u000f\u0005ut\u0001\"\u0001\u0002\u0000\u0005IQO\u001d7SK\u0006$WM\u001d\u000b\u0005\u0003\u0003\u000b9\t\u0006\u0003\u0002L\u0005\r\u0005bBAC\u0003w\u0002\r\u0001]\u0001\u0002k\"A\u00111DA>\u0001\u0004\t)\u0002C\u0004\u0002\f\u001e!\t!!$\u0002\u000f)\f'OR5mKR!\u0011qRAS)\u0011\t\t*a)\u0011\t\u00199\u00121\u0013\t\u0005\u0003+\u000by*\u0004\u0002\u0002\u0018*!\u0011\u0011TAN\u0003\rQ\u0017M\u001d\u0006\u0004\u0003;k\u0012\u0001B;uS2LA!!)\u0002\u0018\n9!*\u0019:GS2,\u0007BB.\u0002\n\u0002\u0007A\f\u0003\u0005\u0002(\u0006%\u0005\u0019AA\u001f\u0003\u00191XM]5gs\"9\u00111V\u0004\u0005\u0002\u00055\u0016a\u0002>ja\u001aKG.\u001a\u000b\u0005\u0003_\u000bi\f\u0005\u0003\u0007/\u0005E\u0006\u0003BAZ\u0003sk!!!.\u000b\t\u0005]\u00161T\u0001\u0004u&\u0004\u0018\u0002BA^\u0003k\u0013qAW5q\r&dW\r\u0003\u0004\\\u0003S\u0003\r\u0001\u0018\u0005\b\u0003\u0003<A\u0011AAb\u00031\u0019HO]3b[J+\u0017\rZ3s)\u0019\t)-!4\u0002PB!aaFAd!\rQ\u0012\u0011Z\u0005\u0004\u0003\u0017\\\"A\u0002*fC\u0012,'\u000f\u0003\u00042\u0003\u007f\u0003\ra\r\u0005\t\u00037\ty\f1\u0001\u0002\u0016!9\u00111[\u0004\u0005\u0002\u0005U\u0017aD4{SBLe\u000e];u'R\u0014X-Y7\u0015\t\u0005]\u0017q\u001c\t\u0005\r]\tI\u000e\u0005\u0003\u00024\u0006m\u0017\u0002BAo\u0003k\u0013qb\u0012.J!&s\u0007/\u001e;TiJ,\u0017-\u001c\u0005\bc\u0005EG\u00111\u00013\u0011\u001d\t\u0019o\u0002C\u0001\u0003K\faB_5q\u0013:\u0004X\u000f^*ue\u0016\fW\u000e\u0006\u0003\u0002h\u0006=\b\u0003\u0002\u0004\u0018\u0003S\u0004B!a-\u0002l&!\u0011Q^A[\u00059Q\u0016\u000e]%oaV$8\u000b\u001e:fC6Dq!MAq\t\u0003\u0007!\u0007C\u0004\u0002t\u001e!\t!!>\u0002\u001fiL\u0007oT;uaV$8\u000b\u001e:fC6$B!a>\u0002\u0000B!aaFA}!\u0011\t\u0019,a?\n\t\u0005u\u0018Q\u0017\u0002\u00105&\u0004x*\u001e;qkR\u001cFO]3b[\"9!%!=\u0005\u0002\u0004\u0019\u0003b\u0002B\u0002\u000f\u0011\u0005!QA\u0001\u0011OjL\u0007oT;uaV$8\u000b\u001e:fC6$BAa\u0002\u0003\u0010A!aa\u0006B\u0005!\u0011\t\u0019La\u0003\n\t\t5\u0011Q\u0017\u0002\u0011\u000fjK\u0005kT;uaV$8\u000b\u001e:fC6DqA\tB\u0001\t\u0003\u00071\u0005C\u0004\u0003\u0014\u001d!\tA!\u0006\u0002\u001f)\f'oT;uaV$8\u000b\u001e:fC6$BAa\u0006\u0003 A!aa\u0006B\r!\u0011\t)Ja\u0007\n\t\tu\u0011q\u0013\u0002\u0010\u0015\u0006\u0014x*\u001e;qkR\u001cFO]3b[\"9!E!\u0005\u0005\u0002\u0004\u0019\u0003b\u0002B\u0012\u000f\u0011\u0005!QE\u0001\u000fU\u0006\u0014\u0018J\u001c9viN#(/Z1n)\u0011\u00119Ca\f\u0011\t\u00199\"\u0011\u0006\t\u0005\u0003+\u0013Y#\u0003\u0003\u0003.\u0005]%A\u0004&be&s\u0007/\u001e;TiJ,\u0017-\u001c\u0005\bc\t\u0005B\u00111\u00013\u0011\u001d\u0011\u0019d\u0002C\u0001\u0005k\t\u0001B_5q\u000b:$(/\u001f\u000b\u0005\u0005o\u0011)\u0005\u0006\u0003\u0003:\tm\u0002c\u0001\u0004\u0018g!A!Q\bB\u0019\u0001\u0004\u0011y$A\u0003f]R\u0014\u0018\u0010\u0005\u0003\u00024\n\u0005\u0013\u0002\u0002B\"\u0003k\u0013\u0001BW5q\u000b:$(/\u001f\u0005\t\u0003o\u0013\t\u00041\u0001\u00022\"9!\u0011J\u0004\u0005\n\t-\u0013aH7bW\u0016\u0014UO\u001a4fe\u0016$'+Z1eKJd\u0015N\\3Ue\u00064XM]:feR!!Q\nB*!\u0019\t\tGa\u0014\u0002r%!!\u0011KA8\u0005=!&/\u0019<feN\f'\r\\3P]\u000e,\u0007\u0002\u0003B+\u0005\u000f\u0002\r!!\u0014\u0002\rI,\u0017\rZ3s\u0011%\u0011IfBI\u0001\n\u0003\u0011Y&\u0001\u000bgS2,wK]5uKJ$C-\u001a4bk2$H%M\u000b\u0003\u0005;RC!!\u0006\u0003`-\u0012!\u0011\r\t\u0005\u0005G\u0012i'\u0004\u0002\u0003f)!!q\rB5\u0003%)hn\u00195fG.,GMC\u0002\u0003l1\t!\"\u00198o_R\fG/[8o\u0013\u0011\u0011yG!\u001a\u0003#Ut7\r[3dW\u0016$g+\u0019:jC:\u001cW\rC\u0005\u0003t\u001d\t\n\u0011\"\u0001\u0003v\u0005!b-\u001b7f/JLG/\u001a:%I\u00164\u0017-\u001e7uII*\"Aa\u001e+\t\u0005u\"q\f")
public final class Using {
    public static boolean fileWriter$default$2() {
        return Using$.MODULE$.fileWriter$default$2();
    }

    public static Charset fileWriter$default$1() {
        return Using$.MODULE$.fileWriter$default$1();
    }

    public static ManagedResource<InputStream> zipEntry(ZipFile zipFile, ZipEntry zipEntry) {
        return Using$.MODULE$.zipEntry(zipFile, zipEntry);
    }

    public static ManagedResource<JarInputStream> jarInputStream(Function0<InputStream> function0) {
        return Using$.MODULE$.jarInputStream(function0);
    }

    public static ManagedResource<JarOutputStream> jarOutputStream(Function0<OutputStream> function0) {
        return Using$.MODULE$.jarOutputStream(function0);
    }

    public static ManagedResource<GZIPOutputStream> gzipOutputStream(Function0<OutputStream> function0) {
        return Using$.MODULE$.gzipOutputStream(function0);
    }

    public static ManagedResource<ZipOutputStream> zipOutputStream(Function0<OutputStream> function0) {
        return Using$.MODULE$.zipOutputStream(function0);
    }

    public static ManagedResource<ZipInputStream> zipInputStream(Function0<InputStream> function0) {
        return Using$.MODULE$.zipInputStream(function0);
    }

    public static ManagedResource<GZIPInputStream> gzipInputStream(Function0<InputStream> function0) {
        return Using$.MODULE$.gzipInputStream(function0);
    }

    public static ManagedResource<Reader> streamReader(InputStream inputStream, Charset charset) {
        return Using$.MODULE$.streamReader(inputStream, charset);
    }

    public static ManagedResource<ZipFile> zipFile(File file) {
        return Using$.MODULE$.zipFile(file);
    }

    public static ManagedResource<JarFile> jarFile(boolean bl, File file) {
        return Using$.MODULE$.jarFile(bl, file);
    }

    public static ManagedResource<BufferedReader> urlReader(Charset charset, URL uRL) {
        return Using$.MODULE$.urlReader(charset, uRL);
    }

    public static Traversable<String> fileLines(Charset charset, File file) {
        return Using$.MODULE$.fileLines(charset, file);
    }

    public static ManagedResource<BufferedReader> fileReader(Charset charset, File file) {
        return Using$.MODULE$.fileReader(charset, file);
    }

    public static ManagedResource<BufferedWriter> fileWriter(Charset charset, boolean bl, File file) {
        return Using$.MODULE$.fileWriter(charset, bl, file);
    }

    public static ManagedResource<FileChannel> fileInputChannel(File file) {
        return Using$.MODULE$.fileInputChannel(file);
    }

    public static ManagedResource<FileChannel> fileOuputChannel(File file) {
        return Using$.MODULE$.fileOuputChannel(file);
    }

    public static ManagedResource<BufferedInputStream> urlInputStream(URL uRL) {
        return Using$.MODULE$.urlInputStream(uRL);
    }

    public static ManagedResource<BufferedOutputStream> fileOutputStream(File file) {
        return Using$.MODULE$.fileOutputStream(file);
    }

    public static ManagedResource<BufferedInputStream> fileInputStream(File file) {
        return Using$.MODULE$.fileInputStream(file);
    }

    public static <T> ManagedResource<T> file(Function1<File, T> function1, File file, Resource<T> resource, OptManifest<T> optManifest) {
        return Using$.MODULE$.file(function1, file, resource, optManifest);
    }

    public static ManagedResource<BufferedInputStream> bufferedInputStream(Function0<InputStream> function0) {
        return Using$.MODULE$.bufferedInputStream(function0);
    }

    public static ManagedResource<BufferedOutputStream> bufferedOutputStream(Function0<OutputStream> function0) {
        return Using$.MODULE$.bufferedOutputStream(function0);
    }
}

