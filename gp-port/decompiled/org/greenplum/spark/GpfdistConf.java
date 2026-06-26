/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.SparkConf
 *  scala.Function0
 *  scala.Function1
 *  scala.None$
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcI$sp
 *  scala.runtime.java8.JFunction0$mcZ$sp
 */
package org.greenplum.spark;

import org.apache.spark.SparkConf;
import org.greenplum.spark.GpfdistConf$;
import scala.Function0;
import scala.Function1;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Serializable;
import scala.collection.immutable.StringOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;

@ScalaSignature(bytes="\u0006\u0001\u0005}b\u0001\u0002\u0010 \u0001\u0019B\u0001\u0002\r\u0001\u0003\u0006\u0004%I!\r\u0005\ts\u0001\u0011\t\u0011)A\u0005e!)!\b\u0001C\u0001w!)q\b\u0001C\u0001\u0001\")q\n\u0001C\u0001!\")A\u000b\u0001C\u0001+\")q\u000b\u0001C\u00011\")A\f\u0001C\u0005;\")!\r\u0001C\u0005G\")a\r\u0001C\u0005O\")!\u000e\u0001C\u0005W\u001e9\u0011QA\u0010\t\u0002\u0005\u001daA\u0002\u0010 \u0011\u0003\tI\u0001\u0003\u0004;\u001b\u0011\u0005\u00111\u0002\u0005\n\u0003\u001bi!\u0019!C\u0005\u0003\u001fAq!!\u0005\u000eA\u0003%A\tC\u0005\u0002\u00145\u0011\r\u0011\"\u0001\u0002\u0010!9\u0011QC\u0007!\u0002\u0013!\u0005\u0002CA\f\u001b\t\u0007I\u0011\u0002)\t\u000f\u0005eQ\u0002)A\u0005#\"I\u00111D\u0007C\u0002\u0013%\u0011q\u0002\u0005\b\u0003;i\u0001\u0015!\u0003E\u0011%\ty\"\u0004b\u0001\n\u0013\ty\u0001C\u0004\u0002\"5\u0001\u000b\u0011\u0002#\t\u0013\u0005\rRB1A\u0005\n\u0005=\u0001bBA\u0013\u001b\u0001\u0006I\u0001\u0012\u0005\t\u0003Oi!\u0019!C\u00051\"9\u0011\u0011F\u0007!\u0002\u0013I\u0006\"CA\u0016\u001b\u0005\u0005I\u0011BA\u0017\u0005-9\u0005O\u001a3jgR\u001cuN\u001c4\u000b\u0005\u0001\n\u0013!B:qCJ\\'B\u0001\u0012$\u0003%9'/Z3oa2,XNC\u0001%\u0003\ry'oZ\u0002\u0001'\r\u0001q%\f\t\u0003Q-j\u0011!\u000b\u0006\u0002U\u0005)1oY1mC&\u0011A&\u000b\u0002\u0007\u0003:L(+\u001a4\u0011\u0005!r\u0013BA\u0018*\u00051\u0019VM]5bY&T\u0018M\u00197f\u0003%\u0019\b/\u0019:l\u0007>tg-F\u00013!\t\u0019t'D\u00015\u0015\t\u0001SG\u0003\u00027G\u00051\u0011\r]1dQ\u0016L!\u0001\u000f\u001b\u0003\u0013M\u0003\u0018M]6D_:4\u0017AC:qCJ\\7i\u001c8gA\u00051A(\u001b8jiz\"\"\u0001\u0010 \u0011\u0005u\u0002Q\"A\u0010\t\u000bA\u001a\u0001\u0019\u0001\u001a\u0002\t!|7\u000f^\u000b\u0002\u0003B\u0019\u0001F\u0011#\n\u0005\rK#AB(qi&|g\u000e\u0005\u0002F\u0019:\u0011aI\u0013\t\u0003\u000f&j\u0011\u0001\u0013\u0006\u0003\u0013\u0016\na\u0001\u0010:p_Rt\u0014BA&*\u0003\u0019\u0001&/\u001a3fM&\u0011QJ\u0014\u0002\u0007'R\u0014\u0018N\\4\u000b\u0005-K\u0013A\u00037jgR,g\u000eU8siV\t\u0011\u000b\u0005\u0002)%&\u00111+\u000b\u0002\u0004\u0013:$\u0018\u0001\u00047pG\u0006$\u0018n\u001c8Q_J$X#\u0001,\u0011\u0007!\u0012\u0015+A\u0003jgN\u001bF*F\u0001Z!\tA#,\u0003\u0002\\S\t9!i\\8mK\u0006t\u0017AB4fi&sG\u000fF\u0002R=\u0002DQa\u0018\u0005A\u0002\u0011\u000b1a[3z\u0011\u0015\t\u0007\u00021\u0001R\u00031!WMZ1vYR4\u0016\r\\;f\u0003)9W\r\u001e\"p_2,\u0017M\u001c\u000b\u00043\u0012,\u0007\"B0\n\u0001\u0004!\u0005\"B1\n\u0001\u0004I\u0016\u0001D4fi>\u0003H/[8o\u0013:$Hc\u0001,iS\")qL\u0003a\u0001\t\")\u0011M\u0003a\u0001-\u00061Ao\u001c+za\u0016,\"\u0001\\8\u0015\r5D(p`A\u0001!\tqw\u000e\u0004\u0001\u0005\u000bA\\!\u0019A9\u0003\u0003Q\u000b\"A];\u0011\u0005!\u001a\u0018B\u0001;*\u0005\u001dqu\u000e\u001e5j]\u001e\u0004\"\u0001\u000b<\n\u0005]L#aA!os\")\u0011p\u0003a\u0001\t\u0006\t1\u000fC\u0003|\u0017\u0001\u0007A0A\u0005d_:4XM\u001d;feB!\u0001& #n\u0013\tq\u0018FA\u0005Gk:\u001cG/[8oc!)ql\u0003a\u0001\t\"1\u00111A\u0006A\u0002\u0011\u000b!bY8oM&<G+\u001f9f\u0003-9\u0005O\u001a3jgR\u001cuN\u001c4\u0011\u0005uj1cA\u0007([Q\u0011\u0011qA\u0001\u000b\u0017\u0016Kv\f\u0015*F\r&CV#\u0001#\u0002\u0017-+\u0015l\u0018)S\u000b\u001aK\u0005\fI\u0001\u0010\u0019&\u001bF+\u0012(`!>\u0013FkX&F3\u0006\u0001B*S*U\u000b:{\u0006k\u0014*U?.+\u0015\fI\u0001\u001a\u0019&\u001bF+\u0012(`!>\u0013Fk\u0018#F\r\u0006+F\nV0W\u00032+V)\u0001\u000eM\u0013N#VIT0Q\u001fJ#v\fR#G\u0003VcEk\u0018,B\u0019V+\u0005%A\tM\u001f\u000e\u000bE+S(O?B{%\u000bV0L\u000bf\u000b!\u0003T(D\u0003RKuJT0Q\u001fJ#vlS#ZA\u0005A\u0001jT*U?.+\u0015,A\u0005I\u001fN#vlS#ZA\u0005Q\u0011jU0T'2{6*R-\u0002\u0017%\u001bvlU*M?.+\u0015\fI\u0001\u0015\u0013N{6k\u0015'`\t\u00163\u0015)\u0016'U?Z\u000bE*V#\u0002+%\u001bvlU*M?\u0012+e)Q+M)~3\u0016\tT+FA\u0005Y!/Z1e%\u0016\u001cx\u000e\u001c<f)\t\ty\u0003\u0005\u0003\u00022\u0005mRBAA\u001a\u0015\u0011\t)$a\u000e\u0002\t1\fgn\u001a\u0006\u0003\u0003s\tAA[1wC&!\u0011QHA\u001a\u0005\u0019y%M[3di\u0002")
public class GpfdistConf
implements Serializable {
    private final SparkConf sparkConf;

    public static String LISTEN_PORT_KEY() {
        return GpfdistConf$.MODULE$.LISTEN_PORT_KEY();
    }

    private SparkConf sparkConf() {
        return this.sparkConf;
    }

    public Option<String> host() {
        return this.sparkConf().getOption(GpfdistConf$.MODULE$.org$greenplum$spark$GpfdistConf$$HOST_KEY());
    }

    public int listenPort() {
        return this.getInt(GpfdistConf$.MODULE$.LISTEN_PORT_KEY(), GpfdistConf$.MODULE$.org$greenplum$spark$GpfdistConf$$LISTEN_PORT_DEFAULT_VALUE());
    }

    public Option<Object> locationPort() {
        return this.getOptionInt(GpfdistConf$.MODULE$.org$greenplum$spark$GpfdistConf$$LOCATION_PORT_KEY(), (Option<Object>)None$.MODULE$);
    }

    public boolean isSSL() {
        return this.getBoolean(GpfdistConf$.MODULE$.org$greenplum$spark$GpfdistConf$$IS_SSL_KEY(), GpfdistConf$.MODULE$.org$greenplum$spark$GpfdistConf$$IS_SSL_DEFAULT_VALUE());
    }

    private int getInt(String key, int defaultValue) {
        return BoxesRunTime.unboxToInt((Object)this.sparkConf().getOption(key).map((Function1 & java.io.Serializable & Serializable)x$1 -> BoxesRunTime.boxToInteger((int)GpfdistConf.$anonfun$getInt$1(this, key, x$1))).getOrElse((Function0)(JFunction0.mcI.sp & java.io.Serializable & Serializable)() -> defaultValue));
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        return BoxesRunTime.unboxToBoolean((Object)this.sparkConf().getOption(key).map((Function1 & java.io.Serializable & Serializable)x$3 -> BoxesRunTime.boxToBoolean((boolean)GpfdistConf.$anonfun$getBoolean$1(this, key, x$3))).getOrElse((Function0)(JFunction0.mcZ.sp & java.io.Serializable & Serializable)() -> defaultValue));
    }

    private Option<Object> getOptionInt(String key, Option<Object> defaultValue) {
        return this.sparkConf().getOption(key).map((Function1 & java.io.Serializable & Serializable)x$5 -> BoxesRunTime.boxToInteger((int)GpfdistConf.$anonfun$getOptionInt$1(this, key, x$5))).orElse((Function0 & java.io.Serializable & Serializable)() -> defaultValue);
    }

    private <T> T toType(String s2, Function1<String, T> converter, String key, String configType) {
        Object object;
        try {
            object = converter.apply((Object)s2.trim());
        }
        catch (Throwable throwable) {
            Throwable throwable2 = throwable;
            if (throwable2 instanceof NumberFormatException ? true : throwable2 instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(new StringBuilder(21).append(key).append(" should be ").append(configType).append(", but was ").append(s2).toString());
            }
            throw throwable;
        }
        return (T)object;
    }

    public static final /* synthetic */ int $anonfun$getInt$2(String x$2) {
        return new StringOps(Predef$.MODULE$.augmentString(x$2)).toInt();
    }

    public static final /* synthetic */ int $anonfun$getInt$1(GpfdistConf $this, String key$1, String x$1) {
        return BoxesRunTime.unboxToInt($this.toType(x$1, (Function1 & java.io.Serializable & Serializable)x$2 -> BoxesRunTime.boxToInteger((int)GpfdistConf.$anonfun$getInt$2(x$2)), key$1, "int"));
    }

    public static final /* synthetic */ boolean $anonfun$getBoolean$2(String x$4) {
        return new StringOps(Predef$.MODULE$.augmentString(x$4)).toBoolean();
    }

    public static final /* synthetic */ boolean $anonfun$getBoolean$1(GpfdistConf $this, String key$2, String x$3) {
        return BoxesRunTime.unboxToBoolean($this.toType(x$3, (Function1 & java.io.Serializable & Serializable)x$4 -> BoxesRunTime.boxToBoolean((boolean)GpfdistConf.$anonfun$getBoolean$2(x$4)), key$2, "boolean"));
    }

    public static final /* synthetic */ int $anonfun$getOptionInt$2(String x$6) {
        return new StringOps(Predef$.MODULE$.augmentString(x$6)).toInt();
    }

    public static final /* synthetic */ int $anonfun$getOptionInt$1(GpfdistConf $this, String key$3, String x$5) {
        return BoxesRunTime.unboxToInt($this.toType(x$5, (Function1 & java.io.Serializable & Serializable)x$6 -> BoxesRunTime.boxToInteger((int)GpfdistConf.$anonfun$getOptionInt$2(x$6)), key$3, "int"));
    }

    public GpfdistConf(SparkConf sparkConf) {
        this.sparkConf = sparkConf;
    }
}

