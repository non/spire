package spire.benchmark

import spire.matrix.dense.Matrix
import spire.matrix.dense.random.{
  Defaults,
  ScalarUniformDistributionFromMinusOneToOne
}
import spire.matrix.dense.BLAS
import BLAS._
import spire.matrix.{Sides, UpperOrLower, Transposition, DiagonalProperty}
import Sides._
import UpperOrLower._
import Transposition._
import DiagonalProperty._

import spire.matrix.dense.tests.RandomUncorrelatedElements

import spire.implicits._
import scala.math
import scala.collection.mutable.ArrayBuffer

import org.jblas
import com.github.fommil.netlib

class MatrixTimer {
  def bench(flops:Long,
            trashCaches: => Unit,
            benched: => Matrix): (Matrix, Array[Double]) = {
    var totalFlops = 0L
    var runs = 0
    var durations = new ArrayBuffer[Long]
    var result: Matrix = null
    while(totalFlops < 1e9) {
      trashCaches
      val start = System.nanoTime
      result = benched
      durations += System.nanoTime - start
      totalFlops += flops
      runs += 1
    }
    (result, durations.map(flops.toDouble/_).toArray.sorted)
  }
}

trait MatrixBenchmark {
  def header:String
  type Arguments
  def trashCaches(args:Arguments): Unit
  def benched(args:Arguments): Matrix
  def flops(args:Arguments): Long

  def formatTitle(h:String) = "%14s".format(h)
  def formatedHeader = formatTitle(header)

  def formatedTiming(args:Arguments)(implicit timer:MatrixTimer) = {
    val (result, gflops) =
      timer.bench(flops(args), trashCaches(args), benched(args))
    val n = gflops.size
    val (q1, q2, q3) = (gflops(n/4), gflops(n/2), gflops(3*n/4))
    "%5.2f +/- %4.2f".format(q2, (q3-q1)/1.349)
  }
}

trait GemmBenchmark extends MatrixBenchmark {
  type Arguments = (Matrix, Matrix, Matrix)

  def trashCaches(args:Arguments) {
    val (a, b, c) = args
    val (m,n) = c.dimensions
    val k = a.dimensions._2
    a(m/2,k/2) = (b(0,0) + a(m-1,k-1))/2
    b(k/2,n/2) = (a(0,0) + b(k-1,n-1))/2
    c(m/2,n/2) = (c(0,0) + c(m-1,n-1))/2
  }

  def flops(args:Arguments) = {
    val (a, b, c) = args
    val (m,n) = c.dimensions
    val k = a.dimensions._2
    (2L*k-1)*m*n
  }
}

trait SpireGemmBenchmark extends GemmBenchmark {
  type GemmType = (Transposition.Value, Transposition.Value,
                   Double, Matrix, Matrix, Double, Matrix) => Unit
  val gemm:GemmType
  def benched(args:Arguments) = {
    val (a, b, c) = args
    gemm(NoTranspose, NoTranspose, 2.0, a, b, 1.0, c)
    c
  }
}

trait SpireNaiveBenchmark extends MatrixBenchmark {
  def header = "spire (naive)"
  val gemm = BLAS.NaiveLevel3.gemm _
  val trsm = BLAS.NaiveLevel3.trsm _
}

trait SpireLayeredBenchmark extends MatrixBenchmark {
  def header = "spire (layered)"
  val gemm = BLAS.LayeredLevel3.SerialGEMM.apply _
  val trsm = BLAS.LayeredLevel3.trsm _
}

trait SpireParallelLayeredBenchmark extends MatrixBenchmark {
  def header = "spire (layered, //)"
  val gemm = BLAS.LayeredLevel3.ParallelGEMM.apply _
}

class JBlasGemmBenchmark extends GemmBenchmark {
  val header = "jblas"
  def benched(args:Arguments) = {
    val (a, b, c) = args
    val (m,n) = c.dimensions
    val k = a.dimensions._2
    jblas.NativeBlas.dgemm('N', 'N',
                           m, n, k, 2.0, a.elements, 0, m, b.elements, 0, k,
                           1.0, c.elements, 0, m)
    c
  }
}

class NetlibJavaGemmBenchmark extends GemmBenchmark {
  val header = "netlib-java"
  val blas = netlib.BLAS.getInstance()
  def benched(args:Arguments) = {
    val (a, b, c) = args
    val (m,n) = c.dimensions
    val k = a.dimensions._2
    blas.dgemm("N", "N",
               m, n, k, 2.0, a.elements, 0, m, b.elements, 0, k,
               1.0, c.elements, 0, m)
    c
  }
}

trait BenchmarkGenerator {
  implicit val gen = Defaults.IntegerGenerator.fromTime(System.nanoTime)
  val uniform = new ScalarUniformDistributionFromMinusOneToOne
  val elts = new RandomUncorrelatedElements(elements=uniform)
}

object MatrixMultiplicationBenchmarks extends BenchmarkGenerator {

  def dimensions(minPowerOf2:Int, maxPowerOf2:Int) = for {
      Seq(k,l) <- (minPowerOf2 to maxPowerOf2).map(2**_).sliding(2)
      n <- Seq(k, (k+l)/2)
      dims <- Seq((n, n, n), (n, 2*n, n))
    } yield dims

