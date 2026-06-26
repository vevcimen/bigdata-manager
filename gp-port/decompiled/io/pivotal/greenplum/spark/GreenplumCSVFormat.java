/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 *  scala.runtime.Null$
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.GreenplumCSVFormat$;
import scala.reflect.ScalaSignature;
import scala.runtime.Null$;
import shadeio.univocity.parsers.csv.CsvParserSettings;

@ScalaSignature(bytes="\u0006\u0001\u0005-q!B\u0012%\u0011\u0003ic!B\u0018%\u0011\u0003\u0001\u0004\"B\u001c\u0002\t\u0003A\u0004bB\u001d\u0002\u0005\u0004%\tA\u000f\u0005\u0007}\u0005\u0001\u000b\u0011B\u001e\t\u000f}\n!\u0019!C\u0001u!1\u0001)\u0001Q\u0001\nmBq!Q\u0001C\u0002\u0013\u0005!\t\u0003\u0004L\u0003\u0001\u0006Ia\u0011\u0005\b\u0019\u0006\u0011\r\u0011\"\u0001;\u0011\u0019i\u0015\u0001)A\u0005w!9a*\u0001b\u0001\n\u0003\u0011\u0005BB(\u0002A\u0003%1\tC\u0004Q\u0003\t\u0007I\u0011\u0001\"\t\rE\u000b\u0001\u0015!\u0003D\u0011\u001d\u0011\u0016A1A\u0005\u0002\tCaaU\u0001!\u0002\u0013\u0019\u0005b\u0002+\u0002\u0005\u0004%\t!\u0016\u0005\u00073\u0006\u0001\u000b\u0011\u0002,\t\u000fi\u000b!\u0019!C\u0001\u0005\"11,\u0001Q\u0001\n\rCq\u0001X\u0001C\u0002\u0013\u0005Q\f\u0003\u0004b\u0003\u0001\u0006IA\u0018\u0005\bE\u0006\u0011\r\u0011\"\u0001^\u0011\u0019\u0019\u0017\u0001)A\u0005=\"9A-\u0001b\u0001\n\u0003\u0011\u0005BB3\u0002A\u0003%1\tC\u0004g\u0003\t\u0007I\u0011A4\t\r-\f\u0001\u0015!\u0003i\u0011\u001da\u0017A1A\u0005\u0002\u001dDa!\\\u0001!\u0002\u0013A\u0007b\u00028\u0002\u0005\u0004%\ta\u001a\u0005\u0007_\u0006\u0001\u000b\u0011\u00025\t\u0011A\f\u0001R1A\u0005\u0002EDQA`\u0001\u0005\nE\f!c\u0012:fK:\u0004H.^7D'Z3uN]7bi*\u0011QEJ\u0001\u0006gB\f'o\u001b\u0006\u0003O!\n\u0011b\u001a:fK:\u0004H.^7\u000b\u0005%R\u0013a\u00029jm>$\u0018\r\u001c\u0006\u0002W\u0005\u0011\u0011n\\\u0002\u0001!\tq\u0013!D\u0001%\u0005I9%/Z3oa2,XnQ*W\r>\u0014X.\u0019;\u0014\u0005\u0005\t\u0004C\u0001\u001a6\u001b\u0005\u0019$\"\u0001\u001b\u0002\u000bM\u001c\u0017\r\\1\n\u0005Y\u001a$AB!osJ+g-\u0001\u0004=S:LGO\u0010\u000b\u0002[\u00051QiU\"B!\u0016+\u0012a\u000f\t\u0003eqJ!!P\u001a\u0003\t\rC\u0017M]\u0001\b\u000bN\u001b\u0015\tU#!\u00039\u0019\u0005*\u0011*`\t\u0016c\u0015*T%U\u000bJ\u000bqb\u0011%B%~#U\tT%N\u0013R+%\u000bI\u0001\b\u001d\u0016;F*\u0013(F+\u0005\u0019\u0005C\u0001#J\u001b\u0005)%B\u0001$H\u0003\u0011a\u0017M\\4\u000b\u0003!\u000bAA[1wC&\u0011!*\u0012\u0002\u0007'R\u0014\u0018N\\4\u0002\u00119+u\u000bT%O\u000b\u0002\nQ!U+P)\u0016\u000ba!U+P)\u0016\u0003\u0013\u0001D)V\u001fR+ul\u0015+S\u0013:;\u0015!D)V\u001fR+ul\u0015+S\u0013:;\u0005%A\u0007W\u00032+ViX(G?:+F\nT\u0001\u000f-\u0006cU+R0P\r~sU\u000b\u0014'!\u0003-)U\n\u0015+Z?Z\u000bE*V#\u0002\u0019\u0015k\u0005\u000bV-`-\u0006cU+\u0012\u0011\u0002\u00159+F\nT0W\u00032+V)F\u0001W!\t\u0011t+\u0003\u0002Yg\t!a*\u001e7m\u0003-qU\u000b\u0014'`-\u0006cU+\u0012\u0011\u0002!\u0011+e)Q+M)~+ejQ(E\u0013:;\u0015!\u0005#F\r\u0006+F\nV0F\u001d\u000e{E)\u0013(HA\u0005i1\u000b\u0016*J\u0007R{\u0016+V(U\u000bN+\u0012A\u0018\t\u0003e}K!\u0001Y\u001a\u0003\u000f\t{w\u000e\\3b]\u0006q1\u000b\u0016*J\u0007R{\u0016+V(U\u000bN\u0003\u0013!E%H\u001d>\u0013ViX,I\u0013R+5\u000bU!D\u000b\u0006\u0011\u0012j\u0012(P%\u0016{v\u000bS%U\u000bN\u0003\u0016iQ#!\u00035)5kQ!Q\u000b\u0012{\u0016+V(U\u000b\u0006qQiU\"B!\u0016#u,U+P)\u0016\u0003\u0013aD5oaV$()\u001e4gKJ\u001c\u0016N_3\u0016\u0003!\u0004\"AM5\n\u0005)\u001c$aA%oi\u0006\u0001\u0012N\u001c9vi\n+hMZ3s'&TX\rI\u0001\u0012[\u0006D8\t[1sgB+'oQ8mk6t\u0017AE7bq\u000eC\u0017M]:QKJ\u001cu\u000e\\;n]\u0002\nQ#\\1y\u000bJ\u0014xN]\"p]R,g\u000e\u001e'f]\u001e$\b.\u0001\fnCb,%O]8s\u0007>tG/\u001a8u\u0019\u0016tw\r\u001e5!\u0003\u001d!UIR!V\u0019R+\u0012A\u001d\t\u0003grl\u0011\u0001\u001e\u0006\u0004k\u0006\u0015\u0011aA2tm*\u0019q/!\u0003\u0002\u000fA\f'o]3sg*\u0011\u0011P_\u0001\nk:Lgo\\2jifT\u0011a_\u0001\u0004G>l\u0017BA?u\u0005E\u00195O\u001e)beN,'oU3ui&twm]\u0001\u000bO\u0016$H)\u001a4bk2$\u0018aB:iC\u0012,\u0017n\u001c\u0006\u0002\u007f*\u0019\u00110!\u0001\u000b\u0007]\f\u0019AC\u0001\u0000\u0015\rI\u0018q\u0001")
public final class GreenplumCSVFormat {
    public static CsvParserSettings DEFAULT() {
        return GreenplumCSVFormat$.MODULE$.DEFAULT();
    }

