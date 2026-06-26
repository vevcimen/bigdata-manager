/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Enumeration$Value
 *  scala.Enumeration$ValueOrdering$
 *  scala.Enumeration$ValueSet
 *  scala.Enumeration$ValueSet$
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.GpfdistServiceState$;
import scala.Enumeration;
import scala.Enumeration$ValueOrdering$;
import scala.Enumeration$ValueSet$;
import scala.reflect.ScalaSignature;

@ScalaSignature(bytes="\u0006\u0001i:Q\u0001E\t\t\u0002q1QAH\t\t\u0002}AQAJ\u0001\u0005\u0002\u001d*A\u0001K\u0001\u0001S!9Q&\u0001b\u0001\n\u0003q\u0003BB\u0018\u0002A\u0003%\u0011\u0006C\u00041\u0003\t\u0007I\u0011\u0001\u0018\t\rE\n\u0001\u0015!\u0003*\u0011\u001d\u0011\u0014A1A\u0005\u00029BaaM\u0001!\u0002\u0013I\u0003b\u0002\u001b\u0002\u0005\u0004%\tA\f\u0005\u0007k\u0005\u0001\u000b\u0011B\u0015\t\u000fY\n!\u0019!C\u0001]!1q'\u0001Q\u0001\n%Bq\u0001O\u0001C\u0002\u0013\u0005a\u0006\u0003\u0004:\u0003\u0001\u0006I!K\u0001\u0014\u000fB4G-[:u'\u0016\u0014h/[2f'R\fG/\u001a\u0006\u0003%M\tQ\"\u001a=uKJt\u0017\r\u001c;bE2,'B\u0001\u000b\u0016\u0003\u0015\u0019\b/\u0019:l\u0015\t1r#A\u0005he\u0016,g\u000e\u001d7v[*\u0011\u0001$G\u0001\ba&4x\u000e^1m\u0015\u0005Q\u0012AA5p\u0007\u0001\u0001\"!H\u0001\u000e\u0003E\u00111c\u00129gI&\u001cHoU3sm&\u001cWm\u0015;bi\u0016\u001c\"!\u0001\u0011\u0011\u0005\u0005\"S\"\u0001\u0012\u000b\u0003\r\nQa]2bY\u0006L!!\n\u0012\u0003\u0017\u0015sW/\\3sCRLwN\\\u0001\u0007y%t\u0017\u000e\u001e \u0015\u0003q\u0011Qa\u0015;bi\u0016\u0004\"AK\u0016\u000e\u0003\u0005I!\u0001\f\u0013\u0003\u000bY\u000bG.^3\u0002\u000fM#x\u000e\u001d9fIV\t\u0011&\u0001\u0005Ti>\u0004\b/\u001a3!\u0003\u00191\u0015-\u001b7fI\u00069a)Y5mK\u0012\u0004\u0013\u0001C*uCJ$\u0018N\\4\u0002\u0013M#\u0018M\u001d;j]\u001e\u0004\u0013aB*uCJ$X\rZ\u0001\t'R\f'\u000f^3eA\u0005A1\u000b^8qa&tw-A\u0005Ti>\u0004\b/\u001b8hA\u00059!+\u001e8oS:<\u0017\u0001\u0003*v]:Lgn\u001a\u0011")
public final class GpfdistServiceState {
    public static Enumeration.Value Running() {
        return GpfdistServiceState$.MODULE$.Running();
    }

    public static Enumeration.Value Stopping() {
        return GpfdistServiceState$.MODULE$.Stopping();
    }

    public static Enumeration.Value Started() {
        return GpfdistServiceState$.MODULE$.Started();
    }

    public static Enumeration.Value Starting() {
        return GpfdistServiceState$.MODULE$.Starting();
    }

    public static Enumeration.Value Failed() {
        return GpfdistServiceState$.MODULE$.Failed();
    }

    public static Enumeration.Value Stopped() {
        return GpfdistServiceState$.MODULE$.Stopped();
    }

    public static Enumeration$ValueSet$ ValueSet() {
        return GpfdistServiceState$.MODULE$.ValueSet();
    }

    public static Enumeration$ValueOrdering$ ValueOrdering() {
        return GpfdistServiceState$.MODULE$.ValueOrdering();
    }

    public static Enumeration.Value withName(String string) {
        return GpfdistServiceState$.MODULE$.withName(string);
    }

    public static Enumeration.Value apply(int n) {
        return GpfdistServiceState$.MODULE$.apply(n);
    }

    public static int maxId() {
        return GpfdistServiceState$.MODULE$.maxId();
    }

    public static Enumeration.ValueSet values() {
        return GpfdistServiceState$.MODULE$.values();
    }

    public static String toString() {
        return GpfdistServiceState$.MODULE$.toString();
    }
}

