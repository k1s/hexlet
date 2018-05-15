package hexlet

import org.scalatest.{FlatSpec, Matchers}

class EvalSpec extends FlatSpec with Matchers {

  implicit val env = Environment.root

  it should "eval simple expressions" in {
    Eval.eval(Fun(Add, Seq(Num(1), Num(2)))) shouldEqual Num(3)

    Eval.eval(Fun(Sub, Seq(Num(1), Num(2), Num(1)))) shouldEqual Num(-2)

    Eval.eval(Fun(Mul, Seq(Num(1), Num(2), Num(3)))) shouldEqual Num(6)

    Eval.eval(Fun(Div, Seq(Num(10), Num(5), Num(0.5)))) shouldEqual Num(4)
  }

  it should "eval compound expressions" in {
    val expr = Fun(Add, Seq(Num(1), Num(2)))

    Eval.eval(Fun(Add, Seq(expr, expr, expr))) shouldEqual Num(9)
  }

}
