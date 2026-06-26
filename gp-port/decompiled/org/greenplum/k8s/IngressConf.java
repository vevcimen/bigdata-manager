/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringUtils
 *  org.apache.spark.SparkConf
 *  scala.Function0
 *  scala.Predef$
 *  scala.Serializable
 *  scala.reflect.ScalaSignature
 */
package org.greenplum.k8s;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.greenplum.k8s.IngressConf$;
import scala.Function0;
import scala.Predef$;
import scala.Serializable;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001\r3A\u0001D\u0007\u0001)!A1\u0004\u0001B\u0001B\u0003%A\u0004C\u0003%\u0001\u0011\u0005Q\u0005C\u0003*\u0001\u0011\u0005!\u0006C\u00037\u0001\u0011\u0005q\u0007C\u0003<\u0001\u0011\u0005q\u0007C\u0003=\u0001\u0011\u0005qgB\u0003>\u001b!\u0005aHB\u0003\r\u001b!\u0005q\bC\u0003%\u0011\u0011\u0005\u0001\tC\u0004B\u0011\t\u0007I\u0011\u0002\u0016\t\r\tC\u0001\u0015!\u0003,\u0005-Ien\u001a:fgN\u001cuN\u001c4\u000b\u00059y\u0011aA69g*\u0011\u0001#E\u0001\nOJ,WM\u001c9mk6T\u0011AE\u0001\u0004_J<7\u0001A\n\u0003\u0001U\u0001\"AF\r\u000e\u0003]Q\u0011\u0001G\u0001\u0006g\u000e\fG.Y\u0005\u00035]\u0011a!\u00118z%\u00164\u0017!C:qCJ\\7i\u001c8g!\ti\"%D\u0001\u001f\u0015\ty\u0002%A\u0003ta\u0006\u00148N\u0003\u0002\"#\u00051\u0011\r]1dQ\u0016L!a\t\u0010\u0003\u0013M\u0003\u0018M]6D_:4\u0017A\u0002\u001fj]&$h\b\u0006\u0002'QA\u0011q\u0005A\u0007\u0002\u001b!)1D\u0001a\u00019\u0005!a.Y7f+\u0005Y\u0003C\u0001\u00174\u001d\ti\u0013\u0007\u0005\u0002//5\tqF\u0003\u00021'\u00051AH]8pizJ!AM\f\u0002\rA\u0013X\rZ3g\u0013\t!TG\u0001\u0004TiJLgn\u001a\u0006\u0003e]\tQ\"[:SK6|g/\u001a)bi\"\u001cX#\u0001\u001d\u0011\u0005YI\u0014B\u0001\u001e\u0018\u0005\u001d\u0011un\u001c7fC:\f\u0011#^:f\u0019>\fGMQ1mC:\u001cWM]%q\u0003I)\b\u000fZ1uK2{7-\u0019;j_:\u0004vN\u001d;\u0002\u0017%swM]3tg\u000e{gN\u001a\t\u0003O!\u0019\"\u0001C\u000b\u0015\u0003y\n!bS#Z?B\u0013VIR%Y\u0003-YU)W0Q%\u00163\u0015\n\u0017\u0011")
public class IngressConf {
    private final SparkConf sparkConf;

    public String name() {
        return this.sparkConf.get(new StringBuilder(5).append(IngressConf$.MODULE$.org$greenplum$k8s$IngressConf$$KEY_PREFIX()).append(".name").toString());
    }

    public boolean isRemovePaths() {
        return this.sparkConf.getBoolean(new StringBuilder(13).append(IngressConf$.MODULE$.org$greenplum$k8s$IngressConf$$KEY_PREFIX()).append(".remove-paths").toString(), true);
    }

    public boolean useLoadBalancerIp() {
        return this.sparkConf.getBoolean(new StringBuilder(20).append(IngressConf$.MODULE$.org$greenplum$k8s$IngressConf$$KEY_PREFIX()).append(".use-loadbalancer-ip").toString(), false);
    }

    public boolean updateLocationPort() {
        return this.sparkConf.getBoolean(new StringBuilder(21).append(IngressConf$.MODULE$.org$greenplum$k8s$IngressConf$$KEY_PREFIX()).append(".update-location-port").toString(), false);
    }

    public IngressConf(SparkConf sparkConf) {
        this.sparkConf = sparkConf;
        Predef$.MODULE$.require(StringUtils.isNotBlank((CharSequence)this.name()), (Function0 & java.io.Serializable & Serializable)() -> new StringBuilder(62).append("no value given for '").append(IngressConf$.MODULE$.org$greenplum$k8s$IngressConf$$KEY_PREFIX()).append(".name' -- an ingress name must be provided").toString());
    }
}

