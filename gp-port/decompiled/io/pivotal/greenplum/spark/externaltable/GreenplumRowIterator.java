/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.SparkEnv$
 *  org.apache.spark.sql.catalyst.InternalRow
 *  org.apache.spark.sql.catalyst.expressions.SpecificInternalRow
 *  org.apache.spark.sql.sources.Filter
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.StructType
 *  scala.Array$
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.MatchError
 *  scala.Option
 *  scala.PartialFunction
 *  scala.Predef$
 *  scala.Predef$$less$colon$less
 *  scala.Serializable
 *  scala.Tuple2
 *  scala.collection.BufferedIterator
 *  scala.collection.GenTraversableOnce
 *  scala.collection.Iterable
 *  scala.collection.Iterator
 *  scala.collection.Iterator$GroupedIterator
 *  scala.collection.Seq
 *  scala.collection.Traversable
 *  scala.collection.TraversableOnce
 *  scala.collection.generic.CanBuildFrom
 *  scala.collection.immutable.IndexedSeq
 *  scala.collection.immutable.List
 *  scala.collection.immutable.Map
 *  scala.collection.immutable.Set
 *  scala.collection.immutable.Stream
 *  scala.collection.immutable.StringOps
 *  scala.collection.immutable.Vector
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.collection.mutable.Buffer
 *  scala.collection.mutable.StringBuilder
 *  scala.math.Numeric
 *  scala.math.Ordering
 *  scala.reflect.ClassTag
 *  scala.reflect.ClassTag$
 *  scala.reflect.ScalaSignature
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.runtime.Nothing$
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.ConnectorUtils;
import io.pivotal.greenplum.spark.ErrorHandling$;
import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import io.pivotal.greenplum.spark.externaltable.DataIterator;
import io.pivotal.greenplum.spark.externaltable.DataTypeConverterFactory$;
import io.pivotal.greenplum.spark.externaltable.GpfdistLocation;
import io.pivotal.greenplum.spark.externaltable.GpfdistService;
import io.pivotal.greenplum.spark.externaltable.GpfdistServiceManager$;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName$;
import io.pivotal.greenplum.spark.externaltable.GreenplumRowIterator$;
import io.pivotal.greenplum.spark.externaltable.GreenplumTableManager$;
import io.pivotal.greenplum.spark.jdbc.ConnectionManager$;
import io.pivotal.greenplum.spark.jdbc.Jdbc$;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import org.apache.spark.SparkEnv$;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.SpecificInternalRow;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;
import org.postgresql.util.PSQLException;
import scala.Array$;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.MatchError;
import scala.Option;
import scala.PartialFunction;
import scala.Predef$;
import scala.Tuple2;
import scala.collection.BufferedIterator;
import scala.collection.GenTraversableOnce;
import scala.collection.Iterable;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.Traversable;
import scala.collection.TraversableOnce;
import scala.collection.generic.CanBuildFrom;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.collection.immutable.Set;
import scala.collection.immutable.Stream;
import scala.collection.immutable.StringOps;
import scala.collection.immutable.Vector;
import scala.collection.mutable.ArrayOps;
import scala.collection.mutable.Buffer;
import scala.collection.mutable.StringBuilder;
import scala.math.Numeric;
import scala.math.Ordering;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;
import scala.reflect.ScalaSignature;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.runtime.Nothing$;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

