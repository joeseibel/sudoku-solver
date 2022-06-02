package sudokusolver.javanostreams.logic;

import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.SudokuNumber;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class BruteForce {
    /*
     * Recursively tries every number for each unsolved cell looking for a solution.
     *
     * Motivation for implementing a brute force solution:
     *
     * The purpose of this solver is to go through the exercise of implementing various logical solutions. Why implement
     * brute force if I only care about logical solutions? The first reason is to check the correctness of the logical
     * solutions. When solving a board, the first thing that is done is to get the brute force solution. After that, any
     * logical modifications will be checked against the brute force solution. If a logical solution tries to set an
     * incorrect value to a cell or remove a candidate from a cell which is the known solution, then an
     * IllegalStateException is thrown.
     *
     * The second reason for implementing brute force is to check for the number of solutions for a board before trying
     * the logical solutions. If a board cannot be solved or if it has multiple solutions, then I don't bother with the
     * logical solutions. The logical solutions are written assuming that they are operating on a board with only one
     * solution.
     *
     * Motivation for throwing exceptions:
     *
     * In the Kotlin version, bruteForce returns a sealed interface with subtypes for the single solution case, the no
     * solution case, and the multiple solution case. This made sense in Kotlin in order to take advantage of sealed
     * interfaces and the when statement. I did not use exceptions for Kotlin because Kotlin does not have checked
     * exceptions, and I wanted the callers of bruteForce to be required to handle these exceptional cases.
     *
     * For this Java version, I decided to use checked exceptions primarily because this approach is much more common in
     * Java. Also, at the time of this writing, the current version of Java is 17. Java now has sealed types, but these
     * are only really useful when paired with pattern matching in switch expressions. Pattern matching is a preview
     * feature in 17, but I decided to not use preview features when writing the Java version of the solver. Even after
     * pattern matching becomes a full feature, I will probably keep the exceptions because throwing checked exceptions
     * is very common when writing Java programs and Java libraries.
     */
    public static Board<SudokuNumber> bruteForce(
            Board<Optional<SudokuNumber>> board
    ) throws NoSolutionsException, MultipleSolutionsException {
        if (board.getCells().stream().noneMatch(Optional::isEmpty)) {
            var filledBoard = board.mapCells(Optional::get);
            if (isSolved(filledBoard)) {
                return filledBoard;
            } else {
                throw new NoSolutionsException();
            }
        }

        var trialAndError = new Board<>(board.rows());
        return bruteForce(trialAndError, 0, 0);
    }

    private static Board<SudokuNumber> bruteForce(
            Board<Optional<SudokuNumber>> trialAndError,
            int rowIndex,
            int columnIndex
    ) throws NoSolutionsException, MultipleSolutionsException {
        if (rowIndex >= Board.UNIT_SIZE) {
            return trialAndError.mapCells(Optional::get);
        } else if (trialAndError.get(rowIndex, columnIndex).isPresent()) {
            return moveToNextCell(trialAndError, rowIndex, columnIndex);
        } else {
            var valid = EnumSet.allOf(SudokuNumber.class);
            trialAndError.getRow(rowIndex).stream().flatMap(Optional::stream).forEach(valid::remove);
            trialAndError.getColumn(columnIndex).stream().flatMap(Optional::stream).forEach(valid::remove);
            trialAndError.getBlock(Board.getBlockIndex(rowIndex, columnIndex))
                    .stream()
                    .flatMap(Optional::stream)
                    .forEach(valid::remove);
            var singleSolution = Optional.<Board<SudokuNumber>>empty();
            for (var guess : valid) {
                trialAndError.set(rowIndex, columnIndex, Optional.of(guess));
                try {
                    var intermediateSolution = moveToNextCell(trialAndError, rowIndex, columnIndex);
                    if (singleSolution.isEmpty()) {
                        singleSolution = Optional.of(intermediateSolution);
                    } else {
                        throw new MultipleSolutionsException();
                    }
                } catch (NoSolutionsException ignored) {
                }
            }
            trialAndError.set(rowIndex, columnIndex, Optional.empty());
            return singleSolution.orElseThrow(NoSolutionsException::new);
        }
    }

    private static Board<SudokuNumber> moveToNextCell(
            Board<Optional<SudokuNumber>> trialAndError,
            int rowIndex,
            int columnIndex
    ) throws NoSolutionsException, MultipleSolutionsException {
        if (columnIndex + 1 >= Board.UNIT_SIZE) {
            return bruteForce(trialAndError, rowIndex + 1, 0);
        } else {
            return bruteForce(trialAndError, rowIndex, columnIndex + 1);
        }
    }

    private static boolean isSolved(Board<SudokuNumber> board) {
        return board.rows().stream().allMatch(row -> Set.copyOf(row).size() == Board.UNIT_SIZE)
                && board.getColumns().stream().allMatch(column -> Set.copyOf(column).size() == Board.UNIT_SIZE)
                && board.getBlocks().stream().allMatch(block -> Set.copyOf(block).size() == Board.UNIT_SIZE);
    }
}
