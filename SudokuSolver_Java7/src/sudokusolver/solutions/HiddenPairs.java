package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.EnumSet;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class HiddenPairs {
	public static boolean hiddenPairs(Puzzle puzzle) {
		boolean changeMade = false;
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			if (hiddenPairsForUnit(puzzle, row)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			if (hiddenPairsForUnit(puzzle, column)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			if (hiddenPairsForUnit(puzzle, block)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean hiddenPairsForUnit(Puzzle puzzle, Iterable<Cell> unit) {
		boolean changeMade = false;
		for (int i = 0; i < SudokuNumber.values().length - 1; i++) {
			SudokuNumber possibleNumberOne = SudokuNumber.values()[i];
			for (int j = i + 1; j < SudokuNumber.values().length; j++) {
				SudokuNumber possibleNumberTwo = SudokuNumber.values()[j];
				ArrayList<Cell> cellsWithEitherNumber = new ArrayList<>();
				for (Cell cell : unit) {
					if (cell.getPossibleValues().contains(possibleNumberOne) || cell.getPossibleValues().contains(possibleNumberTwo)) {
						cellsWithEitherNumber.add(cell);
					}
				}
				if (cellsWithEitherNumber.size() == 2 &&
						cellsWithEitherNumber.get(0).getPossibleValues().contains(possibleNumberOne) &&
						cellsWithEitherNumber.get(0).getPossibleValues().contains(possibleNumberTwo) &&
						cellsWithEitherNumber.get(1).getPossibleValues().contains(possibleNumberOne) &&
						cellsWithEitherNumber.get(1).getPossibleValues().contains(possibleNumberTwo)) {
					EnumSet<SudokuNumber> numbersToRemove = EnumSet.allOf(SudokuNumber.class);
					numbersToRemove.remove(possibleNumberOne);
					numbersToRemove.remove(possibleNumberTwo);
					if (puzzle.removePossibleValues(cellsWithEitherNumber.get(0), numbersToRemove)) {
						changeMade = true;
					}
					if (puzzle.removePossibleValues(cellsWithEitherNumber.get(1), numbersToRemove)) {
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
}