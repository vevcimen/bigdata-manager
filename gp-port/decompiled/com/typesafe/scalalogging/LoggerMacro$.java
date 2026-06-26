/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Function1
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.StringContext$
 *  scala.Tuple2
 *  scala.collection.GenTraversableOnce
 *  scala.collection.Seq
 *  scala.collection.Seq$
 *  scala.collection.immutable.$colon$colon
 *  scala.collection.immutable.List
 *  scala.collection.immutable.List$
 *  scala.collection.immutable.Nil$
 *  scala.reflect.api.Constants$ConstantApi
 *  scala.reflect.api.Exprs$Expr
 *  scala.reflect.api.Names$TermNameApi
 *  scala.reflect.api.Trees$IdentApi
 *  scala.reflect.api.Trees$LiteralApi
 *  scala.reflect.api.Trees$SelectApi
 *  scala.reflect.api.Trees$TreeApi
 *  scala.reflect.macros.blackbox.Context
 *  scala.runtime.BoxedUnit
 */
package com.typesafe.scalalogging;

import java.io.Serializable;
import org.slf4j.Marker;
import scala.Function1;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Some;
import scala.StringContext$;
import scala.Tuple2;
import scala.collection.GenTraversableOnce;
import scala.collection.Seq;
import scala.collection.Seq$;
import scala.collection.immutable.;
import scala.collection.immutable.List;
import scala.collection.immutable.List$;
import scala.collection.immutable.Nil$;
import scala.reflect.api.Constants;
import scala.reflect.api.Exprs;
import scala.reflect.api.Names;
import scala.reflect.api.Trees;
import scala.reflect.macros.blackbox.Context;
import scala.runtime.BoxedUnit;

public final class LoggerMacro$ {
    public static LoggerMacro$ MODULE$;

    static {
        new LoggerMacro$();
    }

