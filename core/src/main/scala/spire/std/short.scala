package spire.std

import spire.algebra._

trait ShortIsEuclideanRing extends EuclideanRing[Short] {
  override def minus(a:Short, b:Short): Short = (a - b).toShort
  def negate(a:Short): Short = (-a).toShort
  def one: Short = 1.toShort
  def plus(a:Short, b:Short): Short = (a + b).toShort
  override def pow(a: Short, b:Int): Short = Math.pow(a, b).toShort
  override def times(a:Short, b:Short): Short = (a * b).toShort
  def zero: Short = 0.toShort
  
  override def fromInt(n: Int): Short = n.toShort

  def quot(a: Short, b: Short) = (a / b).toShort
  def mod(a: Short, b: Short) = (a % b).toShort
  def gcd(a: Short, b: Short): Short = spire.math.gcd(a, b).toShort
}

trait ShortOrder extends Order[Short] {
  override def eqv(x:Short, y:Short) = x == y
  override def neqv(x:Short, y:Short) = x != y
  override def gt(x: Short, y: Short) = x > y
  override def gteqv(x: Short, y: Short) = x >= y
  override def lt(x: Short, y: Short) = x < y
  override def lteqv(x: Short, y: Short) = x <= y
  def compare(x: Short, y: Short) = if (x < y) -1 else if (x > y) 1 else 0
}

trait ShortIsSigned extends Signed[Short] {
  def signum(a: Short): Int = a
  def abs(a: Short): Short = (if (a < 0) -a else a).toShort
}

trait ShortIsReal extends IsIntegral[Short] with ShortOrder with ShortIsSigned {
  def toDouble(n: Short): Double = n.toDouble
}

trait ShortIsBooleanAlgebra extends BooleanAlgebra[Short] {
  def one: Short = (-1: Short)
  def zero: Short = (0: Short)
  def and(a: Short, b: Short): Short = (a & b).toShort
  def or(a: Short, b: Short): Short = (a | b).toShort
  def complement(a: Short): Short = (~a).toShort
  override def xor(a: Short, b: Short): Short = (a ^ b).toShort
}

trait ShortInstances {
  implicit object ShortBooleanAlgebra extends ShortIsBooleanAlgebra
  implicit object ShortAlgebra extends ShortIsEuclideanRing
  implicit object ShortIsReal extends ShortIsReal
}
