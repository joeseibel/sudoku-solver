package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Quad;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Hidden_Candidates#HQ
 *
 * If four candidates exist across four cells in a unit, then those four candidates must be placed in those four cells.
 * All other candidates can be removed from those four cells.
 */
public class HiddenQuads {
    public static List<RemoveCandidates> hiddenQuads(Board<Cell> board) {
        return board.getUnits()
                .stream()
                .flatMap(unit -> Arrays.stream(SudokuNumber.values())
                        .collect(Quad.zipEveryQuad())
                        .flatMap(quad -> {
                            var a = quad.first();
                            var b = quad.second();
                            var c = quad.third();
                            var d = quad.fourth();
                            var cells = unit.stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> cell.candidates().contains(a) ||
                                            cell.candidates().contains(b) ||
                                            cell.candidates().contains(c) ||
                                            cell.candidates().contains(d))
                                    .toList();
                            if (cells.size() == 4) {
                                var union = cells.stream()
                                        .flatMap(cell -> cell.candidates().stream())
                                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class)));
                                if (union.contains(a) && union.contains(b) && union.contains(c) && union.contains(d)) {
                                    return cells.stream().flatMap(cell -> {
                                        var toRemove = EnumSet.copyOf(cell.candidates());
                                        toRemove.remove(a);
                                        toRemove.remove(b);
                                        toRemove.remove(c);
                                        toRemove.remove(d);
                                        return toRemove.stream().map(candidate -> new LocatedCandidate(cell, candidate));
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