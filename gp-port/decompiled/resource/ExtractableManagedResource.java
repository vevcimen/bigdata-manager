/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Option
 *  scala.collection.Seq
 *  scala.reflect.ScalaSignature
 *  scala.util.Try
 */
package resource;

import resource.ExtractedEither;
import resource.ManagedResource;
import scala.Option;
import scala.collection.Seq;
import scala.reflect.ScalaSignature;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\r3q!\u0001\u0002\u0011\u0002G\u0005QA\u0001\u000eFqR\u0014\u0018m\u0019;bE2,W*\u00198bO\u0016$'+Z:pkJ\u001cWMC\u0001\u0004\u0003!\u0011Xm]8ve\u000e,7\u0001A\u000b\u0003\rM\u00192\u0001A\u0004\u000e!\tA1\"D\u0001\n\u0015\u0005Q\u0011!B:dC2\f\u0017B\u0001\u0007\n\u0005\u0019\te.\u001f*fMB\u0019abD\t\u000e\u0003\tI!\u0001\u0005\u0002\u0003\u001f5\u000bg.Y4fIJ+7o\\;sG\u0016\u0004\"AE\n\r\u0001\u00111A\u0003\u0001CC\u0002U\u0011\u0011!Q\t\u0003-e\u0001\"\u0001C\f\n\u0005aI!a\u0002(pi\"Lgn\u001a\t\u0003\u0011iI!aG\u0005\u0003\u0007\u0005s\u0017\u0010C\u0003\u001e\u0001\u0019\u0005a$A\u0002paR,\u0012a\b\t\u0004\u0011\u0001\n\u0012BA\u0011\n\u0005\u0019y\u0005\u000f^5p]\")1\u0005\u0001D\u0001I\u00051Q-\u001b;iKJ,\u0012!\n\t\u0005\u001d\u0019B\u0013#\u0003\u0002(\u0005\tyQ\t\u001f;sC\u000e$X\rZ#ji\",'\u000fE\u0002*Y9j\u0011A\u000b\u0006\u0003W%\t!bY8mY\u0016\u001cG/[8o\u0013\ti#FA\u0002TKF\u0004\"aL\u001c\u000f\u0005A*dBA\u00195\u001b\u0005\u0011$BA\u001a\u0005\u0003\u0019a$o\\8u}%\t!\"\u0003\u00027\u0013\u00059\u0001/Y2lC\u001e,\u0017B\u0001\u001d:\u0005%!\u0006N]8xC\ndWM\u0003\u00027\u0013!)1\b\u0001D\u0001y\u0005)AO]5fIV\tQ\bE\u0002?\u0003Fi\u0011a\u0010\u0006\u0003\u0001&\tA!\u001e;jY&\u0011!i\u0010\u0002\u0004)JL\b")
public interface ExtractableManagedResource<A>
extends ManagedResource<A> {
    public Option<A> opt();

    public ExtractedEither<Seq<Throwable>, A> either();

    public Try<A> tried();
}

