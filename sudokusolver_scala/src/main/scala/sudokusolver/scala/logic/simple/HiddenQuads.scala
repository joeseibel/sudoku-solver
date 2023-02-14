package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HQ
 *
 * If four candidates exist across four cells in a unit, then those four candidates must be placed in those four cells.
 * All other candidates can be removed from those four cells.
 */
def hiddenQuads(board: Board[Cell]): Seq[RemoveCandidates] =
  board.units.flatMap { unit =>
    SudokuNumber.values.toIndexedSeq.zipEveryQuad.flatMap { (a, b, c, d) =>
      val cells = unit.collect { case cell@UnsolvedCell(_, _, candidates)
        if candidates.contains(a) || candidates.contains(b) || candidates.contains(c) || candidates.contains(d) => cell
      }
      if cells.size == 4 then
        val union = cells.map(_.candidates).reduce(_ ++ _)
        if union.contains(a) && union.contains(b) && union.contains(c) && union.contains(d) then
          val removals = for
            cell <- cells
            candidate <- cell.candidates - a - b - c - d
          yield (cell, candidate)
          Some(removals)
        else
          None
      else
        None
    }.flatten
  }.mergeToRemoveCandidates