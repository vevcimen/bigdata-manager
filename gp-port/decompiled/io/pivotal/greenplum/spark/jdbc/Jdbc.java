/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.types.StructType
 *  scala.collection.Seq
 *  scala.reflect.ScalaSignature
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.jdbc;

import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.jdbc.ColumnValueRange;
import io.pivotal.greenplum.spark.jdbc.Jdbc$;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.apache.spark.sql.types.StructType;
import scala.collection.Seq;
import scala.reflect.ScalaSignature;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\t%s!\u0002\f\u0018\u0011\u0003\u0011c!\u0002\u0013\u0018\u0011\u0003)\u0003\"\u0002\u001c\u0002\t\u00039\u0004\"\u0002\u001d\u0002\t\u0003I\u0004\"\u00029\u0002\t\u0003\t\b\"\u0002=\u0002\t\u0003I\bbBA\u0002\u0003\u0011\u0005\u0011Q\u0001\u0005\b\u0003\u0017\tA\u0011AA\u0007\u0011\u001d\t\u0019\"\u0001C\u0001\u0003+Aq!a\u0007\u0002\t\u0003\ti\u0002C\u0004\u00026\u0005!\t!a\u000e\t\u0011\u0005\u001d\u0013\u0001\"\u0001\u001a\u0003\u0013Bq!!'\u0002\t\u0003\tY\nC\u0004\u0002,\u0006!I!!,\t\u000f\u0005}\u0016\u0001\"\u0001\u0002B\"9\u0011qY\u0001\u0005\u0002\u0005%\u0007bBAv\u0003\u0011\u0005\u0011Q\u001e\u0005\b\u0003s\fA\u0011AA~\u0011\u001d\ty0\u0001C\u0005\u0005\u0003AqA!\u0007\u0002\t\u0013\u0011Y\u0002C\u0004\u0003<\u0005!\tA!\u0010\t\u000f\t\u0005\u0013\u0001\"\u0001\u0003D\u0005!!\n\u001a2d\u0015\tA\u0012$\u0001\u0003kI\n\u001c'B\u0001\u000e\u001c\u0003\u0015\u0019\b/\u0019:l\u0015\taR$A\u0005he\u0016,g\u000e\u001d7v[*\u0011adH\u0001\ba&4x\u000e^1m\u0015\u0005\u0001\u0013AA5p\u0007\u0001\u0001\"aI\u0001\u000e\u0003]\u0011AA\u00133cGN\u0019\u0011A\n\u0017\u0011\u0005\u001dRS\"\u0001\u0015\u000b\u0003%\nQa]2bY\u0006L!a\u000b\u0015\u0003\r\u0005s\u0017PU3g!\tiC'D\u0001/\u0015\ty\u0003'\u0001\u0007tG\u0006d\u0017\r\\8hO&twM\u0003\u00022e\u0005AA/\u001f9fg\u00064WMC\u00014\u0003\r\u0019w.\\\u0005\u0003k9\u00121\u0002T1{s2{wmZ5oO\u00061A(\u001b8jiz\"\u0012AI\u0001\u0014G>\u0004\u0018\u0010V1cY\u0016$v.\u0012=uKJt\u0017\r\u001c\u000b\u0007uu:u\nW3\u0011\u0005\u001dZ\u0014B\u0001\u001f)\u0005\u0011)f.\u001b;\t\u000by\u001a\u0001\u0019A \u0002\t\r|gN\u001c\t\u0003\u0001\u0016k\u0011!\u0011\u0006\u0003\u0005\u000e\u000b1a]9m\u0015\u0005!\u0015\u0001\u00026bm\u0006L!AR!\u0003\u0015\r{gN\\3di&|g\u000eC\u0003I\u0007\u0001\u0007\u0011*\u0001\u0005te\u000e$\u0016M\u00197f!\tQU*D\u0001L\u0015\ta\u0015$A\u0007fqR,'O\\1mi\u0006\u0014G.Z\u0005\u0003\u001d.\u0013ac\u0012:fK:\u0004H.^7Rk\u0006d\u0017NZ5fI:\u000bW.\u001a\u0005\u0006!\u000e\u0001\r!U\u0001\tKb$H+\u00192mKB\u0011!+\u0016\b\u0003\u0015NK!\u0001V&\u0002-\u001d\u0013X-\u001a8qYVl\u0017+^1mS\u001aLW\r\u001a(b[\u0016L!AV,\u0003\u0013Q+W\u000e\u001d+bE2,'B\u0001+L\u0011\u0015I6\u00011\u0001[\u0003%\u0001(/\u001a3jG\u0006$X\r\u0005\u0002\\E:\u0011A\f\u0019\t\u0003;\"j\u0011A\u0018\u0006\u0003?\u0006\na\u0001\u0010:p_Rt\u0014BA1)\u0003\u0019\u0001&/\u001a3fM&\u00111\r\u001a\u0002\u0007'R\u0014\u0018N\\4\u000b\u0005\u0005D\u0003\"\u00024\u0004\u0001\u00049\u0017aB2pYVlgn\u001d\t\u0004Q6TfBA5l\u001d\ti&.C\u0001*\u0013\ta\u0007&A\u0004qC\u000e\\\u0017mZ3\n\u00059|'aA*fc*\u0011A\u000eK\u0001\u0014Kb$XM\u001d8bYR\u000b'\r\\3Fq&\u001cHo\u001d\u000b\u0004eV4\bCA\u0014t\u0013\t!\bFA\u0004C_>dW-\u00198\t\u000by\"\u0001\u0019A \t\u000b]$\u0001\u0019A)\u0002\u000bQ\f'\r\\3\u0002%\u001d,GoQ8mk6t7/T3uC\u0012\fG/\u0019\u000b\u0005Oj\\x\u0010C\u0003?\u000b\u0001\u0007q\bC\u0003x\u000b\u0001\u0007A\u0010\u0005\u0002S{&\u0011ap\u0016\u0002\u0006)\u0006\u0014G.\u001a\u0005\u0007\u0003\u0003)\u0001\u0019A4\u0002\u0017\r|G.^7o\u001d\u0006lWm]\u0001\u0019O\u0016$H)[:ue&\u0014W\u000f^5p]B{G.[2z\u000fB,D#\u0002.\u0002\b\u0005%\u0001\"\u0002 \u0007\u0001\u0004y\u0004\"B<\u0007\u0001\u0004a\u0018!F4fi\u0012K7\u000f\u001e:jEV$\u0018n\u001c8Q_2L7-\u001f\u000b\u00065\u0006=\u0011\u0011\u0003\u0005\u0006}\u001d\u0001\ra\u0010\u0005\u0006o\u001e\u0001\r\u0001`\u0001\u001cI\u0016$XM]7j]\u0016$\u0015n\u001d;sS\n,H/[8o!>d\u0017nY=\u0015\u000bi\u000b9\"!\u0007\t\u000byB\u0001\u0019A \t\u000b]D\u0001\u0019\u0001?\u0002E\r\u0014X-\u0019;f\u000fB4G-[:u/JLG/\u00192mK\u0016CH/\u001a:oC2$\u0016M\u00197f)5Q\u0014qDA\u0011\u0003G\t)#a\f\u00022!)a(\u0003a\u0001\u007f!)\u0001*\u0003a\u0001y\")\u0001+\u0003a\u0001#\"9\u0011qE\u0005A\u0002\u0005%\u0012aD4qM\u0012L7\u000f\u001e'pG\u0006$\u0018n\u001c8\u0011\u0007)\u000bY#C\u0002\u0002.-\u0013qb\u00129gI&\u001cH\u000fT8dCRLwN\u001c\u0005\u0006M&\u0001\ra\u001a\u0005\u0007\u0003gI\u0001\u0019\u0001.\u0002%\u0011L7\u000f\u001e:jEV$\u0018n\u001c8Q_2L7-_\u0001\u0013e\u0016$(/[3wKN+w-\\3oi&#7\u000f\u0006\u0003\u0002:\u0005\u0015\u0003#B\u0014\u0002<\u0005}\u0012bAA\u001fQ\t)\u0011I\u001d:bsB\u0019q%!\u0011\n\u0007\u0005\r\u0003FA\u0002J]RDQA\u0010\u0006A\u0002}\nqB]3ue&,g/\u001a*fgVdGo]\u000b\u0005\u0003\u0017\n9\u0006\u0006\u0005\u0002N\u0005%\u00151RAH)\u0011\ty%!\u001f\u0015\t\u0005E\u0013\u0011\u000e\t\u0005Q6\f\u0019\u0006\u0005\u0003\u0002V\u0005]C\u0002\u0001\u0003\b\u00033Z!\u0019AA.\u0005\u0005!\u0016\u0003BA/\u0003G\u00022aJA0\u0013\r\t\t\u0007\u000b\u0002\b\u001d>$\b.\u001b8h!\r9\u0013QM\u0005\u0004\u0003OB#aA!os\"I\u00111N\u0006\u0002\u0002\u0003\u000f\u0011QN\u0001\u000bKZLG-\u001a8dK\u0012\n\u0004CBA8\u0003k\n\u0019&\u0004\u0002\u0002r)\u0019\u00111\u000f\u0015\u0002\u000fI,g\r\\3di&!\u0011qOA9\u0005!\u0019E.Y:t)\u0006<\u0007bBA>\u0017\u0001\u0007\u0011QP\u0001\u0007O\u0016$H/\u001a:\u0011\u000f\u001d\ny(a!\u0002T%\u0019\u0011\u0011\u0011\u0015\u0003\u0013\u0019+hn\u0019;j_:\f\u0004c\u0001!\u0002\u0006&\u0019\u0011qQ!\u0003\u0013I+7/\u001e7u'\u0016$\b\"\u0002 \f\u0001\u0004y\u0004BBAG\u0017\u0001\u0007!,\u0001\u0005tc2\fV/\u001a:z\u0011\u001d\t\tj\u0003a\u0001\u0003'\u000bQb]9m!\u0006\u0014\u0018-\\3uKJ\u001c\b#B\u0014\u0002\u0016\u0006\r\u0014bAALQ\tQAH]3qK\u0006$X\r\u001a \u0002/\r|W\u000e];uK\u000e{G.^7o-\u0006dW/\u001a*b]\u001e,G\u0003CAO\u0003G\u000b)+a*\u0011\u0007\r\ny*C\u0002\u0002\"^\u0011\u0001cQ8mk6tg+\u00197vKJ\u000bgnZ3\t\u000byb\u0001\u0019A \t\u000b]d\u0001\u0019\u0001?\t\r\u0005%F\u00021\u0001[\u0003)\u0019w\u000e\\;n]:\u000bW.Z\u0001\u0016cV,'/_\"pYVlgNV1mk\u0016\u0014\u0016M\\4f)\u0019\ty+a/\u0002>B1\u0011\u0011WA\\\u0003;k!!a-\u000b\u0007\u0005U\u0006&\u0001\u0003vi&d\u0017\u0002BA]\u0003g\u00131\u0001\u0016:z\u0011\u0015qT\u00021\u0001@\u0011\u0019\ti)\u0004a\u00015\u0006)\u0002/\u0019:tK\u000e{G.^7o-\u0006dW/\u001a*b]\u001e,G\u0003BAX\u0003\u0007Dq!!2\u000f\u0001\u0004\t\u0019)\u0001\u0002sg\u0006a!/Z:pYZ,G+\u00192mKRA\u00111ZAr\u0003K\fI\u000f\u0005\u0003\u0002N\u0006}WBAAh\u0015\u0011\t\t.a5\u0002\u000bQL\b/Z:\u000b\u0007\t\u000b)NC\u0002\u001b\u0003/TA!!7\u0002\\\u00061\u0011\r]1dQ\u0016T!!!8\u0002\u0007=\u0014x-\u0003\u0003\u0002b\u0006='AC*ueV\u001cG\u000fV=qK\")ah\u0004a\u0001\u007f!1\u0011q]\bA\u0002i\u000b1!\u001e:m\u0011\u00159x\u00021\u0001J\u000399W\r^\"pYVlgNT1nKN$2aZAx\u0011\u001d\t\t\u0010\u0005a\u0001\u0003g\fA!\\3uCB\u0019\u0001)!>\n\u0007\u0005]\u0018IA\tSKN,H\u000e^*fi6+G/\u0019#bi\u0006\f\u0011bZ3u'\u000eDW-\\1\u0015\t\u0005-\u0017Q \u0005\b\u0003c\f\u0002\u0019AAz\u0003=9W\r^\"bi\u0006d\u0017p\u001d;UsB,GC\u0003B\u0002\u0005\u0013\u0011iA!\u0005\u0003\u0016A!\u0011Q\u001aB\u0003\u0013\u0011\u00119!a4\u0003\u0011\u0011\u000bG/\u0019+za\u0016DqAa\u0003\u0013\u0001\u0004\ty$A\u0004tc2$\u0016\u0010]3\t\u000f\t=!\u00031\u0001\u0002@\u0005I\u0001O]3dSNLwN\u001c\u0005\b\u0005'\u0011\u0002\u0019AA \u0003\u0015\u00198-\u00197f\u0011\u0019\u00119B\u0005a\u0001e\u000611/[4oK\u0012\f1bY8mY\u0016\u001cGO\u0012:p[V!!Q\u0004B\u0016)\u0011\u0011yBa\u000e\u0015\t\t\u0005\"1\u0007\u000b\u0005\u0005G\u0011i\u0003E\u0003i\u0005K\u0011I#C\u0002\u0003(=\u0014aAV3di>\u0014\b\u0003BA+\u0005W!q!!\u0017\u0014\u0005\u0004\tY\u0006C\u0005\u00030M\t\t\u0011q\u0001\u00032\u0005QQM^5eK:\u001cW\r\n\u001a\u0011\r\u0005=\u0014Q\u000fB\u0015\u0011\u001d\tYh\u0005a\u0001\u0005k\u0001raJA@\u0003\u0007\u0013I\u0003C\u0004\u0003:M\u0001\r!a!\u0002\u0013I,7/\u001e7u'\u0016$\u0018aG4fi\u0012K7\u000f\u001e:jEV$X\r\u001a+sC:\u001c\u0018m\u0019;j_:LE\rF\u0002[\u0005\u007fAQA\u0010\u000bA\u0002}\naBZ8s[\u0006$8+\u001d7Rk\u0016\u0014\u0018\u0010F\u0002[\u0005\u000bBaAa\u0012\u0016\u0001\u0004Q\u0016!A:")
public final class Jdbc {
    public static String formatSqlQuery(String string) {
        return Jdbc$.MODULE$.formatSqlQuery(string);
    }

