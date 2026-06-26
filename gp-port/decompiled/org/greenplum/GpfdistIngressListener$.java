/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.client.Config
 *  io.fabric8.kubernetes.client.ConfigBuilder
 *  io.fabric8.kubernetes.client.DefaultKubernetesClient
 *  io.fabric8.kubernetes.client.KubernetesClient
 *  org.apache.spark.SparkConf
 *  scala.Function0
 *  scala.Function1
 *  scala.Option
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.immutable.StringOps
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 */
package org.greenplum;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.Serializable;
import org.apache.spark.SparkConf;
import org.greenplum.k8s.GpfdistIngressEditor;
import org.greenplum.k8s.GpfdistServiceBuilder;
import org.greenplum.k8s.IngressConf;
import org.greenplum.spark.GpfdistConf;
import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.Predef$;
import scala.collection.immutable.StringOps;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;

public final class GpfdistIngressListener$
implements LazyLogging {
    public static GpfdistIngressListener$ MODULE$;
    private final String EXECUTOR_POD_NAME_PREFIX_KEY;
    private final String SPARK_KUBERNETES_NAMESPACE_KEY;
    private final String SPARK_KUBERNETES_DEFAULT_NAMESPACE;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new GpfdistIngressListener$();
    }

    private Logger logger$lzycompute() {
        GpfdistIngressListener$ gpfdistIngressListener$ = this;
        synchronized (gpfdistIngressListener$) {
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

    private Config getConfig(SparkConf sparkConf) {
        BoxedUnit boxedUnit;
        Option kubeContext = sparkConf.getOption("spark.kubernetes.context").filter((Function1 & Serializable & scala.Serializable)x$6 -> BoxesRunTime.boxToBoolean((boolean)GpfdistIngressListener$.$anonfun$getConfig$1(x$6)));
        String kubeContextDesc = (String)kubeContext.map((Function1 & Serializable & scala.Serializable)ctx -> new StringBuilder(8).append("context ").append((String)ctx).toString()).getOrElse((Function0 & Serializable & scala.Serializable)() -> "current context");
        if (this.logger().underlying().isInfoEnabled()) {
            this.logger().underlying().info("Auto-configuring K8S client using {} from users K8S config file", new Object[]{kubeContextDesc});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return new ConfigBuilder(Config.autoConfigure((String)((String)kubeContext.orNull(Predef$.MODULE$.$conforms())))).build();
    }

    public String org$greenplum$GpfdistIngressListener$$getExecutorPodNamePrefix(SparkConf sparkConf) {
        return sparkConf.get(this.EXECUTOR_POD_NAME_PREFIX_KEY());
    }

    public String org$greenplum$GpfdistIngressListener$$getNamespace(SparkConf sparkConf) {
        return (String)sparkConf.getOption(this.SPARK_KUBERNETES_NAMESPACE_KEY()).getOrElse((Function0 & Serializable & scala.Serializable)() -> MODULE$.SPARK_KUBERNETES_DEFAULT_NAMESPACE());
    }

    private GpfdistConf getGpfdistConf(SparkConf sparkConf) {
        return new GpfdistConf(sparkConf);
    }

    public IngressConf org$greenplum$GpfdistIngressListener$$getIngressConf(SparkConf sparkConf) {
        return new IngressConf(sparkConf);
    }

    public KubernetesClient org$greenplum$GpfdistIngressListener$$getKubernetesClient(SparkConf sparkConf) {
        return new DefaultKubernetesClient(this.getConfig(sparkConf));
    }

    public GpfdistServiceBuilder org$greenplum$GpfdistIngressListener$$getGpfdistServiceBuilder(SparkConf sparkConf) {
        return new GpfdistServiceBuilder(this.org$greenplum$GpfdistIngressListener$$getExecutorPodNamePrefix(sparkConf), this.getGpfdistConf(sparkConf));
    }

    public GpfdistIngressEditor org$greenplum$GpfdistIngressListener$$getGpfdistIngressEditor(SparkConf sparkConf) {
        return new GpfdistIngressEditor(sparkConf.getAppId(), this.getGpfdistConf(sparkConf));
    }

    private String EXECUTOR_POD_NAME_PREFIX_KEY() {
        return this.EXECUTOR_POD_NAME_PREFIX_KEY;
    }

    private String SPARK_KUBERNETES_NAMESPACE_KEY() {
        return this.SPARK_KUBERNETES_NAMESPACE_KEY;
    }

    private String SPARK_KUBERNETES_DEFAULT_NAMESPACE() {
        return this.SPARK_KUBERNETES_DEFAULT_NAMESPACE;
    }

    public static final /* synthetic */ boolean $anonfun$getConfig$1(String x$6) {
        return new StringOps(Predef$.MODULE$.augmentString(x$6)).nonEmpty();
    }

    private GpfdistIngressListener$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
        this.EXECUTOR_POD_NAME_PREFIX_KEY = "spark.kubernetes.executor.podNamePrefix";
        this.SPARK_KUBERNETES_NAMESPACE_KEY = "spark.kubernetes.namespace";
        this.SPARK_KUBERNETES_DEFAULT_NAMESPACE = "default";
    }
}

