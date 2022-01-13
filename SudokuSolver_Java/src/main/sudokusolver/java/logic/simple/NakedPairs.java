package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.UnsolvedCell;

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
        return board.getUnits()
                .stream()
                .flatMap(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().size() == 2)
                        .collect(Pair.zipEveryPair())
                        .filter(pair -> pair.first().candidates().equals(pair.second().candidates()))
                        .flatMap(pair -> {
                            var a = pair.first();
                            var b = pair.second();
                            return unit.stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> !cell.equals(a) && !cell.equals(b))
                                    .flatMap(cell -> {
                                        var toRemove = EnumSet.copyOf(cell.candidates());
                                        toRemove.retainAll(a.candidates());
                                        return toRemove.stream()
                                                .map(candidate -> new LocatedCandidate(cell, candidate));
                                    });
                        }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }
}
