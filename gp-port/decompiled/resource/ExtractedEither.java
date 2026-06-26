/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.Product
 *  scala.Serializable
 *  scala.collection.Iterator
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.ScalaRunTime$
 *  scala.util.Either
 */
package resource;

import resource.ExtractedEither$;
import scala.Option;
import scala.Product;
import scala.Serializable;
import scala.collection.Iterator;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxesRunTime;
import scala.runtime.ScalaRunTime$;
import scala.util.Either;

@ScalaSignature(bytes="\u0006\u0001\u0005]c\u0001B\u0001\u0003\u0001\u0016\u0011q\"\u0012=ue\u0006\u001cG/\u001a3FSRDWM\u001d\u0006\u0002\u0007\u0005A!/Z:pkJ\u001cWm\u0001\u0001\u0016\u0007\u0019!cf\u0005\u0003\u0001\u000f5\u0001\u0002C\u0001\u0005\f\u001b\u0005I!\"\u0001\u0006\u0002\u000bM\u001c\u0017\r\\1\n\u00051I!AB!osJ+g\r\u0005\u0002\t\u001d%\u0011q\"\u0003\u0002\b!J|G-^2u!\tA\u0011#\u0003\u0002\u0013\u0013\ta1+\u001a:jC2L'0\u00192mK\"AA\u0003\u0001BK\u0002\u0013\u0005Q#\u0001\u0004fSRDWM]\u000b\u0002-A!qc\b\u0012.\u001d\tARD\u0004\u0002\u001a95\t!D\u0003\u0002\u001c\t\u00051AH]8pizJ\u0011AC\u0005\u0003=%\tq\u0001]1dW\u0006<W-\u0003\u0002!C\t1Q)\u001b;iKJT!AH\u0005\u0011\u0005\r\"C\u0002\u0001\u0003\u0007K\u0001!)\u0019\u0001\u0014\u0003\u0003\u0005\u000b\"a\n\u0016\u0011\u0005!A\u0013BA\u0015\n\u0005\u001dqu\u000e\u001e5j]\u001e\u0004\"\u0001C\u0016\n\u00051J!aA!osB\u00111E\f\u0003\u0007_\u0001!)\u0019\u0001\u0014\u0003\u0003\tC\u0001\"\r\u0001\u0003\u0012\u0003\u0006IAF\u0001\bK&$\b.\u001a:!\u0011\u0015\u0019\u0004\u0001\"\u00015\u0003\u0019a\u0014N\\5u}Q\u0011Qg\u000e\t\u0005m\u0001\u0011S&D\u0001\u0003\u0011\u0015!\"\u00071\u0001\u0017\u0011\u001dI\u0004!!A\u0005\u0002i\nAaY8qsV\u00191H\u0010!\u0015\u0005q\n\u0005\u0003\u0002\u001c\u0001{}\u0002\"a\t \u0005\u000b\u0015B$\u0019\u0001\u0014\u0011\u0005\r\u0002E!B\u00189\u0005\u00041\u0003b\u0002\u000b9!\u0003\u0005\rA\u0011\t\u0005/}it\bC\u0004E\u0001E\u0005I\u0011A#\u0002\u001d\r|\u0007/\u001f\u0013eK\u001a\fW\u000f\u001c;%cU\u0019a)\u0015*\u0016\u0003\u001dS#A\u0006%,\u0003%\u0003\"AS(\u000e\u0003-S!\u0001T'\u0002\u0013Ut7\r[3dW\u0016$'B\u0001(\n\u0003)\tgN\\8uCRLwN\\\u0005\u0003!.\u0013\u0011#\u001e8dQ\u0016\u001c7.\u001a3WCJL\u0017M\\2f\t\u0015)3I1\u0001'\t\u0015y3I1\u0001'\u0011\u001d!\u0006!!A\u0005BU\u000bQ\u0002\u001d:pIV\u001cG\u000f\u0015:fM&DX#\u0001,\u0011\u0005]cV\"\u0001-\u000b\u0005eS\u0016\u0001\u00027b]\u001eT\u0011aW\u0001\u0005U\u00064\u0018-\u0003\u0002^1\n11\u000b\u001e:j]\u001eDqa\u0018\u0001\u0002\u0002\u0013\u0005\u0001-\u0001\u0007qe>$Wo\u0019;Be&$\u00180F\u0001b!\tA!-\u0003\u0002d\u0013\t\u0019\u0011J\u001c;\t\u000f\u0015\u0004\u0011\u0011!C\u0001M\u0006q\u0001O]8ek\u000e$X\t\\3nK:$HC\u0001\u0016h\u0011\u001dAG-!AA\u0002\u0005\f1\u0001\u001f\u00132\u0011\u001dQ\u0007!!A\u0005B-\fq\u0002\u001d:pIV\u001cG/\u0013;fe\u0006$xN]\u000b\u0002YB\u0019Q\u000e\u001d\u0016\u000e\u00039T!a\\\u0005\u0002\u0015\r|G\u000e\\3di&|g.\u0003\u0002r]\nA\u0011\n^3sCR|'\u000fC\u0004t\u0001\u0005\u0005I\u0011\u0001;\u0002\u0011\r\fg.R9vC2$\"!\u001e=\u0011\u0005!1\u0018BA<\n\u0005\u001d\u0011un\u001c7fC:Dq\u0001\u001b:\u0002\u0002\u0003\u0007!\u0006C\u0004{\u0001\u0005\u0005I\u0011I>\u0002\u0011!\f7\u000f[\"pI\u0016$\u0012!\u0019\u0005\b{\u0002\t\t\u0011\"\u0011\u007f\u0003!!xn\u0015;sS:<G#\u0001,\t\u0013\u0005\u0005\u0001!!A\u0005B\u0005\r\u0011AB3rk\u0006d7\u000fF\u0002v\u0003\u000bAq\u0001[@\u0002\u0002\u0003\u0007!fB\u0005\u0002\n\t\t\t\u0011#\u0001\u0002\f\u0005yQ\t\u001f;sC\u000e$X\rZ#ji\",'\u000fE\u00027\u0003\u001b1\u0001\"\u0001\u0002\u0002\u0002#\u0005\u0011qB\n\u0005\u0003\u001b9\u0001\u0003C\u00044\u0003\u001b!\t!a\u0005\u0015\u0005\u0005-\u0001\u0002C?\u0002\u000e\u0005\u0005IQ\t@\t\u0015\u0005e\u0011QBA\u0001\n\u0003\u000bY\"A\u0003baBd\u00170\u0006\u0004\u0002\u001e\u0005\r\u0012q\u0005\u000b\u0005\u0003?\tI\u0003\u0005\u00047\u0001\u0005\u0005\u0012Q\u0005\t\u0004G\u0005\rBAB\u0013\u0002\u0018\t\u0007a\u0005E\u0002$\u0003O!aaLA\f\u0005\u00041\u0003b\u0002\u000b\u0002\u0018\u0001\u0007\u00111\u0006\t\u0007/}\t\t#!\n\t\u0015\u0005=\u0012QBA\u0001\n\u0003\u000b\t$A\u0004v]\u0006\u0004\b\u000f\\=\u0016\r\u0005M\u0012qHA\")\u0011\t)$!\u0012\u0011\u000b!\t9$a\u000f\n\u0007\u0005e\u0012B\u0001\u0004PaRLwN\u001c\t\u0007/}\ti$!\u0011\u0011\u0007\r\ny\u0004\u0002\u0004&\u0003[\u0011\rA\n\t\u0004G\u0005\rCAB\u0018\u0002.\t\u0007a\u0005\u0003\u0006\u0002H\u00055\u0012\u0011!a\u0001\u0003\u0013\n1\u0001\u001f\u00131!\u00191\u0004!!\u0010\u0002B!Q\u0011QJA\u0007\u0003\u0003%I!a\u0014\u0002\u0017I,\u0017\r\u001a*fg>dg/\u001a\u000b\u0003\u0003#\u00022aVA*\u0013\r\t)\u0006\u0017\u0002\u0007\u001f\nTWm\u0019;")
public class ExtractedEither<A, B>
implements Product,
Serializable {
    private final Either<A, B> either;

    public static <A, B> Option<Either<A, B>> unapply(ExtractedEither<A, B> extractedEither) {
        return ExtractedEither$.MODULE$.unapply(extractedEither);
    }

    public static <A, B> ExtractedEither<A, B> apply(Either<A, B> either) {
        return ExtractedEither$.MODULE$.apply(either);
    }

    public Either<A, B> either() {
        return this.either;
    }

    public <A, B> ExtractedEither<A, B> copy(Either<A, B> either) {
        return new ExtractedEither<A, B>(either);
    }

    public <A, B> Either<A, B> copy$default$1() {
        return this.either();
    }

    public String productPrefix() {
        return "ExtractedEither";
    }

    public int productArity() {
        return 1;
    }

    public Object productElement(int x$1) {
        int n = x$1;
        switch (n) {
            case 0: {
                break;
            }
            default: {
                throw new IndexOutOfBoundsException(((Object)BoxesRunTime.boxToInteger((int)x$1)).toString());
            }
        }
        return this.either();
    }

    public Iterator<Object> productIterator() {
        return ScalaRunTime$.MODULE$.typedProductIterator((Product)this);
    }

    public boolean canEqual(Object x$1) {
        return x$1 instanceof ExtractedEither;
    }

    public int hashCode() {
        return ScalaRunTime$.MODULE$._hashCode((Product)this);
    }

    public String toString() {
        return ScalaRunTime$.MODULE$._toString((Product)this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object x$1) {
        if (this == x$1) return true;
        Object object = x$1;
        if (!(object instanceof ExtractedEither)) return false;
        boolean bl = true;
        if (!bl) return false;
        ExtractedEither extractedEither = (ExtractedEither)x$1;
        Either<A, B> either = this.either();
        Either<A, B> either2 = extractedEither.either();
        if (either == null) {
            if (either2 != null) {
                return false;
            }
        } else if (!either.equals(either2)) return false;
        if (!extractedEither.canEqual(this)) return false;
        return true;
    }

    public ExtractedEither(Either<A, B> either) {
        this.either = either;
        Product.$init$((Product)this);
    }
}

