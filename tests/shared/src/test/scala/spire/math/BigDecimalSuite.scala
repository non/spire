package spire.math

import spire.implicits.{nrootOps, BigDecimalAlgebra}

class BigDecimalSuite extends munit.FunSuite {
  test("sqrt") {
    // this sqrt used to infinite loop
    assertEquals(BigDecimal("4.000000000000000000000000000000003").sqrt,
                 BigDecimal("2.000000000000000000000000000000001")
    )
  }
}
