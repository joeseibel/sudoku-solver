package sudokusolver.javanostreams;

public record SolvedCell(int row, int column, SudokuNumber value) implements Cell {
    public SolvedCell {
        Board.validateRowAndColumn(row, column);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
