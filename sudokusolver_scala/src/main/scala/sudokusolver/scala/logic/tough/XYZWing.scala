package sudokusolver.scala.logic.tough

import sudokusolver.scala.*

/*
 * http://www.sudokuwiki.org/XYZ_Wing
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, the hinge has three candidates, the
 * wings have two candidates each, there is one candidate shared among all three cells, two candidates are shared
 * between the hinge and one wing and two candidates between the hinge and the other wing, and the common candidate is
 * the only one shared between the wings, then the common candidate must be the solution to one of the cells. The common
 * candidate can be removed from any cell which can see all three cells.
 */
def xyzWing(board: Board[Cell]): Seq[RemoveCandidates] =
  board.cells.collect { case cell: UnsolvedCell if cell.candidates.size == 3 => cell }.flatMap { hinge =>
    board.cells
      .collect { case cell: UnsolvedCell => cell }
      .zipEveryPair
      .filter { (wingA, wingB) =>
        wingA.candidates.size == 2 && wingB.candidates.size == 2 &&
          hinge.isInSameUnit(wingA) && hinge.isInSameUnit(wingB) &&
          (hinge.candidates | wingA.candidates | wingB.candidates).size == 3
      }
      .flatMap { (wingA, wingB) =>
        val candidates = wingA.candidates & wingB.candidates
        if candidates.size == 1 then
          val candidate = candidates.head
          for
            cell <- board.cells.collect { case cell: UnsolvedCell => cell }
            if cell != hinge && cell != wingA && cell != wingB &&
              cell.candidates.contains(candidate) &&
              cell.isInSameUnit(hinge) && cell.isInSameUnit(wingA) && cell.isInSameUnit(wingB)
          yield cell -> candidate
        else
          Nil
      }
  }.mergeToRemoveCandidates