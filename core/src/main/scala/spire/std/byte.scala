package spire.std

import spire.algebra._

trait ByteIsEuclideanRing extends EuclideanRing[Byte] {
  override def minus(a:Byte, b:Byte): Byte = (a - b).toByte
  def negate(a:Byte): Byte = (-a).toByte
  def one: Byte = 1.toByte
  def plus(a:Byte, b:Byte): Byte = (a + b).toByte
  override def pow(a: Byte, b:Int): Byte = Math.pow(a, b).toByte
  override def times(a:Byte, b:Byte): Byte = (a * b).toByte
  def zero: Byte = 0.toByte
  
  override def fromInt(n: Int): Byte = n.toByte

  def quot(a: Byte, b: Byte) = (a / b).toByte
  def mod(a: Byte, b: Byte) = (a % b).toByte
  def gcd(a: Byte, b: Byte): Byte = spire.math.gcd(a, b).toByte
}

trait ByteIsSigned extends Signed[Byte] {
  def signum(a: Byte): Int = a
  def abs(a: Byte): Byte = (if (a < 0) -a else a).toByte
}

trait ByteOrder extends Order[Byte] {
  override def eqv(x:Byte, y:Byte) = x == y
  override def neqv(x:Byte, y:Byte) = x != y
  override def gt(x: Byte, y: Byte) = x > y
  override def gteqv(x: Byte, y: Byte) = x >= y
  override def lt(x: Byte, y: Byte) = x < y
  override def lteqv(x: Byte, y: Byte) = x <= y
  def compare(x: Byte, y: Byte) = if (x < y) -1 else if (x > y) 1 else 0
}

trait ByteIsReal extends IsIntegral[Byte] with ByteOrder with ByteIsSigned {
  def toDouble(n: Byte): Double = n.toDouble
}

trait ByteIsBooleanAlgebra extends BooleanAlgebra[Byte] {
  def one: Byte = (-1: Byte)
  def zero: Byte = (0: Byte)
  def and(a: Byte, b: Byte): Byte = (a & b).toByte
  def or(a: Byte, b: Byte): Byte = (a | b).toByte
  def complement(a: Byte): Byte = (~a).toByte
  override def xor(a: Byte, b: Byte): Byte = (a ^ b).toByte
}

trait ByteInstances {
  implicit object ByteBooleanAlgebra extends ByteIsBooleanAlgebra
  implicit object ByteAlgebra extends ByteIsEuclideanRing
  implicit object ByteIsReal extends ByteIsReal
}
