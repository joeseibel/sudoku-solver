package sudokusolver.java.logic.diabolical;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.Rectangle;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> {
                    var partitioned = rectangle.getCells()
                            .stream()
                            .collect(Collectors.partitioningBy(cell -> cell.candidates().size() == 2));
                    var floor = partitioned.get(true);
                    var roof = partitioned.get(false);
                    if (floor.size() == 1) {
                        return type1(board, rectangle, floor.getFirst()).stream();
                    } else if (roof.size() == 2) {
                        return type2(board, roof, rectangle.getCommonCandidates()).stream();
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
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
    private static Optional<LocatedCandidate> type1(Board<Cell> board, Rectangle rectangle, UnsolvedCell floor) {
        var row = board.getRow(floor.row())
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .toList();
        var column = board.getColumn(floor.column())
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .toList();
        var strongCandidates = rectangle.getCommonCandidates()
                .stream()
                .filter(candidate -> {
                    var inRowCount = row.stream().filter(cell -> cell.candidates().contains(candidate)).count();
                    var inColumnCount = column.stream()
                            .filter(cell -> cell.candidates().contains(candidate))
                            .count();
                    return inRowCount == 2 && inColumnCount == 2;
                })
                .toList();
        if (strongCandidates.size() == 1) {
            var strongCandidate = strongCandidates.getFirst();
            var oppositeCell = rectangle.getCells()
                    .stream()
                    .filter(cell -> cell.row() != floor.row() && cell.column() != floor.column())
                    .findFirst()
                    .orElseThrow();
            var otherCandidate = rectangle.getCommonCandidates()
                    .stream()
                    .filter(candidate -> candidate != strongCandidate)
                    .findFirst()
                    .orElseThrow();
            return Optional.of(new LocatedCandidate(oppositeCell, otherCandidate));
        } else {
            return Optional.empty();
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
    private static Optional<LocatedCandidate> type2(
            Board<Cell> board,
            List<UnsolvedCell> roof,
            EnumSet<SudokuNumber> commonCandidates
    ) {
        var roofA = roof.getFirst();
        var roofB = roof.getLast();
        var commonCandidatesArray = commonCandidates.toArray(SudokuNumber[]::new);
        var candidateA = commonCandidatesArray[0];
        var candidateB = commonCandidatesArray[1];
        if (roofA.row() == roofB.row()) {
            return getRemoval(roofA, roofB, candidateA, candidateB, Cell::column, board::getColumn);
        } else if (roofA.column() == roofB.column()) {
            return getRemoval(roofA, roofB, candidateA, candidateB, Cell::row, board::getRow);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<LocatedCandidate> getRemoval(
            UnsolvedCell roofA,
            UnsolvedCell roofB,
            SudokuNumber candidateA,
            SudokuNumber candidateB,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var unitA = getUnit.apply(getUnitIndex.applyAsInt(roofA))
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .toList();
        var unitB = getUnit.apply(getUnitIndex.applyAsInt(roofB))
                .stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .toList();
        if (unitA.stream().filter(cell -> cell.candidates().contains(candidateA)).count() == 2) {
            return Optional.of(new LocatedCandidate(roofB, candidateB));
        } else if (unitA.stream().filter(cell -> cell.candidates().contains(candidateB)).count() == 2) {
            return Optional.of(new LocatedCandidate(roofB, candidateA));
        } else if (unitB.stream().filter(cell -> cell.candidates().contains(candidateA)).count() == 2) {
            return Optional.of(new LocatedCandidate(roofA, candidateB));
        } else if (unitB.stream().filter(cell -> cell.candidates().contains(candidateB)).count() == 2) {
            return Optional.of(new LocatedCandidate(roofA, candidateA));
        } else {
            return Optional.empty();
        }
    }
}