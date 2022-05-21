package sudokusolver.java.logic.tough;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.Triple;
import sudokusolver.java.UnsolvedCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Sword_Fish_Strategy
 *
 * For a triple of rows, if a candidate appears in two or three cells for each row and the candidate appears in exactly
 * three columns across the three rows, forming a three by three grid, then the candidate must be placed in three of the
 * nine cells. The candidate can be removed from cells which are in the three columns, but different rows.
 *
 * For a triple of columns, if a candidate appears in two or three cells for each column and the candidate appears in
 * exactly three rows across the three columns, forming a three by three grid, then the candidate must be placed in
 * three of the nine cells. The candidate can be removed from cells which are in the three rows, but different columns.
 */
public class Swordfish {
    public static List<RemoveCandidates> swordfish(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var rowRemovals = swordfish(
                            candidate,
                            board.rows(),
                            board::getColumn,
                            Cell::column
                    );
                    var columnRemovals = swordfish(
                            candidate,
                            board.getColumns(),
                            board::getRow,
                            Cell::row
                    );
                    return Stream.concat(rowRemovals, columnRemovals);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> swordfish(
            SudokuNumber candidate,
            List<List<Cell>> units,
            IntFunction<List<Cell>> getOtherUnit,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        return units.stream()
                .collect(Triple.zipEveryTriple())
                .flatMap(triple -> {
                    var unitA = triple.first();
                    var unitB = triple.second();
                    var unitC = triple.third();
                    var aWithCandidate = unitA.stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates().contains(candidate))
                            .toList();
                    var bWithCandidate = unitB.stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates().contains(candidate))
                            .toList();
                    var cWithCandidate = unitC.stream()
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates().contains(candidate))
                            .toList();
                    if ((aWithCandidate.size() == 2 || aWithCandidate.size() == 3) &&
                            (bWithCandidate.size() == 2 || bWithCandidate.size() == 3) &&
                            (cWithCandidate.size() == 2 || cWithCandidate.size() == 3)
                    ) {
                        var withCandidate = new ArrayList<>(aWithCandidate);
                        withCandidate.addAll(bWithCandidate);
                        withCandidate.addAll(cWithCandidate);
                        var otherUnitIndices = withCandidate.stream()
                                .map(getOtherUnitIndex::applyAsInt)
                                .collect(Collectors.toSet());
                        if (otherUnitIndices.size() == 3) {
                            return otherUnitIndices.stream()
                                    .flatMap(otherUnitIndex -> getOtherUnit.apply(otherUnitIndex).stream())
                                    .filter(UnsolvedCell.class::isInstance)
                                    .map(UnsolvedCell.class::cast)
                                    .filter(cell -> cell.candidates().contains(candidate) &&
                                            !withCandidate.contains(cell))
                                    .map(cell -> new LocatedCandidate(cell, candidate));
                        } else {
                            return Stream.empty();
                        }
                    } else {
                        return Stream.empty();
                    }
                });
    }
}
