package spire.matrix.dense.tests.LayeredBLAS3

import spire.matrix.dense.BLAS
import BLAS.LayeredLevel3.{GEBP, TRSBP}
import spire.matrix.{Transposition, UpperOrLower, Sides, DiagonalProperty}
import Transposition._
import UpperOrLower._
import Sides._
import DiagonalProperty._
import spire.matrix.dense.Matrix
import spire.matrix.dense.random._

import spire.matrix.dense.tests.RandomUncorrelatedElements
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import math.abs

trait LayeredGemmTest extends FunSuite {
  type GemmType = (Transposition.Value, Transposition.Value,
                   Double, Matrix, Matrix, Double, Matrix) => Unit
  val referenceGemm: GemmType
  val layeredGemm: GemmType

  //implicit val gen = Defaults.IntegerGenerator.fromTime(System.nanoTime)
  implicit val gen = Defaults.IntegerGenerator.fromTime(1)

  val p2d = new ScalarUniformPowerOfTwoDistribution(minPower=0, maxPower=6)
  val elts = new RandomUncorrelatedElements(nonSpecialScalars = 1,
                                            nonSpecialDimensions=1,
                                            scalars = p2d,
                                            elements = p2d)

  def pretty(trans:Transposition.Value, r:Int, s:Int) =
    s"[$r x $s]${if(trans == Transpose) "^T" else ""}"

  def msg(transA:Transposition.Value, transB:Transposition.Value,
          alpha:Double, beta:Double, m:Int, n:Int, k:Int) =
    s"""|${pretty(transA, m, k)} x ${pretty(transB, k, n)}
        |alpha=$alpha
        |beta=$beta""".stripMargin

  val title = "Layered GEMM shall give the same results as reference GEMM"

  test(s"$title (small matrices)") {
    for {
      (transA, transB, alpha, a, b, beta, c, m, n, k)
          <- elts.matrixProductSample
      cRef = c.copyToMatrix
      cLayered = c.copyToMatrix
    } {
      referenceGemm(transA, transB, alpha, a, b, beta, cRef)
      layeredGemm(transA, transB, alpha, a, b, beta, cLayered)
      Predef.assert(
        cLayered == cRef,
        s"""|Expected $cRef but got $cLayered with
        |${msg(transA, transB, alpha, beta, m, n, k)}
        |""".stripMargin)
    }
  }

  test(s"$title (large matrices)") {
    val blocking = GEBP.blocking.get
    val mc = blocking.mc
    val kc = blocking.kc
    for{
      (m,k,n) <- Iterator((mc+1, kc+1, 5), (mc+mc/2, kc+kc/2, 5))
      a <- elts.generalMatrixSample(m,k).take(1)
      b <- elts.generalMatrixSample(k,n).take(1)
      cRef = Matrix.empty(m,n)
      cLayered = Matrix.empty(m,n)
    } {
      referenceGemm(NoTranspose, NoTranspose, 1.0, a, b, 0.0, cRef)
      layeredGemm(NoTranspose, NoTranspose, 1.0, a, b, 0.0, cLayered)
      Predef.assert(
        cLayered == cRef,
        s"[$m x $k] [$k x $n]: layered GEMM disagrees with reference GEMM")
    }
  }
}

class SerialLayeredGemmTest extends LayeredGemmTest {
  val referenceGemm = BLAS.NaiveLevel3.gemm _
  val layeredGemm = BLAS.LayeredLevel3.SerialGEMM.apply _
}

