package spire.algebra

import scala.{specialized => spec}

/**
 * The `Order` type class is used to define a total ordering on some type `A`.
 */
trait Order[@spec A] extends Eq[A] {
  self =>

  def eqv(x: A, y: A): Boolean = compare(x, y) == 0
  def gt(x: A, y: A): Boolean = compare(x, y) > 0
  def lt(x: A, y: A): Boolean = compare(x, y) < 0
  def gteqv(x: A, y: A): Boolean = compare(x, y) >= 0
  def lteqv(x: A, y: A): Boolean = compare(x, y) <= 0

  def min(x: A, y: A): A = if (lt(x, y)) x else y
  def max(x: A, y: A): A = if (gt(x, y)) x else y
  def compare(x: A, y: A): Int

  /**
   * Defines an order on `B` by mapping `B` to `A` using `f` and using `A`s
   * order to order `B`.
   */
  override def on[@spec B](f: B => A): Order[B] = new MappedOrder(this)(f)

  /**
   * Defines an ordering on `A` where all arrows switch direction.
   */
  def reverse: Order[A] = new ReversedOrder(this)
}

private[algebra] class MappedOrder[@spec A, @spec B](order: Order[B])(f: A => B) extends Order[A] {
  def compare(x: A, y: A) = order.compare(f(x), f(y))
}

private[algebra] class ReversedOrder[@spec A](order: Order[A]) extends Order[A] {
  def compare(x: A, y: A) = order.compare(y, x)
}

object Order {
  @inline final def apply[A](implicit o: Order[A]) = o

  def by[@spec A, @spec B](f: A => B)(implicit o: Order[B]): Order[A] = o.on(f)

  def from[@spec A](f: (A, A) => Int): Order[A] = new Order[A] {
    def compare(x: A, y: A) = f(x, y)
  }

  implicit def ordering[A](implicit o: Order[A]) = new Ordering[A] {
    def compare(x: A, y: A) = o.compare(x, y)
  }
}
