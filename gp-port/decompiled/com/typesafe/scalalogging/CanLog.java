/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package com.typesafe.scalalogging;

import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001a2q!\u0001\u0002\u0011\u0002\u0007\u0005\u0011B\u0001\u0004DC:dun\u001a\u0006\u0003\u0007\u0011\tAb]2bY\u0006dwnZ4j]\u001eT!!\u0002\u0004\u0002\u0011QL\b/Z:bM\u0016T\u0011aB\u0001\u0004G>l7\u0001A\u000b\u0003\u0015-\u001a\"\u0001A\u0006\u0011\u00051yQ\"A\u0007\u000b\u00039\tQa]2bY\u0006L!\u0001E\u0007\u0003\r\u0005s\u0017PU3g\u0011\u0015\u0011\u0002\u0001\"\u0001\u0014\u0003\u0019!\u0013N\\5uIQ\tA\u0003\u0005\u0002\r+%\u0011a#\u0004\u0002\u0005+:LG\u000fC\u0003\u0019\u0001\u0019\u0005\u0011$\u0001\u0006m_\u001elUm]:bO\u0016$2AG\u0013(!\tY\"E\u0004\u0002\u001dAA\u0011Q$D\u0007\u0002=)\u0011q\u0004C\u0001\u0007yI|w\u000e\u001e \n\u0005\u0005j\u0011A\u0002)sK\u0012,g-\u0003\u0002$I\t11\u000b\u001e:j]\u001eT!!I\u0007\t\u000b\u0019:\u0002\u0019\u0001\u000e\u0002\u0017=\u0014\u0018nZ5oC2l5o\u001a\u0005\u0006Q]\u0001\r!K\u0001\u0002CB\u0011!f\u000b\u0007\u0001\t\u0015a\u0003A1\u0001.\u0005\u0005\t\u0015C\u0001\u00182!\taq&\u0003\u00021\u001b\t9aj\u001c;iS:<\u0007C\u0001\u00073\u0013\t\u0019TBA\u0002B]fDQ!\u000e\u0001\u0005\u0002Y\n\u0001\"\u00194uKJdun\u001a\u000b\u0003)]BQ\u0001\u000b\u001bA\u0002%\u0002")
public interface CanLog<A> {
    public String logMessage(String var1, A var2);

    public static /* synthetic */ void afterLog$(CanLog $this, Object a) {
        $this.afterLog(a);
    }

    default public void afterLog(A a) {
    }

    public static void $init$(CanLog $this) {
    }
}

