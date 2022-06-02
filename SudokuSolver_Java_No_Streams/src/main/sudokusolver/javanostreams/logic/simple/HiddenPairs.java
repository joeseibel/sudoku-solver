package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Pair;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Hidden_Candidates#HP
 *
 * If a pair of candidates exists in exactly two cells in a unit, then those two candidates must be placed in those two
 * cells. All other candidates can be removed from those two cells.
 */
public class HiddenPairs {
    public static List<RemoveCandidates> hiddenPairs(Board<Cell> board) {
        return board.getUnits()
                .stream()
                .flatMap(unit -> Arrays.stream(SudokuNumber.values())
                        .collect(Pair.zipEveryPair())
                        .flatMap(pair -> {
                            var a = pair.first();
                            var b = pair.second();
                            var cellsWithA = unit.stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> cell.candidates().contains(a))
                                    .toList();
                            var cellsWithB = unit.stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> cell.candidates().contains(b));
                            if (cellsWithA.size() == 2 && cellsWithA.equals(cellsWithB.toList())) {
                                return cellsWithA.stream().flatMap(cell -> {
                                    var toRemove = EnumSet.copyOf(cell.candidates());
                                    toRemove.remove(a);
                                    toRemove.remove(b);
                                    return toRemove.stream().map(candidate -> new LocatedCandidate(cell, candidate));
                                });
                            } else {
                                return Stream.empty();
                            }
                        }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }
}
