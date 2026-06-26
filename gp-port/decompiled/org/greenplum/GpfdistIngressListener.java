/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.api.model.Pod
 *  io.fabric8.kubernetes.api.model.Service
 *  io.fabric8.kubernetes.api.model.networking.v1.Ingress
 *  io.fabric8.kubernetes.client.KubernetesClient
 *  io.fabric8.kubernetes.client.dsl.Createable
 *  io.fabric8.kubernetes.client.dsl.Editable
 *  io.fabric8.kubernetes.client.dsl.Gettable
 *  io.fabric8.kubernetes.client.dsl.Nameable
 *  org.apache.spark.SparkConf
 *  org.apache.spark.scheduler.SparkListener
 *  org.apache.spark.scheduler.SparkListenerApplicationEnd
 *  org.apache.spark.scheduler.SparkListenerApplicationStart
 *  org.apache.spark.scheduler.SparkListenerExecutorAdded
 *  org.apache.spark.scheduler.SparkListenerExecutorRemoved
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.Predef$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.JavaConverters$
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.StringOps
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.control.NonFatal$
 */
package org.greenplum;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Createable;
import io.fabric8.kubernetes.client.dsl.Editable;
import io.fabric8.kubernetes.client.dsl.Gettable;
import io.fabric8.kubernetes.client.dsl.Nameable;
import java.io.Serializable;
import java.util.function.UnaryOperator;
import org.apache.spark.SparkConf;
import org.apache.spark.scheduler.SparkListener;
import org.apache.spark.scheduler.SparkListenerApplicationEnd;
import org.apache.spark.scheduler.SparkListenerApplicationStart;
import org.apache.spark.scheduler.SparkListenerExecutorAdded;
import org.apache.spark.scheduler.SparkListenerExecutorRemoved;
import org.greenplum.GpfdistIngressListener$;
import org.greenplum.k8s.GpfdistIngressEditor;
import org.greenplum.k8s.GpfdistServiceBuilder;
import org.greenplum.k8s.IngressConf;
import org.greenplum.k8s.IngressEdit;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Predef$;
import scala.Some;
import scala.collection.JavaConverters$;
import scala.collection.TraversableOnce;
import scala.collection.immutable.StringOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.control.NonFatal$;

