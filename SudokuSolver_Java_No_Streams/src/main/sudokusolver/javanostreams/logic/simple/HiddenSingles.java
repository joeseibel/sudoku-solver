package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Getting_Started
 *
 * If a candidate exists in only one cell in a unit, then the candidates must be placed in that cell.
 */
public class HiddenSingles {
    public static List<SetValue> hiddenSingles(Board<Cell> board) {
        var modifications = new HashSet<SetValue>();
        for (var unit : board.getUnits()) {
            for (var candidate : SudokuNumber.values()) {
                var withCandidate = new ArrayList<UnsolvedCell>();
                for (var cell : unit) {
                    if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                        withCandidate.add(unsolved);
                    }
                }
                if (withCandidate.size() == 1) {
                    modifications.add(new SetValue(withCandidate.get(0), candidate));
                }
            }
        }
        return List.copyOf(modifications);
    }
}