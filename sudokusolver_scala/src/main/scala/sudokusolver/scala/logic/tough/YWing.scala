package sudokusolver.scala.logic.tough

import sudokusolver.scala.{Board, Cell, RemoveCandidates, UnsolvedCell, mergeToRemoveCandidates, zipEveryTriple}

/*
 * http://www.sudokuwiki.org/Y_Wing_Strategy
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, all three cells each have two
 * candidates, there are three total candidates across the three cells, the hinge shares one candidate with one wing and
 * one candidate with the other wing, and the wing cells share a candidate among each other, then this third candidate
 * must be the solution to one of the wings. The third candidate can be removed from any cell which can see both wings.
 */
def yWing(board: Board[Cell]): Seq[RemoveCandidates] =

  def tryHinge(hinge: UnsolvedCell, wingA: UnsolvedCell, wingB: UnsolvedCell) =
    val wingCandidates = wingA.candidates & wingB.candidates
    if hinge.isInSameUnit(wingA) && hinge.isInSameUnit(wingB) &&
      (hinge.candidates & wingA.candidates).size == 1 &&
      (hinge.candidates & wingB.candidates).size == 1 &&
      wingCandidates.size == 1 then
      val candidate = wingCandidates.head
      for
        cell <- board.cells.collect { case cell: UnsolvedCell => cell }
        if cell != wingA && cell != wingB &&
          cell.candidates.contains(candidate) &&
          cell.isInSameUnit(wingA) && cell.isInSameUnit(wingB)
      yield cell -> candidate
    else
      Nil

  board.cells
    .collect { case cell: UnsolvedCell if cell.candidates.size == 2 => cell }
    .zipEveryTriple
    .filter((a, b, c) => (a.candidates | b.candidates | c.candidates).size == 3)
    .flatMap((a, b, c) => tryHinge(a, b, c) ++ tryHinge(b, a, c) ++ tryHinge(c, a, b))
    .mergeToRemoveCandidates