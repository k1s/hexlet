package ali

import Expr.implicits._
import cats.Show
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging

/*
 * Could be result of evaluation or update of env like function addition
 */
sealed trait Eval {

  def let(f: Env => Eval): Eval

}

case class EnvUpdate(env: Env) extends Eval {

  override def let(f: Env => Eval) = f(env)

}

case class Result(result: Either[String, Expr]) extends Eval {

  override def let(f: Env => Eval) = this

}

object Eval extends LazyLogging {

  type ExprFun = List[Expr] => Expr

  def eval(expr: Expr)(implicit env: Env): Eval =
    expr match {
      case fn: Def =>
        EnvUpdate(env.addDef(fn))
      case _ =>
        Result(evalExpression(expr))
    }

  def evalExpression(expr: Expr)(implicit env: Env): Either[String, Expr] = {
    expr match {
      case x: If =>
        evalIf(x)
      case Apply(fun, args) =>
        fun match {
          case name: Id =>
            env.get(name.id) match {
              case Right(f) =>
                args.traverse(e => evalExpression(e)(env)).flatMap {
                  applyF(f, _)
                }
              case left =>
                logger.trace(s"no match in env for ${name.id}")
                left
            }
          case Lambda(lambdaArgs, body) =>
            closure(body, lambdaArgs.map(_.id).toList, args, Map(), env)
        }
      case Id(id) =>
        logger.trace(s"getting id $id from env")
        env.get(id).flatMap(evalExpression)
      case f: Foldable =>
        Right(f)
      case other =>
        Left(s"Evaluation of $other is impossibru!!!")
    }
  }

  @scala.annotation.tailrec
  def closure(body: Expr,
              ids: List[String],
              exprs: List[Expr],
              closureEnv: Map[String, Expr],
              env: Env): Either[String, Expr] =
    (ids, exprs) match {
      case (Nil, Nil) =>
        evalExpression(body)(Env(env, closureEnv))
      case (Nil, exs_) =>
        evalExpression(Apply(body, exs_))(Env(env, closureEnv))
      case (idsHead :: idsTail, exsHead :: exsTail) =>
        closure(body, idsTail, exsTail, closureEnv + (idsHead -> exsHead), env)
      case _ =>
        Left(s"Lambda args size error: $ids != $exprs")
    }

  def applyF(toApply: Expr, args: List[Expr])(implicit env: Env): Either[String, Expr] =
    toApply match {
      case Predefined(f) =>
        Right(f(args))
      case l: Lambda =>
        evalExpression(Apply(l, args))
      case other =>
        Right(other)
    }

  def evalIf(iF: If)(implicit env: Env): Either[String, Expr] =
    evalExpression(iF.test) match {
      case Right(Bool(b)) =>
        if (b)
          evalExpression(iF.thenExpr)
        else
          evalExpression(iF.elseExpr)
      case Right(other) =>
        Left(s"Condition of if should be boolean, but it's $other")
      case other =>
        other
    }

  object EvalSyntax {

    implicit val resultShow: Show[Result] = {
      case Result(Left(error)) => error
      case Result(Right(expr)) => expr.show
    }

  }

}
