package sudokusolver.java.logic.tough;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/XYZ_Wing
 *
 * Given a hinge cell and two wing cells such that the hinge can see both wings, the hinge has three candidates, the
 * wings have two candidates each, there is one candidate shared among all three cells, two candidates are shared
 * between the hinge and one wing and two candidates between the hinge and the other wing, and the common candidate is
 * the only one shared between the wings, then the common candidate must be the solution to one of the cells. The common
 * candidate can be removed from any cell which can see all three cells.
 */
public class XYZWing {
    public static List<RemoveCandidates> xyzWing(Board<Cell> board) {
        return board.getCells()
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(hinge -> hinge.candidates().size() == 3)
                .flatMap(hinge -> board.getCells()
                        .stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .collect(Pair.zipEveryPair())
                        .filter(pair -> {
                            var wingA = pair.first();
                            var wingB = pair.second();
                            var allCandidates = EnumSet.copyOf(hinge.candidates());
                            allCandidates.addAll(wingA.candidates());
                            allCandidates.addAll(wingB.candidates());
                            return wingA.candidates().size() == 2 && wingB.candidates().size() == 2 &&
                                    hinge.isInSameUnit(wingA) && hinge.isInSameUnit(wingB) &&
                                    allCandidates.size() == 3;
                        })
                        .flatMap(pair -> {
                            var wingA = pair.first();
                            var wingB = pair.second();
                            var toRemove = EnumSet.copyOf(wingA.candidates());
                            toRemove.retainAll(wingB.candidates());
                            if (toRemove.size() == 1) {
                                var candidate = toRemove.iterator().next();
                                return board.getCells()
                                        .stream()
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .filter(cell -> !cell.equals(hinge) &&
                                                !cell.equals(wingA) &&
                                                !cell.equals(wingB) &&
                                                cell.candidates().contains(candidate) &&
                                                cell.isInSameUnit(hinge) &&
                                                cell.isInSameUnit(wingA) &&
                                                cell.isInSameUnit(wingB))
                                        .map(cell -> new LocatedCandidate(cell, candidate));
                            } else {
                                return Stream.empty();
                            }
                        }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }
}
