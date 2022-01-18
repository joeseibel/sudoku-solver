package sudokusolver.java.logic.tough;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/X_Wing_Strategy
 *
 * For a pair of rows, if a candidate appears in only two columns of both rows and the columns are the same, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two columns, but different rows.
 *
 * For a pair of columns, if a candidate appears in only two rows of both columns and the rows are the ame, forming a
 * rectangle, then the candidate must be placed in opposite corners of the rectangle. The candidate can be removed from
 * cells which are in the two rows, but different columns.
 */
public class XWing {
    public static List<RemoveCandidates> xWing(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var rowModifications = xWing(
                            candidate,
                            board.rows(),
                            board::getColumn,
                            Cell::column
                    );
                    var columnModifications = xWing(
                            candidate,
                            board.getColumns(),
                            board::getRow,
                            Cell::row
                    );
                    return Stream.concat(rowModifications, columnModifications);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> xWing(
            SudokuNumber candidate,
            List<List<Cell>> units,
            Function<Integer, List<Cell>> getOtherUnit,
            Function<Cell, Integer> getOtherUnitIndex
    ) {
        return units.stream().collect(Pair.zipEveryPair()).flatMap(pair -> {
            var unitA = pair.first();
            var unitB = pair.second();
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
            if (aWithCandidate.size() == 2 && bWithCandidate.size() == 2 &&
                    getOtherUnitIndex.apply(aWithCandidate.get(0))
                            .equals(getOtherUnitIndex.apply(bWithCandidate.get(0))) &&
                    getOtherUnitIndex.apply(aWithCandidate.get(1))
                            .equals(getOtherUnitIndex.apply(bWithCandidate.get(1)))
            ) {
                var otherUnitA = getOtherUnit.apply(getOtherUnitIndex.apply(aWithCandidate.get(0)));
                var otherUnitB = getOtherUnit.apply(getOtherUnitIndex.apply(aWithCandidate.get(1)));
                return Stream.concat(otherUnitA.stream(), otherUnitB.stream())
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate) &&
                                !unitA.contains(cell) &&
                                !unitB.contains(cell))
                        .map(cell -> new LocatedCandidate(cell, candidate));
            } else {
                return Stream.empty();
            }
        });
    }
}
