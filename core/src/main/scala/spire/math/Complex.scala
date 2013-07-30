package spire.math

import spire.algebra._
import spire.std.float._
import spire.std.double._
import spire.std.bigDecimal._

import spire.syntax.convertableFrom._
import spire.syntax.field._
import spire.syntax.isReal._
import spire.syntax.nroot._
import spire.syntax.order._

import scala.{specialized => spec}
import scala.annotation.tailrec
import scala.math.{ScalaNumber, ScalaNumericConversions}
import java.lang.Math


object Complex extends ComplexInstances {
  def i[@spec(Float, Double) T](implicit f: Fractional[T], t: Trig[T], r: IsReal[T]) =
    new Complex(f.zero, f.one)

  def one[@spec(Float, Double) T](implicit f: Fractional[T], t: Trig[T], r: IsReal[T]) =
    new Complex(f.one, f.zero)

  def zero[@spec(Float, Double) T](implicit f: Fractional[T], t: Trig[T], r: IsReal[T]) =
    new Complex(f.zero, f.zero)

  def fromInt[@spec(Float, Double) T](n: Int)(implicit f: Fractional[T], t: Trig[T], r: IsReal[T]) =
    new Complex(f.fromInt(n), f.zero)

  implicit def intToComplex(n: Int) = new Complex(n.toDouble, 0.0)
  implicit def longToComplex(n: Long) = new Complex(n.toDouble, 0.0)
  implicit def floatToComplex(n: Float) = new Complex(n, 0.0F)
  implicit def doubleToComplex(n: Double) = new Complex(n, 0.0)

  implicit def bigIntToComplex(n: BigInt): Complex[BigDecimal] =
    bigDecimalToComplex(BigDecimal(n))

  implicit def bigDecimalToComplex(n: BigDecimal): Complex[BigDecimal] = {
    implicit val mc = n.mc
    new Complex(n, BigDecimal(0))
  }

  def polar[@spec(Float, Double) T](magnitude: T, angle: T)(implicit f: Fractional[T], t: Trig[T], r: IsReal[T]): Complex[T] =
    new Complex(magnitude * t.cos(angle), magnitude * t.sin(angle))

  def apply[@spec(Float, Double) T](real: T)(implicit f: Fractional[T], t: Trig[T], r: IsReal[T]): Complex[T] =
    new Complex(real, f.zero)

  def rootOfUnity[@spec(Float, Double) T](n: Int, x: Int)(implicit f: Fractional[T], t: Trig[T], r: IsReal[T]): Complex[T] = {
    if (x == 0) return one[T]

    if (n % 2 == 0) {
      if (x == n / 2) return -one[T]
      if (n % 4 == 0) {
        if (x == n / 4) return i[T]
        if (x == n * 3 / 4) return -i[T]
      }
    }

    polar(f.one, (t.pi * 2 * x) / n)
  }

  def rootsOfUnity[@spec(Float, Double) T](n: Int)(implicit f: Fractional[T], t: Trig[T], r: IsReal[T]): Array[Complex[T]] = {
    val roots = new Array[Complex[T]](n)
    var sum = one[T]
    roots(0) = sum

    val west = if (n % 2 == 0) n / 2 else -1
    val north = if (n % 4 == 0) n / 4 else -1
    val south = if (n % 4 == 0) 3 * n / 4 else -1

    var x = 1
    val last = n - 1
    while (x < last) {
      val c = x match {
        case `north` => i[T]
        case `west` => -one[T]
        case `south` => -i[T]
        case _ => polar(f.one, (t.pi * 2 * x) / n)
      }
      roots(x) = c
      sum += c
      x += 1
    }

    roots(last) = zero[T] - sum
    roots
  }
}

