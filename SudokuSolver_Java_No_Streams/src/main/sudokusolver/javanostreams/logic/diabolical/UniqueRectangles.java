package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.LocatedCandidate;
import sudokusolver.javanostreams.Pair;
import sudokusolver.javanostreams.Rectangle;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/*
 * https://www.sudokuwiki.org/Unique_Rectangles
 *
 * The Unique Rectangles solution works by identifying the potential for an invalid pattern of candidates called the
 * Deadly Pattern and then removing candidates that if set as the value would lead to the Deadly Pattern. A Deadly
 * Pattern is defined as a group of four unsolved cells arranged to form a rectangle, each cell containing the same two
 * candidates and only those candidates, and the cells being located in two rows, two columns, and two blocks. If a
 * board contains the Deadly Pattern, then the board cannot have a single solution, but would have multiple solutions.
 * The advantage of recognizing this pattern comes when a board contains a pattern which is close to the Deadly Pattern
 * and the removal of certain candidates would lead to the Deadly Pattern. If a valid board contains a pattern which is
 * close to the Deadly Pattern, it is known that the board will never enter into the Deadly Pattern and candidates can
 * be removed if setting those candidates as values would lead to the Deadly Pattern. A rectangle can be further
 * described by identifying its floor cells and its roof cells. A rectangle's floor are the cells that only contain the
 * two common candidates. A rectangle's roof are the cells that contain the two common candidates as well as additional
 * candidates.
 */
public class UniqueRectangles {
    /*
     * Type 1
     *
     * If a rectangle has one roof cell, then this is a potential Deadly Pattern. If the additional candidates were to
     * be removed from the roof, then that would lead to a Deadly Pattern. The two common candidates can be removed from
     * the roof leaving only the additional candidates remaining.
     */
    public static List<RemoveCandidates> uniqueRectanglesType1(Board<Cell> board) {
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> Optional.of(rectangle.getRoof())
                        .filter(roof -> roof.size() == 1)
                        .map(roof -> {
                            var roofCell = roof.get(0);
                            return rectangle.getCommonCandidates()
                                    .stream()
                                    .map(candidate -> new LocatedCandidate(roofCell, candidate));
                        })
                        .orElseGet(Stream::empty))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    /*
     * Type 2
     *
     * If a rectangle has two roof cells and there is only one additional candidate appearing in both roof cells, then
     * this is a potential Deadly Pattern. If the additional candidate were to be removed from the roof cells, then that
     * would lead to a Deadly Pattern, therefore the additional candidate must be the solution for one of the two roof
     * cells. The common candidate can be removed from any other cell that can see both of the roof cells.
     */
    public static List<RemoveCandidates> uniqueRectanglesType2(Board<Cell> board) {
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> Optional.of(rectangle.getRoof())
                        .filter(roof -> roof.size() == 2)
                        .map(roof -> {
                            var roofA = roof.get(0);
                            var roofB = roof.get(1);
                            if (roofA.candidates().size() == 3 && roofA.candidates().equals(roofB.candidates())) {
                                var additionalCandidates = EnumSet.copyOf(roofA.candidates());
                                additionalCandidates.removeAll(rectangle.getCommonCandidates());
                                assert additionalCandidates.size() == 1;
                                var additionalCandidate = additionalCandidates.iterator().next();
                                return board.getCells()
                                        .stream()
                                        .filter(UnsolvedCell.class::isInstance)
                                        .map(UnsolvedCell.class::cast)
                                        .filter(cell -> cell.candidates().contains(additionalCandidate) &&
                                                !cell.equals(roofA) &&
                                                !cell.equals(roofB) &&
                                                cell.isInSameUnit(roofA) &&
                                                cell.isInSameUnit(roofB))
                                        .map(cell -> new LocatedCandidate(cell, additionalCandidate));
                            } else {
                                return Stream.<LocatedCandidate>empty();
                            }
                        })
                        .orElseGet(Stream::empty))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    /*
     * Type 3
     *
     * If a rectangle has two roof cells, each roof cell has one additional candidate, and the additional candidates are
     * different, then this is a potential Deadly Pattern. One or both of these additional candidates must be the
     * solution, so the roof cells can be treated as a single cell with the two additional candidates. If there is
     * another cell that can see both roof cells and has the additional candidates as its candidates, then the roof
     * cells and the other cell effectively form a Naked Pair. The additional candidates can be removed from any other
     * cell in the unit.
     */
    public static List<RemoveCandidates> uniqueRectanglesType3(Board<Cell> board) {
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> Optional.of(rectangle.getRoof())
                        .filter(roof -> roof.size() == 2)
                        .map(roof -> {
                            var roofA = roof.get(0);
                            var roofB = roof.get(1);
                            if (roofA.candidates().size() == 3 && roofB.candidates().size() == 3 &&
                                    !roofA.candidates().equals(roofB.candidates())
                            ) {
                                var additionalCandidates = EnumSet.copyOf(roofA.candidates());
                                additionalCandidates.addAll(roofB.candidates());
                                additionalCandidates.removeAll(rectangle.getCommonCandidates());
                                var rowRemovals = getRemovalsType3(
                                        roofA,
                                        roofB,
                                        additionalCandidates,
                                        Cell::row,
                                        board::getRow
                                );
                                var columnRemovals = getRemovalsType3(
                                        roofA,
                                        roofB,
                                        additionalCandidates,
                                        Cell::column,
                                        board::getColumn
                                );
                                var blockRemovals = getRemovalsType3(
                                        roofA,
                                        roofB,
                                        additionalCandidates,
                                        Cell::block,
                                        board::getBlock
                                );
                                return Stream.of(rowRemovals, columnRemovals, blockRemovals)
                                        .flatMap(Function.identity());
                            } else {
                                return Stream.<LocatedCandidate>empty();
                            }
                        })
                        .orElseGet(Stream::empty))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> getRemovalsType3(
            UnsolvedCell roofA,
            UnsolvedCell roofB,
            EnumSet<SudokuNumber> additionalCandidates,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var indexA = getUnitIndex.applyAsInt(roofA);
        var indexB = getUnitIndex.applyAsInt(roofB);
        if (indexA == indexB) {
            var unit = getUnit.apply(indexA)
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .toList();
            return unit.stream()
                    .filter(cell -> cell.candidates().equals(additionalCandidates))
                    .findFirst()
                    .map(pairCell -> unit.stream()
                            .filter(cell -> !cell.equals(pairCell) && !cell.equals(roofA) && !cell.equals(roofB))
                            .flatMap(cell -> {
                                var removeCandidates = EnumSet.copyOf(cell.candidates());
                                removeCandidates.retainAll(additionalCandidates);
                                return removeCandidates.stream()
                                        .map(candidate -> new LocatedCandidate(cell, candidate));
                            }))
                    .orElseGet(Stream::empty);
        } else {
            return Stream.empty();
        }
    }

