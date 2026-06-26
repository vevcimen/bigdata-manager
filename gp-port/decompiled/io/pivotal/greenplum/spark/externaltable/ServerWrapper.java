/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.externaltable;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001q3A!\u0003\u0006\u0001+!AA\u0004\u0001BC\u0002\u0013\u0005Q\u0004\u0003\u0005*\u0001\t\u0005\t\u0015!\u0003\u001f\u0011\u0015Q\u0003\u0001\"\u0001,\u0011\u0015y\u0003\u0001\"\u00011\u0011\u0015)\u0005\u0001\"\u00011\u0011\u00151\u0005\u0001\"\u0001H\u0011\u0015\u0001\u0006\u0001\"\u0001R\u0011\u0015A\u0006\u0001\"\u0001Z\u00055\u0019VM\u001d<fe^\u0013\u0018\r\u001d9fe*\u00111\u0002D\u0001\u000eKb$XM\u001d8bYR\f'\r\\3\u000b\u00055q\u0011!B:qCJ\\'BA\b\u0011\u0003%9'/Z3oa2,XN\u0003\u0002\u0012%\u00059\u0001/\u001b<pi\u0006d'\"A\n\u0002\u0005%|7\u0001A\n\u0003\u0001Y\u0001\"a\u0006\u000e\u000e\u0003aQ\u0011!G\u0001\u0006g\u000e\fG.Y\u0005\u00037a\u0011a!\u00118z%\u00164\u0017AB:feZ,'/F\u0001\u001f!\tyr%D\u0001!\u0015\ta\u0012E\u0003\u0002#G\u0005)!.\u001a;us*\u0011A%J\u0001\bK\u000ed\u0017\u000e]:f\u0015\u00051\u0013aA8sO&\u0011\u0001\u0006\t\u0002\u0007'\u0016\u0014h/\u001a:\u0002\u000fM,'O^3sA\u00051A(\u001b8jiz\"\"\u0001\f\u0018\u0011\u00055\u0002Q\"\u0001\u0006\t\u000bq\u0019\u0001\u0019\u0001\u0010\u0002\u000bM$\u0018M\u001d;\u0015\u0003E\u0002\"a\u0006\u001a\n\u0005MB\"\u0001B+oSRD3\u0001B\u001bE!\r9b\u0007O\u0005\u0003oa\u0011a\u0001\u001e5s_^\u001c\bCA\u001dB\u001d\tQtH\u0004\u0002<}5\tAH\u0003\u0002>)\u00051AH]8pizJ\u0011!G\u0005\u0003\u0001b\tq\u0001]1dW\u0006<W-\u0003\u0002C\u0007\nIQ\t_2faRLwN\u001c\u0006\u0003\u0001b\u0019\u0013\u0001O\u0001\u0005gR|\u0007/\u0001\u0005hKR\u001cF/\u0019;f+\u0005A\u0005CA%N\u001d\tQ5\n\u0005\u0002<1%\u0011A\nG\u0001\u0007!J,G-\u001a4\n\u00059{%AB*ue&twM\u0003\u0002M1\u0005iq-\u001a;D_:tWm\u0019;peN,\u0012A\u0015\t\u0004/M+\u0016B\u0001+\u0019\u0005\u0015\t%O]1z!\tyb+\u0003\u0002XA\tI1i\u001c8oK\u000e$xN]\u0001\u000eg\u0016$8i\u001c8oK\u000e$xN]:\u0015\u0005ER\u0006\"B.\t\u0001\u0004\u0011\u0016AC2p]:,7\r^8sg\u0002")
public class ServerWrapper {
    private final Server server;

    public Server server() {
        return this.server;
    }

    public void start() throws Exception {
        this.server().start();
    }

    public void stop() {
        this.server().stop();
    }

    public String getState() {
        return this.server().getState();
    }

    public Connector[] getConnectors() {
        return this.server().getConnectors();
    }

    public void setConnectors(Connector[] connectors) {
        this.server().setConnectors(connectors);
    }

    public ServerWrapper(Server server) {
        this.server = server;
    }
}

