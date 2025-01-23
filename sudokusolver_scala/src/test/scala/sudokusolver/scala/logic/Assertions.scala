package sudokusolver.scala.logic

import munit.Assertions.assertEquals
import sudokusolver.scala.*
import sudokusolver.scala.logic.BruteForceSolution.SingleSolution

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
  /*
   * Why am I using sortBy instead of sorted and having BoardModification extend from Ordered? In short, implementing
   * Ordered for BoardModification would lead to the equals method not being consistent with the compare method. I want
   * to sort BoardModifications by the row and column indices only while ignoring other fields. However, I want equality
   * to check all fields, as that is useful in unit tests. Having a different standard for equality between the equals
   * and compare methods breaks the contract of Ordered. It is also possible to use sorted while passing a custom
   * Ordering since Ordering is intended to support multiple orderings for the same type. However, using sortBy seems to
   * be simpler to invoke than creating a custom Ordering.
   */
  val actual = logicFunction(board).sortBy(modification => (modification.row, modification.column))
  for modification <- actual do
    val solution = bruteForceSolution(modification.row, modification.column)
    modification match
      case RemoveCandidates(row, column, candidates) =>
        assert(!candidates.contains(solution), s"Cannot remove candidate $solution from [$row, $column]")
      case SetValue(row, column, value) =>
        assertEquals(value, solution, s"Cannot set value $value to [$row, $column]. Solution is $solution")
  assertEquals(actual, expected)