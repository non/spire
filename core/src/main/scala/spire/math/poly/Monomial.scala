package spire.math.poly

import compat._
import scala.annotation.tailrec
import scala.reflect._
import scala.{specialized => spec}
import spire.algebra._
import spire.implicits._
import spire.math._


/*  A monomial is the product of a coefficient and a list of variables (Char as symbol) 
    each to a non-negative integer power.
*/

class Monomial[@spec(Double) C: ClassTag: Order] private[spire] (val coeff: C, val vars: Map[Char, Int])
  (implicit r: Ring[C]) { lhs =>

  lazy val degree: Int = 
    vars.values.sum

  def isZero: Boolean =
    lhs == Monomial.zero[C]

  def eval(values: Map[Char, C]): C =
    coeff * vars.map({ case (k,v) => values.get(k).get ** v }).reduce(_ * _)

  def unary_- = 
    Monomial[C](-coeff, vars)

  def *:(x: C): Monomial[C] =
    Monomial[C](coeff * x, lhs.vars)

  def :*(k: C): Monomial[C] = 
    k *: lhs

  def :/(x: C)(implicit f: Field[C]): Monomial[C] =
    lhs.*:(x.reciprocal)

  def *(rhs: Monomial[C])(implicit eqm: Eq[Monomial[C]]): Monomial[C] = 
    if(lhs.isZero || rhs.isZero) Monomial.zero[C] else Monomial[C](lhs.coeff * rhs.coeff, lhs.vars + rhs.vars)

  // n.b. only monomials with the same variables form a ring or field
  // but it is like this as we do the arithmetic in MultivariatePolynomial.
  def +(rhs: Monomial[C]): Monomial[C] =
    Monomial[C](lhs.coeff + rhs.coeff, lhs.vars)

  def -(rhs: Monomial[C]): Monomial[C] =
    Monomial[C](lhs.coeff + -(rhs.coeff), lhs.vars)

  def /(rhs: Monomial[C])(implicit f: Field[C]): Monomial[C] =
    if(lhs == rhs) Monomial.one[C] else Monomial[C](lhs.coeff / rhs.coeff, lhs.vars - rhs.vars)

  def divides(rhs: Monomial[C])(implicit ordChar: Order[Char], ordInt: Order[Int]): Boolean = {
    if(lhs.degree == 0 && rhs.degree == 0 && lhs.coeff.abs < rhs.coeff.abs) true else if(lhs.degree == 0 && rhs.degree == 0) false else
      lhs.vars.view.zip(rhs.vars).forall(z => (z._1._2 <= z._2._2))
  }

  def gcd(rhs: Monomial[C])(implicit er: EuclideanRing[C]): Monomial[C] = {
    @tailrec def gcd_(z: Map[Char, Int], x: Map[Char, Int], y: Map[Char, Int]) : Monomial[C] = {
      if(x.isEmpty || y.isEmpty) Monomial[C](er.gcd(lhs.coeff, rhs.coeff), z) else x.head._1 compare y.head._1 match {
        case -1 => gcd_(z, x.tail, y)
        case 1 => gcd_(z, x, y.tail)
        case 0 => {
          val k: Int = min(x.head._2, y.head._2)
          gcd_(Map(x.head._1 -> k) ++ z, x.tail, y.tail)
        }
      } 
    }
    gcd_(Map[Char, Int](), lhs.vars, rhs.vars)
  }

  def lcm(rhs: Monomial[C])(implicit er: EuclideanRing[C]): Monomial[C] = {
    @tailrec def lcm_(z: Map[Char, Int], x: Map[Char, Int], y: Map[Char, Int]) : Monomial[C] = {
      if(x.isEmpty || y.isEmpty) Monomial[C](er.lcm(lhs.coeff, rhs.coeff), z) else x.head._1 compare y.head._1 match {
        case -1 => lcm_(z, x.tail, y)
        case 1 => lcm_(z, x, y.tail)
        case 0 => {
          val k: Int = max(x.head._2, y.head._2)
          lcm_(Map(x.head._1 -> k) ++ z, x.tail, y.tail)
        }
      } 
    }
    lcm_(Map[Char, Int](), lhs.vars, rhs.vars)
  }

  override def equals(that: Any): Boolean = that match {
    case rhs: Monomial[C] if lhs.coeff === rhs.coeff && lhs.vars == rhs.vars => true
    case _ => false
  }

  override def toString = {

    import Monomial._

    val varStr = vars.map(v => v._2 match {
        case 0 => ""
        case 1 => s"${v._1}"
        case e => s"${v._1}^$e"
      }).mkString

    def simpleCoeff: Option[String] = coeff match {
      case 0 => Some("")
      case 1 => if(vars.head._2 == 0) Some(s" + $coeff") else Some(s" + $varStr")
      case -1 => if(vars.head._2 == 0) Some(s" - ${coeff.toString.tail.mkString}") else Some(s" - $varStr")
      case _ => None
    }

    def stringCoeff: Option[String] = coeff.toString match {
      case IsZero() => Some("")
      case IsNegative(posPart) if vars.head._2 == 0 => Some(s" - $posPart")
      case IsNegative(posPart) => Some(s" - $posPart$varStr")
      case _ => None
    }

    if(vars.isEmpty) " + (0)" else simpleCoeff orElse stringCoeff getOrElse s" + $coeff$varStr"
  }

}


