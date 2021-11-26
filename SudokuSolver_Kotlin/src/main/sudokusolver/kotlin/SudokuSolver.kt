package sudokusolver.kotlin

import sudokusolver.kotlin.logic.MultipleSolutions
import sudokusolver.kotlin.logic.NoSolutions
import sudokusolver.kotlin.logic.SingleSolution
import sudokusolver.kotlin.logic.bruteForce
import sudokusolver.kotlin.logic.diabolical.alignedPairExclusion
import sudokusolver.kotlin.logic.diabolical.bug
import sudokusolver.kotlin.logic.diabolical.extendedUniqueRectangles
import sudokusolver.kotlin.logic.diabolical.hiddenUniqueRectangles
import sudokusolver.kotlin.logic.diabolical.jellyfish
import sudokusolver.kotlin.logic.diabolical.medusaRule1
import sudokusolver.kotlin.logic.diabolical.medusaRule2
import sudokusolver.kotlin.logic.diabolical.medusaRule3
import sudokusolver.kotlin.logic.diabolical.medusaRule4
import sudokusolver.kotlin.logic.diabolical.medusaRule5
import sudokusolver.kotlin.logic.diabolical.medusaRule6
import sudokusolver.kotlin.logic.diabolical.uniqueRectanglesType1
import sudokusolver.kotlin.logic.diabolical.uniqueRectanglesType2
import sudokusolver.kotlin.logic.diabolical.uniqueRectanglesType3
import sudokusolver.kotlin.logic.diabolical.uniqueRectanglesType3BWithTriplePseudoCells
import sudokusolver.kotlin.logic.diabolical.uniqueRectanglesType4
import sudokusolver.kotlin.logic.diabolical.uniqueRectanglesType5
import sudokusolver.kotlin.logic.diabolical.wxyzWing
import sudokusolver.kotlin.logic.diabolical.xCyclesRule1
import sudokusolver.kotlin.logic.diabolical.xCyclesRule2
import sudokusolver.kotlin.logic.diabolical.xCyclesRule3
import sudokusolver.kotlin.logic.diabolical.xyChains
import sudokusolver.kotlin.logic.extreme.emptyRectangles
import sudokusolver.kotlin.logic.extreme.finnedSwordfish
import sudokusolver.kotlin.logic.extreme.finnedXWing
import sudokusolver.kotlin.logic.extreme.groupedXCyclesRule1
import sudokusolver.kotlin.logic.extreme.groupedXCyclesRule2
import sudokusolver.kotlin.logic.extreme.groupedXCyclesRule3
import sudokusolver.kotlin.logic.simple.boxLineReduction
import sudokusolver.kotlin.logic.simple.hiddenPairs
import sudokusolver.kotlin.logic.simple.hiddenQuads
import sudokusolver.kotlin.logic.simple.hiddenSingles
import sudokusolver.kotlin.logic.simple.hiddenTriples
import sudokusolver.kotlin.logic.simple.nakedPairs
import sudokusolver.kotlin.logic.simple.nakedQuads
import sudokusolver.kotlin.logic.simple.nakedSingles
import sudokusolver.kotlin.logic.simple.nakedTriples
import sudokusolver.kotlin.logic.simple.pointingPairsPointingTriples
import sudokusolver.kotlin.logic.simple.pruneCandidates
import sudokusolver.kotlin.logic.tough.simpleColoringRule2
import sudokusolver.kotlin.logic.tough.simpleColoringRule4
import sudokusolver.kotlin.logic.tough.swordfish
import sudokusolver.kotlin.logic.tough.xWing
import sudokusolver.kotlin.logic.tough.xyzWing
import sudokusolver.kotlin.logic.tough.yWing

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: SudokuSolverKt board")
    } else {
        val board = args.single()
        if (board.length != UNIT_SIZE_SQUARED || board.any { it !in '0'..'9' }) {
            println("board must be $UNIT_SIZE_SQUARED numbers with blanks expressed as 0")
        } else {
            println(
                when (val solution = solve(parseOptionalBoard(board))) {
                    InvalidNoSolutions -> "No Solutions"
                    InvalidMultipleSolutions -> "Multiple Solutions"
                    is Solution -> solution.board

                    is UnableToSolve -> {
                        """
                            |Unable to solve:
                            |${solution.board.toString().lines().joinToString("\n")}
                            |
                            |Simple String: ${solution.board.toSimpleString()}
                            |
                            |With Candidates:
                            |${solution.board.toStringWithCandidates()}
                        """.trimMargin()
                    }
                }
            )
        }
    }
}

private sealed interface SolveResult
private object InvalidNoSolutions : SolveResult
private object InvalidMultipleSolutions : SolveResult
private class Solution(val board: Board<SudokuNumber>) : SolveResult
private class UnableToSolve(val board: Board<Cell>) : SolveResult

