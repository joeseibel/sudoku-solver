package sudokusolver.javanostreams.logic.extreme;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Pair;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/Empty_Rectangles
 *
 * This solution starts with looking for empty rectangles in blocks. An empty rectangle is a collection of four cells,
 * all contained within a single block, arranged in a rectangle, and none of them contain a particular candidate. The
 * cells can either be solved cells or unsolved cells without the candidate. For the other cells which are in the block,
 * but are outside the rectangle, at least two of them must contain the candidate and those cells must be in at least
 * two different rows and two different columns.
 *
 * This creates a situation in which two lines can be drawn through the block; one line along a row and the other along
 * a column. The two lines must not pass through any of the empty rectangle cells and all the cells with the candidate
 * must have a line pass through it. A valid block is one in which there is only one option for the placement of these
 * lines. This is why the cells with the candidate must be in at least two different rows and two different columns. The
 * cell in which these lines intersect is then used to find removals outside the block. The empty rectangle itself is
 * used to find a valid intersection point, but then the rectangle is disregarded for the remainder of the solution.
 *
 * Removals are looked for in cells which are outside the block, but which can see the intersection. If the intersection
 * can see one end of a strong link which is outside the intersection's block and there is another cell with the
 * candidate outside the intersection's block, but it can see the intersection and the other end of the strong link,
 * then there is a contradiction. If the candidate were to be set as the solution to the other cell, then the strong
 * link and this newly set solution would remove the candidate from every cell within the intersection's block, thus
 * invalidating that block. This means that the candidate cannot be the solution to that cell and can be removed.
 */
public class EmptyRectangles {
    public static List<RemoveCandidates> emptyRectangles(Board<Cell> board) {
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            for (var intersection : getIntersections(board, candidate)) {
                var row = intersection.first();
                var column = intersection.second();
                var block = board.get(row, column).block();
                getRemovals(
                        removals,
                        board,
                        candidate,
                        block,
                        board.getRow(row),
                        Cell::column,
                        board::getColumn,
                        Cell::row,
                        _ -> column
                );
                getRemovals(
                        removals,
                        board,
                        candidate,
                        block,
                        board.getColumn(column),
                        Cell::row,
                        board::getRow,
                        _ -> row,
                        Cell::column
                );
            }
        }
        return removals.toList();
    }

    private static void getRemovals(
            Removals removals,
            Board<Cell> board,
            SudokuNumber candidate,
            int block,
            List<Cell> unit,
            ToIntFunction<Cell> getOtherUnitIndex,
            IntFunction<List<Cell>> getOtherUnit,
            ToIntFunction<Cell> getRemovalRow,
            ToIntFunction<Cell> getRemovalColumn
    ) {
        for (var strongLink1 : unit) {
            if (strongLink1.block() != block && hasCandidate(strongLink1, candidate)) {
                Cell strongLink2 = null;
                for (var cell : getOtherUnit.apply(getOtherUnitIndex.applyAsInt(strongLink1))) {
                    if (hasCandidate(cell, candidate) && !cell.equals(strongLink1)) {
                        if (strongLink2 == null) {
                            strongLink2 = cell;
                        } else {
                            strongLink2 = null;
                            break;
                        }
                    }
                }
                removeIfValid(removals, board, candidate, getRemovalRow, getRemovalColumn, strongLink1, strongLink2);
            }
        }
    }

    /*
     * This method was split off from getRemovals due to a warning on which said, "Method 'getRemovals' is too complex
     * to analyze by data flow algorithm."
     */
    private static void removeIfValid(
            Removals removals,
            Board<Cell> board,
            SudokuNumber candidate,
            ToIntFunction<Cell> getRemovalRow,
            ToIntFunction<Cell> getRemovalColumn,
            Cell strongLink1,
            Cell strongLink2
    ) {
        if (strongLink2 != null &&
                strongLink1.block() != strongLink2.block() &&
                board.get(getRemovalRow.applyAsInt(strongLink2),
                        getRemovalColumn.applyAsInt(strongLink2)) instanceof UnsolvedCell removalCell &&
                removalCell.candidates().contains(candidate)
        ) {
            removals.add(removalCell, candidate);
        }
    }

    private static List<Pair<Integer, Integer>> getIntersections(Board<Cell> board, SudokuNumber candidate) {
        var intersections = new ArrayList<Pair<Integer, Integer>>();
        for (var row = 0; row < Board.UNIT_SIZE; row++) {
            var rowInBlock = row % Board.UNIT_SIZE_SQUARE_ROOT;
            var rectangleRow1 = rowInBlock == 0 ? row + 1 : row - rowInBlock;
            var rectangleRow2 = rowInBlock == 2 ? row - 1 : row - rowInBlock + 2;
            for (var column = 0; column < Board.UNIT_SIZE; column++) {
                var columnInBlock = column % Board.UNIT_SIZE_SQUARE_ROOT;
                var rectangleColumn1 = columnInBlock == 0 ? column + 1 : column - columnInBlock;
                var rectangleColumn2 = columnInBlock == 2 ? column - 1 : column - columnInBlock + 2;
                // Check that the rectangle is empty.
                if (!hasCandidate(board.get(rectangleRow1, rectangleColumn1), candidate) &&
                        !hasCandidate(board.get(rectangleRow1, rectangleColumn2), candidate) &&
                        !hasCandidate(board.get(rectangleRow2, rectangleColumn1), candidate) &&
                        !hasCandidate(board.get(rectangleRow2, rectangleColumn2), candidate) &&
                        // Check that at least one cell in the same block and row as the intersection has the candidate.
                        (hasCandidate(board.get(row, rectangleColumn1), candidate) ||
                                hasCandidate(board.get(row, rectangleColumn2), candidate)) &&
                        /*
                         * Check that at least one cell in the same block and column as the intersection has the
                         * candidate.
                         */
                        (hasCandidate(board.get(rectangleRow1, column), candidate) ||
                                hasCandidate(board.get(rectangleRow2, column), candidate))
                ) {
                    intersections.add(new Pair<>(row, column));
                }
            }
        }
        return intersections;
    }

    private static boolean hasCandidate(Cell cell, SudokuNumber candidate) {
        return cell instanceof UnsolvedCell(_, _, var candidates) && candidates.contains(candidate);
    }
}