package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.SetValue;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidates must be placed in that cell.
 */
public class HiddenSingles {
    public static List<SetValue> hiddenSingles(Board<Cell> board) {
        return board.getUnits().stream().flatMap(unit -> {
            var unsolved = unit.stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .toList();
            return Arrays.stream(SudokuNumber.values()).flatMap(candidate -> {
                var unsolvedWithCandidate = unsolved.stream()
                        .filter(cell -> cell.candidates().contains(candidate))
                        .toList();
                if (unsolvedWithCandidate.size() == 1) {
                    return Stream.of(new SetValue(unsolvedWithCandidate.get(0), candidate));
                } else {
                    return Stream.empty();
                }
            });
        }).distinct().toList();
    }
}
