/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.catalyst.util.CaseInsensitiveMap$
 *  scala.Function1
 *  scala.Function2
 *  scala.MatchError
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.immutable.Map
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.conf;

import io.pivotal.greenplum.spark.conf.Default;
import io.pivotal.greenplum.spark.conf.Options;
import io.pivotal.greenplum.spark.conf.WhatIfMissing;
import java.util.Properties;
import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap$;
import scala.Function1;
import scala.Function2;
import scala.MatchError;
import scala.Option;
import scala.Predef$;
import scala.Serializable;
import scala.Tuple2;
import scala.collection.immutable.Map;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Try;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
@ScalaSignature(bytes="\u0006\u000114A!\u0005\n\u0001;!A1\u0006\u0001B\u0001B\u0003%A\u0006C\u0003;\u0001\u0011\u00051\bC\u0003;\u0001\u0011\u0005a\bC\u0004@\u0001\t\u0007I\u0011\u0001!\t\r\u0005\u0003\u0001\u0015!\u0003-\u0011\u001d\u0011\u0005A1A\u0005\n\rCaa\u0013\u0001!\u0002\u0013!\u0005b\u0002'\u0001\u0005\u0004%\t!\u0014\u0005\u0007#\u0002\u0001\u000b\u0011\u0002(\t\u000fI\u0003!\u0019!C\u0001\u001b\"11\u000b\u0001Q\u0001\n9Cq\u0001\u0016\u0001C\u0002\u0013\u0005Q\n\u0003\u0004V\u0001\u0001\u0006IA\u0014\u0005\u0006-\u0002!\ta\u0016\u0005\u0006A\u0002!\t%\u0019\u0005\u0006E\u0002!\te\u0019\u0002\u0016\u0007>tg.Z2uS>t\u0007k\\8m\u001fB$\u0018n\u001c8t\u0015\t\u0019B#\u0001\u0003d_:4'BA\u000b\u0017\u0003\u0015\u0019\b/\u0019:l\u0015\t9\u0002$A\u0005he\u0016,g\u000e\u001d7v[*\u0011\u0011DG\u0001\ba&4x\u000e^1m\u0015\u0005Y\u0012AA5p\u0007\u0001\u0019B\u0001\u0001\u0010%OA\u0011qDI\u0007\u0002A)\t\u0011%A\u0003tG\u0006d\u0017-\u0003\u0002$A\t1\u0011I\\=SK\u001a\u0004\"aH\u0013\n\u0005\u0019\u0002#\u0001D*fe&\fG.\u001b>bE2,\u0007C\u0001\u0015*\u001b\u0005\u0011\u0012B\u0001\u0016\u0013\u0005\u001dy\u0005\u000f^5p]N\f!c\u001c:jO&t\u0017\r\u001c)be\u0006lW\r^3sgB!Q\u0006N\u001c8\u001d\tq#\u0007\u0005\u00020A5\t\u0001G\u0003\u000229\u00051AH]8pizJ!a\r\u0011\u0002\rA\u0013X\rZ3g\u0013\t)dGA\u0002NCBT!a\r\u0011\u0011\u00055B\u0014BA\u001d7\u0005\u0019\u0019FO]5oO\u00061A(\u001b8jiz\"\"\u0001P\u001f\u0011\u0005!\u0002\u0001\"B\u0016\u0003\u0001\u0004aC#\u0001\u001f\u0002\u0015A\f'/Y7fi\u0016\u00148/F\u0001-\u0003-\u0001\u0018M]1nKR,'o\u001d\u0011\u0002\rA\u0014XMZ5y+\u0005!\u0005CA#K\u001b\u00051%BA$I\u0003\u0011a\u0017M\\4\u000b\u0003%\u000bAA[1wC&\u0011\u0011HR\u0001\baJ,g-\u001b=!\u0003-i\u0017N\\5nk6LE\r\\3\u0016\u00039\u0003\"aH(\n\u0005A\u0003#aA%oi\u0006aQ.\u001b8j[Vl\u0017\n\u001a7fA\u0005yQ.\u0019=j[Vl\u0007k\\8m'&TX-\u0001\tnCbLW.^7Q_>d7+\u001b>fA\u0005i\u0011\u000e\u001a7f)&lWm\\;u\u001bN\fa\"\u001b3mKRKW.Z8vi6\u001b\b%\u0001\u000ehKR\u0004&o\u001c9feRLWm],ji\"\u001cVO\u0019)sK\u001aL\u0007\u0010\u0006\u0002Y=B\u0011\u0011\fX\u0007\u00025*\u00111\fS\u0001\u0005kRLG.\u0003\u0002^5\nQ\u0001K]8qKJ$\u0018.Z:\t\u000b}s\u0001\u0019A\u001c\u0002\u0013M,(\r\u0015:fM&D\u0018\u0001\u00035bg\"\u001cu\u000eZ3\u0015\u00039\u000ba!Z9vC2\u001cHC\u00013h!\tyR-\u0003\u0002gA\t9!i\\8mK\u0006t\u0007\"\u00025\u0011\u0001\u0004I\u0017aA8cUB\u0011qD[\u0005\u0003W\u0002\u00121!\u00118z\u0001")
public class ConnectionPoolOptions
implements Serializable,
Options {
    private final Map<String, String> originalParameters;
    private final Map<String, String> parameters;
    private final String prefix;
    private final int minimumIdle;
    private final int maximumPoolSize;
    private final int idleTimeoutMs;

    @Override
    public String option(String optionName, WhatIfMissing whatIfMissing) {
        return Options.option$((Options)this, optionName, whatIfMissing);
    }

    @Override
    public <T> T option(String optionName, WhatIfMissing whatIfMissing, Function2<String, String, Try<T>> convert) {
        return (T)Options.option$(this, optionName, whatIfMissing, convert);
    }

    @Override
    public Option<String> option(String optionName) {
        return Options.option$(this, optionName);
    }

    @Override
    public <T> Option<T> option(String optionName, Function2<String, String, Try<T>> convert) {
        return Options.option$((Options)this, optionName, convert);
    }

    @Override
    public Function2<String, String, Try<Object>> bool() {
        return Options.bool$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> naturalLong() {
        return Options.naturalLong$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> int() {
        return Options.int$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> positiveInt() {
        return Options.positiveInt$(this);
    }

    @Override
    public Function2<String, String, Try<Object>> nonNegativeInt() {
        return Options.nonNegativeInt$(this);
    }

    @Override
    public Map<String, String> parameters() {
        return this.parameters;
    }

    private String prefix() {
        return this.prefix;
    }

    public int minimumIdle() {
        return this.minimumIdle;
    }

    public int maximumPoolSize() {
        return this.maximumPoolSize;
    }

    public int idleTimeoutMs() {
        return this.idleTimeoutMs;
    }

    public Properties getPropertiesWithSubPrefix(String subPrefix) {
        String fullPrefix = new StringBuilder(2).append(this.prefix()).append(".").append(subPrefix).append(".").toString();
        int prefixLen = fullPrefix.length();
        Properties properties = new Properties();
        this.originalParameters.withFilter((Function1 & java.io.Serializable & Serializable)check$ifrefutable$1 -> BoxesRunTime.boxToBoolean((boolean)ConnectionPoolOptions.$anonfun$getPropertiesWithSubPrefix$1(check$ifrefutable$1))).foreach((Function1 & java.io.Serializable & Serializable)x$1 -> {
            Tuple2 tuple2 = x$1;
            if (tuple2 != null) {
                String key = (String)tuple2._1();
                String value = (String)tuple2._2();
                if (key.startsWith(fullPrefix)) {
                    String keyWithoutPrefix = key.substring(prefixLen);
                    return properties.put(keyWithoutPrefix, value);
                }
                return BoxedUnit.UNIT;
            }
            throw new MatchError((Object)tuple2);
        });
        return properties;
    }

    public int hashCode() {
        return this.originalParameters.hashCode();
    }

    public boolean equals(Object obj) {
        return this.originalParameters.equals(obj);
    }

    public static final /* synthetic */ boolean $anonfun$getPropertiesWithSubPrefix$1(Tuple2 check$ifrefutable$1) {
        Tuple2 tuple2 = check$ifrefutable$1;
        return tuple2 != null;
    }

    public ConnectionPoolOptions(Map<String, String> originalParameters) {
        this.originalParameters = originalParameters;
        Options.$init$(this);
        this.parameters = CaseInsensitiveMap$.MODULE$.apply(originalParameters);
        this.prefix = "pool";
        this.minimumIdle = BoxesRunTime.unboxToInt((Object)this.option(new StringBuilder(8).append(this.prefix()).append(".minIdle").toString(), new Default("0"), this.nonNegativeInt()));
        this.maximumPoolSize = BoxesRunTime.unboxToInt((Object)this.option(new StringBuilder(8).append(this.prefix()).append(".maxSize").toString(), new Default("64"), this.positiveInt()));
        this.idleTimeoutMs = BoxesRunTime.unboxToInt((Object)this.option(new StringBuilder(10).append(this.prefix()).append(".timeoutMs").toString(), new Default("10000"), this.positiveInt()));
    }

    public ConnectionPoolOptions() {
        this((Map<String, String>)Predef$.MODULE$.Map().empty());
    }
}

