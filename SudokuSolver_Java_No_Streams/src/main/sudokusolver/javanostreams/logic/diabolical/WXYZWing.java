package sudokusolver.javanostreams.logic.diabolical;

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
        var removals = new Removals();
        var cells = new ArrayList<UnsolvedCell>();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() <= 4) {
                cells.add(unsolved);
            }
        }
        for (var i = 0; i < cells.size() - 3; i++) {
            var a = cells.get(i);
            for (var j = i + 1; j < cells.size() - 2; j++) {
                var b = cells.get(j);
                for (var k = j + 1; k < cells.size() - 1; k++) {
                    var c = cells.get(k);
                    for (var l = k + 1; l < cells.size(); l++) {
                        var d = cells.get(l);
                        var quadList = List.of(a, b, c, d);
                        var candidates = EnumSet.copyOf(a.candidates());
                        candidates.addAll(b.candidates());
                        candidates.addAll(c.candidates());
                        candidates.addAll(d.candidates());
                        if (candidates.size() == 4) {
                            var nonRestrictedList = EnumSet.noneOf(SudokuNumber.class);
                            for (var candidate : candidates) {
                                pairLoop:
                                for (var m = 0; m < quadList.size() - 1; m++) {
                                    var first = quadList.get(m);
                                    if (first.candidates().contains(candidate)) {
                                        for (var n = m + 1; n < quadList.size(); n++) {
                                            var second = quadList.get(n);
                                            if (second.candidates().contains(candidate) &&
                                                    !first.isInSameUnit(second)
                                            ) {
                                                nonRestrictedList.add(candidate);
                                                break pairLoop;
                                            }
                                        }
                                    }
                                }
                            }
                            if (nonRestrictedList.size() == 1) {
                                var nonRestricted = nonRestrictedList.iterator().next();
                                var withCandidate = new ArrayList<UnsolvedCell>();
                                for (var cell : quadList) {
                                    if (cell.candidates().contains(nonRestricted)) {
                                        withCandidate.add(cell);
                                    }
                                }
                                for (var cell : board.getCells()) {
                                    if (cell instanceof UnsolvedCell unsolved &&
                                            unsolved.candidates().contains(nonRestricted) &&
                                            !quadList.contains(unsolved)
                                    ) {
                                        var canSeeAllWithCandidate = true;
                                        for (var withCandidateCell : withCandidate) {
                                            if (!unsolved.isInSameUnit(withCandidateCell)) {
                                                canSeeAllWithCandidate = false;
                                                break;
                                            }
                                        }
                                        if (canSeeAllWithCandidate) {
                                            removals.add(unsolved, nonRestricted);
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