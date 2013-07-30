package spire.matrix.dense.FunSuite

import spire.matrix.BLAS
import spire.matrix.Constants._
import spire.matrix.dense._

import scala.math.sqrt
import org.scalatest.FunSuite

trait ElementaryReflectorTestLike extends FunSuite
with EuclideanNorm
{
  val ElementaryReflector: ElementaryReflectorLikeCompanion

  test("Construction: exact") {
    val m = Matrix(2,4)(3,   1,  5,  1,
                        21, 22, 23, 24)
    val h = ElementaryReflector.annihilateAndConstruct(m.row(0))
    expectResult(1.5) { h.tau }
    expectResult(1.0/9 :: 5.0/9 :: 1.0/9 :: Nil) { h.essentialPart.toList }
    expectResult(-6) { m.row(0)(0) }
    expectResult(h.essentialPart) { m.row(0).block(1,End) }
    expectResult(21 :: 22 :: 23 :: 24 :: Nil) { m.row(1).toList }
  }

  test("Construction: tiny vector") {
    import ElementaryReflector.safeMin
    val eta = safeMin/8
    val m = Matrix(5,2)(-2*eta, -2,
                           eta,  1,
                          -eta, -1,
                         2*eta,  2,
                           eta,  1)
    // let's make sure we got our test case right,
    // i.e. such that the branch `if(beta.abs < safeMin)` is taken
    // in `trait HouseholderReflectionLike` constructor.
    val norm = euclideanNorm(m.column(0))
    assert(0 < norm && norm < safeMin)

    // the test itself
    val h0 = ElementaryReflector.annihilateAndConstruct(m.column(0))
    val h1 = ElementaryReflector.annihilateAndConstruct(m.column(1))
    assert(h0.tau === h1.tau)
    assert(m.column(0).block(1,End).toList === m.column(1).block(1,End).toList)
    assert(m(0,0) === m(0,1)*eta)
  }

  test("Product with a general matrix from the left") {
    val v = Vector(-1, -2, 2)
    val tau = 1.5
    val h = ElementaryReflector(tau, v)
    val m = Matrix(4, 7)(-1, 1, 0, 0, -3, -2, 0,
                         0, -1, -3, 1, 2, 1, 0,
                         1, -2, -2, -2, 0, -2, -1,
                         -2, 0, -3, -3, 2, 3, 0)
    WorkingArea.reserve(7)
    val expected = Matrix(4,7)( 9.5, -8.0, -1.5, 4.5, -1.5, -12.5, -3.0,
                               -10.5, 8.0, -1.5, -3.5, 0.5, 11.5, 3.0,
                               -20.0, 16.0, 1.0, -11.0, -3.0, 19.0, 5.0,
                               19.0, -18.0, -6.0, 6.0, 5.0, -18.0, -6.0)
    h.applyOnLeft(m)
    expectResult(expected) { m }
  }

  test("Product with a general matrix from the right") {
    val v = Vector(2, -3, -2, 0, -2, -3)
    val tau = 1.25
    val h = ElementaryReflector(tau, v)
    val m = Matrix(4, 7)(-2, -2, 3, 3, -1, 2, -2,
                         -3, -3, -2, -1, -3, 0, 3,
                         -3, 0, 0, -2, -3, 2, -2,
                          3, 1, 3, 1, 1, 0, 0)
    WorkingArea.reserve(4)
    val expected = Matrix(4,7)(21.75, 45.5, -68.25, -44.5, -1.0, -45.5, -73.25,
                               9.5, 22.0, -39.5, -26.0, -3.0, -25.0, -34.5,
                               -6.75, -7.5, 11.25, 5.5, -3.0, 9.5, 9.25,
                               10.5, 16.0, -19.5, -14.0, 1.0, -15.0, -22.5)
    h.applyOnRight(m)
    expectResult(expected) { m }
  }

  test("Product from the right or left with trailing zeros") {
    val m0 = Matrix(5,6)(3, 1, 1, 0, 0, 0,
                         2, 2, 1, 0, 0, 0,
                         4, 2, 4, 0, 0, 0,
                         4, 5, 5, 1, 1, 1,
                         5, 3, 3, 5, 1, 1)
    val v = Vector(3, 0, 0, 0)
    val tau = 1.5
    val h = ElementaryReflector(tau, v)

    WorkingArea.reserve(math.max(m0.dimensions._1, m0.dimensions._2))

    val m1 = m0.copyToMatrix
    val v1 = Vector(3, 0, 0, 0, 0)
    val h1 = ElementaryReflector(tau, v1)
    h1.applyOnLeft(m1)
    val expected = Matrix(5,6)(-10.5,  -9.5,  -5.0, 0.0, 0.0, 0.0,
                               -38.5, -29.5, -17.0, 0.0, 0.0, 0.0,
                                 4.0,   2.0,   4.0, 0.0, 0.0, 0.0,
                                 4.0,   5.0,   5.0, 1.0, 1.0, 1.0,
                                 5.0,   3.0,   3.0, 5.0, 1.0, 1.0)
    expectResult(expected) { m1 }

    val m2 = m0.transposed
    h.applyOnRight(m2)
    expectResult(expected.transposed) { m2 }
  }
}

class ElementaryReflectorWithNaiveBLASTest extends ElementaryReflectorTestLike
{
  val ElementaryReflector = ElementaryReflectorWithNaiveBLAS
}
