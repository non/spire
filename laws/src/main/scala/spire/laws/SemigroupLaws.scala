package spire.laws

import spire.algebra.Semigroup

trait SemigroupLaws[A] {
  implicit def S: Semigroup[A]

  def combineAssociative(x: A, y: A, z: A): IsEq[A] =
    S.combine(S.combine(x, y), z) <=> S.combine(x, S.combine(y, z))

  def combineN1(a: A): IsEq[A] =
    S.combineN(a, 1) <=> a

  def combineN2(a: A): IsEq[A] =
    S.combineN(a, 2) <=> S.combine(a, a)

  def combineN3(a: A): IsEq[A] =
    S.combineN(a, 3) <=> S.combine(S.combine(a, a), a)

  def tryCombine(xs: Vector[A]): IsEq[Option[A]] =
    S.combineAllOption(xs) <=> xs.reduceOption(S.combine)
}

object SemigroupLaws {
  def apply[A](implicit ev: Semigroup[A]): SemigroupLaws[A] =
    new SemigroupLaws[A] { def S: Semigroup[A] = ev }
}
