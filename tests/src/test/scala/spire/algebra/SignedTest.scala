package spire.algebra

import scala.reflect.ClassTag

// scalatest
import org.scalatest.FunSuite

// we need to disable our own === to avoid messing up ScalaTest.
import spire.math.{Rational, Algebraic, Complex}
import spire.implicits.{eqOps => _, _}

// nice alias
import scala.{specialized => spec}

import java.math.MathContext

import spire.math.fpf.FPFilter


class SignedTest extends FunSuite {
  def runWith[@spec(Int, Long, Float, Double) A: Signed: ClassTag](neg: A, pos: A, zero: A) {
    val m = implicitly[ClassTag[A]]

    //// the name to use for this A
    //val cls = m.typeArguments match {
    //  case Nil => m.erasure.getSimpleName
    //  case args => "%s[%s]" format (m.erasure.getSimpleName, args.mkString(","))
    //}

    // the name to use for this A
    val cls = m.runtimeClass.getName

    // test runner which constructs a unique name for each test we run.
    def runTest(name:String)(f: => Unit) = test("%s:%s" format(cls, name))(f)

    runTest("-neg.abs === pos")(assert(neg.abs === pos))
    runTest("pos.abs === pos")(assert(pos.abs === pos))
    runTest("neg.sign == Negative")(assert(neg.sign === Sign.Negative))
    runTest("pos.sign == Positive")(assert(pos.sign === Sign.Positive))
    runTest("zero.sign == Zero")(assert(zero.sign === Sign.Zero))
    runTest("neg.signum < 0")(assert(neg.signum < 0))
    runTest("pos.signum > 0")(assert(pos.signum > 0))
    runTest("zero.signum == 0")(assert(zero.signum === 0))
  }

  runWith[Int](-3, 3, 0)
  runWith[Long](-3, 3, 0)
  runWith[Float](-3, 3, 0)
  runWith[Double](-3, 3, 0)
  runWith[BigInt](-3, 3, 0)
  runWith[BigDecimal](-3, 3, 0)
  runWith[Rational](-3, 3, 0)
  runWith[Algebraic](-3, 3, 0)
  runWith[Complex[Double]](-3, 3, 0)
}
