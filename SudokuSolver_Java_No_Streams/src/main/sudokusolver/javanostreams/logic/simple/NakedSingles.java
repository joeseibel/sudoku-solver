package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Getting_Started
 *
 * If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
 */
public class NakedSingles {
    public static List<SetValue> nakedSingles(Board<Cell> board) {
        var modifications = new ArrayList<SetValue>();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() == 1) {
                modifications.add(new SetValue(unsolved, unsolved.candidates().iterator().next()));
            }
        }
        return modifications;
    }
}