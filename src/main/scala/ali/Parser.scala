package ali

import scala.util.parsing.combinator.RegexParsers

object Parser extends RegexParsers {

  def parse(expr: String): Either[String, Expr] =
    parseAll(hexlet, expr) match {
      case Failure(msg, _) =>
        Left(s"Cannot parse: $msg!")
      case Success(result, _) =>
        Right(result.head)
    }

  def id: Parser[Id] = """[a-zA-Z-_+*=<>\/][a-zA-Z0-9]*""".r ^^ { id => Id(id)}

  def number: Parser[Num] = """(0|[1-9]\d*)""".r ^^ { x => Num(x.toDouble) }

  def atom: Parser[Atom] = number | id

  def apply: Parser[Apply] =
    "(" ~> expr ~ rep(expr) <~ ")" ^^ { case fun ~ args => Apply(fun, args) }

  //TODO fn [x y z]
  def lambda: Parser[Lambda] =
    "(" ~> "\\" ~> rep(id) ~ expr <~ ")" ^^ { case args ~ expr => Lambda(args, expr) }

  def fun: Parser[Fun] =
    "(" ~> "def" ~> id ~ expr <~ ")" ^^ { case name ~ expr => Fun(name, expr) }

  def iF: Parser[If] =
    "(" ~> "if" ~> expr ~ expr ~ expr <~ ")" ^^ { case e1 ~ e2 ~ e3 => If(e1, e2, e3) }

  def vec: Parser[Vec] = "[" ~> rep(expr) <~ "]" ^^ { exprs => Vec(exprs.toVector) }

  def expr: Parser[Expr] = atom | iF | fun | apply | lambda | vec

  def hexlet: Parser[Seq[Expr]] = rep(expr)

}
