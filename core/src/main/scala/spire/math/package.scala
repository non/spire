package spire

import spire.algebra._
import spire.math._

import scala.annotation.tailrec

import java.lang.Long.numberOfTrailingZeros
import java.lang.Math
import java.math.BigInteger

package object math {
  // largest possible double as BigDecimal
  private final val maxDouble = BigDecimal(Double.MaxValue)

  // natural log of largest possible double as BigDecimal
  private final val logMaxDouble = BigDecimal(Math.log(Double.MaxValue))

  // e^logMaxDouble as BigDecimal
  private final val expLogMaxDouble = BigDecimal(Math.exp(Math.log(Double.MaxValue)))

  /**
   * log() implementations
   */

  final def abs(n: Byte): Byte = Math.abs(n).toByte
  final def abs(n: Short): Short = Math.abs(n).toShort
  final def abs(n: Int): Int = Math.abs(n)
  final def abs(n: Long): Long = Math.abs(n)
  final def abs(n: Float): Float = Math.abs(n)
  final def abs(n: Double): Double = Math.abs(n)

  final def ceil(n: Float): Float = Math.ceil(n).toFloat
  final def ceil(n: Double): Double = Math.ceil(n)
  final def ceil(n: BigDecimal): BigDecimal = n.setScale(0, BigDecimal.RoundingMode.CEILING)

  final def floor(n: Float): Float = Math.floor(n).toFloat
  final def floor(n: Double): Double = Math.floor(n)
  final def floor(n: BigDecimal): BigDecimal = n.setScale(0, BigDecimal.RoundingMode.FLOOR)

  final def log(n: Double): Double = Math.log(n)

  // FIXME: we should actually try to return values which are correct for the
  // given precision.
  final def log(n:BigDecimal):BigDecimal = {
    if (n < 0) sys.error("invalid argument: %s" format n)
    else _log(n, BigDecimal(0))
  }
  
  // Since log(n * x) = log(n) + log(x), we can use Math.log to
  // approximate log for BigDecimal.
  @tailrec private final def _log(n:BigDecimal, sofar:BigDecimal): BigDecimal = {
    if (n <= maxDouble) BigDecimal(Math.log(n.toDouble)) + sofar
    else _log(n / maxDouble, logMaxDouble + sofar)
  }

  final def log[A](a: A)(implicit t: Trig[A]): A = t.log(a)

  /**
   * exp() implementations
   */
  final def exp(n:Double):Double = Math.exp(n)

  final def exp(n: BigDecimal): BigDecimal = _exp(n, BigDecimal(1))

  // Since exp(a + b) = exp(a) * exp(b), we can use Math.log to
  // approximate exp for BigDecimal.
  @tailrec private final def _exp(n: BigDecimal, sofar:BigDecimal): BigDecimal =
    if (n <= logMaxDouble) BigDecimal(Math.exp(n.toDouble)) * sofar
    else _exp(n - logMaxDouble, maxDouble * sofar)

  // FIXME: this is not working yet.
  // def exp(k: BigDecimal): BigDecimal = {
  //   import spire.algebra.NRoot.BigDecimalIsNRoot.sqrt
  //
  //   val mc = k.mc
  //   var scale = BigDecimal(10, mc).pow(mc.getPrecision)
  //
  //   var kk = k
  //   var num = BigDecimal(1.0, mc)
  //   var denom = BigInt(1)
  //   var n = BigDecimal(1, mc)
  //   var m = 1
  //   while (n < scale) {
  //     num = num * m + kk
  //     denom = denom * m
  //     n = n * m
  //     m += 1
  //     kk *= k
  //     scale *= k
  //   }
  //
  //   num / BigDecimal(denom, mc)
  // }

  final def exp[A](a: A)(implicit t: Trig[A]): A = t.exp(a)

  /**
   * pow() implementations
   */

  // Since a^b = e^(log(a) * b) we can use exp and log to write pow.
  // TODO: doesn't make precision guarantees, but it's better than nothing.
  private val maxIntEx = BigDecimal(999999999)
  private val minIntEx = BigDecimal(-999999999)

  // FIXME: we should actually try to return values which are correct for the
  // given precision.
  final def pow(base: BigDecimal, ex: BigDecimal) =
    if (ex.isValidInt && ex <= maxIntEx && ex >= minIntEx) base.pow(ex.toInt)
    else exp(log(base) * ex)

  final def pow(base: BigInt, ex: BigInt) = if (ex.signum < 0) {
    if (base == 0) sys.error("zero can't be raised to negative power")
    else if (base == 1) base
    else if (base == -1) if (ex.testBit(0)) BigInt(1) else base
    else BigInt(0)
  } else if (ex.isValidInt) {
    base.pow(ex.toInt)
  } else {
    bigIntPow(BigInt(1), base, ex)
  }

  @tailrec private[math] final def bigIntPow(t: BigInt, b: BigInt, e: BigInt): BigInt = {
    if (e.isValidInt) t * b.pow(e.toInt)
    else if (e.testBit(0)) bigIntPow(t * b, b * b, e >> 1)
    else bigIntPow(t, b * b, e >> 1)
  }

  /**
   * Exponentiation function, e.g. x^y
   *
   * If base^ex doesn't fit in a Long, the result will overflow (unlike
   * Math.pow which will return +/- Infinity). 
   */
  final def pow(base:Long, ex:Long):Long = if (ex < 0L) {
    if(base == 0L) sys.error("zero can't be raised to negative power")
    else if (base == 1L) 1L
    else if (base == -1L) if ((ex & 1L) == 0L) -1L else 1L
    else 0L
  } else {
    longPow(1L, base, ex)
  }

  @tailrec private[math] final def longPow(t:Long, b:Long, e:Long): Long = {
    if (e == 0L) t
    else if ((e & 1) == 1) longPow(t * b, b * b, e >> 1L)
    else longPow(t, b * b, e >> 1L)
  }

  final def pow(base: Double, exponent: Double) = Math.pow(base, exponent)

  final def gcd(_x: Long, _y: Long): Long = {
    if (_x == 0L) return Math.abs(_y)
    if (_y == 0L) return Math.abs(_x)
  
    var x = _x
    var xz = numberOfTrailingZeros(x)
    x = Math.abs(x >> xz)
  
    var y = _y
    var yz = numberOfTrailingZeros(y)
    y = Math.abs(y >> yz)

    while (x != y) {
      if (x > y) {
        x -= y
        x >>= numberOfTrailingZeros(x)
      } else {
        y -= x
        y >>= numberOfTrailingZeros(y)
      }
    }

    if (xz < yz) x << xz else x << yz
  }

  final def gcd(a: BigInt, b: BigInt): BigInt = a.gcd(b)

  final def gcd[A](x: A, y: A)(implicit ev: EuclideanRing[A]): A = ev.gcd(x, y)

  final def lcm(x: Long, y: Long): Long = (x / gcd(x, y)) * y

  final def lcm(a: BigInt, b: BigInt): BigInt = (a / a.gcd(b)) * b

  final def lcm[A](x: A, y: A)(implicit ev: EuclideanRing[A]): A = ev.lcm(x, y)

  final def round(a: Float): Float =
    if (Math.abs(a) >= 16777216.0F) a else Math.round(a).toFloat

  final def round(a: Double): Double =
    if (Math.abs(a) >= 4503599627370496.0) a else Math.round(a).toDouble

  final def round(a: BigDecimal): BigDecimal =
    a.setScale(0, BigDecimal.RoundingMode.HALF_UP)

  final def min(x: Byte, y: Byte): Byte = Math.min(x, y).toByte
  final def min(x: Short, y: Short): Short = Math.min(x, y).toShort
  final def min(x: Int, y: Int): Int = Math.min(x, y)
  final def min(x: Long, y: Long): Long = Math.min(x, y)
  final def min(x: Float, y: Float): Float = Math.min(x, y)
  final def min(x: Double, y: Double): Double = Math.min(x, y)
  final def min[A](x: A, y: A)(implicit ev: Order[A]) = ev.min(x, y)

  final def max(x: Byte, y: Byte): Byte = Math.max(x, y).toByte
  final def max(x: Short, y: Short): Short = Math.max(x, y).toShort
  final def max(x: Int, y: Int): Int = Math.max(x, y)
  final def max(x: Long, y: Long): Long = Math.max(x, y)
  final def max(x: Float, y: Float): Float = Math.max(x, y)
  final def max(x: Double, y: Double): Double = Math.max(x, y)
  final def max[A](x: A, y: A)(implicit ev: Order[A]) = ev.max(x, y)

  final def e[A: Trig]: A = Trig[A].e
  final def pi[A: Trig]: A = Trig[A].pi

  final def sin[A: Trig](a: A): A = Trig[A].sin(a)
  final def cos[A: Trig](a: A): A = Trig[A].cos(a)
  final def tan[A: Trig](a: A): A = Trig[A].tan(a)

  final def asin[A: Trig](a: A): A = Trig[A].asin(a)
  final def acos[A: Trig](a: A): A = Trig[A].acos(a)
  final def atan[A: Trig](a: A): A = Trig[A].atan(a)
  final def atan2[A: Trig](y: A, x: A): A = Trig[A].atan2(y, x)

  final def sinh[A: Trig](x: A): A = Trig[A].sinh(x)
  final def cosh[A: Trig](x: A): A = Trig[A].cosh(x)
  final def tanh[A: Trig](x: A): A = Trig[A].tanh(x)
}

