package spire.laws.shadows

import spire.algebra.{AdditiveCMonoid, Eq}

trait ShadowAdditiveCMonoid[A, S] extends AdditiveCMonoid[Shadow[A, S]] with ShadowAdditiveCSemigroup[A, S] {
  import shadowing._
  implicit def A: AdditiveCMonoid[A]
  implicit def S: AdditiveCMonoid[S]
  implicit def eqA: Eq[A]
  implicit def eqS: Eq[S]

  def zero: Shadow[A, S] = Shadow(A.zero, checked(S.zero))

  override def isZero(x: Shadow[A, S])(implicit ev: Eq[Shadow[A, S]]) = {
    val a = A.isZero(x.a)
    val s = S.isZero(x.s)
    assert(a == s)
    a
  }

  override def sum(xs: TraversableOnce[Shadow[A, S]]): Shadow[A, S] = {
    val seq = xs.toSeq
    val a = A.sum(seq.map(_.a))
    val s = S.sum(seq.map(_.s))
    Shadow(a, checked(s))
  }
}
