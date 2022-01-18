package sudokusolver.java.logic.simple;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Intersection_Removal#IR
 *
 * For a given block, if a candidate appears in only one row, then the candidate for that row must be placed in that
 * block. The candidate can be removed from cells which are in the same row, but different blocks.
 *
 * For a given block, if a candidate appears in only one column, then the candidate for that column must be placed in
 * that block. The candidate can be removed from cells which are in the same column, but different blocks.
 */
public class PointingPairsPointingTriples {
    public static List<RemoveCandidates> pointingPairsPointingTriples(Board<Cell> board) {
        return board.getBlocks()
                .stream()
                .flatMap(block -> Arrays.stream(SudokuNumber.values()).flatMap(candidate -> {
                    var rowModifications = pointingPairsPointingTriples(
                            block,
                            candidate,
                            board::getRow,
                            Cell::row
                    );
                    var columnModifications = pointingPairsPointingTriples(
                            block,
                            candidate,
                            board::getColumn,
                            Cell::column
                    );
                    return Stream.concat(rowModifications, columnModifications);
                }))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> pointingPairsPointingTriples(
            List<Cell> block,
            SudokuNumber candidate,
            Function<Integer, List<Cell>> getUnit,
            Function<Cell, Integer> getUnitIndex
    ) {
        var unitIndices = block.stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().contains(candidate))
                .map(getUnitIndex)
                .collect(Collectors.toSet());
        if (unitIndices.size() == 1) {
            return getUnit.apply(unitIndices.iterator().next())
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.block() != block.get(0).block() && cell.candidates().contains(candidate))
                    .map(cell -> new LocatedCandidate(cell, candidate));
        } else {
            return Stream.empty();
        }
    }
}