class EvenPartitionTest extends FunSuite
with GeneratorDrivenPropertyChecks with Matchers {
  import BLAS.LayeredLevel3.ParallelGEMM.partition

  test("Partitions in 4 chunks of 0..n for n = 1 to 10") {
    assertResult { Seq(0, 1)           } { partition( 1,4) }
    assertResult { Seq(0, 1, 2)        } { partition( 2,4) }
    assertResult { Seq(0, 1, 2, 3)     } { partition( 3,4) }
    assertResult { Seq(0, 1, 2, 3,  4) } { partition( 4,4) }
    assertResult { Seq(0, 2, 3, 4,  5) } { partition( 5,4) }
    assertResult { Seq(0, 2, 4, 5,  6) } { partition( 6,4) }
    assertResult { Seq(0, 2, 4, 6,  7) } { partition( 7,4) }
    assertResult { Seq(0, 2, 4, 6,  8) } { partition( 8,4) }
    assertResult { Seq(0, 3, 5, 7,  9) } { partition( 9,4) }
    assertResult { Seq(0, 3, 6, 8, 10) } { partition(10,4) }
  }

  val sizes = Gen.choose(1, 32)
  val chunks = Gen.choose(1, 8)

  test("Partitions shall span elements range") {
    forAll((sizes, "#elements"), (chunks, "#chunks")) { (n:Int, c:Int) =>
      whenever(c > 1 && n > 1) {
        val p = partition(n, c)
        p.head should be (0)
        p.last should be (n)
      }
    }
  }
  test("Chunks shall be properly ordered") {
    forAll((sizes, "#elements"), (chunks, "#chunks")) { (n:Int, c:Int) =>
      whenever(c > 1 && n > 1) {
        val diffs = partition(n, c).sliding(2).map(x => x(1) - x(0)).toList
        all(diffs) should be > 0
      }
    }
  }

  test("Partitions shall be evenly sized") {
    forAll((sizes, "#elements"), (chunks, "#chunks")) { (n:Int, c:Int) =>
      whenever(c > 1 && n > 1) {
        val _2ndDiffs =
          partition(n, c).sliding(3).map(x => abs(x(0) - 2*x(1) + x(2))).toList
        all(_2ndDiffs) should be <= 1
      }
    }
  }

}

class ParallelLayeredGemmTest extends LayeredGemmTest {
  val referenceGemm = BLAS.LayeredLevel3.SerialGEMM.apply _
  val layeredGemm = BLAS.LayeredLevel3.ParallelGEMM.apply _
}

trait TRSBPPackingTest extends FunSuite {
  val kr:Int

  val title = s"With kr=$kr"

  val aa = GEBP.blocking.get.bufferA.start

  def sample(zeroCond:(Int,Int) => Boolean,
             indicesMapping:(Int,Int,Int) => (Int,Int)) =
    (1 to 9).toList.map { n =>
      Matrix.tabulate(n,n)((i:Int, j:Int) => {
        val (k, l) = indicesMapping(n,i,j)
        if(zeroCond(k,l)) 0 else 11 + 10*k + l
      })
    }

  def lower(i:Int, j:Int) = i < j
  def upper(i:Int, j:Int) = i > j
  def canonical(n:Int, i:Int, j:Int) = (i,j)
  def reversed(n:Int, i:Int, j:Int) = (n-1-i, n-1-j)
  def transposed(n:Int, i:Int, j:Int) = (j,i)

  def pack(sample:List[Matrix],
           layOut:(Matrix, Boolean, Int, Long) => Unit,
           kr:Int) = {
    Array(0.0) ::
    sample.map { a =>
      layOut(a, false, kr, aa)
      val n = a.dimensions._1
      GEBP.getArray(aa, n*(n+1)/2).toList
    }
  }

