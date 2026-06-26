/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.collection.Seq
 *  scala.reflect.ScalaSignature
 *  scala.reflect.api.Exprs$Expr
 *  scala.reflect.api.Trees$TreeApi
 *  scala.reflect.macros.blackbox.Context
 *  scala.runtime.BoxedUnit
 */
package com.typesafe.scalalogging;

import com.typesafe.scalalogging.LoggerMacro$;
import org.slf4j.Marker;
import scala.collection.Seq;
import scala.reflect.ScalaSignature;
import scala.reflect.api.Exprs;
import scala.reflect.api.Trees;
import scala.reflect.macros.blackbox.Context;
import scala.runtime.BoxedUnit;

@ScalaSignature(bytes="\u0006\u0001\u0011}s!B\u0001\u0003\u0011\u0013I\u0011a\u0003'pO\u001e,'/T1de>T!a\u0001\u0003\u0002\u0019M\u001c\u0017\r\\1m_\u001e<\u0017N\\4\u000b\u0005\u00151\u0011\u0001\u0003;za\u0016\u001c\u0018MZ3\u000b\u0003\u001d\t1aY8n\u0007\u0001\u0001\"AC\u0006\u000e\u0003\t1Q\u0001\u0004\u0002\t\n5\u00111\u0002T8hO\u0016\u0014X*Y2s_N\u00111B\u0004\t\u0003\u001fIi\u0011\u0001\u0005\u0006\u0002#\u0005)1oY1mC&\u00111\u0003\u0005\u0002\u0007\u0003:L(+\u001a4\t\u000bUYA\u0011\u0001\f\u0002\rqJg.\u001b;?)\u0005IQ\u0001\u0002\r\f\u0001e\u0011Q\u0002T8hO\u0016\u00148i\u001c8uKb$(C\u0001\u000e\u001d\r\u0011Y2\u0002A\r\u0003\u0019q\u0012XMZ5oK6,g\u000e\u001e \u0011\u0005u!S\"\u0001\u0010\u000b\u0005}\u0001\u0013\u0001\u00032mC\u000e\\'m\u001c=\u000b\u0005\u0005\u0012\u0013AB7bGJ|7O\u0003\u0002$!\u00059!/\u001a4mK\u000e$\u0018BA\u0013\u001f\u0005\u001d\u0019uN\u001c;fqR,Aa\n\u000e!Q\tQ\u0001K]3gSb$\u0016\u0010]3\u0011\u0005)I\u0013B\u0001\u0016\u0003\u0005\u0019aunZ4fe\")Af\u0003C\u0001[\u0005aQM\u001d:pe6+7o]1hKR\u0011af\r\u000b\u0003_}\u0002\"\u0001M\u001d\u000f\u0005E:dB\u0001\u001a4\u0019\u0001AQ\u0001N\u0016A\u0002U\n\u0011a\u0019\t\u0003m]i\u0011aC\u0005\u0003q\u0011\n\u0001\"\u001e8jm\u0016\u00148/Z\u0005\u0003um\u0012A\u0001\u0016:fK&\u0011A(\u0010\u0002\u0006)J,Wm\u001d\u0006\u0003}\t\n1!\u00199j\u0011\u0015\u00015\u00061\u0001B\u0003\u001diWm]:bO\u0016\u00042!\r\"G\u0013\t\u0019EI\u0001\u0003FqB\u0014\u0018BA#!\u0005\u001d\tE.[1tKN\u0004\"a\u0012(\u000f\u0005!c\u0005CA%\u0011\u001b\u0005Q%BA&\t\u0003\u0019a$o\\8u}%\u0011Q\nE\u0001\u0007!J,G-\u001a4\n\u0005=\u0003&AB*ue&twM\u0003\u0002N!!)!k\u0003C\u0001'\u0006\tRM\u001d:pe6+7o]1hK\u000e\u000bWo]3\u0015\u0005QCFcA+Z7B\u0011a+\u000f\b\u0003/^r!A\r-\t\u000bQ\n\u0006\u0019A\u001b\t\u000b\u0001\u000b\u0006\u0019\u0001.\u0011\u0007]\u0013e\tC\u0003]#\u0002\u0007Q,A\u0003dCV\u001cX\rE\u0002X\u0005z\u0003\"a\u00183\u000f\u0005\u0001\u0014gBA%b\u0013\u0005\t\u0012BA2\u0011\u0003\u001d\u0001\u0018mY6bO\u0016L!!\u001a4\u0003\u0013QC'o\\<bE2,'BA2\u0011\u0011\u0015A7\u0002\"\u0001j\u0003A)'O]8s\u001b\u0016\u001c8/Y4f\u0003J<7\u000f\u0006\u0002k]R\u00191n\\9\u0011\u00051LdBA78\u001d\t\u0011d\u000eC\u00035O\u0002\u0007Q\u0007C\u0003AO\u0002\u0007\u0001\u000fE\u0002n\u0005\u001aCQA]4A\u0002M\fA!\u0019:hgB\u0019q\u0002\u001e<\n\u0005U\u0004\"A\u0003\u001fsKB,\u0017\r^3e}A\u0019QNQ<\u0011\u0005=A\u0018BA=\u0011\u0005\r\te.\u001f\u0005\u0006w.!\t\u0001`\u0001\u0013KJ\u0014xN]'fgN\fw-Z'be.,'\u000fF\u0002~\u0003\u0007!RA`A\u0003\u00037\u0001\"a`\u001d\u000f\u0007\u0005\u0005qGD\u00023\u0003\u0007AQ\u0001\u000e>A\u0002UBq!a\u0002{\u0001\u0004\tI!\u0001\u0004nCJ\\WM\u001d\t\u0006\u0003\u0003\u0011\u00151\u0002\t\u0005\u0003\u001b\t9\"\u0004\u0002\u0002\u0010)!\u0011\u0011CA\n\u0003\u0015\u0019HN\u001a\u001bk\u0015\t\t)\"A\u0002pe\u001eLA!!\u0007\u0002\u0010\t1Q*\u0019:lKJDa\u0001\u0011>A\u0002\u0005u\u0001\u0003BA\u0001\u0005\u001aCq!!\t\f\t\u0003\t\u0019#A\ffeJ|'/T3tg\u0006<WmQ1vg\u0016l\u0015M]6feR!\u0011QEA\u0017)!\t9#a\f\u00024\u0005]\u0002cAA\u0015s9\u0019\u00111F\u001c\u000f\u0007I\ni\u0003\u0003\u00045\u0003?\u0001\r!\u000e\u0005\t\u0003\u000f\ty\u00021\u0001\u00022A)\u00111\u0006\"\u0002\f!9\u0001)a\bA\u0002\u0005U\u0002\u0003BA\u0016\u0005\u001aCq\u0001XA\u0010\u0001\u0004\tI\u0004\u0005\u0003\u0002,\ts\u0006bBA\u001f\u0017\u0011\u0005\u0011qH\u0001\u0017KJ\u0014xN]'fgN\fw-Z!sONl\u0015M]6feR!\u0011\u0011IA%)!\t\u0019%a\u0013\u0002P\u0005M\u0003cAA#s9\u0019\u0011qI\u001c\u000f\u0007I\nI\u0005\u0003\u00045\u0003w\u0001\r!\u000e\u0005\t\u0003\u000f\tY\u00041\u0001\u0002NA)\u0011q\t\"\u0002\f!9\u0001)a\u000fA\u0002\u0005E\u0003\u0003BA$\u0005\u001aCqA]A\u001e\u0001\u0004\t)\u0006\u0005\u0003\u0010i\u0006]\u0003\u0003BA$\u0005^Dq!a\u0017\f\t\u0003\ti&A\u0005feJ|'oQ8eKR!\u0011qLA4)\u0011\t\t'!\u001b\u0011\u0007\u0005\r\u0014HD\u0002\u0002f]r1AMA4\u0011\u0019!\u0014\u0011\fa\u0001k!A\u00111NA-\u0001\u0004\ti'\u0001\u0003c_\u0012L\b#BA3\u0005\u0006=\u0004cA\b\u0002r%\u0019\u00111\u000f\t\u0003\tUs\u0017\u000e\u001e\u0005\b\u0003oZA\u0011AA=\u0003-9\u0018M\u001d8NKN\u001c\u0018mZ3\u0015\t\u0005m\u00141\u0011\u000b\u0005\u0003{\n)\tE\u0002\u0002\u0000er1!!!8\u001d\r\u0011\u00141\u0011\u0005\u0007i\u0005U\u0004\u0019A\u001b\t\u000f\u0001\u000b)\b1\u0001\u0002\bB!\u0011\u0011\u0011\"G\u0011\u001d\tYi\u0003C\u0001\u0003\u001b\u000b\u0001c^1s]6+7o]1hK\u000e\u000bWo]3\u0015\t\u0005=\u0015q\u0013\u000b\u0007\u0003#\u000bI*!(\u0011\u0007\u0005M\u0015HD\u0002\u0002\u0016^r1AMAL\u0011\u0019!\u0014\u0011\u0012a\u0001k!9\u0001)!#A\u0002\u0005m\u0005\u0003BAK\u0005\u001aCq\u0001XAE\u0001\u0004\ty\n\u0005\u0003\u0002\u0016\ns\u0006bBAR\u0017\u0011\u0005\u0011QU\u0001\u0010o\u0006\u0014h.T3tg\u0006<W-\u0011:hgR!\u0011qUAX)\u0019\tI+!-\u00026B\u0019\u00111V\u001d\u000f\u0007\u00055vGD\u00023\u0003_Ca\u0001NAQ\u0001\u0004)\u0004b\u0002!\u0002\"\u0002\u0007\u00111\u0017\t\u0005\u0003[\u0013e\tC\u0004s\u0003C\u0003\r!a.\u0011\t=!\u0018\u0011\u0018\t\u0005\u0003[\u0013u\u000fC\u0004\u0002>.!\t!a0\u0002#]\f'O\\'fgN\fw-Z'be.,'\u000f\u0006\u0003\u0002B\u0006%GCBAb\u0003\u0017\fy\rE\u0002\u0002Ffr1!a28\u001d\r\u0011\u0014\u0011\u001a\u0005\u0007i\u0005m\u0006\u0019A\u001b\t\u0011\u0005\u001d\u00111\u0018a\u0001\u0003\u001b\u0004R!a2C\u0003\u0017Aq\u0001QA^\u0001\u0004\t\t\u000e\u0005\u0003\u0002H\n3\u0005bBAk\u0017\u0011\u0005\u0011q[\u0001\u0017o\u0006\u0014h.T3tg\u0006<WmQ1vg\u0016l\u0015M]6feR!\u0011\u0011\\Aq)!\tY.a9\u0002h\u0006-\bcAAos9\u0019\u0011q\\\u001c\u000f\u0007I\n\t\u000f\u0003\u00045\u0003'\u0004\r!\u000e\u0005\t\u0003\u000f\t\u0019\u000e1\u0001\u0002fB)\u0011q\u001c\"\u0002\f!9\u0001)a5A\u0002\u0005%\b\u0003BAp\u0005\u001aCq\u0001XAj\u0001\u0004\ti\u000f\u0005\u0003\u0002`\ns\u0006bBAy\u0017\u0011\u0005\u00111_\u0001\u0016o\u0006\u0014h.T3tg\u0006<W-\u0011:hg6\u000b'o[3s)\u0011\t)0!@\u0015\u0011\u0005]\u0018q B\u0002\u0005\u000f\u00012!!?:\u001d\r\tYp\u000e\b\u0004e\u0005u\bB\u0002\u001b\u0002p\u0002\u0007Q\u0007\u0003\u0005\u0002\b\u0005=\b\u0019\u0001B\u0001!\u0015\tYPQA\u0006\u0011\u001d\u0001\u0015q\u001ea\u0001\u0005\u000b\u0001B!a?C\r\"9!/a<A\u0002\t%\u0001\u0003B\bu\u0005\u0017\u0001B!a?Co\"9!qB\u0006\u0005\u0002\tE\u0011\u0001C<be:\u001cu\u000eZ3\u0015\t\tM!1\u0004\u000b\u0005\u0005+\u0011i\u0002E\u0002\u0003\u0018er1A!\u00078\u001d\r\u0011$1\u0004\u0005\u0007i\t5\u0001\u0019A\u001b\t\u0011\u0005-$Q\u0002a\u0001\u0005?\u0001RA!\u0007C\u0003_BqAa\t\f\t\u0003\u0011)#A\u0006j]\u001a|W*Z:tC\u001e,G\u0003\u0002B\u0014\u0005_!BA!\u000b\u00032A\u0019!1F\u001d\u000f\u0007\t5rGD\u00023\u0005_Aa\u0001\u000eB\u0011\u0001\u0004)\u0004b\u0002!\u0003\"\u0001\u0007!1\u0007\t\u0005\u0005[\u0011e\tC\u0004\u00038-!\tA!\u000f\u0002!%tgm\\'fgN\fw-Z\"bkN,G\u0003\u0002B\u001e\u0005\u0007\"bA!\u0010\u0003F\t%\u0003c\u0001B s9\u0019!\u0011I\u001c\u000f\u0007I\u0012\u0019\u0005\u0003\u00045\u0005k\u0001\r!\u000e\u0005\b\u0001\nU\u0002\u0019\u0001B$!\u0011\u0011\tE\u0011$\t\u000fq\u0013)\u00041\u0001\u0003LA!!\u0011\t\"_\u0011\u001d\u0011ye\u0003C\u0001\u0005#\nq\"\u001b8g_6+7o]1hK\u0006\u0013xm\u001d\u000b\u0005\u0005'\u0012Y\u0006\u0006\u0004\u0003V\tu#\u0011\r\t\u0004\u0005/Jdb\u0001B-o9\u0019!Ga\u0017\t\rQ\u0012i\u00051\u00016\u0011\u001d\u0001%Q\na\u0001\u0005?\u0002BA!\u0017C\r\"9!O!\u0014A\u0002\t\r\u0004\u0003B\bu\u0005K\u0002BA!\u0017Co\"9!\u0011N\u0006\u0005\u0002\t-\u0014!E5oM>lUm]:bO\u0016l\u0015M]6feR!!Q\u000eB;)\u0019\u0011yGa\u001e\u0003|A\u0019!\u0011O\u001d\u000f\u0007\tMtGD\u00023\u0005kBa\u0001\u000eB4\u0001\u0004)\u0004\u0002CA\u0004\u0005O\u0002\rA!\u001f\u0011\u000b\tM$)a\u0003\t\u000f\u0001\u00139\u00071\u0001\u0003~A!!1\u000f\"G\u0011\u001d\u0011\ti\u0003C\u0001\u0005\u0007\u000ba#\u001b8g_6+7o]1hK\u000e\u000bWo]3NCJ\\WM\u001d\u000b\u0005\u0005\u000b\u0013i\t\u0006\u0005\u0003\b\n=%1\u0013BL!\r\u0011I)\u000f\b\u0004\u0005\u0017;db\u0001\u001a\u0003\u000e\"1AGa A\u0002UB\u0001\"a\u0002\u0003\u0000\u0001\u0007!\u0011\u0013\t\u0006\u0005\u0017\u0013\u00151\u0002\u0005\b\u0001\n}\u0004\u0019\u0001BK!\u0011\u0011YI\u0011$\t\u000fq\u0013y\b1\u0001\u0003\u001aB!!1\u0012\"_\u0011\u001d\u0011ij\u0003C\u0001\u0005?\u000bQ#\u001b8g_6+7o]1hK\u0006\u0013xm]'be.,'\u000f\u0006\u0003\u0003\"\n%F\u0003\u0003BR\u0005W\u0013yKa-\u0011\u0007\t\u0015\u0016HD\u0002\u0003(^r1A\rBU\u0011\u0019!$1\u0014a\u0001k!A\u0011q\u0001BN\u0001\u0004\u0011i\u000bE\u0003\u0003(\n\u000bY\u0001C\u0004A\u00057\u0003\rA!-\u0011\t\t\u001d&I\u0012\u0005\be\nm\u0005\u0019\u0001B[!\u0011yAOa.\u0011\t\t\u001d&i\u001e\u0005\b\u0005w[A\u0011\u0001B_\u0003!IgNZ8D_\u0012,G\u0003\u0002B`\u0005\u000f$BA!1\u0003JB\u0019!1Y\u001d\u000f\u0007\t\u0015wGD\u00023\u0005\u000fDa\u0001\u000eB]\u0001\u0004)\u0004\u0002CA6\u0005s\u0003\rAa3\u0011\u000b\t\u0015')a\u001c\t\u000f\t=7\u0002\"\u0001\u0003R\u0006aA-\u001a2vO6+7o]1hKR!!1\u001bBn)\u0011\u0011)N!8\u0011\u0007\t]\u0017HD\u0002\u0003Z^r1A\rBn\u0011\u0019!$Q\u001aa\u0001k!9\u0001I!4A\u0002\t}\u0007\u0003\u0002Bm\u0005\u001aCqAa9\f\t\u0003\u0011)/A\teK\n,x-T3tg\u0006<WmQ1vg\u0016$BAa:\u0003pR1!\u0011\u001eBy\u0005k\u00042Aa;:\u001d\r\u0011io\u000e\b\u0004e\t=\bB\u0002\u001b\u0003b\u0002\u0007Q\u0007C\u0004A\u0005C\u0004\rAa=\u0011\t\t5(I\u0012\u0005\b9\n\u0005\b\u0019\u0001B|!\u0011\u0011iO\u00110\t\u000f\tm8\u0002\"\u0001\u0003~\u0006\u0001B-\u001a2vO6+7o]1hK\u0006\u0013xm\u001d\u000b\u0005\u0005\u007f\u001c9\u0001\u0006\u0004\u0004\u0002\r%1Q\u0002\t\u0004\u0007\u0007IdbAB\u0003o9\u0019!ga\u0002\t\rQ\u0012I\u00101\u00016\u0011\u001d\u0001%\u0011 a\u0001\u0007\u0017\u0001Ba!\u0002C\r\"9!O!?A\u0002\r=\u0001\u0003B\bu\u0007#\u0001Ba!\u0002Co\"91QC\u0006\u0005\u0002\r]\u0011A\u00053fEV<W*Z:tC\u001e,W*\u0019:lKJ$Ba!\u0007\u0004\"Q111DB\u0012\u0007O\u00012a!\b:\u001d\r\u0019yb\u000e\b\u0004e\r\u0005\u0002B\u0002\u001b\u0004\u0014\u0001\u0007Q\u0007\u0003\u0005\u0002\b\rM\u0001\u0019AB\u0013!\u0015\u0019yBQA\u0006\u0011\u001d\u000151\u0003a\u0001\u0007S\u0001Baa\bC\r\"91QF\u0006\u0005\u0002\r=\u0012a\u00063fEV<W*Z:tC\u001e,7)Y;tK6\u000b'o[3s)\u0011\u0019\td!\u000f\u0015\u0011\rM21HB \u0007\u0007\u00022a!\u000e:\u001d\r\u00199d\u000e\b\u0004e\re\u0002B\u0002\u001b\u0004,\u0001\u0007Q\u0007\u0003\u0005\u0002\b\r-\u0002\u0019AB\u001f!\u0015\u00199DQA\u0006\u0011\u001d\u000151\u0006a\u0001\u0007\u0003\u0002Baa\u000eC\r\"9Ala\u000bA\u0002\r\u0015\u0003\u0003BB\u001c\u0005zCqa!\u0013\f\t\u0003\u0019Y%\u0001\feK\n,x-T3tg\u0006<W-\u0011:hg6\u000b'o[3s)\u0011\u0019ie!\u0016\u0015\u0011\r=3qKB.\u0007?\u00022a!\u0015:\u001d\r\u0019\u0019f\u000e\b\u0004e\rU\u0003B\u0002\u001b\u0004H\u0001\u0007Q\u0007\u0003\u0005\u0002\b\r\u001d\u0003\u0019AB-!\u0015\u0019\u0019FQA\u0006\u0011\u001d\u00015q\ta\u0001\u0007;\u0002Baa\u0015C\r\"9!oa\u0012A\u0002\r\u0005\u0004\u0003B\bu\u0007G\u0002Baa\u0015Co\"91qM\u0006\u0005\u0002\r%\u0014!\u00033fEV<7i\u001c3f)\u0011\u0019Yga\u001d\u0015\t\r54Q\u000f\t\u0004\u0007_JdbAB9o9\u0019!ga\u001d\t\rQ\u001a)\u00071\u00016\u0011!\tYg!\u001aA\u0002\r]\u0004#BB9\u0005\u0006=\u0004bBB>\u0017\u0011\u00051QP\u0001\riJ\f7-Z'fgN\fw-\u001a\u000b\u0005\u0007\u007f\u001a9\t\u0006\u0003\u0004\u0002\u000e%\u0005cABBs9\u00191QQ\u001c\u000f\u0007I\u001a9\t\u0003\u00045\u0007s\u0002\r!\u000e\u0005\b\u0001\u000ee\u0004\u0019ABF!\u0011\u0019)I\u0011$\t\u000f\r=5\u0002\"\u0001\u0004\u0012\u0006\tBO]1dK6+7o]1hK\u000e\u000bWo]3\u0015\t\rM51\u0014\u000b\u0007\u0007+\u001bij!)\u0011\u0007\r]\u0015HD\u0002\u0004\u001a^r1AMBN\u0011\u0019!4Q\u0012a\u0001k!9\u0001i!$A\u0002\r}\u0005\u0003BBM\u0005\u001aCq\u0001XBG\u0001\u0004\u0019\u0019\u000b\u0005\u0003\u0004\u001a\ns\u0006bBBT\u0017\u0011\u00051\u0011V\u0001\u0011iJ\f7-Z'fgN\fw-Z!sON$Baa+\u00044R11QVB[\u0007s\u00032aa,:\u001d\r\u0019\tl\u000e\b\u0004e\rM\u0006B\u0002\u001b\u0004&\u0002\u0007Q\u0007C\u0004A\u0007K\u0003\raa.\u0011\t\rE&I\u0012\u0005\be\u000e\u0015\u0006\u0019AB^!\u0011yAo!0\u0011\t\rE&i\u001e\u0005\b\u0007\u0003\\A\u0011ABb\u0003I!(/Y2f\u001b\u0016\u001c8/Y4f\u001b\u0006\u00148.\u001a:\u0015\t\r\u00157Q\u001a\u000b\u0007\u0007\u000f\u001cyma5\u0011\u0007\r%\u0017HD\u0002\u0004L^r1AMBg\u0011\u0019!4q\u0018a\u0001k!A\u0011qAB`\u0001\u0004\u0019\t\u000eE\u0003\u0004L\n\u000bY\u0001C\u0004A\u0007\u007f\u0003\ra!6\u0011\t\r-'I\u0012\u0005\b\u00073\\A\u0011ABn\u0003]!(/Y2f\u001b\u0016\u001c8/Y4f\u0007\u0006,8/Z'be.,'\u000f\u0006\u0003\u0004^\u000e\u0015H\u0003CBp\u0007O\u001cYoa<\u0011\u0007\r\u0005\u0018HD\u0002\u0004d^r1AMBs\u0011\u0019!4q\u001ba\u0001k!A\u0011qABl\u0001\u0004\u0019I\u000fE\u0003\u0004d\n\u000bY\u0001C\u0004A\u0007/\u0004\ra!<\u0011\t\r\r(I\u0012\u0005\b9\u000e]\u0007\u0019ABy!\u0011\u0019\u0019O\u00110\t\u000f\rU8\u0002\"\u0001\u0004x\u00061BO]1dK6+7o]1hK\u0006\u0013xm]'be.,'\u000f\u0006\u0003\u0004z\u0012\u0005A\u0003CB~\t\u0007!9\u0001b\u0003\u0011\u0007\ru\u0018HD\u0002\u0004\u0000^r1A\rC\u0001\u0011\u0019!41\u001fa\u0001k!A\u0011qABz\u0001\u0004!)\u0001E\u0003\u0004\u0000\n\u000bY\u0001C\u0004A\u0007g\u0004\r\u0001\"\u0003\u0011\t\r}(I\u0012\u0005\be\u000eM\b\u0019\u0001C\u0007!\u0011yA\u000fb\u0004\u0011\t\r}(i\u001e\u0005\b\t'YA\u0011\u0001C\u000b\u0003%!(/Y2f\u0007>$W\r\u0006\u0003\u0005\u0018\u0011}A\u0003\u0002C\r\tC\u00012\u0001b\u0007:\u001d\r!ib\u000e\b\u0004e\u0011}\u0001B\u0002\u001b\u0005\u0012\u0001\u0007Q\u0007\u0003\u0005\u0002l\u0011E\u0001\u0019\u0001C\u0012!\u0015!iBQA8\u0011\u001d!9c\u0003C\u0005\tS\ta\u0004Z3d_:\u001cHO];di&sG/\u001a:q_2\fG/\u001a3NKN\u001c\u0018mZ3\u0015\t\u0011-Bq\u0007\u000b\u0005\t[!9\u0005E\u0004\u0010\t_!\u0019\u0004\"\u000f\n\u0007\u0011E\u0002C\u0001\u0004UkBdWM\r\t\u0005\tk\u0011eID\u00023\toAa\u0001\u000eC\u0013\u0001\u0004)\u0004C\u0002C\u001e\t\u0003\")%\u0004\u0002\u0005>)\u0019Aq\b\t\u0002\u0015\r|G\u000e\\3di&|g.\u0003\u0003\u0005D\u0011u\"aA*fcB!AQ\u0007\"x\u0011\u001d\u0001EQ\u0005a\u0001\tgAq\u0001b\u0013\f\t\u0013!i%\u0001\u0006g_Jl\u0017\r^!sON$B\u0001b\u0014\u0005XQ!A\u0011\u000bC-!\u0019!Y\u0004\"\u0011\u0005TA!AQ\u000b\"\u000f\u001d\r\u0011Dq\u000b\u0005\u0007i\u0011%\u0003\u0019A\u001b\t\u000fI$I\u00051\u0001\u0005\\A!q\u0002\u001eC/!\u0011!)FQ<")
public final class LoggerMacro {
    public static Trees.TreeApi traceCode(Context context, Exprs.Expr<BoxedUnit> expr) {
        return LoggerMacro$.MODULE$.traceCode(context, expr);
    }

