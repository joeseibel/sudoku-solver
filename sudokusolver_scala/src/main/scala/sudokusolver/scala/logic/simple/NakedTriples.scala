package sudokusolver.scala.logic.simple

import sudokusolver.scala.{Board, Cell, RemoveCandidates, UnsolvedCell, mergeToRemoveCandidates, zipEveryTriple}

/*
 * http://www.sudokuwiki.org/Naked_Candidates#NT
 *
 * If a unit has three unsolved cells with a total of three candidates among them, then those three candidates must be
 * placed in those three cells. The three candidates can be removed from every other cell in the unit.
 */
def nakedTriples(board: Board[Cell]): Seq[RemoveCandidates] =
  board.units.flatMap { unit =>
    unit.collect { case cell: UnsolvedCell => cell }.zipEveryTriple.flatMap { (a, b, c) =>
      val unionOfCandidates = a.candidates | b.candidates | c.candidates
      if unionOfCandidates.size == 3 then
        for
          cell <- unit.collect { case cell: UnsolvedCell => cell }
          if cell != a && cell != b && cell != c
          candidate <- cell.candidates intersect unionOfCandidates
        yield cell -> candidate
      else
        Nil
    }
  }.mergeToRemoveCandidates