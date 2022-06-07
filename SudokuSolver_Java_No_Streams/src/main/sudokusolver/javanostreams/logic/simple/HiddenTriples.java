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
 * https://www.sudokuwiki.org/Hidden_Candidates#HT
 *
 * If three candidates exist across three cells in a unit, then those three candidates must be placed in those three
 * cells. All other candidates can be removed from those three cells.
 */
public class HiddenTriples {
    public static List<RemoveCandidates> hiddenTriples(Board<Cell> board) {
        var removals = new Removals();
        for (var unit : board.getUnits()) {
            for (var i = 0; i < SudokuNumber.values().length - 2; i++) {
                var a = SudokuNumber.values()[i];
                for (var j = i + 1; j < SudokuNumber.values().length - 1; j++) {
                    var b = SudokuNumber.values()[j];
                    for (var k = j + 1; k < SudokuNumber.values().length; k++) {
                        var c = SudokuNumber.values()[k];
                        var cells = new ArrayList<UnsolvedCell>();
                        for (var cell : unit) {
                            if (cell instanceof UnsolvedCell unsolved &&
                                    (unsolved.candidates().contains(a) ||
                                            unsolved.candidates().contains(b) ||
                                            unsolved.candidates().contains(c))
                            ) {
                                cells.add(unsolved);
                            }
                        }
                        if (cells.size() == 3) {
                            var union = EnumSet.noneOf(SudokuNumber.class);
                            for (var cell : cells) {
                                union.addAll(cell.candidates());
                            }
                            if (union.contains(a) && union.contains(b) && union.contains(c)) {
                                for (var cell : cells) {
                                    var toRemove = EnumSet.copyOf(cell.candidates());
                                    toRemove.remove(a);
                                    toRemove.remove(b);
                                    toRemove.remove(c);
                                    removals.add(cell, toRemove);
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