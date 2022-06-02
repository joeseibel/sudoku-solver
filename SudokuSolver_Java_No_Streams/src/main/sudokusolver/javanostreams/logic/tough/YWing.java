package sudokusolver.javanostreams.logic.tough;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.Triple;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
        return board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().size() == 2)
                .collect(Triple.zipEveryTriple())
                .filter(triple -> {
                    var a = triple.first();
                    var b = triple.second();
                    var c = triple.third();
                    var allCandidates = EnumSet.copyOf(a.candidates());
                    allCandidates.addAll(b.candidates());
                    allCandidates.addAll(c.candidates());
                    return allCandidates.size() == 3;
                })
                .flatMap(triple -> {
                    var a = triple.first();
                    var b = triple.second();
                    var c = triple.third();
                    return Stream.of(
                            tryHinge(board, a, b, c),
                            tryHinge(board, b, a, c),
                            tryHinge(board, c, a, b)
                    ).flatMap(Function.identity());
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> tryHinge(
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
            return board.getCells()
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> !cell.equals(wingA) && !cell.equals(wingB) &&
                            cell.candidates().contains(candidate) &&
                            cell.isInSameUnit(wingA) && cell.isInSameUnit(wingB))
                    .map(cell -> new LocatedCandidate(cell, candidate));
        } else {
            return Stream.empty();
        }
    }
}
