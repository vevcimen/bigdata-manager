/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Product
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.reflect.ScalaSignature
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName$;
import io.pivotal.greenplum.spark.externaltable.SqlObjectNameUtils$;
import scala.Product;
import scala.Serializable;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u0005mh!B\u001f?\u0003CI\u0005\"\u0002,\u0001\t\u00039\u0006\"\u0002.\u0001\r\u0003Y\u0006\"B4\u0001\r\u0003AwABA}}!\u00051OB\u0003>}!\u0005\u0011\u000fC\u0003W\u000b\u0011\u0005!\u000fC\u0003u\u000b\u0011\u0005Q\u000fC\u0003}\u000b\u0011\u0005Q\u0010C\u0004\u0002v\u0015!I!a\u001e\t\u000f\u0005\u0005U\u0001\"\u0003\u0002\u0004\u001a)\u0001/\u0002\"\u0002\\\"A\u0011p\u0003BK\u0002\u0013\u00051\fC\u0005\u0002^.\u0011\t\u0012)A\u00059\"A1p\u0003BK\u0002\u0013\u00051\fC\u0005\u0002\u0004-\u0011\t\u0012)A\u00059\"1ak\u0003C\u0001\u0003?D\u0001BW\u0006\t\u0006\u0004%\te\u0017\u0005\tO.A)\u0019!C!Q\"I\u0011\u0011B\u0006\t\u0006\u0004%\te\u0017\u0005\n\u0003\u0017Y\u0011\u0011!C\u0001\u0003KD\u0011\"!\u0005\f#\u0003%\t!a\u0005\t\u0013\u0005-8\"%A\u0005\u0002\u0005M\u0001\"CA\u0015\u0017\u0005\u0005I\u0011IA\u0016\u0011%\tYdCA\u0001\n\u0003\ti\u0004C\u0005\u0002F-\t\t\u0011\"\u0001\u0002n\"I\u0011QJ\u0006\u0002\u0002\u0013\u0005\u0013q\n\u0005\n\u0003;Z\u0011\u0011!C\u0001\u0003cD\u0011\"!\u001b\f\u0003\u0003%\t%a\u001b\t\u0013\u000554\"!A\u0005B\u0005Ux!CAD\u000b\u0005\u0005\t\u0012AAE\r!\u0001X!!A\t\u0002\u0005-\u0005B\u0002, \t\u0003\tI\nC\u0005\u0002\n}\t\t\u0011\"\u0012\u0002\u001c\"I\u0011QT\u0010\u0002\u0002\u0013\u0005\u0015q\u0014\u0005\n\u0003K{\u0012\u0011!CA\u0003OC\u0011\"!/ \u0003\u0003%I!a/\u0007\u000b},!)!\u0001\t\u0011m,#Q3A\u0005\u0002mC\u0011\"a\u0001&\u0005#\u0005\u000b\u0011\u0002/\t\rY+C\u0011AA\u0003\u0011!QV\u0005#b\u0001\n\u0003Z\u0006\u0002C4&\u0011\u000b\u0007I\u0011\t5\t\u0013\u0005%Q\u0005#b\u0001\n\u0003Z\u0006\"CA\u0006K\u0005\u0005I\u0011AA\u0007\u0011%\t\t\"JI\u0001\n\u0003\t\u0019\u0002C\u0005\u0002*\u0015\n\t\u0011\"\u0011\u0002,!I\u00111H\u0013\u0002\u0002\u0013\u0005\u0011Q\b\u0005\n\u0003\u000b*\u0013\u0011!C\u0001\u0003\u000fB\u0011\"!\u0014&\u0003\u0003%\t%a\u0014\t\u0013\u0005uS%!A\u0005\u0002\u0005}\u0003\"CA5K\u0005\u0005I\u0011IA6\u0011%\ti'JA\u0001\n\u0003\nygB\u0005\u0002D\u0016\t\t\u0011#\u0001\u0002F\u001aAq0BA\u0001\u0012\u0003\t9\r\u0003\u0004Wm\u0011\u0005\u0011q\u001a\u0005\n\u0003\u00131\u0014\u0011!C#\u00037C\u0011\"!(7\u0003\u0003%\t)!5\t\u0013\u0005\u0015f'!A\u0005\u0002\u0006U\u0007\"CA]m\u0005\u0005I\u0011BA^\u0011%\tI,BA\u0001\n\u0013\tYL\u0001\fHe\u0016,g\u000e\u001d7v[F+\u0018\r\\5gS\u0016$g*Y7f\u0015\ty\u0004)A\u0007fqR,'O\\1mi\u0006\u0014G.\u001a\u0006\u0003\u0003\n\u000bQa\u001d9be.T!a\u0011#\u0002\u0013\u001d\u0014X-\u001a8qYVl'BA#G\u0003\u001d\u0001\u0018N^8uC2T\u0011aR\u0001\u0003S>\u001c\u0001a\u0005\u0003\u0001\u0015B\u001b\u0006CA&O\u001b\u0005a%\"A'\u0002\u000bM\u001c\u0017\r\\1\n\u0005=c%AB!osJ+g\r\u0005\u0002L#&\u0011!\u000b\u0014\u0002\b!J|G-^2u!\tYE+\u0003\u0002V\u0019\na1+\u001a:jC2L'0\u00192mK\u00061A(\u001b8jiz\"\u0012\u0001\u0017\t\u00033\u0002i\u0011AP\u0001\faJ,\u0007/\u0019:fIN\u000bF*F\u0001]!\tiFM\u0004\u0002_EB\u0011q\fT\u0007\u0002A*\u0011\u0011\rS\u0001\u0007yI|w\u000e\u001e \n\u0005\rd\u0015A\u0002)sK\u0012,g-\u0003\u0002fM\n11\u000b\u001e:j]\u001eT!a\u0019'\u0002\u0013E,XM]=Be\u001e\u001cX#A5\u0011\u0007-SG.\u0003\u0002l\u0019\n)\u0011I\u001d:bsB\u00111*\\\u0005\u0003]2\u00131!\u00118zS\r\u00011\"\n\u0002\u0006)\u0006\u0014G.Z\n\u0004\u000b)\u001bF#A:\u0011\u0005e+\u0011\u0001\u00034peR\u000b'\r\\3\u0015\u0007YD(\u0010\u0005\u0002x\u00175\tQ\u0001C\u0003z\u000f\u0001\u0007A,\u0001\u0004tG\",W.\u0019\u0005\u0006w\u001e\u0001\r\u0001X\u0001\u0005]\u0006lW-\u0001\u0007g_J$V-\u001c9UC\ndW\rF\u0002\u007f\u0003g\u0002\"a^\u0013\u0003\u0013Q+W\u000e\u001d+bE2,7\u0003B\u0013Y!N\u000bQA\\1nK\u0002\"2A`A\u0004\u0011\u0015Y\b\u00061\u0001]\u0003!!xn\u0015;sS:<\u0017\u0001B2paf$2A`A\b\u0011\u001dYH\u0006%AA\u0002q\u000babY8qs\u0012\"WMZ1vYR$\u0013'\u0006\u0002\u0002\u0016)\u001aA,a\u0006,\u0005\u0005e\u0001\u0003BA\u000e\u0003Ki!!!\b\u000b\t\u0005}\u0011\u0011E\u0001\nk:\u001c\u0007.Z2lK\u0012T1!a\tM\u0003)\tgN\\8uCRLwN\\\u0005\u0005\u0003O\tiBA\tv]\u000eDWmY6fIZ\u000b'/[1oG\u0016\fQ\u0002\u001d:pIV\u001cG\u000f\u0015:fM&DXCAA\u0017!\u0011\ty#!\u000f\u000e\u0005\u0005E\"\u0002BA\u001a\u0003k\tA\u0001\\1oO*\u0011\u0011qG\u0001\u0005U\u00064\u0018-C\u0002f\u0003c\tA\u0002\u001d:pIV\u001cG/\u0011:jif,\"!a\u0010\u0011\u0007-\u000b\t%C\u0002\u0002D1\u00131!\u00138u\u00039\u0001(o\u001c3vGR,E.Z7f]R$2\u0001\\A%\u0011%\tY\u0005MA\u0001\u0002\u0004\ty$A\u0002yIE\nq\u0002\u001d:pIV\u001cG/\u0013;fe\u0006$xN]\u000b\u0003\u0003#\u0002R!a\u0015\u0002Z1l!!!\u0016\u000b\u0007\u0005]C*\u0001\u0006d_2dWm\u0019;j_:LA!a\u0017\u0002V\tA\u0011\n^3sCR|'/\u0001\u0005dC:,\u0015/^1m)\u0011\t\t'a\u001a\u0011\u0007-\u000b\u0019'C\u0002\u0002f1\u0013qAQ8pY\u0016\fg\u000e\u0003\u0005\u0002LI\n\t\u00111\u0001m\u0003!A\u0017m\u001d5D_\u0012,GCAA \u0003\u0019)\u0017/^1mgR!\u0011\u0011MA9\u0011!\tY\u0005NA\u0001\u0002\u0004a\u0007\"B>\t\u0001\u0004a\u0016A\u0004<bY&$\u0017\r^3TG\",W.\u0019\u000b\u0005\u0003s\ny\bE\u0002L\u0003wJ1!! M\u0005\u0011)f.\u001b;\t\u000beL\u0001\u0019\u0001/\u0002\u0019Y\fG.\u001b3bi\u0016t\u0015-\\3\u0015\t\u0005e\u0014Q\u0011\u0005\u0006w*\u0001\r\u0001X\u0001\u0006)\u0006\u0014G.\u001a\t\u0003o~\u0019BaHAG'B9\u0011qRAK9r3XBAAI\u0015\r\t\u0019\nT\u0001\beVtG/[7f\u0013\u0011\t9*!%\u0003#\u0005\u00137\u000f\u001e:bGR4UO\\2uS>t'\u0007\u0006\u0002\u0002\nR\u0011\u0011QF\u0001\u0006CB\u0004H.\u001f\u000b\u0006m\u0006\u0005\u00161\u0015\u0005\u0006s\n\u0002\r\u0001\u0018\u0005\u0006w\n\u0002\r\u0001X\u0001\bk:\f\u0007\u000f\u001d7z)\u0011\tI+!.\u0011\u000b-\u000bY+a,\n\u0007\u00055FJ\u0001\u0004PaRLwN\u001c\t\u0006\u0017\u0006EF\fX\u0005\u0004\u0003gc%A\u0002+va2,'\u0007\u0003\u0005\u00028\u000e\n\t\u00111\u0001w\u0003\rAH\u0005M\u0001\fe\u0016\fGMU3t_24X\r\u0006\u0002\u0002>B!\u0011qFA`\u0013\u0011\t\t-!\r\u0003\r=\u0013'.Z2u\u0003%!V-\u001c9UC\ndW\r\u0005\u0002xmM!a'!3T!\u0019\ty)a3]}&!\u0011QZAI\u0005E\t%m\u001d;sC\u000e$h)\u001e8di&|g.\r\u000b\u0003\u0003\u000b$2A`Aj\u0011\u0015Y\u0018\b1\u0001])\u0011\t9.!7\u0011\t-\u000bY\u000b\u0018\u0005\t\u0003oS\u0014\u0011!a\u0001}N!1\u0002\u0017)T\u0003\u001d\u00198\r[3nC\u0002\"RA^Aq\u0003GDQ!\u001f\tA\u0002qCQa\u001f\tA\u0002q#RA^At\u0003SDq!\u001f\u000b\u0011\u0002\u0003\u0007A\fC\u0004|)A\u0005\t\u0019\u0001/\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%eQ\u0019A.a<\t\u0013\u0005-\u0013$!AA\u0002\u0005}B\u0003BA1\u0003gD\u0001\"a\u0013\u001c\u0003\u0003\u0005\r\u0001\u001c\u000b\u0005\u0003C\n9\u0010\u0003\u0005\u0002Lu\t\t\u00111\u0001m\u0003Y9%/Z3oa2,X.U;bY&4\u0017.\u001a3OC6,\u0007")
public abstract class GreenplumQualifiedName
implements Product,
Serializable {
    public static TempTable forTempTable(String string) {
        return GreenplumQualifiedName$.MODULE$.forTempTable(string);
    }

    public static Table forTable(String string, String string2) {
        return GreenplumQualifiedName$.MODULE$.forTable(string, string2);
    }

    public Iterator<Object> productIterator() {
        return Product.productIterator$((Product)this);
    }

    public String productPrefix() {
        return Product.productPrefix$((Product)this);
    }

    public abstract String preparedSQL();

    public abstract Object[] queryArgs();

    public GreenplumQualifiedName() {
        Product.$init$((Product)this);
    }

    public static final class Table
    extends GreenplumQualifiedName {
        private String preparedSQL;
        private Object[] queryArgs;
        private String toString;
        private final String schema;
        private final String name;
        private volatile byte bitmap$0;

        public String schema() {
            return this.schema;
        }

        public String name() {
            return this.name;
        }

        private String preparedSQL$lzycompute() {
            Table table = this;
            synchronized (table) {
                if ((byte)(this.bitmap$0 & 1) == 0) {
                    this.preparedSQL = "WHERE table_schema = ? AND table_name = ?";
                    this.bitmap$0 = (byte)(this.bitmap$0 | 1);
                }
            }
            return this.preparedSQL;
        }

        @Override
        public String preparedSQL() {
            if ((byte)(this.bitmap$0 & 1) == 0) {
                return this.preparedSQL$lzycompute();
            }
            return this.preparedSQL;
        }

        private Object[] queryArgs$lzycompute() {
            Table table = this;
            synchronized (table) {
                if ((byte)(this.bitmap$0 & 2) == 0) {
                    this.queryArgs = new Object[]{this.schema(), this.name()};
                    this.bitmap$0 = (byte)(this.bitmap$0 | 2);
                }
            }
            return this.queryArgs;
        }

        @Override
        public Object[] queryArgs() {
            if ((byte)(this.bitmap$0 & 2) == 0) {
                return this.queryArgs$lzycompute();
            }
            return this.queryArgs;
        }

        private String toString$lzycompute() {
            Table table = this;
            synchronized (table) {
                if ((byte)(this.bitmap$0 & 4) == 0) {
                    this.toString = new StringBuilder(1).append(SqlObjectNameUtils$.MODULE$.escape(this.schema())).append(".").append(SqlObjectNameUtils$.MODULE$.escape(this.name())).toString();
                    this.bitmap$0 = (byte)(this.bitmap$0 | 4);
                }
            }
            return this.toString;
        }

        public String toString() {
            if ((byte)(this.bitmap$0 & 4) == 0) {
                return this.toString$lzycompute();
            }
            return this.toString;
        }

        public Table copy(String schema, String name) {
            return new Table(schema, name);
        }

        public String copy$default$1() {
            return this.schema();
        }

        public String copy$default$2() {
            return this.name();
        }

        @Override
        public String productPrefix() {
            return "Table";
        }

        public int productArity() {
            return 2;
        }

        public Object productElement(int x$1) {
            int n = x$1;
            switch (n) {
                case 0: {
                    return this.schema();
                }
                case 1: {
                    return this.name();
                }
            }
            throw new IndexOutOfBoundsException(Integer.toString(x$1));
        }

        @Override
        public Iterator<Object> productIterator() {
            return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
        }

        public boolean canEqual(Object x$1) {
            return x$1 instanceof Table;
        }

        public int hashCode() {
            return ScalaRunTime$.MODULE$._hashCode((Product)this);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object x$1) {
            if (this == x$1) return true;
            Object object = x$1;
            if (!(object instanceof Table)) return false;
            boolean bl = true;
            if (!bl) return false;
            Table table = (Table)x$1;
            String string = this.schema();
            String string2 = table.schema();
            if (string == null) {
                if (string2 != null) {
                    return false;
                }
            } else if (!string.equals(string2)) return false;
            String string3 = this.name();
            String string4 = table.name();
            if (string3 == null) {
                if (string4 == null) return true;
                return false;
            } else {
                if (!string3.equals(string4)) return false;
                return true;
            }
        }

        public Table(String schema, String name) {
            this.schema = schema;
            this.name = name;
        }
    }

    public static final class TempTable
    extends GreenplumQualifiedName {
        private String preparedSQL;
        private Object[] queryArgs;
        private String toString;
        private final String name;
        private volatile byte bitmap$0;

        public String name() {
            return this.name;
        }

        private String preparedSQL$lzycompute() {
            TempTable tempTable = this;
            synchronized (tempTable) {
                if ((byte)(this.bitmap$0 & 1) == 0) {
                    this.preparedSQL = "WHERE table_name = ?";
                    this.bitmap$0 = (byte)(this.bitmap$0 | 1);
                }
            }
            return this.preparedSQL;
        }

        @Override
        public String preparedSQL() {
            if ((byte)(this.bitmap$0 & 1) == 0) {
                return this.preparedSQL$lzycompute();
            }
            return this.preparedSQL;
        }

        private Object[] queryArgs$lzycompute() {
            TempTable tempTable = this;
            synchronized (tempTable) {
                if ((byte)(this.bitmap$0 & 2) == 0) {
                    this.queryArgs = new Object[]{this.name()};
                    this.bitmap$0 = (byte)(this.bitmap$0 | 2);
                }
            }
            return this.queryArgs;
        }

        @Override
        public Object[] queryArgs() {
            if ((byte)(this.bitmap$0 & 2) == 0) {
                return this.queryArgs$lzycompute();
            }
            return this.queryArgs;
        }

        private String toString$lzycompute() {
            TempTable tempTable = this;
            synchronized (tempTable) {
                if ((byte)(this.bitmap$0 & 4) == 0) {
                    this.toString = String.valueOf(SqlObjectNameUtils$.MODULE$.escape(this.name()));
                    this.bitmap$0 = (byte)(this.bitmap$0 | 4);
                }
            }
            return this.toString;
        }

        public String toString() {
            if ((byte)(this.bitmap$0 & 4) == 0) {
                return this.toString$lzycompute();
            }
            return this.toString;
        }

        public TempTable copy(String name) {
            return new TempTable(name);
        }

        public String copy$default$1() {
            return this.name();
        }

        @Override
        public String productPrefix() {
            return "TempTable";
        }

        public int productArity() {
            return 1;
        }

        public Object productElement(int x$1) {
            int n = x$1;
            switch (n) {
                case 0: {
                    return this.name();
                }
            }
            throw new IndexOutOfBoundsException(Integer.toString(x$1));
        }

        @Override
        public Iterator<Object> productIterator() {
            return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
        }

        public boolean canEqual(Object x$1) {
            return x$1 instanceof TempTable;
        }

        public int hashCode() {
            return ScalaRunTime$.MODULE$._hashCode((Product)this);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object x$1) {
            if (this == x$1) return true;
            Object object = x$1;
            if (!(object instanceof TempTable)) return false;
            boolean bl = true;
            if (!bl) return false;
            TempTable tempTable = (TempTable)x$1;
            String string = this.name();
            String string2 = tempTable.name();
            if (string != null) {
                if (!string.equals(string2)) return false;
                return true;
            }
            if (string2 == null) return true;
            return false;
        }

        public TempTable(String name) {
            this.name = name;
        }
    }
}

