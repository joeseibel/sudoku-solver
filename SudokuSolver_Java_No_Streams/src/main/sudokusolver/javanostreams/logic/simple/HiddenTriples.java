package sudokusolver.javanostreams.logic.simple;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.Triple;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Hidden_Candidates#HT
 *
 * If three candidates exist across three cells in a unit, then those three candidates must be placed in those three
 * cells. All other candidates can be removed from those three cells.
 */
public class HiddenTriples {
    public static List<RemoveCandidates> hiddenTriples(Board<Cell> board) {
        return board.getUnits()
                .stream()
                .flatMap(unit -> Arrays.stream(SudokuNumber.values())
                        .collect(Triple.zipEveryTriple())
                        .flatMap(triple -> {
                            var a = triple.first();
                            var b = triple.second();
                            var c = triple.third();
                            var cells = unit.stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> cell.candidates().contains(a) ||
                                            cell.candidates().contains(b) ||
                                            cell.candidates().contains(c))
                                    .toList();
                            if (cells.size() == 3) {
                                var union = cells.stream()
                                        .flatMap(cell -> cell.candidates().stream())
                                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
                                if (union.contains(a) && union.contains(b) && union.contains(c)) {
                                    return cells.stream().flatMap(cell -> {
                                        var toRemove = EnumSet.copyOf(cell.candidates());
                                        toRemove.remove(a);
                                        toRemove.remove(b);
                                        toRemove.remove(c);
                                        return toRemove.stream()
                                                .map(candidate -> new LocatedCandidate(cell, candidate));
                                    });
                                } else {
                                    return Stream.empty();
                                }
                            } else {
                                return Stream.empty();
                            }
                        }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }
}