  test("Packing: Lower Triangular, Column Slicing") {
    val packed = pack(sample(lower, canonical),
                      TRSBP.packLowerTriangleAsColumnSlices,
                      kr)
    kr match {
      case 2 => {
        assertResult {
          1.0/11 :: Nil } { packed(1) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: Nil } { packed(2) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 1.0/33 ::
          Nil } { packed(3) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 41.0 :: 42.0 :: 1.0/33 ::
          43 :: 1.0/44 :: Nil } { packed(4) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 41.0 :: 42.0 :: 51.0 ::
          52.0 :: 1.0/33 :: 43.0 :: 1.0/44 :: 53.0 :: 54.0 :: 1.0/55 ::
          Nil } { packed(5) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 41.0 :: 42.0 :: 51.0 ::
          52.0 :: 61.0 :: 62.0 :: 1.0/33 :: 43.0 :: 1.0/44 :: 53.0 :: 54.0 ::
          63.0 :: 64.0 :: 1.0/55 :: 65.0 :: 1.0/66 :: Nil } { packed(6) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 41.0 :: 42.0 :: 51.0 ::
          52.0 :: 61.0 :: 62.0 :: 71.0 :: 72.0 :: 1.0/33 :: 43.0 :: 1.0/44 ::
          53.0 :: 54.0 :: 63.0 :: 64.0 :: 73.0 :: 74.0 :: 1.0/55 :: 65.0 ::
          1.0/66 :: 75.0 :: 76.0 :: 1.0/77 :: Nil } { packed(7) }
      }

      case 4 => {
        assertResult {
          1.0/11 :: Nil } { packed(1) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: Nil } { packed(2) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 1.0/33 :: Nil } { packed(3) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 :: 1.0/33 :: 41.0 :: 42.0 ::
          43.0 :: 1.0/44 :: Nil } { packed(4) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 ::1.0/33 :: 41.0 ::
          42.0 :: 43.0 :: 1.0/44 :: 51.0 :: 52.0 :: 53.0 :: 54.0 ::
          1.0/55 :: Nil } { packed(5) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 ::1.0/33 :: 41.0 ::
          42.0 :: 43.0 :: 1.0/44 :: 51.0 :: 52.0 :: 53.0 :: 54.0 :: 61.0 ::
          62.0 :: 63.0 :: 64.0 :: 1.0/55 :: 65.0 :: 1.0/66 :: Nil} { packed(6) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 ::1.0/33 :: 41.0 ::
          42.0 :: 43.0 :: 1.0/44 :: 51.0 :: 52.0 :: 53.0 :: 54.0 :: 61.0 ::
          62.0 :: 63.0 :: 64.0 :: 71.0 :: 72.0 :: 73.0 :: 74.0 :: 1.0/55 ::
          65.0 :: 1.0/66 :: 75.0 :: 76.0 :: 1.0/77 :: Nil} { packed(7) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 ::1.0/33 :: 41.0 ::
          42.0 :: 43.0 :: 1.0/44 :: 51.0 :: 52.0 :: 53.0 :: 54.0 :: 61.0 ::
          62.0 :: 63.0 :: 64.0 :: 71.0 :: 72.0 :: 73.0 :: 74.0 :: 81.0 ::
          82.0 :: 83.0 :: 84.0 :: 1.0/55 :: 65.0 :: 1.0/66 :: 75.0 :: 76.0 ::
          1.0/77 :: 85.0 :: 86.0 :: 87.0 :: 1.0/88 :: Nil} { packed(8) }
        assertResult {
          1.0/11 :: 21.0 :: 1.0/22 :: 31.0 :: 32.0 ::1.0/33 :: 41.0 ::
          42.0 :: 43.0 :: 1.0/44 :: 51.0 :: 52.0 :: 53.0 :: 54.0 :: 61.0 ::
          62.0 :: 63.0 :: 64.0 :: 71.0 :: 72.0 :: 73.0 :: 74.0 :: 81.0 ::
          82.0 :: 83.0 :: 84.0 :: 91.0 :: 92.0 :: 93.0 :: 94.0 :: 1.0/55 ::
          65.0 :: 1.0/66 :: 75.0 :: 76.0 :: 1.0/77 :: 85.0 :: 86.0 :: 87.0 ::
          1.0/88 :: 95.0 :: 96.0 :: 97.0 :: 98.0 :: 1.0/99 ::
          Nil} { packed(9) }
      }

      case _ =>
    }
  }

