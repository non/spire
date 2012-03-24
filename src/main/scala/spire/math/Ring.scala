package spire.math

import annotation.tailrec
import scala.{specialized => spec}
import scala.math.{abs, ceil, floor, pow => mpow}


trait Ring[@spec(Int,Long,Float,Double) A] extends Eq[A] { self =>
  def abs(a:A):A
  def minus(a:A, b:A):A
  def negate(a:A):A
  def one:A
  def plus(a:A, b:A):A
  def pow(a:A, n:Int):A
  def sign(a: A): Sign = Sign(self.signum(a))
  def signum(a: A): Int
  def times(a:A, b:A):A
  def zero:A

  // TODO: Implement log n version.
  @tailrec private def fromInt(n: Int, a: A): A = {
    if (n > 0) {
      fromInt(n - 1, plus(a, one))
    } else if (n < 0) {
      fromInt(n + 1, plus(a, negate(one)))
    } else {
      a
    }
  }


  /**
   * This constructs an `A` from an integer which is equivalent to adding
   * together `n` `one`'s. If `n` is negative, then it is equivalent to adding
   * together the additive inverse of `one` `n` times.
   *
   * The default implementation is very inefficient, performing `n` adds. Most
   * likely you will (and should) override the default implementation and
   * optimize it.
   */
  def fromInt(n: Int): A = fromInt(n, zero)

  def additive = new AdditiveMonoid[A]()(this)
  def multiplicative = new MultiplicativeMonoid[A]()(this)
}

final class RingOps[@spec(Int,Long,Float,Double) A](lhs:A)(implicit ev:Ring[A]) {
  def abs = ev.abs(lhs)
  def unary_- = ev.negate(lhs)

  def -(rhs:A) = ev.minus(lhs, rhs)
  def +(rhs:A) = ev.plus(lhs, rhs)
  def *(rhs:A) = ev.times(lhs, rhs)

  def pow(rhs:Int) = ev.pow(lhs, rhs)
  def **(rhs:Int) = ev.pow(lhs, rhs)
  def ~^(rhs:Int) = ev.pow(lhs, rhs)
  
  def sign: Sign = ev.sign(lhs)
  def signum: Int = ev.signum(lhs)
}

object Ring {
  implicit object IntIsRing extends IntIsRing
  implicit object LongIsRing extends LongIsRing
  implicit object FloatIsRing extends FloatIsRing
  implicit object DoubleIsRing extends DoubleIsRing
  implicit object BigIntIsRing extends BigIntIsRing
  implicit object BigDecimalIsRing extends BigDecimalIsRing
  implicit object RationalIsRing extends RationalIsRing
  implicit def complexIsRing[A:FractionalWithNRoot] = new ComplexIsRingCls
  implicit object RealIsRing extends RealIsRing

  def apply[A](implicit r:Ring[A]):Ring[A] = r
}

trait IntIsRing extends Ring[Int] with IntEq {
  def abs(a:Int): Int = scala.math.abs(a)
  def minus(a:Int, b:Int): Int = a - b
  def negate(a:Int): Int = -a
  def one: Int = 1
  def plus(a:Int, b:Int): Int = a + b
  def pow(a:Int, b:Int): Int = mpow(a, b).toInt
  def signum(a: Int): Int = a.signum
  def times(a:Int, b:Int): Int = a * b
  def zero: Int = 0

  override def fromInt(n: Int): Int = n
}

trait LongIsRing extends Ring[Long] with LongEq {
  def abs(a:Long): Long = scala.math.abs(a)
  def minus(a:Long, b:Long): Long = a - b
  def negate(a:Long): Long = -a
  def one: Long = 1L
  def plus(a:Long, b:Long): Long = a + b
  def pow(a: Long, b:Int): Long = b match {
    case 0 => 1
    case 1 => a
    case 2 => a * a
    case 3 => a * a * a
    case _ =>
      if (b > 0) {
        val e = b >> 1
        val c = if ((b & 1) == 1) a else 1
        c * pow(a, e) * pow(a, e)
      } else {
        0
      }
  }
  def signum(a: Long): Int = a.signum
  def times(a:Long, b:Long): Long = a * b
  def zero: Long = 0L
  
  override def fromInt(n: Int): Long = n
}

trait FloatIsRing extends Ring[Float] with FloatEq {
  def abs(a:Float): Float = scala.math.abs(a)
  def minus(a:Float, b:Float): Float = a - b
  def negate(a:Float): Float = -a
  def one: Float = 1.0F
  def plus(a:Float, b:Float): Float = a + b
  def pow(a:Float, b:Int): Float = mpow(a, b).toFloat
  def signum(a: Float): Int = a.signum
  def times(a:Float, b:Float): Float = a * b
  def zero: Float = 0.0F
  
