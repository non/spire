package spire.math

case class ApproximationContext[A](error: A)
object ApproximationContext {
  implicit def rational2error(q: Rational) = ApproximationContext(q)
}
