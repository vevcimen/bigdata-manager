/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Option$
 *  scala.Serializable
 *  scala.Some
 *  scala.collection.Iterator
 *  scala.collection.JavaConverters$
 *  scala.reflect.ScalaSignature
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Serializable;
import scala.Some;
import scala.collection.Iterator;
import scala.collection.JavaConverters$;
import scala.reflect.ScalaSignature;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001a3A\u0001C\u0005\u0001)!)a\u0004\u0001C\u0001?!)!\u0005\u0001C\u0001G!)q\u0006\u0001C\u0001G!)\u0001\u0007\u0001C\u0001c!)A\b\u0001C\u0001{!)Q\t\u0001C\u0001\r\")a\u000b\u0001C\u0005/\n\tb*\u001a;x_J\\w\n]3sCRLwN\\:\u000b\u0005)Y\u0011\u0001B;uS2T!\u0001D\u0007\u0002\u000bM\u0004\u0018M]6\u000b\u00059y\u0011!C4sK\u0016t\u0007\u000f\\;n\u0015\t\u0001\u0012#A\u0004qSZ|G/\u00197\u000b\u0003I\t!![8\u0004\u0001M\u0019\u0001!F\u000e\u0011\u0005YIR\"A\f\u000b\u0003a\tQa]2bY\u0006L!AG\f\u0003\r\u0005s\u0017PU3g!\t1B$\u0003\u0002\u001e/\ta1+\u001a:jC2L'0\u00192mK\u00061A(\u001b8jiz\"\u0012\u0001\t\t\u0003C\u0001i\u0011!C\u0001\u0014O\u0016$Hj\\2bY\"{7\u000f^!eIJ,7o]\u000b\u0002IA\u0011Q\u0005\f\b\u0003M)\u0002\"aJ\f\u000e\u0003!R!!K\n\u0002\rq\u0012xn\u001c;?\u0013\tYs#\u0001\u0004Qe\u0016$WMZ\u0005\u0003[9\u0012aa\u0015;sS:<'BA\u0016\u0018\u0003A9W\r\u001e'pG\u0006d\u0007j\\:u\u001d\u0006lW-\u0001\u000bhKRLe.\u001a;BI\u0012\u0014Xm]:Cs:\u000bW.\u001a\u000b\u0003ei\u0002\"a\r\u001d\u000e\u0003QR!!\u000e\u001c\u0002\u00079,GOC\u00018\u0003\u0011Q\u0017M^1\n\u0005e\"$aC%oKR\fE\r\u001a:fgNDQa\u000f\u0003A\u0002\u0011\nAA\\1nK\u0006Ir-\u001a;OKR<xN]6J]R,'OZ1dK\nKh*Y7f)\tqD\tE\u0002\u0017\u007f\u0005K!\u0001Q\f\u0003\r=\u0003H/[8o!\t\u0019$)\u0003\u0002Di\t\u0001b*\u001a;x_J\\\u0017J\u001c;fe\u001a\f7-\u001a\u0005\u0006w\u0015\u0001\r\u0001J\u0001&O\u0016$\u0018J\\3ui\u0005#GM]3tg\nKh*\u001a;x_J\\\u0017J\u001c;fe\u001a\f7-\u001a(b[\u0016$\"aR+\u0011\u0007!SE*D\u0001J\u0015\tQq#\u0003\u0002L\u0013\n\u0019AK]=\u0011\u00075\u0013&G\u0004\u0002O!:\u0011qeT\u0005\u00021%\u0011\u0011kF\u0001\ba\u0006\u001c7.Y4f\u0013\t\u0019FK\u0001\u0005Ji\u0016\u0014\u0018\r^8s\u0015\t\tv\u0003C\u0003<\r\u0001\u0007A%\u0001\u0007hKRdunY1m\u0011>\u001cH/F\u00013\u0001")
public class NetworkOperations
implements Serializable {
    public String getLocalHostAddress() {
        return this.getLocalHost().getHostAddress();
    }

    public String getLocalHostName() {
        return this.getLocalHost().getHostName();
    }

    public InetAddress getInetAddressByName(String name) {
        return InetAddress.getByName(name);
    }

    public Option<NetworkInterface> getNetworkInterfaceByName(String name) {
        return Option$.MODULE$.apply((Object)NetworkInterface.getByName(name));
    }

    public Try<Iterator<InetAddress>> getInet4AddressByNetworkInterfaceName(String name) {
        Option<NetworkInterface> option = this.getNetworkInterfaceByName(name);
        if (option instanceof Some) {
            Some some = (Some)option;
            NetworkInterface nic = (NetworkInterface)some.value();
            return new Success(JavaConverters$.MODULE$.enumerationAsScalaIteratorConverter(nic.getInetAddresses()).asScala());
        }
        if (None$.MODULE$.equals(option)) {
            return new Failure((Throwable)new IllegalArgumentException(new StringBuilder(33).append("no network interface named ").append(name).append(" found").toString()));
        }
        throw new MatchError(option);
    }

    private InetAddress getLocalHost() {
        return InetAddress.getLocalHost();
    }
}

