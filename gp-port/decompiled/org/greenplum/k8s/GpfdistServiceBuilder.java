/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.api.model.IntOrString
 *  io.fabric8.kubernetes.api.model.ObjectMeta
 *  io.fabric8.kubernetes.api.model.OwnerReference
 *  io.fabric8.kubernetes.api.model.OwnerReferenceBuilder
 *  io.fabric8.kubernetes.api.model.OwnerReferenceFluentImpl
 *  io.fabric8.kubernetes.api.model.Pod
 *  io.fabric8.kubernetes.api.model.Service
 *  io.fabric8.kubernetes.api.model.ServiceBuilder
 *  io.fabric8.kubernetes.api.model.ServiceFluent$MetadataNested
 *  io.fabric8.kubernetes.api.model.ServiceFluent$SpecNested
 *  io.fabric8.kubernetes.api.model.ServiceFluentImpl
 *  io.fabric8.kubernetes.api.model.ServicePort
 *  io.fabric8.kubernetes.api.model.ServicePortBuilder
 *  io.fabric8.kubernetes.api.model.ServicePortFluentImpl
 *  scala.Function0
 *  scala.Option$
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.JavaConverters$
 *  scala.collection.TraversableOnce
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 */
package org.greenplum.k8s;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.OwnerReferenceFluentImpl;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceFluent;
import io.fabric8.kubernetes.api.model.ServiceFluentImpl;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServicePortFluentImpl;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import org.greenplum.spark.GpfdistConf;
import org.greenplum.spark.GpfdistConf$;
import scala.Function0;
import scala.Option$;
import scala.Predef$;
import scala.collection.JavaConverters$;
import scala.collection.TraversableOnce;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;

