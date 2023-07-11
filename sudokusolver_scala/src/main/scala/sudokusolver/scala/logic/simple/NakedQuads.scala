package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NQ
 *
 * If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
 * placed in those four cells. The four candidates can be removed from every other cell in the unit.
 */
def nakedQuads(board: Board[Cell]): Seq[RemoveCandidates] =
  val removals = for
    unit <- board.units
    (a, b, c, d) <- unit.collect { case cell: UnsolvedCell => cell }.zipEveryQuad
    unionOfCandidates = a.candidates | b.candidates | c.candidates | d.candidates
    if unionOfCandidates.size == 4
    cell <- unit.collect { case cell: UnsolvedCell => cell }
    if cell != a && cell != b && cell != c && cell != d
    candidate <- cell.candidates & unionOfCandidates
  yield cell -> candidate
  removals.mergeToRemoveCandidates