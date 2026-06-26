/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.Serializable
 *  scala.collection.IterableLike
 *  scala.collection.JavaConverters$
 *  scala.collection.immutable.List
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.conf.ConnectorOptions;
import io.pivotal.greenplum.spark.externaltable.GpfdistHandler;
import io.pivotal.greenplum.spark.externaltable.GpfdistService;
import io.pivotal.greenplum.spark.externaltable.PartitionData;
import io.pivotal.greenplum.spark.externaltable.ServerWrapper;
import io.pivotal.greenplum.spark.externaltable.ServiceKey;
import io.pivotal.greenplum.spark.util.TransactionData;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import scala.Function1;
import scala.collection.IterableLike;
import scala.collection.JavaConverters$;
import scala.collection.immutable.List;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Try;

public final class GpfdistServiceManager$
implements LazyLogging {
    public static GpfdistServiceManager$ MODULE$;
    private final ConcurrentHashMap<String, Try<TransactionData>> bufferMap;
    private final ConcurrentHashMap<String, PartitionData> sendBufferMap;
    private final ConcurrentHashMap<ServiceKey, GpfdistService> servicesMap;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new GpfdistServiceManager$();
    }

    private Logger logger$lzycompute() {
        GpfdistServiceManager$ gpfdistServiceManager$ = this;
        synchronized (gpfdistServiceManager$) {
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

    private ConcurrentHashMap<String, Try<TransactionData>> bufferMap() {
        return this.bufferMap;
    }

    private ConcurrentHashMap<String, PartitionData> sendBufferMap() {
        return this.sendBufferMap;
    }

    private ConcurrentHashMap<ServiceKey, GpfdistService> servicesMap() {
        return this.servicesMap;
    }

    public GpfdistService getService(ConnectorOptions connectorOptions) {
        BoxedUnit boxedUnit;
        GpfdistService service;
        long serviceTimeout;
        BoxedUnit boxedUnit2;
        BoxedUnit boxedUnit3;
        List<Object> ports = connectorOptions.port();
        ServiceKey key = new ServiceKey(ports);
        if (this.logger().underlying().isTraceEnabled()) {
            ((IterableLike)JavaConverters$.MODULE$.asScalaSetConverter(this.servicesMap().entrySet()).asScala()).foreach((Function1 & Serializable & scala.Serializable)e -> {
                GpfdistServiceManager$.$anonfun$getService$1(e);
                return BoxedUnit.UNIT;
            });
            boxedUnit3 = BoxedUnit.UNIT;
        } else {
            boxedUnit3 = BoxedUnit.UNIT;
        }
        if (!this.servicesMap().containsKey(key)) {
            ConcurrentHashMap<ServiceKey, GpfdistService> concurrentHashMap = this.servicesMap();
            synchronized (concurrentHashMap) {
                Object object;
                if (!this.servicesMap().containsKey(key)) {
                    BoxedUnit boxedUnit4;
                    GpfdistHandler handler = new GpfdistHandler(this.bufferMap(), this.sendBufferMap());
                    QueuedThreadPool pool = new QueuedThreadPool();
                    pool.setName(new StringBuilder(7).append("Gpfdist").append(handler.hashCode()).toString());
                    pool.setDaemon(true);
                    Server server = new Server(pool);
                    ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler("Gpfdist-JettyScheduler", true);
                    server.addBean(scheduler);
                    server.setHandler(handler);
                    String serverHost = connectorOptions.getServerHost();
                    GpfdistService gpfdistService = new GpfdistService(serverHost, ports, this.bufferMap(), this.sendBufferMap(), new ServerWrapper(server), connectorOptions.timeoutInMillis());
                    if (this.logger().underlying().isDebugEnabled()) {
                        this.logger().underlying().debug("Service for {} is being started....", new Object[]{ports});
                        boxedUnit4 = BoxedUnit.UNIT;
                    } else {
                        boxedUnit4 = BoxedUnit.UNIT;
                    }
                    gpfdistService.start();
                    object = this.servicesMap().put(key, gpfdistService);
                } else {
                    object = BoxedUnit.UNIT;
                }
                BoxedUnit boxedUnit5 = object;
                // MONITOREXIT @DISABLED, blocks:[0, 1, 12] lbl34 : MonitorExitStatement: MONITOREXIT : var4_4
                boxedUnit2 = boxedUnit5;
            }
        } else {
            boxedUnit2 = BoxedUnit.UNIT;
        }
        if ((serviceTimeout = (service = this.servicesMap().get(key)).getTimeout()) < connectorOptions.timeoutInMillis()) {
            if (this.logger().underlying().isWarnEnabled()) {
                this.logger().underlying().warn(new StringBuilder(253).append("Unable to change the GpfdistService timeout after the service ").append("has started. GpfdistService started with timeout ").append(serviceTimeout).append(" but ").append("a higher timeout (").append(connectorOptions.timeoutInMillis()).append(") is configured. ").append("Ensure that the desired GpfdistService timeout is set for the first ").append("operation that accesses Greenplum.").toString());
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return service;
    }

    public void stopAndRemove(ServiceKey key) {
        BoxedUnit boxedUnit;
        GpfdistService service = this.servicesMap().remove(key);
        if (service != null) {
            service.stop();
            return;
        }
        if (this.logger().underlying().isWarnEnabled()) {
            this.logger().underlying().warn("Unable to find service for {}", new Object[]{key});
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
    }

    public static final /* synthetic */ void $anonfun$getService$1(Map.Entry e) {
        BoxedUnit boxedUnit;
        if (MODULE$.logger().underlying().isTraceEnabled()) {
            MODULE$.logger().underlying().trace("{}: [key: {}; value: {}]", BoxesRunTime.boxToInteger((int)MODULE$.hashCode()), e.getKey(), e.getValue());
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
    }

    private GpfdistServiceManager$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
        this.bufferMap = new ConcurrentHashMap();
        this.sendBufferMap = new ConcurrentHashMap();
        this.servicesMap = new ConcurrentHashMap();
    }
}

