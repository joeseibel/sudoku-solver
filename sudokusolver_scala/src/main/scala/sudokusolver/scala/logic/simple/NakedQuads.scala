package sudokusolver.scala.logic.simple

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NQ
 *
 * If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
 * placed in those four cells. The four candidates can be removed from every other cell in the unit.
 */
def nakedQuads(board: Board[Cell]): Seq[RemoveCandidates] =
  board.units.flatMap { unit =>
    unit.collect { case cell: UnsolvedCell => cell }.zipEveryQuad.flatMap { (a, b, c, d) =>
      val unionOfCandidates = a.candidates ++ b.candidates ++ c.candidates ++ d.candidates
      if unionOfCandidates.size == 4 then
        val removals = for
          cell <- unit.collect { case cell: UnsolvedCell if cell != a && cell != b && cell != c && cell != d => cell }
          candidate <- cell.candidates intersect unionOfCandidates
        yield (cell, candidate)
        Some(removals)
      else
        None
    }.flatten
  }.mergeToRemoveCandidates