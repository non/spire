package spire
package algebra

/**
 * A simple ADT representing the `Sign` of an object.
 */
sealed abstract class Sign(val toInt: Int) {
  import Sign._

  def unary_-(): Sign = this match {
    case Positive => Negative
    case Negative => Positive
    case Zero => Zero
  }

  def *(that: Sign): Sign = Sign(this.toInt * that.toInt)

  def **(that: Int): Sign = Sign(spire.math.pow(this.toInt, that).toInt)
}

object Sign {
  case object Zero extends Sign(0)
  case object Positive extends Sign(1)
  case object Negative extends Sign(-1)

  implicit def sign2int(s: Sign): Int = s.toInt
  implicit def apply(i: Int): Sign =
    if (i == 0) Zero else if (i > 0) Positive else Negative

  class SignAlgebra extends CMonoid[Sign] with Signed[Sign] {
    def empty: Sign = Positive
    def combine(a: Sign, b: Sign): Sign = a * b
    // the Signum instance is slightly abusive, as Sign doesn't provide an additive monoid
    override def sign(a: Sign): Sign = a
    def signum(a: Sign): Int = a.toInt
    def abs(a: Sign) = if (a == Negative) Positive else a
    def compare(x: Sign, y: Sign): Int = java.lang.Integer.signum(x.toInt - y.toInt)
  }

  implicit final val SignAlgebra = new SignAlgebra

  implicit final val SignMultiplicativeGroup: MultiplicativeCMonoid[Sign] =
    Multiplicative(SignAlgebra)
}
