package sudokusolver.javanostreams.logic.diabolical;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.Rectangle;
import sudokusolver.javanostreams.Removals;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

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
        var removals = new Removals();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var roof = rectangle.getRoof();
            if (roof.size() == 1) {
                var roofCell = roof.getFirst();
                removals.add(roofCell, rectangle.getCommonCandidates());
            }
        }
        return removals.toList();
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
        var removals = new Removals();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var roof = rectangle.getRoof();
            if (roof.size() == 2) {
                var roofA = roof.getFirst();
                var roofB = roof.getLast();
                if (roofA.candidates().size() == 3 && roofA.candidates().equals(roofB.candidates())) {
                    var additionalCandidates = EnumSet.copyOf(roofA.candidates());
                    additionalCandidates.removeAll(rectangle.getCommonCandidates());
                    assert additionalCandidates.size() == 1;
                    var additionalCandidate = additionalCandidates.iterator().next();
                    for (var cell : board.getCells()) {
                        if (cell instanceof UnsolvedCell unsolved &&
                                unsolved.candidates().contains(additionalCandidate) &&
                                !unsolved.equals(roofA) &&
                                !unsolved.equals(roofB) &&
                                unsolved.isInSameUnit(roofA) &&
                                unsolved.isInSameUnit(roofB)
                        ) {
                            removals.add(unsolved, additionalCandidate);
                        }
                    }
                }
            }
        }
        return removals.toList();
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
        var removals = new Removals();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var roof = rectangle.getRoof();
            if (roof.size() == 2) {
                var roofA = roof.getFirst();
                var roofB = roof.getLast();
                if (roofA.candidates().size() == 3 && roofB.candidates().size() == 3 &&
                        !roofA.candidates().equals(roofB.candidates())
                ) {
                    var additionalCandidates = EnumSet.copyOf(roofA.candidates());
                    additionalCandidates.addAll(roofB.candidates());
                    additionalCandidates.removeAll(rectangle.getCommonCandidates());
                    getRemovalsType3(removals, roofA, roofB, additionalCandidates, Cell::row, board::getRow);
                    getRemovalsType3(removals, roofA, roofB, additionalCandidates, Cell::column, board::getColumn);
                    getRemovalsType3(removals, roofA, roofB, additionalCandidates, Cell::block, board::getBlock);
                }
            }
        }
        return removals.toList();
    }

    private static void getRemovalsType3(
            Removals removals,
            UnsolvedCell roofA,
            UnsolvedCell roofB,
            EnumSet<SudokuNumber> additionalCandidates,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var indexA = getUnitIndex.applyAsInt(roofA);
        var indexB = getUnitIndex.applyAsInt(roofB);
        if (indexA == indexB) {
            var unit = new ArrayList<UnsolvedCell>();
            for (var cell : getUnit.apply(indexA)) {
                if (cell instanceof UnsolvedCell unsolved) {
                    unit.add(unsolved);
                }
            }
            for (var pairCell : unit) {
                if (pairCell.candidates().equals(additionalCandidates)) {
                    for (var cell : unit) {
                        if (!cell.equals(pairCell) && !cell.equals(roofA) && !cell.equals(roofB)) {
                            var removeCandidates = EnumSet.copyOf(cell.candidates());
                            removeCandidates.retainAll(additionalCandidates);
                            removals.add(cell, removeCandidates);
                        }
                    }
                    break;
                }
            }
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
        var removals = new Removals();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var roof = rectangle.getRoof();
            if (roof.size() == 2) {
                var roofA = roof.getFirst();
                var roofB = roof.getLast();
                var additionalCandidates = EnumSet.copyOf(roofA.candidates());
                additionalCandidates.addAll(roofB.candidates());
                additionalCandidates.removeAll(rectangle.getCommonCandidates());
                getRemovalsType3B(removals, roofA, roofB, additionalCandidates, Cell::row, board::getRow);
                getRemovalsType3B(removals, roofA, roofB, additionalCandidates, Cell::column, board::getColumn);
                getRemovalsType3B(removals, roofA, roofB, additionalCandidates, Cell::block, board::getBlock);
            }
        }
        return removals.toList();
    }

    private static void getRemovalsType3B(
            Removals removals,
            UnsolvedCell roofA,
            UnsolvedCell roofB,
            EnumSet<SudokuNumber> additionalCandidates,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var indexA = getUnitIndex.applyAsInt(roofA);
        var indexB = getUnitIndex.applyAsInt(roofB);
        if (indexA == indexB) {
            var unit = new ArrayList<UnsolvedCell>();
            for (var cell : getUnit.apply(indexA)) {
                if (cell instanceof UnsolvedCell unsolved && !unsolved.equals(roofA) && !unsolved.equals(roofB)) {
                    unit.add(unsolved);
                }
            }
            for (var i = 0; i < unit.size() - 1; i++) {
                var tripleA = unit.get(i);
                for (var j = i + 1; j < unit.size(); j++) {
                    var tripleB = unit.get(j);
                    var tripleCandidates = EnumSet.copyOf(additionalCandidates);
                    tripleCandidates.addAll(tripleA.candidates());
                    tripleCandidates.addAll(tripleB.candidates());
                    if (tripleCandidates.size() == 3) {
                        for (var cell : unit) {
                            if (!cell.equals(tripleA) && !cell.equals(tripleB)) {
                                var removeCandidates = EnumSet.copyOf(cell.candidates());
                                removeCandidates.retainAll(tripleCandidates);
                                removals.add(cell, removeCandidates);
                            }
                        }
                    }
                }
            }
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
        var removals = new Removals();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var roof = rectangle.getRoof();
            if (roof.size() == 2) {
                var commonCandidates = rectangle.getCommonCandidates().toArray(SudokuNumber[]::new);
                getRemovalsType4(removals, roof, commonCandidates, Cell::row, board::getRow);
                getRemovalsType4(removals, roof, commonCandidates, Cell::column, board::getColumn);
                getRemovalsType4(removals, roof, commonCandidates, Cell::block, board::getBlock);
            }
        }
        return removals.toList();
    }

    private static void getRemovalsType4(
            Removals removals,
            List<UnsolvedCell> roof,
            SudokuNumber[] commonCandidates,
            ToIntFunction<Cell> getUnitIndex,
            IntFunction<List<Cell>> getUnit
    ) {
        var roofA = roof.getFirst();
        var roofB = roof.getLast();
        var indexA = getUnitIndex.applyAsInt(roofA);
        var indexB = getUnitIndex.applyAsInt(roofB);
        if (indexA == indexB) {
            var unit = new ArrayList<UnsolvedCell>();
            for (var cell : getUnit.apply(indexA)) {
                if (cell instanceof UnsolvedCell unsolved) {
                    unit.add(unsolved);
                }
            }
            var commonCandidatesA = commonCandidates[0];
            var commonCandidatesB = commonCandidates[1];
            searchUnit(removals, roof, unit, commonCandidatesA, commonCandidatesB);
            searchUnit(removals, roof, unit, commonCandidatesB, commonCandidatesA);
        }
    }

    private static void searchUnit(
            Removals removals,
            List<UnsolvedCell> roof,
            List<UnsolvedCell> unit,
            SudokuNumber search,
            SudokuNumber removal
    ) {
        var withSearchCount = 0;
        for (var cell : unit) {
            if (cell.candidates().contains(search)) {
                withSearchCount++;
            }
        }
        if (withSearchCount == 2) {
            for (var roofCell : roof) {
                removals.add(roofCell, removal);
            }
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
        var modifications = new ArrayList<SetValue>();
        for (var rectangle : Rectangle.createRectangles(board)) {
            var floor = rectangle.getFloor();
            if (floor.size() == 2) {
                var floorA = floor.getFirst();
                var floorB = floor.getLast();
                if (floorA.row() != floorB.row() && floorA.column() != floorB.column()) {
                    for (var candidate : rectangle.getCommonCandidates()) {
                        var isStrongLinkCandidate = true;
                        for (var floorCell : floor) {
                            var row = board.getRow(floorCell.row());
                            var column = board.getColumn(floorCell.column());
                            if (doesNotHaveStrongLink(candidate, row) || doesNotHaveStrongLink(candidate, column)) {
                                isStrongLinkCandidate = false;
                            }
                        }
                        if (isStrongLinkCandidate) {
                            for (var floorCell : floor) {
                                modifications.add(new SetValue(floorCell, candidate));
                            }
                        }
                    }
                }
            }
        }
        return modifications;
    }

    private static boolean doesNotHaveStrongLink(SudokuNumber candidate, List<Cell> unit) {
        var withCandidateCount = 0;
        for (var cell : unit) {
            if (cell instanceof UnsolvedCell(_, _, var candidates) && candidates.contains(candidate)) {
                withCandidateCount++;
            }
        }
        return withCandidateCount != 2;
    }
}