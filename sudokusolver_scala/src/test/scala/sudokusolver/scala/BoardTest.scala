package sudokusolver.scala

import munit.FunSuite

class BoardTest extends FunSuite:
  test("Board getBlock index too low") {
    val board = Board(Vector.tabulate(UnitSize)(row => Vector.tabulate(UnitSize)(column => row * UnitSize + column)))
    val message = "requirement failed: blockIndex is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(board.getBlock(-1))
  }

  test("Board getBlock index too high") {
    val board = Board(Vector.tabulate(UnitSize)(row => Vector.tabulate(UnitSize)(column => row * UnitSize + column)))
    val message = "requirement failed: blockIndex is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(board.getBlock(9))
  }

  test("Board wrong size") {
    val message = "requirement failed: elements size is 0, must be 9."
    interceptMessage[IllegalArgumentException](message)(Board(Vector()))
  }

  test("Board wrong inner size") {
    val message = "requirement failed: elements(0) size is 0, must be 9."
    interceptMessage[IllegalArgumentException](message)(Board(Vector.fill(UnitSize)(Vector())))
  }