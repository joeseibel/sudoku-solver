package sudokusolver.scala.logic.diabolical

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Extended_Unique_Rectangles
 *
 * Extended Unique Rectangles are like Unique Rectangles except that they are 2x3 instead of 2x2. The cells in the
 * rectangle must be spread over three blocks and the dimension that has three elements must be spread over three units
 * (rows or columns). If there are only three candidates found among the six cells, then such a rectangle is the Deadly
 * Pattern. If there is one cell with additional candidates, then the removal of such candidates would lead to a Deadly
 * Pattern. The common candidates can be removed from the cell leaving only the additional candidates remaining.
 */
def extendedUniqueRectangles(board: Board[Cell]): Seq[RemoveCandidates] =
  (getRemovals(board.rows) ++ getRemovals(board.columns)).mergeToRemoveCandidates

private def getRemovals(units: Seq[Seq[Cell]]): Seq[LocatedCandidate] =
  units.toIndexedSeq
    .zipEveryPair
    .flatMap { (unitA, unitB) =>
      (unitA zip unitB).flatMap { (cellA, cellB) =>
        cellA match
          case cellA: UnsolvedCell => cellB match
            case cellB: UnsolvedCell => Some(cellA -> cellB)
            case _ => None
          case _ => None
      }.toIndexedSeq.zipEveryTriple
    }
    .map((otherA, otherB, otherC) => Seq(otherA(0), otherB(0), otherC(0)) -> Seq(otherA(1), otherB(1), otherC(1)))
    .filter((unitA, unitB) => (unitA ++ unitB).map(_.block).toSet.size == 3)
    .flatMap { (unitA, unitB) =>
      val unitACandidates = unitA.map(_.candidates).reduce(_ | _)
      val unitBCandidates = unitB.map(_.candidates).reduce(_ | _)
      if unitACandidates.size == 3 then
        getRemovals(unitACandidates, unitB, unitBCandidates)
      else if unitBCandidates.size == 3 then
        getRemovals(unitBCandidates, unitA, unitACandidates)
      else
        Nil
    }

private def getRemovals(
                         commonCandidates: Set[SudokuNumber],
                         unit: Seq[UnsolvedCell],
                         unitCandidates: Set[SudokuNumber]
                       ): Seq[LocatedCandidate] =
  if unitCandidates.size > 3 && commonCandidates.subsetOf(unitCandidates) then
    unit.filter(!_.candidates.subsetOf(commonCandidates)) match
      case Seq(withAdditional) => (withAdditional.candidates & commonCandidates).toSeq.map(withAdditional -> _)
      case _ => Nil
  else
    Nil