package sudokusolver.kotlin

sealed class BoardModification {
    abstract val row: Int
    abstract val column: Int
}

data class RemoveCandidates(
    override val row: Int,
    override val column: Int,
    val candidates: Iterable<SudokuNumber>
) : BoardModification()

data class SetValue(override val row: Int, override val column: Int, val value: SudokuNumber) : BoardModification()