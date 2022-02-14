package sudokusolver.java.logic.diabolical;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.Quad;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/WXYZ_Wing
 *
 * WXYZ-Wing applies for a quad of unsolved cells that has a total of four candidates among the quad. The quad may
 * contain restricted candidates and non-restricted candidates. A restricted candidate is one in which each cell of the
 * quad with the candidate can see every other cell of the quad with the candidate. A non-restricted candidate is one in
 * which at least one cell of the quad with the candidate cannot see every other cell of the quad with the candidate. If
 * a quad contains exactly one non-restricted candidate, then that candidate must be the solution to one of the cells of
 * the quad. The non-restricted candidate can be removed from any cell outside the quad that can see every cell of the
 * quad with the candidate.
 */
public class WXYZWing {
    public static List<RemoveCandidates> wxyzWing(Board<Cell> board) {
        return board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().size() <= 4)
                .collect(Quad.zipEveryQuad())
                .flatMap(quad -> {
                    var a = quad.first();
                    var b = quad.second();
                    var c = quad.third();
                    var d = quad.fourth();
                    var quadList = List.of(a, b, c, d);
                    var candidates = EnumSet.copyOf(a.candidates());
                    candidates.addAll(b.candidates());
                    candidates.addAll(c.candidates());
                    candidates.addAll(d.candidates());
                    if (candidates.size() == 4) {
                        var nonRestrictedList = candidates.stream()
                                .filter(candidate -> quadList.stream()
                                        .filter(cell -> cell.candidates().contains(candidate))
                                        .collect(Pair.zipEveryPair())
                                        .anyMatch(pair -> !pair.first().isInSameUnit(pair.second())))
                                .toList();
                        if (nonRestrictedList.size() == 1) {
                            var nonRestricted = nonRestrictedList.get(0);
                            var withCandidate = quadList.stream()
                                    .filter(cell -> cell.candidates().contains(nonRestricted))
                                    .toList();
                            return board.getCells()
                                    .stream()
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> cell.candidates().contains(nonRestricted) &&
                                            !quadList.contains(cell) &&
                                            withCandidate.stream().allMatch(cell::isInSameUnit))
                                    .map(cell -> new LocatedCandidate(cell, nonRestricted));
                        } else {
                            return Stream.empty();
                        }
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }
}
