package sudokusolver.java;

public record SolvedCell(int row, int column, SudokuNumber value) implements Cell {
    public SolvedCell {
        SudokuUtil.validateRowAndColumn(row, column);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
