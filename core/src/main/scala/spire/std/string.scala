package spire.std

import spire.algebra._

@SerialVersionUID(0L)
class StringMonoid extends Monoid[String] with Serializable {
  def id = ""
  def op(x: String, y: String): String = x + y
}

@SerialVersionUID(0L)
class StringOrder extends Order[String] with Serializable {
  override def eqv(x: String, y: String): Boolean = x == y
  override def neqv(x: String, y: String): Boolean = x != y
  def compare(x: String, y: String): Int = x.compareTo(y)
}

@SerialVersionUID(0L)
object LevenshteinDistance extends MetricSpace[String, Int] with Serializable {
  def distance(a: String, b: String): Int = {
    var row0 = new Array[Int](b.length + 1)
    var row1 = new Array[Int](b.length + 1)

    var j = 0
    while (j < row0.length) {
      row0(j) = j
      j += 1
    }

    var i = 0
    while (i < a.length) {
      row1(0) = i + 1

      var j = 1
      while (j < row1.length) {
        val d = row0(j - 1) + (if (a.charAt(i) == b.charAt(j - 1)) 0 else 1)
        val h = row1(j - 1) + 1
        val v = row0(j) + 1

        row1(j) = if (d < h) {
          if (v < d) v else d
        } else if (v < h) v else h

        j += 1
      }

      var tmp = row0; row0 = row1; row1 = tmp
      i += 1
    }

    row0(b.length)
  }
}

trait StringInstances0 {
  implicit def levenshteinDistance: MetricSpace[String, Int] = LevenshteinDistance
}

trait StringInstances extends StringInstances0 {
  implicit final val StringAlgebra = new StringMonoid
  implicit final val StringOrder = new StringOrder
}
