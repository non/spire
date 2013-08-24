package spire.std

import spire.algebra._

@SerialVersionUID(0L)
class OptionMonoid[A: Semigroup] extends Monoid[Option[A]] with Serializable {
  def id: Option[A] = None
  def op(x: Option[A], y: Option[A]): Option[A] = (x, y) match {
    case (Some(x), Some(y)) => Some(Semigroup[A].op(x, y))
    case (None, None) => None
    case (x, None) => x
    case (None, y) => y
  }
}

@SerialVersionUID(0L)
class OptionEq[A: Eq] extends Eq[Option[A]] with Serializable {
  def eqv(x: Option[A], y: Option[A]) = (x, y) match {
    case (Some(x), Some(y)) => Eq[A].eqv(x, y)
    case (None, None) => true
    case _ => false
  }
}

@SerialVersionUID(0L)
class OptionOrder[A: Order] extends OptionEq[A] with Order[Option[A]] with Serializable {
  override def eqv(x: Option[A], y: Option[A]) = (x, y) match {
    case (Some(x), Some(y)) => Eq[A].eqv(x, y)
    case (None, None) => true
    case _ => false
  }

  def compare(x: Option[A], y: Option[A]): Int = {
    (x, y) match {
      case (None, None) => 0
      case (None, Some(_)) => -1
      case (Some(_), None) => 1
      case (Some(x0), Some(y0)) => Order[A].compare(x0, y0)
    }
  }
}

trait OptionInstances0 {
  implicit def OptionEq[A: Eq] = new OptionEq[A]
}

trait OptionInstances extends OptionInstances0 {
  implicit def OptionMonoid[A: Semigroup] = new OptionMonoid[A]

  implicit def OptionOrder[A: Order] = new OptionOrder[A]
}
