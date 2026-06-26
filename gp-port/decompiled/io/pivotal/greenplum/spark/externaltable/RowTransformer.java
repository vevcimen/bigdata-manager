/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.Row
 *  scala.Function1
 *  scala.collection.Seq
 *  scala.reflect.ScalaSignature
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.RowTransformer$;
import org.apache.spark.sql.Row;
import scala.Function1;
import scala.collection.Seq;
import scala.reflect.ScalaSignature;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001y;Qa\u0002\u0005\t\u0002M1Q!\u0006\u0005\t\u0002YAQaJ\u0001\u0005\u0002!Bq!K\u0001C\u0002\u0013\u0005!\u0006\u0003\u0004:\u0003\u0001\u0006Ia\u000b\u0005\u0006u\u0005!\ta\u000f\u0005\u00065\u0006!IaW\u0001\u000f%><HK]1og\u001a|'/\\3s\u0015\tI!\"A\u0007fqR,'O\\1mi\u0006\u0014G.\u001a\u0006\u0003\u00171\tQa\u001d9be.T!!\u0004\b\u0002\u0013\u001d\u0014X-\u001a8qYVl'BA\b\u0011\u0003\u001d\u0001\u0018N^8uC2T\u0011!E\u0001\u0003S>\u001c\u0001\u0001\u0005\u0002\u0015\u00035\t\u0001B\u0001\bS_^$&/\u00198tM>\u0014X.\u001a:\u0014\u0007\u00059R\u0004\u0005\u0002\u001975\t\u0011DC\u0001\u001b\u0003\u0015\u00198-\u00197b\u0013\ta\u0012D\u0001\u0004B]f\u0014VM\u001a\t\u0003=\u0015j\u0011a\b\u0006\u0003A\u0005\nAb]2bY\u0006dwnZ4j]\u001eT!AI\u0012\u0002\u0011QL\b/Z:bM\u0016T\u0011\u0001J\u0001\u0004G>l\u0017B\u0001\u0014 \u0005-a\u0015M_=M_\u001e<\u0017N\\4\u0002\rqJg.\u001b;?)\u0005\u0019\u0012\u0001E5eK:$\u0018\u000e^=Gk:\u001cG/[8o+\u0005Y\u0003\u0003\u0002\r-]9J!!L\r\u0003\u0013\u0019+hn\u0019;j_:\f\u0004CA\u00188\u001b\u0005\u0001$BA\u00193\u0003\r\u0019\u0018\u000f\u001c\u0006\u0003\u0017MR!\u0001N\u001b\u0002\r\u0005\u0004\u0018m\u00195f\u0015\u00051\u0014aA8sO&\u0011\u0001\b\r\u0002\u0004%><\u0018!E5eK:$\u0018\u000e^=Gk:\u001cG/[8oA\u0005Yq-\u001a;Gk:\u001cG/[8o)\ra$\t\u0017\t\u0004{\u0001[S\"\u0001 \u000b\u0005}J\u0012\u0001B;uS2L!!\u0011 \u0003\u0007Q\u0013\u0018\u0010C\u0003D\u000b\u0001\u0007A)A\u0005ta\u0006\u00148nQ8mgB\u0019Q)\u0014)\u000f\u0005\u0019[eBA$K\u001b\u0005A%BA%\u0013\u0003\u0019a$o\\8u}%\t!$\u0003\u0002M3\u00059\u0001/Y2lC\u001e,\u0017B\u0001(P\u0005\r\u0019V-\u001d\u0006\u0003\u0019f\u0001\"!U+\u000f\u0005I\u001b\u0006CA$\u001a\u0013\t!\u0016$\u0001\u0004Qe\u0016$WMZ\u0005\u0003-^\u0013aa\u0015;sS:<'B\u0001+\u001a\u0011\u0015IV\u00011\u0001E\u0003!9\u0007\u000f\u001a2D_2\u001c\u0018\u0001\u00054pe6\fGoQ8mk6tG*[:u)\t\u0001F\fC\u0003^\r\u0001\u0007A)A\u0004d_2,XN\\:")
public final class RowTransformer {
    public static Try<Function1<Row, Row>> getFunction(Seq<String> seq, Seq<String> seq2) {
        return RowTransformer$.MODULE$.getFunction(seq, seq2);
    }

    public static Function1<Row, Row> identityFunction() {
        return RowTransformer$.MODULE$.identityFunction();
    }
}

