package sudokusolver.java.logic.extreme;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Pair;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Finned_Swordfish
 *
 * Finned Swordfish is an extension of Swordfish in a similar manner to the way that Finned X-Wing is an extension of
 * X-Wing. As a reminder, Swordfish looks for a 3x3 grid of cells in which a particular candidate appears in most or all
 * of those cells. If the candidate appears two or three times in each row of the grid and for those rows, the candidate
 * appears in exactly three columns, then the candidate can be removed from the columns of the grid, but in different
 * rows. If the candidate appears two or three times in each column of the grid and for those columns, the candidate
 * appears in exactly three rows, then the candidate can be removed from the rows of the grid, but in different columns.
 *
 * In Finned Swordfish, eight of the cells of a 3x3 grid will follow the same rules as Swordfish. Only one cell will
 * have additional unsolved cells with the candidate next to it. The fin must be in the same block as the cell, but the
 * cell itself may or may not have the candidate.
 *
 * For a triple of rows, two rows are the base rows if the candidate appears two or three times in each row and the
 * candidate appears in exactly three columns of the two rows. The remaining row is a finned row if the candidate
 * appears once or twice outside the three columns, but in the same block as one of the cells of the grid. That cell is
 * the finned cell. The candidate can be removed from cells that are in the same column as the finned cell, but are
 * outside the grid.
 *
 * For a triple of columns, two columns are the base columns if the candidate appears two or three times in each column
 * and the candidate appears in exactly three rows of the two columns. The remaining column is a finned column if the
 * candidate appears once or twice outside the three rows, but in the same block as one of the cells of the grid. That
 * cell is the finned cell. The candidate can be removed from cells that are in the same row as the finned cell, but are
 * outside the grid.
 */
public class FinnedSwordfish {
    public static List<RemoveCandidates> finnedSwordfish(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var rowRemovals = finnedSwordfish(
                            candidate,
                            board.rows(),
                            Cell::row,
                            Cell::column,
                            board::getColumn,
                            board::get
                    );
                    var columnRemovals = finnedSwordfish(
                            candidate,
                            board.getColumns(),
                            Cell::column,
                            Cell::row,
                            board::getRow,
                            (finnedUnitIndex, otherUnitIndex) -> board.get(otherUnitIndex, finnedUnitIndex)
                    );
                    return Stream.concat(rowRemovals, columnRemovals);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> finnedSwordfish(
            SudokuNumber candidate,
            List<List<Cell>> units,
            ToIntFunction<Cell> getUnitIndex,
            ToIntFunction<Cell> getOtherUnitIndex,
            IntFunction<List<Cell>> getOtherUnit,
            BiFunction<Integer, Integer, Cell> getFinnedCell
    ) {
        var unitsWithCandidate = units.stream()
                .map(unit -> unit.stream()
                        .filter(UnsolvedCell.class::isInstance)
                        .map(UnsolvedCell.class::cast)
                        .filter(cell -> cell.candidates().contains(candidate))
                        .toList())
                .filter(unit -> !unit.isEmpty())
                .toList();
        return unitsWithCandidate.stream()
                .filter(unit -> unit.size() == 2 || unit.size() == 3)
                .collect(Pair.zipEveryPair())
                .flatMap(pair -> {
                    var baseUnitA = pair.first();
                    var baseUnitB = pair.second();
                    var otherUnitIndices = Stream.concat(baseUnitA.stream(), baseUnitB.stream())
                            .map(getOtherUnitIndex::applyAsInt)
                            .collect(Collectors.toSet());
                    if (otherUnitIndices.size() == 3) {
                        return unitsWithCandidate.stream().flatMap(finnedUnit -> {
                            var finnedUnitIndex = getUnitIndex.applyAsInt(finnedUnit.getFirst());
                            var unitIndices = new HashSet<Integer>();
                            unitIndices.add(finnedUnitIndex);
                            unitIndices.add(getUnitIndex.applyAsInt(baseUnitA.getFirst()));
                            unitIndices.add(getUnitIndex.applyAsInt(baseUnitB.getFirst()));
                            if (unitIndices.size() == 3) {
                                var outsideOtherUnitIndices = finnedUnit.stream()
                                        .filter(cell -> !otherUnitIndices.contains(getOtherUnitIndex.applyAsInt(cell)))
                                        .toList();
                                if (outsideOtherUnitIndices.size() == 1 || outsideOtherUnitIndices.size() == 2) {
                                    var blockIndices = outsideOtherUnitIndices.stream()
                                            .map(Cell::block)
                                            .collect(Collectors.toSet());
                                    if (blockIndices.size() == 1) {
                                        var blockIndex = blockIndices.iterator().next();
                                        var finnedCells = otherUnitIndices.stream()
                                                .map(otherUnitIndex ->
                                                        getFinnedCell.apply(finnedUnitIndex, otherUnitIndex))
                                                .filter(finnedCell -> finnedCell.block() == blockIndex)
                                                .toList();
                                        if (finnedCells.size() == 1) {
                                            return getOtherUnit
                                                    .apply(getOtherUnitIndex.applyAsInt(finnedCells.getFirst()))
                                                    .stream()
                                                    .filter(UnsolvedCell.class::isInstance)
                                                    .map(UnsolvedCell.class::cast)
                                                    .filter(cell -> cell.candidates().contains(candidate) &&
                                                            cell.block() == blockIndex &&
                                                            !unitIndices.contains(getUnitIndex.applyAsInt(cell)))
                                                    .map(cell -> new LocatedCandidate(cell, candidate));
                                        } else {
                                            return Stream.empty();
                                        }
                                    } else {
                                        return Stream.empty();
                                    }
                                } else {
                                    return Stream.empty();
                                }
                            } else {
                                return Stream.empty();
                            }
                        });
                    } else {
                        return Stream.empty();
                    }
                });
    }
}