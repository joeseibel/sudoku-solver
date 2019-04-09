package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.parseBoard
import sudokusolver.kotlin.parseOptionalBoard

internal class BruteForceKtTest {
    @Test
    fun testBruteForceSingleSolution() {
        val board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010"
        val expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        assertEquals(SingleSolution(parseBoard(expected)), bruteForce(parseOptionalBoard(board)))
    }

    @Test
    fun testBruteForceNoSolutions() {
        val board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010"
        assertEquals(NoSolutions, bruteForce(parseOptionalBoard(board)))
    }

    @Test
    fun testBruteForceMultipleSolutions() {
        val board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010"
        assertEquals(MultipleSolutions, bruteForce(parseOptionalBoard(board)))
    }

    @Test
    fun testBruteForceAlreadySolved() {
        val board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        assertEquals(SingleSolution(parseBoard(board)), bruteForce(parseOptionalBoard(board)))
    }

    @Test
    fun testBruteForceInvalidSolution() {
        val board = "817942563234615789569837142451329678623781495978564321796158234182473956345296818"
        assertEquals(NoSolutions, bruteForce(parseOptionalBoard(board)))
    }
}