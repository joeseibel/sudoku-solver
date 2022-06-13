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
 * https://www.sudokuwiki.org/Hidden_Candidates#HQ
 *
 * If four candidates exist across four cells in a unit, then those four candidates must be placed in those four cells.
 * All other candidates can be removed from those four cells.
 */
public class HiddenQuads {
    public static List<RemoveCandidates> hiddenQuads(Board<Cell> board) {
        var removals = new Removals();
        for (var unit : board.getUnits()) {
            for (var i = 0; i < SudokuNumber.values().length - 3; i++) {
                var a = SudokuNumber.values()[i];
                for (var j = i + 1; j < SudokuNumber.values().length - 2; j++) {
                    var b = SudokuNumber.values()[j];
                    for (var k = j + 1; k < SudokuNumber.values().length - 1; k++) {
                        var c = SudokuNumber.values()[k];
                        for (var l = k + 1; l < SudokuNumber.values().length; l++) {
                            var d = SudokuNumber.values()[l];
                            var cells = new ArrayList<UnsolvedCell>();
                            for (var cell : unit) {
                                if (cell instanceof UnsolvedCell unsolved &&
                                        (unsolved.candidates().contains(a) ||
                                                unsolved.candidates().contains(b) ||
                                                unsolved.candidates().contains(c) ||
                                                unsolved.candidates().contains(d))
                                ) {
                                    cells.add(unsolved);
                                }
                            }
                            if (cells.size() == 4) {
                                var union = EnumSet.noneOf(SudokuNumber.class);
                                for (var cell : cells) {
                                    union.addAll(cell.candidates());
                                }
                                if (union.contains(a) && union.contains(b) && union.contains(c) && union.contains(d)) {
                                    for (var cell : cells) {
                                        var toRemove = EnumSet.copyOf(cell.candidates());
                                        toRemove.remove(a);
                                        toRemove.remove(b);
                                        toRemove.remove(c);
                                        toRemove.remove(d);
                                        for (var candidate : toRemove) {
                                            removals.add(cell, EnumSet.of(candidate));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return removals.toList();
    }
}