final case class Complex[@spec(Float, Double) T](real: T, imag: T)(implicit f: Fractional[T], t: Trig[T], r: IsReal[T])
    extends ScalaNumber with ScalaNumericConversions with Serializable {

  import spire.syntax.order._

  def doubleValue: Double = real.toDouble
  def floatValue: Float = real.toFloat
  def longValue: Long = real.toLong
  def intValue: Int = real.toInt
  override def shortValue: Short = real.toShort
  override def byteValue: Byte = real.toByte

  def isWhole: Boolean = imag === f.zero && real.isWhole

  /**
   * This implements csgn(z), which (except for z=0) observes:
   * 
   * csgn(z) = z / sqrt(z*z) = sqrt(z*z) / z
   */
  def signum: Int = if (real === f.zero) {
    imag compare f.zero
  } else {
    if (real < f.zero) -1 else 1
  }

  def underlying: (T, T) = (real, imag)

  /**
   * This implements sgn(z), which (except for z=0) observes:
   * 
   * sgn(z) = z / abs(z) = abs(z) / z
   */
  def complexSignum: Complex[T] = {
    val a = abs
    if (a === f.zero) this else this / a
  }

  override final def isValidInt: Boolean =
    imag === f.zero && real.isWhole &&
      real <= f.fromInt(Int.MaxValue) && real >= f.fromInt(Int.MinValue)

  override def hashCode: Int =
    if (isValidInt) real.toInt else 19 * real.## + 41 * imag.## + 97

  // not typesafe, so this is the best we can do :(
  override def equals(that: Any): Boolean = that match {
    case that: Complex[_] => real == that.real && imag == that.imag
    case that => unifiedPrimitiveEquals(that)
  }

  override def toString: String = "(%s + %si)" format (real, imag)

  def abs: T = (real * real + imag * imag).sqrt
  def arg: T = t.atan2(imag, real)

  def norm: T = real * real + imag * imag
  def conjugate: Complex[T] = new Complex(real, -imag)

  def asTuple: (T, T) = (real, imag)
  def asPolarTuple: (T, T) = (abs, arg)

  def isZero: Boolean = real === f.zero && imag === f.zero
  def isImaginary: Boolean = real === f.zero
  def isReal: Boolean = imag === f.zero

  def eqv(b: Complex[T]): Boolean = real === b.real && imag === b.imag
  def neqv(b: Complex[T]): Boolean = real =!= b.real || imag =!= b.imag

  def unary_-(): Complex[T] = new Complex(-real, -imag)

  def +(b: T): Complex[T] = new Complex(real + b, imag)
  def -(b: T): Complex[T] = new Complex(real - b, imag)
  def *(b: T): Complex[T] = new Complex(real * b, imag * b)
  def /(b: T): Complex[T] = new Complex(real / b, imag / b)

  def /~(b: T): Complex[T] = (this / b).floor
  def %(b: T): Complex[T] = this - (this /~ b) * b
  def /%(b: T): (Complex[T], Complex[T]) = {
    val q = this /~ b
    (q, this - q * b)
  }

  def **(b: T): Complex[T] = pow(b)
  def pow(b: T): Complex[T] = if (b === f.zero) {
    Complex.one[T]
  } else if (this.isZero) {
    if (b < f.zero)
      throw new Exception("raising 0 to negative/complex power")
    Complex.zero[T]
  } else {
    Complex.polar(abs fpow b, arg * b)
  }

  def +(b: Complex[T]): Complex[T] =
    new Complex(real + b.real, imag + b.imag)

  def -(b: Complex[T]): Complex[T] =
    new Complex(real - b.real, imag - b.imag)

  def *(b: Complex[T]): Complex[T] =
    new Complex(real * b.real - imag * b.imag, imag * b.real + real * b.imag)

  def /(b: Complex[T]): Complex[T] = {
    val abs_breal = b.real.abs
    val abs_bimag = b.imag.abs

    if (abs_breal >= abs_bimag) {
      if (abs_breal === f.zero) throw new Exception("/ by zero")
      val ratio = b.imag / b.real
      val denom = b.real + b.imag * ratio
      new Complex((real + imag * ratio) / denom, (imag - real * ratio) / denom)

    } else {
      if (abs_bimag === f.zero) throw new Exception("/ by zero")
      val ratio = b.real / b.imag
      val denom = b.real * ratio + b.imag
      new Complex((real * ratio + imag) / denom, (imag * ratio - real) /denom)
    }
  }

  def /~(b: Complex[T]): Complex[T] = {
    val d = this / b
    new Complex(d.real.floor, d.imag.floor)
  }

  def %(b: Complex[T]): Complex[T] = this - (this /~ b) * b

  def /%(b: Complex[T]): (Complex[T], Complex[T]) = {
    val q = this /~ b
    (q, this - q * b)
  }

  def **(b: Int): Complex[T] = pow(b)

  def nroot(k: Int): Complex[T] = pow(Complex(f.fromInt(k).reciprocal, f.zero))

  def pow(b: Int): Complex[T] = Complex.polar(abs.pow(b), arg * b)

  def **(b: Complex[T]): Complex[T] = pow(b)

  def pow(b: Complex[T]): Complex[T] = if (b.isZero) {
    Complex.one[T]
  } else if (this.isZero) {
    if (b.imag =!= f.zero || b.real < f.zero)
      throw new Exception("raising 0 to negative/complex power")
    Complex.zero[T]
  } else if (b.imag =!= f.zero) {
    val len = (abs fpow b.real) / t.exp(arg * b.imag)
    val phase = arg * b.real + t.log(abs) * b.imag
    Complex.polar(len, phase)
  } else {
    Complex.polar(abs fpow b.real, arg * b.real)
  }

  // we are going with the "principal value" definition of Log.
  def log: Complex[T] = {
    if (this.isZero) throw new IllegalArgumentException("log(0) undefined")
    new Complex(t.log(abs), arg)
  }

  def sqrt: Complex[T] = {
    val v = ((real.abs + this.abs) / f.fromInt(2)).sqrt
    if (real > f.zero)
      new Complex(v, imag / (v + v))
    else
      new Complex(imag.abs / (v + v), v * f.fromInt(imag.signum))
  }

  def floor: Complex[T] = new Complex(real.floor, imag.floor)
  def ceil: Complex[T] = new Complex(real.ceil, imag.ceil)
  def round: Complex[T] = new Complex(real.round, imag.round)

  // acos(z) = -i*(log(z + i*(sqrt(1 - z*z))))
  def acos: Complex[T] = {
    val z2 = this * this
    val s = new Complex(f.one - z2.real, -z2.imag).sqrt
    val l = new Complex(real + s.imag, imag + s.real).log
    new Complex(l.imag, -l.real)
  }

  // asin(z) = -i*(log(sqrt(1 - z*z) + i*z))
  def asin: Complex[T] = {
    val z2 = this * this
    val s = new Complex(f.one - z2.real, -z2.imag).sqrt
    val l = new Complex(s.real + -imag, s.imag + real).log
    new Complex(l.imag, -l.real)
  }

  // atan(z) = (i/2) log((i + z)/(i - z))
  def atan: Complex[T] = {
    val n = new Complex(real, imag + f.one)
    val d = new Complex(-real, f.one - imag)
    val l = (n / d).log
    new Complex(l.imag / f.fromInt(-2), l.real / f.fromInt(2))
  }

  // exp(a+ci) = (exp(a) * cos(c)) + (exp(a) * sin(c))i
  def exp: Complex[T] =
    new Complex(t.exp(real) * t.cos(imag), t.exp(real) * t.sin(imag))

  // sin(a+ci) = (sin(a) * cosh(c)) + (-cos(a) * sinh(c))i
  def sin: Complex[T] =
    new Complex(t.sin(real) * t.cosh(imag), -t.cos(real) * t.sinh(imag))

  // sinh(a+ci) = (sinh(a) * cos(c)) + (-cosh(a) * sin(c))i
  def sinh: Complex[T] =
    new Complex(t.sinh(real) * t.cos(imag), -t.cosh(real) * t.sin(imag))

  // cos(a+ci) = (cos(a) * cosh(c)) + (-sin(a) * sinh(c))i 
  def cos: Complex[T] =
    new Complex(t.cos(real) * t.cosh(imag), -t.sin(real) * t.sinh(imag))

  // cosh(a+ci) = (cosh(a) * cos(c)) + (-sinh(a) * sin(c))i 
  def cosh: Complex[T] =
    new Complex(t.cosh(real) * t.cos(imag), -t.sinh(real) * t.sin(imag))

  // tan(a+ci) = (sin(a+a) + sinh(c+c)i) / (cos(a+a) + cosh(c+c))
  def tan: Complex[T] = {
    val r2 = real + real
    val i2 = imag + imag
    val d = t.cos(r2) + t.cosh(i2)
    new Complex(t.sin(r2) / d, t.sinh(i2) / d)
  }

  // tanh(a+ci) = (sinh(a+a) + sin(c+c)i) / (cosh(a+a) + cos(c+c))
  def tanh: Complex[T] = {
    val r2 = real + real
    val i2 = imag + imag
    val d = t.cos(r2) + t.cosh(i2)
    new Complex(t.sinh(r2) / d, t.sin(i2) / d)
  }
}


