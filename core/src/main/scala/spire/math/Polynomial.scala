package spire.math

import compat._
import scala.annotation.tailrec
import spire.algebra._
import spire.implicits._
import spire.syntax._

/**
 * Polynomial
 * A univariate polynomial class and EuclideanRing extension trait 
 * for arithmetic operations.Polynomials can be instantiated using 
 * any type C, with exponents given by Int values. Arithmetic and 
 * many other basic operations require either implicit Ring[C] 
 * and/or Field[C]'s in scope.
*/


// Univariate polynomial term
case class Term[C](coeff: C, exp: Int)(implicit r: Ring[C], s: Signed[C]) {

  def toTuple: (Int, C) = (exp, coeff)

  def eval(x: C): C = 
    coeff * (x pow exp)

  def isIndexZero: Boolean = 
    exp == 0

  def isZero: Boolean =
    s.sign(coeff) == Sign.Zero

  def divideBy(x: C)(implicit f: Field[C]): Term[C] =
    Term(coeff / x, exp)

  def der: Term[C] =
    Term(coeff * r.fromInt(exp), exp - 1)

  def int(implicit f: Field[C]): Term[C] =
    Term(coeff / f.fromInt(exp + 1), exp + 1)

  override def toString = {
    import r._
    val pos = (s.sign(coeff) != Sign.Negative)
    (coeff, exp) match {
      case (0, _) => ""
      case (c, 0) => if (pos) s" + ${c}" else s" - ${-c}"
      case (1, e) => if (e > 1) s" + x^$e" else s" + x"
      case (-1, e) => if (e > 1) s" - x^$e" else s" - x"
      case (c, 1) => if (pos) s" + ${c}x" else s" - ${-c}x"
      case (c, e) => if (pos) s" + ${c}x^$e" else s" - ${-c}x^$e"
    }
  }
}

object Term {
  def fromTuple[C: Ring : Signed](tpl: (Int, C)): Term[C] = Term(tpl._2, tpl._1)
  def zero[C: Signed](implicit r: Ring[C]): Term[C] = Term(r.zero, 0)
  def one[C: Signed](implicit r: Ring[C]): Term[C] = Term(r.one, 0)
}


// Univariate polynomial class
class Polynomial[C] private[spire] (val data: Map[Int, C])(implicit r: Ring[C], s: Signed[C]) {

  override def equals(that: Any): Boolean = that match {
    case p: Polynomial[_] => data == p.data
    case _ => false
  }

  def terms: List[Term[C]] =
    data.map(Term.fromTuple(_)).toList

  implicit object BigEndianPolynomialOrdering extends Order[Term[C]] {
    def compare(x:Term[C], y:Term[C]): Int = y.exp compare x.exp
  }

  def allTerms: List[Term[C]] = {
    val m = degree
    val cs = new Array[Term[C]](m + 1)
    terms.foreach(t => cs(t.exp) = t)
    for(i <- 0 to m)
      if (cs(i) == null) cs(i) = Term(r.zero, i)
    cs.toList.reverse
  }

  def coeffs: List[C] =
    allTerms.map(_.coeff)

  def maxTerm: Term[C] =
    data.foldLeft(Term.zero[C]) { case (term, (e, c)) =>
      if (term.exp <= e) Term(c, e) else term
    }

  def degree: Int =
    if (data.isEmpty) 0 else data.keys.qmax

  def maxOrderTermCoeff: C =
    maxTerm.coeff

  def apply(x: C): C =
    data.view.foldLeft(r.zero)((sum, t) => sum + Term.fromTuple(t).eval(x))

  def isZero: Boolean =
    data.forall { case (e, c) => s.sign(c) == Sign.Zero }

  def isEmpty : Boolean = 
    data.isEmpty

  def monic(implicit f: Field[C]): Polynomial[C] = {
    val m = maxOrderTermCoeff
    new Polynomial(data.map { case (e, c) => (e, c / m) })
  }

  def derivative: Polynomial[C] =
    Polynomial(data.flatMap { case (e, c) =>
      if (e > 0) Some(Term(c, e).der) else None
    })

  def integral(implicit f: Field[C]): Polynomial[C] =
    Polynomial(data.map(t => Term.fromTuple(t).int))

  override def toString =
    if (isZero) {
      "(0)"
    } else {
      val ts = terms.toArray
      QuickSort.sort(ts)
      val s = ts.mkString
      "(" + (if (s.take(3) == " - ") "-" + s.drop(3) else s.drop(3)) + ")"
    }

}


object Polynomial {

  def apply[C: Ring](data: Map[Int, C])(implicit s: Signed[C]): Polynomial[C] =
    new Polynomial(data.filterNot { case (e, c) => s.sign(c) == Sign.Zero })

  def apply[C: Ring : Signed](terms: Iterable[Term[C]]): Polynomial[C] =
    new Polynomial(terms.view.filterNot(_.isZero).map(_.toTuple).toMap)

  def apply[C: Ring : Signed](terms: Traversable[Term[C]]): Polynomial[C] =
    new Polynomial(terms.view.filterNot(_.isZero).map(_.toTuple).toMap)

  def apply[C: Ring](c: C, e: Int)(implicit s: Signed[C]): Polynomial[C] =
    new Polynomial(Map(e -> c).filterNot { case (e, c) => s.sign(c) == Sign.Zero})

  private val termRe = "([0-9]+\\.[0-9]+|[0-9]+/[0-9]+|[0-9]+)?(?:([a-z])(?:\\^([0-9]+))?)?".r
  private val operRe = " *([+-]) *".r

