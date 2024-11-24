package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.Optional;

/*
 * http://www.sudokuwiki.org/BUG
 *
 * BUG applies to boards with exactly one unsolved cell with three candidates and every other unsolved cell has two
 * candidates. Removing one of the candidates from the cell with three candidates will result in a board in which all of
 * its unsolved cells have two candidates, which would have multiple solutions. Since removing that candidate from that
 * cell would lead to an invalid board, that candidate must be the solution to that cell.
 *
 * For the three candidates of the cell, two candidates will appear twice in the cell's row, twice in the cell's column,
 * and twice in the cell's block, while one candidate will appear three times in the cell's row, three times in the
 * cell's column, and three times in the cell's block. This check is only performed against the cell's row.
 */
public class BUG {
    public static Optional<SetValue> bug(Board<Cell> board) {
        var cellsWithNotTwo = new ArrayList<UnsolvedCell>();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() != 2) {
                cellsWithNotTwo.add(unsolved);
            }
        }
        if (cellsWithNotTwo.size() == 1) {
            var cell = cellsWithNotTwo.getFirst();
            if (cell.candidates().size() == 3) {
                var row = new ArrayList<UnsolvedCell>();
                for (var rowCell : board.getRow(cell.row())) {
                    if (rowCell instanceof UnsolvedCell unsolved) {
                        row.add(unsolved);
                    }
                }
                var candidates = new ArrayList<SudokuNumber>();
                for (var candidate : cell.candidates()) {
                    var withCandidate = 0;
                    for (var rowCell : row) {
                        if (rowCell.candidates().contains(candidate)) {
                            withCandidate++;
                        }
                    }
                    if (withCandidate == 3) {
                        candidates.add(candidate);
                    }
                }
                assert candidates.size() == 1 : "There are multiple candidates that appear three times in the row";
                return Optional.of(new SetValue(cell, candidates.getFirst()));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}