object FloatComplex {
  import FastComplex.{encode}

  final def apply(real: Float, imag: Float): FloatComplex =
    new FloatComplex(encode(real, imag))

  final def apply(real: Double, imag: Double) =
    new FloatComplex(encode(real.toFloat, imag.toFloat))

  def polar(magnitude: Float, angle: Float) =
    new FloatComplex(FastComplex.polar(magnitude, angle))

  final val i = new FloatComplex(4575657221408423936L)
  final val one = new FloatComplex(1065353216L)
  final val zero = new FloatComplex(0L)
}

/**
 * Value class which encodes two floating point values in a Long.
 *
 * We get (basically) unboxed complex numbers using this hack.
 * The underlying implementation lives in the FastComplex object.
 */
class FloatComplex(val u: Long) extends AnyVal {
  override final def toString: String = "(%s+%si)" format (real, imag)

  final def real: Float = FastComplex.real(u)
  final def imag: Float = FastComplex.imag(u)
  final def repr = "FloatComplex(%s, %s)" format(real, imag)
  final def abs: Float = FastComplex.abs(u)
  final def angle: Float = FastComplex.angle(u)
  final def conjugate = new FloatComplex(FastComplex.conjugate(u))
  final def isWhole: Boolean = FastComplex.isWhole(u)
  final def signum: Int = FastComplex.signum(u)
  final def complexSignum = new FloatComplex(FastComplex.complexSignum(u))
  final def negate = new FloatComplex(FastComplex.negate(u))

