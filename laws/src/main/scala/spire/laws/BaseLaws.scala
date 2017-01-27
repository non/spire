package spire
package laws

import spire.algebra._
import spire.implicits._

import org.typelevel.discipline.Laws

import org.scalacheck.Arbitrary
import org.scalacheck.Prop._

object BaseLaws {
  def apply[A : Eq : Arbitrary] = new BaseLaws[A] {
    def Equ = Eq[A]
    def Arb = implicitly[Arbitrary[A]]
  }
}

trait BaseLaws[A] extends Laws {

  implicit def Equ: Eq[A]
  implicit def Arb: Arbitrary[A]

  def metricSpace[R](implicit MSA: MetricSpace[A, R], SR: Signed[R], OR: Order[R], ASR: AdditiveSemigroup[R]) = new SimpleRuleSet(
    name = "metricSpace",
    "non-negative" → forAll((a1: A, a2: A) =>
      MSA.distance(a1, a2).sign != Sign.Negative
    ),
    "identity" → forAll((a: A) =>
      MSA.distance(a, a).sign == Sign.Zero
    ),
    "equality" → forAll((a1: A, a2: A) =>
      // generating equal values is hard, and Scalacheck will give up if it can't
      // hence, not using `==>` here
      a1 =!= a2 || MSA.distance(a1, a2).sign == Sign.Zero
    ),
    "symmetry" → forAll((a1: A, a2: A) =>
      MSA.distance(a1, a2) === MSA.distance(a2, a1)
    ),
    "triangleInequality" → forAll((a1: A, a2: A, a3: A) =>
      (MSA.distance(a1, a2) + MSA.distance(a2, a3)) >= MSA.distance(a1, a3)
    )
  )

    def uniqueFactorizationDomain(implicit A: UniqueFactorizationDomain[A], RA: CRing[A]) = new SimpleRuleSet(
    name = "uniqueFactorizationDomain",
      "all factors are prime" → forAll( (x: A) =>
        RA.isZero(x) || {
          val factorization = A.factor(x)
          factorization.elements.keys.forall(A.isPrime(_))
        }
      ),
      "multiplying factors returns the original element" → forAll( (x: A) =>
        RA.isZero(x) || {
          val factorization = A.factor(x)
          val prod = factorization.elements.map(f => RA.pow(f._1, f._2) ).foldLeft(RA.one)(RA.times)
          RA.times(factorization.unit, prod) === x
        }
      )
    )

}

// vim: expandtab:ts=2:sw=2
