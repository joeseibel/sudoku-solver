package sudokusolver.scala.logic.extreme

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Sue_De_Coq
 *
 * This solution starts with looking for two or three cells in the same linear unit (row or column) and block and the
 * union of candidates across the cells has a size which is at least two more than the number of cells. In other words,
 * if two cells are selected, then they must have at least four candidates. If three cells are selected, they must have
 * at least five candidates. These cells are the main group in this solution.
 *
 * Once the main group is identified, this solution then searches for an Almost Locked Set in the same linear unit as
 * the main group and also for an ALS in the same block as the main group. As a reminder, an ALS is a set of n unsolved
 * cells, all of which can see each other, and there are n + 1 candidates across all n cells. The two ALSs can only
 * contain candidates found in the main group, they must contain all the candidates of the main group, and there can be
 * no common candidates across the two ALSs.
 *
 * Once we have the main group and the two ALSs, it is then certain that each of the common candidates must appear in
 * one of the three groups. Therefore, for any common candidate, that candidate cannot be the solution for any cell
 * which can see the main group and can see the ALS that has the candidate. The candidates of the linear unit ALS can be
 * removed from other cells of that linear unit which are not a part of the main group. The candidates of the block unit
 * ALS can be removed from other cells of that block which are not a part of the main group.
 */
def sueDeCoq(board: Board[Cell]): Seq[RemoveCandidates] =

  def sueDeCoq(units: Seq[IndexedSeq[Cell]], getUnitIndex: Cell => Int) =
    units.map(_.collect { case cell: UnsolvedCell => cell }).flatMap { unit =>
      unit.groupBy(_.block).toSeq.flatMap { (blockIndex, unitByBlock) =>
        val otherCellsInUnit = unit.filter(_.block != blockIndex)
        val block = board.getBlock(blockIndex).collect { case cell: UnsolvedCell => cell }
        val otherCellsInBlock = block.filter(getUnitIndex(_) != getUnitIndex(unit.head))

        def getGroupRemovals(group: Seq[UnsolvedCell]) =
          val candidates = group.map(_.candidates).reduce(_ | _)
          if candidates.size >= group.size + 2 then
            getAlmostLockedSets(otherCellsInUnit, candidates).flatMap { unitALS =>
              getAlmostLockedSets(otherCellsInBlock, candidates)
                .filter { blockALS =>
                  unitALS.candidates.size + blockALS.candidates.size == candidates.size &&
                    (unitALS.candidates & blockALS.candidates).isEmpty
                }
                .flatMap { blockALS =>
                  val unitRemovals = unit.filter(cell => !group.contains(cell) && !unitALS.cells.contains(cell))
                    .flatMap(cell => (cell.candidates & unitALS.candidates).map(cell -> _))
                  val blockRemovals = block.filter(cell => !group.contains(cell) && !blockALS.cells.contains(cell))
                    .flatMap(cell => (cell.candidates & blockALS.candidates).map(cell -> _))
                  unitRemovals ++ blockRemovals
                }
            }
          else
            Nil

        unitByBlock match
          case Seq(_, _) => getGroupRemovals(unitByBlock)
          case Seq(_, _, _) => getGroupRemovals(unitByBlock) ++
            unitByBlock.zipEveryPair.flatMap((a, b) => getGroupRemovals(Seq(a, b)))
          case _ => Nil
      }
    }

  val rowRemovals = sueDeCoq(board.rows, _.row)
  val columnRemovals = sueDeCoq(board.columns, _.column)
  (rowRemovals ++ columnRemovals).mergeToRemoveCandidates

private class ALS(val cells: Set[UnsolvedCell], val candidates: Set[SudokuNumber])

private def getAlmostLockedSets(cells: IndexedSeq[UnsolvedCell], groupCandidates: Set[SudokuNumber]): Seq[ALS] =
  val almostLockedSets1 = cells.filter(cell => cell.candidates.size == 2 && cell.candidates.subsetOf(groupCandidates))
    .map(cell => ALS(Set(cell), cell.candidates))
  val almostLockedSets2 = cells.zipEveryPair
    .map((a, b) => ALS(Set(a, b), a.candidates | b.candidates))
    .filter(als => als.candidates.size == 3 && als.candidates.subsetOf(groupCandidates))
  val almostLockedSets3 = cells.zipEveryTriple
    .map((a, b, c) => ALS(Set(a, b, c), a.candidates | b.candidates | c.candidates))
    .filter(als => als.candidates.size == 4 && als.candidates.subsetOf(groupCandidates))
  val almostLockedSets4 = cells.zipEveryQuad
    .map((a, b, c, d) => ALS(Set(a, b, c, d), a.candidates | b.candidates | c.candidates | d.candidates))
    .filter(als => als.candidates.size == 5 && als.candidates.subsetOf(groupCandidates))
  almostLockedSets1 ++ almostLockedSets2 ++ almostLockedSets3 ++ almostLockedSets4