  final def +(b: FloatComplex) = new FloatComplex(FastComplex.add(u, b.u))
  final def -(b: FloatComplex) = new FloatComplex(FastComplex.subtract(u, b.u))
  final def *(b: FloatComplex) = new FloatComplex(FastComplex.multiply(u, b.u))
  final def /(b: FloatComplex) = new FloatComplex(FastComplex.divide(u, b.u))
  final def /~(b: FloatComplex) = new FloatComplex(FastComplex.quot(u, b.u))
  final def %(b: FloatComplex) = new FloatComplex(FastComplex.mod(u, b.u))

  final def /%(b: FloatComplex) = FastComplex.quotmod(u, b.u) match {
    case (q, m) => (new FloatComplex(q), new FloatComplex(m))
  }

  final def pow(b: FloatComplex) = new FloatComplex(FastComplex.pow(u, b.u))
  final def **(b: FloatComplex) = pow(b)

  final def pow(b: Int) = new FloatComplex(FastComplex.pow(u, FastComplex(b.toFloat, 0.0F)))
  final def **(b: Int) = pow(b)
}


/**
 * FastComplex is an ugly, beautiful hack.
 *
 * The basic idea is to encode two 32-bit Floats into a single 64-bit Long.
 * The lower-32 bits are the "real" Float and the upper-32 are the "imaginary"
 * Float.
 *
 * Since we're overloading the meaning of Long, all the operations have to be
 * defined on the FastComplex object, meaning the syntax for using this is a
 * bit ugly. To add to the ugly beauty of the whole thing I could imagine
 * defining implicit operators on Long like +@, -@, *@, /@, etc.
 *
 * You might wonder why it's even worth doing this. The answer is that when
 * you need to allocate an array of e.g. 10-20 million complex numbers, the GC
 * overhead of using *any* object is HUGE. Since we can't build our own
 * "pass-by-value" types on the JVM we are stuck doing an encoding like this.
 *
 * Here are some profiling numbers for summing an array of complex numbers,
 * timed against a concrete case class implementation using Float (in ms):
 *
 *  size | encoded |  class
 *    1M |     5.1 |    5.8
 *    5M |    28.5 |   91.7
 *   10M |    67.7 |  828.1
 *   20M |   228.0 | 2687.0
 *
 * Not bad, eh?
 */
