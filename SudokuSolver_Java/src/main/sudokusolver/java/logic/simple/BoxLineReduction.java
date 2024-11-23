package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Intersection_Removal#LBR
 *
 * For a given row, if a candidate appears in only one block, then the candidate for that block must be placed in that
 * row. The candidate can be removed from the cells which are in the same block, but different rows.
 *
 * For a given column, if a candidate appears in only one block, then the candidate for that block must be placed in
 * that column. The candidate can be removed from cells which are in the same block, but different columns.
 */
public class BoxLineReduction {
    public static List<RemoveCandidates> boxLineReduction(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var rowModifications = boxLineReduction(
                            board,
                            candidate,
                            board.rows(),
                            Cell::row
                    );
                    var columnModifications = boxLineReduction(
                            board,
                            candidate,
                            board.getColumns(),
                            Cell::column
                    );
                    return Stream.concat(rowModifications, columnModifications);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> boxLineReduction(
            Board<Cell> board,
            SudokuNumber candidate,
            List<List<Cell>> units,
            ToIntFunction<Cell> getUnitIndex
    ) {
        return units.stream().flatMap(unit -> {
            var blockIndices = unit.stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(candidate))
                    .map(Cell::block)
                    .collect(Collectors.toSet());
            if (blockIndices.size() == 1) {
                var unitIndex = getUnitIndex.applyAsInt(unit.getFirst());
                return board.getBlock(blockIndices.iterator().next())
                        .stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> getUnitIndex.applyAsInt(cell) != unitIndex &&
                                cell.candidates().contains(candidate))
                        .map(cell -> new LocatedCandidate(cell, candidate));
            } else {
                return Stream.empty();
            }
        });
    }
}