package sudokusolver.javanostreams.logic.tough;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.Triple;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * https://www.sudokuwiki.org/Y_Wing_Strategy
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, all three cells each have two
 * candidates, there are three total candidates across the three cells, the hinge shares one candidate with one wing and
 * one candidate with the other wing, and the wing cells share a candidate among each other, then this third candidate
 * must be the solution to one of the wings. The third candidate can be removed from any cell which can see both wings.
 */
public class YWing {
    public static List<RemoveCandidates> yWing(Board<Cell> board) {
        var removals = new Removals();
        var cells = new ArrayList<UnsolvedCell>();
        for (var cell : board.getCells()) {
            if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().size() == 2) {
                cells.add(unsolved);
            }
        }
        for (var triple : Triple.zipEveryTriple(cells)) {
            var a = triple.first();
            var b = triple.second();
            var c = triple.third();
            var allCandidates = EnumSet.copyOf(a.candidates());
            allCandidates.addAll(b.candidates());
            allCandidates.addAll(c.candidates());
            if (allCandidates.size() == 3) {
                tryHinge(removals, board, a, b, c);
                tryHinge(removals, board, b, a, c);
                tryHinge(removals, board, c, a, b);
            }
        }
        return removals.toList();
    }

    private static void tryHinge(
            Removals removals,
            Board<Cell> board,
            UnsolvedCell hinge,
            UnsolvedCell wingA,
            UnsolvedCell wingB
    ) {
        var wingCandidates = EnumSet.copyOf(wingA.candidates());
        wingCandidates.retainAll(wingB.candidates());
        var hingeAndACandidates = EnumSet.copyOf(hinge.candidates());
        hingeAndACandidates.retainAll(wingA.candidates());
        var hingeAndBCandidates = EnumSet.copyOf(hinge.candidates());
        hingeAndBCandidates.retainAll(wingB.candidates());
        if (hinge.isInSameUnit(wingA) && hinge.isInSameUnit(wingB) &&
                hingeAndACandidates.size() == 1 && hingeAndBCandidates.size() == 1 && wingCandidates.size() == 1
        ) {
            var candidate = wingCandidates.iterator().next();
            for (var cell : board.getCells()) {
                if (cell instanceof UnsolvedCell unsolved &&
                        !unsolved.equals(wingA) &&
                        !unsolved.equals(wingB) &&
                        unsolved.candidates().contains(candidate) &&
                        unsolved.isInSameUnit(wingA) &&
                        unsolved.isInSameUnit(wingB)
                ) {
                    removals.add(unsolved, candidate);
                }
            }
        }
    }
}