    public Trees.TreeApi errorMessage(Context c, Exprs.Expr<String> message) {
        Exprs.Expr<String> expr = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.errorMessageArgs(c, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi errorMessageCause(Context c, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<String> expr = message;
        Exprs.Expr<Throwable> expr2 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("error")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi errorMessageArgs(Context c, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<String> expr = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("error")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("error")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$20 -> c.universe().Liftable().liftExpr().apply(fresh$macro$20), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi errorMessageMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr2);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.errorMessageArgsMarker(c, expr, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi errorMessageCauseMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Exprs.Expr<Throwable> expr3 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("error")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr3), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi errorMessageArgsMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("error")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("error")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$42 -> c.universe().Liftable().liftExpr().apply(fresh$macro$42), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi errorCode(Context c, Exprs.Expr<BoxedUnit> body) {
        Exprs.Expr<BoxedUnit> expr = body;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isErrorEnabled")), c.universe().Liftable().liftExpr().apply(expr), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi warnMessage(Context c, Exprs.Expr<String> message) {
        Exprs.Expr<String> expr = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.warnMessageArgs(c, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi warnMessageCause(Context c, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<String> expr = message;
        Exprs.Expr<Throwable> expr2 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("warn")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi warnMessageArgs(Context c, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<String> expr = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("warn")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("warn")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$61 -> c.universe().Liftable().liftExpr().apply(fresh$macro$61), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi warnMessageMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr2);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.warnMessageArgsMarker(c, expr, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi warnMessageCauseMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Exprs.Expr<Throwable> expr3 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("warn")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr3), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi warnMessageArgsMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("warn")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("warn")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$83 -> c.universe().Liftable().liftExpr().apply(fresh$macro$83), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi warnCode(Context c, Exprs.Expr<BoxedUnit> body) {
        Exprs.Expr<BoxedUnit> expr = body;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isWarnEnabled")), c.universe().Liftable().liftExpr().apply(expr), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi infoMessage(Context c, Exprs.Expr<String> message) {
        Exprs.Expr<String> expr = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.infoMessageArgs(c, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi infoMessageCause(Context c, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<String> expr = message;
        Exprs.Expr<Throwable> expr2 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("info")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi infoMessageArgs(Context c, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<String> expr = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("info")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("info")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$102 -> c.universe().Liftable().liftExpr().apply(fresh$macro$102), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi infoMessageMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr2);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.infoMessageArgsMarker(c, expr, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi infoMessageCauseMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Exprs.Expr<Throwable> expr3 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("info")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr3), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi infoMessageArgsMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("info")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("info")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$124 -> c.universe().Liftable().liftExpr().apply(fresh$macro$124), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi infoCode(Context c, Exprs.Expr<BoxedUnit> body) {
        Exprs.Expr<BoxedUnit> expr = body;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isInfoEnabled")), c.universe().Liftable().liftExpr().apply(expr), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi debugMessage(Context c, Exprs.Expr<String> message) {
        Exprs.Expr<String> expr = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.debugMessageArgs(c, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi debugMessageCause(Context c, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<String> expr = message;
        Exprs.Expr<Throwable> expr2 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("debug")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi debugMessageArgs(Context c, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<String> expr = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("debug")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("debug")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$143 -> c.universe().Liftable().liftExpr().apply(fresh$macro$143), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi debugMessageMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr2);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.debugMessageArgsMarker(c, expr, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi debugMessageCauseMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Exprs.Expr<Throwable> expr3 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("debug")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr3), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi debugMessageArgsMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("debug")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("debug")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$165 -> c.universe().Liftable().liftExpr().apply(fresh$macro$165), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi debugCode(Context c, Exprs.Expr<BoxedUnit> body) {
        Exprs.Expr<BoxedUnit> expr = body;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isDebugEnabled")), c.universe().Liftable().liftExpr().apply(expr), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi traceMessage(Context c, Exprs.Expr<String> message) {
        Exprs.Expr<String> expr = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.traceMessageArgs(c, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi traceMessageCause(Context c, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<String> expr = message;
        Exprs.Expr<Throwable> expr2 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("trace")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi traceMessageArgs(Context c, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<String> expr = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("trace")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("trace")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$184 -> c.universe().Liftable().liftExpr().apply(fresh$macro$184), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi traceMessageMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> tuple2 = this.deconstructInterpolatedMessage(c, expr2);
        if (tuple2 == null) {
            throw new MatchError(tuple2);
        }
        Exprs.Expr messageFormat = (Exprs.Expr)tuple2._1();
        Seq args = (Seq)tuple2._2();
        Tuple2 tuple22 = new Tuple2((Object)messageFormat, (Object)args);
        Tuple2 tuple23 = tuple22;
        Exprs.Expr messageFormat2 = (Exprs.Expr)tuple23._1();
        Seq args2 = (Seq)tuple23._2();
        return this.traceMessageArgsMarker(c, expr, (Exprs.Expr<String>)messageFormat2, (Seq<Exprs.Expr<Object>>)args2);
    }

    public Trees.TreeApi traceMessageCauseMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Exprs.Expr<Throwable> cause) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Exprs.Expr<Throwable> expr3 = cause;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("trace")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr3), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi traceMessageArgsMarker(Context c, Exprs.Expr<Marker> marker, Exprs.Expr<String> message, Seq<Exprs.Expr<Object>> args) {
        Exprs.Expr<Marker> expr = marker;
        Exprs.Expr<String> expr2 = message;
        Seq<Exprs.Expr<Object>> seq = args;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        Seq<Exprs.Expr<Object>> anyRefArgs = this.formatArgs(c, seq);
        return seq.length() == 2 ? c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("trace")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)new .colon.colon((Object)c.universe().Typed().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TermName().apply("Array")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.head()), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(anyRefArgs.apply(1)), (List)Nil$.MODULE$)), (List)Nil$.MODULE$)), (Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTypeIdent().apply(c.universe().TypeName().apply("_*"))), (List)Nil$.MODULE$))), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$)) : c.universe().If().apply(c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), (List)new .colon.colon((Object)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)Nil$.MODULE$), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("trace")), (List)new .colon.colon((Object)((List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr), (List)new .colon.colon((Object)c.universe().Liftable().liftExpr().apply(expr2), (List)Nil$.MODULE$)).$plus$plus((GenTraversableOnce)anyRefArgs.toList().map((Function1 & Serializable & scala.Serializable)fresh$macro$206 -> c.universe().Liftable().liftExpr().apply(fresh$macro$206), List$.MODULE$.canBuildFrom()), List$.MODULE$.canBuildFrom())), (List)Nil$.MODULE$)), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    public Trees.TreeApi traceCode(Context c, Exprs.Expr<BoxedUnit> body) {
        Exprs.Expr<BoxedUnit> expr = body;
        Trees.SelectApi underlying = c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply((Object)c.prefix()), c.universe().TermName().apply("underlying"));
        return c.universe().If().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)underlying, c.universe().TermName().apply("isTraceEnabled")), c.universe().Liftable().liftExpr().apply(expr), c.universe().internal().reificationSupport().SyntacticBlock().apply((List)Nil$.MODULE$));
    }

    private Tuple2<Exprs.Expr<String>, Seq<Exprs.Expr<Object>>> deconstructInterpolatedMessage(Context c, Exprs.Expr<String> message) {
        Tuple2 tuple2;
        Exprs.Expr<String> expr = message;
        Trees.TreeApi treeApi = expr.tree();
        Option<Tuple2<List<Trees.TreeApi>, List<Trees.TreeApi>>> option = new Object(c){
            private final Context c$1;

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public Option<Tuple2<List<Trees.TreeApi>, List<Trees.TreeApi>>> unapply(Object tree) {
                Object object = tree;
                Option option = this.c$1.universe().TreeTag().unapply(object);
                if (option.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi = (Trees.TreeApi)option.get();
                Some some = this.c$1.universe().internal().reificationSupport().SyntacticApplied().unapply(treeApi);
                if (some.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi2 = (Trees.TreeApi)((Tuple2)some.get())._1();
                List list = (List)((Tuple2)some.get())._2();
                Option option2 = this.c$1.universe().TreeTag().unapply((Object)treeApi2);
                if (option2.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi3 = (Trees.TreeApi)option2.get();
                Option option3 = this.c$1.universe().internal().reificationSupport().SyntacticSelectTerm().unapply(treeApi3);
                if (option3.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi4 = (Trees.TreeApi)((Tuple2)option3.get())._1();
                Names.TermNameApi termNameApi = (Names.TermNameApi)((Tuple2)option3.get())._2();
                Option option4 = this.c$1.universe().TreeTag().unapply((Object)treeApi4);
                if (option4.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi5 = (Trees.TreeApi)option4.get();
                Some some2 = this.c$1.universe().internal().reificationSupport().SyntacticApplied().unapply(treeApi5);
                if (some2.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi6 = (Trees.TreeApi)((Tuple2)some2.get())._1();
                List list2 = (List)((Tuple2)some2.get())._2();
                Option option5 = this.c$1.universe().TreeTag().unapply((Object)treeApi6);
                if (option5.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi7 = (Trees.TreeApi)option5.get();
                Option option6 = this.c$1.universe().internal().reificationSupport().SyntacticSelectTerm().unapply(treeApi7);
                if (option6.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi8 = (Trees.TreeApi)((Tuple2)option6.get())._1();
                Names.TermNameApi termNameApi2 = (Names.TermNameApi)((Tuple2)option6.get())._2();
                Option option7 = this.c$1.universe().TreeTag().unapply((Object)treeApi8);
                if (option7.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi9 = (Trees.TreeApi)option7.get();
                Option option8 = this.c$1.universe().internal().reificationSupport().SyntacticSelectTerm().unapply(treeApi9);
                if (option8.isEmpty()) return None$.MODULE$;
                Trees.TreeApi treeApi10 = (Trees.TreeApi)((Tuple2)option8.get())._1();
                Names.TermNameApi termNameApi3 = (Names.TermNameApi)((Tuple2)option8.get())._2();
                Option option9 = this.c$1.universe().IdentTag().unapply((Object)treeApi10);
                if (option9.isEmpty()) return None$.MODULE$;
                Trees.IdentApi identApi = (Trees.IdentApi)option9.get();
                Option option10 = this.c$1.universe().internal().reificationSupport().SyntacticTermIdent().unapply(identApi);
                if (option10.isEmpty()) return None$.MODULE$;
                Names.TermNameApi termNameApi4 = (Names.TermNameApi)((Tuple2)option10.get())._1();
                boolean bl = ((Tuple2)option10.get())._2$mcZ$sp();
                Option option11 = this.c$1.universe().TermNameTag().unapply((Object)termNameApi4);
                if (option11.isEmpty()) return None$.MODULE$;
                Names.TermNameApi termNameApi5 = (Names.TermNameApi)option11.get();
                Option option12 = this.c$1.universe().TermName().unapply(termNameApi5);
                if (option12.isEmpty()) return None$.MODULE$;
                String string = (String)option12.get();
                if (!"scala".equals(string)) return None$.MODULE$;
                if (false != bl) return None$.MODULE$;
                Option option13 = this.c$1.universe().TermNameTag().unapply((Object)termNameApi3);
                if (option13.isEmpty()) return None$.MODULE$;
                Names.TermNameApi termNameApi6 = (Names.TermNameApi)option13.get();
                Option option14 = this.c$1.universe().TermName().unapply(termNameApi6);
                if (option14.isEmpty()) return None$.MODULE$;
                String string2 = (String)option14.get();
                if (!"StringContext".equals(string2)) return None$.MODULE$;
                Option option15 = this.c$1.universe().TermNameTag().unapply((Object)termNameApi2);
                if (option15.isEmpty()) return None$.MODULE$;
                Names.TermNameApi termNameApi7 = (Names.TermNameApi)option15.get();
                Option option16 = this.c$1.universe().TermName().unapply(termNameApi7);
                if (option16.isEmpty()) return None$.MODULE$;
                String string3 = (String)option16.get();
                if (!"apply".equals(string3)) return None$.MODULE$;
                if (!(list2 instanceof .colon.colon)) return None$.MODULE$;
                .colon.colon colon2 = (.colon.colon)list2;
                List qq$77340ecc$macro$1 = (List)colon2.head();
                List list3 = colon2.tl$access$1();
                if (!Nil$.MODULE$.equals(list3)) return None$.MODULE$;
                Option option17 = this.c$1.universe().TermNameTag().unapply((Object)termNameApi);
                if (option17.isEmpty()) return None$.MODULE$;
                Names.TermNameApi termNameApi8 = (Names.TermNameApi)option17.get();
                Option option18 = this.c$1.universe().TermName().unapply(termNameApi8);
                if (option18.isEmpty()) return None$.MODULE$;
                String string4 = (String)option18.get();
                if (!"s".equals(string4)) return None$.MODULE$;
                if (!(list instanceof .colon.colon)) return None$.MODULE$;
                .colon.colon colon3 = (.colon.colon)list;
                List qq$77340ecc$macro$2 = (List)colon3.head();
                List list4 = colon3.tl$access$1();
                if (!Nil$.MODULE$.equals(list4)) return None$.MODULE$;
                return new Some((Object)new Tuple2((Object)qq$77340ecc$macro$1, (Object)qq$77340ecc$macro$2));
            }
            {
                this.c$1 = c$1;
            }
        }.unapply(treeApi);
        if (!option.isEmpty()) {
            List parts = (List)((Tuple2)option.get())._1();
            List args = (List)((Tuple2)option.get())._2();
            String format = parts.iterator().map((Function1 & Serializable & scala.Serializable)x0$1 -> {
                String string;
                Object str;
                block3: {
                    Trees.TreeApi treeApi;
                    block2: {
                        treeApi = x0$1;
                        Option option = c.universe().LiteralTag().unapply((Object)treeApi);
                        if (option.isEmpty()) break block2;
                        Trees.LiteralApi literalApi = (Trees.LiteralApi)option.get();
                        Option option2 = c.universe().Literal().unapply(literalApi);
                        if (option2.isEmpty()) break block2;
                        Constants.ConstantApi constantApi = (Constants.ConstantApi)option2.get();
                        Option option3 = c.universe().ConstantTag().unapply((Object)constantApi);
                        if (option3.isEmpty()) break block2;
                        Constants.ConstantApi constantApi2 = (Constants.ConstantApi)option3.get();
                        Option option4 = c.universe().Constant().unapply(constantApi2);
                        if (!option4.isEmpty() && (str = option4.get()) instanceof String) break block3;
                    }
                    throw new MatchError((Object)treeApi);
                }
                String string2 = string = (String)str;
                return string2;
            }).map((Function1 & Serializable & scala.Serializable)str -> StringContext$.MODULE$.treatEscapes(str)).map((Function1 & Serializable & scala.Serializable)str -> args.nonEmpty() ? str.replace("{}", "\\{}") : str).mkString("{}");
            List formatArgs = (List)args.map((Function1 & Serializable & scala.Serializable)t -> c.Expr(t, c.universe().WeakTypeTag().Any()), List$.MODULE$.canBuildFrom());
            tuple2 = new Tuple2((Object)c.Expr(c.universe().Liftable().liftString().apply((Object)format), c.universe().WeakTypeTag().Nothing()), (Object)formatArgs);
        } else {
            tuple2 = new Tuple2(expr, (Object)Seq$.MODULE$.empty());
        }
        return tuple2;
    }

    private Seq<Exprs.Expr<Object>> formatArgs(Context c, Seq<Exprs.Expr<Object>> args) {
        Seq<Exprs.Expr<Object>> seq = args;
        return (Seq)seq.map((Function1 & Serializable & scala.Serializable)arg -> c.Expr(arg.tree().tpe().$less$colon$less(c.universe().weakTypeOf(c.universe().WeakTypeTag().AnyRef())) ? arg.tree() : c.universe().internal().reificationSupport().SyntacticTypeApplied().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply(c.universe().Liftable().liftExpr().apply(arg), c.universe().TermName().apply("asInstanceOf")), (List)new .colon.colon((Object)c.universe().internal().reificationSupport().SyntacticSelectType().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticSelectTerm().apply((Trees.TreeApi)c.universe().internal().reificationSupport().SyntacticTermIdent().apply(c.universe().TermName().apply("_root_"), false), c.universe().TermName().apply("scala")), c.universe().TypeName().apply("AnyRef")), (List)Nil$.MODULE$)), c.universe().WeakTypeTag().AnyRef()), Seq$.MODULE$.canBuildFrom());
    }

    private LoggerMacro$() {
        MODULE$ = this;
    }
}

