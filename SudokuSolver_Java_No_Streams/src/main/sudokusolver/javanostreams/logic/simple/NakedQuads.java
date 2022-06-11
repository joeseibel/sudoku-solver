package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Naked_Candidates#NQ
 *
 * If a unit has four unsolved cells with a total of four candidates among them, then those four candidates must be
 * placed in those four cells. The four candidates can be removed from every other cell in the unit.
 */
public class NakedQuads {
    public static List<RemoveCandidates> nakedQuads(Board<Cell> board) {
        var removals = new Removals();
        for (var unit : board.getUnits()) {
            var unsolvedInUnit = new ArrayList<UnsolvedCell>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved) {
                    unsolvedInUnit.add(unsolved);
                }
            }
            for (var i = 0; i < unsolvedInUnit.size() - 3; i++) {
                var a = unsolvedInUnit.get(i);
                for (var j = i + 1; j < unsolvedInUnit.size() - 2; j++) {
                    var b = unsolvedInUnit.get(j);
                    for (var k = j + 1; k < unsolvedInUnit.size() - 1; k++) {
                        var c = unsolvedInUnit.get(k);
                        for (var l = k + 1; l < unsolvedInUnit.size(); l++) {
                            var d = unsolvedInUnit.get(l);
                            var unionOfCandidates = EnumSet.copyOf(a.candidates());
                            unionOfCandidates.addAll(b.candidates());
                            unionOfCandidates.addAll(c.candidates());
                            unionOfCandidates.addAll(d.candidates());
                            if (unionOfCandidates.size() == 4) {
                                for (var cell : unsolvedInUnit) {
                                    if (!cell.equals(a) && !cell.equals(b) && !cell.equals(c) && !cell.equals(d)) {
                                        var toRemove = EnumSet.copyOf(cell.candidates());
                                        toRemove.retainAll(unionOfCandidates);
                                        removals.add(cell, toRemove);
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