package sudokusolver.scala.logic.simple

import sudokusolver.scala.{Board, Cell, SetValue, UnsolvedCell}

/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
 */
def nakedSingles(board: Board[Cell]): Seq[SetValue] =
  board.cells.collect { case cell: UnsolvedCell if cell.candidates.size == 1 => SetValue(cell, cell.candidates.head) }