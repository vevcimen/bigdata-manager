/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Function2
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple2
 *  scala.collection.immutable.Map
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.conf;

import io.pivotal.greenplum.spark.conf.Default;
import io.pivotal.greenplum.spark.conf.ErrorIfMissing$;
import io.pivotal.greenplum.spark.conf.WhatIfMissing;
import java.io.Serializable;
import scala.Function1;
import scala.Function2;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Predef$;
import scala.Some;
import scala.Tuple2;
import scala.collection.immutable.Map;
import scala.collection.immutable.StringOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
@ScalaSignature(bytes="\u0006\u0001u4q\u0001D\u0007\u0011\u0002\u0007\u0005\u0001\u0004C\u0003 \u0001\u0011\u0005\u0001\u0005C\u0003%\u0001\u0019EQ\u0005C\u00035\u0001\u0011\u0005Q\u0007C\u00035\u0001\u0011\u0005a\bC\u00035\u0001\u0011\u0005\u0001\fC\u00035\u0001\u0011\u0005Q\fC\u0003g\u0001\u0011\u0005q\rC\u0003n\u0001\u0011\u0005a\u000eC\u0003u\u0001\u0011\u0005Q\u000fC\u0003|\u0001\u0011\u0005Q\u000fC\u0003}\u0001\u0011\u0005QOA\u0004PaRLwN\\:\u000b\u00059y\u0011\u0001B2p]\u001aT!\u0001E\t\u0002\u000bM\u0004\u0018M]6\u000b\u0005I\u0019\u0012!C4sK\u0016t\u0007\u000f\\;n\u0015\t!R#A\u0004qSZ|G/\u00197\u000b\u0003Y\t!![8\u0004\u0001M\u0011\u0001!\u0007\t\u00035ui\u0011a\u0007\u0006\u00029\u0005)1oY1mC&\u0011ad\u0007\u0002\u0007\u0003:L(+\u001a4\u0002\r\u0011Jg.\u001b;%)\u0005\t\u0003C\u0001\u000e#\u0013\t\u00193D\u0001\u0003V]&$\u0018A\u00039be\u0006lW\r^3sgV\ta\u0005\u0005\u0003(]E\ndB\u0001\u0015-!\tI3$D\u0001+\u0015\tYs#\u0001\u0004=e>|GOP\u0005\u0003[m\ta\u0001\u0015:fI\u00164\u0017BA\u00181\u0005\ri\u0015\r\u001d\u0006\u0003[m\u0001\"a\n\u001a\n\u0005M\u0002$AB*ue&tw-\u0001\u0004paRLwN\u001c\u000b\u0004cYB\u0004\"B\u001c\u0004\u0001\u0004\t\u0014AC8qi&|gNT1nK\")\u0011h\u0001a\u0001u\u0005iq\u000f[1u\u0013\u001al\u0015n]:j]\u001e\u0004\"a\u000f\u001f\u000e\u00035I!!P\u0007\u0003\u001b]C\u0017\r^%g\u001b&\u001c8/\u001b8h+\ty$\t\u0006\u0003A\u00172k\u0005CA!C\u0019\u0001!Qa\u0011\u0003C\u0002\u0011\u0013\u0011\u0001V\t\u0003\u000b\"\u0003\"A\u0007$\n\u0005\u001d[\"a\u0002(pi\"Lgn\u001a\t\u00035%K!AS\u000e\u0003\u0007\u0005s\u0017\u0010C\u00038\t\u0001\u0007\u0011\u0007C\u0003:\t\u0001\u0007!\bC\u0003O\t\u0001\u0007q*A\u0004d_:4XM\u001d;\u0011\u000bi\u0001\u0016'\r*\n\u0005E[\"!\u0003$v]\u000e$\u0018n\u001c83!\r\u0019f\u000bQ\u0007\u0002)*\u0011QkG\u0001\u0005kRLG.\u0003\u0002X)\n\u0019AK]=\u0015\u0005ec\u0006c\u0001\u000e[c%\u00111l\u0007\u0002\u0007\u001fB$\u0018n\u001c8\t\u000b]*\u0001\u0019A\u0019\u0016\u0005y\u000bGcA0cGB\u0019!D\u00171\u0011\u0005\u0005\u000bG!B\"\u0007\u0005\u0004!\u0005\"B\u001c\u0007\u0001\u0004\t\u0004\"\u0002(\u0007\u0001\u0004!\u0007#\u0002\u000eQcE*\u0007cA*WA\u0006!!m\\8m+\u0005A\u0007#\u0002\u000eQcEJ\u0007cA*WUB\u0011!d[\u0005\u0003Yn\u0011qAQ8pY\u0016\fg.A\u0006oCR,(/\u00197M_:<W#A8\u0011\u000bi\u0001\u0016'\r9\u0011\u0007M3\u0016\u000f\u0005\u0002\u001be&\u00111o\u0007\u0002\u0005\u0019>tw-A\u0002j]R,\u0012A\u001e\t\u00065A\u000b\u0014g\u001e\t\u0004'ZC\bC\u0001\u000ez\u0013\tQ8DA\u0002J]R\f1\u0002]8tSRLg/Z%oi\u0006qan\u001c8OK\u001e\fG/\u001b<f\u0013:$\b")
public interface Options {
    public Map<String, String> parameters();