object FastComplex {
  import java.lang.Math.{atan2, cos, sin, sqrt}

  // note the superstitious use of @inline and final everywhere

  final def apply(real: Float, imag: Float) = encode(real, imag)
  final def apply(real: Double, imag: Double) = encode(real.toFloat, imag.toFloat)

  // encode a float as some bits
  @inline final def bits(n: Float): Int = java.lang.Float.floatToRawIntBits(n)

  // decode some bits into a float
  @inline final def bits(n: Int): Float = java.lang.Float.intBitsToFloat(n)

  // get the real part of the complex number
  @inline final def real(d: Long): Float = bits((d & 0xffffffff).toInt)

  // get the imaginary part of the complex number
  @inline final def imag(d: Long): Float = bits((d >> 32).toInt)

  // define some handy constants
  final val i = encode(0.0F, 1.0F)
  final val one = encode(1.0F, 0.0F)
  final val zero = encode(0.0F, 0.0F)

  // encode two floats representing a complex number
  @inline final def encode(real: Float, imag: Float): Long = {
    (bits(imag).toLong << 32) + bits(real).toLong
  }

  // encode two floats representing a complex number in polar form
  @inline final def polar(magnitude: Float, angle: Float): Long = {
    encode(magnitude * cos(angle).toFloat, magnitude * sin(angle).toFloat)
  }

  // decode should be avoided in fast code because it allocates a Tuple2.
  final def decode(d: Long): (Float, Float) = (real(d), imag(d))

  // produces a string representation of the Long/(Float,Float)
  final def toRepr(d: Long): String = "FastComplex(%s -> %s)" format(d, decode(d))

  // get the magnitude/absolute value
  final def abs(d: Long): Float = {
    val re = real(d)
    val im = imag(d)
    sqrt(re * re + im * im).toFloat
  }

  // get the angle/argument
  final def angle(d: Long): Float = atan2(imag(d), real(d)).toFloat

  // get the complex conjugate
  final def conjugate(d: Long): Long = encode(real(d), -imag(d))

  // see if the complex number is a whole value
  final def isWhole(d: Long): Boolean = real(d) % 1.0F == 0.0F && imag(d) % 1.0F == 0.0F

  // get the sign of the complex number
  final def signum(d: Long): Int = real(d) compare 0.0F

  // get the complex sign of the complex number
  final def complexSignum(d: Long): Long = {
    val m = abs(d)
    if (m == 0.0F) zero else divide(d, encode(m, 0.0F))
  }

  // negation
  final def negate(a: Long): Long = encode(-real(a), -imag(a))

  // addition
  final def add(a: Long, b: Long): Long = encode(real(a) + real(b), imag(a) + imag(b))

  // subtraction
  final def subtract(a: Long, b: Long): Long = encode(real(a) - real(b), imag(a) - imag(b))

