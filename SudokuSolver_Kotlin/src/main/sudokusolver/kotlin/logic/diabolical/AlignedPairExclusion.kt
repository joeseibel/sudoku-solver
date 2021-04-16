package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.enumUnzip
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import sudokusolver.kotlin.zipEveryQuad
import sudokusolver.kotlin.zipEveryTriple
import java.util.EnumSet

/*
 * https://www.sudokuwiki.org/Aligned_Pair_Exclusion
 *
 * To understand Aligned Pair Exclusion, it is helpful to first define what an Almost Locked Set is. An ALS is a set of
 * n unsolved cells, all of which can see each other, and there are n + 1 candidates across all n cells. In the simplest
 * case, any unsolved cell with two candidates is an ALS; there is one cell and two candidates. A pair of cells is an
 * ALS if they can see each other and the union of candidates has a size of three. If there are three cells that see
 * each other and there are four candidates across those three cells, than those three cells are an ALS.
 *
 * Aligned Pair Exclusion considers a pair of unsolved cells, which may or may not see each other, and checks for
 * solution combinations for that pair which would cause problems for that pair or for Almost Locks Sets which are
 * visible to that pair. This will result in a list of solution combinations for the pair, some of which are known to be
 * invalid, and the others which could potentially be valid. If a particular candidate in one of the cells of the pair
 * only appears among the invalid combinations, then that candidate cannot be the solution to that cell and can be
 * removed.
 *
 * How is a solution combination for a pair checked for validity? The first simple thing to look at is if the candidates
 * of the combination are the same and the two cells can see each other, then the combination is invalid. If the
 * candidates are not the same, then it is time to look at the ALSs that are visible to both cells of the pair. If a
 * solution combination is a subset of the candidates of a visible ALS, then that combination would cause problems for
 * the ALS and the combination is invalid.
 *
 * The simplest case of checking an ALS is when the ALS has one cell and two candidates. If the solution combination has
 * the same candidates as the ALS, then the solution combination would empty the ALS. This is a very obvious case, but
 * it gets a little more complicated when an ALS has more than one cell and more than two candidates. The link at the
 * start of this comment has some examples with ALSs that have two cells and three cells. It is helpful to walk through
 * these examples to see how a solution combination which is a subset of the candidates of an an ALS is invalid.
 */
fun alignedPairExclusion(board: Board<Cell>): List<RemoveCandidates> =
    board.cells.filterIsInstance<UnsolvedCell>().zipEveryPair().flatMap { (cellA, cellB) ->
        val almostLockedSets = getAlmostLockedSets(board, cellA, cellB)
        val (validACandidates, validBCandidates) = cellA.candidates
            .flatMap { candidateA -> cellB.candidates.map { candidateB -> candidateA to candidateB } }
            .filterNot { (candidateA, candidateB) ->
                if (candidateA == candidateB) {
                    cellA isInSameUnit cellB
                } else {
                    val pairAsSet = EnumSet.of(candidateA, candidateB)
                    almostLockedSets.any { it.containsAll(pairAsSet) }
                }
            }
            .enumUnzip()
        val removalsA = cellA.candidates.filter { it !in validACandidates }.map { cellA to it }
        val removalsB = cellB.candidates.filter { it !in validBCandidates }.map { cellB to it }
        removalsA + removalsB
    }.mergeToRemoveCandidates()

private fun getAlmostLockedSets(
    board: Board<Cell>,
    cellA: UnsolvedCell,
    cellB: UnsolvedCell
): List<EnumSet<SudokuNumber>> {
    val visible = board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { it != cellA && it != cellB && it isInSameUnit cellA && it isInSameUnit cellB }
    val almostLockedSets1 = visible.map { it.candidates }.filter { it.size == 2 }
    val almostLockedSets2 = visible.zipEveryPair()
        .filter { (alsA, alsB) -> alsA isInSameUnit alsB }
        .map { (alsA, alsB) -> enumUnion(alsA.candidates, alsB.candidates) }
        .filter { it.size == 3 }
    val almostLockedSets3 = visible.zipEveryTriple()
        .filter { (alsA, alsB, alsC) -> alsA isInSameUnit alsB && alsA isInSameUnit alsC && alsB isInSameUnit alsC }
        .map { (alsA, alsB, alsC) -> enumUnion(alsA.candidates, alsB.candidates, alsC.candidates) }
        .filter { it.size == 4 }
    val almostLockedSets4 = visible.zipEveryQuad()
        .filter { (alsA, alsB, alsC, alsD) ->
            alsA isInSameUnit alsB &&
                    alsA isInSameUnit alsC &&
                    alsA isInSameUnit alsD &&
                    alsB isInSameUnit alsC &&
                    alsB isInSameUnit alsD &&
                    alsC isInSameUnit alsD
        }
        .map { (alsA, alsB, alsC, alsD) ->
            enumUnion(alsA.candidates, alsB.candidates, alsC.candidates, alsD.candidates)
        }
        .filter { it.size == 5 }
    return almostLockedSets1 + almostLockedSets2 + almostLockedSets3 + almostLockedSets4
}