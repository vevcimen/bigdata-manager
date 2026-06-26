/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 *  scala.util.control.ControlThrowable
 */
package resource;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.PooledConnection;
import resource.MediumPriorityResourceImplicits$HttpURLConnectionResource$;
import resource.MediumPriorityResourceImplicits$gzipOuputStraemResource$;
import resource.MediumPriorityResourceImplicits$jarFileResource$;
import resource.Resource$;
import scala.reflect.ScalaSignature;
import scala.util.control.ControlThrowable;

@ScalaSignature(bytes="\u0006\u0001A3q!\u0001\u0002\u0011\u0002\u0007\u0005QA\u0001\u0005SKN|WO]2f\u0015\u0005\u0019\u0011\u0001\u0003:fg>,(oY3\u0004\u0001U\u0011aAG\n\u0003\u0001\u001d\u0001\"\u0001C\u0006\u000e\u0003%Q\u0011AC\u0001\u0006g\u000e\fG.Y\u0005\u0003\u0019%\u0011a!\u00118z%\u00164\u0007\"\u0002\b\u0001\t\u0003y\u0011A\u0002\u0013j]&$H\u0005F\u0001\u0011!\tA\u0011#\u0003\u0002\u0013\u0013\t!QK\\5u\u0011\u0015!\u0002\u0001\"\u0001\u0016\u0003\u0011y\u0007/\u001a8\u0015\u0005A1\u0002\"B\f\u0014\u0001\u0004A\u0012!\u0001:\u0011\u0005eQB\u0002\u0001\u0003\u00067\u0001\u0011\r\u0001\b\u0002\u0002%F\u0011Q\u0004\t\t\u0003\u0011yI!aH\u0005\u0003\u000f9{G\u000f[5oOB\u0011\u0001\"I\u0005\u0003E%\u00111!\u00118z\u0011\u0015!\u0003A\"\u0001&\u0003\u0015\u0019Gn\\:f)\t\u0001b\u0005C\u0003\u0018G\u0001\u0007\u0001\u0004C\u0003)\u0001\u0011\u0005\u0011&A\ndY>\u001cX-\u00114uKJ,\u0005pY3qi&|g\u000eF\u0002\u0011U-BQaF\u0014A\u0002aAQ\u0001L\u0014A\u00025\n\u0011\u0001\u001e\t\u0003]Yr!a\f\u001b\u000f\u0005A\u001aT\"A\u0019\u000b\u0005I\"\u0011A\u0002\u001fs_>$h(C\u0001\u000b\u0013\t)\u0014\"A\u0004qC\u000e\\\u0017mZ3\n\u0005]B$!\u0003+ie><\u0018M\u00197f\u0015\t)\u0014\u0002C\u0003;\u0001\u0011\u00051(\u0001\tjg\u001a\u000bG/\u00197Fq\u000e,\u0007\u000f^5p]R\u0011Ah\u0010\t\u0003\u0011uJ!AP\u0005\u0003\u000f\t{w\u000e\\3b]\")A&\u000fa\u0001[!)\u0011\t\u0001C\u0001\u0005\u0006\u0019\u0012n\u001d*fi\"\u0014xn\u001e8Fq\u000e,\u0007\u000f^5p]R\u0011Ah\u0011\u0005\u0006Y\u0001\u0003\r!L\u0004\u0006\u000b\nA\tAR\u0001\t%\u0016\u001cx.\u001e:dKB\u0011q\tS\u0007\u0002\u0005\u0019)\u0011A\u0001E\u0001\u0013N\u0019\u0001j\u0002&\u0011\u0005\u001d[\u0015B\u0001'\u0003\u0005}iU\rZ5v[B\u0013\u0018n\u001c:jif\u0014Vm]8ve\u000e,\u0017*\u001c9mS\u000eLGo\u001d\u0005\u0006\u001d\"#\taT\u0001\u0007y%t\u0017\u000e\u001e \u0015\u0003\u0019\u0003")
public interface Resource<R> {
    public static <A> Resource<A> reflectiveDisposableResource() {
        return Resource$.MODULE$.reflectiveDisposableResource();
    }

    public static <A> Resource<A> reflectiveCloseableResource() {
        return Resource$.MODULE$.reflectiveCloseableResource();
    }

    public static <A extends PooledConnection> Resource<A> pooledConnectionResource() {
        return Resource$.MODULE$.pooledConnectionResource();
    }

    public static <A extends ResultSet> Resource<A> resultSetResource() {
        return Resource$.MODULE$.resultSetResource();
    }

    public static <A extends Statement> Resource<A> statementResource() {
        return Resource$.MODULE$.statementResource();
    }

    public static <A extends Connection> Resource<A> connectionResource() {
        return Resource$.MODULE$.connectionResource();
    }

    public static <A extends Closeable> Resource<A> closeableResource() {
        return Resource$.MODULE$.closeableResource();
    }

    public static MediumPriorityResourceImplicits$HttpURLConnectionResource$ HttpURLConnectionResource() {
        return Resource$.MODULE$.HttpURLConnectionResource();
    }

    public static MediumPriorityResourceImplicits$jarFileResource$ jarFileResource() {
        return Resource$.MODULE$.jarFileResource();
    }

    public static MediumPriorityResourceImplicits$gzipOuputStraemResource$ gzipOuputStraemResource() {
        return Resource$.MODULE$.gzipOuputStraemResource();
    }

    public static /* synthetic */ void open$(Resource $this, Object r) {
        $this.open(r);
    }

    default public void open(R r) {
    }

    public void close(R var1);

    public static /* synthetic */ void closeAfterException$(Resource $this, Object r, Throwable t) {
        $this.closeAfterException(r, t);
    }

    default public void closeAfterException(R r, Throwable t) {
        this.close(r);
    }

    public static /* synthetic */ boolean isFatalException$(Resource $this, Throwable t) {
        return $this.isFatalException(t);
    }

    default public boolean isFatalException(Throwable t) {
        return t instanceof VirtualMachineError;
    }

    public static /* synthetic */ boolean isRethrownException$(Resource $this, Throwable t) {
        return $this.isRethrownException(t);
    }

    default public boolean isRethrownException(Throwable t) {
        Throwable throwable = t;
        boolean bl = throwable instanceof ControlThrowable ? true : throwable instanceof InterruptedException;
        return bl;
    }

    public static void $init$(Resource $this) {
    }
}

