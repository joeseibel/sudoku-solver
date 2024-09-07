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

  test("row too low") {
    val message = "requirement failed: row is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(RemoveCandidates(-1, 0, 1))
  }

  test("row too high") {
    val message = "requirement failed: row is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(SetValue(9, 0, 1))
  }

  test("column too low") {
    val message = "requirement failed: column is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(SolvedCell(0, -1, SudokuNumber.ONE))
  }

  test("column too high") {
    val message = "requirement failed: column is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(UnsolvedCell(0, 9))
  }