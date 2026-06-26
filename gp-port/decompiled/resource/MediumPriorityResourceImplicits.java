/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package resource;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.PooledConnection;
import resource.LowPriorityResourceImplicits;
import resource.MediumPriorityResourceImplicits$HttpURLConnectionResource$;
import resource.MediumPriorityResourceImplicits$gzipOuputStraemResource$;
import resource.MediumPriorityResourceImplicits$jarFileResource$;
import resource.Resource;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001\u00055daB\u0001\u0003!\u0003\r\t#\u0002\u0002 \u001b\u0016$\u0017.^7Qe&|'/\u001b;z%\u0016\u001cx.\u001e:dK&k\u0007\u000f\\5dSR\u001c(\"A\u0002\u0002\u0011I,7o\\;sG\u0016\u001c\u0001aE\u0002\u0001\r1\u0001\"a\u0002\u0006\u000e\u0003!Q\u0011!C\u0001\u0006g\u000e\fG.Y\u0005\u0003\u0017!\u0011a!\u00118z%\u00164\u0007CA\u0007\u000f\u001b\u0005\u0011\u0011BA\b\u0003\u0005qaun\u001e)sS>\u0014\u0018\u000e^=SKN|WO]2f\u00136\u0004H.[2jiNDQ!\u0005\u0001\u0005\u0002I\ta\u0001J5oSR$C#A\n\u0011\u0005\u001d!\u0012BA\u000b\t\u0005\u0011)f.\u001b;\t\u000b]\u0001A1\u0001\r\u0002#\rdwn]3bE2,'+Z:pkJ\u001cW-\u0006\u0002\u001aEU\t!DE\u0002\u001c\ru1A\u0001\b\f\u00015\taAH]3gS:,W.\u001a8u}A\u0019QB\b\u0011\n\u0005}\u0011!\u0001\u0003*fg>,(oY3\u0011\u0005\u0005\u0012C\u0002\u0001\u0003\u0006GY\u0011\r\u0001\n\u0002\u0002\u0003F\u0011Q\u0005\u000b\t\u0003\u000f\u0019J!a\n\u0005\u0003\u000f9{G\u000f[5oOB\u0011\u0011FL\u0007\u0002U)\u00111\u0006L\u0001\u0003S>T\u0011!L\u0001\u0005U\u00064\u0018-\u0003\u00020U\tI1\t\\8tK\u0006\u0014G.\u001a\u0005\u0006c\u0001!\u0019AM\u0001\u0013G>tg.Z2uS>t'+Z:pkJ\u001cW-\u0006\u00024qU\tAGE\u00026\rY2A\u0001\b\u0019\u0001iA\u0019QBH\u001c\u0011\u0005\u0005BD!B\u00121\u0005\u0004I\u0014CA\u0013;!\tYd(D\u0001=\u0015\tiD&A\u0002tc2L!a\u0010\u001f\u0003\u0015\r{gN\\3di&|g\u000eC\u0003B\u0001\u0011\r!)A\tti\u0006$X-\\3oiJ+7o\\;sG\u0016,\"a\u0011%\u0016\u0003\u0011\u00132!\u0012\u0004G\r\u0011a\u0002\t\u0001#\u0011\u00075qr\t\u0005\u0002\"\u0011\u0012)1\u0005\u0011b\u0001\u0013F\u0011QE\u0013\t\u0003w-K!\u0001\u0014\u001f\u0003\u0013M#\u0018\r^3nK:$\b\"\u0002(\u0001\t\u0007y\u0015!\u0005:fgVdGoU3u%\u0016\u001cx.\u001e:dKV\u0011\u0001+V\u000b\u0002#J\u0019!KB*\u0007\tqi\u0005!\u0015\t\u0004\u001by!\u0006CA\u0011V\t\u0015\u0019SJ1\u0001W#\t)s\u000b\u0005\u0002<1&\u0011\u0011\f\u0010\u0002\n%\u0016\u001cX\u000f\u001c;TKRDQa\u0017\u0001\u0005\u0004q\u000b\u0001\u0004]8pY\u0016$7i\u001c8oK\u000e$\u0018n\u001c8SKN|WO]2f+\ti&-F\u0001_%\ryf\u0001\u0019\u0004\u00059i\u0003a\fE\u0002\u000e=\u0005\u0004\"!\t2\u0005\u000b\rR&\u0019A2\u0012\u0005\u0015\"\u0007CA3j\u001b\u00051'BA\u001fh\u0015\u0005A\u0017!\u00026bm\u0006D\u0018B\u00016g\u0005A\u0001vn\u001c7fI\u000e{gN\\3di&|gnB\u0003m\u0001!\rQ.A\fhu&\u0004x*\u001e9viN#(/Y3n%\u0016\u001cx.\u001e:dKB\u0011an\\\u0007\u0002\u0001\u0019)\u0001\u000f\u0001E\u0001c\n9rM_5q\u001fV\u0004X\u000f^*ue\u0006,WNU3t_V\u00148-Z\n\u0004_\u001a\u0011\bcA\u0007\u001fgB\u0011A/_\u0007\u0002k*\u0011ao^\u0001\u0004u&\u0004(B\u0001=-\u0003\u0011)H/\u001b7\n\u0005i,(\u0001E$[\u0013B{U\u000f\u001e9viN#(/Z1n\u0011\u0015ax\u000e\"\u0001~\u0003\u0019a\u0014N\\5u}Q\tQ\u000e\u0003\u0004\u0000_\u0012\u0005\u0013\u0011A\u0001\u0006G2|7/\u001a\u000b\u0004'\u0005\r\u0001BBA\u0003}\u0002\u00071/A\u0001s\u0011\u001d\tIa\u001cC!\u0003\u0017\t\u0001\u0002^8TiJLgn\u001a\u000b\u0003\u0003\u001b\u0001B!a\u0004\u0002\u00165\u0011\u0011\u0011\u0003\u0006\u0004\u0003'a\u0013\u0001\u00027b]\u001eLA!a\u0006\u0002\u0012\t11\u000b\u001e:j]\u001e<q!a\u0007\u0001\u0011\u0007\ti\"A\bkCJ4\u0015\u000e\\3SKN|WO]2f!\rq\u0017q\u0004\u0004\b\u0003C\u0001\u0001\u0012AA\u0012\u0005=Q\u0017M\u001d$jY\u0016\u0014Vm]8ve\u000e,7#BA\u0010\r\u0005\u0015\u0002\u0003B\u0007\u001f\u0003O\u0001B!!\u000b\u000205\u0011\u00111\u0006\u0006\u0004\u0003[9\u0018a\u00016be&!\u0011\u0011GA\u0016\u0005\u001dQ\u0015M\u001d$jY\u0016Dq\u0001`A\u0010\t\u0003\t)\u0004\u0006\u0002\u0002\u001e!9q0a\b\u0005B\u0005eBcA\n\u0002<!A\u0011QAA\u001c\u0001\u0004\t9\u0003\u0003\u0005\u0002\n\u0005}A\u0011IA\u0006\u000f\u001d\t\t\u0005\u0001E\u0002\u0003\u0007\n\u0011\u0004\u0013;uaV\u0013FjQ8o]\u0016\u001cG/[8o%\u0016\u001cx.\u001e:dKB\u0019a.!\u0012\u0007\u000f\u0005\u001d\u0003\u0001#\u0001\u0002J\tI\u0002\n\u001e;q+Jc5i\u001c8oK\u000e$\u0018n\u001c8SKN|WO]2f'\u0015\t)EBA&!\u0011ia$!\u0014\u0011\t\u0005=\u0013QK\u0007\u0003\u0003#R1!a\u0015-\u0003\rqW\r^\u0005\u0005\u0003/\n\tFA\tIiR\u0004XK\u0015'D_:tWm\u0019;j_:Dq\u0001`A#\t\u0003\tY\u0006\u0006\u0002\u0002D!9q0!\u0012\u0005B\u0005}CcA\n\u0002b!A\u00111MA/\u0001\u0004\ti%A\u0001d\u0011!\tI!!\u0012\u0005B\u0005-\u0011f\u0001\u0001\u0002j)\u0019\u00111\u000e\u0002\u0002\u0011I+7o\\;sG\u0016\u0004")
public interface MediumPriorityResourceImplicits
extends LowPriorityResourceImplicits {
    public MediumPriorityResourceImplicits$gzipOuputStraemResource$ gzipOuputStraemResource();

    public MediumPriorityResourceImplicits$jarFileResource$ jarFileResource();

    public MediumPriorityResourceImplicits$HttpURLConnectionResource$ HttpURLConnectionResource();

    public static /* synthetic */ Resource closeableResource$(MediumPriorityResourceImplicits $this) {
        return $this.closeableResource();
    }

    default public <A extends Closeable> Resource<A> closeableResource() {
        return new Resource<A>(null){

            public void open(Object r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(Object r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                r.close();
            }

            public String toString() {
                return "Resource[java.io.Closeable]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static /* synthetic */ Resource connectionResource$(MediumPriorityResourceImplicits $this) {
        return $this.connectionResource();
    }

    default public <A extends Connection> Resource<A> connectionResource() {
        return new Resource<A>(null){

            public void open(Object r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(Object r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                r.close();
            }

            public String toString() {
                return "Resource[java.sql.Connection]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static /* synthetic */ Resource statementResource$(MediumPriorityResourceImplicits $this) {
        return $this.statementResource();
    }

    default public <A extends Statement> Resource<A> statementResource() {
        return new Resource<A>(null){

            public void open(Object r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(Object r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                r.close();
            }

            public String toString() {
                return "Resource[java.sql.Statement]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static /* synthetic */ Resource resultSetResource$(MediumPriorityResourceImplicits $this) {
        return $this.resultSetResource();
    }

    default public <A extends ResultSet> Resource<A> resultSetResource() {
        return new Resource<A>(null){

            public void open(Object r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(Object r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                r.close();
            }

            public String toString() {
                return "Resource[java.sql.ResultSet]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static /* synthetic */ Resource pooledConnectionResource$(MediumPriorityResourceImplicits $this) {
        return $this.pooledConnectionResource();
    }

    default public <A extends PooledConnection> Resource<A> pooledConnectionResource() {
        return new Resource<A>(null){

            public void open(Object r) {
                Resource.open$(this, r);
            }

            public void closeAfterException(Object r, Throwable t) {
                Resource.closeAfterException$(this, r, t);
            }

            public boolean isFatalException(Throwable t) {
                return Resource.isFatalException$(this, t);
            }

            public boolean isRethrownException(Throwable t) {
                return Resource.isRethrownException$(this, t);
            }

            public void close(A r) {
                r.close();
            }

            public String toString() {
                return "Resource[javax.sql.PooledConnection]";
            }
            {
                Resource.$init$(this);
            }
        };
    }

    public static void $init$(MediumPriorityResourceImplicits $this) {
    }
}

