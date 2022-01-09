package sudokusolver.java;

import sudokusolver.java.logic.BruteForce;
import sudokusolver.java.logic.MultipleSolutionsException;
import sudokusolver.java.logic.NoSolutionsException;

public class SudokuSolver {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: java sudokusolver.java.SudokuSolver board");
        } else {
            var board = args[0];
            var showError = board.length() != Board.UNIT_SIZE_SQUARED;
            for (int i = 0; !showError && i < board.length(); i++) {
                if (board.charAt(i) < '0' || board.charAt(i) > '9') {
                    showError = true;
                    break;
                }
            }
            if (showError) {
                System.out.println("board must be " + Board.UNIT_SIZE_SQUARED + " numbers with blanks expressed as 0");
            } else {
                //TODO Switch from brute force to logical solutions.
                var parsed = BoardFactory.parseOptionalBoard(board);
                try {
                    var solution = BruteForce.bruteForce(parsed);
                    System.out.println("Brute force solution:");
                    System.out.println(solution);
                } catch (NoSolutionsException e) {
                    System.out.println("No Solutions");
                } catch (MultipleSolutionsException e) {
                    System.out.println("Multiple Solutions");
                }
            }
        }
    }
}