@ScalaSignature(bytes="\u0006\u0001\u0005uh\u0001B\u0016-\u0001EB\u0001B\u0012\u0001\u0003\u0006\u0004%Ia\u0012\u0005\t\u0019\u0002\u0011\t\u0011)A\u0005\u0011\"AQ\n\u0001BC\u0002\u0013%a\n\u0003\u0005\\\u0001\t\u0005\t\u0015!\u0003P\u0011!a\u0006A!b\u0001\n\u0013i\u0006\u0002\u00033\u0001\u0005\u0003\u0005\u000b\u0011\u00020\t\u0011\u0015\u0004!Q1A\u0005\n\u0019D\u0001B\u001b\u0001\u0003\u0002\u0003\u0006Ia\u001a\u0005\tW\u0002\u0011)\u0019!C\u0005Y\"A\u0001\u000f\u0001B\u0001B\u0003%Q\u000eC\u0003r\u0001\u0011\u0005!\u000fC\u0003r\u0001\u0011\u0005!\u0010C\u0004}\u0001\t\u0007I\u0011B?\t\u000f\u0005]\u0001\u0001)A\u0005}\"A\u0011\u0011\u0004\u0001C\u0002\u0013%Q\u0010C\u0004\u0002\u001c\u0001\u0001\u000b\u0011\u0002@\t\u0011\u0005u\u0001A1A\u0005\nuDq!a\b\u0001A\u0003%a\u0010C\u0004\u0002\"\u0001!\t%a\t\t\u000f\u0005]\u0002\u0001\"\u0011\u0002:!9\u0011Q\t\u0001\u0005B\u0005\u001d\u0003bBA*\u0001\u0011\u0005\u0013Q\u000b\u0005\b\u0003C\u0002A\u0011BA2\u0011\u001d\tI\b\u0001C\u0005\u0003wBq!!\"\u0001\t\u0013\t9iB\u0004\u0002$2B\t!!*\u0007\r-b\u0003\u0012AAT\u0011\u0019\t8\u0004\"\u0001\u00020\"9\u0011\u0011W\u000e\u0005\n\u0005M\u0006bBA_7\u0011%\u0011q\u0018\u0005\b\u0003\u0007\\B\u0011BAc\u0011\u001d\tIm\u0007C\u0005\u0003\u0017Dq!!7\u001c\t\u0013\tY\u000eC\u0004\u0002`n!I!!9\t\u000f\u0005\u00158\u0004\"\u0003\u0002h\"9\u00111^\u000e\u0005\n\u00055\b\u0002CAy7\t\u0007I\u0011B?\t\u000f\u0005M8\u0004)A\u0005}\"A\u0011Q_\u000eC\u0002\u0013%Q\u0010C\u0004\u0002xn\u0001\u000b\u0011\u0002@\t\u0011\u0005e8D1A\u0005\nuDq!a?\u001cA\u0003%aP\u0001\fHa\u001a$\u0017n\u001d;J]\u001e\u0014Xm]:MSN$XM\\3s\u0015\tic&A\u0005he\u0016,g\u000e\u001d7v[*\tq&A\u0002pe\u001e\u001c\u0001aE\u0002\u0001eq\u0002\"a\r\u001e\u000e\u0003QR!!\u000e\u001c\u0002\u0013M\u001c\u0007.\u001a3vY\u0016\u0014(BA\u001c9\u0003\u0015\u0019\b/\u0019:l\u0015\tId&\u0001\u0004ba\u0006\u001c\u0007.Z\u0005\u0003wQ\u0012Qb\u00159be.d\u0015n\u001d;f]\u0016\u0014\bCA\u001fE\u001b\u0005q$BA A\u00031\u00198-\u00197bY><w-\u001b8h\u0015\t\t%)\u0001\u0005usB,7/\u00194f\u0015\u0005\u0019\u0015aA2p[&\u0011QI\u0010\u0002\f\u0019\u0006T\u0018\u0010T8hO&tw-A\u0005ta\u0006\u00148nQ8oMV\t\u0001\n\u0005\u0002J\u00156\ta'\u0003\u0002Lm\tI1\u000b]1sW\u000e{gNZ\u0001\u000bgB\f'o[\"p]\u001a\u0004\u0013\u0001E6vE\u0016\u0014h.\u001a;fg\u000ec\u0017.\u001a8u+\u0005y\u0005C\u0001)Z\u001b\u0005\t&B\u0001*T\u0003\u0019\u0019G.[3oi*\u0011A+V\u0001\u000bWV\u0014WM\u001d8fi\u0016\u001c(B\u0001,X\u0003\u001d1\u0017M\u0019:jGbR\u0011\u0001W\u0001\u0003S>L!AW)\u0003!-+(-\u001a:oKR,7o\u00117jK:$\u0018!E6vE\u0016\u0014h.\u001a;fg\u000ec\u0017.\u001a8uA\u0005)r\r\u001d4eSN$8+\u001a:wS\u000e,')^5mI\u0016\u0014X#\u00010\u0011\u0005}\u0013W\"\u00011\u000b\u0005\u0005d\u0013aA69g&\u00111\r\u0019\u0002\u0016\u000fB4G-[:u'\u0016\u0014h/[2f\u0005VLG\u000eZ3s\u0003Y9\u0007O\u001a3jgR\u001cVM\u001d<jG\u0016\u0014U/\u001b7eKJ\u0004\u0013aC5oOJ,7o]\"p]\u001a,\u0012a\u001a\t\u0003?\"L!!\u001b1\u0003\u0017%swM]3tg\u000e{gNZ\u0001\rS:<'/Z:t\u0007>tg\rI\u0001\u0015OB4G-[:u\u0013:<'/Z:t\u000b\u0012LGo\u001c:\u0016\u00035\u0004\"a\u00188\n\u0005=\u0004'\u0001F$qM\u0012L7\u000f^%oOJ,7o]#eSR|'/A\u000bha\u001a$\u0017n\u001d;J]\u001e\u0014Xm]:FI&$xN\u001d\u0011\u0002\rqJg.\u001b;?)\u0019\u0019XO^<ysB\u0011A\u000fA\u0007\u0002Y!)ai\u0003a\u0001\u0011\")Qj\u0003a\u0001\u001f\")Al\u0003a\u0001=\")Qm\u0003a\u0001O\")1n\u0003a\u0001[R\u00111o\u001f\u0005\u0006\r2\u0001\r\u0001S\u0001\u0006CB\u0004\u0018\nZ\u000b\u0002}B\u0019q0!\u0005\u000f\t\u0005\u0005\u0011Q\u0002\t\u0005\u0003\u0007\tI!\u0004\u0002\u0002\u0006)\u0019\u0011q\u0001\u0019\u0002\rq\u0012xn\u001c;?\u0015\t\tY!A\u0003tG\u0006d\u0017-\u0003\u0003\u0002\u0010\u0005%\u0011A\u0002)sK\u0012,g-\u0003\u0003\u0002\u0014\u0005U!AB*ue&twM\u0003\u0003\u0002\u0010\u0005%\u0011AB1qa&#\u0007%A\u0005oC6,7\u000f]1dK\u0006Qa.Y7fgB\f7-\u001a\u0011\u0002+\u0015DXmY;u_J\u0004v\u000e\u001a(b[\u0016\u0004&/\u001a4jq\u00061R\r_3dkR|'\u000fU8e\u001d\u0006lW\r\u0015:fM&D\b%\u0001\np]\u0006\u0003\b\u000f\\5dCRLwN\\*uCJ$H\u0003BA\u0013\u0003[\u0001B!a\n\u0002*5\u0011\u0011\u0011B\u0005\u0005\u0003W\tIA\u0001\u0003V]&$\bbBA\u0018'\u0001\u0007\u0011\u0011G\u0001\u0011CB\u0004H.[2bi&|gn\u0015;beR\u00042aMA\u001a\u0013\r\t)\u0004\u000e\u0002\u001e'B\f'o\u001b'jgR,g.\u001a:BaBd\u0017nY1uS>t7\u000b^1si\u0006yqN\\#yK\u000e,Ho\u001c:BI\u0012,G\r\u0006\u0003\u0002&\u0005m\u0002bBA\u001f)\u0001\u0007\u0011qH\u0001\u000eKb,7-\u001e;pe\u0006#G-\u001a3\u0011\u0007M\n\t%C\u0002\u0002DQ\u0012!d\u00159be.d\u0015n\u001d;f]\u0016\u0014X\t_3dkR|'/\u00113eK\u0012\f\u0011c\u001c8Fq\u0016\u001cW\u000f^8s%\u0016lwN^3e)\u0011\t)#!\u0013\t\u000f\u0005-S\u00031\u0001\u0002N\u0005yQ\r_3dkR|'OU3n_Z,G\rE\u00024\u0003\u001fJ1!!\u00155\u0005q\u0019\u0006/\u0019:l\u0019&\u001cH/\u001a8fe\u0016CXmY;u_J\u0014V-\\8wK\u0012\f\u0001c\u001c8BaBd\u0017nY1uS>tWI\u001c3\u0015\t\u0005\u0015\u0012q\u000b\u0005\b\u000332\u0002\u0019AA.\u00039\t\u0007\u000f\u001d7jG\u0006$\u0018n\u001c8F]\u0012\u00042aMA/\u0013\r\ty\u0006\u000e\u0002\u001c'B\f'o\u001b'jgR,g.\u001a:BaBd\u0017nY1uS>tWI\u001c3\u0002%\u001d,G/\u0012=fGV$xN\u001d)pI\nK\u0018\n\u001a\u000b\u0005\u0003K\n)\b\u0005\u0003\u0002h\u0005ETBAA5\u0015\u0011\tY'!\u001c\u0002\u000b5|G-\u001a7\u000b\u0007\u0005=4+A\u0002ba&LA!a\u001d\u0002j\t\u0019\u0001k\u001c3\t\r\u0005]t\u00031\u0001\u007f\u0003\u0019)\u00070Z2JI\u0006y2M]3bi\u0016<\u0005O\u001a3jgR\u001cVM\u001d<jG\u00164uN]#yK\u000e,Ho\u001c:\u0015\t\u0005u\u00141\u0011\t\u0005\u0003O\ny(\u0003\u0003\u0002\u0002\u0006%$aB*feZL7-\u001a\u0005\u0007\u0003oB\u0002\u0019\u0001@\u0002\u0017\u0015$\u0017\u000e^%oOJ,7o\u001d\u000b\u0005\u0003\u0013\u000bI\n\u0005\u0003\u0002\f\u0006UUBAAG\u0015\u0011\ty)!%\u0002\u0005Y\f$\u0002BAJ\u0003S\n!B\\3uo>\u00148.\u001b8h\u0013\u0011\t9*!$\u0003\u000f%swM]3tg\"9\u00111T\rA\u0002\u0005u\u0015AB3eSR|'\u000fE\u0002`\u0003?K1!!)a\u0005-Ien\u001a:fgN,E-\u001b;\u0002-\u001d\u0003h\rZ5ti&swM]3tg2K7\u000f^3oKJ\u0004\"\u0001^\u000e\u0014\tm\tI\u000b\u0010\t\u0005\u0003O\tY+\u0003\u0003\u0002.\u0006%!AB!osJ+g\r\u0006\u0002\u0002&\u0006Iq-\u001a;D_:4\u0017n\u001a\u000b\u0005\u0003k\u000bY\fE\u0002Q\u0003oK1!!/R\u0005\u0019\u0019uN\u001c4jO\")a)\ba\u0001\u0011\u0006Ar-\u001a;Fq\u0016\u001cW\u000f^8s!>$g*Y7f!J,g-\u001b=\u0015\u0007y\f\t\rC\u0003G=\u0001\u0007\u0001*\u0001\u0007hKRt\u0015-\\3ta\u0006\u001cW\rF\u0002\u007f\u0003\u000fDQAR\u0010A\u0002!\u000babZ3u\u000fB4G-[:u\u0007>tg\r\u0006\u0003\u0002N\u0006]\u0007\u0003BAh\u0003'l!!!5\u000b\u0005]b\u0013\u0002BAk\u0003#\u00141b\u00129gI&\u001cHoQ8oM\")a\t\ta\u0001\u0011\u0006qq-\u001a;J]\u001e\u0014Xm]:D_:4GcA4\u0002^\")a)\ta\u0001\u0011\u0006\u0019r-\u001a;Lk\n,'O\\3uKN\u001cE.[3oiR\u0019q*a9\t\u000b\u0019\u0013\u0003\u0019\u0001%\u00021\u001d,Go\u00129gI&\u001cHoU3sm&\u001cWMQ;jY\u0012,'\u000fF\u0002_\u0003SDQAR\u0012A\u0002!\u000bqcZ3u\u000fB4G-[:u\u0013:<'/Z:t\u000b\u0012LGo\u001c:\u0015\u00075\fy\u000fC\u0003GI\u0001\u0007\u0001*\u0001\u000fF1\u0016\u001bU\u000bV(S?B{Ei\u0018(B\u001b\u0016{\u0006KU#G\u0013b{6*R-\u0002;\u0015CViQ+U\u001fJ{\u0006k\u0014#`\u001d\u0006kUi\u0018)S\u000b\u001aK\u0005lX&F3\u0002\nad\u0015)B%.{6*\u0016\"F%:+E+R*`\u001d\u0006kUi\u0015)B\u0007\u0016{6*R-\u0002?M\u0003\u0016IU&`\u0017V\u0013UI\u0015(F)\u0016\u001bvLT!N\u000bN\u0003\u0016iQ#`\u0017\u0016K\u0006%\u0001\u0012T!\u0006\u00136jX&V\u0005\u0016\u0013f*\u0012+F'~#UIR!V\u0019R{f*Q'F'B\u000b5)R\u0001$'B\u000b%kS0L+\n+%KT#U\u000bN{F)\u0012$B+2#vLT!N\u000bN\u0003\u0016iQ#!\u0001")
public class GpfdistIngressListener
extends SparkListener
implements LazyLogging {
    private final SparkConf sparkConf;
    private final KubernetesClient kubernetesClient;
    private final GpfdistServiceBuilder gpfdistServiceBuilder;
    private final IngressConf ingressConf;
    private final GpfdistIngressEditor gpfdistIngressEditor;
    private final String appId;
    private final String namespace;
    private final String executorPodNamePrefix;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        GpfdistIngressListener gpfdistIngressListener = this;
        synchronized (gpfdistIngressListener) {
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

    private SparkConf sparkConf() {
        return this.sparkConf;
    }

    private KubernetesClient kubernetesClient() {
        return this.kubernetesClient;
    }

    private GpfdistServiceBuilder gpfdistServiceBuilder() {
        return this.gpfdistServiceBuilder;
    }

    private IngressConf ingressConf() {
        return this.ingressConf;
    }

    private GpfdistIngressEditor gpfdistIngressEditor() {
        return this.gpfdistIngressEditor;
    }

    private String appId() {
        return this.appId;
    }

    private String namespace() {
        return this.namespace;
    }

    private String executorPodNamePrefix() {
        return this.executorPodNamePrefix;
    }

    public void onApplicationStart(SparkListenerApplicationStart applicationStart) {
        BoxedUnit boxedUnit;
        Ingress ingress = (Ingress)((Gettable)((Nameable)this.kubernetesClient().network().v1().ingresses().inNamespace(this.namespace())).withName(this.ingressConf().name())).get();
        Option loadBalancerIp = Option$.MODULE$.apply((Object)ingress.getStatus()).flatMap((Function1 & Serializable & scala.Serializable)status -> Option$.MODULE$.apply((Object)status.getLoadBalancer())).flatMap((Function1 & Serializable & scala.Serializable)loadBalancer -> Option$.MODULE$.apply((Object)loadBalancer.getIngress()).map((Function1 & Serializable & scala.Serializable)x$1 -> ((TraversableOnce)JavaConverters$.MODULE$.asScalaBufferConverter(x$1).asScala()).toList())).flatMap((Function1 & Serializable & scala.Serializable)x$2 -> x$2.headOption()).flatMap((Function1 & Serializable & scala.Serializable)ip -> Option$.MODULE$.apply((Object)ip.getIp())).filter((Function1 & Serializable & scala.Serializable)x$3 -> BoxesRunTime.boxToBoolean((boolean)GpfdistIngressListener.$anonfun$onApplicationStart$6(x$3)));
        Option ingressTls = Option$.MODULE$.apply((Object)ingress.getSpec()).flatMap((Function1 & Serializable & scala.Serializable)spec -> Option$.MODULE$.apply((Object)spec.getTls()).map((Function1 & Serializable & scala.Serializable)x$4 -> ((TraversableOnce)JavaConverters$.MODULE$.asScalaBufferConverter(x$4).asScala()).toList())).flatMap((Function1 & Serializable & scala.Serializable)x$5 -> x$5.headOption());
        if (loadBalancerIp.isDefined() && this.ingressConf().useLoadBalancerIp()) {
            BoxedUnit boxedUnit2;
            if (this.logger().underlying().isInfoEnabled()) {
                this.logger().underlying().info("Ingress with name {} has a load balancer with IP address {}", new Object[]{this.ingressConf().name(), loadBalancerIp});
                boxedUnit2 = BoxedUnit.UNIT;
            } else {
                boxedUnit2 = BoxedUnit.UNIT;
            }
            boxedUnit = this.sparkConf().set("spark.greenplum.gpfdist.host", (String)loadBalancerIp.get());
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        if (this.ingressConf().updateLocationPort()) {
            int n;
            Option option = ingressTls;
            if (option instanceof Some) {
                n = 443;
            } else if (None$.MODULE$.equals(option)) {
                n = 80;
            } else {
                throw new MatchError((Object)option);
            }
            int locationPort = n;
            this.sparkConf().set("spark.greenplum.gpfdist.location-port", Integer.toString(locationPort));
            return;
        }
    }

    public void onExecutorAdded(SparkListenerExecutorAdded executorAdded) {
        BoxedUnit boxedUnit;
        String executorId = executorAdded.executorId();
        if (this.logger().underlying().isInfoEnabled()) {
            this.logger().underlying().info("executor {} added", new Object[]{executorId});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        Service executorGpfdistService = this.createGpfdistServiceForExecutor(executorId);
        this.editIngress(this.gpfdistIngressEditor().addPath(executorId, executorGpfdistService));
    }

    public void onExecutorRemoved(SparkListenerExecutorRemoved executorRemoved) {
        BoxedUnit boxedUnit;
        String executorId = executorRemoved.executorId();
        if (this.logger().underlying().isInfoEnabled()) {
            this.logger().underlying().info("executor {} removed", new Object[]{executorId});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        if (this.ingressConf().isRemovePaths()) {
            this.editIngress(this.gpfdistIngressEditor().removePathForExecutor(executorId));
            return;
        }
    }

    public void onApplicationEnd(SparkListenerApplicationEnd applicationEnd) {
        BoxedUnit boxedUnit;
        if (this.logger().underlying().isInfoEnabled()) {
            this.logger().underlying().info("application {} ended", new Object[]{this.appId()});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        Object object = this.ingressConf().isRemovePaths() ? this.editIngress(this.gpfdistIngressEditor().removeAllPathsForApplication()) : BoxedUnit.UNIT;
        try {
            BoxedUnit boxedUnit2;
            BoxedUnit boxedUnit3;
            if (this.logger().underlying().isInfoEnabled()) {
                this.logger().underlying().info("closing Kubernetes client...");
                boxedUnit3 = BoxedUnit.UNIT;
            } else {
                boxedUnit3 = BoxedUnit.UNIT;
            }
            this.kubernetesClient().close();
            if (this.logger().underlying().isInfoEnabled()) {
                this.logger().underlying().info("Kubernetes client has been closed.");
                boxedUnit2 = BoxedUnit.UNIT;
            } else {
                boxedUnit2 = BoxedUnit.UNIT;
            }
        }
        catch (Throwable throwable) {
            Throwable throwable2 = throwable;
            Option option = NonFatal$.MODULE$.unapply(throwable2);
            if (!option.isEmpty()) {
                Throwable e = (Throwable)option.get();
                if (this.logger().underlying().isWarnEnabled()) {
                    this.logger().underlying().warn("an error occurred while closing the Kubernetes client: {}", new Object[]{e.getMessage()});
                }
            }
            throw throwable;
        }
    }

    private Pod getExecutorPodById(String execId) {
        String name = new StringBuilder(6).append(this.executorPodNamePrefix()).append("-exec-").append(execId).toString();
        return (Pod)((Gettable)((Nameable)this.kubernetesClient().pods().inNamespace(this.namespace())).withName(name)).get();
    }

    private Service createGpfdistServiceForExecutor(String execId) {
        Pod executorPod = this.getExecutorPodById(execId);
        Service gpfdistService = this.gpfdistServiceBuilder().buildGpfdistServiceForExecutor(execId, executorPod);
        return (Service)((Createable)this.kubernetesClient().services().inNamespace(this.namespace())).create((Object)gpfdistService);
    }

    private Ingress editIngress(IngressEdit editor) {
        return (Ingress)((Editable)((Nameable)this.kubernetesClient().network().v1().ingresses().inNamespace(this.namespace())).withName(this.ingressConf().name())).edit((UnaryOperator)editor);
    }

    public static final /* synthetic */ boolean $anonfun$onApplicationStart$6(String x$3) {
        return new StringOps(Predef$.MODULE$.augmentString(x$3)).nonEmpty();
    }

    public GpfdistIngressListener(SparkConf sparkConf, KubernetesClient kubernetesClient, GpfdistServiceBuilder gpfdistServiceBuilder, IngressConf ingressConf, GpfdistIngressEditor gpfdistIngressEditor) {
        this.sparkConf = sparkConf;
        this.kubernetesClient = kubernetesClient;
        this.gpfdistServiceBuilder = gpfdistServiceBuilder;
        this.ingressConf = ingressConf;
        this.gpfdistIngressEditor = gpfdistIngressEditor;
        LazyLogging.$init$(this);
        Predef$.MODULE$.require(kubernetesClient != null, (Function0 & Serializable & scala.Serializable)() -> "Kubernetes client is not available. Please check your Kubernetes client with kubectl and retry.");
        this.appId = sparkConf.getAppId();
        this.namespace = GpfdistIngressListener$.MODULE$.org$greenplum$GpfdistIngressListener$$getNamespace(sparkConf);
        this.executorPodNamePrefix = GpfdistIngressListener$.MODULE$.org$greenplum$GpfdistIngressListener$$getExecutorPodNamePrefix(sparkConf);
    }

    public GpfdistIngressListener(SparkConf sparkConf) {
        this(sparkConf, GpfdistIngressListener$.MODULE$.org$greenplum$GpfdistIngressListener$$getKubernetesClient(sparkConf), GpfdistIngressListener$.MODULE$.org$greenplum$GpfdistIngressListener$$getGpfdistServiceBuilder(sparkConf), GpfdistIngressListener$.MODULE$.org$greenplum$GpfdistIngressListener$$getIngressConf(sparkConf), GpfdistIngressListener$.MODULE$.org$greenplum$GpfdistIngressListener$$getGpfdistIngressEditor(sparkConf));
    }
}

