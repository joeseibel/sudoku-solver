package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import java.util.EnumSet

fun assertRemoveCandidates(modification: RemoveCandidates, row: Int, column: Int, vararg candidates: Int) {
    assertEquals(
        RemoveCandidates(
            row,
            column,
            candidates.mapTo(EnumSet.noneOf(SudokuNumber::class.java)) { SudokuNumber.values()[it - 1] }
        ),
        modification
    )
}

fun assertSetValue(modification: SetValue, row: Int, column: Int, value: Int) {
    assertEquals(SetValue(row, column, SudokuNumber.values()[value - 1]), modification)
}