    public static int maxErrorContentLength() {
        return GreenplumCSVFormat$.MODULE$.maxErrorContentLength();
    }

    public static int maxCharsPerColumn() {
        return GreenplumCSVFormat$.MODULE$.maxCharsPerColumn();
    }

    public static int inputBufferSize() {
        return GreenplumCSVFormat$.MODULE$.inputBufferSize();
    }

    public static String ESCAPED_QUOTE() {
        return GreenplumCSVFormat$.MODULE$.ESCAPED_QUOTE();
    }

    public static boolean IGNORE_WHITESPACE() {
        return GreenplumCSVFormat$.MODULE$.IGNORE_WHITESPACE();
    }

    public static boolean STRICT_QUOTES() {
        return GreenplumCSVFormat$.MODULE$.STRICT_QUOTES();
    }

    public static String DEFAULT_ENCODING() {
        return GreenplumCSVFormat$.MODULE$.DEFAULT_ENCODING();
    }

    public static Null$ NULL_VALUE() {
        return GreenplumCSVFormat$.MODULE$.NULL_VALUE();
    }

    public static String EMPTY_VALUE() {
        return GreenplumCSVFormat$.MODULE$.EMPTY_VALUE();
    }

    public static String VALUE_OF_NULL() {
        return GreenplumCSVFormat$.MODULE$.VALUE_OF_NULL();
    }

    public static String QUOTE_STRING() {
        return GreenplumCSVFormat$.MODULE$.QUOTE_STRING();
    }

    public static char QUOTE() {
        return GreenplumCSVFormat$.MODULE$.QUOTE();
    }

    public static String NEWLINE() {
        return GreenplumCSVFormat$.MODULE$.NEWLINE();
    }

    public static char CHAR_DELIMITER() {
        return GreenplumCSVFormat$.MODULE$.CHAR_DELIMITER();
    }

    public static char ESCAPE() {
        return GreenplumCSVFormat$.MODULE$.ESCAPE();
    }
}

