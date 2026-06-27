package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.FilterType;
import sudokusolver.java.SetValue;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidates must be placed in that cell.
 */
public class HiddenSingles {
    public static List<SetValue> hiddenSingles(Board<Cell> board) {
        return board.getUnits().stream().flatMap(unit -> {
            var unsolved = unit.stream().gather(FilterType.of(UnsolvedCell.class)).toList();
            return Arrays.stream(SudokuNumber.values()).<SetValue>mapMulti((candidate, consumer) -> {
                var unsolvedWithCandidate = unsolved.stream()
                        .filter(cell -> cell.candidates().contains(candidate))
                        .toList();
                if (unsolvedWithCandidate.size() == 1) {
                    consumer.accept(new SetValue(unsolvedWithCandidate.getFirst(), candidate));
                }
            });
        }).distinct().toList();
    }
}