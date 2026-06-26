/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function0
 *  scala.Function1
 *  scala.Function2
 *  scala.Option
 *  scala.PartialFunction
 *  scala.Predef$$less$colon$less
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
 *  scala.collection.immutable.Vector
 *  scala.collection.mutable.Buffer
 *  scala.collection.mutable.StringBuilder
 *  scala.math.Numeric
 *  scala.math.Ordering
 *  scala.reflect.ClassTag
 *  scala.reflect.ScalaSignature
 *  scala.runtime.Nothing$
 */
package io.pivotal.greenplum.spark.externaltable;

import java.util.NoSuchElementException;
import scala.Function0;
import scala.Function1;
import scala.Function2;
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
import scala.collection.immutable.Vector;
import scala.collection.mutable.Buffer;
import scala.collection.mutable.StringBuilder;
import scala.math.Numeric;
import scala.math.Ordering;
import scala.reflect.ClassTag;
import scala.reflect.ScalaSignature;
import scala.runtime.Nothing$;

@ScalaSignature(bytes="\u0006\u0001\u00194aa\u0005\u000b\u0002\u0002Yq\u0002\"\u0002 \u0001\t\u0003y\u0004b\u0002\"\u0001\u0001\u0004%Ia\u0011\u0005\b\u000f\u0002\u0001\r\u0011\"\u0003I\u0011\u0019q\u0005\u0001)Q\u0005\t\"Iq\n\u0001a\u0001\u0002\u0004%I\u0001\u0015\u0005\n#\u0002\u0001\r\u00111A\u0005\nIC\u0011\u0002\u0016\u0001A\u0002\u0003\u0005\u000b\u0015B\u001a\t\u000fU\u0003\u0001\u0019!C\u0005\u0007\"9a\u000b\u0001a\u0001\n\u00139\u0006BB-\u0001A\u0003&A\tC\u0004[\u0001\u0001\u0007I\u0011C\"\t\u000fm\u0003\u0001\u0019!C\t9\"1a\f\u0001Q!\n\u0011CQa\u0018\u0001\u0007\u0012\u0001DQ!\u0019\u0001\u0007\u0012\tDQa\u0019\u0001\u0005\u0002\tDQ\u0001\u001a\u0001\u0005B\rCQ!\u001a\u0001\u0005B\u0001\u0014ABT3yi&#XM]1u_JT!!\u0006\f\u0002\u001b\u0015DH/\u001a:oC2$\u0018M\u00197f\u0015\t9\u0002$A\u0003ta\u0006\u00148N\u0003\u0002\u001a5\u0005IqM]3f]BdW/\u001c\u0006\u00037q\tq\u0001]5w_R\fGNC\u0001\u001e\u0003\tIw.\u0006\u0002 kM\u0019\u0001\u0001\t\u0014\u0011\u0005\u0005\"S\"\u0001\u0012\u000b\u0003\r\nQa]2bY\u0006L!!\n\u0012\u0003\r\u0005s\u0017PU3g!\r9\u0003g\r\b\u0003Q9r!!K\u0017\u000e\u0003)R!a\u000b\u0017\u0002\rq\u0012xn\u001c;?\u0007\u0001I\u0011aI\u0005\u0003_\t\nq\u0001]1dW\u0006<W-\u0003\u00022e\tA\u0011\n^3sCR|'O\u0003\u00020EA\u0011A'\u000e\u0007\u0001\t\u00151\u0004A1\u00018\u0005\u0005)\u0016C\u0001\u001d<!\t\t\u0013(\u0003\u0002;E\t9aj\u001c;iS:<\u0007CA\u0011=\u0013\ti$EA\u0002B]f\fa\u0001P5oSRtD#\u0001!\u0011\u0007\u0005\u00031'D\u0001\u0015\u0003\u001d9w\u000e\u001e(fqR,\u0012\u0001\u0012\t\u0003C\u0015K!A\u0012\u0012\u0003\u000f\t{w\u000e\\3b]\u0006Yqm\u001c;OKb$x\fJ3r)\tIE\n\u0005\u0002\"\u0015&\u00111J\t\u0002\u0005+:LG\u000fC\u0004N\u0007\u0005\u0005\t\u0019\u0001#\u0002\u0007a$\u0013'\u0001\u0005h_RtU\r\u001f;!\u0003%qW\r\u001f;WC2,X-F\u00014\u00035qW\r\u001f;WC2,Xm\u0018\u0013fcR\u0011\u0011j\u0015\u0005\b\u001b\u001a\t\t\u00111\u00014\u0003)qW\r\u001f;WC2,X\rI\u0001\u0007G2|7/\u001a3\u0002\u0015\rdwn]3e?\u0012*\u0017\u000f\u0006\u0002J1\"9Q*CA\u0001\u0002\u0004!\u0015aB2m_N,G\rI\u0001\tM&t\u0017n\u001d5fI\u0006aa-\u001b8jg\",Gm\u0018\u0013fcR\u0011\u0011*\u0018\u0005\b\u001b2\t\t\u00111\u0001E\u0003%1\u0017N\\5tQ\u0016$\u0007%A\u0004hKRtU\r\u001f;\u0015\u0003M\nQa\u00197pg\u0016$\u0012!S\u0001\u000eG2|7/Z%g\u001d\u0016,G-\u001a3\u0002\u000f!\f7OT3yi\u0006!a.\u001a=u\u0001")
public abstract class NextIterator<U>
implements Iterator<U> {
    private boolean gotNext;
    private U nextValue;
    private boolean closed;
    private boolean finished;

    public Iterator<U> seq() {
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

    public Iterator<U> take(int n) {
        return Iterator.take$((Iterator)this, (int)n);
    }

    public Iterator<U> drop(int n) {
        return Iterator.drop$((Iterator)this, (int)n);
    }

    public Iterator<U> slice(int from, int until) {
        return Iterator.slice$((Iterator)this, (int)from, (int)until);
    }

    public Iterator<U> sliceIterator(int from, int until) {
        return Iterator.sliceIterator$((Iterator)this, (int)from, (int)until);
    }

    public <B> Iterator<B> map(Function1<U, B> f) {
        return Iterator.map$((Iterator)this, f);
    }

    public <B> Iterator<B> $plus$plus(Function0<GenTraversableOnce<B>> that) {
        return Iterator.$plus$plus$((Iterator)this, that);
    }

    public <B> Iterator<B> flatMap(Function1<U, GenTraversableOnce<B>> f) {
        return Iterator.flatMap$((Iterator)this, f);
    }

    public Iterator<U> filter(Function1<U, Object> p) {
        return Iterator.filter$((Iterator)this, p);
    }

    public <B> boolean corresponds(GenTraversableOnce<B> that, Function2<U, B, Object> p) {
        return Iterator.corresponds$((Iterator)this, that, p);
    }

    public Iterator<U> withFilter(Function1<U, Object> p) {
        return Iterator.withFilter$((Iterator)this, p);
    }

    public Iterator<U> filterNot(Function1<U, Object> p) {
        return Iterator.filterNot$((Iterator)this, p);
    }

    public <B> Iterator<B> collect(PartialFunction<U, B> pf) {
        return Iterator.collect$((Iterator)this, pf);
    }

    public <B> Iterator<B> scanLeft(B z, Function2<B, U, B> op) {
        return Iterator.scanLeft$((Iterator)this, z, op);
    }

    public <B> Iterator<B> scanRight(B z, Function2<U, B, B> op) {
        return Iterator.scanRight$((Iterator)this, z, op);
    }

    public Iterator<U> takeWhile(Function1<U, Object> p) {
        return Iterator.takeWhile$((Iterator)this, p);
    }

    public Tuple2<Iterator<U>, Iterator<U>> partition(Function1<U, Object> p) {
        return Iterator.partition$((Iterator)this, p);
    }

    public Tuple2<Iterator<U>, Iterator<U>> span(Function1<U, Object> p) {
        return Iterator.span$((Iterator)this, p);
    }

    public Iterator<U> dropWhile(Function1<U, Object> p) {
        return Iterator.dropWhile$((Iterator)this, p);
    }

    public <B> Iterator<Tuple2<U, B>> zip(Iterator<B> that) {
        return Iterator.zip$((Iterator)this, that);
    }

    public <A1> Iterator<A1> padTo(int len, A1 elem) {
        return Iterator.padTo$((Iterator)this, (int)len, elem);
    }

    public Iterator<Tuple2<U, Object>> zipWithIndex() {
        return Iterator.zipWithIndex$((Iterator)this);
    }

    public <B, A1, B1> Iterator<Tuple2<A1, B1>> zipAll(Iterator<B> that, A1 thisElem, B1 thatElem) {
        return Iterator.zipAll$((Iterator)this, that, thisElem, thatElem);
    }

    public <U> void foreach(Function1<U, U> f) {
        Iterator.foreach$((Iterator)this, f);
    }

    public boolean forall(Function1<U, Object> p) {
        return Iterator.forall$((Iterator)this, p);
    }

    public boolean exists(Function1<U, Object> p) {
        return Iterator.exists$((Iterator)this, p);
    }

    public boolean contains(Object elem) {
        return Iterator.contains$((Iterator)this, (Object)elem);
    }

    public Option<U> find(Function1<U, Object> p) {
        return Iterator.find$((Iterator)this, p);
    }

    public int indexWhere(Function1<U, Object> p) {
        return Iterator.indexWhere$((Iterator)this, p);
    }

    public int indexWhere(Function1<U, Object> p, int from) {
        return Iterator.indexWhere$((Iterator)this, p, (int)from);
    }

    public <B> int indexOf(B elem) {
        return Iterator.indexOf$((Iterator)this, elem);
    }

    public <B> int indexOf(B elem, int from) {
        return Iterator.indexOf$((Iterator)this, elem, (int)from);
    }

    public BufferedIterator<U> buffered() {
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

    public Tuple2<Iterator<U>, Iterator<U>> duplicate() {
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

    public Traversable<U> toTraversable() {
        return Iterator.toTraversable$((Iterator)this);
    }

    public Iterator<U> toIterator() {
        return Iterator.toIterator$((Iterator)this);
    }

    public Stream<U> toStream() {
        return Iterator.toStream$((Iterator)this);
    }

    public String toString() {
        return Iterator.toString$((Iterator)this);
    }

    public List<U> reversed() {
        return TraversableOnce.reversed$((TraversableOnce)this);
    }

    public int size() {
        return TraversableOnce.size$((TraversableOnce)this);
    }

    public boolean nonEmpty() {
        return TraversableOnce.nonEmpty$((TraversableOnce)this);
    }

    public int count(Function1<U, Object> p) {
        return TraversableOnce.count$((TraversableOnce)this, p);
    }

    public <B> Option<B> collectFirst(PartialFunction<U, B> pf) {
        return TraversableOnce.collectFirst$((TraversableOnce)this, pf);
    }

    public <B> B $div$colon(B z, Function2<B, U, B> op) {
        return (B)TraversableOnce.$div$colon$((TraversableOnce)this, z, op);
    }

    public <B> B $colon$bslash(B z, Function2<U, B, B> op) {
        return (B)TraversableOnce.$colon$bslash$((TraversableOnce)this, z, op);
    }

    public <B> B foldLeft(B z, Function2<B, U, B> op) {
        return (B)TraversableOnce.foldLeft$((TraversableOnce)this, z, op);
    }

    public <B> B foldRight(B z, Function2<U, B, B> op) {
        return (B)TraversableOnce.foldRight$((TraversableOnce)this, z, op);
    }

    public <B> B reduceLeft(Function2<B, U, B> op) {
        return (B)TraversableOnce.reduceLeft$((TraversableOnce)this, op);
    }

    public <B> B reduceRight(Function2<U, B, B> op) {
        return (B)TraversableOnce.reduceRight$((TraversableOnce)this, op);
    }

    public <B> Option<B> reduceLeftOption(Function2<B, U, B> op) {
        return TraversableOnce.reduceLeftOption$((TraversableOnce)this, op);
    }

    public <B> Option<B> reduceRightOption(Function2<U, B, B> op) {
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

    public <B> B aggregate(Function0<B> z, Function2<B, U, B> seqop, Function2<B, B, B> combop) {
        return (B)TraversableOnce.aggregate$((TraversableOnce)this, z, seqop, combop);
    }

    public <B> B sum(Numeric<B> num) {
        return (B)TraversableOnce.sum$((TraversableOnce)this, num);
    }

    public <B> B product(Numeric<B> num) {
        return (B)TraversableOnce.product$((TraversableOnce)this, num);
    }

    public <B> U min(Ordering<B> cmp) {
        return (U)TraversableOnce.min$((TraversableOnce)this, cmp);
    }

    public <B> U max(Ordering<B> cmp) {
        return (U)TraversableOnce.max$((TraversableOnce)this, cmp);
    }

    public <B> U maxBy(Function1<U, B> f, Ordering<B> cmp) {
        return (U)TraversableOnce.maxBy$((TraversableOnce)this, f, cmp);
    }

    public <B> U minBy(Function1<U, B> f, Ordering<B> cmp) {
        return (U)TraversableOnce.minBy$((TraversableOnce)this, f, cmp);
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

    public List<U> toList() {
        return TraversableOnce.toList$((TraversableOnce)this);
    }

    public Iterable<U> toIterable() {
        return TraversableOnce.toIterable$((TraversableOnce)this);
    }

    public Seq<U> toSeq() {
        return TraversableOnce.toSeq$((TraversableOnce)this);
    }

    public IndexedSeq<U> toIndexedSeq() {
        return TraversableOnce.toIndexedSeq$((TraversableOnce)this);
    }

    public <B> Buffer<B> toBuffer() {
        return TraversableOnce.toBuffer$((TraversableOnce)this);
    }

    public <B> Set<B> toSet() {
        return TraversableOnce.toSet$((TraversableOnce)this);
    }

    public Vector<U> toVector() {
        return TraversableOnce.toVector$((TraversableOnce)this);
    }

    public <Col> Col to(CanBuildFrom<Nothing$, U, Col> cbf) {
        return (Col)TraversableOnce.to$((TraversableOnce)this, cbf);
    }

    public <T, U> Map<T, U> toMap(Predef$.less.colon.less<U, Tuple2<T, U>> ev) {
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

    private boolean gotNext() {
        return this.gotNext;
    }

    private void gotNext_$eq(boolean x$1) {
        this.gotNext = x$1;
    }

    private U nextValue() {
        return this.nextValue;
    }

    private void nextValue_$eq(U x$1) {
        this.nextValue = x$1;
    }

    private boolean closed() {
        return this.closed;
    }

    private void closed_$eq(boolean x$1) {
        this.closed = x$1;
    }

    public boolean finished() {
        return this.finished;
    }

    public void finished_$eq(boolean x$1) {
        this.finished = x$1;
    }

    public abstract U getNext();

    public abstract void close();

    public void closeIfNeeded() {
        if (!this.closed()) {
            this.closed_$eq(true);
            this.close();
            return;
        }
    }

    public boolean hasNext() {
        if (!this.finished() && !this.gotNext()) {
            this.nextValue_$eq(this.getNext());
            if (this.finished()) {
                this.closeIfNeeded();
            }
            this.gotNext_$eq(true);
        }
        return !this.finished();
    }

    public U next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("End of stream");
        }
        this.gotNext_$eq(false);
        return this.nextValue();
    }

    public NextIterator() {
        GenTraversableOnce.$init$((GenTraversableOnce)this);
        TraversableOnce.$init$((TraversableOnce)this);
        Iterator.$init$((Iterator)this);
        this.gotNext = false;
        this.closed = false;
        this.finished = false;
    }
}

