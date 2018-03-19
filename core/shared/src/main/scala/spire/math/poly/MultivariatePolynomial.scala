package spire.math.poly

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuilder
import scala.collection.mutable.Set
import scala.reflect._
import scala.{specialized => spec}
import spire.algebra._
import spire.implicits._
import spire.math._


class MultivariatePolynomial[@spec(Double) C: Order] private[spire] (val terms: Array[Monomial[C]])
  (implicit val ct: ClassTag[C], ordm: Order[Monomial[C]]) { lhs =>

  def isZero(implicit r: Semiring[C], eq: Eq[C]): Boolean =
    terms.forall(_.isZero)

  def isEmpty: Boolean =
    terms.isEmpty

  def degree: Int =
    if(isEmpty) 0 else terms.map(_.degree).max

  def numTerms: Int =
    terms.length

  def allVariables: Array[Char] =
    terms.flatMap(t => t.vars.keys).distinct

  def allTerms: Array[Monomial[C]] = terms

  def eval(values: Map[Char, C])(implicit r: Ring[C]): C = {
    require(allVariables.forall(values.contains), "Can't evaluate polynomial without all the variable (symbol) values!")
    terms.map(_.eval(values)).reduce(_ + _)
  }

  def evalPartial(values: Map[Char, C])(implicit f: Field[C]): MultivariatePolynomial[C] =
    MultivariatePolynomial(terms.map(_.evalPartial(values)))

  def unary_-(implicit r: Rng[C]): MultivariatePolynomial[C] =
    MultivariatePolynomial[C](terms.map(_.unary_-))

  def head(implicit r: Ring[C]): Monomial[C] =
    if(isZero) Monomial.zero[C] else allTerms.head

  def headCoefficient(implicit r: Ring[C]): C =
    head.coeff

  def tail(implicit r: Semiring[C]): MultivariatePolynomial[C] =
    MultivariatePolynomial[C](allTerms.tail)

  def monic(implicit f: Field[C]): MultivariatePolynomial[C] =
    if(isZero) MultivariatePolynomial.zero[C] else
      MultivariatePolynomial[C](terms.map(_.:/(headCoefficient)))

  private final def sum(l: Array[Monomial[C]], r: Array[Monomial[C]])(implicit ring: Semiring[C]) = {
    val a = ArrayBuilder.make[Monomial[C]]()
    val ar = new Array[Boolean](r.length)
    cfor(0)(_ < ar.length, _ + 1) { x => ar(x) = false }
    cfor(0)(_ < l.length, _ + 1) { i =>
      var added = false
      cfor(0)(_ < r.length, _ + 1) { j =>
        if(l(i) === r(j)) {
          val sumTerm = l(i) add r(j)
          a += sumTerm
          added = true
          ar(j) = true
        }
      }
      if(!added) a += l(i)
    }
    cfor(0)(_ < ar.length, _ + 1) { x =>
      if(!ar(x)) a += r(x)
    }
    a.result()
  }

  private [poly] final def simplify(ts: Array[Monomial[C]])
    (implicit r: Semiring[C]): Array[Monomial[C]] =
    MultivariatePolynomial.simplify(ts)

  // EuclideanRing ops
  def +(rhs: MultivariatePolynomial[C])(implicit r: Semiring[C]): MultivariatePolynomial[C] =
    MultivariatePolynomial[C](sum(lhs.terms, rhs.terms))

  def -(rhs: MultivariatePolynomial[C])(implicit r: Rng[C]): MultivariatePolynomial[C] =
    lhs + (-rhs)

  def *(rhs: MultivariatePolynomial[C])(implicit r: Ring[C]): MultivariatePolynomial[C] =
    if(rhs == MultivariatePolynomial.one[C]) lhs else if(rhs == MultivariatePolynomial.zero[C]) MultivariatePolynomial.zero[C]
      else MultivariatePolynomial[C](simplify(lhs.terms.flatMap(lt => rhs.terms.map(rt => lt * rt))))

  def /~(rhs: MultivariatePolynomial[C])(implicit f: Field[C], ct: ClassTag[C]): MultivariatePolynomial[C] =
    lhs./%(rhs)._1

  def %(rhs: MultivariatePolynomial[C])(implicit f: Field[C], ct: ClassTag[C]): MultivariatePolynomial[C] =
    lhs./%(rhs)._2

  def /%(rhs: MultivariatePolynomial[C])(implicit f: Field[C], ct: ClassTag[C]) = {

    @tailrec def quotMod_(quot: MultivariatePolynomial[C],
                          dividend: MultivariatePolynomial[C],
                          divisor: MultivariatePolynomial[C]): (MultivariatePolynomial[C], MultivariatePolynomial[C]) = {
      if(divisor.isEmpty || dividend.isEmpty) (quot, dividend) else {
        if(divisor.head.divides(dividend.head)) {
          val divTerm = MultivariatePolynomial[C](dividend.head / divisor.head)
          val prod = divisor * divTerm
          val quotSum = quot + divTerm
          val rem = dividend - prod
          if(rem.isZero) (quotSum, rem) else quotMod_(quotSum, rem, divisor)
        } else if(!rhs.allTerms.forall(t => t.divides(dividend.head))) (quot, dividend) else quotMod_(quot, dividend, divisor.tail)
      }
    }

    if (lhs == rhs) {
      (MultivariatePolynomial.one[C], MultivariatePolynomial.zero[C])
    } else if (rhs == MultivariatePolynomial.one[C]) {
      (lhs, MultivariatePolynomial.zero[C])
    } else if (rhs == MultivariatePolynomial.zero[C]) {
      (lhs, MultivariatePolynomial.zero[C])
    } else if (!rhs.head.divides(lhs.head)) {
      (MultivariatePolynomial.zero[C], lhs)
    } else {
      quotMod_(MultivariatePolynomial.zero[C], lhs, rhs)
    }
  }

  // VectorSpace ops
  def *:(k: C)(implicit r: Semiring[C]): MultivariatePolynomial[C] =
    if(k === r.zero) MultivariatePolynomial.zero[C] else MultivariatePolynomial[C](terms.map(_.*:(k)))

  def :*(k: C)(implicit r: Semiring[C]): MultivariatePolynomial[C] = k *: lhs

  def :/ (k: C)(implicit f: Field[C]): MultivariatePolynomial[C] = lhs.*:(k.reciprocal)

  override def equals(that: Any): Boolean = that match {
    case rhs: MultivariatePolynomial[_] if lhs.degree == rhs.degree && lhs.numTerms == rhs.numTerms =>
      lhs.allTerms.view.zip(rhs.allTerms.asInstanceOf[Array[Monomial[C]]]).map(z => z._1 compare z._2).forall(_ == 0)

    case rhs: MultivariatePolynomial[_] => if(lhs.isEmpty && rhs.isEmpty) true else false
    case _ => false
  }


  override def toString =
    if (isEmpty) "0"
    else {
      QuickSort.sort(terms)(ordm, implicitly[ClassTag[Monomial[C]]])
      val s = terms.mkString(" + ")
      s.replaceAll("\\+ -", "- ")
    }
}