@ScalaSignature(bytes="\u0006\u0001\u00154Aa\u0003\u0007\u0001'!AA\u0005\u0001BC\u0002\u0013%Q\u0005\u0003\u00052\u0001\t\u0005\t\u0015!\u0003'\u0011!\u0011\u0004A!A!\u0002\u0013\u0019\u0004\"B\u001d\u0001\t\u0003Q\u0004bB \u0001\u0005\u0004%I\u0001\u0011\u0005\u0007\t\u0002\u0001\u000b\u0011B!\t\u000f\u0015\u0003!\u0019!C\u0005\r\"1Q\u000b\u0001Q\u0001\n\u001dCQA\u0016\u0001\u0005\u0002]CQA\u0019\u0001\u0005\n\r\u0014Qc\u00129gI&\u001cHoU3sm&\u001cWMQ;jY\u0012,'O\u0003\u0002\u000e\u001d\u0005\u00191\u000eO:\u000b\u0005=\u0001\u0012!C4sK\u0016t\u0007\u000f\\;n\u0015\u0005\t\u0012aA8sO\u000e\u00011c\u0001\u0001\u00155A\u0011Q\u0003G\u0007\u0002-)\tq#A\u0003tG\u0006d\u0017-\u0003\u0002\u001a-\t1\u0011I\\=SK\u001a\u0004\"a\u0007\u0012\u000e\u0003qQ!!\b\u0010\u0002\u0019M\u001c\u0017\r\\1m_\u001e<\u0017N\\4\u000b\u0005}\u0001\u0013\u0001\u0003;za\u0016\u001c\u0018MZ3\u000b\u0003\u0005\n1aY8n\u0013\t\u0019CDA\u0006MCjLHj\\4hS:<\u0017!F3yK\u000e,Ho\u001c:Q_\u0012t\u0015-\\3Qe\u00164\u0017\u000e_\u000b\u0002MA\u0011qE\f\b\u0003Q1\u0002\"!\u000b\f\u000e\u0003)R!a\u000b\n\u0002\rq\u0012xn\u001c;?\u0013\tic#\u0001\u0004Qe\u0016$WMZ\u0005\u0003_A\u0012aa\u0015;sS:<'BA\u0017\u0017\u0003Y)\u00070Z2vi>\u0014\bk\u001c3OC6,\u0007K]3gSb\u0004\u0013aC4qM\u0012L7\u000f^\"p]\u001a\u0004\"\u0001N\u001c\u000e\u0003UR!A\u000e\b\u0002\u000bM\u0004\u0018M]6\n\u0005a*$aC$qM\u0012L7\u000f^\"p]\u001a\fa\u0001P5oSRtDcA\u001e>}A\u0011A\bA\u0007\u0002\u0019!)A\u0005\u0002a\u0001M!)!\u0007\u0002a\u0001g\u0005\tr\r\u001d4eSN$H*[:uK:\u0004vN\u001d;\u0016\u0003\u0005\u0003\"!\u0006\"\n\u0005\r3\"aA%oi\u0006\u0011r\r\u001d4eSN$H*[:uK:\u0004vN\u001d;!\u0003I9\u0007O\u001a3jgR\u001cVM\u001d<jG\u0016\u0004vN\u001d;\u0016\u0003\u001d\u0003\"\u0001S*\u000e\u0003%S!AS&\u0002\u000b5|G-\u001a7\u000b\u00051k\u0015aA1qS*\u0011ajT\u0001\u000bWV\u0014WM\u001d8fi\u0016\u001c(B\u0001)R\u0003\u001d1\u0017M\u0019:jGbR\u0011AU\u0001\u0003S>L!\u0001V%\u0003\u0017M+'O^5dKB{'\u000f^\u0001\u0014OB4G-[:u'\u0016\u0014h/[2f!>\u0014H\u000fI\u0001\u001fEVLG\u000eZ$qM\u0012L7\u000f^*feZL7-\u001a$pe\u0016CXmY;u_J$2\u0001W.^!\tA\u0015,\u0003\u0002[\u0013\n91+\u001a:wS\u000e,\u0007\"\u0002/\n\u0001\u00041\u0013AB3yK\u000eLE\rC\u0003_\u0013\u0001\u0007q,A\u0006fq\u0016\u001cW\u000f^8s!>$\u0007C\u0001%a\u0013\t\t\u0017JA\u0002Q_\u0012\fAc]3sm&\u001cWMT1nK\u001a{'/\u0012=fG&#GC\u0001\u0014e\u0011\u0015a&\u00021\u0001'\u0001")
public class GpfdistServiceBuilder
implements LazyLogging {
    private final String executorPodNamePrefix;
    private final int gpfdistListenPort;
    private final ServicePort gpfdistServicePort;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        GpfdistServiceBuilder gpfdistServiceBuilder = this;
        synchronized (gpfdistServiceBuilder) {
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

    private String executorPodNamePrefix() {
        return this.executorPodNamePrefix;
    }

    private int gpfdistListenPort() {
        return this.gpfdistListenPort;
    }

    private ServicePort gpfdistServicePort() {
        return this.gpfdistServicePort;
    }

    public Service buildGpfdistServiceForExecutor(String execId, Pod executorPod) {
        BoxedUnit boxedUnit;
        BoxedUnit boxedUnit2;
        BoxedUnit boxedUnit3;
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("building Kubernetes Service targeting gpfdist server running on executor {}", new Object[]{execId});
            boxedUnit3 = BoxedUnit.UNIT;
        } else {
            boxedUnit3 = BoxedUnit.UNIT;
        }
        ObjectMeta executorPodMetadata = (ObjectMeta)Option$.MODULE$.apply((Object)executorPod.getMetadata()).getOrElse((Function0 & Serializable & scala.Serializable)() -> {
            throw new NoSuchElementException(new StringBuilder(33).append("Pod for executor ").append(execId).append(" has no metadata").toString());
        });
        Map executorPodSelector = (Map)Option$.MODULE$.apply((Object)executorPodMetadata.getLabels()).getOrElse((Function0 & Serializable & scala.Serializable)() -> {
            throw new NoSuchElementException(new StringBuilder(31).append("Pod for executor ").append(execId).append(" has no labels").toString());
        });
        String executorPodApiVersion = executorPod.getApiVersion();
        String executorPodKind = executorPod.getKind();
        String executorPodName = executorPodMetadata.getName();
        String executorPodUid = executorPodMetadata.getUid();
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("building owner reference with apiVersion={}, kind={}, name={}, and uid={}", executorPodApiVersion, executorPodKind, executorPodName, executorPodUid);
            boxedUnit2 = BoxedUnit.UNIT;
        } else {
            boxedUnit2 = BoxedUnit.UNIT;
        }
        OwnerReference executorPodOwnerReference = ((OwnerReferenceBuilder)((OwnerReferenceFluentImpl)((OwnerReferenceFluentImpl)((OwnerReferenceFluentImpl)new OwnerReferenceBuilder().withApiVersion(executorPodApiVersion)).withKind(executorPodKind)).withName(executorPodName)).withUid(executorPodUid)).build();
        String serviceName = this.serviceNameForExecId(execId);
        String selectorDesc = ((TraversableOnce)JavaConverters$.MODULE$.mapAsScalaMapConverter(executorPodSelector).asScala()).mkString("{ ", ", ", " }");
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug("building service with name={}, selector={}, and port={}", serviceName, selectorDesc, BoxesRunTime.boxToInteger((int)this.gpfdistListenPort()));
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return ((ServiceBuilder)((ServiceFluent.SpecNested)((ServiceFluentImpl)((ServiceFluent.MetadataNested)new ServiceBuilder().withNewMetadata().withName(serviceName).withOwnerReferences(new OwnerReference[]{executorPodOwnerReference})).endMetadata()).withNewSpec().withSelector(executorPodSelector).withPorts(new ServicePort[]{this.gpfdistServicePort()}).withType("ClusterIP")).endSpec()).build();
    }

    private String serviceNameForExecId(String execId) {
        return new StringBuilder(13).append(this.executorPodNamePrefix()).append("-gpfdist-svc-").append(execId).toString();
    }

    public GpfdistServiceBuilder(String executorPodNamePrefix, GpfdistConf gpfdistConf) {
        this.executorPodNamePrefix = executorPodNamePrefix;
        LazyLogging.$init$(this);
        this.gpfdistListenPort = gpfdistConf.listenPort();
        Predef$.MODULE$.require(this.gpfdistListenPort() != 0, (Function0 & Serializable & scala.Serializable)() -> new StringBuilder(39).append(GpfdistConf$.MODULE$.LISTEN_PORT_KEY()).append(" should be a positive integer, but was ").append(this.gpfdistListenPort()).toString());
        this.gpfdistServicePort = ((ServicePortBuilder)((ServicePortFluentImpl)((ServicePortFluentImpl)new ServicePortBuilder().withProtocol("TCP")).withPort(Predef$.MODULE$.int2Integer(this.gpfdistListenPort()))).withTargetPort(new IntOrString(Predef$.MODULE$.int2Integer(this.gpfdistListenPort())))).build();
    }
}

