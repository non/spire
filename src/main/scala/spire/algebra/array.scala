package spire.algebra

import scala.{ specialized => spec }
import scala.reflect.ClassTag

trait ArrayModule[@spec(Int,Long,Float,Double) A] extends Module[Array[A], A] {
  implicit def classTag: ClassTag[A]

  def zero: Array[A] = new Array[A](0)

  def negate(x: Array[A]): Array[A] = {
    val y = new Array[A](x.length)
    var i = 0
    while (i < x.length) {
      y(i) = scalar.negate(x(i))
      i += 1
    }
    y
  }

  def plus(x: Array[A], y: Array[A]): Array[A] = {
    var prefix = math.min(x.length, y.length)
    var z = new Array[A](math.max(x.length, y.length))
    var i = 0
    while (i < prefix) {
      z(i) = scalar.plus(x(i), y(i))
      i += 1
    }
    while (i < x.length) { z(i) = x(i); i += 1 }
    while (i < y.length) { z(i) = y(i); i += 1 }
    z
  }

  override def minus(x: Array[A], y: Array[A]): Array[A] = {
    var prefix = math.min(x.length, y.length)
    var z = new Array[A](math.max(x.length, y.length))
    var i = 0
    while (i < prefix) {
      z(i) = scalar.minus(x(i), y(i))
      i += 1
    }
    while (i < x.length) { z(i) = x(i); i += 1 }
    while (i < y.length) { z(i) = scalar.negate(y(i)); i += 1 }
    z
  }

  def timesl(r: A, x: Array[A]): Array[A] = {
    val y = new Array[A](x.length)
    var i = 0
    while (i < y.length) {
      y(i) = scalar.times(r, x(i))
      i += 1
    }
    y
  }
}

trait ArrayVectorSpace[@spec(Int,Long,Float,Double) A] extends ArrayModule[A] with VectorSpace[Array[A], A]

trait ArrayInnerProductSpace[@spec(Int,Long,Float,Double) A] extends ArrayVectorSpace[A]
with InnerProductSpace[Array[A], A] {
  def dot(x: Array[A], y: Array[A]): A = {
    var z = scalar.zero
    var i = 0
    while (i < x.length && i < y.length) {
      z = scalar.plus(z, scalar.times(x(i), y(i)))
      i += 1
    }
    z
  }
}
