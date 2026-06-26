/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath
 *  io.fabric8.kubernetes.api.model.networking.v1.Ingress
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressFluent$SpecNested
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressRuleFluent$HttpNested
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressSpecFluent$RulesNested
 *  scala.reflect.ScalaSignature
 */
package org.greenplum.k8s;

import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressFluent;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleFluent;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecFluent;
import org.greenplum.k8s.IngressEdit;
import org.greenplum.spark.GpfdistConf;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001a2A!\u0002\u0004\u0001\u001b!A!\u0003\u0001B\u0001B\u0003%1\u0003\u0003\u0005&\u0001\t\u0005\t\u0015!\u0003'\u0011\u0015a\u0003\u0001\"\u0001.\u0011\u0015\t\u0004\u0001\"\u00113\u0005I\tE\r\u001a)bi\"Len\u001a:fgN,E-\u001b;\u000b\u0005\u001dA\u0011aA69g*\u0011\u0011BC\u0001\nOJ,WM\u001c9mk6T\u0011aC\u0001\u0004_J<7\u0001A\n\u0003\u00019\u0001\"a\u0004\t\u000e\u0003\u0019I!!\u0005\u0004\u0003\u0017%swM]3tg\u0016#\u0017\u000e^\u0001\u0010QR$\b/\u00138he\u0016\u001c8\u000fU1uQB\u0011AcI\u0007\u0002+)\u0011acF\u0001\u0003mFR!\u0001G\r\u0002\u00159,Go^8sW&twM\u0003\u0002\u001b7\u0005)Qn\u001c3fY*\u0011A$H\u0001\u0004CBL'B\u0001\u0010 \u0003)YWOY3s]\u0016$Xm\u001d\u0006\u0003A\u0005\nqAZ1ce&\u001c\u0007HC\u0001#\u0003\tIw.\u0003\u0002%+\ty\u0001\n\u0016+Q\u0013:<'/Z:t!\u0006$\b.A\u0006ha\u001a$\u0017n\u001d;D_:4\u0007CA\u0014+\u001b\u0005A#BA\u0015\t\u0003\u0015\u0019\b/\u0019:l\u0013\tY\u0003FA\u0006Ha\u001a$\u0017n\u001d;D_:4\u0017A\u0002\u001fj]&$h\bF\u0002/_A\u0002\"a\u0004\u0001\t\u000bI\u0019\u0001\u0019A\n\t\u000b\u0015\u001a\u0001\u0019\u0001\u0014\u0002\u000b\u0005\u0004\b\u000f\\=\u0015\u0005M2\u0004C\u0001\u000b5\u0013\t)TCA\u0004J]\u001e\u0014Xm]:\t\u000b]\"\u0001\u0019A\u001a\u0002\u0003Q\u0004")
public class AddPathIngressEdit
extends IngressEdit {
    private final HTTPIngressPath httpIngressPath;

    @Override
    public Ingress apply(Ingress t) {
        return ((IngressBuilder)((IngressFluent.SpecNested)((IngressSpecFluent.RulesNested)((IngressRuleFluent.HttpNested)new IngressBuilder(t).editOrNewSpec().editMatchingRule(rule -> this.isTargetRule((IngressRuleBuilder)rule)).editOrNewHttp().addToPaths(new HTTPIngressPath[]{this.httpIngressPath})).endHttp()).endRule()).endSpec()).build();
    }

    public AddPathIngressEdit(HTTPIngressPath httpIngressPath, GpfdistConf gpfdistConf) {
        this.httpIngressPath = httpIngressPath;
        super(gpfdistConf);
    }
}