    public static /* synthetic */ String option$(Options $this, String optionName, WhatIfMissing whatIfMissing) {
        return $this.option(optionName, whatIfMissing);
    }

    default public String option(String optionName, WhatIfMissing whatIfMissing) {
        Option option;
        Tuple2 tuple2 = new Tuple2(this.option(optionName), (Object)whatIfMissing);
        if (tuple2 != null) {
            Some some;
            String string;
            Option option2 = (Option)tuple2._1();
            WhatIfMissing whatIfMissing2 = (WhatIfMissing)tuple2._2();
            if ((option2 instanceof Some && (string = (String)(some = (Some)option2).value()) == null ? true : None$.MODULE$.equals(option2)) && ErrorIfMissing$.MODULE$.equals(whatIfMissing2)) {
                throw new IllegalArgumentException(new StringBuilder(42).append("requirement failed: Option '").append(optionName).append("' is required.").toString());
            }
        }
        if (tuple2 != null) {
            Some some;
            String string;
            Option option3 = (Option)tuple2._1();
            WhatIfMissing whatIfMissing3 = (WhatIfMissing)tuple2._2();
            if ((option3 instanceof Some && (string = (String)(some = (Some)option3).value()) == null ? true : None$.MODULE$.equals(option3)) && whatIfMissing3 instanceof Default) {
                Default default_ = (Default)whatIfMissing3;
                String string2 = default_.value();
                return string2;
            }
        }
        if (tuple2 != null && (option = (Option)tuple2._1()) instanceof Some) {
            Some some = (Some)option;
            String value = (String)some.value();
            return value;
        }
        throw new MatchError((Object)tuple2);
    }

    public static /* synthetic */ Object option$(Options $this, String optionName, WhatIfMissing whatIfMissing, Function2 convert) {
        return $this.option(optionName, whatIfMissing, convert);
    }

    default public <T> T option(String optionName, WhatIfMissing whatIfMissing, Function2<String, String, Try<T>> convert) {
        String stringValue = this.option(optionName, whatIfMissing);
        return (T)((Try)convert.apply((Object)stringValue, (Object)optionName)).get();
    }

    public static /* synthetic */ Option option$(Options $this, String optionName) {
        return $this.option(optionName);
    }

    default public Option<String> option(String optionName) {
        return this.parameters().get((Object)optionName);
    }

    public static /* synthetic */ Option option$(Options $this, String optionName, Function2 convert) {
        return $this.option(optionName, convert);
    }

    default public <T> Option<T> option(String optionName, Function2<String, String, Try<T>> convert) {
        return this.option(optionName).map((Function1 & Serializable & scala.Serializable)x$1 -> ((Try)convert.apply(x$1, (Object)optionName)).get());
    }

    public static /* synthetic */ Function2 bool$(Options $this) {
        return $this.bool();
    }