    public static Trees.TreeApi traceMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.traceMessageArgsMarker(context, expr, expr2, seq);
    }

    public static Trees.TreeApi traceMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3) {
        return LoggerMacro$.MODULE$.traceMessageCauseMarker(context, expr, expr2, expr3);
    }

    public static Trees.TreeApi traceMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2) {
        return LoggerMacro$.MODULE$.traceMessageMarker(context, expr, expr2);
    }

    public static Trees.TreeApi traceMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.traceMessageArgs(context, expr, seq);
    }

    public static Trees.TreeApi traceMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2) {
        return LoggerMacro$.MODULE$.traceMessageCause(context, expr, expr2);
    }

    public static Trees.TreeApi traceMessage(Context context, Exprs.Expr<String> expr) {
        return LoggerMacro$.MODULE$.traceMessage(context, expr);
    }

    public static Trees.TreeApi debugCode(Context context, Exprs.Expr<BoxedUnit> expr) {
        return LoggerMacro$.MODULE$.debugCode(context, expr);
    }

    public static Trees.TreeApi debugMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.debugMessageArgsMarker(context, expr, expr2, seq);
    }

    public static Trees.TreeApi debugMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3) {
        return LoggerMacro$.MODULE$.debugMessageCauseMarker(context, expr, expr2, expr3);
    }

    public static Trees.TreeApi debugMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2) {
        return LoggerMacro$.MODULE$.debugMessageMarker(context, expr, expr2);
    }

    public static Trees.TreeApi debugMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.debugMessageArgs(context, expr, seq);
    }

    public static Trees.TreeApi debugMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2) {
        return LoggerMacro$.MODULE$.debugMessageCause(context, expr, expr2);
    }

    public static Trees.TreeApi debugMessage(Context context, Exprs.Expr<String> expr) {
        return LoggerMacro$.MODULE$.debugMessage(context, expr);
    }

    public static Trees.TreeApi infoCode(Context context, Exprs.Expr<BoxedUnit> expr) {
        return LoggerMacro$.MODULE$.infoCode(context, expr);
    }

    public static Trees.TreeApi infoMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.infoMessageArgsMarker(context, expr, expr2, seq);
    }

    public static Trees.TreeApi infoMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3) {
        return LoggerMacro$.MODULE$.infoMessageCauseMarker(context, expr, expr2, expr3);
    }

    public static Trees.TreeApi infoMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2) {
        return LoggerMacro$.MODULE$.infoMessageMarker(context, expr, expr2);
    }

    public static Trees.TreeApi infoMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.infoMessageArgs(context, expr, seq);
    }

    public static Trees.TreeApi infoMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2) {
        return LoggerMacro$.MODULE$.infoMessageCause(context, expr, expr2);
    }

    public static Trees.TreeApi infoMessage(Context context, Exprs.Expr<String> expr) {
        return LoggerMacro$.MODULE$.infoMessage(context, expr);
    }

    public static Trees.TreeApi warnCode(Context context, Exprs.Expr<BoxedUnit> expr) {
        return LoggerMacro$.MODULE$.warnCode(context, expr);
    }

    public static Trees.TreeApi warnMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.warnMessageArgsMarker(context, expr, expr2, seq);
    }

    public static Trees.TreeApi warnMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3) {
        return LoggerMacro$.MODULE$.warnMessageCauseMarker(context, expr, expr2, expr3);
    }

    public static Trees.TreeApi warnMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2) {
        return LoggerMacro$.MODULE$.warnMessageMarker(context, expr, expr2);
    }

    public static Trees.TreeApi warnMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.warnMessageArgs(context, expr, seq);
    }

    public static Trees.TreeApi warnMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2) {
        return LoggerMacro$.MODULE$.warnMessageCause(context, expr, expr2);
    }

    public static Trees.TreeApi warnMessage(Context context, Exprs.Expr<String> expr) {
        return LoggerMacro$.MODULE$.warnMessage(context, expr);
    }

    public static Trees.TreeApi errorCode(Context context, Exprs.Expr<BoxedUnit> expr) {
        return LoggerMacro$.MODULE$.errorCode(context, expr);
    }

    public static Trees.TreeApi errorMessageArgsMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.errorMessageArgsMarker(context, expr, expr2, seq);
    }

    public static Trees.TreeApi errorMessageCauseMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2, Exprs.Expr<Throwable> expr3) {
        return LoggerMacro$.MODULE$.errorMessageCauseMarker(context, expr, expr2, expr3);
    }

    public static Trees.TreeApi errorMessageMarker(Context context, Exprs.Expr<Marker> expr, Exprs.Expr<String> expr2) {
        return LoggerMacro$.MODULE$.errorMessageMarker(context, expr, expr2);
    }

    public static Trees.TreeApi errorMessageArgs(Context context, Exprs.Expr<String> expr, Seq<Exprs.Expr<Object>> seq) {
        return LoggerMacro$.MODULE$.errorMessageArgs(context, expr, seq);
    }

    public static Trees.TreeApi errorMessageCause(Context context, Exprs.Expr<String> expr, Exprs.Expr<Throwable> expr2) {
        return LoggerMacro$.MODULE$.errorMessageCause(context, expr, expr2);
    }

    public static Trees.TreeApi errorMessage(Context context, Exprs.Expr<String> expr) {
        return LoggerMacro$.MODULE$.errorMessage(context, expr);
    }
}