@ScalaSignature(bytes="\u0006\u0001\u0005-f\u0001B\u000f\u001f\u0001%B\u0001b\u0015\u0001\u0003\u0002\u0003\u0006I\u0001\u0016\u0005\t9\u0002\u0011\t\u0011)A\u0005;\"A\u0011\r\u0001B\u0001B\u0003%!\r\u0003\u0005i\u0001\t\u0005\t\u0015!\u0003j\u0011!y\u0007A!A!\u0002\u0013\u0001\b\u0002C:\u0001\u0005\u0003\u0005\u000b\u0011\u0002;\t\u0011u\u0004!\u0011!Q\u0001\nyDq!a\u0001\u0001\t\u0003\t)\u0001C\u0005\u0002\u001a\u0001\u0011\r\u0011\"\u0003\u0002\u001c!A\u00111\u0006\u0001!\u0002\u0013\ti\u0002C\u0005\u0002.\u0001\u0011\r\u0011\"\u0003\u00020!9\u0011\u0011\u0007\u0001!\u0002\u0013!\u0006\"CA\u001a\u0001\t\u0007I\u0011BA\u0018\u0011\u001d\t)\u0004\u0001Q\u0001\nQC\u0011\"a\u000e\u0001\u0005\u0004%\t!!\u000f\t\u0011\u0005\u0005\u0003\u0001)A\u0005\u0003wA\u0011\"a\u0011\u0001\u0001\u0004%I!!\u0012\t\u0013\u00055\u0003\u00011A\u0005\n\u0005=\u0003\u0002CA.\u0001\u0001\u0006K!a\u0012\t\u0013\u0005u\u0003A1A\u0005\u0002\u0005}\u0003\u0002CA;\u0001\u0001\u0006I!!\u0019\t\u000f\u0005]\u0004\u0001\"\u0011\u0002z!9\u0011\u0011\u0011\u0001\u0005B\u0005\ruaBAC=!\u0005\u0011q\u0011\u0004\u0007;yA\t!!#\t\u000f\u0005\r\u0011\u0004\"\u0001\u0002\f\"9\u0011QR\r\u0005\u0002\u0005=\u0005\"CAJ3E\u0005I\u0011AAK\u0005Q9%/Z3oa2,XNU8x\u0013R,'/\u0019;pe*\u0011q\u0004I\u0001\u000eKb$XM\u001d8bYR\f'\r\\3\u000b\u0005\u0005\u0012\u0013!B:qCJ\\'BA\u0012%\u0003%9'/Z3oa2,XN\u0003\u0002&M\u00059\u0001/\u001b<pi\u0006d'\"A\u0014\u0002\u0005%|7\u0001A\n\u0005\u0001)\u0002\u0014\n\u0005\u0002,]5\tAFC\u0001.\u0003\u0015\u00198-\u00197b\u0013\tyCF\u0001\u0004B]f\u0014VM\u001a\t\u0004cebdB\u0001\u001a8\u001d\t\u0019d'D\u00015\u0015\t)\u0004&\u0001\u0004=e>|GOP\u0005\u0002[%\u0011\u0001\bL\u0001\ba\u0006\u001c7.Y4f\u0013\tQ4H\u0001\u0005Ji\u0016\u0014\u0018\r^8s\u0015\tAD\u0006\u0005\u0002>\u000f6\taH\u0003\u0002@\u0001\u0006A1-\u0019;bYf\u001cHO\u0003\u0002B\u0005\u0006\u00191/\u001d7\u000b\u0005\u0005\u001a%B\u0001#F\u0003\u0019\t\u0007/Y2iK*\ta)A\u0002pe\u001eL!\u0001\u0013 \u0003\u0017%sG/\u001a:oC2\u0014vn\u001e\t\u0003\u0015Fk\u0011a\u0013\u0006\u0003\u00196\u000bAb]2bY\u0006dwnZ4j]\u001eT!AT(\u0002\u0011QL\b/Z:bM\u0016T\u0011\u0001U\u0001\u0004G>l\u0017B\u0001*L\u0005-a\u0015M_=M_\u001e<\u0017N\\4\u0002\u001b\u0005\u0004\b\u000f\\5dCRLwN\\%e!\t)\u0016L\u0004\u0002W/B\u00111\u0007L\u0005\u000312\na\u0001\u0015:fI\u00164\u0017B\u0001.\\\u0005\u0019\u0019FO]5oO*\u0011\u0001\fL\u0001\na\u0006\u0014H/\u001b;j_:\u0004\"AX0\u000e\u0003\u0001J!\u0001\u0019\u0011\u0003%\u001d\u0013X-\u001a8qYVl\u0007+\u0019:uSRLwN\\\u0001\u0007g\u000eDW-\\1\u0011\u0005\r4W\"\u00013\u000b\u0005\u0015\u0004\u0015!\u0002;za\u0016\u001c\u0018BA4e\u0005)\u0019FO];diRK\b/Z\u0001\u0011OJ,WM\u001c9mk6|\u0005\u000f^5p]N\u0004\"A[7\u000e\u0003-T!\u0001\u001c\u0011\u0002\t\r|gNZ\u0005\u0003].\u0014\u0001c\u0012:fK:\u0004H.^7PaRLwN\\:\u0002\u0015\u0005dGnQ8mk6t7\u000fE\u00022cRK!A]\u001e\u0003\u0007M+\u0017/A\u0004gS2$XM]:\u0011\u0007-*x/\u0003\u0002wY\t)\u0011I\u001d:bsB\u0011\u0001p_\u0007\u0002s*\u0011!\u0010Q\u0001\bg>,(oY3t\u0013\ta\u0018P\u0001\u0004GS2$XM]\u0001\u000fG>tg.Z2u_J,F/\u001b7t!\tqv0C\u0002\u0002\u0002\u0001\u0012abQ8o]\u0016\u001cGo\u001c:Vi&d7/\u0001\u0004=S:LGO\u0010\u000b\u0011\u0003\u000f\tY!!\u0004\u0002\u0010\u0005E\u00111CA\u000b\u0003/\u00012!!\u0003\u0001\u001b\u0005q\u0002\"B*\t\u0001\u0004!\u0006\"\u0002/\t\u0001\u0004i\u0006\"B1\t\u0001\u0004\u0011\u0007\"\u00025\t\u0001\u0004I\u0007\"B8\t\u0001\u0004\u0001\b\"B:\t\u0001\u0004!\bbB?\t!\u0003\u0005\rA`\u0001\u0005G>tg.\u0006\u0002\u0002\u001eA!\u0011qDA\u0014\u001b\t\t\tCC\u0002B\u0003GQ!!!\n\u0002\t)\fg/Y\u0005\u0005\u0003S\t\tC\u0001\u0006D_:tWm\u0019;j_:\fQaY8o]\u0002\n\u0011CZ5mi\u0016\u0014x\u000b[3sK\u000ec\u0017-^:f+\u0005!\u0016A\u00054jYR,'o\u00165fe\u0016\u001cE.Y;tK\u0002\nab\u001e5fe\u0016\u0004&/\u001a3jG\u0006$X-A\bxQ\u0016\u0014X\r\u0015:fI&\u001c\u0017\r^3!\u00031!\u0017\r^1Ji\u0016\u0014\u0018\r^8s+\t\tY\u0004\u0005\u0003\u0002\n\u0005u\u0012bAA =\taA)\u0019;b\u0013R,'/\u0019;pe\u0006iA-\u0019;b\u0013R,'/\u0019;pe\u0002\na\u0002\u001d:pG\u0016\u001c8/\u001a3D_VtG/\u0006\u0002\u0002HA\u00191&!\u0013\n\u0007\u0005-CFA\u0002J]R\f!\u0003\u001d:pG\u0016\u001c8/\u001a3D_VtGo\u0018\u0013fcR!\u0011\u0011KA,!\rY\u00131K\u0005\u0004\u0003+b#\u0001B+oSRD\u0011\"!\u0017\u0013\u0003\u0003\u0005\r!a\u0012\u0002\u0007a$\u0013'A\bqe>\u001cWm]:fI\u000e{WO\u001c;!\u0003)\u0019wN\u001c<feR,'o]\u000b\u0003\u0003C\u0002BaK;\u0002dAA1&!\u001aU\u0003S\n\t&C\u0002\u0002h1\u0012\u0011BR;oGRLwN\u001c\u001a\u0011\t\u0005-\u0014\u0011O\u0007\u0003\u0003[R1!a\u001c?\u0003-)\u0007\u0010\u001d:fgNLwN\\:\n\t\u0005M\u0014Q\u000e\u0002\u0014'B,7-\u001b4jG&sG/\u001a:oC2\u0014vn^\u0001\fG>tg/\u001a:uKJ\u001c\b%A\u0004iCNtU\r\u001f;\u0016\u0005\u0005m\u0004cA\u0016\u0002~%\u0019\u0011q\u0010\u0017\u0003\u000f\t{w\u000e\\3b]\u0006!a.\u001a=u)\u0005a\u0014\u0001F$sK\u0016t\u0007\u000f\\;n%><\u0018\n^3sCR|'\u000fE\u0002\u0002\ne\u0019\"!\u0007\u0016\u0015\u0005\u0005\u001d\u0015\u0001\u00064jYR,'o\u00165fe\u0016\u0004&/\u001a3jG\u0006$X\rF\u0002U\u0003#CQa]\u000eA\u0002Q\f1\u0004\n7fgNLg.\u001b;%OJ,\u0017\r^3sI\u0011,g-Y;mi\u0012:TCAALU\rq\u0018\u0011T\u0016\u0003\u00037\u0003B!!(\u0002(6\u0011\u0011q\u0014\u0006\u0005\u0003C\u000b\u0019+A\u0005v]\u000eDWmY6fI*\u0019\u0011Q\u0015\u0017\u0002\u0015\u0005tgn\u001c;bi&|g.\u0003\u0003\u0002*\u0006}%!E;oG\",7m[3e-\u0006\u0014\u0018.\u00198dK\u0002")
public class GreenplumRowIterator
implements Iterator<InternalRow>,
LazyLogging {
    private final GreenplumPartition partition;
    private final StructType schema;
    private final GreenplumOptions greenplumOptions;
    private final Seq<String> allColumns;
    private final Connection conn;
    private final String filterWhereClause;
    private final String wherePredicate;
    private final DataIterator dataIterator;
    private int processedCount;
    private final Function2<String, SpecificInternalRow, BoxedUnit>[] converters;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    public static ConnectorUtils $lessinit$greater$default$7() {
        return GreenplumRowIterator$.MODULE$.$lessinit$greater$default$7();
    }

    public static String filterWherePredicate(Filter[] filterArray) {
        return GreenplumRowIterator$.MODULE$.filterWherePredicate(filterArray);
    }

    public Iterator<InternalRow> seq() {
        return Iterator.seq$((Iterator)this);
    }

    public boolean isEmpty() {
        return Iterator.isEmpty$((Iterator)this);
    }

    public boolean isTraversableAgain() {
        return Iterator.isTraversableAgain$((Iterator)this);
    }

    public boolean hasDefiniteSize() {
        return Iterator.hasDefiniteSize$((Iterator)this);
    }

    public Iterator<InternalRow> take(int n) {
        return Iterator.take$((Iterator)this, (int)n);
    }

    public Iterator<InternalRow> drop(int n) {
        return Iterator.drop$((Iterator)this, (int)n);
    }

    public Iterator<InternalRow> slice(int from, int until) {
        return Iterator.slice$((Iterator)this, (int)from, (int)until);
    }

    public Iterator<InternalRow> sliceIterator(int from, int until) {
        return Iterator.sliceIterator$((Iterator)this, (int)from, (int)until);
    }

    public <B> Iterator<B> map(Function1<InternalRow, B> f) {
        return Iterator.map$((Iterator)this, f);
    }

    public <B> Iterator<B> $plus$plus(Function0<GenTraversableOnce<B>> that) {
        return Iterator.$plus$plus$((Iterator)this, that);
    }

    public <B> Iterator<B> flatMap(Function1<InternalRow, GenTraversableOnce<B>> f) {
        return Iterator.flatMap$((Iterator)this, f);
    }

    public Iterator<InternalRow> filter(Function1<InternalRow, Object> p) {
        return Iterator.filter$((Iterator)this, p);
    }

    public <B> boolean corresponds(GenTraversableOnce<B> that, Function2<InternalRow, B, Object> p) {
        return Iterator.corresponds$((Iterator)this, that, p);
    }

    public Iterator<InternalRow> withFilter(Function1<InternalRow, Object> p) {
        return Iterator.withFilter$((Iterator)this, p);
    }

    public Iterator<InternalRow> filterNot(Function1<InternalRow, Object> p) {
        return Iterator.filterNot$((Iterator)this, p);
    }

    public <B> Iterator<B> collect(PartialFunction<InternalRow, B> pf) {
        return Iterator.collect$((Iterator)this, pf);
    }

    public <B> Iterator<B> scanLeft(B z, Function2<B, InternalRow, B> op) {
        return Iterator.scanLeft$((Iterator)this, z, op);
    }

    public <B> Iterator<B> scanRight(B z, Function2<InternalRow, B, B> op) {
        return Iterator.scanRight$((Iterator)this, z, op);
    }

    public Iterator<InternalRow> takeWhile(Function1<InternalRow, Object> p) {
        return Iterator.takeWhile$((Iterator)this, p);
    }

    public Tuple2<Iterator<InternalRow>, Iterator<InternalRow>> partition(Function1<InternalRow, Object> p) {
        return Iterator.partition$((Iterator)this, p);
    }

    public Tuple2<Iterator<InternalRow>, Iterator<InternalRow>> span(Function1<InternalRow, Object> p) {
        return Iterator.span$((Iterator)this, p);
    }

    public Iterator<InternalRow> dropWhile(Function1<InternalRow, Object> p) {
        return Iterator.dropWhile$((Iterator)this, p);
    }

    public <B> Iterator<Tuple2<InternalRow, B>> zip(Iterator<B> that) {
        return Iterator.zip$((Iterator)this, that);
    }

    public <A1> Iterator<A1> padTo(int len, A1 elem) {
        return Iterator.padTo$((Iterator)this, (int)len, elem);
    }

    public Iterator<Tuple2<InternalRow, Object>> zipWithIndex() {
        return Iterator.zipWithIndex$((Iterator)this);
    }

    public <B, A1, B1> Iterator<Tuple2<A1, B1>> zipAll(Iterator<B> that, A1 thisElem, B1 thatElem) {
        return Iterator.zipAll$((Iterator)this, that, thisElem, thatElem);
    }

    public <U> void foreach(Function1<InternalRow, U> f) {
        Iterator.foreach$((Iterator)this, f);
    }

    public boolean forall(Function1<InternalRow, Object> p) {
        return Iterator.forall$((Iterator)this, p);
    }

    public boolean exists(Function1<InternalRow, Object> p) {
        return Iterator.exists$((Iterator)this, p);
    }

    public boolean contains(Object elem) {
        return Iterator.contains$((Iterator)this, (Object)elem);
    }

    public Option<InternalRow> find(Function1<InternalRow, Object> p) {
        return Iterator.find$((Iterator)this, p);
    }

    public int indexWhere(Function1<InternalRow, Object> p) {
        return Iterator.indexWhere$((Iterator)this, p);
    }

    public int indexWhere(Function1<InternalRow, Object> p, int from) {
        return Iterator.indexWhere$((Iterator)this, p, (int)from);
    }

    public <B> int indexOf(B elem) {
        return Iterator.indexOf$((Iterator)this, elem);
    }

    public <B> int indexOf(B elem, int from) {
        return Iterator.indexOf$((Iterator)this, elem, (int)from);
    }

    public BufferedIterator<InternalRow> buffered() {
        return Iterator.buffered$((Iterator)this);
    }

    public <B> Iterator.GroupedIterator<B> grouped(int size) {
        return Iterator.grouped$((Iterator)this, (int)size);
    }

    public <B> Iterator.GroupedIterator<B> sliding(int size, int step) {
        return Iterator.sliding$((Iterator)this, (int)size, (int)step);
    }

    public <B> int sliding$default$2() {
        return Iterator.sliding$default$2$((Iterator)this);
    }

    public int length() {
        return Iterator.length$((Iterator)this);
    }

    public Tuple2<Iterator<InternalRow>, Iterator<InternalRow>> duplicate() {
        return Iterator.duplicate$((Iterator)this);
    }

    public <B> Iterator<B> patch(int from, Iterator<B> patchElems, int replaced) {
        return Iterator.patch$((Iterator)this, (int)from, patchElems, (int)replaced);
    }

    public <B> void copyToArray(Object xs, int start, int len) {
        Iterator.copyToArray$((Iterator)this, (Object)xs, (int)start, (int)len);
    }

    public boolean sameElements(Iterator<?> that) {
        return Iterator.sameElements$((Iterator)this, that);
    }

    public Traversable<InternalRow> toTraversable() {
        return Iterator.toTraversable$((Iterator)this);
    }

    public Iterator<InternalRow> toIterator() {
        return Iterator.toIterator$((Iterator)this);
    }

    public Stream<InternalRow> toStream() {
        return Iterator.toStream$((Iterator)this);
    }

    public String toString() {
        return Iterator.toString$((Iterator)this);
    }

    public List<InternalRow> reversed() {
        return TraversableOnce.reversed$((TraversableOnce)this);
    }

    public int size() {
        return TraversableOnce.size$((TraversableOnce)this);
    }

    public boolean nonEmpty() {
        return TraversableOnce.nonEmpty$((TraversableOnce)this);
    }

    public int count(Function1<InternalRow, Object> p) {
        return TraversableOnce.count$((TraversableOnce)this, p);
    }

    public <B> Option<B> collectFirst(PartialFunction<InternalRow, B> pf) {
        return TraversableOnce.collectFirst$((TraversableOnce)this, pf);
    }

    public <B> B $div$colon(B z, Function2<B, InternalRow, B> op) {
        return (B)TraversableOnce.$div$colon$((TraversableOnce)this, z, op);
    }

    public <B> B $colon$bslash(B z, Function2<InternalRow, B, B> op) {
        return (B)TraversableOnce.$colon$bslash$((TraversableOnce)this, z, op);
    }

    public <B> B foldLeft(B z, Function2<B, InternalRow, B> op) {
        return (B)TraversableOnce.foldLeft$((TraversableOnce)this, z, op);
    }

    public <B> B foldRight(B z, Function2<InternalRow, B, B> op) {
        return (B)TraversableOnce.foldRight$((TraversableOnce)this, z, op);
    }

    public <B> B reduceLeft(Function2<B, InternalRow, B> op) {
        return (B)TraversableOnce.reduceLeft$((TraversableOnce)this, op);
    }

    public <B> B reduceRight(Function2<InternalRow, B, B> op) {
        return (B)TraversableOnce.reduceRight$((TraversableOnce)this, op);
    }

    public <B> Option<B> reduceLeftOption(Function2<B, InternalRow, B> op) {
        return TraversableOnce.reduceLeftOption$((TraversableOnce)this, op);
    }

    public <B> Option<B> reduceRightOption(Function2<InternalRow, B, B> op) {
        return TraversableOnce.reduceRightOption$((TraversableOnce)this, op);
    }

    public <A1> A1 reduce(Function2<A1, A1, A1> op) {
        return (A1)TraversableOnce.reduce$((TraversableOnce)this, op);
    }

    public <A1> Option<A1> reduceOption(Function2<A1, A1, A1> op) {
        return TraversableOnce.reduceOption$((TraversableOnce)this, op);
    }

    public <A1> A1 fold(A1 z, Function2<A1, A1, A1> op) {
        return (A1)TraversableOnce.fold$((TraversableOnce)this, z, op);
    }

    public <B> B aggregate(Function0<B> z, Function2<B, InternalRow, B> seqop, Function2<B, B, B> combop) {
        return (B)TraversableOnce.aggregate$((TraversableOnce)this, z, seqop, combop);
    }

    public <B> B sum(Numeric<B> num) {
        return (B)TraversableOnce.sum$((TraversableOnce)this, num);
    }

    public <B> B product(Numeric<B> num) {
        return (B)TraversableOnce.product$((TraversableOnce)this, num);
    }

    public Object min(Ordering cmp) {
        return TraversableOnce.min$((TraversableOnce)this, (Ordering)cmp);
    }

    public Object max(Ordering cmp) {
        return TraversableOnce.max$((TraversableOnce)this, (Ordering)cmp);
    }

    public Object maxBy(Function1 f, Ordering cmp) {
        return TraversableOnce.maxBy$((TraversableOnce)this, (Function1)f, (Ordering)cmp);
    }

    public Object minBy(Function1 f, Ordering cmp) {
        return TraversableOnce.minBy$((TraversableOnce)this, (Function1)f, (Ordering)cmp);
    }

    public <B> void copyToBuffer(Buffer<B> dest) {
        TraversableOnce.copyToBuffer$((TraversableOnce)this, dest);
    }

    public <B> void copyToArray(Object xs, int start) {
        TraversableOnce.copyToArray$((TraversableOnce)this, (Object)xs, (int)start);
    }

    public <B> void copyToArray(Object xs) {
        TraversableOnce.copyToArray$((TraversableOnce)this, (Object)xs);
    }

    public <B> Object toArray(ClassTag<B> evidence$1) {
        return TraversableOnce.toArray$((TraversableOnce)this, evidence$1);
    }

    public List<InternalRow> toList() {
        return TraversableOnce.toList$((TraversableOnce)this);
    }

    public Iterable<InternalRow> toIterable() {
        return TraversableOnce.toIterable$((TraversableOnce)this);
    }

    public Seq<InternalRow> toSeq() {
        return TraversableOnce.toSeq$((TraversableOnce)this);
    }

    public IndexedSeq<InternalRow> toIndexedSeq() {
        return TraversableOnce.toIndexedSeq$((TraversableOnce)this);
    }

    public <B> Buffer<B> toBuffer() {
        return TraversableOnce.toBuffer$((TraversableOnce)this);
    }

    public <B> Set<B> toSet() {
        return TraversableOnce.toSet$((TraversableOnce)this);
    }

    public Vector<InternalRow> toVector() {
        return TraversableOnce.toVector$((TraversableOnce)this);
    }

    public <Col> Col to(CanBuildFrom<Nothing$, InternalRow, Col> cbf) {
        return (Col)TraversableOnce.to$((TraversableOnce)this, cbf);
    }

    public <T, U> Map<T, U> toMap(Predef$.less.colon.less<InternalRow, Tuple2<T, U>> ev) {
        return TraversableOnce.toMap$((TraversableOnce)this, ev);
    }

    public String mkString(String start, String sep, String end) {
        return TraversableOnce.mkString$((TraversableOnce)this, (String)start, (String)sep, (String)end);
    }

    public String mkString(String sep) {
        return TraversableOnce.mkString$((TraversableOnce)this, (String)sep);
    }

    public String mkString() {
        return TraversableOnce.mkString$((TraversableOnce)this);
    }

    public StringBuilder addString(StringBuilder b, String start, String sep, String end) {
        return TraversableOnce.addString$((TraversableOnce)this, (StringBuilder)b, (String)start, (String)sep, (String)end);
    }

    public StringBuilder addString(StringBuilder b, String sep) {
        return TraversableOnce.addString$((TraversableOnce)this, (StringBuilder)b, (String)sep);
    }

    public StringBuilder addString(StringBuilder b) {
        return TraversableOnce.addString$((TraversableOnce)this, (StringBuilder)b);
    }

    public int sizeHintIfCheap() {
        return GenTraversableOnce.sizeHintIfCheap$((GenTraversableOnce)this);
    }

    private Logger logger$lzycompute() {
        GreenplumRowIterator greenplumRowIterator = this;
        synchronized (greenplumRowIterator) {
            if (!this.bitmap$trans$0) {
                this.logger = LazyLogging.logger$(this);
                this.bitmap$trans$0 = true;
            }
        }
        return this.logger;
    }

    @Override
    public Logger logger() {
        if (!this.bitmap$trans$0) {
            return this.logger$lzycompute();
        }
        return this.logger;
    }

    private Connection conn() {
        return this.conn;
    }

    private String filterWhereClause() {
        return this.filterWhereClause;
    }

    private String wherePredicate() {
        return this.wherePredicate;
    }

    public DataIterator dataIterator() {
        return this.dataIterator;
    }

    private int processedCount() {
        return this.processedCount;
    }

    private void processedCount_$eq(int x$1) {
        this.processedCount = x$1;
    }

    public Function2<String, SpecificInternalRow, BoxedUnit>[] converters() {
        return this.converters;
    }

    public boolean hasNext() {
        return this.dataIterator().hasNext();
    }

    public InternalRow next() {
        BoxedUnit boxedUnit;
        SpecificInternalRow row = new SpecificInternalRow((Seq)Predef$.MODULE$.wrapRefArray((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])this.schema.fields())).map((Function1 & Serializable & scala.Serializable)x -> x.dataType(), Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(DataType.class)))));
        try {
            String[] record = this.dataIterator().next();
            new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])record)).zipWithIndex(Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(Tuple2.class))))).foreach((Function1 & Serializable & scala.Serializable)x0$1 -> {
                GreenplumRowIterator.$anonfun$next$2(this, row, x0$1);
                return BoxedUnit.UNIT;
            });
        }
        catch (Exception e) {
            this.dataIterator().closeIfNeeded();
            throw e;
        }
        this.processedCount_$eq(this.processedCount() + 1);
        if (this.processedCount() % 100000 == 0) {
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("partition.index = {} processedCount = {}", new Object[]{BoxesRunTime.boxToInteger((int)this.partition.index()), BoxesRunTime.boxToInteger((int)this.processedCount())});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return row;
    }

    private final /* synthetic */ void liftedTree1$1(GreenplumQualifiedName.Table internalTable$1, GreenplumQualifiedName.TempTable externalTable$1, GpfdistService service$1, String txId$1) {
        try {
            Jdbc$.MODULE$.copyTableToExternal(this.conn(), internalTable$1, externalTable$1, this.wherePredicate(), this.allColumns);
            this.conn().commit();
        }
        catch (PSQLException p) {
            BoxedUnit boxedUnit;
            if (this.logger().underlying().isErrorEnabled()) {
                this.logger().underlying().error("There was a PSQL exception: {}", new Object[]{p.getMessage()});
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            Try<InputStream> try_ = service$1.getReceivedDataFor(txId$1);
            if (try_ instanceof Failure) {
                BoxedUnit boxedUnit2;
                Failure failure = (Failure)try_;
                Throwable error = failure.exception();
                if (this.logger().underlying().isErrorEnabled()) {
                    this.logger().underlying().error("There was an failure in the HTTP handler: {}", new Object[]{error.getMessage()});
                    boxedUnit2 = BoxedUnit.UNIT;
                } else {
                    boxedUnit2 = BoxedUnit.UNIT;
                }
                ErrorHandling$.MODULE$.appendCauseToErrorChain(p, error);
            } else if (try_ instanceof Success) {
            } else {
                throw new MatchError(try_);
            }
            throw p;
        }
    }

    private final /* synthetic */ void liftedTree2$1() {
        block3: {
            try {
                this.conn().rollback();
            }
            catch (Throwable e) {
                BoxedUnit boxedUnit;
                if (this.logger().underlying().isWarnEnabled()) {
                    this.logger().underlying().warn("Ignoring failure to rollback transaction: {}", new Object[]{e.getMessage()});
                    boxedUnit = BoxedUnit.UNIT;
                    break block3;
                }
                boxedUnit = BoxedUnit.UNIT;
            }
        }
    }

    private final /* synthetic */ void liftedTree3$1() {
        block3: {
            try {
                this.conn().close();
            }
            catch (Throwable e) {
                BoxedUnit boxedUnit;
                if (this.logger().underlying().isWarnEnabled()) {
                    this.logger().underlying().warn("Ignoring failure to close transaction: {}", new Object[]{e.getMessage()});
                    boxedUnit = BoxedUnit.UNIT;
                    break block3;
                }
                boxedUnit = BoxedUnit.UNIT;
            }
        }
    }

    public static final /* synthetic */ void $anonfun$next$2(GreenplumRowIterator $this, SpecificInternalRow row$1, Tuple2 x0$1) {
        Tuple2 tuple2 = x0$1;
        if (tuple2 != null) {
            String value = (String)tuple2._1();
            int index = tuple2._2$mcI$sp();
            if (value != null) {
                if (index < $this.converters().length) {
                    BoxedUnit cfr_ignored_0 = (BoxedUnit)$this.converters()[index].apply((Object)value, (Object)row$1);
                    return;
                }
                return;
            }
            if (!$this.schema.fields()[index].nullable()) {
                String columnName = $this.schema.fields()[index].name();
                String tableName = $this.greenplumOptions.dbTable();
                throw new RuntimeException(new java.lang.StringBuilder(49).append("The ").append(columnName).append(" column in ").append(tableName).append(" table should not have null value.").toString());
            }
            return;
        }
        throw new MatchError((Object)tuple2);
    }

    public GreenplumRowIterator(String applicationId, GreenplumPartition partition, StructType schema, GreenplumOptions greenplumOptions, Seq<String> allColumns, Filter[] filters, ConnectorUtils connectorUtils) {
        DataIterator dataIterator;
        block21: {
            String string;
            this.partition = partition;
            this.schema = schema;
            this.greenplumOptions = greenplumOptions;
            this.allColumns = allColumns;
            GenTraversableOnce.$init$((GenTraversableOnce)this);
            TraversableOnce.$init$((TraversableOnce)this);
            Iterator.$init$((Iterator)this);
            LazyLogging.$init$(this);
            this.conn = ConnectionManager$.MODULE$.getConnection(greenplumOptions, false);
            this.filterWhereClause = GreenplumRowIterator$.MODULE$.filterWherePredicate(filters);
            if (new StringOps(Predef$.MODULE$.augmentString(this.filterWhereClause())).nonEmpty()) {
                BoxedUnit boxedUnit;
                if (this.logger().underlying().isDebugEnabled()) {
                    this.logger().underlying().debug("Including '{}' in WHERE clause", new Object[]{this.filterWhereClause()});
                    boxedUnit = BoxedUnit.UNIT;
                } else {
                    boxedUnit = BoxedUnit.UNIT;
                }
                string = new java.lang.StringBuilder(9).append("(").append(this.filterWhereClause()).append(") AND (").append(partition.whereClause()).append(")").toString();
            } else {
                string = partition.whereClause();
            }
            this.wherePredicate = string;
            try {
                BoxedUnit boxedUnit;
                long threadId = Thread.currentThread().getId();
                String executorId = SparkEnv$.MODULE$.get().executorId();
                String externalTableName = GreenplumTableManager$.MODULE$.generateExternalTableName(applicationId, greenplumOptions.dbTable(), executorId, threadId, allColumns);
                GreenplumQualifiedName.Table internalTable = GreenplumQualifiedName$.MODULE$.forTable(greenplumOptions.dbSchema(), greenplumOptions.dbTable());
                GreenplumQualifiedName.TempTable externalTable = GreenplumQualifiedName$.MODULE$.forTempTable(externalTableName);
                GpfdistService service = GpfdistServiceManager$.MODULE$.getService(greenplumOptions.connectorOptions());
                if (!Jdbc$.MODULE$.externalTableExists(this.conn(), externalTable)) {
                    BoxedUnit boxedUnit2;
                    GpfdistLocation location = connectorUtils.getLocation(greenplumOptions.connectorOptions(), service.getPort());
                    String pathPrefix = connectorUtils.getLocationPathPrefix(applicationId, executorId);
                    GpfdistLocation locationWithPath = location.withPath(pathPrefix);
                    if (this.logger().underlying().isDebugEnabled()) {
                        this.logger().underlying().debug("Temporary external table {} not found, creating table with location='{}' and columns=[{}]", externalTable, location.getUrl(), allColumns.mkString(","));
                        boxedUnit2 = BoxedUnit.UNIT;
                    } else {
                        boxedUnit2 = BoxedUnit.UNIT;
                    }
                    String distributionPolicy = greenplumOptions.connectorOptions().matchDistributionPolicy() ? Jdbc$.MODULE$.determineDistributionPolicy(this.conn(), internalTable) : "DISTRIBUTED RANDOMLY";
                    Jdbc$.MODULE$.createGpfdistWritableExternalTable(this.conn(), internalTable, externalTable, locationWithPath, allColumns, distributionPolicy);
                }
                String txId = Jdbc$.MODULE$.getDistributedTransactionId(this.conn());
                if (this.logger().underlying().isDebugEnabled()) {
                    this.logger().underlying().debug("Distributed transaction id is {} for RDD partition with WHERE clause '{}''", new String[]{txId, partition.whereClause()});
                    boxedUnit = BoxedUnit.UNIT;
                } else {
                    boxedUnit = BoxedUnit.UNIT;
                }
                this.liftedTree1$1(internalTable, externalTable, service, txId);
                Try<InputStream> try_ = service.getReceivedDataFor(txId);
                if (try_ instanceof Success) {
                    BoxedUnit boxedUnit3;
                    Success success = (Success)try_;
                    InputStream data = (InputStream)success.value();
                    if (this.logger().underlying().isDebugEnabled()) {
                        this.logger().underlying().debug("Reading table data for table {} for transaction id {}", new Object[]{internalTable, txId});
                        boxedUnit3 = BoxedUnit.UNIT;
                    } else {
                        boxedUnit3 = BoxedUnit.UNIT;
                    }
                    dataIterator = new DataIterator(data);
                    break block21;
                }
                if (try_ instanceof Failure) {
                    BoxedUnit boxedUnit4;
                    BoxedUnit boxedUnit5;
                    Failure failure = (Failure)try_;
                    Throwable error = failure.exception();
                    if (this.logger().underlying().isErrorEnabled()) {
                        this.logger().underlying().error("Table copy succeeded but there was an error in the HTTP handler; this should not happen");
                        boxedUnit5 = BoxedUnit.UNIT;
                    } else {
                        boxedUnit5 = BoxedUnit.UNIT;
                    }
                    if (this.logger().underlying().isErrorEnabled()) {
                        this.logger().underlying().error(error.getMessage());
                        boxedUnit4 = BoxedUnit.UNIT;
                    } else {
                        boxedUnit4 = BoxedUnit.UNIT;
                    }
                    throw error;
                }
                throw new MatchError(try_);
            }
            finally {
                if (!this.conn().isClosed()) {
                    this.liftedTree2$1();
                    this.liftedTree3$1();
                }
            }
        }
        this.dataIterator = dataIterator;
        this.processedCount = 0;
        this.converters = DataTypeConverterFactory$.MODULE$.create(schema);
    }
}

