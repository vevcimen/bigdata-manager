/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.Tuple4
 *  scala.collection.Iterator
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 *  scala.runtime.Statics
 */
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey$;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.Tuple4;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;
import scala.runtime.Statics;

@ScalaSignature(bytes="\u0006\u0001\u00055u!B\u0011#\u0011\u0003ic!B\u0018#\u0011\u0003\u0001\u0004\"\u0002\u001e\u0002\t\u0003Y\u0004\"\u0002\u001f\u0002\t\u0003i\u0004\u0002CA/\u0003\t\u0007I\u0011\u0002#\t\u000f\u0005}\u0013\u0001)A\u0005\u000b\"A\u0011\u0011M\u0001\u0005\u0002\t\n\u0019\u0007\u0003\u0005=\u0003\u0005\u0005I\u0011QA5\u0011%\t\u0019(AA\u0001\n\u0003\u000b)\bC\u0005\u0002\u0004\u0006\t\t\u0011\"\u0003\u0002\u0006\u001a)qF\t!#\u007f!A1I\u0003BK\u0002\u0013\u0005A\t\u0003\u0005Q\u0015\tE\t\u0015!\u0003F\u0011!\t&B!f\u0001\n\u0003!\u0005\u0002\u0003*\u000b\u0005#\u0005\u000b\u0011B#\t\u0011MS!Q3A\u0005\u0002\u0011C\u0001\u0002\u0016\u0006\u0003\u0012\u0003\u0006I!\u0012\u0005\t+*\u0011)\u001a!C\u0001-\"A!L\u0003B\tB\u0003%q\u000bC\u0003;\u0015\u0011%1\fC\u0004a\u0015\u0005\u0005I\u0011A1\t\u000f\u0019T\u0011\u0013!C\u0001O\"9!OCI\u0001\n\u00039\u0007bB:\u000b#\u0003%\ta\u001a\u0005\bi*\t\n\u0011\"\u0001v\u0011\u001d9(\"!A\u0005BaD\u0001\"!\u0001\u000b\u0003\u0003%\tA\u0016\u0005\n\u0003\u0007Q\u0011\u0011!C\u0001\u0003\u000bA\u0011\"!\u0005\u000b\u0003\u0003%\t%a\u0005\t\u0013\u0005\u0005\"\"!A\u0005\u0002\u0005\r\u0002\"CA\u0017\u0015\u0005\u0005I\u0011IA\u0018\u0011%\t\tDCA\u0001\n\u0003\n\u0019\u0004C\u0005\u00026)\t\t\u0011\"\u0011\u00028\u0005i1i\u001c8oK\u000e$\u0018n\u001c8LKfT!a\t\u0013\u0002\t)$'m\u0019\u0006\u0003K\u0019\nQa\u001d9be.T!a\n\u0015\u0002\u0013\u001d\u0014X-\u001a8qYVl'BA\u0015+\u0003\u001d\u0001\u0018N^8uC2T\u0011aK\u0001\u0003S>\u001c\u0001\u0001\u0005\u0002/\u00035\t!EA\u0007D_:tWm\u0019;j_:\\U-_\n\u0004\u0003E:\u0004C\u0001\u001a6\u001b\u0005\u0019$\"\u0001\u001b\u0002\u000bM\u001c\u0017\r\\1\n\u0005Y\u001a$AB!osJ+g\r\u0005\u00023q%\u0011\u0011h\r\u0002\r'\u0016\u0014\u0018.\u00197ju\u0006\u0014G.Z\u0001\u0007y%t\u0017\u000e\u001e \u0015\u00035\nQ!\u00199qYf$\u0012BPA\u001e\u0003\u007f\t\u0019%!\u0014\u0011\u00059R1\u0003\u0002\u00062\u0001^\u0002\"AM!\n\u0005\t\u001b$a\u0002)s_\u0012,8\r^\u0001\bU\u0012\u00147-\u0016:m+\u0005)\u0005C\u0001$N\u001d\t95\n\u0005\u0002Ig5\t\u0011J\u0003\u0002KY\u00051AH]8pizJ!\u0001T\u001a\u0002\rA\u0013X\rZ3g\u0013\tquJ\u0001\u0004TiJLgn\u001a\u0006\u0003\u0019N\n\u0001B\u001b3cGV\u0013H\u000eI\u0001\tkN,'OT1nK\u0006IQo]3s\u001d\u0006lW\rI\u0001\u000fQ\u0006\u001c\b.\u001a3QCN\u001cxo\u001c:e\u0003=A\u0017m\u001d5fIB\u000b7o]<pe\u0012\u0004\u0013!G2p]:,7\r^5p]B{w\u000e\\(qi&|gn\u001d%bg\",\u0012a\u0016\t\u0003eaK!!W\u001a\u0003\u0007%sG/\u0001\u000ed_:tWm\u0019;j_:\u0004vn\u001c7PaRLwN\\:ICND\u0007\u0005F\u0003?9vsv\fC\u0003D'\u0001\u0007Q\tC\u0003R'\u0001\u0007Q\tC\u0003T'\u0001\u0007Q\tC\u0003V'\u0001\u0007q+\u0001\u0003d_BLH#\u0002 cG\u0012,\u0007bB\"\u0015!\u0003\u0005\r!\u0012\u0005\b#R\u0001\n\u00111\u0001F\u0011\u001d\u0019F\u0003%AA\u0002\u0015Cq!\u0016\u000b\u0011\u0002\u0003\u0007q+\u0001\bd_BLH\u0005Z3gCVdG\u000fJ\u0019\u0016\u0003!T#!R5,\u0003)\u0004\"a\u001b9\u000e\u00031T!!\u001c8\u0002\u0013Ut7\r[3dW\u0016$'BA84\u0003)\tgN\\8uCRLwN\\\u0005\u0003c2\u0014\u0011#\u001e8dQ\u0016\u001c7.\u001a3WCJL\u0017M\\2f\u00039\u0019w\u000e]=%I\u00164\u0017-\u001e7uII\nabY8qs\u0012\"WMZ1vYR$3'\u0001\bd_BLH\u0005Z3gCVdG\u000f\n\u001b\u0016\u0003YT#aV5\u0002\u001bA\u0014x\u000eZ;diB\u0013XMZ5y+\u0005I\bC\u0001>\u0000\u001b\u0005Y(B\u0001?~\u0003\u0011a\u0017M\\4\u000b\u0003y\fAA[1wC&\u0011aj_\u0001\raJ|G-^2u\u0003JLG/_\u0001\u000faJ|G-^2u\u000b2,W.\u001a8u)\u0011\t9!!\u0004\u0011\u0007I\nI!C\u0002\u0002\fM\u00121!\u00118z\u0011!\tyaGA\u0001\u0002\u00049\u0016a\u0001=%c\u0005y\u0001O]8ek\u000e$\u0018\n^3sCR|'/\u0006\u0002\u0002\u0016A1\u0011qCA\u000f\u0003\u000fi!!!\u0007\u000b\u0007\u0005m1'\u0001\u0006d_2dWm\u0019;j_:LA!a\b\u0002\u001a\tA\u0011\n^3sCR|'/\u0001\u0005dC:,\u0015/^1m)\u0011\t)#a\u000b\u0011\u0007I\n9#C\u0002\u0002*M\u0012qAQ8pY\u0016\fg\u000eC\u0005\u0002\u0010u\t\t\u00111\u0001\u0002\b\u0005A\u0001.Y:i\u0007>$W\rF\u0001X\u0003!!xn\u0015;sS:<G#A=\u0002\r\u0015\fX/\u00197t)\u0011\t)#!\u000f\t\u0013\u0005=\u0001%!AA\u0002\u0005\u001d\u0001BBA\u001f\u0007\u0001\u0007Q)A\u0002ve2Da!!\u0011\u0004\u0001\u0004)\u0015\u0001B;tKJDq!!\u0012\u0004\u0001\u0004\t9%\u0001\u0005qCN\u001cxo\u001c:e!\u0011\u0011\u0014\u0011J#\n\u0007\u0005-3G\u0001\u0004PaRLwN\u001c\u0005\b\u0003\u001f\u001a\u0001\u0019AA)\u0003U\u0019wN\u001c8fGRLwN\u001c)p_2|\u0005\u000f^5p]N\u0004B!a\u0015\u0002Z5\u0011\u0011Q\u000b\u0006\u0004\u0003/\"\u0013\u0001B2p]\u001aLA!a\u0017\u0002V\t)2i\u001c8oK\u000e$\u0018n\u001c8Q_>dw\n\u001d;j_:\u001c\u0018\u0001B:bYR\fQa]1mi\u0002\nA\u0002[1tQB\u000b7o]<pe\u0012$2!RA3\u0011\u0019\t9G\u0002a\u0001\u000b\u0006!\u0001/Y:t)%q\u00141NA7\u0003_\n\t\bC\u0003D\u000f\u0001\u0007Q\tC\u0003R\u000f\u0001\u0007Q\tC\u0003T\u000f\u0001\u0007Q\tC\u0003V\u000f\u0001\u0007q+A\u0004v]\u0006\u0004\b\u000f\\=\u0015\t\u0005]\u0014q\u0010\t\u0006e\u0005%\u0013\u0011\u0010\t\be\u0005mT)R#X\u0013\r\tih\r\u0002\u0007)V\u0004H.\u001a\u001b\t\u0011\u0005\u0005\u0005\"!AA\u0002y\n1\u0001\u001f\u00131\u0003-\u0011X-\u00193SKN|GN^3\u0015\u0005\u0005\u001d\u0005c\u0001>\u0002\n&\u0019\u00111R>\u0003\r=\u0013'.Z2u\u0001")
public class ConnectionKey
implements Product,
Serializable {
    private final String jdbcUrl;
    private final String userName;
    private final String hashedPassword;
    private final int connectionPoolOptionsHash;

    public static Option<Tuple4<String, String, String, Object>> unapply(ConnectionKey connectionKey) {
        return ConnectionKey$.MODULE$.unapply(connectionKey);
    }

    public static ConnectionKey apply(String string, String string2, String string3, int n) {
        return ConnectionKey$.MODULE$.apply(string, string2, string3, n);
    }

    public static ConnectionKey apply(String string, String string2, Option<String> option, ConnectionPoolOptions connectionPoolOptions) {
        return ConnectionKey$.MODULE$.apply(string, string2, option, connectionPoolOptions);
    }

    public String jdbcUrl() {
        return this.jdbcUrl;
    }

    public String userName() {
        return this.userName;
    }

    public String hashedPassword() {
        return this.hashedPassword;
    }

    public int connectionPoolOptionsHash() {
        return this.connectionPoolOptionsHash;
    }

    public ConnectionKey copy(String jdbcUrl, String userName, String hashedPassword, int connectionPoolOptionsHash) {
        return new ConnectionKey(jdbcUrl, userName, hashedPassword, connectionPoolOptionsHash);
    }

    public String copy$default$1() {
        return this.jdbcUrl();
    }

    public String copy$default$2() {
        return this.userName();
    }

    public String copy$default$3() {
        return this.hashedPassword();
    }

    public int copy$default$4() {
        return this.connectionPoolOptionsHash();
    }

    public String productPrefix() {
        return "ConnectionKey";
    }

    public int productArity() {
        return 4;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.jdbcUrl();
            }
            case 1: {
                return this.userName();
            }
            case 2: {
                return this.hashedPassword();
            }
            case 3: {
                return BoxesRunTime.boxToInteger((int)this.connectionPoolOptionsHash());
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof ConnectionKey;
    }

    public int hashCode() {
        int n = -889275714;
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.jdbcUrl()));
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.userName()));
        n = Statics.mix((int)n, (int)Statics.anyHash((Object)this.hashedPassword()));
        n = Statics.mix((int)n, (int)this.connectionPoolOptionsHash());
        return Statics.finalizeHash((int)n, (int)4);
    }

    public String toString() {
        return ScalaRunTime$.MODULE$._toString((Product)this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object x$1) {
        if (this == x$1) return true;
        Object object = x$1;
        if (!(object instanceof ConnectionKey)) return false;
        boolean bl = true;
        if (!bl) return false;
        ConnectionKey connectionKey = (ConnectionKey)x$1;
        String string = this.jdbcUrl();
        String string2 = connectionKey.jdbcUrl();
        if (string == null) {
            if (string2 != null) {
                return false;
            }
        } else if (!string.equals(string2)) return false;
        String string3 = this.userName();
        String string4 = connectionKey.userName();
        if (string3 == null) {
            if (string4 != null) {
                return false;
            }
        } else if (!string3.equals(string4)) return false;
        String string5 = this.hashedPassword();
        String string6 = connectionKey.hashedPassword();
        if (string5 == null) {
            if (string6 != null) {
                return false;
            }
        } else if (!string5.equals(string6)) return false;
        if (this.connectionPoolOptionsHash() != connectionKey.connectionPoolOptionsHash()) return false;
        if (!connectionKey.canEqual(this)) return false;
        return true;
    }

    public ConnectionKey(String jdbcUrl, String userName, String hashedPassword, int connectionPoolOptionsHash) {
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.connectionPoolOptionsHash = connectionPoolOptionsHash;
        Product.$init$((Product)this);
    }
}

