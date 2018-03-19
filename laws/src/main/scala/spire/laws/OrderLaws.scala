package spire
package laws

import spire.algebra._
import spire.implicits._

import org.typelevel.discipline.Laws

import org.scalacheck.{Arbitrary, Prop}
import org.scalacheck.Prop._

object OrderLaws {
  def apply[A : Eq : Arbitrary] = new OrderLaws[A] {
    def Equ = Eq[A]
    def Arb = implicitly[Arbitrary[A]]
  }
}

trait OrderLaws[A] extends Laws {

  implicit def Equ: Eq[A]
  implicit def Arb: Arbitrary[A]

  // copy of these laws in LimitedRangeLaws.scala

  def partialOrder(implicit A: PartialOrder[A]) = new OrderProperties(
    name = "partialOrder",
    parent = None,
    "reflexitivity" → forAll((x: A) =>
      x <= x
    ),
    "antisymmetry" → forAll((x: A, y: A) =>
      (x <= y && y <= x) imp (x === y)
    ),
    "transitivity" → forAll((x: A, y: A, z: A) =>
      (x <= y && y <= z) imp (x <= z)
    ),
    "gteqv" → forAll((x: A, y: A) =>
      (x <= y) === (y >= x)
    ),
    "lt" → forAll((x: A, y: A) =>
      (x < y) === (x <= y && x =!= y)
    ),
    "gt" → forAll((x: A, y: A) =>
      (x < y) === (y > x)
    )
  )

  def order(implicit A: Order[A]) = new OrderProperties(
    name = "order",
    parent = Some(partialOrder),
    "totality" → forAll((x: A, y: A) =>
      x <= y || y <= x
    )
  )

  def signed(implicit A: Signed[A]) = new OrderProperties(
    name = "signed",
    parent = Some(order),
    "abs non-negative" → forAll((x: A) =>
      x.abs.sign != Sign.Negative
    ),
    "signum returns -1/0/1" → forAll((x: A) =>
      x.signum.abs <= 1
    ),
    "signum is sign.toInt" → forAll((x: A) =>
      x.signum == x.sign.toInt
    )
  )

  def truncatedDivision(implicit cRingA: CRing[A], truncatedDivisionA: TruncatedDivision[A]) = new DefaultRuleSet(
    name = "truncatedDivision",
    parent = Some(signed),
    "division rule (t_/%)" → forAll { (x: A, y: A) =>
      y.isZero || {
        val (q, r) = x t_/% y
        x === y * q + r
      }
    },
    "division rule (f_/%)" → forAll { (x: A, y: A) =>
      y.isZero || {
        val (q, r) = x f_/% y
        x === y * q + r
      }
    },
    "quotient is integer (t_/~)" → forAll { (x: A, y: A) =>
      y.isZero || (x t_/~ y).toBigIntOpt.nonEmpty
    },
    "quotient is integer (f_/~)" → forAll { (x: A, y: A) =>
      y.isZero || (x f_/~ y).toBigIntOpt.nonEmpty
    },
    "|r| < |y| (t_%)" → forAll { (x: A, y: A) =>
      y.isZero || {
        val r = x t_% y
        r.abs < y.abs
      }
    },
    "|r| < |y| (f_%)" → forAll { (x: A, y: A) =>
      y.isZero || {
        val r = x f_% y
        r.abs < y.abs
      }
    },
    "r = 0 or sign(r) = sign(x) (t_%)" → forAll { (x: A, y: A) =>
      y.isZero || {
        val r = x t_% y
        r.isZero || (r.sign === x.sign)
      }
    },
    "r = 0 or sign(r) = sign(y) (f_%)" → forAll { (x: A, y: A) =>
      y.isZero || {
        val r = x f_% y
        r.isZero || (r.sign === y.sign)
      }
    },
    "t_/~" → forAll { (x: A, y: A) =>
      y.isZero || {
        (x t_/% y)._1 === (x t_/~ y)
      }
    },
    "t_%" → forAll { (x: A, y: A) =>
      y.isZero || {
        (x t_/% y)._2 === (x t_% y)
      }
    },
    "f_/~" → forAll { (x: A, y: A) =>
      y.isZero || {
        (x f_/% y)._1 === (x f_/~ y)
      }
    },
    "f_%" → forAll { (x: A, y: A) =>
      y.isZero || {
        (x f_/% y)._2 === (x f_% y)
      }
    }
  )

  class OrderProperties(
    name: String,
    parent: Option[OrderProperties],
    props: (String, Prop)*
  ) extends DefaultRuleSet(name, parent, props: _*)

}

// vim: expandtab:ts=2:sw=2
