package sudokusolver.java.logic.diabolical;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Quad;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Jelly_Fish_Strategy
 *
 * For a quad of rows, if a candidate appears in two, three, or four cells for each row and the candidate appears in
 * exactly four columns across the four rows, forming a four by four grid, then the candidate must be placed in four of
 * the sixteen cells. The candidate can be removed from cells which are in the four columns, but different rows.
 *
 * For a quad of columns, if a candidate appears in two, three, or four cells for each column and the candidate appears
 * in exactly four rows across the four columns, forming a four by four grid, then the candidate must be placed in four
 * of the sixteen cells. The candidate can be removed from cells which are in the four rows, but different columns
 */
public class Jellyfish {
    public static List<RemoveCandidates> jellyfish(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var rowRemovals = jellyfish(
                            candidate,
                            board.rows(),
                            board::getColumn,
                            Cell::column
                    );
                    var columnRemovals = jellyfish(
                            candidate,
                            board.getColumns(),
                            board::getRow,
                            Cell::row
                    );
                    return Stream.concat(rowRemovals, columnRemovals);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> jellyfish(
            SudokuNumber candidate,
            List<List<Cell>> units,
            IntFunction<List<Cell>> getOtherUnit,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        return units.stream().collect(Quad.zipEveryQuad()).flatMap(quad -> {
            var unitA = quad.first();
            var unitB = quad.second();
            var unitC = quad.third();
            var unitD = quad.fourth();
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
            var dWithCandidate = unitD.stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(candidate))
                    .toList();
            if (aWithCandidate.size() >= 2 && aWithCandidate.size() <= 4 &&
                    bWithCandidate.size() >= 2 && bWithCandidate.size() <= 4 &&
                    cWithCandidate.size() >= 2 && cWithCandidate.size() <= 4 &&
                    dWithCandidate.size() >= 2 && dWithCandidate.size() <= 4
            ) {
                var withCandidate = new ArrayList<UnsolvedCell>();
                withCandidate.addAll(aWithCandidate);
                withCandidate.addAll(bWithCandidate);
                withCandidate.addAll(cWithCandidate);
                withCandidate.addAll(dWithCandidate);
                var otherUnitIndices = withCandidate.stream()
                        .map(getOtherUnitIndex::applyAsInt)
                        .collect(Collectors.toSet());
                if (otherUnitIndices.size() == 4) {
                    return otherUnitIndices.stream()
                            .flatMap(otherUnitIndex -> getOtherUnit.apply(otherUnitIndex).stream())
                            .filter(UnsolvedCell.class::isInstance)
                            .map(UnsolvedCell.class::cast)
                            .filter(cell -> cell.candidates().contains(candidate) && !withCandidate.contains(cell))
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
