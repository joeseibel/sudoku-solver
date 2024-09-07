package sudokusolver.scala

import munit.FunSuite

class BoardModificationTest extends FunSuite:
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