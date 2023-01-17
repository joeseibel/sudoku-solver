package sudokusolver.scala

import munit.FunSuite

class CellTest extends FunSuite:
  test("Cell row too low") {
    val message = "requirement failed: row is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(SolvedCell(-1, 0, SudokuNumber.ONE))
  }

  test("Cell row too high") {
    val message = "requirement failed: row is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(SolvedCell(9, 0, SudokuNumber.ONE))
  }

  test("Cell column too low") {
    val message = "requirement failed: column is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(UnsolvedCell(0, -1))
  }

  test("Cell column too high") {
    val message = "requirement failed: column is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(UnsolvedCell(0, 9))
  }

  test("UnsolvedCell candidates are empty") {
    val message = "requirement failed: candidates must not be empty."
    interceptMessage[IllegalArgumentException](message)(UnsolvedCell(0, 0, Set.empty))
  }

  test("parseSimpleCells wrong length") {
    val message = "requirement failed: simpleBoard.length is 0, must be 81."
    interceptMessage[IllegalArgumentException](message)(parseSimpleCells(""))
  }