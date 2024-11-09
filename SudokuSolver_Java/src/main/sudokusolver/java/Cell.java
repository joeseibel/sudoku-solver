package sudokusolver.java;

public sealed interface Cell permits SolvedCell, UnsolvedCell {
    int row();

    int column();

    default int block() {
        return Board.getBlockIndex(row(), column());
    }
}