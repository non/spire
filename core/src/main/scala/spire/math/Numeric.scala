package spire.math

import spire.algebra._
import spire.std._

import scala.{specialized => spec}

/**
 * TODO
 * 3. LiteralOps? Literal conversions?
 * 4. Review operator symbols?
 * 5. Support for more operators?
 * 6. Start to worry about things like e.g. pow(BigInt, BigInt)
 */

trait Numeric[@spec(Int,Long,Float,Double) A] extends Field[A] with NRoot[A]
with ConvertableFrom[A] with ConvertableTo[A] with Order[A] with Signed[A]

object Numeric {
  implicit object IntIsNumeric extends IntIsNumeric
  implicit object LongIsNumeric extends LongIsNumeric
  implicit object FloatIsNumeric extends FloatIsNumeric
  implicit object DoubleIsNumeric extends DoubleIsNumeric
  implicit object BigIntIsNumeric extends BigIntIsNumeric
  implicit object BigDecimalIsNumeric extends BigDecimalIsNumeric
  implicit def RationalIsNumeric(implicit ctx: ApproximationContext[Rational] =
      ApproximationContext(Rational(1, 1000000000))) = new RationalIsNumeric {
    val context = ctx
  }
  implicit object RealIsNumeric extends RealIsNumeric
  implicit def complexIsNumeric[A: Fractional: Trig: IsReal] = new ComplexIsNumeric

  @inline final def apply[A](implicit ev:Numeric[A]):Numeric[A] = ev
}

private[math] trait IntIsNumeric extends Numeric[Int] with IntIsEuclideanRing with IntIsNRoot
with ConvertableFromInt with ConvertableToInt with IntOrder with IntIsSigned {
  override def fromInt(n: Int): Int = n
  override def fromDouble(n: Double): Int = n.toInt
  def div(a:Int, b:Int) = a / b
  def ceil(a: Int): Int = a
  def floor(a: Int): Int = a
  def round(a: Int): Int = a
  def isWhole(a:Int) = true
}

private[math] trait LongIsNumeric extends Numeric[Long] with LongIsEuclideanRing with LongIsNRoot
with ConvertableFromLong with ConvertableToLong with LongOrder with LongIsSigned {
  override def fromInt(n: Int): Long = n
  override def fromDouble(n: Double): Long = n.toLong
  def div(a:Long, b:Long) = a / b
  def ceil(a: Long): Long = a
  def floor(a: Long): Long = a
  def round(a: Long): Long = a
  def isWhole(a:Long) = true
}

private[math] trait BigIntIsNumeric extends Numeric[BigInt] with BigIntIsEuclideanRing
with BigIntIsNRoot with ConvertableFromBigInt with ConvertableToBigInt
with BigIntOrder with BigIntIsSigned {
  override def fromInt(n: Int): BigInt = BigInt(n)
  override def fromDouble(n: Double): BigInt = BigDecimal(n).toBigInt
  def div(a:BigInt, b:BigInt) = a / b
  def ceil(a: BigInt): BigInt = a
  def floor(a: BigInt): BigInt = a
  def round(a: BigInt): BigInt = a
  def isWhole(a:BigInt) = true
}

private[math] trait FloatIsNumeric extends Numeric[Float] with FloatIsField
with FloatIsNRoot with ConvertableFromFloat with ConvertableToFloat
with FloatOrder with FloatIsSigned {
  override def fromInt(n: Int): Float = n.toFloat
  override def fromDouble(n: Double): Float = n.toFloat
}

private[math] trait DoubleIsNumeric extends Numeric[Double] with DoubleIsField
with DoubleIsNRoot with ConvertableFromDouble with ConvertableToDouble
with DoubleOrder with DoubleIsSigned {
  override def fromInt(n: Int): Double = n.toDouble
  override def fromDouble(n: Double): Double = n
}

private[math] trait BigDecimalIsNumeric extends Numeric[BigDecimal] with BigDecimalIsField
with BigDecimalIsNRoot with ConvertableFromBigDecimal with ConvertableToBigDecimal
with BigDecimalOrder with BigDecimalIsSigned {
  override def fromInt(n: Int): BigDecimal = BigDecimal(n)
  override def fromDouble(n: Double): BigDecimal = BigDecimal(n)
}

private[math] trait RationalIsNumeric extends Numeric[Rational] with RationalIsField
with RationalIsNRoot with ConvertableFromRational with ConvertableToRational
with RationalIsReal {
  override def toDouble(n: Rational): Double = n.toDouble
  override def fromInt(n: Int): Rational = Rational(n)
  override def fromDouble(n: Double): Rational = Rational(n)
}

private[math] trait RealIsNumeric extends Numeric[Real] with RealIsField with RealIsNRoot
with ConvertableFromReal with ConvertableToReal with RealOrder with RealIsSigned {
  override def fromInt(n: Int): Real = Real(n)
  override def fromDouble(n: Double): Real = Real(n)
}


class ComplexIsNumeric[A](implicit val f:Fractional[A], val t:Trig[A], val r:IsReal[A])
extends ComplexIsField[A] with Numeric[Complex[A]] with ComplexEq[A]
with ComplexIsTrig[A] with ComplexIsNRoot[A]
with ConvertableFromComplex[A] with ConvertableToComplex[A]
with Order[Complex[A]] with ComplexIsSigned[A] {
  override def fromInt(n: Int): Complex[A] = Complex.fromInt[A](n)
  override def fromDouble(n: Double): Complex[A] = Complex[A](f.fromDouble(n))

  override def eqv(x: Complex[A], y: Complex[A]): Boolean = x == y
  override def nroot(a: Complex[A], n: Int) = a.pow(reciprocal(fromInt(n)))
  override def gt(x:Complex[A], y:Complex[A]) = sys.error("undefined")
  override def gteqv(x:Complex[A], y:Complex[A]) = sys.error("undefined")
  override def lt(x:Complex[A], y:Complex[A]) = sys.error("undefined")
  override def lteqv(x:Complex[A], y:Complex[A]) = sys.error("undefined")
  def compare(x:Complex[A], y:Complex[A]): Int = if (x eqv y) 0 else sys.error("undefined")
}