  def apply(s: String): Polynomial[Rational] = {

    // represents a term, plus a named variable v
    case class T(c: Rational, v: String, e: Int)

    // parse all the terms and operators out of the string
    @tailrec def parse(s: String, ts: List[T]): List[T] =
      if (s.isEmpty) {
        ts
      } else {
        val (op, s2) = operRe.findPrefixMatchOf(s) match {
          case Some(m) => (m.group(1), s.substring(m.end))
          case None => if (ts.isEmpty) ("+", s) else sys.error(s"parse error: $s")
        }

        val m2 = termRe.findPrefixMatchOf(s2).getOrElse(sys.error("parse error: $s2"))
        val c0 = Option(m2.group(1)).getOrElse("1")
        val c = if (op == "-") "-" + c0 else c0
        val v = Option(m2.group(2)).getOrElse("")
        val e0 = Option(m2.group(3)).getOrElse("")
        val e = if (e0 != "") e0 else if (v == "") "0" else "1"

        val t = try {
          T(Rational(c), v, e.toInt)
        } catch {
          case _: Exception => sys.error(s"parse error: $c $e")
        }
        parse(s2.substring(m2.end), if (t.c == 0) ts else t :: ts)
      }

    // do some pre-processing to remove whitespace/outer parens
    val t = s.trim
    val u = if (t.startsWith("(") && t.endsWith(")")) t.substring(1, t.length - 1) else t

    // parse out the terms
    val ts = parse(u, Nil)

    // make sure we have at most one variable
    val vs = ts.view.map(_.v).toSet.filter(_ != "")
    if (vs.size > 1) sys.error("only univariate polynomials supported")

    // we're done!
    Polynomial(ts.map(t => (t.e, t.c)).toMap)
  }

  implicit def pRD: PolynomialRing[Double] = new PolynomialRing[Double] {
    val r = Ring[Double]
    val s = Signed[Double]
    val f = Field[Double]
  }

  implicit def pRR: PolynomialRing[Rational] = new PolynomialRing[Rational] {
    val r = Ring[Rational]
    val s = Signed[Rational]
    val f = Field[Rational]
  }
}



// Univariate Polynomials Form a EuclideanRing
trait PolynomialRing[C] extends EuclideanRing[Polynomial[C]] {

  implicit def r: Ring[C]
  implicit def s: Signed[C]
  implicit def f: Field[C]

  implicit def tR: Ring[Term[C]] = new Ring[Term[C]] {
    def negate(t: Term[C]): Term[C] = Term(-t.coeff, t.exp)
    def zero: Term[C] = Term(r.zero, 0)
    def one: Term[C] = Term(r.one, 0)
    def plus(x: Term[C], y: Term[C]): Term[C] =
      Term(x.coeff + y.coeff, y.exp)
    def times(x: Term[C], y: Term[C]): Term[C] =
      Term(x.coeff * y.coeff, x.exp + y.exp)
  }

  def zero = Polynomial(Map(0 -> r.zero))

  def one = Polynomial(Map(0 -> r.one))

  def negate(x: Polynomial[C]): Polynomial[C] =
    Polynomial(x.data.map { case (e, c) => (e, -c) })

  def plus(x: Polynomial[C], y: Polynomial[C]): Polynomial[C] =
    Polynomial(x.data + y.data)

  def times(x: Polynomial[C], y: Polynomial[C]): Polynomial[C] =
    Polynomial(x.data.view.foldLeft(Map.empty[Int, C]) { case (m, (ex, cx)) =>
      y.data.foldLeft(m) { case (m, (ey, cy)) =>
        val e = ex + ey
        val c = cx * cy
        m.updated(e, m.get(e).map(_ + c).getOrElse(c))
      }
    })

  def quotMod(x: Polynomial[C], y: Polynomial[C]): (Polynomial[C], Polynomial[C]) = {
    require(!y.isZero, "Can't divide by polynomial of zero!")

    def zipSum(x: List[C], y: List[C]): List[C] = {
      val (s, l) = if(x.length > y.length) (y, x) else (x, y)
     (s.zip(l).map(z => z._1 + z._2) ++ l.drop(s.length)).tail
    }

    def polyFromCoeffsLE(cs: List[C]): Polynomial[C] =
      Polynomial(cs.zipWithIndex.map({ case (c, e) => Term(c, e) }))

    def polyFromCoeffsBE(cs: List[C]): Polynomial[C] = {
      val ncs = cs.dropWhile(s.sign(_) == Sign.Zero)
      Polynomial(((ncs.length - 1) to 0 by -1).zip(ncs).map(Term.fromTuple(_)))
    }
            
    @tailrec def eval(q: List[C], u: List[C], n: Int): (Polynomial[C], Polynomial[C]) = {
      lazy val v0 = if(y.isEmpty) r.zero else y.maxOrderTermCoeff
      lazy val q0 = u.head / v0
      lazy val uprime = zipSum(u, y.coeffs.map(_ * -q0))
      if (u.isEmpty || n < 0) (polyFromCoeffsLE(q), polyFromCoeffsBE(u)) 
        else eval(q0 :: q, uprime, n - 1)
    }

    val ym = y.maxTerm
    if (ym.exp == 0) {
      val q = Polynomial(x.data.map { case (e, c) => (e, c / ym.coeff) })
      val r = Polynomial(Map.empty[Int, C])
      (q, r)
    } else eval(Nil, x.coeffs, x.degree - y.degree)

  }

  def quot(x: Polynomial[C], y: Polynomial[C]): Polynomial[C] = quotMod(x, y)._1
    
  def mod(x: Polynomial[C], y: Polynomial[C]): Polynomial[C] = quotMod(x, y)._2

  @tailrec final def gcd(x: Polynomial[C], y: Polynomial[C]): Polynomial[C] =
    if (y.isZero && x.isZero) zero
    else if (y.maxTerm.isZero) x
    else gcd(y, mod(x, y))

}

