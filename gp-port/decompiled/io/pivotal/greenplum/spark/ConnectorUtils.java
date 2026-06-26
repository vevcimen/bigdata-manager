/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.TraversableOnce
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.java8.JFunction0$mcI$sp
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.ConnectorUtils$;
import io.pivotal.greenplum.spark.conf.ConnectorOptions;
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation$;
import io.pivotal.greenplum.spark.util.NetworkOperations;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import scala.Function0;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Some;
import scala.collection.TraversableOnce;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.java8.JFunction0;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\u0005\u0005c\u0001B\t\u0013\u0001mA\u0001\"\n\u0001\u0003\u0006\u0004%IA\n\u0005\t[\u0001\u0011\t\u0011)A\u0005O!)a\u0006\u0001C\u0001_!)1\u0007\u0001C\u0001i!)\u0001\n\u0001C\u0001\u0013\")\u0011\f\u0001C\u00015\")!\r\u0001C\u0005G\")Q\r\u0001C\u0005M\")\u0011\u000e\u0001C\u0005U\")Q\u000f\u0001C\u0005m\u001e9\u00111\u0002\n\t\u0002\u00055aAB\t\u0013\u0011\u0003\ty\u0001\u0003\u0004/\u0019\u0011\u0005\u0011\u0011\u0003\u0005\u0007\u00112!\t!a\u0005\t\u0013\u0005eA\"%A\u0005\u0002\u0005m\u0001\"CA\u0019\u0019\u0005\u0005I\u0011BA\u001a\u00059\u0019uN\u001c8fGR|'/\u0016;jYNT!a\u0005\u000b\u0002\u000bM\u0004\u0018M]6\u000b\u0005U1\u0012!C4sK\u0016t\u0007\u000f\\;n\u0015\t9\u0002$A\u0004qSZ|G/\u00197\u000b\u0003e\t!![8\u0004\u0001M\u0019\u0001\u0001\b\u0012\u0011\u0005u\u0001S\"\u0001\u0010\u000b\u0003}\tQa]2bY\u0006L!!\t\u0010\u0003\r\u0005s\u0017PU3g!\ti2%\u0003\u0002%=\ta1+\u001a:jC2L'0\u00192mK\u0006\tb.\u001a;x_J\\w\n]3sCRLwN\\:\u0016\u0003\u001d\u0002\"\u0001K\u0016\u000e\u0003%R!A\u000b\n\u0002\tU$\u0018\u000e\\\u0005\u0003Y%\u0012\u0011CT3uo>\u00148n\u00149fe\u0006$\u0018n\u001c8t\u0003IqW\r^<pe.|\u0005/\u001a:bi&|gn\u001d\u0011\u0002\rqJg.\u001b;?)\t\u0001$\u0007\u0005\u00022\u00015\t!\u0003C\u0004&\u0007A\u0005\t\u0019A\u0014\u0002\u0017\u001d,G\u000fT8dCRLwN\u001c\u000b\u0004km\u001a\u0005C\u0001\u001c:\u001b\u00059$B\u0001\u001d\u0013\u00035)\u0007\u0010^3s]\u0006dG/\u00192mK&\u0011!h\u000e\u0002\u0010\u000fB4G-[:u\u0019>\u001c\u0017\r^5p]\")A\b\u0002a\u0001{\u0005\u00012m\u001c8oK\u000e$xN](qi&|gn\u001d\t\u0003}\u0005k\u0011a\u0010\u0006\u0003\u0001J\tAaY8oM&\u0011!i\u0010\u0002\u0011\u0007>tg.Z2u_J|\u0005\u000f^5p]NDQ\u0001\u0012\u0003A\u0002\u0015\u000b\u0001\u0003\\8dC2<\u0005O\u001a3jgR\u0004vN\u001d;\u0011\u0005u1\u0015BA$\u001f\u0005\rIe\u000e^\u0001\u0016O\u0016$Hj\\2bi&|g\u000eU1uQB\u0013XMZ5y)\rQUk\u0016\t\u0003\u0017Js!\u0001\u0014)\u0011\u00055sR\"\u0001(\u000b\u0005=S\u0012A\u0002\u001fs_>$h(\u0003\u0002R=\u00051\u0001K]3eK\u001aL!a\u0015+\u0003\rM#(/\u001b8h\u0015\t\tf\u0004C\u0003W\u000b\u0001\u0007!*A\u0007baBd\u0017nY1uS>t\u0017\n\u001a\u0005\u00061\u0016\u0001\rAS\u0001\u000bKb,7-\u001e;pe&#\u0017AJ4fi\"{7\u000f^!eIJ,7o\u001d\"z\u001d\u0016$xo\u001c:l\u0013:$XM\u001d4bG\u0016\u0014\u0015PT1nKR\u00111\f\u0019\t\u00049zSU\"A/\u000b\u0005)r\u0012BA0^\u0005\r!&/\u001f\u0005\u0006C\u001a\u0001\rAS\u0001\u0005]\u0006lW-A\bhKRdunY1uS>t\u0007j\\:u)\tQE\rC\u0003=\u000f\u0001\u0007Q(A\bhKRdunY1uS>t\u0007k\u001c:u)\r)u\r\u001b\u0005\u0006y!\u0001\r!\u0010\u0005\u0006\t\"\u0001\r!R\u0001&O\u0016$\u0018J\\3ui\u0005#GM]3tg\nKh*\u001a;x_J\\\u0017J\u001c;fe\u001a\f7-\u001a(b[\u0016$\"a\u001b;\u0011\u0007qsF\u000e\u0005\u0002ne6\taN\u0003\u0002pa\u0006\u0019a.\u001a;\u000b\u0003E\fAA[1wC&\u00111O\u001c\u0002\r\u0013:,G\u000fN!eIJ,7o\u001d\u0005\u0006C&\u0001\rAS\u0001\u0011M&tG-\u00138fiR\nE\r\u001a:fgN$\"a[<\t\u000baT\u0001\u0019A=\u0002\u0013\u0005$GM]3tg\u0016\u001c\b\u0003\u0002>\u0000\u0003\u000bq!a_?\u000f\u00055c\u0018\"A\u0010\n\u0005yt\u0012a\u00029bG.\fw-Z\u0005\u0005\u0003\u0003\t\u0019AA\bUe\u00064XM]:bE2,wJ\\2f\u0015\tqh\u0004E\u0002n\u0003\u000fI1!!\u0003o\u0005-Ie.\u001a;BI\u0012\u0014Xm]:\u0002\u001d\r{gN\\3di>\u0014X\u000b^5mgB\u0011\u0011\u0007D\n\u0004\u0019q\u0011CCAA\u0007)\u0015Q\u0015QCA\f\u0011\u00151f\u00021\u0001K\u0011\u0015Af\u00021\u0001K\u0003m!C.Z:tS:LG\u000fJ4sK\u0006$XM\u001d\u0013eK\u001a\fW\u000f\u001c;%cU\u0011\u0011Q\u0004\u0016\u0004O\u0005}1FAA\u0011!\u0011\t\u0019#!\f\u000e\u0005\u0005\u0015\"\u0002BA\u0014\u0003S\t\u0011\"\u001e8dQ\u0016\u001c7.\u001a3\u000b\u0007\u0005-b$\u0001\u0006b]:|G/\u0019;j_:LA!a\f\u0002&\t\tRO\\2iK\u000e\\W\r\u001a,be&\fgnY3\u0002\u0017I,\u0017\r\u001a*fg>dg/\u001a\u000b\u0003\u0003k\u0001B!a\u000e\u0002>5\u0011\u0011\u0011\b\u0006\u0004\u0003w\u0001\u0018\u0001\u00027b]\u001eLA!a\u0010\u0002:\t1qJ\u00196fGR\u0004")
public class ConnectorUtils
implements scala.Serializable {
    private final NetworkOperations networkOperations;

    public static NetworkOperations $lessinit$greater$default$1() {
        return ConnectorUtils$.MODULE$.$lessinit$greater$default$1();
    }

    private NetworkOperations networkOperations() {
        return this.networkOperations;
    }

    public GpfdistLocation getLocation(ConnectorOptions connectorOptions, int localGpfdistPort) {
        return GpfdistLocation$.MODULE$.apply(this.getLocationHost(connectorOptions), this.getLocationPort(connectorOptions, localGpfdistPort), connectorOptions.useSsl());
    }

    public String getLocationPathPrefix(String applicationId, String executorId) {
        return ConnectorUtils$.MODULE$.getLocationPathPrefix(applicationId, executorId);
    }

    public Try<String> getHostAddressByNetworkInterfaceByName(String name) {
        return this.getInet4AddressByNetworkInterfaceName(name).map((Function1 & Serializable & scala.Serializable)x$1 -> x$1.getHostAddress());
    }

    private String getLocationHost(ConnectorOptions connectorOptions) {
        if (connectorOptions.useLocalHostname()) {
            return this.networkOperations().getLocalHostName();
        }
        if (connectorOptions.serverAddressFromEnvironment().isDefined()) {
            return this.networkOperations().getInetAddressByName((String)connectorOptions.serverAddressFromEnvironment().get()).getHostAddress();
        }
        if (connectorOptions.networkInterfaceName().isDefined()) {
            return (String)this.getInet4AddressByNetworkInterfaceName((String)connectorOptions.networkInterfaceName().get()).map((Function1 & Serializable & scala.Serializable)x$2 -> x$2.getHostAddress()).get();
        }
        if (connectorOptions.gpfdistHost().isDefined()) {
            return (String)connectorOptions.gpfdistHost().get();
        }
        return this.networkOperations().getLocalHostAddress();
    }

    private int getLocationPort(ConnectorOptions connectorOptions, int localGpfdistPort) {
        return BoxesRunTime.unboxToInt((Object)connectorOptions.getGpfdistLocationPort().getOrElse((Function0)(JFunction0.mcI.sp & Serializable & scala.Serializable)() -> localGpfdistPort));
    }

    private Try<Inet4Address> getInet4AddressByNetworkInterfaceName(String name) {
        return this.networkOperations().getInet4AddressByNetworkInterfaceName(name).flatMap((Function1 & Serializable & scala.Serializable)addresses -> this.findInet4Address((TraversableOnce<InetAddress>)addresses));
    }

    private Try<Inet4Address> findInet4Address(TraversableOnce<InetAddress> addresses) {
        Option option = addresses.find((Function1 & Serializable & scala.Serializable)x$3 -> BoxesRunTime.boxToBoolean((boolean)ConnectorUtils.$anonfun$findInet4Address$1(x$3)));
        if (option instanceof Some) {
            Some some = (Some)option;
            InetAddress inetAddress = (InetAddress)some.value();
            return new Success((Object)((Inet4Address)inetAddress));
        }
        if (None$.MODULE$.equals(option)) {
            return new Failure((Throwable)new IllegalArgumentException("no IPv4 address found"));
        }
        throw new MatchError((Object)option);
    }

    public static final /* synthetic */ boolean $anonfun$findInet4Address$1(InetAddress x$3) {
        return x$3 instanceof Inet4Address;
    }

    public ConnectorUtils(NetworkOperations networkOperations) {
        this.networkOperations = networkOperations;
    }
}

