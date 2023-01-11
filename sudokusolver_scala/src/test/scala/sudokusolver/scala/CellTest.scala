package sudokusolver.scala

import munit.FunSuite

class CellTest extends FunSuite:
  test("parseSimpleCells wrong length") {
    val message = "requirement failed: simpleBoard.length is 0, must be 81."
    interceptMessage[IllegalArgumentException](message)(parseSimpleCells(""))
  }