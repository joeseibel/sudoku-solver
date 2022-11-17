package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Rectangle;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/*
 * https://www.sudokuwiki.org/Hidden_Unique_Rectangles
 *
 * The basic premise of the Hidden Unique Rectangles solution is exactly the same as Unique Rectangles. The only
 * difference is that Hidden Unique Rectangles adds more specific types to the solution. These additional types look for
 * strong links between cells of a rectangle. A strong link exists between two cells for a given candidate when those
 * two cells are the only cells with the candidate in a given row or column.
 */
public class HiddenUniqueRectangles {
    public static List<RemoveCandidates> hiddenUniqueRectangles(Board<Cell> board) {
        var removals = new Removals();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var floor = new ArrayList<UnsolvedCell>();
            var roof = new ArrayList<UnsolvedCell>();
            for (var cell : rectangle.getCells()) {
                if (cell.candidates().size() == 2) {
                    floor.add(cell);
                } else {
                    roof.add(cell);
                }
            }
            if (floor.size() == 1) {
                type1(removals, board, rectangle, floor.get(0));
            } else if (roof.size() == 2) {
                type2(removals, board, roof, rectangle.getCommonCandidates());
            }
        }
        return removals.toList();
    }

    /*
     * Type 1
     *
     * If a rectangle has one floor cell, then consider the roof cell on the opposite corner of the rectangle. If one of
     * the common candidates appears twice in that cell's row and twice in that cell's column, which implies that the
     * other occurrences in that row and column are in the two other corners of the rectangle, then setting the other
     * common candidate as the value to that cell would lead to the Deadly Pattern. Therefore, the other common
     * candidate cannot be the solution to that cell. The other common candidate can be removed from the roof cell which
     * is opposite of the one floor cell.
     */
    private static void type1(Removals removals, Board<Cell> board, Rectangle rectangle, UnsolvedCell floor) {
        var row = new ArrayList<UnsolvedCell>();
        for (var cell : board.getRow(floor.row())) {
            if (cell instanceof UnsolvedCell unsolved) {
                row.add(unsolved);
            }
        }
        var column = new ArrayList<UnsolvedCell>();
        for (var cell : board.getColumn(floor.column())) {
            if (cell instanceof UnsolvedCell unsolved) {
                column.add(unsolved);
            }
        }
        var strongCandidates = EnumSet.noneOf(SudokuNumber.class);
        for (var candidate : rectangle.getCommonCandidates()) {
            var inRowCount = 0;
            for (var cell : row) {
                if (cell.candidates().contains(candidate)) {
                    inRowCount++;
                }
            }
            if (inRowCount == 2) {
                var inColumnCount = 0;
                for (var cell : column) {
                    if (cell.candidates().contains(candidate)) {
                        inColumnCount++;
                    }
                }
                if (inColumnCount == 2) {
                    strongCandidates.add(candidate);
                }
            }
        }
        if (strongCandidates.size() == 1) {
            var strongCandidate = strongCandidates.iterator().next();
            UnsolvedCell oppositeCell = null;
            for (var cell : rectangle.getCells()) {
                if (cell.row() != floor.row() && cell.column() != floor.column()) {
                    oppositeCell = cell;
                }
            }
            assert oppositeCell != null;
            SudokuNumber otherCandidate = null;
            for (var candidate : rectangle.getCommonCandidates()) {
                if (candidate != strongCandidate) {
                    otherCandidate = candidate;
                    break;
                }
            }
            assert otherCandidate != null;
            removals.add(oppositeCell, otherCandidate);
        }
    }

    /*
     * Type 2
     *
     * If a rectangle has two roof cells, those cells are in the same row, and there exists a strong link for one of the
     * common candidates between one of the roof cells and its corresponding floor cell in the same column, then setting
     * the other common candidate as the value to the other roof cell would lead to the Deadly Pattern. Therefore, the
     * other common candidate cannot be the solution to the other roof cell. The other common candidate can be removed
     * from the other roof cell.
     *
     * If a rectangle has two roof cells, those cells are in the same column, and there exists a strong link for one of
     * the common candidates between one of the roof cells and its corresponding floor cell in the same row, then
     * setting the other common candidate as the value to the other roof cell would lead to the Deadly Pattern.
     * Therefore, the other common candidate cannot be the solution to the other roof cell. The other common candidate
     * can be removed from the other roof cell.
     */
    private static void type2(
            Removals removals,
            Board<Cell> board,
            List<UnsolvedCell> roof,
            EnumSet<SudokuNumber> commonCandidates
    ) {
        var roofA = roof.get(0);
        var roofB = roof.get(1);
        var commonCandidatesArray = commonCandidates.toArray(SudokuNumber[]::new);
        var candidateA = commonCandidatesArray[0];
        var candidateB = commonCandidatesArray[1];
        if (roofA.row() == roofB.row()) {
            getRemoval(removals, roofA, roofB, candidateA, candidateB, Cell::column, board::getColumn);
        } else if (roofA.column() == roofB.column()) {
            getRemoval(removals, roofA, roofB, candidateA, candidateB, Cell::row, board::getRow);
        }
    }

    private static void getRemoval(
            Removals removals,
            UnsolvedCell roofA,
            UnsolvedCell roofB,
            SudokuNumber candidateA,
            SudokuNumber candidateB,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var unitAWithCandidateACount = 0;
        var unitAWithCandidateBCount = 0;
        for (var cell : getUnit.apply(getUnitIndex.applyAsInt(roofA))) {
            if (cell instanceof UnsolvedCell unsolved) {
                if (unsolved.candidates().contains(candidateA)) {
                    unitAWithCandidateACount++;
                }
                if (unsolved.candidates().contains(candidateB)) {
                    unitAWithCandidateBCount++;
                }
            }
        }
        if (unitAWithCandidateACount == 2) {
            removals.add(roofB, candidateB);
        } else if (unitAWithCandidateBCount == 2) {
            removals.add(roofB, candidateA);
        } else {
            var unitBWithCandidateACount = 0;
            var unitBWithCandidateBCount = 0;
            for (var cell : getUnit.apply(getUnitIndex.applyAsInt(roofB))) {
                if (cell instanceof UnsolvedCell unsolved) {
                    if (unsolved.candidates().contains(candidateA)) {
                        unitBWithCandidateACount++;
                    }
                    if (unsolved.candidates().contains(candidateB)) {
                        unitBWithCandidateBCount++;
                    }
                }
            }
            if (unitBWithCandidateACount == 2) {
                removals.add(roofA, candidateB);
            } else if (unitBWithCandidateBCount == 2) {
                removals.add(roofA, candidateA);
            }
        }
    }
}