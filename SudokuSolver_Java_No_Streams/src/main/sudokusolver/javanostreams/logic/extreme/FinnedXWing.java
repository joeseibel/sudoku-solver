package sudokusolver.javanostreams.logic.extreme;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

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
        var removals = new Removals();
        for (var candidate : SudokuNumber.values()) {
            finnedXWing(removals, board, candidate, board.rows(), Cell::column);
            finnedXWing(removals, board, candidate, board.getColumns(), Cell::row);
        }
        return removals.toList();
    }

    private static void finnedXWing(
            Removals removals,
            Board<Cell> board,
            SudokuNumber candidate,
            List<List<Cell>> units,
            ToIntFunction<Cell> getOtherUnitIndex
    ) {
        for (var baseUnit : units) {
            var withCandidate = new ArrayList<UnsolvedCell>();
            for (var cell : baseUnit) {
                if (cell instanceof UnsolvedCell unsolved && unsolved.candidates().contains(candidate)) {
                    withCandidate.add(unsolved);
                }
            }
            if (withCandidate.size() == 2) {
                var baseUnitCell1 = withCandidate.getFirst();
                var baseUnitCell2 = withCandidate.getLast();
                if (baseUnitCell1.block() != baseUnitCell2.block()) {
                    for (var finnedUnit : units) {
                        if (finnedUnit.getFirst().block() != baseUnit.getFirst().block()) {
                            var finnedUnitByBlock = new HashMap<Integer, List<UnsolvedCell>>();
                            for (var cell : finnedUnit) {
                                if (cell instanceof UnsolvedCell unsolved &&
                                        unsolved.candidates().contains(candidate)
                                ) {
                                    finnedUnitByBlock.computeIfAbsent(unsolved.block(), key -> new ArrayList<>())
                                            .add(unsolved);
                                }
                            }
                            if (finnedUnitByBlock.size() == 2) {
                                var finnedUnitCell1 = finnedUnit.get(getOtherUnitIndex.applyAsInt(baseUnitCell1));
                                var finnedUnitCell2 = finnedUnit.get(getOtherUnitIndex.applyAsInt(baseUnitCell2));
                                var firstAttempt = tryFin(
                                        removals,
                                        board,
                                        candidate,
                                        getOtherUnitIndex,
                                        finnedUnitByBlock,
                                        finnedUnitCell1,
                                        finnedUnitCell2
                                );
                                if (!firstAttempt) {
                                    tryFin(
                                            removals,
                                            board,
                                            candidate,
                                            getOtherUnitIndex,
                                            finnedUnitByBlock,
                                            finnedUnitCell2,
                                            finnedUnitCell1
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean tryFin(
            Removals removals,
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
                otherBlock.size() == 1 &&
                otherCorner instanceof UnsolvedCell &&
                otherBlock.contains(otherCorner)
        ) {
            for (var finnedCell : finnedBlock) {
                if (!finnedCell.equals(finnedCorner)) {
                    for (var cell : board.getBlock(finnedCorner.block())) {
                        if (cell instanceof UnsolvedCell unsolved &&
                                getOtherUnitIndex.applyAsInt(unsolved) == getOtherUnitIndex.applyAsInt(finnedCorner) &&
                                !unsolved.equals(finnedCorner) &&
                                unsolved.candidates().contains(candidate)
                        ) {
                            removals.add(unsolved, candidate);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}