  // multiplication
  final def multiply(a: Long, b: Long): Long = {
    val re_a = real(a)
    val im_a = imag(a)
    val re_b = real(b)
    val im_b = imag(b)
    encode(re_a * re_b - im_a * im_b, im_a * re_b + re_a * im_b)
  }

  // division
  final def divide(a: Long, b: Long): Long = {
    val re_a = real(a)
    val im_a = imag(a)
    val re_b = real(b)
    val im_b = imag(b)

    val abs_re_b = Math.abs(re_b)
    val abs_im_b = Math.abs(im_b)

    if (abs_re_b >= abs_im_b) {
      if (abs_re_b == 0.0F) throw new ArithmeticException("/0")
      val ratio = im_b / re_b
      val denom = re_b + im_b * ratio
      encode((re_a + im_a * ratio) / denom, (im_a - re_a * ratio) / denom)

    } else {
      if (abs_im_b == 0.0F) throw new ArithmeticException("/0")
      val ratio = re_b / im_b
      val denom = re_b * ratio + im_b
      encode((re_a * ratio + im_a) / denom, (im_a * ratio - re_a) / denom)
    }
  }

  final def quot(a: Long, b: Long): Long =
    encode(Math.floor(real(divide(a, b))).toFloat, 0.0F)

  final def mod(a: Long, b: Long): Long = subtract(a, multiply(b, quot(a, b)))

  final def quotmod(a: Long, b: Long): (Long, Long) = {
    val q = quot(a, b)
    (q, subtract(a, multiply(b, quot(a, b))))
  }

  // exponentiation
  final def pow(a: Long, b: Long): Long = if (b == zero) {
    encode(1.0F, 0.0F)

  } else if (a == zero) {
    if (imag(b) != 0.0F || real(b) < 0.0F)
      throw new Exception("raising 0 to negative/complex power")
    zero

  } else if (imag(b) != 0.0F) {
    val im_b = imag(b)
    val re_b = real(b)
    val len = (Math.pow(abs(a), re_b) / exp((angle(a) * im_b))).toFloat
    val phase = (angle(a) * re_b + log(abs(a)) * im_b).toFloat
    polar(len, phase)

  } else {
    val len = Math.pow(abs(a), real(b)).toFloat
    val phase = (angle(a) * real(b)).toFloat
    polar(len, phase)
  }
}

trait ComplexInstances {
  implicit def ComplexAlgebra[@spec(Float, Double) A: Fractional: Trig: IsReal] =
    new ComplexAlgebra[A] {
      val f = Fractional[A]
      val t = Trig[A]
      val r = IsReal[A]
      def scalar = f
      def nroot = f
    }

  implicit def ComplexEq[A: Fractional] =
    new ComplexEq[A] {}

  implicit def ComplexIsSigned[A: Fractional: Trig: IsReal] =
    new ComplexIsSigned[A] {
      val f = Fractional[A]
      val t = Trig[A]
      val r = IsReal[A]
    }
}

private[math] trait ComplexIsRing[@spec(Float, Double) A] extends Ring[Complex[A]] {
  implicit def f: Fractional[A]
  implicit def t: Trig[A]
  implicit def r: IsReal[A]

  override def minus(a: Complex[A], b: Complex[A]): Complex[A] = a - b
  def negate(a: Complex[A]): Complex[A] = -a
  def one: Complex[A] = Complex.one(f, t, r)
  def plus(a: Complex[A], b: Complex[A]): Complex[A] = a + b
  override def pow(a: Complex[A], b: Int): Complex[A] = a.pow(b)
  override def times(a: Complex[A], b: Complex[A]): Complex[A] = a * b
  def zero: Complex[A] = Complex.zero(f, t, r)

  override def fromInt(n: Int): Complex[A] = Complex.fromInt[A](n)
}

