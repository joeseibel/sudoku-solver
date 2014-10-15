package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.Set;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class NakedPairs {
	public static boolean nakedPairs(Puzzle puzzle) {
		boolean changeMade = false;
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			ArrayList<Set<SudokuNumber>> nakedPairs = findNakedPairs(row);
			if (eliminatePossibleValuesBasedOnNakedPairs(puzzle, row, nakedPairs)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			ArrayList<Set<SudokuNumber>> nakedPairs = findNakedPairs(column);
			if (eliminatePossibleValuesBasedOnNakedPairs(puzzle, column, nakedPairs)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			ArrayList<Set<SudokuNumber>> nakedPairs = findNakedPairs(block);
			if (eliminatePossibleValuesBasedOnNakedPairs(puzzle, block, nakedPairs)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static ArrayList<Set<SudokuNumber>> findNakedPairs(Iterable<Cell> unit) {
		ArrayList<Set<SudokuNumber>> encounteredPairs = new ArrayList<>();
		ArrayList<Set<SudokuNumber>> nakedPairs = new ArrayList<>();
		for (Cell cell : unit) {
			if (cell.getPossibleValues().size() == 2) {
				if (encounteredPairs.contains(cell.getPossibleValues())) {
					nakedPairs.add(cell.getPossibleValues());
				} else {
					encounteredPairs.add(cell.getPossibleValues());
				}
			}
		}
		return nakedPairs;
	}
	
	private static boolean eliminatePossibleValuesBasedOnNakedPairs(Puzzle puzzle, Iterable<Cell> unit,
			ArrayList<Set<SudokuNumber>> nakedPairs) {
		boolean changeMade = false;
		for (Set<SudokuNumber> pair : nakedPairs) {
			for (Cell cell : unit) {
				if (!cell.getPossibleValues().equals(pair)) {
					if (puzzle.removePossibleValues(cell, pair)) {
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
}