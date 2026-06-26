/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.api.model.networking.v1.Ingress
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.Predef$
 *  scala.Some
 *  scala.Tuple2
 *  scala.reflect.ScalaSignature
 */
package org.greenplum.k8s;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import java.util.function.UnaryOperator;
import org.greenplum.spark.GpfdistConf;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Predef$;
import scala.Some;
import scala.Tuple2;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001I3Q\u0001B\u0003\u0002\u00021A\u0001\"\u000f\u0001\u0003\u0002\u0003\u0006IA\u000f\u0005\u0006\u0001\u0002!\t!\u0011\u0005\u0006\u000b\u0002!\tA\u0012\u0002\f\u0013:<'/Z:t\u000b\u0012LGO\u0003\u0002\u0007\u000f\u0005\u00191\u000eO:\u000b\u0005!I\u0011!C4sK\u0016t\u0007\u000f\\;n\u0015\u0005Q\u0011aA8sO\u000e\u00011\u0003\u0002\u0001\u000e+=\u0002\"AD\n\u000e\u0003=Q!\u0001E\t\u0002\t1\fgn\u001a\u0006\u0002%\u0005!!.\u0019<b\u0013\t!rB\u0001\u0004PE*,7\r\u001e\t\u0004-miR\"A\f\u000b\u0005aI\u0012\u0001\u00034v]\u000e$\u0018n\u001c8\u000b\u0005i\t\u0012\u0001B;uS2L!\u0001H\f\u0003\u001bUs\u0017M]=Pa\u0016\u0014\u0018\r^8s!\tqR&D\u0001 \u0015\t\u0001\u0013%\u0001\u0002wc)\u0011!eI\u0001\u000b]\u0016$xo\u001c:lS:<'B\u0001\u0013&\u0003\u0015iw\u000eZ3m\u0015\t1s%A\u0002ba&T!\u0001K\u0015\u0002\u0015-,(-\u001a:oKR,7O\u0003\u0002+W\u00059a-\u00192sS\u000eD$\"\u0001\u0017\u0002\u0005%|\u0017B\u0001\u0018 \u0005\u001dIen\u001a:fgN\u0004\"\u0001M\u001c\u000e\u0003ER!AM\u001a\u0002\u0019M\u001c\u0017\r\\1m_\u001e<\u0017N\\4\u000b\u0005Q*\u0014\u0001\u0003;za\u0016\u001c\u0018MZ3\u000b\u0003Y\n1aY8n\u0013\tA\u0014GA\u0006MCjLHj\\4hS:<\u0017aC4qM\u0012L7\u000f^\"p]\u001a\u0004\"a\u000f \u000e\u0003qR!!P\u0004\u0002\u000bM\u0004\u0018M]6\n\u0005}b$aC$qM\u0012L7\u000f^\"p]\u001a\fa\u0001P5oSRtDC\u0001\"E!\t\u0019\u0005!D\u0001\u0006\u0011\u0015I$\u00011\u0001;\u00031I7\u000fV1sO\u0016$(+\u001e7f)\t9U\n\u0005\u0002I\u00176\t\u0011JC\u0001K\u0003\u0015\u00198-\u00197b\u0013\ta\u0015JA\u0004C_>dW-\u00198\t\u000b9\u001b\u0001\u0019A(\u0002\tI,H.\u001a\t\u0003=AK!!U\u0010\u0003%%swM]3tgJ+H.\u001a\"vS2$WM\u001d")
public abstract class IngressEdit
implements UnaryOperator<Ingress>,
LazyLogging {
    private final GpfdistConf gpfdistConf;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        IngressEdit ingressEdit = this;
        synchronized (ingressEdit) {
            if (!this.bitmap$trans$0) {
                this.logger = LazyLogging.logger$(this);
                this.bitmap$trans$0 = true;
            }
        }
        return this.logger;
    }

    @Override
    public Logger logger() {
        if (!this.bitmap$trans$0) {
            return this.logger$lzycompute();
        }
        return this.logger;
    }

    public boolean isTargetRule(IngressRuleBuilder rule) {
        Option option;
        Boolean ruleHasHttp = rule.hasHttp();
        if (!this.gpfdistConf.isSSL()) {
            return !Predef$.MODULE$.Boolean2boolean(rule.hasHost()) && Predef$.MODULE$.Boolean2boolean(ruleHasHttp);
        }
        Option ruleHost = Option$.MODULE$.apply((Object)rule.getHost());
        Tuple2 tuple2 = new Tuple2(this.gpfdistConf.host(), (Object)ruleHost);
        if (tuple2 != null) {
            Option option2 = (Option)tuple2._1();
            Option option3 = (Option)tuple2._2();
            if (option2 instanceof Some) {
                Some some = (Some)option2;
                String g2 = (String)some.value();
                if (option3 instanceof Some) {
                    Some some2 = (Some)option3;
                    String r = (String)some2.value();
                    String string = g2;
                    String string2 = r;
                    return !(string != null ? !string.equals(string2) : string2 != null);
                }
            }
        }
        if (tuple2 != null) {
            Option option4 = (Option)tuple2._1();
            Option option5 = (Option)tuple2._2();
            if (option4 instanceof Some && None$.MODULE$.equals(option5)) {
                return false;
            }
        }
        if (tuple2 != null && None$.MODULE$.equals(option = (Option)tuple2._1())) {
            return Predef$.MODULE$.Boolean2boolean(ruleHasHttp);
        }
        throw new MatchError((Object)tuple2);
    }

    public IngressEdit(GpfdistConf gpfdistConf) {
        this.gpfdistConf = gpfdistConf;
        LazyLogging.$init$(this);
    }
}

