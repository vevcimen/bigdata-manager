/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.collection.Seq
 *  scala.reflect.ScalaSignature
 *  scala.reflect.api.Exprs$Expr
 *  scala.reflect.api.Trees$TreeApi
 *  scala.reflect.macros.blackbox.Context
 */
package com.typesafe.scalalogging;

import com.typesafe.scalalogging.LoggerTakingImplicitMacro$;
import org.slf4j.Marker;
import scala.collection.Seq;
import scala.reflect.ScalaSignature;
import scala.reflect.api.Exprs;
import scala.reflect.api.Trees;
import scala.reflect.macros.blackbox.Context;

@ScalaSignature(bytes="\u0006\u0001\u00155t!B\u0001\u0003\u0011\u0013I\u0011!\u0007'pO\u001e,'\u000fV1lS:<\u0017*\u001c9mS\u000eLG/T1de>T!a\u0001\u0003\u0002\u0019M\u001c\u0017\r\\1m_\u001e<\u0017N\\4\u000b\u0005\u00151\u0011\u0001\u0003;za\u0016\u001c\u0018MZ3\u000b\u0003\u001d\t1aY8n\u0007\u0001\u0001\"AC\u0006\u000e\u0003\t1Q\u0001\u0004\u0002\t\n5\u0011\u0011\u0004T8hO\u0016\u0014H+Y6j]\u001eLU\u000e\u001d7jG&$X*Y2s_N\u00111B\u0004\t\u0003\u001fIi\u0011\u0001\u0005\u0006\u0002#\u0005)1oY1mC&\u00111\u0003\u0005\u0002\u0007\u0003:L(+\u001a4\t\u000bUYA\u0011\u0001\f\u0002\rqJg.\u001b;?)\u0005IQ\u0001\u0002\r\f\u0001e\u0011Q\u0002T8hO\u0016\u00148i\u001c8uKb$XC\u0001\u000e/%\tYRD\u0002\u0003\u001d\u0017\u0001Q\"\u0001\u0004\u001fsK\u001aLg.Z7f]Rt\u0004C\u0001\u0010&\u001b\u0005y\"B\u0001\u0011\"\u0003!\u0011G.Y2lE>D(B\u0001\u0012$\u0003\u0019i\u0017m\u0019:pg*\u0011A\u0005E\u0001\be\u00164G.Z2u\u0013\t1sDA\u0004D_:$X\r\u001f;\u0006\t!Z\u0002%\u000b\u0002\u000b!J,g-\u001b=UsB,\u0007c\u0001\u0006+Y%\u00111F\u0001\u0002\u0015\u0019><w-\u001a:UC.LgnZ%na2L7-\u001b;\u0011\u00055rC\u0002\u0001\u0003\u0006_]\u0011\r\u0001\r\u0002\u0002\u0003F\u0011\u0011\u0007\u000e\t\u0003\u001fIJ!a\r\t\u0003\u000f9{G\u000f[5oOB\u0011q\"N\u0005\u0003mA\u00111!\u00118z\u0011\u0015A4\u0002\"\u0001:\u00031)'O]8s\u001b\u0016\u001c8/Y4f+\tQT\t\u0006\u0002<\u0001R\u0011A(\u0016\u000b\u0003{9\u0003\"A\u0010%\u000f\u0005}2eBA\u0017A\u0011\u0015\tu\u00071\u0001C\u0003\u0005\u0019\u0007cA\"\u0018\t6\t1\u0002\u0005\u0002.\u000b\u0012)qf\u000eb\u0001a%\u0011q)J\u0001\tk:Lg/\u001a:tK&\u0011\u0011J\u0013\u0002\u0005)J,W-\u0003\u0002L\u0019\n)AK]3fg*\u0011QjI\u0001\u0004CBL\u0007\"B(8\u0001\u0004\u0001\u0016!A1\u0011\u0007}\nF)\u0003\u0002S'\n!Q\t\u001f9s\u0013\t!\u0016EA\u0004BY&\f7/Z:\t\u000bY;\u0004\u0019A,\u0002\u000f5,7o]1hKB\u0019q(\u0015-\u0011\u0005e\u0003gB\u0001._!\tY\u0006#D\u0001]\u0015\ti\u0006\"\u0001\u0004=e>|GOP\u0005\u0003?B\ta\u0001\u0015:fI\u00164\u0017BA1c\u0005\u0019\u0019FO]5oO*\u0011q\f\u0005\u0005\u0006I.!\t!Z\u0001\u0012KJ\u0014xN]'fgN\fw-Z\"bkN,WC\u00014p)\t9G\u000eF\u0002ieR$\"!\u001b9\u0011\u0005)DeBA6G\u001d\tiC\u000eC\u0003BG\u0002\u0007Q\u000eE\u0002D/9\u0004\"!L8\u0005\u000b=\u001a'\u0019\u0001\u0019\t\u000b=\u001b\u0007\u0019A9\u0011\u0007-\ff\u000eC\u0003WG\u0002\u00071\u000fE\u0002l#bCQ!^2A\u0002Y\fQaY1vg\u0016\u00042a[)x!\tAXP\u0004\u0002zw:\u00111L_\u0005\u0002#%\u0011A\u0010E\u0001\ba\u0006\u001c7.Y4f\u0013\tqxPA\u0005UQJ|w/\u00192mK*\u0011A\u0010\u0005\u0005\b\u0003\u0007YA\u0011AA\u0003\u0003A)'O]8s\u001b\u0016\u001c8/Y4f\u0003J<7/\u0006\u0003\u0002\b\u0005eA\u0003BA\u0005\u0003'!b!a\u0003\u0002 \u0005\rB\u0003BA\u0007\u00037\u00012!a\u0004I\u001d\r\t\tB\u0012\b\u0004[\u0005M\u0001bB!\u0002\u0002\u0001\u0007\u0011Q\u0003\t\u0005\u0007^\t9\u0002E\u0002.\u00033!aaLA\u0001\u0005\u0004\u0001\u0004bB(\u0002\u0002\u0001\u0007\u0011Q\u0004\t\u0006\u0003#\t\u0016q\u0003\u0005\b-\u0006\u0005\u0001\u0019AA\u0011!\u0011\t\t\"\u0015-\t\u0011\u0005\u0015\u0012\u0011\u0001a\u0001\u0003O\tA!\u0019:hgB)q\"!\u000b\u0002.%\u0019\u00111\u0006\t\u0003\u0015q\u0012X\r]3bi\u0016$g\b\u0005\u0003\u0002\u0012E#\u0004bBA\u0019\u0017\u0011\u0005\u00111G\u0001\u0013KJ\u0014xN]'fgN\fw-Z'be.,'/\u0006\u0003\u00026\u0005\u001dC\u0003BA\u001c\u0003\u0003\"b!!\u000f\u0002N\u0005\rD\u0003BA\u001e\u0003\u0013\u00022!!\u0010I\u001d\r\tyD\u0012\b\u0004[\u0005\u0005\u0003bB!\u00020\u0001\u0007\u00111\t\t\u0005\u0007^\t)\u0005E\u0002.\u0003\u000f\"aaLA\u0018\u0005\u0004\u0001\u0004bB(\u00020\u0001\u0007\u00111\n\t\u0006\u0003\u007f\t\u0016Q\t\u0005\t\u0003\u001f\ny\u00031\u0001\u0002R\u00051Q.\u0019:lKJ\u0004R!a\u0010R\u0003'\u0002B!!\u0016\u0002`5\u0011\u0011q\u000b\u0006\u0005\u00033\nY&A\u0003tY\u001a$$N\u0003\u0002\u0002^\u0005\u0019qN]4\n\t\u0005\u0005\u0014q\u000b\u0002\u0007\u001b\u0006\u00148.\u001a:\t\u000fY\u000by\u00031\u0001\u0002fA!\u0011qH)Y\u0011\u001d\tIg\u0003C\u0001\u0003W\nq#\u001a:s_JlUm]:bO\u0016\u001c\u0015-^:f\u001b\u0006\u00148.\u001a:\u0016\t\u00055\u0014q\u0010\u000b\u0005\u0003_\nI\b\u0006\u0005\u0002r\u0005\u0015\u0015\u0011RAG)\u0011\t\u0019(!!\u0011\u0007\u0005U\u0004JD\u0002\u0002x\u0019s1!LA=\u0011\u001d\t\u0015q\ra\u0001\u0003w\u0002BaQ\f\u0002~A\u0019Q&a \u0005\r=\n9G1\u00011\u0011\u001dy\u0015q\ra\u0001\u0003\u0007\u0003R!a\u001eR\u0003{B\u0001\"a\u0014\u0002h\u0001\u0007\u0011q\u0011\t\u0006\u0003o\n\u00161\u000b\u0005\b-\u0006\u001d\u0004\u0019AAF!\u0011\t9(\u0015-\t\u000fU\f9\u00071\u0001\u0002\u0010B!\u0011qO)x\u0011\u001d\t\u0019j\u0003C\u0001\u0003+\u000ba#\u001a:s_JlUm]:bO\u0016\f%oZ:NCJ\\WM]\u000b\u0005\u0003/\u000bI\u000b\u0006\u0003\u0002\u001a\u0006\rF\u0003CAN\u0003_\u000b\u0019,a.\u0015\t\u0005u\u00151\u0016\t\u0004\u0003?CebAAQ\r:\u0019Q&a)\t\u000f\u0005\u000b\t\n1\u0001\u0002&B!1iFAT!\ri\u0013\u0011\u0016\u0003\u0007_\u0005E%\u0019\u0001\u0019\t\u000f=\u000b\t\n1\u0001\u0002.B)\u0011\u0011U)\u0002(\"A\u0011qJAI\u0001\u0004\t\t\fE\u0003\u0002\"F\u000b\u0019\u0006C\u0004W\u0003#\u0003\r!!.\u0011\t\u0005\u0005\u0016\u000b\u0017\u0005\t\u0003K\t\t\n1\u0001\u0002:B)q\"!\u000b\u0002<B!\u0011\u0011U)5\u0011\u001d\tyl\u0003C\u0001\u0003\u0003\f1b^1s]6+7o]1hKV!\u00111YAk)\u0011\t)-a4\u0015\t\u0005\u001d\u00171\u001c\u000b\u0005\u0003\u0013\f9\u000eE\u0002\u0002L\"s1!!4G\u001d\ri\u0013q\u001a\u0005\b\u0003\u0006u\u0006\u0019AAi!\u0011\u0019u#a5\u0011\u00075\n)\u000e\u0002\u00040\u0003{\u0013\r\u0001\r\u0005\b\u001f\u0006u\u0006\u0019AAm!\u0015\ti-UAj\u0011\u001d1\u0016Q\u0018a\u0001\u0003;\u0004B!!4R1\"9\u0011\u0011]\u0006\u0005\u0002\u0005\r\u0018\u0001E<be:lUm]:bO\u0016\u001c\u0015-^:f+\u0011\t)/a>\u0015\t\u0005\u001d\u0018\u0011\u001f\u000b\u0007\u0003S\fiP!\u0001\u0015\t\u0005-\u0018\u0011 \t\u0004\u0003[DebAAx\r:\u0019Q&!=\t\u000f\u0005\u000by\u000e1\u0001\u0002tB!1iFA{!\ri\u0013q\u001f\u0003\u0007_\u0005}'\u0019\u0001\u0019\t\u000f=\u000by\u000e1\u0001\u0002|B)\u0011q^)\u0002v\"9a+a8A\u0002\u0005}\b\u0003BAx#bCq!^Ap\u0001\u0004\u0011\u0019\u0001\u0005\u0003\u0002pF;\bb\u0002B\u0004\u0017\u0011\u0005!\u0011B\u0001\u0010o\u0006\u0014h.T3tg\u0006<W-\u0011:hgV!!1\u0002B\u000f)\u0011\u0011iAa\u0006\u0015\r\t=!1\u0005B\u0014)\u0011\u0011\tBa\b\u0011\u0007\tM\u0001JD\u0002\u0003\u0016\u0019s1!\fB\f\u0011\u001d\t%Q\u0001a\u0001\u00053\u0001BaQ\f\u0003\u001cA\u0019QF!\b\u0005\r=\u0012)A1\u00011\u0011\u001dy%Q\u0001a\u0001\u0005C\u0001RA!\u0006R\u00057AqA\u0016B\u0003\u0001\u0004\u0011)\u0003\u0005\u0003\u0003\u0016EC\u0006\u0002CA\u0013\u0005\u000b\u0001\rA!\u000b\u0011\u000b=\tICa\u000b\u0011\t\tU\u0011\u000b\u000e\u0005\b\u0005_YA\u0011\u0001B\u0019\u0003E9\u0018M\u001d8NKN\u001c\u0018mZ3NCJ\\WM]\u000b\u0005\u0005g\u0011)\u0005\u0006\u0003\u00036\t}BC\u0002B\u001c\u0005\u0017\u0012y\u0005\u0006\u0003\u0003:\t\u001d\u0003c\u0001B\u001e\u0011:\u0019!Q\b$\u000f\u00075\u0012y\u0004C\u0004B\u0005[\u0001\rA!\u0011\u0011\t\r;\"1\t\t\u0004[\t\u0015CAB\u0018\u0003.\t\u0007\u0001\u0007C\u0004P\u0005[\u0001\rA!\u0013\u0011\u000b\tu\u0012Ka\u0011\t\u0011\u0005=#Q\u0006a\u0001\u0005\u001b\u0002RA!\u0010R\u0003'BqA\u0016B\u0017\u0001\u0004\u0011\t\u0006\u0005\u0003\u0003>EC\u0006b\u0002B+\u0017\u0011\u0005!qK\u0001\u0017o\u0006\u0014h.T3tg\u0006<WmQ1vg\u0016l\u0015M]6feV!!\u0011\fB6)\u0011\u0011YF!\u001a\u0015\u0011\tu#\u0011\u000fB;\u0005s\"BAa\u0018\u0003nA\u0019!\u0011\r%\u000f\u0007\t\rdID\u0002.\u0005KBq!\u0011B*\u0001\u0004\u00119\u0007\u0005\u0003D/\t%\u0004cA\u0017\u0003l\u00111qFa\u0015C\u0002ABqa\u0014B*\u0001\u0004\u0011y\u0007E\u0003\u0003dE\u0013I\u0007\u0003\u0005\u0002P\tM\u0003\u0019\u0001B:!\u0015\u0011\u0019'UA*\u0011\u001d1&1\u000ba\u0001\u0005o\u0002BAa\u0019R1\"9QOa\u0015A\u0002\tm\u0004\u0003\u0002B2#^DqAa \f\t\u0003\u0011\t)A\u000bxCJtW*Z:tC\u001e,\u0017I]4t\u001b\u0006\u00148.\u001a:\u0016\t\t\r%Q\u0013\u000b\u0005\u0005\u000b\u0013y\t\u0006\u0005\u0003\b\nm%q\u0014BR)\u0011\u0011IIa&\u0011\u0007\t-\u0005JD\u0002\u0003\u000e\u001as1!\fBH\u0011\u001d\t%Q\u0010a\u0001\u0005#\u0003BaQ\f\u0003\u0014B\u0019QF!&\u0005\r=\u0012iH1\u00011\u0011\u001dy%Q\u0010a\u0001\u00053\u0003RA!$R\u0005'C\u0001\"a\u0014\u0003~\u0001\u0007!Q\u0014\t\u0006\u0005\u001b\u000b\u00161\u000b\u0005\b-\nu\u0004\u0019\u0001BQ!\u0011\u0011i)\u0015-\t\u0011\u0005\u0015\"Q\u0010a\u0001\u0005K\u0003RaDA\u0015\u0005O\u0003BA!$Ri!9!1V\u0006\u0005\u0002\t5\u0016aC5oM>lUm]:bO\u0016,BAa,\u0003BR!!\u0011\u0017B^)\u0011\u0011\u0019La2\u0015\t\tU&1\u0019\t\u0004\u0005oCeb\u0001B]\r:\u0019QFa/\t\u000f\u0005\u0013I\u000b1\u0001\u0003>B!1i\u0006B`!\ri#\u0011\u0019\u0003\u0007_\t%&\u0019\u0001\u0019\t\u000f=\u0013I\u000b1\u0001\u0003FB)!\u0011X)\u0003@\"9aK!+A\u0002\t%\u0007\u0003\u0002B]#bCqA!4\f\t\u0003\u0011y-\u0001\tj]\u001a|W*Z:tC\u001e,7)Y;tKV!!\u0011\u001bBr)\u0011\u0011\u0019N!8\u0015\r\tU'\u0011\u001eBw)\u0011\u00119N!:\u0011\u0007\te\u0007JD\u0002\u0003\\\u001as1!\fBo\u0011\u001d\t%1\u001aa\u0001\u0005?\u0004BaQ\f\u0003bB\u0019QFa9\u0005\r=\u0012YM1\u00011\u0011\u001dy%1\u001aa\u0001\u0005O\u0004RAa7R\u0005CDqA\u0016Bf\u0001\u0004\u0011Y\u000f\u0005\u0003\u0003\\FC\u0006bB;\u0003L\u0002\u0007!q\u001e\t\u0005\u00057\fv\u000fC\u0004\u0003t.!\tA!>\u0002\u001f%tgm\\'fgN\fw-Z!sON,BAa>\u0004\nQ!!\u0011`B\u0002)\u0019\u0011Ypa\u0004\u0004\u0014Q!!Q`B\u0006!\r\u0011y\u0010\u0013\b\u0004\u0007\u00031ebA\u0017\u0004\u0004!9\u0011I!=A\u0002\r\u0015\u0001\u0003B\"\u0018\u0007\u000f\u00012!LB\u0005\t\u0019y#\u0011\u001fb\u0001a!9qJ!=A\u0002\r5\u0001#BB\u0001#\u000e\u001d\u0001b\u0002,\u0003r\u0002\u00071\u0011\u0003\t\u0005\u0007\u0003\t\u0006\f\u0003\u0005\u0002&\tE\b\u0019AB\u000b!\u0015y\u0011\u0011FB\f!\u0011\u0019\t!\u0015\u001b\t\u000f\rm1\u0002\"\u0001\u0004\u001e\u0005\t\u0012N\u001c4p\u001b\u0016\u001c8/Y4f\u001b\u0006\u00148.\u001a:\u0016\t\r}1\u0011\u0007\u000b\u0005\u0007C\u0019Y\u0003\u0006\u0004\u0004$\r]21\b\u000b\u0005\u0007K\u0019\u0019\u0004E\u0002\u0004(!s1a!\u000bG\u001d\ri31\u0006\u0005\b\u0003\u000ee\u0001\u0019AB\u0017!\u0011\u0019uca\f\u0011\u00075\u001a\t\u0004\u0002\u00040\u00073\u0011\r\u0001\r\u0005\b\u001f\u000ee\u0001\u0019AB\u001b!\u0015\u0019I#UB\u0018\u0011!\tye!\u0007A\u0002\re\u0002#BB\u0015#\u0006M\u0003b\u0002,\u0004\u001a\u0001\u00071Q\b\t\u0005\u0007S\t\u0006\fC\u0004\u0004B-!\taa\u0011\u0002-%tgm\\'fgN\fw-Z\"bkN,W*\u0019:lKJ,Ba!\u0012\u0004XQ!1qIB))!\u0019Ie!\u0018\u0004b\r\u0015D\u0003BB&\u00073\u00022a!\u0014I\u001d\r\u0019yE\u0012\b\u0004[\rE\u0003bB!\u0004@\u0001\u000711\u000b\t\u0005\u0007^\u0019)\u0006E\u0002.\u0007/\"aaLB \u0005\u0004\u0001\u0004bB(\u0004@\u0001\u000711\f\t\u0006\u0007\u001f\n6Q\u000b\u0005\t\u0003\u001f\u001ay\u00041\u0001\u0004`A)1qJ)\u0002T!9aka\u0010A\u0002\r\r\u0004\u0003BB(#bCq!^B \u0001\u0004\u00199\u0007\u0005\u0003\u0004PE;\bbBB6\u0017\u0011\u00051QN\u0001\u0016S:4w.T3tg\u0006<W-\u0011:hg6\u000b'o[3s+\u0011\u0019yg!!\u0015\t\rE41\u0010\u000b\t\u0007g\u001a9ia#\u0004\u0010R!1QOBB!\r\u00199\b\u0013\b\u0004\u0007s2ebA\u0017\u0004|!9\u0011i!\u001bA\u0002\ru\u0004\u0003B\"\u0018\u0007\u007f\u00022!LBA\t\u0019y3\u0011\u000eb\u0001a!9qj!\u001bA\u0002\r\u0015\u0005#BB=#\u000e}\u0004\u0002CA(\u0007S\u0002\ra!#\u0011\u000b\re\u0014+a\u0015\t\u000fY\u001bI\u00071\u0001\u0004\u000eB!1\u0011P)Y\u0011!\t)c!\u001bA\u0002\rE\u0005#B\b\u0002*\rM\u0005\u0003BB=#RBqaa&\f\t\u0003\u0019I*\u0001\u0007eK\n,x-T3tg\u0006<W-\u0006\u0003\u0004\u001c\u000e5F\u0003BBO\u0007O#Baa(\u00044R!1\u0011UBX!\r\u0019\u0019\u000b\u0013\b\u0004\u0007K3ebA\u0017\u0004(\"9\u0011i!&A\u0002\r%\u0006\u0003B\"\u0018\u0007W\u00032!LBW\t\u0019y3Q\u0013b\u0001a!9qj!&A\u0002\rE\u0006#BBS#\u000e-\u0006b\u0002,\u0004\u0016\u0002\u00071Q\u0017\t\u0005\u0007K\u000b\u0006\fC\u0004\u0004:.!\taa/\u0002#\u0011,'-^4NKN\u001c\u0018mZ3DCV\u001cX-\u0006\u0003\u0004>\u000e=G\u0003BB`\u0007\u0013$ba!1\u0004V\u000eeG\u0003BBb\u0007#\u00042a!2I\u001d\r\u00199M\u0012\b\u0004[\r%\u0007bB!\u00048\u0002\u000711\u001a\t\u0005\u0007^\u0019i\rE\u0002.\u0007\u001f$aaLB\\\u0005\u0004\u0001\u0004bB(\u00048\u0002\u000711\u001b\t\u0006\u0007\u000f\f6Q\u001a\u0005\b-\u000e]\u0006\u0019ABl!\u0011\u00199-\u0015-\t\u000fU\u001c9\f1\u0001\u0004\\B!1qY)x\u0011\u001d\u0019yn\u0003C\u0001\u0007C\f\u0001\u0003Z3ck\u001elUm]:bO\u0016\f%oZ:\u0016\t\r\r8Q\u001f\u000b\u0005\u0007K\u001cy\u000f\u0006\u0004\u0004h\u000em8q \u000b\u0005\u0007S\u001c9\u0010E\u0002\u0004l\"s1a!<G\u001d\ri3q\u001e\u0005\b\u0003\u000eu\u0007\u0019ABy!\u0011\u0019uca=\u0011\u00075\u001a)\u0010\u0002\u00040\u0007;\u0014\r\u0001\r\u0005\b\u001f\u000eu\u0007\u0019AB}!\u0015\u0019i/UBz\u0011\u001d16Q\u001ca\u0001\u0007{\u0004Ba!<R1\"A\u0011QEBo\u0001\u0004!\t\u0001E\u0003\u0010\u0003S!\u0019\u0001\u0005\u0003\u0004nF#\u0004b\u0002C\u0004\u0017\u0011\u0005A\u0011B\u0001\u0013I\u0016\u0014WoZ'fgN\fw-Z'be.,'/\u0006\u0003\u0005\f\u0011uA\u0003\u0002C\u0007\t/!b\u0001b\u0004\u0005$\u0011\u001dB\u0003\u0002C\t\t?\u00012\u0001b\u0005I\u001d\r!)B\u0012\b\u0004[\u0011]\u0001bB!\u0005\u0006\u0001\u0007A\u0011\u0004\t\u0005\u0007^!Y\u0002E\u0002.\t;!aa\fC\u0003\u0005\u0004\u0001\u0004bB(\u0005\u0006\u0001\u0007A\u0011\u0005\t\u0006\t+\tF1\u0004\u0005\t\u0003\u001f\")\u00011\u0001\u0005&A)AQC)\u0002T!9a\u000b\"\u0002A\u0002\u0011%\u0002\u0003\u0002C\u000b#bCq\u0001\"\f\f\t\u0003!y#A\feK\n,x-T3tg\u0006<WmQ1vg\u0016l\u0015M]6feV!A\u0011\u0007C\")\u0011!\u0019\u0004\"\u0010\u0015\u0011\u0011UB\u0011\nC'\t#\"B\u0001b\u000e\u0005FA\u0019A\u0011\b%\u000f\u0007\u0011mbID\u0002.\t{Aq!\u0011C\u0016\u0001\u0004!y\u0004\u0005\u0003D/\u0011\u0005\u0003cA\u0017\u0005D\u00111q\u0006b\u000bC\u0002ABqa\u0014C\u0016\u0001\u0004!9\u0005E\u0003\u0005<E#\t\u0005\u0003\u0005\u0002P\u0011-\u0002\u0019\u0001C&!\u0015!Y$UA*\u0011\u001d1F1\u0006a\u0001\t\u001f\u0002B\u0001b\u000fR1\"9Q\u000fb\u000bA\u0002\u0011M\u0003\u0003\u0002C\u001e#^Dq\u0001b\u0016\f\t\u0003!I&\u0001\feK\n,x-T3tg\u0006<W-\u0011:hg6\u000b'o[3s+\u0011!Y\u0006\"\u001c\u0015\t\u0011uCq\r\u000b\t\t?\"\u0019\bb\u001e\u0005|Q!A\u0011\rC8!\r!\u0019\u0007\u0013\b\u0004\tK2ebA\u0017\u0005h!9\u0011\t\"\u0016A\u0002\u0011%\u0004\u0003B\"\u0018\tW\u00022!\fC7\t\u0019yCQ\u000bb\u0001a!9q\n\"\u0016A\u0002\u0011E\u0004#\u0002C3#\u0012-\u0004\u0002CA(\t+\u0002\r\u0001\"\u001e\u0011\u000b\u0011\u0015\u0014+a\u0015\t\u000fY#)\u00061\u0001\u0005zA!AQM)Y\u0011!\t)\u0003\"\u0016A\u0002\u0011u\u0004#B\b\u0002*\u0011}\u0004\u0003\u0002C3#RBq\u0001b!\f\t\u0003!))\u0001\u0007ue\u0006\u001cW-T3tg\u0006<W-\u0006\u0003\u0005\b\u0012eE\u0003\u0002CE\t'#B\u0001b#\u0005 R!AQ\u0012CN!\r!y\t\u0013\b\u0004\t#3ebA\u0017\u0005\u0014\"9\u0011\t\"!A\u0002\u0011U\u0005\u0003B\"\u0018\t/\u00032!\fCM\t\u0019yC\u0011\u0011b\u0001a!9q\n\"!A\u0002\u0011u\u0005#\u0002CI#\u0012]\u0005b\u0002,\u0005\u0002\u0002\u0007A\u0011\u0015\t\u0005\t#\u000b\u0006\fC\u0004\u0005&.!\t\u0001b*\u0002#Q\u0014\u0018mY3NKN\u001c\u0018mZ3DCV\u001cX-\u0006\u0003\u0005*\u0012mF\u0003\u0002CV\tk#b\u0001\",\u0005B\u0012\u0015G\u0003\u0002CX\t{\u00032\u0001\"-I\u001d\r!\u0019L\u0012\b\u0004[\u0011U\u0006bB!\u0005$\u0002\u0007Aq\u0017\t\u0005\u0007^!I\fE\u0002.\tw#aa\fCR\u0005\u0004\u0001\u0004bB(\u0005$\u0002\u0007Aq\u0018\t\u0006\tg\u000bF\u0011\u0018\u0005\b-\u0012\r\u0006\u0019\u0001Cb!\u0011!\u0019,\u0015-\t\u000fU$\u0019\u000b1\u0001\u0005HB!A1W)x\u0011\u001d!Ym\u0003C\u0001\t\u001b\f\u0001\u0003\u001e:bG\u0016lUm]:bO\u0016\f%oZ:\u0016\t\u0011=G\u0011\u001d\u000b\u0005\t#$Y\u000e\u0006\u0004\u0005T\u0012\u001dH1\u001e\u000b\u0005\t+$\u0019\u000fE\u0002\u0005X\"s1\u0001\"7G\u001d\riC1\u001c\u0005\b\u0003\u0012%\u0007\u0019\u0001Co!\u0011\u0019u\u0003b8\u0011\u00075\"\t\u000f\u0002\u00040\t\u0013\u0014\r\u0001\r\u0005\b\u001f\u0012%\u0007\u0019\u0001Cs!\u0015!I.\u0015Cp\u0011\u001d1F\u0011\u001aa\u0001\tS\u0004B\u0001\"7R1\"A\u0011Q\u0005Ce\u0001\u0004!i\u000fE\u0003\u0010\u0003S!y\u000f\u0005\u0003\u0005ZF#\u0004b\u0002Cz\u0017\u0011\u0005AQ_\u0001\u0013iJ\f7-Z'fgN\fw-Z'be.,'/\u0006\u0003\u0005x\u0016%A\u0003\u0002C}\u000b\u0007!b\u0001b?\u0006\u0010\u0015MA\u0003\u0002C\u007f\u000b\u0017\u00012\u0001b@I\u001d\r)\tA\u0012\b\u0004[\u0015\r\u0001bB!\u0005r\u0002\u0007QQ\u0001\t\u0005\u0007^)9\u0001E\u0002.\u000b\u0013!aa\fCy\u0005\u0004\u0001\u0004bB(\u0005r\u0002\u0007QQ\u0002\t\u0006\u000b\u0003\tVq\u0001\u0005\t\u0003\u001f\"\t\u00101\u0001\u0006\u0012A)Q\u0011A)\u0002T!9a\u000b\"=A\u0002\u0015U\u0001\u0003BC\u0001#bCq!\"\u0007\f\t\u0003)Y\"A\fue\u0006\u001cW-T3tg\u0006<WmQ1vg\u0016l\u0015M]6feV!QQDC\u0018)\u0011)y\"\"\u000b\u0015\u0011\u0015\u0005RQGC\u001d\u000b{!B!b\t\u00062A\u0019QQ\u0005%\u000f\u0007\u0015\u001dbID\u0002.\u000bSAq!QC\f\u0001\u0004)Y\u0003\u0005\u0003D/\u00155\u0002cA\u0017\u00060\u00111q&b\u0006C\u0002ABqaTC\f\u0001\u0004)\u0019\u0004E\u0003\u0006(E+i\u0003\u0003\u0005\u0002P\u0015]\u0001\u0019AC\u001c!\u0015)9#UA*\u0011\u001d1Vq\u0003a\u0001\u000bw\u0001B!b\nR1\"9Q/b\u0006A\u0002\u0015}\u0002\u0003BC\u0014#^Dq!b\u0011\f\t\u0003))%\u0001\fue\u0006\u001cW-T3tg\u0006<W-\u0011:hg6\u000b'o[3s+\u0011)9%\"\u0017\u0015\t\u0015%S1\u000b\u000b\t\u000b\u0017*y&b\u0019\u0006hQ!QQJC.!\r)y\u0005\u0013\b\u0004\u000b#2ebA\u0017\u0006T!9\u0011)\"\u0011A\u0002\u0015U\u0003\u0003B\"\u0018\u000b/\u00022!LC-\t\u0019yS\u0011\tb\u0001a!9q*\"\u0011A\u0002\u0015u\u0003#BC)#\u0016]\u0003\u0002CA(\u000b\u0003\u0002\r!\"\u0019\u0011\u000b\u0015E\u0013+a\u0015\t\u000fY+\t\u00051\u0001\u0006fA!Q\u0011K)Y\u0011!\t)#\"\u0011A\u0002\u0015%\u0004#B\b\u0002*\u0015-\u0004\u0003BC)#R\u0002")
public final class LoggerTakingImplicitMacro {
    public static <A> Trees.TreeApi traceMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.traceMessageArgsMarker(context, expr, expr2, seq, expr3);
    }

    public static <A> Trees.TreeApi traceMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3, Exprs.Expr<A> expr4) {
        return LoggerTakingImplicitMacro$.MODULE$.traceMessageCauseMarker(context, expr, expr2, expr3, expr4);
    }

    public static <A> Trees.TreeApi traceMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.traceMessageMarker(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi traceMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.traceMessageArgs(context, expr, seq, expr2);
    }

    public static <A> Trees.TreeApi traceMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.traceMessageCause(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi traceMessage(Context context, Exprs.Expr<String> expr, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.traceMessage(context, expr, expr2);
    }

    public static <A> Trees.TreeApi debugMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.debugMessageArgsMarker(context, expr, expr2, seq, expr3);
    }

    public static <A> Trees.TreeApi debugMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3, Exprs.Expr<A> expr4) {
        return LoggerTakingImplicitMacro$.MODULE$.debugMessageCauseMarker(context, expr, expr2, expr3, expr4);
    }

    public static <A> Trees.TreeApi debugMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.debugMessageMarker(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi debugMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.debugMessageArgs(context, expr, seq, expr2);
    }

    public static <A> Trees.TreeApi debugMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.debugMessageCause(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi debugMessage(Context context, Exprs.Expr<String> expr, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.debugMessage(context, expr, expr2);
    }

    public static <A> Trees.TreeApi infoMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.infoMessageArgsMarker(context, expr, expr2, seq, expr3);
    }

    public static <A> Trees.TreeApi infoMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3, Exprs.Expr<A> expr4) {
        return LoggerTakingImplicitMacro$.MODULE$.infoMessageCauseMarker(context, expr, expr2, expr3, expr4);
    }

    public static <A> Trees.TreeApi infoMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.infoMessageMarker(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi infoMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.infoMessageArgs(context, expr, seq, expr2);
    }

    public static <A> Trees.TreeApi infoMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.infoMessageCause(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi infoMessage(Context context, Exprs.Expr<String> expr, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.infoMessage(context, expr, expr2);
    }

    public static <A> Trees.TreeApi warnMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.warnMessageArgsMarker(context, expr, expr2, seq, expr3);
    }

    public static <A> Trees.TreeApi warnMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3, Exprs.Expr<A> expr4) {
        return LoggerTakingImplicitMacro$.MODULE$.warnMessageCauseMarker(context, expr, expr2, expr3, expr4);
    }

    public static <A> Trees.TreeApi warnMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.warnMessageMarker(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi warnMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.warnMessageArgs(context, expr, seq, expr2);
    }

    public static <A> Trees.TreeApi warnMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.warnMessageCause(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi warnMessage(Context context, Exprs.Expr<String> expr, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.warnMessage(context, expr, expr2);
    }

    public static <A> Trees.TreeApi errorMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.errorMessageArgsMarker(context, expr, expr2, seq, expr3);
    }

    public static <A> Trees.TreeApi errorMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3, Exprs.Expr<A> expr4) {
        return LoggerTakingImplicitMacro$.MODULE$.errorMessageCauseMarker(context, expr, expr2, expr3, expr4);
    }

    public static <A> Trees.TreeApi errorMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.errorMessageMarker(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi errorMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.errorMessageArgs(context, expr, seq, expr2);
    }

    public static <A> Trees.TreeApi errorMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2, Exprs.Expr<A> expr3) {
        return LoggerTakingImplicitMacro$.MODULE$.errorMessageCause(context, expr, expr2, expr3);
    }

    public static <A> Trees.TreeApi errorMessage(Context context, Exprs.Expr<String> expr, Exprs.Expr<A> expr2) {
        return LoggerTakingImplicitMacro$.MODULE$.errorMessage(context, expr, expr2);
    }
}