object MultivariatePolynomial {

  implicit def lexOrdering[@spec(Double) C: ClassTag: Semiring: Order] = new MonomialOrderingLex[C] {
    val ordCoeff = Order[C]
    val scalar = Semiring[C]
    val ct = implicitly[ClassTag[C]]
  }

  implicit def monomialToMultivariatePolynomial[@spec(Double) C: ClassTag: Semiring: Order](term: Monomial[C]): MultivariatePolynomial[C] = apply(term)

  def apply[@spec(Double) C: ClassTag: Semiring: Order](terms: Monomial[C]*): MultivariatePolynomial[C] =
    new MultivariatePolynomial[C](simplify(terms.filterNot(t => t.isZero).toArray))

  def apply[@spec(Double) C: ClassTag: Semiring: Order](terms: Traversable[Monomial[C]]): MultivariatePolynomial[C] =
    new MultivariatePolynomial[C](simplify(terms.filterNot(t => t.isZero).toArray))

  def apply(str: String): MultivariatePolynomial[Rational] =
    parseFractional[Rational](str)

  def zero[@spec(Double) C: ClassTag: Order](implicit r: Semiring[C]) =
    new MultivariatePolynomial[C](new Array[Monomial[C]](0))

  def one[@spec(Double) C: ClassTag: Order](implicit r: Ring[C]) =
    new MultivariatePolynomial[C](Array(Monomial.one[C]))

  private [poly] final def simplify[@spec(Double) C: Order: ClassTag](ts: Array[Monomial[C]])
    (implicit r: Semiring[C]): Array[Monomial[C]] = {

    @tailrec
    def go(ts: Array[Monomial[C]], a: ArrayBuilder[Monomial[C]]): Array[Monomial[C]] = ts.length match {
      case 0 => a.result()
      case 1 => a += ts(0); a.result()
      case _ => {
        val reduction = ts.filter(_.vars === ts(0).vars).reduce(_ add _)
        a += reduction
        go(ts.filterNot(_.vars === ts(0).vars), a)
      }
    }
    val a: ArrayBuilder[Monomial[C]] = ArrayBuilder.make[Monomial[C]]
    go(ts, a).qsorted
  }

  final def parseFractional[@spec(Double) C: ClassTag: Fractional](str: String): MultivariatePolynomial[C] = {
    val terms = Monomial.Re.termFractional

    val monomials: Array[Monomial[C]] =
      terms.findAllIn(str).toArray.filter(_ != "").map(Monomial.parseFractional[C](_))
    new MultivariatePolynomial[C](simplify(monomials))
  }

  final def parseIntegral[@spec(Int, Long) C: Integral: ClassTag](str: String): MultivariatePolynomial[C] = {
    val terms = Monomial.Re.termIntegral

    val monomials: Array[Monomial[C]] =
      terms.findAllIn(str).toArray.filter(_ != "").map(Monomial.parseIntegral[C](_))
    new MultivariatePolynomial[C](simplify(monomials))
  }
}

