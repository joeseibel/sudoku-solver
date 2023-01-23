package sudokusolver.scala

import munit.FunSuite

class BoardModificationTest extends FunSuite:
  test("BoardModification row too low") {
    val message = "requirement failed: row is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(RemoveCandidates(-1, 0, 1))
  }

  test("BoardModification row too high") {
    val message = "requirement failed: row is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(RemoveCandidates(9, 0, 1))
  }

  test("BoardModification column too low") {
    val message = "requirement failed: column is -1, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(SetValue(0, -1, 1))
  }

  test("BoardModification column too high") {
    val message = "requirement failed: column is 9, must be between 0 and 8."
    interceptMessage[IllegalArgumentException](message)(SetValue(0, 9, 1))
  }

  test("RemoveCandidates candidates are empty") {
    val message = "requirement failed: candidates must not be empty."
    interceptMessage[IllegalArgumentException](message)(RemoveCandidates(0, 0))
  }

  test("RemoveCandidates not a candidate for cell") {
    val message = "requirement failed: 1 is not a candidate for [0, 0]."
    val cell = UnsolvedCell(0, 0, Set(SudokuNumber.TWO))
    interceptMessage[IllegalArgumentException](message)(RemoveCandidates(cell, Set(SudokuNumber.ONE)))
  }

  test("SetValue not a candidate for cell") {
    val message = "requirement failed: 1 is not a candidate for [0, 0]."
    val cell = UnsolvedCell(0, 0, Set(SudokuNumber.TWO))
    interceptMessage[IllegalArgumentException](message)(SetValue(cell, SudokuNumber.ONE))
  }