    default public Function2<String, String, Try<Object>> bool() {
        return (Function2 & Serializable & scala.Serializable)(value, name) -> {
            Success success;
            try {
                success = new Success((Object)BoxesRunTime.boxToBoolean((boolean)new StringOps(Predef$.MODULE$.augmentString(value)).toBoolean()));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                success = new Failure((Throwable)new IllegalArgumentException(new StringBuilder(61).append("requirement failed: Option '").append((String)name).append("' should be either true or false.").toString()));
            }
            return success;
        };
    }

    public static /* synthetic */ Function2 naturalLong$(Options $this) {
        return $this.naturalLong();
    }

    default public Function2<String, String, Try<Object>> naturalLong() {
        return (Function2 & Serializable & scala.Serializable)(value, name) -> {
            Failure failure;
            block3: {
                try {
                    if (new StringOps(Predef$.MODULE$.augmentString(value)).toLong() >= 0L) {
                        failure = new Success((Object)BoxesRunTime.boxToLong((long)new StringOps(Predef$.MODULE$.augmentString(value)).toLong()));
                        break block3;
                    }
                    failure = new Failure((Throwable)new IllegalArgumentException(new StringBuilder(56).append("requirement failed: Option '").append((String)name).append("' should be zero or greater.").toString()));
                }
                catch (NumberFormatException numberFormatException) {
                    failure = new Failure((Throwable)new IllegalArgumentException(new StringBuilder(53).append("requirement failed: Option '").append((String)name).append("' should be a valid long.").toString()));
                }
            }
            return failure;
        };
    }

    public static /* synthetic */ Function2 int$(Options $this) {
        return $this.int();
    }

    default public Function2<String, String, Try<Object>> int() {
        return (Function2 & Serializable & scala.Serializable)(value, name) -> {
            Success success;
            try {
                success = new Success((Object)BoxesRunTime.boxToInteger((int)new StringOps(Predef$.MODULE$.augmentString(value)).toInt()));
            }
            catch (NumberFormatException numberFormatException) {
                success = new Failure((Throwable)new IllegalArgumentException(new StringBuilder(56).append("requirement failed: Option '").append((String)name).append("' should be a valid integer.").toString()));
            }
            return success;
        };
    }

    public static /* synthetic */ Function2 positiveInt$(Options $this) {
        return $this.positiveInt();
    }

    default public Function2<String, String, Try<Object>> positiveInt() {
        return (Function2 & Serializable & scala.Serializable)(value, name) -> ((Try)this.int().apply(value, name)).flatMap((Function1 & Serializable & scala.Serializable)n -> Options.$anonfun$positiveInt$2(name, BoxesRunTime.unboxToInt((Object)n)));
    }

    public static /* synthetic */ Function2 nonNegativeInt$(Options $this) {
        return $this.nonNegativeInt();
    }

    default public Function2<String, String, Try<Object>> nonNegativeInt() {
        return (Function2 & Serializable & scala.Serializable)(value, name) -> ((Try)this.int().apply(value, name)).flatMap((Function1 & Serializable & scala.Serializable)n -> Options.$anonfun$nonNegativeInt$2(name, BoxesRunTime.unboxToInt((Object)n)));
    }

    public static /* synthetic */ Try $anonfun$positiveInt$2(String name$1, int n) {
        if (n > 0) {
            return new Success((Object)BoxesRunTime.boxToInteger((int)n));
        }
        return new Failure((Throwable)new IllegalArgumentException(new StringBuilder(59).append("requirement failed: Option '").append(name$1).append("' should be a positive integer.").toString()));
    }

    public static /* synthetic */ Try $anonfun$nonNegativeInt$2(String name$2, int n) {
        if (n >= 0) {
            return new Success((Object)BoxesRunTime.boxToInteger((int)n));
        }
        return new Failure((Throwable)new IllegalArgumentException(new StringBuilder(56).append("requirement failed: Option '").append(name$2).append("' should be zero or greater.").toString()));
    }

    public static void $init$(Options $this) {
    }
}

