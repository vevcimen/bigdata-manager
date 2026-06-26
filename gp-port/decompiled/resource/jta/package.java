/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.transaction.Transaction
 *  scala.reflect.ScalaSignature
 */
package resource.jta;

import javax.transaction.Transaction;
import resource.Resource;
import resource.jta.package$;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u00015:Q!\u0001\u0002\t\u0002\u001d\tq\u0001]1dW\u0006<WM\u0003\u0002\u0004\t\u0005\u0019!\u000e^1\u000b\u0003\u0015\t\u0001B]3t_V\u00148-Z\u0002\u0001!\tA\u0011\"D\u0001\u0003\r\u0015Q!\u0001#\u0001\f\u0005\u001d\u0001\u0018mY6bO\u0016\u001c\"!\u0003\u0007\u0011\u00055\u0001R\"\u0001\b\u000b\u0003=\tQa]2bY\u0006L!!\u0005\b\u0003\r\u0005s\u0017PU3g\u0011\u0015\u0019\u0012\u0002\"\u0001\u0015\u0003\u0019a\u0014N\\5u}Q\tq\u0001C\u0003\u0017\u0013\u0011\rq#\u0001\nue\u0006t7/Y2uS>t7+\u001e9q_J$XC\u0001\r +\u0005I\u0002c\u0001\u000e\u001c;5\tA!\u0003\u0002\u001d\t\tA!+Z:pkJ\u001cW\r\u0005\u0002\u001f?1\u0001A!\u0002\u0011\u0016\u0005\u0004\t#!A!\u0012\u0005\t*\u0003CA\u0007$\u0013\t!cBA\u0004O_RD\u0017N\\4\u0011\u0005\u0019ZS\"A\u0014\u000b\u0005!J\u0013a\u0003;sC:\u001c\u0018m\u0019;j_:T\u0011AK\u0001\u0006U\u00064\u0018\r_\u0005\u0003Y\u001d\u00121\u0002\u0016:b]N\f7\r^5p]\u0002")
public final class package {
    public static <A extends Transaction> Resource<A> transactionSupport() {
        return package$.MODULE$.transactionSupport();
    }
}