object Monomial {

  def apply[@spec(Double) C: ClassTag: Order: Ring](c: C, v: (Char, Int)*): Monomial[C] = 
    checkCreateMonomial(c, v.toArray)

  def apply[@spec(Double) C: ClassTag: Order: Ring](c: C, v: List[(Char, Int)]): Monomial[C] =
    checkCreateMonomial(c, v.toArray)

  def apply[@spec(Double) C: ClassTag: Order: Ring](c: C, v: Map[Char, Int]): Monomial[C] =
    checkCreateMonomial(c, v.toArray)

  def checkCreateMonomial[@spec(Double) C: ClassTag: Order](c: C, arr: Array[(Char, Int)])
    (implicit r: Ring[C]): Monomial[C] = c match {
      case n if n === r.zero => zero[C]
      case _ => {
        arr.length match {
          case 0 => zero[C]
          case 1 => {
            if(c === r.zero && arr(0)._2 == 0) zero[C] else if(arr(0)._2 == 0) xzero(c) else {
              new Monomial[C](c, arr.toMap)
            }
          }
          case _ => {
            QuickSort.sort(arr)(Order[(Char, Int)], implicitly[ClassTag[(Char, Int)]])
            if(arr.forall(_._2 == 0)) xzero(c) else new Monomial[C](c, arr.filterNot(_._2 == 0).toMap) 
          }
        }
      }
    }

  def zero[@spec(Double) C: ClassTag: Order](implicit r: Ring[C]): Monomial[C] =
    new Monomial[C](r.zero, Map[Char, Int]())
  
  def one[@spec(Double) C: ClassTag: Order](implicit r: Ring[C]): Monomial[C] = 
    new Monomial[C](r.one, Map('x' -> 0))

  def xzero[@spec(Double) C: ClassTag: Order](c: C)(implicit r: Ring[C]): Monomial[C] =
    new Monomial[C](c, Map('x' -> 0))

  def x[@spec(Double) C: ClassTag: Order](implicit r: Ring[C]): Monomial[C] = 
    new Monomial[C](r.one, Map('x' -> 1))

  private val IsZero = "0".r
  private val IsNegative = "-(.*)".r

  implicit def monomialOrd[@spec(Double) C: ClassTag: Order: Semiring] = new MonomialOrderingLex[C] {
    val ordCoeff = Order[C]
    val scalar = Semiring[C]
    val ct = implicitly[ClassTag[C]]
  }

}

