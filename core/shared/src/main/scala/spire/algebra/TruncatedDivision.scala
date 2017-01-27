package spire
package algebra

/**
 * Division and modulus for computer scientists
 * taken from https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/divmodnote-letter.pdf
 * 
 * For two numbers x (dividend) and y (divisor) on an ordered ring with y != 0,
 * there exists a pair of numbers q (quotient) and r (remainder)
 * such that these laws are satisfied:
 * 
 * (1) q is an integer
 * (2) x = y * q + r (division rule)
 * (3) |r| < |y|,
 * (4t) r = 0 or sign(r) = sign(x),
 * (4f) r = 0 or sign(r) = sign(y).
 * 
 * where sign is the sign function, and the absolute value 
 * function |x| is defined as |x| = x if x >=0, and |x| = -x otherwise.
 * 
 * We define functions tmod and tquot such that:
 * q = tquot(x, y) and r = tmod(x, y) obey rule (4t)
 * and functions fmod and fquot such that:
 * q = fquot(x, y) and r = fmod(x, y) obey rule (4f)
 * 
 * Law (4t) corresponds to ISO C99 and Haskell's quot/rem.
 * Law (4f) is described by Knuth and used by Haskell,
 * and fmod corresponds to the REM function of the IEEE floating-point standard.
 */
trait TruncatedDivision[@sp(Byte, Short, Int, Long, Float, Double) A] extends Any with Signed[A] {
  /** Returns the integer `a` such that `x = a * one`, if it exists. */
  def toBigIntOption(x: A): Option[BigInt]

  def tquot(x: A, y: A): A
  def tmod(x: A, y: A): A
  def tquotmod(x: A, y: A): (A, A) = (tquot(x, y), tmod(x, y))

  def fquot(x: A, y: A): A
  def fmod(x: A, y: A): A
  def fquotmod(x: A, y: A): (A, A)
}

trait TruncatedDivisionCRing[@sp(Byte, Short, Int, Long, Float, Double) A] extends Any with TruncatedDivision[A] with CRing[A] { self =>

  def fmod(x: A, y: A): A = {
    val tm = tmod(x, y)
    if (signum(tm) == -signum(y)) plus(tm, y) else tm
  }

  def fquot(x: A, y: A): A = {
    val (tq, tm) = tquotmod(x, y)
    if (signum(tm) == -signum(y)) minus(tq, one) else tq
  }

  def fquotmod(x: A, y: A): (A, A) = {
    val (tq, tm) = tquotmod(x, y)
    TruncatedDivision.fquotmodFromTquotmod(x, y, tq, tm)(self, self)
  }

}

object TruncatedDivision {

  def fquotmodFromTquotmod[A:CRing:Signed](x: A, y: A, tquot: A, tmod: A): (A, A) = {
    val signsDiffer = (Signed[A].signum(tmod) == -Signed[A].signum(y))
    val fquot = if (signsDiffer) CRing[A].minus(tquot, CRing[A].one) else tquot
    val fmod = if (signsDiffer) CRing[A].plus(tmod, y) else tmod
    (fquot, fmod)
  }

  def apply[A](implicit ev: TruncatedDivision[A]): TruncatedDivision[A] = ev

}
