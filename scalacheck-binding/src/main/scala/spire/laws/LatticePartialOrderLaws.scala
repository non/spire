package spire.laws

import spire.algebra._
import spire.algebra.lattice._
import spire.implicits._

import org.typelevel.discipline.Laws

import org.scalacheck.{Arbitrary, Prop}
import org.scalacheck.Prop._

object LatticePartialOrderLaws {
  def apply[A : Eq : Arbitrary] = new LatticePartialOrderLaws[A] {
    def Equ = Eq[A]
    def Arb = implicitly[Arbitrary[A]]
  }
}

trait LatticePartialOrderLaws[A] extends Laws {

  implicit def Equ: Eq[A]
  implicit def Arb: Arbitrary[A]

  def joinSemilatticePartialOrder(implicit A: JoinSemilattice[A] with PartialOrder[A]) = new LatticePartialOrderProperties(
    name = "joinSemilatticePartialOrder",
    parents = Seq.empty,
    bases = Seq("order" → OrderLaws[A].partialOrder, "lattice" → LatticeLaws[A].joinSemilattice),
    "join.lteqv" → forAll((x: A, y: A) =>
      (x <= y) === (y === (x join y))
    )
  )

  def meetSemilatticePartialOrder(implicit A: MeetSemilattice[A] with PartialOrder[A]) = new LatticePartialOrderProperties(
    name = "meetSemilatticePartialOrder",
    parents = Seq.empty,
    bases = Seq("order" → OrderLaws[A].partialOrder, "lattice" → LatticeLaws[A].meetSemilattice),
    "meet.lteqv" → forAll((x: A, y: A) =>
      (x <= y) === (x === (x meet y))
    )
  )

  def latticePartialOrder(implicit A: Lattice[A] with PartialOrder[A]) = new LatticePartialOrderProperties(
    name = "latticePartialOrder",
    parents = Seq(joinSemilatticePartialOrder, meetSemilatticePartialOrder),
    bases = Seq.empty
  )

  class LatticePartialOrderProperties(
    val name: String,
    val parents: Seq[LatticePartialOrderProperties],
    val bases: Seq[(String, Laws#RuleSet)],
    val props: (String, Prop)*
  ) extends RuleSet
}

// vim: expandtab:ts=2:sw=2