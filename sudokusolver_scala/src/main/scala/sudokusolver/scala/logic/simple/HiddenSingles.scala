package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidate must be placed in that cell.
 */
def hiddenSingles(board: Board[Cell]): Seq[SetValue] =
  board.units.flatMap { unit =>
    val unsolved = unit.collect { case cell: UnsolvedCell => cell }
    SudokuNumber.values.flatMap { candidate =>
      unsolved.filter(_.candidates.contains(candidate)) match
        case Seq(cell) => Some(SetValue(cell, candidate))
        case _ => None
    }
  }.distinct