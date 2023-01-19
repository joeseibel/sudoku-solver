package sudokusolver.scala.logic.simple

import munit.FunSuite
import sudokusolver.scala.logic.BruteForceSolution.SingleSolution
import sudokusolver.scala.logic.bruteForce
import sudokusolver.scala.{RemoveCandidates, SolvedCell, UnsolvedCell, parseSimpleCells}

class PruneCandidatesTest extends FunSuite:
  test("test") {
    val board = "000105000140000670080002400063070010900000003010090520007200080026000035000409000"
    val expected = Seq(
      RemoveCandidates(0, 0, 1, 4, 5, 8, 9),
      RemoveCandidates(0, 1, 1, 2, 4, 5, 6, 8),
      RemoveCandidates(0, 2, 1, 3, 4, 5, 6, 7, 8),
      RemoveCandidates(0, 4, 1, 2, 5, 7, 9),
      RemoveCandidates(0, 6, 1, 4, 5, 6, 7),
      RemoveCandidates(0, 7, 1, 2, 3, 4, 5, 6, 7, 8),
      RemoveCandidates(0, 8, 1, 3, 4, 5, 6, 7),
      RemoveCandidates(1, 2, 1, 3, 4, 6, 7, 8),
      RemoveCandidates(1, 3, 1, 2, 4, 5, 6, 7),
      RemoveCandidates(1, 4, 1, 2, 4, 5, 6, 7, 9),
      RemoveCandidates(1, 5, 1, 2, 4, 5, 6, 7, 9),
      RemoveCandidates(1, 8, 1, 3, 4, 5, 6, 7),
      RemoveCandidates(2, 0, 1, 2, 4, 8, 9),
      RemoveCandidates(2, 2, 1, 2, 3, 4, 6, 7, 8),
      RemoveCandidates(2, 3, 1, 2, 4, 5, 8),
      RemoveCandidates(2, 4, 1, 2, 4, 5, 7, 8, 9),
      RemoveCandidates(2, 7, 1, 2, 3, 4, 6, 7, 8),
      RemoveCandidates(2, 8, 2, 3, 4, 5, 6, 7, 8),
      RemoveCandidates(3, 0, 1, 3, 6, 7, 9),
      RemoveCandidates(3, 3, 1, 2, 3, 4, 6, 7, 9),
      RemoveCandidates(3, 5, 1, 2, 3, 5, 6, 7, 9),
      RemoveCandidates(3, 6, 1, 2, 3, 4, 5, 6, 7),
      RemoveCandidates(3, 8, 1, 2, 3, 5, 6, 7),
      RemoveCandidates(4, 1, 1, 2, 3, 4, 6, 8, 9),
      RemoveCandidates(4, 2, 1, 3, 6, 7, 9),
      RemoveCandidates(4, 3, 1, 2, 3, 4, 7, 9),
      RemoveCandidates(4, 4, 3, 7, 9),
      RemoveCandidates(4, 5, 2, 3, 5, 7, 9),
      RemoveCandidates(4, 6, 1, 2, 3, 4, 5, 6, 9),
      RemoveCandidates(4, 7, 1, 2, 3, 5, 7, 8, 9),
      RemoveCandidates(5, 0, 1, 2, 3, 5, 6, 9),
      RemoveCandidates(5, 2, 1, 2, 3, 5, 6, 7, 9),
      RemoveCandidates(5, 3, 1, 2, 4, 5, 7, 9),
      RemoveCandidates(5, 5, 1, 2, 5, 7, 9),
      RemoveCandidates(5, 8, 1, 2, 3, 5, 9),
      RemoveCandidates(6, 0, 1, 2, 6, 7, 8, 9),
      RemoveCandidates(6, 1, 1, 2, 4, 6, 7, 8),
      RemoveCandidates(6, 4, 2, 4, 7, 8, 9),
      RemoveCandidates(6, 5, 2, 4, 5, 7, 8, 9),
      RemoveCandidates(6, 6, 2, 3, 4, 5, 6, 7, 8),
      RemoveCandidates(6, 8, 2, 3, 5, 7, 8),
      RemoveCandidates(7, 0, 1, 2, 3, 5, 6, 7, 9),
      RemoveCandidates(7, 3, 1, 2, 3, 4, 5, 6, 9),
      RemoveCandidates(7, 4, 2, 3, 4, 5, 6, 7, 9),
      RemoveCandidates(7, 5, 2, 3, 4, 5, 6, 9),
      RemoveCandidates(7, 6, 2, 3, 4, 5, 6, 8),
      RemoveCandidates(8, 0, 1, 2, 4, 6, 7, 9),
      RemoveCandidates(8, 1, 1, 2, 4, 6, 7, 8, 9),
      RemoveCandidates(8, 2, 2, 3, 4, 6, 7, 9),
      RemoveCandidates(8, 4, 2, 4, 7, 9),
      RemoveCandidates(8, 6, 3, 4, 5, 6, 8, 9),
      RemoveCandidates(8, 7, 1, 2, 3, 4, 5, 7, 8, 9),
      RemoveCandidates(8, 8, 3, 4, 5, 8, 9)
    )
    val cellBoard = parseSimpleCells(board)
    val numberBoard = cellBoard.mapCells {
      case SolvedCell(_, _, value) => Some(value)
      case _: UnsolvedCell => None
    }
    val bruteForceSolution = bruteForce(numberBoard).asInstanceOf[SingleSolution].solution
    val actual = pruneCandidates(cellBoard).sorted
    actual.foreach { modification =>
      val row = modification.row
      val column = modification.column
      val solution = bruteForceSolution(row, column)
      assert(
        !modification.candidates.contains(solution),
        s"Cannot remove candidate $solution from [$row, $column]"
      )
    }
    assertEquals(actual, expected)
  }