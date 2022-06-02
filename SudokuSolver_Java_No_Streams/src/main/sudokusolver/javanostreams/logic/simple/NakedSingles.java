package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.List;

/*
 * https://www.sudokuwiki.org/Getting_Started
 *
 * If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
 */
public class NakedSingles {
    public static List<SetValue> nakedSingles(Board<Cell> board) {
        return board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().size() == 1)
                .map(cell -> new SetValue(cell, cell.candidates().iterator().next()))
                .toList();
    }
}
