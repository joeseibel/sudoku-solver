package sudokusolver.java.logic.extreme;

import sudokusolver.java.Board;
import sudokusolver.java.Cell;
import sudokusolver.java.LocatedCandidate;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SudokuNumber;
import sudokusolver.java.UnsolvedCell;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Finned_X_Wing
 *
 * Finned X-Wing is an extension of X-Wing in which one of the corners of a rectangle of cells has a fin next to it. As
 * a reminder, X-Wing looks for a rectangle of unsolved cells with a particular candidate. If the candidate only appears
 * twice in each of the rows of the rectangle, then the candidate can be removed from the columns of the rectangle, but
 * in different rows. If the candidate only appears twice in each of the columns of the rectangle, then the candidate
 * can be removed from the rows of the rectangle, but in different columns.
 *
 * In Finned X-Wing, three of the corners of a rectangle will follow the same rules as X-Wing. Only one corner will have
 * additional unsolved cells with the candidate next to it. The fin must be in the same block as the corner, but the
 * corner itself may or may not have the candidate. If the corner does not have the candidate, the pattern is called a
 * Sashimi Finned X-Wing. From an implementation perspective, there is no difference between a regular Finned X-Wing and
 * Sashimi.
 *
 * For a pair of rows in different blocks, one row is the base row if the candidate appears exactly twice in that row,
 * but in different blocks. The other row is considered to be a finned row if the candidate appears in two blocks of
 * that row, one of those blocks of the row contains a regular corner, and the other block of the row contains a fin. A
 * regular corner is a cell with the candidate, it shares the same column as one of the candidates of the base row, and
 * there are no other candidates in that block of the row. The candidates of the base row along with the regular corner
 * form three corners of a rectangle with the fourth corner being a finned corner. The fourth corner may or may not have
 * the candidate. A fin is one or two cells in the finned row that do not share a column with either of the candidates
 * of the base row, but are in the same block as the finned corner. With all of these constraints, the candidate must be
 * placed in opposite corners of the rectangle, or the fin in the case of the finned corner. The candidate can be
 * removed from cells which are in the same column as the finned corner, the same block as the fin, but different rows.
 *
 * For a pair of columns in different blocks, one column is the base column if the candidate appears exactly twice in
 * that column, but in different blocks. The other column is considered to be a finned column if the candidate appears
 * in two blocks of that column, one of those blocks of the column contains a regular corner, and the other block of the
 * column contains a fin. A regular corner is a cell with the candidate, it shares the same row as one of the candidates
 * of the base column, and there are no other candidates in that block of the column. The candidates of the base column
 * along with the regular corner form three corners of a rectangle with the fourth corner being a finned corner. The
 * fourth corner may or may not have the candidate. A fin is one or two cells in the finned column that do not share a
 * row with either of the candidates of the base column, but are in the same block as the finned corner. With all of
 * these constraints, the candidate must be placed in opposite corners of the rectangle, or the fin in the case of the
 * finned corner. The candidate can be removed from cells which are in the same row as the finned corner, the same block
 * as the fin, but different columns.
 */
public class FinnedXWing {
    public static List<RemoveCandidates> finnedXWing(Board<Cell> board) {
        return Arrays.stream(SudokuNumber.values())
                .flatMap(candidate -> {
                    var rowRemovals = finnedXWing(board, candidate, board.rows(), Cell::column);
                    var columnRemovals = finnedXWing(
                            board,
                            candidate,
                            board.getColumns(),
                            Cell::row
                    );
                    return Stream.concat(rowRemovals, columnRemovals);
                })
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> finnedXWing(
            Board<Cell> board,
            SudokuNumber candidate,
            List<List<Cell>> units,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        return units.stream().flatMap(baseUnit -> {
            var withCandidate = baseUnit.stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> cell.candidates().contains(candidate))
                    .toList();
            if (withCandidate.size() == 2) {
                var baseUnitCell1 = withCandidate.getFirst();
                var baseUnitCell2 = withCandidate.getLast();
                if (baseUnitCell1.block() != baseUnitCell2.block()) {
                    return units.stream()
                            .filter(finnedUnit -> finnedUnit.getFirst().block() != baseUnit.getFirst().block())
                            .flatMap(finnedUnit -> {
                                var finnedUnitByBlock = finnedUnit.stream()
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .filter(cell -> cell.candidates().contains(candidate))
                                        .collect(Collectors.groupingBy(Cell::block));
                                if (finnedUnitByBlock.size() == 2) {
                                    var finnedUnitCell1 = finnedUnit.get(
                                            getOtherUnitIndex.applyAsInt(baseUnitCell1)
                                    );
                                    var finnedUnitCell2 = finnedUnit.get(
                                            getOtherUnitIndex.applyAsInt(baseUnitCell2)
                                    );
                                    var firstAttempt = tryFin(
                                            board,
                                            candidate,
                                            getOtherUnitIndex,
                                            finnedUnitByBlock,
                                            finnedUnitCell1,
                                            finnedUnitCell2
                                    );
                                    return firstAttempt
                                            .or(() -> tryFin(
                                                    board,
                                                    candidate,
                                                    getOtherUnitIndex,
                                                    finnedUnitByBlock,
                                                    finnedUnitCell2,
                                                    finnedUnitCell1
                                            ))
                                            .orElseGet(Stream::empty);
                                } else {
                                    return Stream.empty();
                                }
                            });
                } else {
                    return Stream.empty();
                }
            } else {
                return Stream.empty();
            }
        });
    }

    private static Optional<Stream<LocatedCandidate>> tryFin(
            Board<Cell> board,
            SudokuNumber candidate,
            ToIntFunction<Cell> getOtherUnitIndex,
            Map<Integer, List<UnsolvedCell>> finnedUnitByBlock,
            Cell finnedCorner,
            Cell otherCorner
    ) {
        var finnedBlock = finnedUnitByBlock.get(finnedCorner.block());
        var otherBlock = finnedUnitByBlock.get(otherCorner.block());
        if (finnedBlock != null &&
                otherBlock != null &&
                finnedBlock.stream().anyMatch(cell -> !cell.equals(finnedCorner)) &&
                otherBlock.size() == 1 &&
                otherCorner instanceof UnsolvedCell &&
                otherBlock.contains(otherCorner)
        ) {
            var modifications = board.getBlock(finnedCorner.block())
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> getOtherUnitIndex.applyAsInt(cell) == getOtherUnitIndex.applyAsInt(finnedCorner) &&
                            !cell.equals(finnedCorner) &&
                            cell.candidates().contains(candidate))
                    .map(cell -> new LocatedCandidate(cell, candidate));
            return Optional.of(modifications);
        } else {
            return Optional.empty();
        }
    }
}