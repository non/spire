package spire.math

import spire.algebra._
import spire.std._
import spire.macrosk.Ops

import scala.{specialized => sp}

trait Integral[@sp(Int,Long) A] extends EuclideanRing[A] with ConvertableFrom[A] with ConvertableTo[A] with IsReal[A]

object Integral {
  implicit final val IntIsIntegral = new IntIsIntegral
  implicit final val LongIsIntegral = new LongIsIntegral
  implicit final val BigIntIsIntegral = new BigIntIsIntegral
  implicit final val SafeLongIsIntegral = new SafeLongIsIntegral

  @inline final def apply[A](implicit ev: Integral[A]) = ev
}

class IntegralOps[A](lhs: A)(implicit ev: Integral[A]) {
  def factor: prime.Factors = prime.factor(toSafeLong)
  def isPrime: Boolean = prime.isPrime(toSafeLong)
  def toSafeLong: SafeLong = SafeLong(ev.toBigInt(lhs))
}

@SerialVersionUID(0L)
private[math] class IntIsIntegral extends Integral[Int] with IntIsEuclideanRing
with ConvertableFromInt with ConvertableToInt with IntIsReal with Serializable {
  override def fromInt(n: Int): Int = n
  override def toDouble(n: Int): Double = n.toDouble
}

@SerialVersionUID(0L)
private[math] class LongIsIntegral extends Integral[Long] with LongIsEuclideanRing
with ConvertableFromLong with ConvertableToLong with LongIsReal with Serializable {
  override def fromInt(n: Int): Long = n.toLong
  override def toDouble(n: Long): Double = n.toDouble
}

@SerialVersionUID(0L)
private[math] class BigIntIsIntegral extends Integral[BigInt] with BigIntIsEuclideanRing
with ConvertableFromBigInt with ConvertableToBigInt with BigIntIsReal with Serializable {
  override def fromInt(n: Int): BigInt = BigInt(n)
  override def toDouble(n: BigInt): Double = n.toDouble
}

@SerialVersionUID(0L)
private[math] class SafeLongIsIntegral extends Integral[SafeLong] with SafeLongIsEuclideanRing
with ConvertableFromSafeLong with ConvertableToSafeLong with SafeLongIsReal with Serializable {
  override def fromInt(n: Int): SafeLong = SafeLong(n)
  override def toDouble(n: SafeLong): Double = n.toDouble
}
