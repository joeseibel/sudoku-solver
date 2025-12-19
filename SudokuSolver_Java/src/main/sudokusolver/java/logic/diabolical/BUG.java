package sudokusolver.java.logic.diabolical;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.SetValue;
import sudokusolver.java.UnsolvedCell;

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
        var cellsWithNotTwo = board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().size() != 2)
                .toList();
        if (cellsWithNotTwo.size() == 1) {
            var cell = cellsWithNotTwo.getFirst();
            if (cell.candidates().size() == 3) {
                var row = board.getRow(cell.row())
                        .stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .toList();
                var candidates = cell.candidates()
                        .stream()
                        .filter(candidate -> row.stream()
                                .filter(rowCell -> rowCell.candidates().contains(candidate))
                                .count() == 3)
                        .toList();
                assert candidates.size() == 1;
                return Optional.of(new SetValue(cell, candidates.getFirst()));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}