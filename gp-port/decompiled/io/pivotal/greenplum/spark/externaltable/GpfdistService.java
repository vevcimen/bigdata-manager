/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Enumeration$Value
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.Predef$
 *  scala.Some
 *  scala.collection.Iterator
 *  scala.collection.immutable.List
 *  scala.collection.immutable.StringOps
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.conf.ConnectorOptions$;
import io.pivotal.greenplum.spark.externaltable.GpfdistServiceState$;
import io.pivotal.greenplum.spark.externaltable.PartitionData;
import io.pivotal.greenplum.spark.externaltable.ServerWrapper;
import io.pivotal.greenplum.spark.util.TransactionData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import scala.Enumeration;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Predef$;
import scala.Some;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.collection.immutable.StringOps;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\u0005%e\u0001B\u000b\u0017\u0001\u0005B\u0001B\r\u0001\u0003\u0002\u0003\u0006Ia\r\u0005\t}\u0001\u0011\t\u0011)A\u0005\u007f!A1\n\u0001B\u0001B\u0003%A\n\u0003\u0005_\u0001\t\u0005\t\u0015!\u0003`\u0011!!\u0007A!b\u0001\n\u0013)\u0007\u0002C5\u0001\u0005\u0003\u0005\u000b\u0011\u00024\t\u0011)\u0004!\u0011!Q\u0001\n-DQA\u001c\u0001\u0005\u0002=DQA\u001c\u0001\u0005\u0002]Dq! \u0001C\u0002\u0013%a\u0010C\u0004\u0002\u0010\u0001\u0001\u000b\u0011B@\t\u000f\u0005E\u0001\u0001\"\u0001\u0002\u0014!9\u00111\u0004\u0001\u0005\u0002\u0005M\u0001bBA\u000f\u0001\u0011\u0005\u0011q\u0004\u0005\b\u0003C\u0001A\u0011AA\u0012\u0011\u001d\t)\u0004\u0001C\u0001\u0003oAq!!\u0013\u0001\t\u0003\tY\u0005C\u0004\u0002N\u0001!\t!a\u0014\t\u000f\u0005m\u0003\u0001\"\u0001\u0002^!9\u0011q\r\u0001\u0005\n\u0005%$AD$qM\u0012L7\u000f^*feZL7-\u001a\u0006\u0003/a\tQ\"\u001a=uKJt\u0017\r\u001c;bE2,'BA\r\u001b\u0003\u0015\u0019\b/\u0019:l\u0015\tYB$A\u0005he\u0016,g\u000e\u001d7v[*\u0011QDH\u0001\ba&4x\u000e^1m\u0015\u0005y\u0012AA5p\u0007\u0001\u00192\u0001\u0001\u0012)!\t\u0019c%D\u0001%\u0015\u0005)\u0013!B:dC2\f\u0017BA\u0014%\u0005\u0019\te.\u001f*fMB\u0011\u0011\u0006M\u0007\u0002U)\u00111\u0006L\u0001\rg\u000e\fG.\u00197pO\u001eLgn\u001a\u0006\u0003[9\n\u0001\u0002^=qKN\fg-\u001a\u0006\u0002_\u0005\u00191m\\7\n\u0005ER#a\u0003'bufdunZ4j]\u001e\f1c]3sm\u0016\u00148i\u001c8oK\u000e$xN\u001d%pgR\u0004\"\u0001N\u001e\u000f\u0005UJ\u0004C\u0001\u001c%\u001b\u00059$B\u0001\u001d!\u0003\u0019a$o\\8u}%\u0011!\bJ\u0001\u0007!J,G-\u001a4\n\u0005qj$AB*ue&twM\u0003\u0002;I\u0005q\u0011M^1jY\u0006\u0014G.\u001a)peR\u001c\bc\u0001!F\u0011:\u0011\u0011i\u0011\b\u0003m\tK\u0011!J\u0005\u0003\t\u0012\nq\u0001]1dW\u0006<W-\u0003\u0002G\u000f\n!A*[:u\u0015\t!E\u0005\u0005\u0002$\u0013&\u0011!\n\n\u0002\u0004\u0013:$\u0018!\u00032vM\u001a,'/T1q!\u0011i%k\r+\u000e\u00039S!a\u0014)\u0002\tU$\u0018\u000e\u001c\u0006\u0002#\u0006!!.\u0019<b\u0013\t\u0019fJA\u0002NCB\u00042!V,Z\u001b\u00051&BA(%\u0013\tAfKA\u0002Uef\u0004\"A\u0017/\u000e\u0003mS!a\u0014\r\n\u0005u[&a\u0004+sC:\u001c\u0018m\u0019;j_:$\u0015\r^1\u0002\u001bM,g\u000e\u001a\"vM\u001a,'/T1q!\u0011i%k\r1\u0011\u0005\u0005\u0014W\"\u0001\f\n\u0005\r4\"!\u0004)beRLG/[8o\t\u0006$\u0018-\u0001\u0004tKJ4XM]\u000b\u0002MB\u0011\u0011mZ\u0005\u0003QZ\u0011QbU3sm\u0016\u0014xK]1qa\u0016\u0014\u0018aB:feZ,'\u000fI\u0001\u0010i&lWm\\;u\u0013:l\u0015\u000e\u001c7jgB\u00111\u0005\\\u0005\u0003[\u0012\u0012A\u0001T8oO\u00061A(\u001b8jiz\"r\u0001]9sgR,h\u000f\u0005\u0002b\u0001!)!\u0007\u0003a\u0001g!)a\b\u0003a\u0001\u007f!)1\n\u0003a\u0001\u0019\")a\f\u0003a\u0001?\")A\r\u0003a\u0001M\")!\u000e\u0003a\u0001WR1\u0001\u000f_={wrDQAP\u0005A\u0002}BQaS\u0005A\u00021CQAX\u0005A\u0002}CQ\u0001Z\u0005A\u0002\u0019DQA[\u0005A\u0002-\fqa\u001d;beR,G-F\u0001\u0000!\u0011\t\t!a\u0003\u000e\u0005\u0005\r!\u0002BA\u0003\u0003\u000f\ta!\u0019;p[&\u001c'bAA\u0005\u001d\u0006Q1m\u001c8dkJ\u0014XM\u001c;\n\t\u00055\u00111\u0001\u0002\u000e\u0003R|W.[2C_>dW-\u00198\u0002\u0011M$\u0018M\u001d;fI\u0002\nQa\u001d;beR$\"!!\u0006\u0011\u0007\r\n9\"C\u0002\u0002\u001a\u0011\u0012A!\u00168ji\u0006!1\u000f^8q\u0003\u001d9W\r\u001e)peR$\u0012\u0001S\u0001\u0006gR\fG/Z\u000b\u0003\u0003K\u0001B!a\n\u0002.9\u0019\u0011-!\u000b\n\u0007\u0005-b#A\nHa\u001a$\u0017n\u001d;TKJ4\u0018nY3Ti\u0006$X-\u0003\u0003\u00020\u0005E\"!\u0002,bYV,\u0017bAA\u001aI\tYQI\\;nKJ\fG/[8o\u0003I9W\r\u001e*fG\u0016Lg/\u001a3ECR\fgi\u001c:\u0015\t\u0005e\u0012Q\t\t\u0005+^\u000bY\u0004\u0005\u0003\u0002>\u0005\u0005SBAA \u0015\ty\u0002+\u0003\u0003\u0002D\u0005}\"aC%oaV$8\u000b\u001e:fC6Da!a\u0012\u0011\u0001\u0004\u0019\u0014!\u0004;sC:\u001c\u0018m\u0019;j_:LE-\u0001\u0006hKR$\u0016.\\3pkR$\u0012a[\u0001\u0014g\u0016$\b+\u0019:uSRLwN\u001c#bi\u00064uN\u001d\u000b\u0007\u0003#\n\u0019&a\u0016\u0011\u0007U;\u0006\u000f\u0003\u0004\u0002VI\u0001\raM\u0001\u0004W\u0016L\bBBA-%\u0001\u0007\u0001-A\u0007qCJ$\u0018\u000e^5p]\u0012\u000bG/Y\u0001\u0017e\u0016lwN^3QCJ$\u0018\u000e^5p]\u0012\u000bG/\u0019$peR!\u0011qLA3!\u0011\u0019\u0013\u0011\r1\n\u0007\u0005\rDE\u0001\u0004PaRLwN\u001c\u0005\u0007\u0003+\u001a\u0002\u0019A\u001a\u0002\u00199,woQ8o]\u0016\u001cGo\u001c:\u0015\t\u0005-\u0014\u0011\u0011\t\u0005\u0003[\ni(\u0004\u0002\u0002p)\u0019A-!\u001d\u000b\t\u0005M\u0014QO\u0001\u0006U\u0016$H/\u001f\u0006\u0005\u0003o\nI(A\u0004fG2L\u0007o]3\u000b\u0005\u0005m\u0014aA8sO&!\u0011qPA8\u0005=\u0019VM\u001d<fe\u000e{gN\\3di>\u0014\bB\u00023\u0015\u0001\u0004\t\u0019\t\u0005\u0003\u0002n\u0005\u0015\u0015\u0002BAD\u0003_\u0012aaU3sm\u0016\u0014\b")
public class GpfdistService
implements LazyLogging {
    private final String serverConnectorHost;
    private final List<Object> availablePorts;
    private final Map<String, Try<TransactionData>> bufferMap;
    private final Map<String, PartitionData> sendBufferMap;
    private final ServerWrapper server;
    private final long timeoutInMillis;
    private final AtomicBoolean started;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        GpfdistService gpfdistService = this;
        synchronized (gpfdistService) {
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

    private ServerWrapper server() {
        return this.server;
    }

    private AtomicBoolean started() {
        return this.started;
    }

    public synchronized void start() {
        Iterator portsIterator = this.availablePorts.iterator();
        if (!this.started().get()) {
            while (portsIterator.hasNext() && !this.started().get()) {
                BoxedUnit boxedUnit;
                int currentPort = BoxesRunTime.unboxToInt((Object)portsIterator.next());
                try {
                    ServerConnector connector = this.newConnector(this.server().server());
                    connector.setHost(this.serverConnectorHost);
                    connector.setPort(currentPort);
                    connector.setIdleTimeout(this.timeoutInMillis);
                    this.server().setConnectors((Connector[])((Object[])new Connector[]{connector}));
                    this.server().start();
                    this.started().set(true);
                    if (this.logger().underlying().isInfoEnabled()) {
                        this.logger().underlying().info(new StringBuilder(41).append("Successfully started Gpfdist service on ").append(this.serverConnectorHost).append(":").append(currentPort != 0 ? BoxesRunTime.boxToInteger((int)currentPort) : BoxesRunTime.boxToInteger((int)this.getPort())).toString());
                        boxedUnit = BoxedUnit.UNIT;
                        continue;
                    }
                    boxedUnit = BoxedUnit.UNIT;
                }
                catch (BindException bindException) {
                    if (this.logger().underlying().isWarnEnabled()) {
                        this.logger().underlying().warn("Unable to bind port {}", new Object[]{BoxesRunTime.boxToInteger((int)currentPort)});
                        boxedUnit = BoxedUnit.UNIT;
                        continue;
                    }
                    boxedUnit = BoxedUnit.UNIT;
                }
                catch (IOException e) {
                    if (this.logger().underlying().isWarnEnabled()) {
                        this.logger().underlying().warn("Error when starting GpfdistService: {}", new Object[]{e.getMessage()});
                        boxedUnit = BoxedUnit.UNIT;
                        continue;
                    }
                    boxedUnit = BoxedUnit.UNIT;
                }
            }
            if (!this.started().get()) {
                throw new RuntimeException(new StringBuilder(47).append("Unable to start GpfdistService on any of ports=").append(this.availablePorts.mkString(", ")).toString());
            }
            return;
        }
    }

    public synchronized void stop() {
        if (this.started().compareAndSet(true, false)) {
            this.server().stop();
            return;
        }
    }

    public int getPort() {
        Enumeration.Value value = this.state();
        Enumeration.Value value2 = GpfdistServiceState$.MODULE$.Started();
        if (value == null ? value2 != null : !value.equals(value2)) {
            throw new RuntimeException("There is no server started");
        }
        return ((ServerConnector)new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])this.server().getConnectors())).head()).getLocalPort();
    }

    public Enumeration.Value state() {
        return GpfdistServiceState$.MODULE$.withName(new StringOps(Predef$.MODULE$.augmentString(this.server().getState().toLowerCase())).capitalize());
    }

    public Try<InputStream> getReceivedDataFor(String transactionId) {
        Try<TransactionData> try_ = this.bufferMap.remove(transactionId);
        if (try_ == null) {
            return new Success((Object)new ByteArrayInputStream(new byte[0]));
        }
        if (try_ instanceof Success) {
            Success success = (Success)try_;
            TransactionData transactionData = (TransactionData)success.value();
            return new Success((Object)transactionData.getInputStream());
        }
        if (try_ instanceof Failure) {
            Failure failure = (Failure)try_;
            Throwable exception = failure.exception();
            return new Failure(exception);
        }
        throw new MatchError(try_);
    }

    public long getTimeout() {
        return this.timeoutInMillis;
    }

    public Try<GpfdistService> setPartitionDataFor(String key, PartitionData partitionData) {
        if (this.sendBufferMap.containsKey(key)) {
            String msg = new StringBuilder(48).append("Send buffer already exists for the given path = ").append(key).toString();
            return new Failure((Throwable)new IllegalStateException(msg));
        }
        this.sendBufferMap.put(key, partitionData);
        return new Success((Object)this);
    }

    public Option<PartitionData> removePartitionDataFor(String key) {
        Option partitionInfo = Option$.MODULE$.apply((Object)this.sendBufferMap.remove(key));
        Option option = partitionInfo;
        if (option instanceof Some) {
            Some some = (Some)option;
            PartitionData p = (PartitionData)some.value();
            if (!p.handled().get()) {
                if (this.logger().underlying().isWarnEnabled()) {
                    this.logger().underlying().warn(new StringBuilder(62).append("Data has not been marked as processed for path ").append(key).append(" and partition ").append(p.partitionIndex()).toString());
                }
            }
        } else if (None$.MODULE$.equals(option)) {
            if (this.logger().underlying().isWarnEnabled()) {
                this.logger().underlying().warn("No partition exists for path {}", new Object[]{key});
            }
        } else {
            throw new MatchError((Object)option);
        }
        return partitionInfo;
    }

    private ServerConnector newConnector(Server server) {
        ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler("Gpfdist-JettyScheduler", true);
        return new ServerConnector(server, null, (Scheduler)scheduler, null, -1, -1, new HttpConnectionFactory());
    }

    public GpfdistService(String serverConnectorHost, List<Object> availablePorts, Map<String, Try<TransactionData>> bufferMap, Map<String, PartitionData> sendBufferMap, ServerWrapper server, long timeoutInMillis) {
        this.serverConnectorHost = serverConnectorHost;
        this.availablePorts = availablePorts;
        this.bufferMap = bufferMap;
        this.sendBufferMap = sendBufferMap;
        this.server = server;
        this.timeoutInMillis = timeoutInMillis;
        LazyLogging.$init$(this);
        if (server == null) {
            throw new NullPointerException("server is null");
        }
        this.started = new AtomicBoolean(false);
    }

    public GpfdistService(List<Object> availablePorts, Map<String, Try<TransactionData>> bufferMap, Map<String, PartitionData> sendBufferMap, ServerWrapper server, long timeoutInMillis) {
        this(ConnectorOptions$.MODULE$.GPDB_DEFAULT_SERVER_HOST(), availablePorts, bufferMap, sendBufferMap, server, timeoutInMillis);
    }
}

