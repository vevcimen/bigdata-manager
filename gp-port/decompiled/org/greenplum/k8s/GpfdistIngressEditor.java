/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.fabric8.kubernetes.api.model.Service
 *  io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath
 *  io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder
 *  io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathFluent$BackendNested
 *  io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathFluentImpl
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressBackendFluent$ServiceNested
 *  io.fabric8.kubernetes.api.model.networking.v1.IngressServiceBackendFluent$PortNested
 *  scala.Function0
 *  scala.Function1
 *  scala.Option
 *  scala.Option$
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.JavaConverters$
 *  scala.collection.TraversableOnce
 *  scala.reflect.ScalaSignature
 */
package org.greenplum.k8s;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathFluent;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathFluentImpl;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBackendFluent;
import io.fabric8.kubernetes.api.model.networking.v1.IngressServiceBackendFluent;
import io.pivotal.greenplum.spark.ConnectorUtils$;
import java.io.Serializable;
import org.greenplum.k8s.AddPathIngressEdit;
import org.greenplum.k8s.IngressEdit;
import org.greenplum.k8s.RemoveAllPathsWithPrefixIngressEdit;
import org.greenplum.spark.GpfdistConf;
import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.Option$;
import scala.Predef$;
import scala.collection.JavaConverters$;
import scala.collection.TraversableOnce;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001\u00194A!\u0004\b\u0001+!AA\u0004\u0001BC\u0002\u0013%Q\u0004\u0003\u0005*\u0001\t\u0005\t\u0015!\u0003\u001f\u0011!Q\u0003A!b\u0001\n\u0013Y\u0003\u0002\u0003\u001a\u0001\u0005\u0003\u0005\u000b\u0011\u0002\u0017\t\u000bM\u0002A\u0011\u0001\u001b\t\u000fe\u0002!\u0019!C\u0005;!1!\b\u0001Q\u0001\nyAQa\u000f\u0001\u0005\u0002qBQA\u0015\u0001\u0005\u0002MCQ!\u0016\u0001\u0005\u0002YCQa\u0016\u0001\u0005\u0002aCQa\u0019\u0001\u0005\n\u0011\u0014Ac\u00129gI&\u001cH/\u00138he\u0016\u001c8/\u00123ji>\u0014(BA\b\u0011\u0003\rY\u0007h\u001d\u0006\u0003#I\t\u0011b\u001a:fK:\u0004H.^7\u000b\u0003M\t1a\u001c:h\u0007\u0001\u0019\"\u0001\u0001\f\u0011\u0005]QR\"\u0001\r\u000b\u0003e\tQa]2bY\u0006L!a\u0007\r\u0003\r\u0005s\u0017PU3g\u0003\u0015\t\u0007\u000f]%e+\u0005q\u0002CA\u0010'\u001d\t\u0001C\u0005\u0005\u0002\"15\t!E\u0003\u0002$)\u00051AH]8pizJ!!\n\r\u0002\rA\u0013X\rZ3g\u0013\t9\u0003F\u0001\u0004TiJLgn\u001a\u0006\u0003Ka\ta!\u00199q\u0013\u0012\u0004\u0013aC4qM\u0012L7\u000f^\"p]\u001a,\u0012\u0001\f\t\u0003[Aj\u0011A\f\u0006\u0003_A\tQa\u001d9be.L!!\r\u0018\u0003\u0017\u001d\u0003h\rZ5ti\u000e{gNZ\u0001\rOB4G-[:u\u0007>tg\rI\u0001\u0007y%t\u0017\u000e\u001e \u0015\u0007U:\u0004\b\u0005\u00027\u00015\ta\u0002C\u0003\u001d\u000b\u0001\u0007a\u0004C\u0003+\u000b\u0001\u0007A&\u0001\u000biiR\u0004\b+\u0019;i!J,g-\u001b=G_J\f\u0005\u000f]\u0001\u0016QR$\b\u000fU1uQB\u0013XMZ5y\r>\u0014\u0018\t\u001d9!\u0003\u001d\tG\r\u001a)bi\"$2!\u0010!C!\t1d(\u0003\u0002@\u001d\tY\u0011J\\4sKN\u001cX\tZ5u\u0011\u0015\t\u0005\u00021\u0001\u001f\u0003\u0019)\u00070Z2JI\")1\t\u0003a\u0001\t\u000691/\u001a:wS\u000e,\u0007CA#Q\u001b\u00051%BA$I\u0003\u0015iw\u000eZ3m\u0015\tI%*A\u0002ba&T!a\u0013'\u0002\u0015-,(-\u001a:oKR,7O\u0003\u0002N\u001d\u00069a-\u00192sS\u000eD$\"A(\u0002\u0005%|\u0017BA)G\u0005\u001d\u0019VM\u001d<jG\u0016\fQC]3n_Z,\u0007+\u0019;i\r>\u0014X\t_3dkR|'\u000f\u0006\u0002>)\")\u0011)\u0003a\u0001=\u0005a\"/Z7pm\u0016\fE\u000e\u001c)bi\"\u001chi\u001c:BaBd\u0017nY1uS>tG#A\u001f\u0002=\t,\u0018\u000e\u001c3IiR\u0004\u0018J\\4sKN\u001c\b+\u0019;i\r>\u00148+\u001a:wS\u000e,GcA-bEB\u0011!lX\u0007\u00027*\u0011A,X\u0001\u0003mFR!A\u0018$\u0002\u00159,Go^8sW&tw-\u0003\u0002a7\ny\u0001\n\u0016+Q\u0013:<'/Z:t!\u0006$\b\u000eC\u0003B\u0017\u0001\u0007a\u0004C\u0003D\u0017\u0001\u0007A)A\u000fhKR<\u0005O\u001a3jgRDE\u000f\u001e9QCRDgi\u001c:Fq\u0016\u001cW\u000f^8s)\tqR\rC\u0003B\u0019\u0001\u0007a\u0004")
public class GpfdistIngressEditor {
    private final String appId;
    private final GpfdistConf gpfdistConf;
    private final String httpPathPrefixForApp;

    private String appId() {
        return this.appId;
    }

    private GpfdistConf gpfdistConf() {
        return this.gpfdistConf;
    }

    private String httpPathPrefixForApp() {
        return this.httpPathPrefixForApp;
    }

    public IngressEdit addPath(String execId, Service service) {
        HTTPIngressPath pathToAdd = this.buildHttpIngressPathForService(execId, service);
        return new AddPathIngressEdit(pathToAdd, this.gpfdistConf());
    }

    public IngressEdit removePathForExecutor(String execId) {
        String pathPrefixForExecutor = this.getGpfdistHttpPathForExecutor(execId);
        return new RemoveAllPathsWithPrefixIngressEdit(pathPrefixForExecutor, this.gpfdistConf());
    }

    public IngressEdit removeAllPathsForApplication() {
        return new RemoveAllPathsWithPrefixIngressEdit(this.httpPathPrefixForApp(), this.gpfdistConf());
    }

    public HTTPIngressPath buildHttpIngressPathForService(String execId, Service service) {
        Option serviceName = Option$.MODULE$.apply((Object)service.getMetadata()).flatMap((Function1 & Serializable & scala.Serializable)objectMeta -> Option$.MODULE$.apply((Object)objectMeta.getName()));
        Predef$.MODULE$.assert(serviceName.isDefined(), (Function0 & Serializable & scala.Serializable)() -> "received Service object with no name");
        Option servicePort = Option$.MODULE$.apply((Object)service.getSpec()).flatMap((Function1 & Serializable & scala.Serializable)spec -> Option$.MODULE$.apply((Object)spec.getPorts()).map((Function1 & Serializable & scala.Serializable)x$1 -> ((TraversableOnce)JavaConverters$.MODULE$.asScalaBufferConverter(x$1).asScala()).toList())).flatMap((Function1 & Serializable & scala.Serializable)ports -> ports.headOption()).map((Function1 & Serializable & scala.Serializable)x$2 -> x$2.getPort());
        Predef$.MODULE$.assert(servicePort.isDefined(), (Function0 & Serializable & scala.Serializable)() -> new StringBuilder(40).append("Service ").append(serviceName).append(" did not have a port in its spec").toString());
        String path = this.getGpfdistHttpPathForExecutor(execId);
        return ((HTTPIngressPathBuilder)((HTTPIngressPathFluent.BackendNested)((IngressBackendFluent.ServiceNested)((IngressServiceBackendFluent.PortNested)((HTTPIngressPathFluentImpl)((HTTPIngressPathFluentImpl)new HTTPIngressPathBuilder().withPath(path)).withPathType("Prefix")).withNewBackend().withNewService().withName((String)serviceName.get()).withNewPort().withNumber((Integer)servicePort.get())).endPort()).endService()).endBackend()).build();
    }

    private String getGpfdistHttpPathForExecutor(String execId) {
        return ConnectorUtils$.MODULE$.getLocationPathPrefix(this.appId(), execId);
    }

    public GpfdistIngressEditor(String appId, GpfdistConf gpfdistConf) {
        this.appId = appId;
        this.gpfdistConf = gpfdistConf;
        this.httpPathPrefixForApp = new StringBuilder(1).append("/").append(appId).toString();
    }
}

