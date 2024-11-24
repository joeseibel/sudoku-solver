package sudokusolver.javanostreams.logic.extreme;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

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
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            finnedSwordfish(removals, candidate, board.rows(), Cell::row, Cell::column, board::getColumn, board::get);
            finnedSwordfish(removals, candidate, board.getColumns(), Cell::column, Cell::row, board::getRow, (finnedUnitIndex, otherUnitIndex) -> board.get(otherUnitIndex, finnedUnitIndex));
        }
        return removals.toList();
    }

    private static void finnedSwordfish(
            Removals removals,
            SudokuNumber candidate,
            List<List<Cell>> units,
            ToIntFunction<Cell> getUnitIndex,
            ToIntFunction<Cell> getOtherUnitIndex,
            IntFunction<List<Cell>> getOtherUnit,
            BiFunction<Integer, Integer, Cell> getFinnedCell
    ) {
        var unitsWithCandidate = new ArrayList<List<UnsolvedCell>>();
        for (var unit : units) {
            var withCandidate = new ArrayList<UnsolvedCell>();
            for (var cell : unit) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    withCandidate.add(unsolved);
                }
            }
            if (!withCandidate.isEmpty()) {
                unitsWithCandidate.add(withCandidate);
            }
        }
        for (var i = 0; i < unitsWithCandidate.size() - 1; i++) {
            var baseUnitA = unitsWithCandidate.get(i);
            if (baseUnitA.size() == 2 || baseUnitA.size() == 3) {
                for (var j = i + 1; j < unitsWithCandidate.size(); j++) {
                    var baseUnitB = unitsWithCandidate.get(j);
                    if (baseUnitB.size() == 2 || baseUnitB.size() == 3) {
                        var otherUnitIndices = new HashSet<Integer>();
                        for (var cell : baseUnitA) {
                            otherUnitIndices.add(getOtherUnitIndex.applyAsInt(cell));
                        }
                        for (var cell : baseUnitB) {
                            otherUnitIndices.add(getOtherUnitIndex.applyAsInt(cell));
                        }
                        if (otherUnitIndices.size() == 3) {
                            for (var finnedUnit : unitsWithCandidate) {
                                var finnedUnitIndex = getUnitIndex.applyAsInt(finnedUnit.getFirst());
                                var unitIndices = new HashSet<Integer>();
                                unitIndices.add(finnedUnitIndex);
                                unitIndices.add(getUnitIndex.applyAsInt(baseUnitA.getFirst()));
                                unitIndices.add(getUnitIndex.applyAsInt(baseUnitB.getFirst()));
                                if (unitIndices.size() == 3) {
                                    var outsideOtherUnitIndices = new ArrayList<UnsolvedCell>();
                                    for (var cell : finnedUnit) {
                                        if (!otherUnitIndices.contains(getOtherUnitIndex.applyAsInt(cell))) {
                                            outsideOtherUnitIndices.add(cell);
                                        }
                                    }
                                    if (outsideOtherUnitIndices.size() == 1 || outsideOtherUnitIndices.size() == 2) {
                                        var blockIndices = new HashSet<Integer>();
                                        for (var cell : outsideOtherUnitIndices) {
                                            blockIndices.add(cell.block());
                                        }
                                        if (blockIndices.size() == 1) {
                                            var blockIndex = blockIndices.iterator().next();
                                            var finnedCells = new ArrayList<Cell>();
                                            for (var otherUnitIndex : otherUnitIndices) {
                                                var finnedCell = getFinnedCell
                                                        .apply(finnedUnitIndex, otherUnitIndex);
                                                if (finnedCell.block() == blockIndex) {
                                                    finnedCells.add(finnedCell);
                                                }
                                            }
                                            if (finnedCells.size() == 1) {
                                                for (var cell : getOtherUnit
                                                        .apply(getOtherUnitIndex.applyAsInt(finnedCells.getFirst()))
                                                ) {
                                                    if (cell instanceof UnsolvedCell unsolved &&
                                                            unsolved.candidates().contains(candidate) &&
                                                            unsolved.block() == blockIndex &&
                                                            !unitIndices.contains(getUnitIndex.applyAsInt(unsolved))
                                                    ) {
                                                        removals.add(unsolved, candidate);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}