  test("Colunm slicing: from upper to lower triangular") {
    val canons = pack(sample(upper, canonical),
                      TRSBP.packUpperTriangleAsColumnSlices, kr)
    val alternatives = pack(sample(upper, reversed),
                            TRSBP.packLowerTriangleAsColumnSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("Colunm slicing: from lower to upper triangular") {
    val canons = pack(sample(lower, canonical),
                      TRSBP.packLowerTriangleAsColumnSlices, kr)
    val alternatives = pack(sample(lower, reversed),
                            TRSBP.packUpperTriangleAsColumnSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("Row slicing: from upper to lower triangular") {
    val canons = pack(sample(upper, canonical),
                      TRSBP.packUpperTriangleAsRowSlices, kr)
    val alternatives = pack(sample(upper, reversed),
                            TRSBP.packLowerTriangleAsRowSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("Row slicing: from lower to upper triangular") {
    val canons = pack(sample(lower, canonical),
                      TRSBP.packLowerTriangleAsRowSlices, kr)
    val alternatives = pack(sample(lower, reversed),
                            TRSBP.packUpperTriangleAsRowSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("From upper triangular row slicing to lower triangular column slicing")
  {
    val canons = pack(sample(upper, canonical),
                      TRSBP.packUpperTriangleAsRowSlices, kr)
    val alternatives = pack(sample(upper, transposed),
                            TRSBP.packLowerTriangleAsColumnSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("From lower triangular column slicing to upper triangular row slicing")
  {
    val canons = pack(sample(lower, canonical),
                      TRSBP.packLowerTriangleAsColumnSlices, kr)
    val alternatives = pack(sample(lower, transposed),
                            TRSBP.packUpperTriangleAsRowSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("From lower triangular row slicing to upper triangular column slicing")
  {
    val canons = pack(sample(lower, canonical),
                      TRSBP.packLowerTriangleAsRowSlices, kr)
    val alternatives = pack(sample(lower, transposed),
                            TRSBP.packUpperTriangleAsColumnSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }

  test("From upper triangular column slicing to lower triangular row slicing")
  {
    val canons = pack(sample(upper, canonical),
                      TRSBP.packUpperTriangleAsColumnSlices, kr)
    val alternatives = pack(sample(upper, transposed),
                            TRSBP.packLowerTriangleAsRowSlices, kr)
    for((canon, alternative) <- canons.zip(alternatives)) {
      assert(canon === alternative)
    }
  }
}

class TRSBPPackingTest2 extends TRSBPPackingTest { val kr = 2 }
class TRSBPPackingTest4 extends TRSBPPackingTest { val kr = 4 }

class LayeredTrsmTest extends FunSuite {
  import BLAS.NaiveLevel3.{trsm => referenceTrsm}
  import BLAS.LayeredLevel3.{trsm => layeredTrsm}
  //implicit val gen = Defaults.IntegerGenerator.fromTime(System.nanoTime)
  implicit val gen = Defaults.IntegerGenerator.fromTime(1)

  val title = "Layered TRSM shall give the same results as reference TRSM"

  def diff(a:Matrix, b:Matrix) = {
    a.zip(b).map((xy:(Double, Double)) => {
      val (x,y) = xy
      val s = x.abs + y.abs
      if(s != 0) (x - y).abs/s else 0
    }).max
  }

  def msg(side:Sides.Value, uplo:UpperOrLower.Value,
          trans:Transposition.Value, diag:DiagonalProperty.Value,
          alpha:Double, m:Int, n:Int) = {
    val a = if (uplo == Lower) "L" else "U"
    val at = if(trans == Transpose) s"$a^T" else s"$a"
    val lhs = if(side == FromLeft) s"$at X" else s"X $at"
    val p = if(side == FromLeft) m else n
    val unit = if(diag == UnitDiagonal) "(unit diag.)" else ""
    s"""|$lhs = alpha B
        |A: $p x $p $unit
        |X and B: $m x $n
        |alpha = $alpha
     """.stripMargin
  }

  val p2d = new ScalarUniformDistributionFromMinusOneToOne()
  val elts = new RandomUncorrelatedElements(nonSpecialScalars = 1,
                                            nonSpecialDimensions=1,
                                            scalars = p2d,
                                            elements = p2d)

  test(s"$title (small matrices)") {
    for {
      (side, uplo, trans, diag, alpha, a, b, m, n)
        <- elts.triangularSystemSample
      xRef = b.copyToMatrix
      xLayered = b.copyToMatrix
    } {
      referenceTrsm(side, uplo, trans, diag, alpha, a, xRef)
      layeredTrsm(side, uplo, trans, diag, alpha, a, xLayered)
      assert(diff(xLayered, xRef) < 0.001,
             s"""|Expected ${xRef.formatted("%.18f")}
                 |but got ${xLayered.formatted("%.18f")} with
                 |${msg(side, uplo, trans, diag, alpha, m, n)}
                 |a=${a.formatted("%.8f", true)}
                 |b=${b.formatted("%.8f", true)}
                 |~~~
                 |""".stripMargin)
    }
  }

  test(s"$title (large matrices)") {
    import BLAS.LayeredLevel3._
    val blockingForGEBP = GEBP.blocking.get()
    val mc = blockingForGEBP.mc
    val blocking = GEBP.blocking.get()
    val kc = blocking.kc
    for{
      (m,n) <- Iterator((mc+1, kc+1), (mc+mc/2, kc+kc/2))
      side <- Iterator(FromLeft, FromRight)
      uplo <- Iterator(Lower, Upper)
      trans <- Iterator(NoTranspose, Transpose)
      diag = UnitDiagonal
      a <- elts.triangularMatrixSample(if(side == FromLeft) m else n, uplo, diag
                                       ).take(1)
      b <- elts.generalMatrixSample(m,n).take(1)
      xRef = b.copyToMatrix
      xLayered = b.copyToMatrix
      alpha = -1.0
    } {
      referenceTrsm(side, uplo, trans, diag, alpha, a, xRef)
      layeredTrsm(side, uplo, trans, diag, alpha, a, xLayered)
      assert(diff(xLayered, xRef) < 0.01,
             s"""|Layered TRSM disagrees with reference TRSM for
                 |${msg(side, uplo, trans, diag, alpha, m, n)}""".stripMargin)
    }
  }
}
