/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple4
 *  scala.math.package$
 *  scala.runtime.BoxesRunTime
 */
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.apache.commons.codec.digest.Crypt;
import scala.Function0;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple4;
import scala.math.package$;
import scala.runtime.BoxesRunTime;

public final class ConnectionKey$
implements Serializable {
    public static ConnectionKey$ MODULE$;
    private final String salt;

    static {
        new ConnectionKey$();
    }

    public ConnectionKey apply(String url, String user, Option<String> password, ConnectionPoolOptions connectionPoolOptions) {
        return this.apply(url, user, this.hashPassword((String)password.getOrElse((Function0 & java.io.Serializable & Serializable)() -> "")), connectionPoolOptions.hashCode());
    }

    private String salt() {
        return this.salt;
    }

    public String hashPassword(String pass) {
        return Crypt.crypt(pass, new StringBuilder(3).append("$6$").append(this.salt()).toString());
    }

    public ConnectionKey apply(String jdbcUrl, String userName, String hashedPassword, int connectionPoolOptionsHash) {
        return new ConnectionKey(jdbcUrl, userName, hashedPassword, connectionPoolOptionsHash);
    }

    public Option<Tuple4<String, String, String, Object>> unapply(ConnectionKey x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple4((Object)x$0.jdbcUrl(), (Object)x$0.userName(), (Object)x$0.hashedPassword(), (Object)BoxesRunTime.boxToInteger((int)x$0.connectionPoolOptionsHash())));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ConnectionKey$() {
        MODULE$ = this;
        SecureRandom sr = new SecureRandom();
        int saltLength = 16;
        int encodingBaseExponent = 4;
        int radix = (int)package$.MODULE$.pow(2.0, (double)encodingBaseExponent);
        this.salt = new BigInteger(encodingBaseExponent * saltLength, sr).toString(radix);
    }
}

