package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals

fun assertRemoveCandidates(modification: RemoveCandidates, row: Int, column: Int, vararg candidates: Int) {
    assertEquals(
        RemoveCandidates(row, column, candidates.map { SudokuNumber.values()[it - 1] }.toEnumSet()),
        modification
    )
}

fun assertSetValue(modification: SetValue, row: Int, column: Int, value: Int) {
    assertEquals(SetValue(row, column, SudokuNumber.values()[value - 1]), modification)
}