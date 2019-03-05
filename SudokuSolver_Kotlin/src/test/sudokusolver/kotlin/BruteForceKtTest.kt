package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BruteForceKtTest {
    @Test
    fun bruteForceSingleSolution() {
        val board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010"
        val expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        assertEquals(SingleSolution(expected.toBoard()), bruteForce(board.toOptionalBoard()))
    }

    @Test
    fun bruteForceNoSolutions() {
        val board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010"
        assertEquals(NoSolutions, bruteForce(board.toOptionalBoard()))
    }

    @Test
    fun bruteForceMultipleSolutions() {
        val board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010"
        assertEquals(MultipleSolutions, bruteForce(board.toOptionalBoard()))
    }

    @Test
    fun bruteForceAlreadySolved() {
        val board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        assertEquals(SingleSolution(board.toBoard()), bruteForce(board.toOptionalBoard()))
    }

    @Test
    fun bruteForceInvalidSolution() {
        val board = "817942563234615789569837142451329678623781495978564321796158234182473956345296818"
        assertEquals(NoSolutions, bruteForce(board.toOptionalBoard()))
    }
}

private fun String.toBoard(): Board<SudokuNumber> {
    require(length == UNIT_SIZE * UNIT_SIZE)
    return Board(chunked(UNIT_SIZE).map { row ->
        row.map { cell ->
            SudokuNumber.values()[cell.toInt() - '0'.toInt() - 1]
        }
    })
}

private fun String.toOptionalBoard(): Board<SudokuNumber?> {
    require(length == UNIT_SIZE * UNIT_SIZE)
    return Board(chunked(UNIT_SIZE).map { row ->
        row.map { cell ->
            if (cell == '0') null else SudokuNumber.values()[cell.toInt() - '0'.toInt() - 1]
        }
    })
}