    public static String getDistributedTransactionId(Connection connection) {
        return Jdbc$.MODULE$.getDistributedTransactionId(connection);
    }

    public static StructType getSchema(ResultSetMetaData resultSetMetaData) {
        return Jdbc$.MODULE$.getSchema(resultSetMetaData);
    }

    public static Seq<String> getColumnNames(ResultSetMetaData resultSetMetaData) {
        return Jdbc$.MODULE$.getColumnNames(resultSetMetaData);
    }

    public static StructType resolveTable(Connection connection, String string, GreenplumQualifiedName greenplumQualifiedName) {
        return Jdbc$.MODULE$.resolveTable(connection, string, greenplumQualifiedName);
    }

    public static Try<ColumnValueRange> parseColumnValueRange(ResultSet resultSet) {
        return Jdbc$.MODULE$.parseColumnValueRange(resultSet);
    }

    public static ColumnValueRange computeColumnValueRange(Connection connection, GreenplumQualifiedName.Table table, String string) {
        return Jdbc$.MODULE$.computeColumnValueRange(connection, table, string);
    }

    public static int[] retrieveSegmentIds(Connection connection) {
        return Jdbc$.MODULE$.retrieveSegmentIds(connection);
    }

    public static void createGpfdistWritableExternalTable(Connection connection, GreenplumQualifiedName.Table table, GreenplumQualifiedName.TempTable tempTable, GpfdistLocation gpfdistLocation, Seq<String> seq, String string) {
        Jdbc$.MODULE$.createGpfdistWritableExternalTable(connection, table, tempTable, gpfdistLocation, seq, string);
    }