    /*
     * Type 3/3b with Triple Pseudo-Cells
     *
     * If a rectangle has two roof cells, then this is a potential Deadly Pattern. If the roof cells can see two other
     * cells and the union of candidates among the roof cells' additional candidates and the other cells' candidates is
     * three candidates, then the roof cells and the other two cells effectively form a Naked Triple. The three
     * candidates in the union can be removed from any other cell in the unit.
     */
    public static List<RemoveCandidates> uniqueRectanglesType3BWithTriplePseudoCells(Board<Cell> board) {
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> Optional.of(rectangle.getRoof())
                        .filter(roof -> roof.size() == 2)
                        .map(roof -> {
                            var roofA = roof.get(0);
                            var roofB = roof.get(1);
                            var additionalCandidates = EnumSet.copyOf(roofA.candidates());
                            additionalCandidates.addAll(roofB.candidates());
                            additionalCandidates.removeAll(rectangle.getCommonCandidates());
                            var rowRemovals = getRemovalsType3B(
                                    roofA,
                                    roofB,
                                    additionalCandidates,
                                    Cell::row,
                                    board::getRow
                            );
                            var columnRemovals = getRemovalsType3B(
                                    roofA,
                                    roofB,
                                    additionalCandidates,
                                    Cell::column,
                                    board::getColumn
                            );
                            var blockRemovals = getRemovalsType3B(
                                    roofA,
                                    roofB,
                                    additionalCandidates,
                                    Cell::block,
                                    board::getBlock
                            );
                            return Stream.of(rowRemovals, columnRemovals, blockRemovals).flatMap(Function.identity());
                        })
                        .orElseGet(Stream::empty))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> getRemovalsType3B(
            UnsolvedCell roofA,
            UnsolvedCell roofB,
            EnumSet<SudokuNumber> additionalCandidates,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var indexA = getUnitIndex.applyAsInt(roofA);
        var indexB = getUnitIndex.applyAsInt(roofB);
        if (indexA == indexB) {
            var unit = getUnit.apply(indexA)
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast)
                    .filter(cell -> !cell.equals(roofA) && !cell.equals(roofB))
                    .toList();
            return unit.stream()
                    .collect(Pair.zipEveryPair())
                    .flatMap(pair -> {
                        var tripleA = pair.first();
                        var tripleB = pair.second();
                        var tripleCandidates = EnumSet.copyOf(additionalCandidates);
                        tripleCandidates.addAll(tripleA.candidates());
                        tripleCandidates.addAll(tripleB.candidates());
                        if (tripleCandidates.size() == 3) {
                            return unit.stream()
                                    .filter(cell -> !cell.equals(tripleA) && !cell.equals(tripleB))
                                    .flatMap(cell -> {
                                        var removeCandidates = EnumSet.copyOf(cell.candidates());
                                        removeCandidates.retainAll(tripleCandidates);
                                        return removeCandidates.stream()
                                                .map(candidate -> new LocatedCandidate(cell, candidate));
                                    });
                        } else {
                            return Stream.empty();
                        }
                    });
        } else {
            return Stream.empty();
        }
    }

    /*
     * Type 4
     *
     * If a rectangle has two roof cells, then this is a potential Deadly Pattern. For a unit common to the roof cells,
     * if one of the common candidates are only found in the roof cells of that unit, then setting the other candidate
     * as the solution to one of the roof cells would lead to the Deadly Pattern. The other common candidate can be
     * removed from the roof cells.
     */
    public static List<RemoveCandidates> uniqueRectanglesType4(Board<Cell> board) {
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> Optional.of(rectangle.getRoof())
                        .filter(roof -> roof.size() == 2)
                        .map(roof -> {
                            var commonCandidates = rectangle.getCommonCandidates()
                                    .toArray(SudokuNumber[]::new);
                            var rowRemovals = getRemovalsType4(
                                    roof,
                                    commonCandidates,
                                    Cell::row,
                                    board::getRow
                            );
                            var columnRemovals = getRemovalsType4(
                                    roof,
                                    commonCandidates,
                                    Cell::column,
                                    board::getColumn
                            );
                            var blockRemovals = getRemovalsType4(
                                    roof,
                                    commonCandidates,
                                    Cell::block,
                                    board::getBlock
                            );
                            return Stream.of(rowRemovals, columnRemovals, blockRemovals).flatMap(Function.identity());
                        })
                        .orElseGet(Stream::empty))
                .collect(LocatedCandidate.mergeToRemoveCandidates());
    }

    private static Stream<LocatedCandidate> getRemovalsType4(
            List<UnsolvedCell> roof,
            SudokuNumber[] commonCandidates,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var roofA = roof.get(0);
        var roofB = roof.get(1);
        var indexA = getUnitIndex.applyAsInt(roofA);
        var indexB = getUnitIndex.applyAsInt(roofB);
        if (indexA == indexB) {
            var unit = getUnit.apply(indexA)
                    .stream()
                    .filter(UnsolvedCell.class::isInstance)
                    .map(UnsolvedCell.class::cast).toList();
            var commonCandidateA = commonCandidates[0];
            var commonCandidateB = commonCandidates[1];
            return Stream.concat(
                    searchUnit(roof, unit, commonCandidateA, commonCandidateB),
                    searchUnit(roof, unit, commonCandidateB, commonCandidateA)
            );
        } else {
            return Stream.empty();
        }
    }

    private static Stream<LocatedCandidate> searchUnit(
            List<UnsolvedCell> roof,
            List<UnsolvedCell> unit,
            SudokuNumber search,
            SudokuNumber removal
    ) {
        if (unit.stream().filter(cell -> cell.candidates().contains(search)).count() == 2) {
            return roof.stream().map(roofCell -> new LocatedCandidate(roofCell, removal));
        } else {
            return Stream.empty();
        }
    }

    /*
     * Type 5
     *
     * If a rectangle has two floor cells in diagonally opposite corners of the rectangle and one of the common
     * candidates only appears in the rectangle for the rows and columns that the rectangle exists in, thus forming
     * strong links for the candidate along the four edges of the rectangle, then this is a potential Deadly Pattern. If
     * the non-strong link candidate were to be set as the solution to one of the floor cells, then the strong link
     * candidate would have to be the solution for the roof cells and the non-strong link candidate would need to be set
     * as the solution to the other floor cell, leading to the Deadly Pattern. The non-strong link candidate cannot be
     * the solution to either floor cell. Since each floor cell only contains two candidates, this means that the strong
     * link candidate must be the solution for the floor cells.
     */
    public static List<SetValue> uniqueRectanglesType5(Board<Cell> board) {
        return Rectangle.createRectangles(board)
                .stream()
                .flatMap(rectangle -> Optional.of(rectangle.getFloor())
                        .filter(floor -> floor.size() == 2)
                        .map(floor -> {
                            var floorA = floor.get(0);
                            var floorB = floor.get(1);
                            if (floorA.row() != floorB.row() && floorA.column() != floorB.column()) {
                                return rectangle.getCommonCandidates()
                                        .stream()
                                        .filter(candidate -> floor.stream().allMatch(floorCell -> {
                                            var row = board.getRow(floorCell.row());
                                            var column = board.getColumn(floorCell.column());
                                            return hasStrongLink(candidate, row) && hasStrongLink(candidate, column);
                                        }))
                                        .findFirst()
                                        .stream()
                                        .flatMap(strongLinkCandidate -> floor.stream()
                                                .map(floorCell -> new SetValue(floorCell, strongLinkCandidate)));
                            } else {
                                return Stream.<SetValue>empty();
                            }
                        })
                        .orElseGet(Stream::empty))
                .toList();
    }

    private static boolean hasStrongLink(SudokuNumber candidate, List<Cell> unit) {
        return unit.stream()
                .filter(UnsolvedCell.class::isInstance)
                .map(UnsolvedCell.class::cast)
                .filter(cell -> cell.candidates().contains(candidate))
                .count() == 2;
    }
}
