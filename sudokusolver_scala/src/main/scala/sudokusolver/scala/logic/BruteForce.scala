package sudokusolver.scala.logic

import sudokusolver.scala.logic.BruteForceSolution.{MultipleSolutions, NoSolutions, SingleSolution}
import sudokusolver.scala.{Board, SudokuNumber, UnitSize, getBlockIndex}

enum BruteForceSolution:
  case NoSolutions
  case MultipleSolutions
  case SingleSolution(solution: Board[SudokuNumber])

/*
 * Recursively tries every number for each unsolved cell looking for a solution.
 *
 * Motivation for implementing a brute force solution:
 *
 * The purpose of this solver is to go through the exercise of implementing various logical solutions. Why implement
 * brute force if I only care about logical solutions? The first reason is to check the correctness of the logical
 * solutions. When solving a board, the first thing that is done is to get the brute force solution. After that, any
 * logical modifications will be checked against the brute force solution. If a logical solution tries to set an
 * incorrect value to a cell or remove a candidate from a cell which is the known solution, then an
 * IllegalStateException is thrown.
 *
 * The second reason for implementing brute force is to check for the number of solutions for a board before trying the
 * logical solutions. If a board cannot be solved or if it has multiple solutions, then I don't bother with the logical
 * solutions. The logical solutions are written assuming that they are operating on a board with only one solution.
 */
def bruteForce(board: Board[Option[SudokuNumber]]): BruteForceSolution =
  if !board.cells.contains(None) then
    val filledBoard = board.mapCells(_.get)
    if isSolved(filledBoard) then SingleSolution(filledBoard) else NoSolutions
  else
    bruteForce(board, 0, 0)

private def bruteForce(board: Board[Option[SudokuNumber]], rowIndex: Int, columnIndex: Int): BruteForceSolution =
  if rowIndex >= UnitSize then
    SingleSolution(board.mapCells(_.get))
  else

    def moveToNextCell(guess: Option[SudokuNumber] = None) =
      val withGuess = guess match
        case Some(_) => board.updated(rowIndex, columnIndex, guess)
        case None => board
      if columnIndex + 1 >= UnitSize then
        bruteForce(withGuess, rowIndex + 1, 0)
      else
        bruteForce(withGuess, rowIndex, columnIndex + 1)

    board(rowIndex, columnIndex) match
      case Some(_) => moveToNextCell()
      case None =>
        val rowInvalid = board.getRow(rowIndex).flatten.toSet
        val columnInvalid = board.getColumn(columnIndex).flatten.toSet
        val blockInvalid = board.getBlock(getBlockIndex(rowIndex, columnIndex)).flatten.toSet
        val invalid = rowInvalid ++ columnInvalid ++ blockInvalid
        val valid = SudokuNumber.values.toSet -- invalid
        valid.foldLeft(NoSolutions) { (previousResult, guess) =>
          previousResult match
            case MultipleSolutions => MultipleSolutions
            case SingleSolution(_) =>
              moveToNextCell(Some(guess)) match
                case MultipleSolutions | SingleSolution(_) => MultipleSolutions
                case NoSolutions => previousResult
            case NoSolutions => moveToNextCell(Some(guess))
        }

private def isSolved(board: Board[SudokuNumber]): Boolean =
  board.rows.forall(_.distinct.size == UnitSize)
    && board.columns.forall(_.distinct.size == UnitSize)
    && board.blocks.forall(_.distinct.size == UnitSize)