private fun solve(input: Board<SudokuNumber?>): SolveResult {
    when (val bruteForceSolution = bruteForce(input)) {
        NoSolutions -> return InvalidNoSolutions
        MultipleSolutions -> return InvalidMultipleSolutions

        is SingleSolution -> {
            val mutableBoard = createMutableCellBoard(input)
            do {
                if (mutableBoard.cells.filterIsInstance<UnsolvedCell>().isEmpty()) {
                    return Solution(bruteForceSolution.solution)
                }
                /*
                 * Why do I convert the MutableBoard to an immutable Board just to pass the immutable one to the logic
                 * functions? I wanted the logic functions to have the guarantee that the board will not change while
                 * they are running. Even if I have the logic functions take an AbstractBoard, that would be no
                 * guarantee. That would simply prevent the logic function from mutating the board without casting it.
                 * The only way to have mutability here and true immutability in the logic functions is to copy the
                 * contents from MutableBoard to Board.
                 *
                 * In this case, Rust would be better here than the JVM. In Rust, the board would be owned and mutable
                 * here. Read-only references would then be passed to the logic functions. The Rust compiler would
                 * ensure that the board is never modified here while read-only references are borrowed by the logic
                 * functions. With Rust, we could have mutability here, guaranteed immutability in the logic functions,
                 * and no copying.
                 */
                val board = mutableBoard.toBoard()
                //Start of simple solutions.
                val modifications = pruneCandidates(board)
                    .ifEmpty { nakedSingles(board) }
                    .ifEmpty { hiddenSingles(board) }
                    .ifEmpty { nakedPairs(board) }
                    .ifEmpty { nakedTriples(board) }
                    .ifEmpty { hiddenPairs(board) }
                    .ifEmpty { hiddenTriples(board) }
                    .ifEmpty { nakedQuads(board) }
                    .ifEmpty { hiddenQuads(board) }
                    .ifEmpty { pointingPairsPointingTriples(board) }
                    .ifEmpty { boxLineReduction(board) }
                    //Start of tough solutions.
                    .ifEmpty { xWing(board) }
                    .ifEmpty { simpleColoringRule2(board) }
                    .ifEmpty { simpleColoringRule4(board) }
                    .ifEmpty { yWing(board) }
                    .ifEmpty { swordfish(board) }
                    .ifEmpty { xyzWing(board) }
                    //Start of diabolical solutions.
                    .ifEmpty { xCyclesRule1(board) }
                    .ifEmpty { xCyclesRule2(board) }
                    .ifEmpty { xCyclesRule3(board) }
                    .ifEmpty { listOfNotNull(bug(board)) }
                    .ifEmpty { xyChains(board) }
                    .ifEmpty { medusaRule1(board) }
                    .ifEmpty { medusaRule2(board) }
                    .ifEmpty { medusaRule3(board) }
                    .ifEmpty { medusaRule4(board) }
                    .ifEmpty { medusaRule5(board) }
                    .ifEmpty { medusaRule6(board) }
                    .ifEmpty { jellyfish(board) }
                    .ifEmpty { uniqueRectanglesType1(board) }
                    .ifEmpty { uniqueRectanglesType2(board) }
                    .ifEmpty { uniqueRectanglesType3(board) }
                    .ifEmpty { uniqueRectanglesType3BWithTriplePseudoCells(board) }
                    .ifEmpty { uniqueRectanglesType4(board) }
                    .ifEmpty { uniqueRectanglesType5(board) }
                    .ifEmpty { extendedUniqueRectangles(board) }
                    .ifEmpty { hiddenUniqueRectangles(board) }
                    .ifEmpty { wxyzWing(board) }
                    .ifEmpty { alignedPairExclusion(board) }
                    //Start of extreme solutions.
                    .ifEmpty { groupedXCyclesRule1(board) }
                    .ifEmpty { groupedXCyclesRule2(board) }
                    .ifEmpty { groupedXCyclesRule3(board) }
                    .ifEmpty { emptyRectangles(board) }
                    .ifEmpty { finnedXWing(board) }
                    .ifEmpty { finnedSwordfish(board) }
                modifications.forEach { modification ->
                    val row = modification.row
                    val column = modification.column
                    val cell = mutableBoard[row, column]
                    check(cell is UnsolvedCell) { "[$row, $column] is already solved." }
                    val knownSolution = bruteForceSolution.solution[row, column]
                    when (modification) {
                        is RemoveCandidates -> {
                            modification.candidates.forEach { candidate ->
                                check(candidate != knownSolution) {
                                    "Cannot remove candidate $candidate from [$row, $column]"
                                }
                                check(candidate in cell.candidates) {
                                    "$candidate is not a candidate of [$row, $column]"
                                }
                                cell.candidates -= candidate
                            }
                        }

                        is SetValue -> {
                            val value = modification.value
                            check(value == knownSolution) {
                                "Cannot set value $value to [$row, $column]. Solution is $knownSolution"
                            }
                            mutableBoard[row, column] = SolvedCell(row, column, value)
                        }
                    }
                }
            } while (modifications.isNotEmpty())

            return UnableToSolve(mutableBoard.toBoard())
        }
    }
}