package spire.math

import scala.{specialized => spec}
import scala.math.min

import scala.annotation.tailrec

//import Implicits._

/**
 *  Interface for a sorting strategy object.
 */
trait Sort {
  def sort[@spec A:Order:Manifest](data:Array[A]): Unit
}

/**
 * In-place merge sort implementation. This sort is stable but does mutate
 * the given array. It is an in-place sort but it does allocate a temporary
 * array of the same size as the input.
 *
 * This sort is faster than quickSort, but must allocate extra space.
 */
object MergeSort extends Sort {
  final def sort[@spec A](in: Array[A])(implicit o: Order[A], m: Manifest[A]) = {
    if (in.length < 2) in else {
      var initLen = 1

      val passes = 32 - java.lang.Integer.numberOfLeadingZeros(in.length - 1)
      if (passes % 2 == 1) {
        var i = 0
        val len = in.length & (~1)
        while (i < len) {
          if (o.gt(in(i), in(i + 1))) {
            val tmp = in(i)
            in(i) = in(i + 1)
            in(i + 1) = tmp
          }
          i += 2
        }
        initLen = 2
      }

      val out = new Array[A](in.length)
      sort2(in, out, initLen)
    }
  }


  @tailrec private final def sort2[@spec A](in: Array[A], out: Array[A], len: Int)(implicit o: Order[A]): Array[A] = {
    if (len < in.length) {
      val nextLen = 2 * len
      var i = 0
      while ((i + len) <= in.length) {
        merge2(in, out, i, len)
        i += nextLen
      }
      if (i < in.length)
        System.arraycopy(in, i, out, i, in.length - i)
      sort2(out, in, nextLen)
    } else in
  }

  // TODO: making this private breaks specialization, but we'd like to hide it
  // somehow. 2.11 maybe?
  /**
   * Helper method for mergeSort, used to do a single "merge" between two
   * sections of the input array. The start, mid and end parameters denote the
   * left and right ranges of the input to merge, as well as the area of the
   * ouput to write to.
   *
   * This method will be called approximately N times (where N is the length
   * of the array to be sorted), which is why we inline it.
   */
  @inline final def merge2[@spec A](in: Array[A], out: Array[A], left: Int, len: Int)(implicit o: Order[A]) {
    val right = left + len
    var end = right + len
    if (end > in.length) end = in.length

    var i = left
    var j = right
    var k = left
    while (i < right && j < end) {
      if (o.lteqv(in(i), in(j))) {
        out(k) = in(i); i += 1
      } else {
        out(k) = in(j); j += 1
      }
      k += 1
    }
    if (i < right) System.arraycopy(in, i, out, k, right - i)
    if (j < end) System.arraycopy(in, j, out, k, end - j)
  }
}

/**
 * This is a specialized version of Scala's built-in quicksort
 * implementation. It is not a stable sort, and mutates its input. It does
 * not allocate extra space.
 */
object QuickSort {
  final def sort[@spec K](x:Array[K])(implicit o:Order[K], m:Manifest[K]) {
    def swap(k:K, a: Int, b: Int) {
      val t = x(a)
      x(a) = x(b)
      x(b) = t
    }
    def vecswap(k:K, _a: Int, _b: Int, n: Int) {
      var a = _a
      var b = _b
      var i = 0
      while (i < n) {
        swap(k, a, b)
        i += 1
        a += 1
        b += 1
      }
    }
    def med3(k:K, a: Int, b: Int, c: Int) = {
      if (o.lt(x(a), x(b))) {
        if (o.lt(x(b), x(c))) b else if (o.lt(x(a), x(c))) c else a
      } else {
        if (o.lt(x(b), x(c))) b else if (o.lt(x(a), x(c))) c else a
      }
    }
    def sort2(k:K, off: Int, len: Int) {
      // Insertion sort on smallest arrays
      if (len < 7) {
        var i = off
        while (i < len + off) {
          var j = i
          while (j > off && o.gt(x(j-1), x(j))) {
            swap(k, j, j-1)
            j -= 1
          }
          i += 1
        }
      } else {
        // Choose a partition element, v
        var m = off + (len >> 1)        // Small arrays, middle element
        if (len > 7) {
          var l = off
          var n = off + len - 1
          if (len > 40) {        // Big arrays, pseudomedian of 9
            val s = len / 8
            l = med3(k, l, l+s, l+2*s)
            m = med3(k, m-s, m, m+s)
            n = med3(k, n-2*s, n-s, n)
          }
          m = med3(k, l, m, n) // Mid-size, med of 3
        }
        val v = x(m)
  
        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        var done = false
        while (!done) {
          while (b <= c && o.lteqv(x(b), v)) {
            if (o.eqv(x(b), v)) {
              swap(k, a, b)
              a += 1
            }
            b += 1
          }
          while (c >= b && o.gteqv(x(c), v)) {
            if (o.eqv(x(c), v)) {
              swap(k, c, d)
              d -= 1
            }
            c -= 1
          }
          if (b > c) {
            done = true
          } else {
            swap(k, b, c)
            c -= 1
            b += 1
          }
        }
  
        // Swap partition elements back to middle
        val n = off + len
        var s = math.min(a-off, b-a)
        vecswap(k, off, b-s, s)
        s = math.min(d-c, n-d-1)
        vecswap(k, b, n-s, s)
  
        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
          sort2(k, off, s)
        s = d - c
        if (s > 1)
          sort2(k, n-s, s)
      }
    }

    sort2(x(0), 0, x.length)
  }
}

// TODO: would be nice to try implementing e.g. Tim Peters' sort algorithm.

/**
 * Both sorts are roughly the same speed right now. Merge sort is stable but
 * uses extra memory. Quicksort is a bit faster but is O(n^2) in worst case.
 */
object Sorting {
  final def mergeSort[@spec A:Order:Manifest](data:Array[A]) = MergeSort.sort(data)
  final def quickSort[@spec K:Order:Manifest](data:Array[K]) = QuickSort.sort(data)
}
