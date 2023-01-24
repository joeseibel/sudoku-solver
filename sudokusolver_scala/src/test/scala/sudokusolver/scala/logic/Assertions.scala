package sudokusolver.scala.logic

import munit.Assertions.assertEquals
import sudokusolver.scala.logic.BruteForceSolution.SingleSolution
import sudokusolver.scala.*

def assertLogicalSolution[T <: BoardModification](expected: Seq[T],
                                                  withCandidates: String,
                                                  logicFunction: Board[Cell] => Seq[T]): Unit =
  assertLogicalSolution(expected, parseCellsWithCandidates(withCandidates), logicFunction)

def assertLogicalSolution[T <: BoardModification](expected: Seq[T],
                                                  board: Board[Cell],
                                                  logicFunction: Board[Cell] => Seq[T]): Unit =
  val numberBoard = board.mapCells {
    case SolvedCell(_, _, value) => Some(value)
    case _: UnsolvedCell => None
  }
  val bruteForceSolution = bruteForce(numberBoard).asInstanceOf[SingleSolution].solution
  val actual = logicFunction(board).sorted
  for modification <- actual do
    val solution = bruteForceSolution(modification.row, modification.column)
    modification match
      case RemoveCandidates(row, column, candidates) =>
        assert(!candidates.contains(solution), s"Cannot remove candidate $solution from [$row, $column]")
      case SetValue(row, column, value) =>
        assertEquals(value, solution, s"Cannot set value $value to [$row, $column]. Solution is $solution")
  assertEquals(actual, expected)