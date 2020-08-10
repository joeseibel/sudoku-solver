package sudokusolver.kotlin.logic.diabolical

import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.enumUnion
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair
import sudokusolver.kotlin.zipEveryQuad

/*
 * https://www.sudokuwiki.org/WXYZ_Wing
 *
 * WXYZ-Wing applies for a quad of unsolved cells that has a total of four candidates among the quad. The quad may
 * contain restricted candidates and non-restricted candidates. A restricted candidate is one in which each cell of the
 * quad with the candidate can see every other cell of the quad with the candidate. A non-restricted candidate is one in
 * which at least one cell of the quad with the candidate cannot see every other cell of the quad with the candidate. If
 * a quad contains exactly one non-restricted candidate, then that candidate must be the solution to one of the cells of
 * the quad. The non-restricted candidate can be removed from any cell outside of the quad that can see every cell of
 * the quad with the candidate.
 */
fun wxyzWing(board: Board<Cell>): List<RemoveCandidates> =
    board.cells
        .filterIsInstance<UnsolvedCell>()
        .filter { it.candidates.size <= 4 }
        .zipEveryQuad()
        .mapNotNull { (a, b, c, d) ->
            val quad = listOf(a, b, c, d)
            enumUnion(a.candidates, b.candidates, c.candidates, d.candidates)
                .takeIf { it.size == 4 }
                ?.singleOrNull { candidate ->
                    quad.filter { candidate in it.candidates }.zipEveryPair().any { (a, b) -> !(a isInSameUnit b) }
                }
                ?.let { nonRestricted ->
                    val withCandidate = quad.filter { nonRestricted in it.candidates }
                    board.cells
                        .filterIsInstance<UnsolvedCell>()
                        .filter { cell ->
                            nonRestricted in cell.candidates &&
                                    cell !in quad &&
                                    withCandidate.all { cell isInSameUnit it }
                        }
                        .map { it to nonRestricted }
                }
        }
        .flatten()
        .mergeToRemoveCandidates()