private[math] trait ComplexIsEuclideanRing[@spec(Float,Double) A]
extends ComplexIsRing[A] with EuclideanRing[Complex[A]] {
  import spire.syntax.order._

  def quot(a: Complex[A], b: Complex[A]) = a /~ b
  def mod(a: Complex[A], b: Complex[A]) = a % b
  override def quotmod(a: Complex[A], b: Complex[A]) = a /% b
  def gcd(a: Complex[A], b: Complex[A]): Complex[A] = {
    @tailrec def _gcd(a: Complex[A], b: Complex[A]): Complex[A] = {
      if (a.abs < f.one) one
      else if (b.isZero) a
      else if (b.abs < f.one) one
      else _gcd(b, a % b)
    }
    _gcd(a, b)
  }
}

private[math] trait ComplexIsField[@spec(Float,Double) A]
extends ComplexIsEuclideanRing[A] with Field[Complex[A]] {
  override def fromDouble(n: Double): Complex[A] = Complex(f.fromDouble(n))
  def div(a: Complex[A], b: Complex[A]) = a / b
  def ceil(a: Complex[A]): Complex[A] = a.ceil
  def floor(a: Complex[A]): Complex[A] = a.floor
  def round(a: Complex[A]): Complex[A] = a.round
  def isWhole(a: Complex[A]) = a.isWhole
}

private[math] trait ComplexIsTrig[@spec(Float, Double) A] extends Trig[Complex[A]] {
  implicit def f: Fractional[A]
  implicit def t: Trig[A]
  implicit def r: IsReal[A]

  def e: Complex[A] = new Complex[A](t.e, f.zero)
  def pi: Complex[A] = new Complex[A](t.pi, f.zero)

  def exp(a: Complex[A]): Complex[A] = a.exp
  def log(a: Complex[A]): Complex[A] = a.log

  def sin(a: Complex[A]): Complex[A] = a.sin
  def cos(a: Complex[A]): Complex[A] = a.cos
  def tan(a: Complex[A]): Complex[A] = a.tan

  def asin(a: Complex[A]): Complex[A] = a.sin
  def acos(a: Complex[A]): Complex[A] = a.cos
  def atan(a: Complex[A]): Complex[A] = a.tan
  def atan2(y: Complex[A], x: Complex[A]): Complex[A] =
    new Complex(x.real, y.imag).atan

  def sinh(x: Complex[A]): Complex[A] = x.sinh
  def cosh(x: Complex[A]): Complex[A] = x.cosh
  def tanh(x: Complex[A]): Complex[A] = x.tanh

  def toRadians(a: Complex[A]): Complex[A] = a
  def toDegrees(a: Complex[A]): Complex[A] = a
}

private[math] trait ComplexIsNRoot[A] extends NRoot[Complex[A]] {
  def nroot(a: Complex[A], k: Int): Complex[A] = a.nroot(k)
  override def sqrt(a: Complex[A]): Complex[A] = a.sqrt
  def fpow(a: Complex[A], b: Complex[A]): Complex[A] = a.pow(b)
}

private[math] trait ComplexEq[A] extends Eq[Complex[A]] {
  def eqv(x: Complex[A], y: Complex[A]) = x eqv y
  override def neqv(x: Complex[A], y: Complex[A]) = x neqv y
}

private[math] trait ComplexIsSigned[A] extends Signed[Complex[A]] {
  implicit def f: Fractional[A]
  implicit def t: Trig[A]
  implicit def r: IsReal[A]

  def signum(a: Complex[A]): Int = a.signum
  def abs(a: Complex[A]): Complex[A] = Complex[A](a.abs, f.zero)
}

private[math] trait ComplexAlgebra[@spec(Float, Double) A] extends ComplexIsField[A]
    with ComplexIsTrig[A] with ComplexIsNRoot[A]
    with InnerProductSpace[Complex[A], A]
    with FieldAlgebra[Complex[A], A] {
  def timesl(a: A, v: Complex[A]): Complex[A] = Complex(a, scalar.zero) * v
  def dot(x: Complex[A], y: Complex[A]): A =
    scalar.plus(scalar.times(x.real, y.real), scalar.times(x.imag, y.imag))
}
