package spire.algebra

import spire.math._
import spire.macrosk.Ops

import java.lang.{Math => mth}
import scala.{ specialized => spec }


/**
 * A trait for things that have some notion of sign and the ability to ensure
 * something has a positive sign.
 */
trait Signed[@spec(Double, Float, Int, Long) A] {
  def sign(a: A): Sign = Sign(signum(a))
  def signum(a: A): Int
  def abs(a: A): A
}

trait SignedLow {
  implicit def orderedRingIsSigned[A:Order:Ring]: Signed[A] = new OrderedRingIsSigned[A]
}

object Signed extends SignedLow {
  implicit object ByteIsSigned extends ByteIsSigned
  implicit object ShortIsSigned extends ShortIsSigned
  implicit object IntIsSigned extends IntIsSigned
  implicit object LongIsSigned extends LongIsSigned
  implicit object BigIntIsSigned extends BigIntIsSigned
  implicit object FloatIsSigned extends FloatIsSigned
  implicit object DoubleIsSigned extends DoubleIsSigned
  implicit object BigDecimalIsSigned extends BigDecimalIsSigned
  implicit object RationalIsSigned extends RationalIsSigned
  implicit object RealIsSigned extends RealIsSigned
  implicit object SafeLongIsSigned extends SafeLongIsSigned
  implicit object NumberIsSigned extends NumberIsSigned

  implicit def complexIsSigned[A: Fractional: Trig] = new ComplexIsSigned[A] {
    val f = Fractional[A]
    val t = Trig[A]
  }
    
  implicit def gaussianIsSigned[A: Integral: NRoot] = new GaussianIsSigned[A] {
    val f = Integral[A]
    val n = NRoot[A]
  }

  def apply[A](implicit s: Signed[A]): Signed[A] = s
}

final class SignedOps[A:Signed](lhs: A) {
  def abs(): A = macro Ops.unop[A]
  def sign(): Sign = macro Ops.unop[Sign]
  def signum(): Int = macro Ops.unop[Int]
}

class OrderedRingIsSigned[A](implicit o:Order[A], r:Ring[A]) extends Signed[A] {
  def signum(a:A) = o.compare(a, r.zero)
  def abs(a:A) = if (signum(a) < 0) r.negate(a) else a
}

trait ByteIsSigned extends Signed[Byte] {
  def signum(a: Byte): Int = a
  def abs(a: Byte): Byte = (if (a < 0) -a else a).toByte
}

trait ShortIsSigned extends Signed[Short] {
  def signum(a: Short): Int = a
  def abs(a: Short): Short = (if (a < 0) -a else a).toShort
}

trait IntIsSigned extends Signed[Int] {
  def signum(a: Int): Int = a
  def abs(a: Int): Int = if (a < 0) -a else a
}

trait LongIsSigned extends Signed[Long] {
  def signum(a: Long): Int = a.toInt
  def abs(a: Long): Long = if (a < 0L) -a else a
}

trait BigIntIsSigned extends Signed[BigInt] {
  def signum(a: BigInt): Int = a.toInt
  def abs(a: BigInt): BigInt = a.abs
}

trait FloatIsSigned extends Signed[Float] {
  def signum(a: Float): Int = mth.signum(a).toInt
  def abs(a: Float): Float = if (a < 0.0f) -a else a
}

trait DoubleIsSigned extends Signed[Double] {
  def signum(a: Double): Int = mth.signum(a).toInt
  def abs(a: Double): Double = if (a < 0.0) -a else a
}

trait BigDecimalIsSigned extends Signed[BigDecimal] {
  def signum(a: BigDecimal): Int = a.signum
  def abs(a: BigDecimal): BigDecimal = a.abs
}

trait RationalIsSigned extends Signed[Rational] {
  override def sign(a: Rational): Sign = a.sign
  def signum(a: Rational): Int = a.signum
  def abs(a: Rational): Rational = a.abs
}

trait RealIsSigned extends Signed[Real] {
  override def sign(a: Real): Sign = a.sign
  def signum(a: Real): Int = a.signum
  def abs(a: Real): Real = a.abs
}

trait SafeLongIsSigned extends Signed[SafeLong] {
  def signum(a: SafeLong): Int = a.toBigInt.toInt
  def abs(a: SafeLong): SafeLong = a.abs
}

trait NumberIsSigned extends Signed[Number] {
  def signum(a: Number): Int = a.signum
  def abs(a: Number): Number = a.abs
}

trait ComplexIsSigned[A] extends Signed[Complex[A]] {
  implicit def f:Fractional[A]
  implicit def t:Trig[A]

  def signum(a: Complex[A]): Int = a.signum
  def abs(a: Complex[A]): Complex[A] = Complex[A](a.abs, f.zero)
}

trait GaussianIsSigned[A] extends Signed[Gaussian[A]] {
  implicit def f: Integral[A]
  implicit def n: NRoot[A]
  def signum(a: Gaussian[A]): Int = a.signum
  def abs(a: Gaussian[A]): Gaussian[A] = Gaussian[A](n.sqrt(a.norm), f.zero)
}
