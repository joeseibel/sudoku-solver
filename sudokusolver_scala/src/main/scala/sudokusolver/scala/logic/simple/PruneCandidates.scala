package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * If a cell is solved, then no other cells in the same unit can have that number as a candidate.
 */
def pruneCandidates(board: Board[Cell]): Seq[RemoveCandidates] =
  board.cells.collect { case cell: UnsolvedCell =>
    val sameRow = board.getRow(cell.row)
    val sameColumn = board.getColumn(cell.column)
    val sameBlock = board.getBlock(cell.block)
    val sameUnits = sameRow ++ sameColumn ++ sameBlock
    val toRemove = cell.candidates intersect sameUnits.collect { case SolvedCell(_, _, value) => value }.toSet
    if toRemove.nonEmpty then Some(RemoveCandidates(cell, toRemove)) else None
  }.flatten