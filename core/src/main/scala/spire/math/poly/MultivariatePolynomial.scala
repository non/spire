package spire.math.poly

import compat._
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuilder
import scala.collection.mutable.Set
import scala.util.matching.Regex
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

  def eval(values: Map[Char, C])(implicit r: Ring[C]): C = {
    require(allVariables.forall(values.contains), "Can't evaluate polynomial without all the variable (symbol) values!")
    terms.map(_.eval(values)).reduce(_ + _)
  }

  def unary_-(implicit r: Rng[C]): MultivariatePolynomial[C] = 
    MultivariatePolynomial[C](terms.map(_.unary_-))

  def head(implicit r: Ring[C]): Monomial[C] =
    if(isZero) Monomial.zero[C] else terms.head

  def headCoefficient(implicit r: Ring[C]): C =
    head.coeff

  def tail(implicit r: Semiring[C]): MultivariatePolynomial[C] = 
    MultivariatePolynomial[C](terms.tail)

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
          val sumTerm = l(i) + r(j) 
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

  @tailrec private final def simplify(ts: Array[Monomial[C]], a: ArrayBuilder[Monomial[C]] = ArrayBuilder.make[Monomial[C]])
    (implicit r: Semiring[C]): Array[Monomial[C]] = ts.length match {
    case 0 => a.result()
    case 1 => a += ts(0); a.result()
    case _ => {
      val reduction = ts.filter(_ === ts(0)).reduce(_ + _)
      a += reduction
      simplify(ts.filterNot(_ === ts(0)), a)
    }
  }   

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
        } else if(!rhs.terms.forall(t => t.divides(dividend.head))) (quot, dividend) else quotMod_(quot, dividend, divisor.tail)
      }
    }

    if(lhs == rhs) (MultivariatePolynomial.one[C], MultivariatePolynomial.zero[C]) else if(rhs == MultivariatePolynomial.one[C]) {
      (lhs, MultivariatePolynomial.zero[C]) } else if(rhs == MultivariatePolynomial.zero[C]) { (lhs, MultivariatePolynomial.zero[C]) } 
        else if(!rhs.head.divides(lhs.head)) (MultivariatePolynomial.zero[C], lhs) else quotMod_(MultivariatePolynomial.zero[C], lhs, rhs)
  }

  // VectorSpace ops
  def *:(k: C)(implicit r: Semiring[C]): MultivariatePolynomial[C] = 
    if(k === r.zero) MultivariatePolynomial.zero[C] else MultivariatePolynomial[C](terms.map(_.*:(k)))

  def :*(k: C)(implicit r: Semiring[C]): MultivariatePolynomial[C] = k *: lhs

  def :/ (k: C)(implicit f: Field[C]): MultivariatePolynomial[C] = lhs.*:(k.reciprocal)

  override def equals(that: Any): Boolean = that match {
    case rhs: MultivariatePolynomial[_] if lhs.degree == rhs.degree && lhs.numTerms == rhs.numTerms =>
      lhs.terms.view.zip(rhs.terms.asInstanceOf[Array[Monomial[C]]]).map(z => z._1 compare z._2).forall(_ == 0)

    case rhs: MultivariatePolynomial[_] => if(lhs.isEmpty && rhs.isEmpty) true else false
    case _ => false
  }

  override def toString =
    if (isEmpty) {
      "(0)"
    } else {
      val s = terms.mkString
      "(" + (if (s.take(3) == " - ") "-" + s.drop(3) else s.drop(3)) + ")"
    }

}


object MultivariatePolynomial {

  implicit def lexOrdering[@spec(Double) C: ClassTag: Semiring: Order] = new MonomialOrderingLex[C] {
    val ordCoeff = Order[C]
    val scalar = Semiring[C]
    val ct = implicitly[ClassTag[C]]
  }
  
  def apply[@spec(Double) C: ClassTag: Semiring: Order](terms: Monomial[C]*): MultivariatePolynomial[C] = {
    val ts = terms.filterNot(t => t.isZero).toArray
    ts.qsort
    new MultivariatePolynomial[C](ts)
  }

  def apply[@spec(Double) C: ClassTag: Semiring: Order](terms: Traversable[Monomial[C]]): MultivariatePolynomial[C] = {
    val ts = terms.filterNot(t => t.isZero).toArray
    ts.qsort
    new MultivariatePolynomial[C](ts)
  }

  def apply(s: String)(implicit r: Ring[Rational], o: Order[Rational], ct: ClassTag[Rational]): MultivariatePolynomial[Rational] = 
    parse(s)

  def zero[@spec(Double) C: ClassTag: Order](implicit r: Semiring[C]) = 
    new MultivariatePolynomial[C](new Array[Monomial[C]](0))

  def one[@spec(Double) C: ClassTag: Order](implicit r: Ring[C]) = 
    new MultivariatePolynomial[C](Array(Monomial.one[C]))

  val operRe = " *([+-]) *".r
  val termRe = "(\\d+\\.\\d+|\\d+/\\d+|\\d+)?((?:\\w\\^\\d+|\\w)+)?".r
  val varRe = "(\\w)\\^(\\d+)|(\\w)".r

  def parse(s: String)(implicit r: Ring[Rational], o: Order[Rational], ct: ClassTag[Rational]): MultivariatePolynomial[Rational] = {
    def parse(s: String, ts: List[Monomial[Rational]]): List[Monomial[Rational]] =
      if (s.isEmpty) {
        ts
      } else {
        val (op, s2) = operRe.findPrefixMatchOf(s) match {
          case Some(m) => (m.group(1), s.substring(m.end))
          case None => if (ts.isEmpty) ("+", s) else sys.error(s"parse error: $s")
        }
        val termMatch = termRe.findPrefixMatchOf(s2).getOrElse(sys.error("parse error: $s2"))
        val termsSubgroup = termMatch.subgroups
        val c0 = Option(termsSubgroup.head).getOrElse("1")
        val c = if (op == "-") s"-$c0" else c0
        val vstr = Option(termsSubgroup(1)).getOrElse("")
        var t = if(vstr != "") {
          val vlist = varRe.findAllMatchIn(vstr).toList.map(v => {
            val vsub = v.subgroups
            if(vsub.head != null) (vsub.head.charAt(0), vsub(1).toInt) else (vsub.last.charAt(0), 1)
          })
          Monomial(Rational(c), vlist)
        } else Monomial(Rational(c), ('x', 0))
        parse(s2.substring(termMatch.end), if (c == 0) ts else t :: ts)
    }
    val t = s.trim
    val u = if (t.startsWith("(") && t.endsWith(")")) t.substring(1, t.length - 1) else t
    val ts = parse(u, Nil)
    MultivariatePolynomial(ts)
  }

}

