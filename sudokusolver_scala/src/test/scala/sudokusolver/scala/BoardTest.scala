package sudokusolver.scala

import munit.FunSuite

class BoardTest extends FunSuite:
  test("Board wrong size") {
    val message = "requirement failed: elements size is 0, must be 9."
    interceptMessage[IllegalArgumentException](message)(Board(Vector()))
  }

  test("Board wrong inner size.") {
    val message = "requirement failed: elements(0) size is 0, must be 9."
    interceptMessage[IllegalArgumentException](message)(Board(Vector.fill(UnitSize)(Vector())))
  }