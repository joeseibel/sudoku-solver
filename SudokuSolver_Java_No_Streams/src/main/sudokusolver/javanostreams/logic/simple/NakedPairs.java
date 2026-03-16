package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Naked_Candidates#NP
 *
 * If a pair of unsolved cells in a unit has the same two candidates, then those two candidates must be placed in those
 * two cells. The two candidates can be removed from every other cell in the unit.
 */
public class NakedPairs {
    public static List<RemoveCandidates> nakedPairs(Board<Cell> board) {
        var removals = new Removals();
        for (var unit : board.getUnits()) {
            for (var i = 0; i < unit.size() - 1; i++) {
                if (unit.get(i) instanceof UnsolvedCell a && a.candidates().size() == 2) {
                    for (var j = i + 1; j < unit.size(); j++) {
                        if (unit.get(j) instanceof UnsolvedCell b && a.candidates().equals(b.candidates())) {
                            for (var cell : unit) {
                                if (cell instanceof UnsolvedCell unsolved &&
                                        !unsolved.equals(a) && !unsolved.equals(b)
                                ) {
                                    var toRemove = EnumSet.copyOf(unsolved.candidates());
                                    toRemove.retainAll(a.candidates());
                                    removals.add(unsolved, toRemove);
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