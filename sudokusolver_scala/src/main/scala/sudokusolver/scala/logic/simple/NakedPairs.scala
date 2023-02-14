package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NP
 *
 * If a pair of unsolved cells in a unit has the same two candidates, then those two candidates must be placed in those
 * two cells. The two candidates can be removed from every other cell in the unit.
 */
def nakedPairs(board: Board[Cell]): Seq[RemoveCandidates] =
  val removals = for
    unit <- board.units
    (a, b) <- unit.collect { case cell: UnsolvedCell if cell.candidates.size == 2 => cell }.zipEveryPair
    if a.candidates == b.candidates
    cell <- unit.collect { case cell: UnsolvedCell => cell }
    if cell != a && cell != b
    candidate <- cell.candidates intersect a.candidates
  yield cell -> candidate
  removals.mergeToRemoveCandidates