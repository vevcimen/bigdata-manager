/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.rdd.RDD
 *  org.apache.spark.sql.Row
 *  org.apache.spark.sql.SQLContext
 *  org.apache.spark.sql.sources.BaseRelation
 *  org.apache.spark.sql.sources.Filter
 *  org.apache.spark.sql.sources.PrunedFilteredScan
 *  org.apache.spark.sql.types.StructField
 *  org.apache.spark.sql.types.StructType
 *  scala.Array$
 *  scala.Function1
 *  scala.Option
 *  scala.Predef$
 *  scala.Product
 *  scala.Serializable
 *  scala.Tuple3
 *  scala.collection.Iterator
 *  scala.collection.Seq
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ClassTag$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.GreenplumRDD;
import io.pivotal.greenplum.spark.GreenplumRDD$;
import io.pivotal.greenplum.spark.GreenplumRelation$;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import java.io.Serializable;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.sources.PrunedFilteredScan;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Array$;
import scala.Function1;
import scala.Option;
import scala.Predef$;
import scala.Product;
import scala.Tuple3;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ClassTag$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;

@ScalaSignature(bytes="\u0006\u0001\u0005}h!\u0002\u0012$\u0001\u000eZ\u0003\u0002C#\u0001\u0005+\u0007I\u0011A$\t\u00119\u0003!\u0011#Q\u0001\n!C\u0001b\u0014\u0001\u0003\u0016\u0004%\t\u0001\u0015\u0005\t1\u0002\u0011\t\u0012)A\u0005#\"A\u0011\f\u0001BK\u0002\u0013\u0005!\f\u0003\u0005b\u0001\tE\t\u0015!\u0003\\\u0011!\u0011\u0007A!b\u0001\n\u0003\u0019\u0007\u0002\u00035\u0001\u0005\u0003\u0005\u000b\u0011\u00023\t\u000b5\u0004A\u0011\u00018\t\u000fU\u0004!\u0019!C!m\"1!\u0010\u0001Q\u0001\n]DQa\u001f\u0001\u0005\nqDq!!\u0007\u0001\t\u0003\nY\u0002C\u0004\u0002*\u0001!\t%a\u000b\t\u0013\u0005\u0015\u0003!!A\u0005\u0002\u0005\u001d\u0003\"CA*\u0001E\u0005I\u0011AA+\u0011%\tY\u0007AI\u0001\n\u0003\ti\u0007C\u0005\u0002r\u0001\t\n\u0011\"\u0001\u0002t!I\u0011q\u000f\u0001\u0002\u0002\u0013\u0005\u0013\u0011\u0010\u0005\n\u0003\u0013\u0003\u0011\u0011!C\u0001\u0003\u0017C\u0011\"a%\u0001\u0003\u0003%\t!!&\t\u0013\u0005\u0005\u0006!!A\u0005B\u0005\r\u0006\"CAY\u0001\u0005\u0005I\u0011AAZ\u0011%\t9\fAA\u0001\n\u0003\nI\fC\u0005\u0002<\u0002\t\t\u0011\"\u0011\u0002>\"I\u0011q\u0018\u0001\u0002\u0002\u0013\u0005\u0013\u0011Y\u0004\u000b\u0003\u000b\u001c\u0013\u0011!E\u0001G\u0005\u001dg!\u0003\u0012$\u0003\u0003E\taIAe\u0011\u0019iG\u0004\"\u0001\u0002R\"I\u00111\u0018\u000f\u0002\u0002\u0013\u0015\u0013Q\u0018\u0005\n\u0003'd\u0012\u0011!CA\u0003+D\u0011\"!9\u001d\u0003\u0003%\t)a9\t\u0013\u0005UH$!A\u0005\n\u0005](!E$sK\u0016t\u0007\u000f\\;n%\u0016d\u0017\r^5p]*\u0011A%J\u0001\u0006gB\f'o\u001b\u0006\u0003M\u001d\n\u0011b\u001a:fK:\u0004H.^7\u000b\u0005!J\u0013a\u00029jm>$\u0018\r\u001c\u0006\u0002U\u0005\u0011\u0011n\\\n\u0006\u00011JDH\u0011\t\u0003[]j\u0011A\f\u0006\u0003_A\nqa]8ve\u000e,7O\u0003\u00022e\u0005\u00191/\u001d7\u000b\u0005\u0011\u001a$B\u0001\u001b6\u0003\u0019\t\u0007/Y2iK*\ta'A\u0002pe\u001eL!\u0001\u000f\u0018\u0003\u0019\t\u000b7/\u001a*fY\u0006$\u0018n\u001c8\u0011\u00055R\u0014BA\u001e/\u0005I\u0001&/\u001e8fI\u001aKG\u000e^3sK\u0012\u001c6-\u00198\u0011\u0005u\u0002U\"\u0001 \u000b\u0003}\nQa]2bY\u0006L!!\u0011 \u0003\u000fA\u0013x\u000eZ;diB\u0011QhQ\u0005\u0003\tz\u0012AbU3sS\u0006d\u0017N_1cY\u0016\faa]2iK6\f7\u0001A\u000b\u0002\u0011B\u0011\u0011\nT\u0007\u0002\u0015*\u00111\nM\u0001\u0006if\u0004Xm]\u0005\u0003\u001b*\u0013!b\u0015;sk\u000e$H+\u001f9f\u0003\u001d\u00198\r[3nC\u0002\nQ\u0001]1siN,\u0012!\u0015\t\u0004{I#\u0016BA*?\u0005\u0015\t%O]1z!\t)f+D\u0001$\u0013\t96E\u0001\nHe\u0016,g\u000e\u001d7v[B\u000b'\u000f^5uS>t\u0017A\u00029beR\u001c\b%\u0001\the\u0016,g\u000e\u001d7v[>\u0003H/[8ogV\t1\f\u0005\u0002]?6\tQL\u0003\u0002_G\u0005!1m\u001c8g\u0013\t\u0001WL\u0001\tHe\u0016,g\u000e\u001d7v[>\u0003H/[8og\u0006\trM]3f]BdW/\\(qi&|gn\u001d\u0011\u0002\u0015M\fHnQ8oi\u0016DH/F\u0001e!\t)g-D\u00011\u0013\t9\u0007G\u0001\u0006T#2\u001buN\u001c;fqR\f1b]9m\u0007>tG/\u001a=uA!\u0012\u0001B\u001b\t\u0003{-L!\u0001\u001c \u0003\u0013Q\u0014\u0018M\\:jK:$\u0018A\u0002\u001fj]&$h\b\u0006\u0003peN$HC\u00019r!\t)\u0006\u0001C\u0003c\u0013\u0001\u0007A\rC\u0003F\u0013\u0001\u0007\u0001\nC\u0003P\u0013\u0001\u0007\u0011\u000bC\u0003Z\u0013\u0001\u00071,\u0001\boK\u0016$7i\u001c8wKJ\u001c\u0018n\u001c8\u0016\u0003]\u0004\"!\u0010=\n\u0005et$a\u0002\"p_2,\u0017M\\\u0001\u0010]\u0016,GmQ8om\u0016\u00148/[8oA\u00051\u0002O]8kK\u000e$8k\u00195f[\u0006$vnQ8mk6t7\u000fF\u0002I{zDQ!\u0012\u0007A\u0002!Caa \u0007A\u0002\u0005\u0005\u0011aB2pYVlgn\u001d\t\u0005{I\u000b\u0019\u0001\u0005\u0003\u0002\u0006\u0005Ma\u0002BA\u0004\u0003\u001f\u00012!!\u0003?\u001b\t\tYAC\u0002\u0002\u000e\u0019\u000ba\u0001\u0010:p_Rt\u0014bAA\t}\u00051\u0001K]3eK\u001aLA!!\u0006\u0002\u0018\t11\u000b\u001e:j]\u001eT1!!\u0005?\u0003A)h\u000e[1oI2,GMR5mi\u0016\u00148\u000f\u0006\u0003\u0002\u001e\u0005\u0015\u0002\u0003B\u001fS\u0003?\u00012!LA\u0011\u0013\r\t\u0019C\f\u0002\u0007\r&dG/\u001a:\t\u000f\u0005\u001dR\u00021\u0001\u0002\u001e\u00059a-\u001b7uKJ\u001c\u0018!\u00032vS2$7kY1o)\u0019\ti#a\u0010\u0002DA1\u0011qFA\u001b\u0003si!!!\r\u000b\u0007\u0005M\"'A\u0002sI\u0012LA!a\u000e\u00022\t\u0019!\u000b\u0012#\u0011\u0007\u0015\fY$C\u0002\u0002>A\u00121AU8x\u0011\u001d\t\tE\u0004a\u0001\u0003\u0003\tqB]3rk&\u0014X\rZ\"pYVlgn\u001d\u0005\b\u0003Oq\u0001\u0019AA\u000f\u0003\u0011\u0019w\u000e]=\u0015\u0011\u0005%\u0013QJA(\u0003#\"2\u0001]A&\u0011\u0015\u0011w\u00021\u0001e\u0011\u001d)u\u0002%AA\u0002!CqaT\b\u0011\u0002\u0003\u0007\u0011\u000bC\u0004Z\u001fA\u0005\t\u0019A.\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%cU\u0011\u0011q\u000b\u0016\u0004\u0011\u0006e3FAA.!\u0011\ti&a\u001a\u000e\u0005\u0005}#\u0002BA1\u0003G\n\u0011\"\u001e8dQ\u0016\u001c7.\u001a3\u000b\u0007\u0005\u0015d(\u0001\u0006b]:|G/\u0019;j_:LA!!\u001b\u0002`\t\tRO\\2iK\u000e\\W\r\u001a,be&\fgnY3\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%eU\u0011\u0011q\u000e\u0016\u0004#\u0006e\u0013AD2paf$C-\u001a4bk2$HeM\u000b\u0003\u0003kR3aWA-\u00035\u0001(o\u001c3vGR\u0004&/\u001a4jqV\u0011\u00111\u0010\t\u0005\u0003{\n9)\u0004\u0002\u0002\u0000)!\u0011\u0011QAB\u0003\u0011a\u0017M\\4\u000b\u0005\u0005\u0015\u0015\u0001\u00026bm\u0006LA!!\u0006\u0002\u0000\u0005a\u0001O]8ek\u000e$\u0018I]5usV\u0011\u0011Q\u0012\t\u0004{\u0005=\u0015bAAI}\t\u0019\u0011J\u001c;\u0002\u001dA\u0014x\u000eZ;di\u0016cW-\\3oiR!\u0011qSAO!\ri\u0014\u0011T\u0005\u0004\u00037s$aA!os\"I\u0011qT\u000b\u0002\u0002\u0003\u0007\u0011QR\u0001\u0004q\u0012\n\u0014a\u00049s_\u0012,8\r^%uKJ\fGo\u001c:\u0016\u0005\u0005\u0015\u0006CBAT\u0003[\u000b9*\u0004\u0002\u0002**\u0019\u00111\u0016 \u0002\u0015\r|G\u000e\\3di&|g.\u0003\u0003\u00020\u0006%&\u0001C%uKJ\fGo\u001c:\u0002\u0011\r\fg.R9vC2$2a^A[\u0011%\tyjFA\u0001\u0002\u0004\t9*\u0001\u0005iCND7i\u001c3f)\t\ti)\u0001\u0005u_N#(/\u001b8h)\t\tY(\u0001\u0004fcV\fGn\u001d\u000b\u0004o\u0006\r\u0007\"CAP5\u0005\u0005\t\u0019AAL\u0003E9%/Z3oa2,XNU3mCRLwN\u001c\t\u0003+r\u0019B\u0001HAf\u0005B\u0019Q(!4\n\u0007\u0005=gH\u0001\u0004B]f\u0014VM\u001a\u000b\u0003\u0003\u000f\fQ!\u00199qYf$\u0002\"a6\u0002\\\u0006u\u0017q\u001c\u000b\u0004a\u0006e\u0007\"\u00022 \u0001\u0004!\u0007\"B# \u0001\u0004A\u0005\"B( \u0001\u0004\t\u0006\"B- \u0001\u0004Y\u0016aB;oCB\u0004H.\u001f\u000b\u0005\u0003K\f\t\u0010E\u0003>\u0003O\fY/C\u0002\u0002jz\u0012aa\u00149uS>t\u0007CB\u001f\u0002n\"\u000b6,C\u0002\u0002pz\u0012a\u0001V;qY\u0016\u001c\u0004\u0002CAzA\u0005\u0005\t\u0019\u00019\u0002\u0007a$\u0003'A\u0006sK\u0006$'+Z:pYZ,GCAA}!\u0011\ti(a?\n\t\u0005u\u0018q\u0010\u0002\u0007\u001f\nTWm\u0019;")
public class GreenplumRelation
extends BaseRelation
implements PrunedFilteredScan,
Product,
scala.Serializable {
    private final StructType schema;
    private final GreenplumPartition[] parts;
    private final GreenplumOptions greenplumOptions;
    private final transient SQLContext sqlContext;
    private final boolean needConversion;

    public static Option<Tuple3<StructType, GreenplumPartition[], GreenplumOptions>> unapply(GreenplumRelation greenplumRelation) {
        return GreenplumRelation$.MODULE$.unapply(greenplumRelation);
    }

    public static GreenplumRelation apply(StructType structType, GreenplumPartition[] greenplumPartitionArray, GreenplumOptions greenplumOptions, SQLContext sQLContext) {
        return GreenplumRelation$.MODULE$.apply(structType, greenplumPartitionArray, greenplumOptions, sQLContext);
    }

    public StructType schema() {
        return this.schema;
    }

    public GreenplumPartition[] parts() {
        return this.parts;
    }

    public GreenplumOptions greenplumOptions() {
        return this.greenplumOptions;
    }

    public SQLContext sqlContext() {
        return this.sqlContext;
    }

    public boolean needConversion() {
        return this.needConversion;
    }

    private StructType projectSchemaToColumns(StructType schema, String[] columns) {
        return new StructType((StructField[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])columns)).map((Function1 & Serializable & scala.Serializable)col -> (StructField)schema.find((Function1 & Serializable & scala.Serializable)x$1 -> BoxesRunTime.boxToBoolean((boolean)GreenplumRelation.$anonfun$projectSchemaToColumns$2(col, x$1))).get(), Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(StructField.class))));
    }

    public Filter[] unhandledFilters(Filter[] filters) {
        return (Filter[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])filters)).filter((Function1 & Serializable & scala.Serializable)x$2 -> BoxesRunTime.boxToBoolean((boolean)GreenplumRelation.$anonfun$unhandledFilters$1(x$2)));
    }

    public RDD<Row> buildScan(String[] requiredColumns, Filter[] filters) {
        StructType projectedSchema = this.projectSchemaToColumns(this.schema(), requiredColumns);
        return new GreenplumRDD(this.sqlContext().sparkContext(), projectedSchema, this.parts(), this.greenplumOptions(), (Seq<String>)Predef$.MODULE$.wrapRefArray((Object[])requiredColumns), filters);
    }

    public GreenplumRelation copy(StructType schema, GreenplumPartition[] parts, GreenplumOptions greenplumOptions, SQLContext sqlContext) {
        return new GreenplumRelation(schema, parts, greenplumOptions, sqlContext);
    }

    public StructType copy$default$1() {
        return this.schema();
    }

    public GreenplumPartition[] copy$default$2() {
        return this.parts();
    }

    public GreenplumOptions copy$default$3() {
        return this.greenplumOptions();
    }

    public String productPrefix() {
        return "GreenplumRelation";
    }

    public int productArity() {
        return 3;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                return this.schema();
            }
            case 1: {
                return this.parts();
            }
            case 2: {
                return this.greenplumOptions();
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(x$1));
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof GreenplumRelation;
    }

    public int hashCode() {
        return ScalaRunTime$.MODULE$._hashCode((Product)this);
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
        if (!(object instanceof GreenplumRelation)) return false;
        boolean bl = true;
        if (!bl) return false;
        GreenplumRelation greenplumRelation = (GreenplumRelation)((Object)x$1);
        StructType structType = this.schema();
        StructType structType2 = greenplumRelation.schema();
        if (structType == null) {
            if (structType2 != null) {
                return false;
            }
        } else if (!structType.equals(structType2)) return false;
        if (this.parts() != greenplumRelation.parts()) return false;
        GreenplumOptions greenplumOptions = this.greenplumOptions();
        GreenplumOptions greenplumOptions2 = greenplumRelation.greenplumOptions();
        if (greenplumOptions == null) {
            if (greenplumOptions2 != null) {
                return false;
            }
        } else if (!greenplumOptions.equals(greenplumOptions2)) return false;
        if (!greenplumRelation.canEqual((Object)this)) return false;
        return true;
    }

    public static final /* synthetic */ boolean $anonfun$projectSchemaToColumns$2(String col$1, StructField x$1) {
        String string = x$1.name();
        String string2 = col$1;
        return !(string != null ? !string.equals(string2) : string2 != null);
    }

    public static final /* synthetic */ boolean $anonfun$unhandledFilters$1(Filter x$2) {
        return GreenplumRDD$.MODULE$.compileFilter(x$2).isEmpty();
    }

    public GreenplumRelation(StructType schema, GreenplumPartition[] parts, GreenplumOptions greenplumOptions, SQLContext sqlContext) {
        this.schema = schema;
        this.parts = parts;
        this.greenplumOptions = greenplumOptions;
        this.sqlContext = sqlContext;
        Product.$init$((Product)this);
        this.needConversion = false;
    }
}

