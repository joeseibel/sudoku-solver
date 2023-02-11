package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Hidden_Candidates#HP
 *
 * If a pair of candidates exists in exactly two cells in a unit, then those two candidates must be placed in those two
 * cells. All other candidates can be removed from those two cells.
 */
def hiddenPairs(board: Board[Cell]): Seq[RemoveCandidates] =
  val removals = board.units.flatMap { unit =>
    SudokuNumber.values.toIndexedSeq.zipEveryPair.flatMap { (a, b) =>
      val cells = unit.collect { case cell: UnsolvedCell if cell.candidates.contains(a) => cell }
      cells match
        case Seq(_, _) if cells == unit.collect { case cell: UnsolvedCell if cell.candidates.contains(b) => cell } =>
          Some(cells.flatMap(cell => (cell.candidates - a - b).map(candidate => (cell, candidate))))
        case _ => None
    }.flatten
  }
  removals.mergeToRemoveCandidates