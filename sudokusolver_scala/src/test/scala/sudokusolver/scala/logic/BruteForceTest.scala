package sudokusolver.scala.logic

import munit.FunSuite
import sudokusolver.scala.logic.BruteForceSolution.{MultipleSolutions, NoSolutions, SingleSolution}
import sudokusolver.scala.{parseBoard, parseOptionalBoard}

class BruteForceTest extends FunSuite:
  test("bruteForce single solution") {
    val board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010"
    val expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
    assertEquals(bruteForce(parseOptionalBoard(board)), SingleSolution(parseBoard(expected)))
  }

  test("bruteForce no solutions") {
    val board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010"
    assertEquals(bruteForce(parseOptionalBoard(board)), NoSolutions)
  }

  test("bruteForce multiple solutions") {
    val board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010"
    assertEquals(bruteForce(parseOptionalBoard(board)), MultipleSolutions)
  }

  test("bruteForce already solved") {
    val board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
    assertEquals(bruteForce(parseOptionalBoard(board)), SingleSolution(parseBoard(board)))
  }

  test("bruteForce invalid solution") {
    val board = "817942563234615789569837142451329678623781495978564321796158234182473956345296818"
    assertEquals(bruteForce(parseOptionalBoard(board)), NoSolutions)
  }