package sudokusolver.scala

import munit.FunSuite

class SudokuNumberTest extends FunSuite:
  test("sudokuNumber unexpected Char") {
    val message = "ch is 'a', must be between '1' and '9'."
    interceptMessage[IllegalArgumentException](message)(sudokuNumber('a'))
  }

  test("Parse optional board wrong length") {
    val message = "requirement failed: board.length is 0, must be 81."
    interceptMessage[IllegalArgumentException](message)(parseOptionalBoard(""))
  }

  test("Parse board wrong length") {
    val message = "requirement failed: board.length is 0, must be 81."
    interceptMessage[IllegalArgumentException](message)(parseBoard(""))
  }