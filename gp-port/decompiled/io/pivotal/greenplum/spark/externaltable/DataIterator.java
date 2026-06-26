/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.reflect.ScalaSignature
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import io.pivotal.greenplum.spark.GreenplumCSVFormat$;
import io.pivotal.greenplum.spark.externaltable.NextIterator;
import java.io.InputStream;
import scala.reflect.ScalaSignature;
import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.ResultIterator;
import shadeio.univocity.parsers.csv.CsvParser;

@ScalaSignature(bytes="\u0006\u0001-4Q!\u0003\u0006\u0001\u0015QA\u0001\"\u000e\u0001\u0003\u0006\u0004%\tA\u000e\u0005\t}\u0001\u0011\t\u0011)A\u0005o!)q\b\u0001C\u0001\u0001\"A1\t\u0001EC\u0002\u0013\u0005A\tC\u0003Y\u0001\u0011E\u0013\fC\u0003[\u0001\u0011\u0005\u0013\fC\u0003\\\u0001\u0011\u0005C\fC\u0003a\u0001\u0011E\u0013M\u0001\u0007ECR\f\u0017\n^3sCR|'O\u0003\u0002\f\u0019\u0005iQ\r\u001f;fe:\fG\u000e^1cY\u0016T!!\u0004\b\u0002\u000bM\u0004\u0018M]6\u000b\u0005=\u0001\u0012!C4sK\u0016t\u0007\u000f\\;n\u0015\t\t\"#A\u0004qSZ|G/\u00197\u000b\u0003M\t!![8\u0014\u0007\u0001)2\u0006E\u0002\u0017/ei\u0011AC\u0005\u00031)\u0011ABT3yi&#XM]1u_J\u00042AG\u000f \u001b\u0005Y\"\"\u0001\u000f\u0002\u000bM\u001c\u0017\r\\1\n\u0005yY\"!B!se\u0006L\bC\u0001\u0011)\u001d\t\tc\u0005\u0005\u0002#75\t1E\u0003\u0002%K\u00051AH]8piz\u001a\u0001!\u0003\u0002(7\u00051\u0001K]3eK\u001aL!!\u000b\u0016\u0003\rM#(/\u001b8h\u0015\t93\u0004\u0005\u0002-g5\tQF\u0003\u0002/_\u0005a1oY1mC2|wmZ5oO*\u0011\u0001'M\u0001\tif\u0004Xm]1gK*\t!'A\u0002d_6L!\u0001N\u0017\u0003\u00171\u000b'0\u001f'pO\u001eLgnZ\u0001\fS:\u0004X\u000f^*ue\u0016\fW.F\u00018!\tAD(D\u0001:\u0015\t\u0019\"HC\u0001<\u0003\u0011Q\u0017M^1\n\u0005uJ$aC%oaV$8\u000b\u001e:fC6\fA\"\u001b8qkR\u001cFO]3b[\u0002\na\u0001P5oSRtDCA!C!\t1\u0002\u0001C\u00036\u0007\u0001\u0007q'\u0001\bqCJ\u001cXM]%uKJ\fGo\u001c:\u0016\u0003\u0015\u0003BAR'P+6\tqI\u0003\u0002IQ\u000611m\\7n_:T!A\u00136\u0002\u000fA\f'o]3sg*\u0011A*M\u0001\nk:Lgo\\2jifL!AT$\u0003\u001dI+7/\u001e7u\u0013R,'/\u0019;peB\u0019!$\b)\u0011\u0005E#V\"\u0001*\u000b\u0005MS\u0014\u0001\u00027b]\u001eL!!\u000b*\u0011\u0005\u00193\u0016BA,H\u00059\u0001\u0016M]:j]\u001e\u001cuN\u001c;fqR\fqaZ3u\u001d\u0016DH\u000fF\u0001\u001a\u0003\u0011qW\r\u001f;\u0002\u000f!\f7OT3yiV\tQ\f\u0005\u0002\u001b=&\u0011ql\u0007\u0002\b\u0005>|G.Z1o\u0003\u0015\u0019Gn\\:f)\u0005\u0011\u0007C\u0001\u000ed\u0013\t!7D\u0001\u0003V]&$\u0018aB:iC\u0012,\u0017n\u001c\u0006\u0002K*\u0011AJ\u001a\u0006\u0003\u0015\u001eT\u0011!\u001a\u0006\u0003\u0019&\u0004")
public class DataIterator
extends NextIterator<String[]>
implements LazyLogging {
    private ResultIterator<String[], ParsingContext> parserIterator;
    private final InputStream inputStream;
    private transient Logger logger;
    private volatile boolean bitmap$0;
    private volatile transient boolean bitmap$trans$0;

    private Logger logger$lzycompute() {
        DataIterator dataIterator = this;
        synchronized (dataIterator) {
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

    public InputStream inputStream() {
        return this.inputStream;
    }

    private ResultIterator<String[], ParsingContext> parserIterator$lzycompute() {
        DataIterator dataIterator = this;
        synchronized (dataIterator) {
            if (!this.bitmap$0) {
                this.parserIterator = new CsvParser(GreenplumCSVFormat$.MODULE$.DEFAULT()).iterate(this.inputStream(), GreenplumCSVFormat$.MODULE$.DEFAULT_ENCODING()).iterator();
                this.bitmap$0 = true;
            }
        }
        return this.parserIterator;
    }

    public ResultIterator<String[], ParsingContext> parserIterator() {
        if (!this.bitmap$0) {
            return this.parserIterator$lzycompute();
        }
        return this.parserIterator;
    }

    @Override
    public String[] getNext() {
        return this.next();
    }

    @Override
    public String[] next() {
        if (this.parserIterator().hasNext()) {
            return (String[])this.parserIterator().next();
        }
        this.finished_$eq(true);
        return null;
    }

    @Override
    public boolean hasNext() {
        return this.parserIterator().hasNext();
    }

    @Override
    public void close() {
        this.inputStream().close();
    }

    public DataIterator(InputStream inputStream) {
        this.inputStream = inputStream;
        LazyLogging.$init$(this);
    }
}

