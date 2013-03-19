package spire.stats

import language.higherKinds

import scala.collection.GenTraversable

import spire.math.Fractional

object fractional {
  implicit class FractionalMeansMA[M[_],A](val xs: M[A]) extends AnyVal {
    def arithmeticMean(implicit fmeans: FractionalMeans[M], frac: Fractional[A]): A = fmeans.arithmeticMean(xs)
    def arithmeticMeanBy[B](f: A ⇒ B)(implicit fmeans: FractionalMeans[M], frac: Fractional[B]): B = fmeans.arithmeticMeanBy(xs)(f)

    def geometricMean(implicit fmeans: FractionalMeans[M], frac: Fractional[A]): A = fmeans.geometricMean(xs)
    def geometricMeanBy[B](f: A ⇒ B)(implicit fmeans: FractionalMeans[M], frac: Fractional[B]): B = fmeans.geometricMeanBy(xs)(f)

    def harmonicMean(implicit fmeans: FractionalMeans[M], frac: Fractional[A]): A = fmeans.harmonicMean(xs)
    def harmonicMeanBy[B](f: A ⇒ B)(implicit fmeans: FractionalMeans[M], frac: Fractional[B]): B = fmeans.harmonicMeanBy(xs)(f)

    def quadraticMean(implicit fmeans: FractionalMeans[M], frac: Fractional[A]): A = fmeans.quadraticMean(xs)
    def quadraticMeanBy[B](f: A ⇒ B)(implicit fmeans: FractionalMeans[M], frac: Fractional[B]): B = fmeans.quadraticMeanBy(xs)(f)
  }
}

trait FractionalMeans[M[_]] {
  def arithmeticMean[A:Fractional](xs: M[A]): A
  def arithmeticMeanBy[A,B:Fractional](xs: M[A])(f: A ⇒ B): B

  def geometricMean[A:Fractional](xs: M[A]): A
  def geometricMeanBy[A,B:Fractional](xs: M[A])(f: A ⇒ B): B

  def harmonicMean[A:Fractional](xs: M[A]): A
  def harmonicMeanBy[A,B:Fractional](xs: M[A])(f: A ⇒ B): B

  def quadraticMean[A:Fractional](xs: M[A]): A
  def quadraticMeanBy[A,B:Fractional](xs: M[A])(f: A ⇒ B): B
}

trait FractionalMeans0 {
  implicit def GenTraversableMeans[CC[X] <: GenTraversable[X]]: FractionalMeans[CC] = new GenTraversableFractionalMeans[CC] {}
}

object FractionalMeans extends FractionalMeans0 {
  implicit object ArrayMeans extends ArrayFractionalMeans
}

trait ArrayFractionalMeans extends FractionalMeans[Array] {
  def arithmeticMean[A](xs: Array[A])(implicit num: Fractional[A]): A = {
    val sum = xs.foldLeft(num.zero)(num.plus)
    num.div(sum, num.fromInt(xs.length))
  }

  def arithmeticMeanBy[A,B](xs: Array[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val sum = xs.foldLeft(num.zero)((b,a) ⇒ num.plus(b, f(a)))
    num.div(sum, num.fromInt(xs.length))
  }

  def geometricMean[A](xs: Array[A])(implicit num: Fractional[A]): A = {
    val prod = xs.foldLeft(num.one)(num.times)
    num.fpow(prod, num.reciprocal(num.fromInt(xs.length)))
  }

  def geometricMeanBy[A,B](xs: Array[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val prod = xs.foldLeft(num.one)((b,a) ⇒ num.times(b, f(a)))
    num.fpow(prod, num.reciprocal(num.fromInt(xs.length)))
  }

  def harmonicMean[A](xs: Array[A])(implicit num: Fractional[A]): A = {
    val sum = xs.foldLeft(num.zero)((agg,a) ⇒ num.plus(agg, num.reciprocal(a)))
    num.div(num.fromInt(xs.length), sum)
  }

  def harmonicMeanBy[A,B](xs: Array[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val sum = xs.foldLeft(num.zero)((b,a) ⇒ num.plus(b, num.reciprocal(f(a))))
    num.div(num.fromInt(xs.length), sum)
  }

  def quadraticMean[A](xs: Array[A])(implicit num: Fractional[A]): A = {
    val sum = xs.foldLeft(num.zero)((agg,a) ⇒ num.plus(agg, num.times(a,a)))
    num.sqrt(num.div(sum, num.fromInt(xs.length)))
  }

  def quadraticMeanBy[A,B](xs: Array[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val sum = xs.foldLeft(num.zero)((b,a) ⇒ num.plus(b, num.pow(f(a),2)))
    num.sqrt(num.div(sum, num.fromInt(xs.length)))
  }
}

trait GenTraversableFractionalMeans[CC[X] <: GenTraversable[X]] extends FractionalMeans[CC] {
  def arithmeticMean[A](xs: CC[A])(implicit num: Fractional[A]): A = {
    val sum = xs.aggregate(num.zero)(num.plus, num.plus)
    num.div(sum, num.fromInt(xs.size))
  }

  def arithmeticMeanBy[A,B](xs: CC[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val sum = xs.aggregate(num.zero)((b,a) ⇒ num.plus(b, f(a)), num.plus)
    num.div(sum, num.fromInt(xs.size))
  }

  def geometricMean[A](xs: CC[A])(implicit num: Fractional[A]): A = {
    val prod = xs.aggregate(num.one)(num.times, num.times)
    num.fpow(prod, num.reciprocal(num.fromInt(xs.size)))
  }

  def geometricMeanBy[A,B](xs: CC[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val prod = xs.aggregate(num.one)((b,a) ⇒ num.times(b, f(a)), num.times)
    num.fpow(prod, num.reciprocal(num.fromInt(xs.size)))
  }

  def harmonicMean[A](xs: CC[A])(implicit num: Fractional[A]): A = {
    val sum = xs.aggregate(num.zero)((agg,a) ⇒ num.plus(agg, num.reciprocal(a)), num.plus)
    num.div(num.fromInt(xs.size), sum)
  }

  def harmonicMeanBy[A,B](xs: CC[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val sum = xs.aggregate(num.zero)((b,a) ⇒ num.plus(b, num.reciprocal(f(a))), num.plus)
    num.div(num.fromInt(xs.size), sum)
  }

  def quadraticMean[A](xs: CC[A])(implicit num: Fractional[A]): A = {
    val sum = xs.aggregate(num.zero)((agg,a) ⇒ num.plus(agg, num.times(a,a)), num.plus)
    num.sqrt(num.div(sum, num.fromInt(xs.size)))
  }

  def quadraticMeanBy[A,B](xs: CC[A])(f: A ⇒ B)(implicit num: Fractional[B]): B = {
    val sum = xs.aggregate(num.zero)((b,a) ⇒ num.plus(b, num.pow(f(a),2)), num.plus)
    num.sqrt(num.div(sum, num.fromInt(xs.size)))
  }
}
