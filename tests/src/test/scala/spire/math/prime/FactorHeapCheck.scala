package spire
package math.prime

import spire.math.SafeLong

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import spire.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

class FactorHeapCheck extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  import SieveUtil._

  property("heap property") {
    forAll { (ns0: List[Int]) =>
      val ns = ns0.map(n => Factor(n, n))
      val sorted = ns.sorted.map(_.next)

      val h = new FactorHeap
      ns.foreach(h += _)
      h.size shouldBe ns.length

      var result = List.empty[SafeLong]
      while (h.nonEmpty) result = h.dequeue.next :: result

      result shouldBe sorted
    }
  }
}
