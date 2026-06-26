/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder
 *  io.fabric8.kubernetes.api.model.networking.v1.Ingress
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressFluent$SpecNested
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressRuleFluent$HttpNested
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressSpecFluent$RulesNested
 *  org.apache.commons.lang3.StringUtils
 *  scala.reflect.ScalaSignature
 */
package org.greenplum.k8s;

import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressFluent;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleFluent;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecFluent;
import org.apache.commons.lang3.StringUtils;
import org.greenplum.k8s.IngressEdit;
import org.greenplum.spark.GpfdistConf;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u000193AAB\u0004\u0001\u001d!A1\u0003\u0001B\u0001B\u0003%A\u0003\u0003\u0005\"\u0001\t\u0005\t\u0015!\u0003#\u0011\u0015A\u0003\u0001\"\u0001*\u0011\u0015i\u0003\u0001\"\u0011/\u0011\u0015\u0019\u0005\u0001\"\u0003E\u0005\r\u0012V-\\8wK\u0006cG\u000eU1uQN<\u0016\u000e\u001e5Qe\u00164\u0017\u000e_%oOJ,7o]#eSRT!\u0001C\u0005\u0002\u0007-D4O\u0003\u0002\u000b\u0017\u0005IqM]3f]BdW/\u001c\u0006\u0002\u0019\u0005\u0019qN]4\u0004\u0001M\u0011\u0001a\u0004\t\u0003!Ei\u0011aB\u0005\u0003%\u001d\u00111\"\u00138he\u0016\u001c8/\u00123ji\u0006Q\u0001/\u0019;i!J,g-\u001b=\u0011\u0005UqbB\u0001\f\u001d!\t9\"$D\u0001\u0019\u0015\tIR\"\u0001\u0004=e>|GO\u0010\u0006\u00027\u0005)1oY1mC&\u0011QDG\u0001\u0007!J,G-\u001a4\n\u0005}\u0001#AB*ue&twM\u0003\u0002\u001e5\u0005Yq\r\u001d4eSN$8i\u001c8g!\t\u0019c%D\u0001%\u0015\t)\u0013\"A\u0003ta\u0006\u00148.\u0003\u0002(I\tYq\t\u001d4eSN$8i\u001c8g\u0003\u0019a\u0014N\\5u}Q\u0019!f\u000b\u0017\u0011\u0005A\u0001\u0001\"B\n\u0004\u0001\u0004!\u0002\"B\u0011\u0004\u0001\u0004\u0011\u0013!B1qa2LHCA\u0018B!\t\u0001t(D\u00012\u0015\t\u00114'\u0001\u0002wc)\u0011A'N\u0001\u000b]\u0016$xo\u001c:lS:<'B\u0001\u001c8\u0003\u0015iw\u000eZ3m\u0015\tA\u0014(A\u0002ba&T!AO\u001e\u0002\u0015-,(-\u001a:oKR,7O\u0003\u0002={\u00059a-\u00192sS\u000eD$\"\u0001 \u0002\u0005%|\u0017B\u0001!2\u0005\u001dIen\u001a:fgNDQA\u0011\u0003A\u0002=\n\u0011\u0001^\u0001\u000fSNl\u0015\r^2iS:<\u0007+\u0019;i)\t)\u0015\n\u0005\u0002G\u000f6\t!$\u0003\u0002I5\t9!i\\8mK\u0006t\u0007\"\u0002&\u0006\u0001\u0004Y\u0015aC5oOJ,7o\u001d)bi\"\u0004\"\u0001\r'\n\u00055\u000b$A\u0006%U)BKen\u001a:fgN\u0004\u0016\r\u001e5Ck&dG-\u001a:")
public class RemoveAllPathsWithPrefixIngressEdit
extends IngressEdit {
    private final String pathPrefix;

    @Override
    public Ingress apply(Ingress t) {
        return ((IngressBuilder)((IngressFluent.SpecNested)((IngressSpecFluent.RulesNested)((IngressRuleFluent.HttpNested)new IngressBuilder(t).editSpec().editMatchingRule(rule -> this.isTargetRule((IngressRuleBuilder)rule)).editHttp().removeMatchingFromPaths(ingressPath -> this.isMatchingPath((HTTPIngressPathBuilder)ingressPath))).endHttp()).endRule()).endSpec()).build();
    }

    private boolean isMatchingPath(HTTPIngressPathBuilder ingressPath) {
        return StringUtils.startsWith((CharSequence)ingressPath.getPath(), (CharSequence)this.pathPrefix);
    }

    public RemoveAllPathsWithPrefixIngressEdit(String pathPrefix, GpfdistConf gpfdistConf) {
        this.pathPrefix = pathPrefix;
        super(gpfdistConf);
    }
}