  override def fromInt(n: Int): Float = n
}

trait DoubleIsRing extends Ring[Double] with DoubleEq {
  def abs(a:Double): Double = scala.math.abs(a)
  def minus(a:Double, b:Double): Double = a - b
  def negate(a:Double): Double = -a
  def one: Double = 1.0
  def plus(a:Double, b:Double): Double = a + b
  def pow(a:Double, b:Int): Double = mpow(a, b)
  def signum(a: Double): Int = a.signum
  def times(a:Double, b:Double): Double = a * b
  def zero: Double = 0.0

  override def fromInt(n: Int): Double = n
}

trait BigIntIsRing extends Ring[BigInt] with BigIntEq {
  def abs(a:BigInt): BigInt = a.abs
  def minus(a:BigInt, b:BigInt): BigInt = a - b
  def negate(a:BigInt): BigInt = -a
  def one: BigInt = BigInt(1)
  def plus(a:BigInt, b:BigInt): BigInt = a + b
  def pow(a:BigInt, b:Int): BigInt = a pow b
  def signum(a: BigInt): Int = a.signum
  def times(a:BigInt, b:BigInt): BigInt = a * b
  def zero: BigInt = BigInt(0)
  
  override def fromInt(n: Int): BigInt = BigInt(n)
}

trait BigDecimalIsRing extends Ring[BigDecimal] with BigDecimalEq {
  def abs(a:BigDecimal): BigDecimal = a.abs
  def minus(a:BigDecimal, b:BigDecimal): BigDecimal = a - b
  def negate(a:BigDecimal): BigDecimal = -a
  def one: BigDecimal = BigDecimal(1.0)
  def plus(a:BigDecimal, b:BigDecimal): BigDecimal = a + b
  def pow(a:BigDecimal, b:Int): BigDecimal = a.pow(b)
  def signum(a: BigDecimal): Int = a.signum
  def times(a:BigDecimal, b:BigDecimal): BigDecimal = a * b
  def zero: BigDecimal = BigDecimal(0.0)

  override def fromInt(n: Int): BigDecimal = BigDecimal(n)
}

trait RationalIsRing extends Ring[Rational] with RationalEq {
  def abs(a:Rational): Rational = a.abs
  def minus(a:Rational, b:Rational): Rational = a - b
  def negate(a:Rational): Rational = -a
  def one: Rational = Rational.one
  def plus(a:Rational, b:Rational): Rational = a + b
  def pow(a:Rational, b:Int): Rational = a.pow(b)
  def signum(a: Rational): Int = a.signum
  def times(a:Rational, b:Rational): Rational = a * b
  def zero: Rational = Rational.zero
  
  override def fromInt(n: Int): Rational = Rational(n)
}

trait ComplexIsRing[A] extends ComplexEq[A] with Ring[Complex[A]] {
  implicit val f:FractionalWithNRoot[A]

  def abs(a:Complex[A]): Complex[A] = Complex(a.abs, f.zero)(f)
  def minus(a:Complex[A], b:Complex[A]): Complex[A] = a - b
  def negate(a:Complex[A]): Complex[A] = -a
  def one: Complex[A] = Complex.one(f)
  def plus(a:Complex[A], b:Complex[A]): Complex[A] = a + b
  def pow(a:Complex[A], b:Int):Complex[A] = a.pow(Complex(f.fromInt(b), f.zero))
  def signum(a: Complex[A]): Int = a.signum
  def times(a:Complex[A], b:Complex[A]): Complex[A] = a * b
  def zero: Complex[A] = Complex.zero(f)

  override def fromInt(n: Int): Complex[A] = Complex(f.fromInt(n), f.zero)
}

trait RealIsRing extends Ring[Real] with RealEq {
  def abs(r: Real): Real = r.abs
  def minus(a: Real, b: Real): Real = a - b
  def negate(a: Real): Real = -a
  def one: Real = Real(1)
  def plus(a: Real, b: Real): Real = a + b
  def pow(a: Real, b: Int): Real = a pow b
  override def sign(a: Real): Sign = a.sign
  def signum(a: Real): Int = a.signum
  def times(a: Real, b: Real): Real = a * b
  def zero: Real = Real(0)
  
  override def fromInt(n: Int): Real = Real(n)
}

class ComplexIsRingCls[A](implicit val f:FractionalWithNRoot[A])
extends ComplexIsRing[A]
