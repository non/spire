package spire.matrix.dense.random

import spire.matrix.dense._
import spire.matrix.Sides
import Sides._
import spire.matrix.dense.BLAS
import spire.random
import spire.implicits._
import java.lang.Math.{signum, copySign}

object Defaults {
  type IntegerGenerator = spire.random.mutable.Well512
  val IntegerGenerator = spire.random.mutable.Well512
}

/**
 * Bernoulli distribution
 *
 * Its sample space is {true, false} with the given probability for true.
 */
class BernoulliDistribution(val probabilityOfTrue:Double)(
  implicit private val gen:Defaults.IntegerGenerator)
extends Iterator[Boolean]
{
  require(0 <= probabilityOfTrue && probabilityOfTrue <=1)
  val min = false
  val max = true
  def hasNext = true
  def next: Boolean =
    if(probabilityOfTrue == 0) false else gen.nextDouble <= probabilityOfTrue
}

/**
 * Random scalars distributed according to some distribution
 */
abstract class ScalarDistribution(
  implicit private val gen: Defaults.IntegerGenerator)
extends Iterator[Double] {
  val min, max: Double
  def hasNext = true
}

/**
 * Random scalars uniformly distributed in the interval [min, max)
 */
class ScalarUniformDistribution(val min:Double, val max:Double)(
  implicit gen: Defaults.IntegerGenerator)
extends ScalarDistribution {
  def next = gen.nextDouble(min, max)
}

/**
 * Random scalars uniformly distributed in the interval [0, 1)
 */
class ScalarUniformDistributionFromZeroToOne(
  implicit gen: Defaults.IntegerGenerator)
extends ScalarDistribution {
  val min = 0.0
  val max = 1.0
  def next = gen.nextDouble
}

/**
 * Random scalars uniformly distributed in the interval [-1, 1)
 */
class ScalarUniformDistributionFromMinusOneToOne(
  implicit gen: Defaults.IntegerGenerator)
extends ScalarDistribution {
  val min = -1.0
  val max =  1.0
  def next = 2*gen.nextDouble - 1
}

/**
 * Random scalars normally distributed
 */
class ScalarNormalDistribution(
  implicit gen: Defaults.IntegerGenerator)
extends ScalarDistribution {
  val min = java.lang.Double.MIN_VALUE
  val max = java.lang.Double.MAX_VALUE
  def next = gen.nextGaussian
}

/**
 * Random variable 2^X^ where X is a uniform integer random variable
 */
class ScalarUniformPowerOfTwoDistribution(minPower:Int, maxPower:Int)(
  implicit gen: Defaults.IntegerGenerator)
extends ScalarDistribution {
  val min = 2.0**minPower
  val max = 2.0**maxPower
  val sign = new BernoulliDistribution(0.5)
  def next = {
    val a = 2.0**gen.nextInt(minPower, maxPower)
    if(sign.next) a else -a
  }
}

/**
 * The Haar distribution for orthogonal matrices of dimension n.
 *
 * In the following, U will always refer to a matrix drawn from this
 * distribution. Every method of this class has a base runtime cost of
 * O(n^2^) random numbers generated from a normal distribution,
 * and a base storage cost of O(n).
 *
 * An economical method [1, theorem 3.3] to generate such a matrix U is used:
 * a sequence of n-1 elementrary reflections H,,0,,, H,,1,,, ..., H,,n-2,,
 * is generated such that U = D H,,0,, H,,1,, ... H,,n-2,,, for a well chosen
 * diagonal matrix D, but this product is only computed
 * if the matrix U is requested, and only one elementary reflection
 * is stored at a time.
 *
 * Refrence: subroutine DLAROR from LAPACK [2]
 *
 * Note: subroutine DLARGE does also deal with random orthogonal matrices
 * using the same method but it leaves out the multiplication by the matrix D
 * and therefore the distribution is not the Haar measure. As a result, the
 * eigenvalues are not neatly distributed on the circle of radius 1 centered
 * on 0 in the complex plane.
 *
 * [1] G.W. Stewart, The efficient generation of random orthogonal matrices
 *     with an application to condition estimators,
 *     SIAM Journal on Numerical Analysis 17 (1980), no. 3, 403–409.
 *
 * [2]
 */
class OrthogonalMatricesHaarDistribution(val l:Int)
                                        (implicit gen: Defaults.IntegerGenerator,
                                         implicit val work: Scratchpad)
extends Iterator[Matrix] with BLAS.NaiveLevel1 {

  val ElementaryReflector = ElementaryReflectorWithNaiveBLAS

  private val elements = new ScalarNormalDistribution
  private val v = Vector.zero(l)
  private val r = Vector.zero(l)

  /**
   * Overwrite A with U A, A U or U A U^T^
   *
   * The runtime cost is O(n^3^) on the top of the base runtime cost
   * whereas there is no extra storage cost.
   */
  def overwriteWithProductByNext(sides:Sides.Value, a:Matrix):Unit = {
    val (m,n) = a.dimensions
    sides match {
      case FromLeft => {
        require(m == l)
        cforRange(m-2 to 0 by -1) { i =>
          val u = v.block(0,m-i)
          u := elements
          val h = ElementaryReflector.annihilateAndConstruct(u)
          r(i) = signum(u(0))
          h.applyOnLeft(a.block(i,m)(0,n))
        }
        r(m-1) = signum(elements.next)
        cforRange(0 until m) { i => scale(r(i), a.row(i)) }
      }
      case FromRight => {
        require(n == l)
        cforRange(n-2 to 0 by -1) { j =>
          val u = v.block(0,n-j)
          u := elements
          val h = ElementaryReflector.annihilateAndConstruct(u)
          r(j) = signum(u(0))
          h.applyOnRight(a.block(0,m)(j,n))
        }
        r(n-1) = signum(elements.next)
        cforRange(0 until n) { j => scale(r(j), a.column(j)) }
      }
      case Congruent => {
        require(m == l && n == l)
        cforRange(l-2 to 0 by -1) { k =>
          val u = v.block(0,l-k)
          u := elements
          val h = ElementaryReflector.annihilateAndConstruct(u)
          r(k) = signum(u(0))
          h.applyOnLeft(a.block(k,l)(0,l))
          h.applyOnRight(a.block(0,l)(k,l))
        }
        r(l-1) = signum(elements.next)
        cforRange(0 until l) { i => scale(r(i), a.row(i)) }
        cforRange(0 until l) { j => scale(r(j), a.column(j)) }
      }
    }
  }

  def hasNext = true

  def next: Matrix = {
    val result = Matrix.identity(l)
    overwriteWithProductByNext(FromLeft, result)
    result
  }
}

object OrthogonalMatricesHaarDistribution {
  def apply(n:Int) = {
    val gen = Defaults.IntegerGenerator.fromTime(System.nanoTime)
    val work = new Scratchpad(minimumScratchpad(n))
    new OrthogonalMatricesHaarDistribution(n)(gen, work)
  }

  def minimumScratchpad(n:Int) = ScratchpadSpecs(vectorLength = n)
}