    public static String determineDistributionPolicy(Connection connection, GreenplumQualifiedName.Table table) {
        return Jdbc$.MODULE$.determineDistributionPolicy(connection, table);
    }

    public static String getDistributionPolicy(Connection connection, GreenplumQualifiedName.Table table) {
        return Jdbc$.MODULE$.getDistributionPolicy(connection, table);
    }

    public static String getDistributionPolicyGp5(Connection connection, GreenplumQualifiedName.Table table) {
        return Jdbc$.MODULE$.getDistributionPolicyGp5(connection, table);
    }

    public static Seq<String> getColumnsMetadata(Connection connection, GreenplumQualifiedName.Table table, Seq<String> seq) {
        return Jdbc$.MODULE$.getColumnsMetadata(connection, table, seq);
    }

    public static boolean externalTableExists(Connection connection, GreenplumQualifiedName.TempTable tempTable) {
        return Jdbc$.MODULE$.externalTableExists(connection, tempTable);
    }

    public static void copyTableToExternal(Connection connection, GreenplumQualifiedName greenplumQualifiedName, GreenplumQualifiedName.TempTable tempTable, String string, Seq<String> seq) {
        Jdbc$.MODULE$.copyTableToExternal(connection, greenplumQualifiedName, tempTable, string, seq);
    }
}

