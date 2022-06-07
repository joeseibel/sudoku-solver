package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Hidden_Candidates#HP
 *
 * If a pair of candidates exists in exactly two cells in a unit, then those two candidates must be placed in those two
 * cells. All other candidates can be removed from those two cells.
 */
public class HiddenPairs {
    public static List<RemoveCandidates> hiddenPairs(Board<Cell> board) {
        var removals = new Removals();
        for (var unit : board.getUnits()) {
            for (var i = 0; i < SudokuNumber.values().length - 1; i++) {
                for (var j = i + 1; j < SudokuNumber.values().length; j++) {
                    var a = SudokuNumber.values()[i];
                    var b = SudokuNumber.values()[j];
                    var cellsWithA = new ArrayList<UnsolvedCell>();
                    for (var cell : unit) {
                        if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(a)) {
                            cellsWithA.add(unsolved);
                        }
                    }
                    var cellsWithB = new ArrayList<UnsolvedCell>();
                    for (var cell : unit) {
                        if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(b)) {
                            cellsWithB.add(unsolved);
                        }
                    }
                    if (cellsWithA.size() == 2 && cellsWithA.equals(cellsWithB)) {
                        for (var cell : cellsWithA) {
                            var toRemove = EnumSet.copyOf(cell.candidates());
                            toRemove.remove(a);
                            toRemove.remove(b);
                            if (!toRemove.isEmpty()) {
                                removals.add(cell, toRemove);
                            }
                        }
                    }
                }
            }
        }
        return removals.toList();
    }
}