// An equivalent monomial has the same variables (that's all!)
// not checking that the variable exponents are equal using this instance
// Lexicographic ordering
// e.g. x^2 > xy > xz > x > y^2 > yz > y > z^2 > z > 1
trait MonomialOrderingLex[@spec(Double) C] extends Order[Monomial[C]] {

  implicit def ordCoeff: Order[C]
  implicit val ordChar = Order[Char]
  implicit val ordInt = Order[Int]

  override def eqv(x: Monomial[C], y: Monomial[C]): Boolean =
    x.vars.toArray === y.vars.toArray 

  def compare(l: Monomial[C], r: Monomial[C]): Int = {
    @tailrec def compare_(x: Map[Char, Int], y: Map[Char, Int]): Int = {
      (x.isEmpty, y.isEmpty) match {
        case (true, true) => l.coeff compare r.coeff
        case (false, true) => -1
        case (true, false) => 1
        case _ => ordChar.compare(x.head._1, y.head._1) match {
          case -1 => -1
          case 1 => 1
          case 0 => ordInt.compare(x.head._2, y.head._2) match {
            case -1 => 1
            case 1 => -1
            case 0 => compare_(x.tail, y.tail)
          }
        }
      }
    }
    compare_(l.vars, r.vars)
  }
  
}

// Graded lexicographic ordering
// e.g. x^2 > xy > xz > y^2 > yz > z^2 > x > y > z > 1
trait MonomialOrderingGlex[@spec(Double) C] extends Order[Monomial[C]] {

  implicit def ordCoeff: Order[C]
  implicit val ordChar = Order[Char]
  implicit val ordInt = Order[Int]

  override def eqv(x: Monomial[C], y: Monomial[C]): Boolean =
    x.vars.toArray === y.vars.toArray

  def compare(l: Monomial[C], r: Monomial[C]): Int = {
    @tailrec def compare_(x: Map[Char, Int], y: Map[Char, Int]): Int = {
     (x.isEmpty, y.isEmpty) match {
        case (true, true) => l.coeff compare r.coeff
        case (false, true) => -1
        case (true, false) => 1
        case _ => ordInt.compare(x.values.sum, y.values.sum) match {
          case -1 => 1
          case 1 => -1
          case 0 => ordChar.compare(x.head._1, y.head._1) match {
            case -1 => -1
            case 1 => 1
            case 0 => ordInt.compare(x.head._2, y.head._2) match {
              case -1 => 1
              case 1 => -1
              case 0 => compare_(x.tail, y.tail)
            }
          }
        }  
      }
    }
    compare_(l.vars, r.vars)
  }
}

//Graded reverse lexicographic ordering
// e.g. x^2 > xy > y^2 > xz > yz > z^2 > x > y > z
trait MonomialOrderingGrevlex[@spec(Double) C] extends Order[Monomial[C]] {

  implicit def ordCoeff: Order[C]
  implicit val ordChar = Order[Char]
  implicit val ordInt = Order[Int]

  override def eqv(x: Monomial[C], y: Monomial[C]): Boolean =
    x.vars.toArray === y.vars.toArray

  def compare(l: Monomial[C], r: Monomial[C]): Int = {
    @tailrec def compare_(x: Map[Char, Int], y: Map[Char, Int]): Int = {
      (x.isEmpty, y.isEmpty) match {
        case (true, true) => l.coeff compare r.coeff
        case (false, true) => -1
        case (true, false) => 1
        case _ => ordInt.compare(x.values.sum, y.values.sum) match {
          case -1 => 1
          case 1 => -1
          case 0 => ordChar.compare(x.head._1, y.head._1) match {
            case -1 => -1
            case 1 => 1
            case 0 => compare_(x.tail, y.tail)
          }
        }
      }
    }
    compare_(l.vars.toArray.reverse.toMap, r.vars.toArray.reverse.toMap)
  }
}