  def main(args:Array[String]) {
    val (lo, hi) = if(args.size == 0) (1, 8)
                   else (args(0).toInt, args(1).toInt)
    println("Gflops/s for product of two general matrices")
    println("--------------------------------------------")
    val dimsHeader = "%4s  %4s  %4s".format("m", "k", "n")
    val benchmarks = new JBlasGemmBenchmark ::
                     new NetlibJavaGemmBenchmark ::
                     new SpireGemmBenchmark with SpireNaiveBenchmark ::
                     new SpireGemmBenchmark with SpireLayeredBenchmark ::
                     new SpireGemmBenchmark with SpireParallelLayeredBenchmark ::
                     Nil
    val benchmarksHeader = benchmarks.map(_.formatedHeader).mkString("  ")
    println(dimsHeader ++ "  " ++ benchmarksHeader)

    implicit val timer = new MatrixTimer

    for {
      (m,k,n) <- dimensions(lo, hi)
      a <- elts.generalMatrixSample(m,k).take(1)
      b <- elts.generalMatrixSample(k,n).take(1)
      c = Matrix.empty(m, n)
    } {
      println("%4d  %4d  %4d".format(m,k,n) ++ "  " ++
              benchmarks.map(_.formatedTiming((a, b, c))).mkString("  "))
    }
  }
}

trait TrsmBenchmark extends MatrixBenchmark {
  type Arguments = (Matrix, Matrix)
  def topHeader:String
  def formattedTopHeader = formatTitle(topHeader)
  val side: Sides.Value
  val uplo: UpperOrLower.Value
  val trans: Transposition.Value
  val diag: DiagonalProperty.Value

  def trashCaches(args:Arguments) {
    val (a, b) = args
    val (m,n) = b.dimensions
    val p = a.dimensions._1
    a(p/2,p/2) = (b(0,0) + a(p-1,p-1))/2
    b(m/2,n/2) = (a(0,0) + b(m-1,n-1))/2
  }

  def flops(args:Arguments) = {
    val (a, b) = args
    val (m,n) = b.dimensions
    val (p,q) = if(side == FromLeft) (m,n) else (n,m)
    p*(p+1L)*q
  }
}

trait Benchmark_L_X_eq_B extends TrsmBenchmark {
  def topHeader = "L X = B"
  val side  = FromLeft
  val uplo  = Lower
  val trans = NoTranspose
  val diag  = NonUnitDiagonal
}

trait Benchmark_X_U_eq_B extends TrsmBenchmark {
  def topHeader = "X U = B"
  val side  = FromRight
  val uplo  = Upper
  val trans = NoTranspose
  val diag  = NonUnitDiagonal
}

trait SpireTrsmBenchmark extends TrsmBenchmark {
  type TrsmType = (Sides.Value, UpperOrLower.Value,
                   Transposition.Value, DiagonalProperty.Value,
                   Double, Matrix, Matrix) => Unit
  val trsm: TrsmType

  def benched(args:Arguments) = {
    val (a, b) = args
    trsm(side, uplo, trans, diag, 2.0, a, b)
    b
  }
}

object TriangularSolverBenchmarks extends BenchmarkGenerator {

  def dimensions(minPowerOf2:Int, maxPowerOf2:Int, reverse:Boolean) = for {
      Seq(k,l) <- (if(reverse) maxPowerOf2 to minPowerOf2 by -1
                   else minPowerOf2 to maxPowerOf2).map(2**_).sliding(2)
      m <- Seq(k, (k+l)/2)
    } yield m

  def main(args:Array[String]) {
    val (lo, hi) = if(args.size == 0) (1, 8)
                   else (args(0).toInt, args(1).toInt)
    val reverse = args.size == 3 && args(2) == "R"
    println("Gflop/s for triangular solver A X = B with all matrices m x m")
    println("-------------------------------------------------------------")
    val dimsHeader = "%4s".format("m")
    val benchmarks =
      new Benchmark_L_X_eq_B with SpireTrsmBenchmark with SpireNaiveBenchmark ::
      new Benchmark_L_X_eq_B with SpireTrsmBenchmark with SpireLayeredBenchmark ::
      new Benchmark_X_U_eq_B with SpireTrsmBenchmark with SpireNaiveBenchmark ::
      new Benchmark_X_U_eq_B with SpireTrsmBenchmark with SpireLayeredBenchmark ::
      Nil

    val benchmarksTopHeader = benchmarks.map(_.formattedTopHeader).mkString("  ")
    val benchmarksHeader = benchmarks.map(_.formatedHeader).mkString("  ")
    println("    "     ++ "  " ++ benchmarksTopHeader)
    println(dimsHeader ++ "  " ++ benchmarksHeader)

    implicit val timer = new MatrixTimer

    for {
      m <- dimensions(lo, hi, reverse)
      a <- elts.triangularMatrixSample(m, Lower, UnitDiagonal).take(1)
      b <- elts.generalMatrixSample(m,m).take(1)
    } {
      println("%4d  ".format(m) ++ "  " ++
              benchmarks.map(_.formatedTiming((a, b))).mkString("  "))
    }
  }
}

