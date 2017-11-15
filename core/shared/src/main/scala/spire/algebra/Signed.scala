package spire
package algebra

/**
  * A trait for linearly ordered additive commutative monoid. The following laws holds:
  *
  * (1) if `a <= b` then `a + c <= b + c` (linear order),
  * (2) `signum(x) = -1` if `x < 0`, `signum(x) = 1` if `x > 0`, `signum(x) = 0` otherwise,
  *
  * Negative elements only appear when `scalar` is a additive abelian group, and then
  * (3) `abs(x) = -x` if `x < 0`, or `x` otherwise,
  *
  * Laws (1) and (2) lead to the triange inequality:
  *
  * (4) `abs(a + b) <= abs(a) + abs(b)`
  *
  * Signed should never be extended in implementations, rather the
  * Signed.OnAdditiveCMonoid and Singed.OnAdditiveAbGroup subtraits.
  * We cannot use self-types to express the constraint `self: AdditiveCMonoid =>` (interaction with specialization?).
  */
trait Signed[@sp(Byte, Short, Int, Long, Float, Double) A] extends Any with Order[A] {
  /** Returns Zero if `a` is 0, Positive if `a` is positive, and Negative is `a` is negative. */
  def sign(a: A): Sign = Sign(signum(a))

  /** Returns 0 if `a` is 0, 1 if `a` is positive, and -1 is `a` is negative. */
  def signum(a: A): Int

  /** An idempotent function that ensures an object has a non-negative sign. */
  def abs(a: A): A

  def isSignZero(a: A): Boolean = signum(a) == 0
  def isSignPositive(a: A): Boolean = signum(a) > 0
  def isSignNegative(a: A): Boolean = signum(a) < 0

  def isSignNonZero(a: A): Boolean = signum(a) != 0
  def isSignNonPositive(a: A): Boolean = signum(a) <= 0
  def isSignNonNegative(a: A): Boolean = signum(a) >= 0
}

object Signed {

  trait OnAdditiveCMonoid[@sp(Byte, Short, Int, Long, Float, Double) A] extends Any with Signed[A] {

    def additiveStructure: AdditiveCMonoid[A]

    /** Returns 0 if `a` is 0, 1 if `a` is positive, and -1 is `a` is negative. */
    def signum(a: A): Int = {
      val c = compare(a, additiveStructure.zero)
      if (c < 0) -1
      else if (c > 0) 1
      else 0
    }
  }

  trait OnAdditiveAbGroup[@sp(Byte, Short, Int, Long, Float, Double) A] extends Any with OnAdditiveCMonoid[A] {

    def additiveStructure: AdditiveAbGroup[A]

    def abs(a: A): A = if (compare(a, additiveStructure.zero) < 0) additiveStructure.negate(a) else a
  }

  def apply[A](implicit s: Signed[A]): Signed[A] = s

}
