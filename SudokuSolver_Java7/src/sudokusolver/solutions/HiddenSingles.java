package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class HiddenSingles {
	public static boolean hiddenSingles(Puzzle puzzle) {
		boolean changeMade = false;
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			if (hiddenSingles(puzzle, row)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			if (hiddenSingles(puzzle, column)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			if (hiddenSingles(puzzle, block)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean hiddenSingles(Puzzle puzzle, Iterable<Cell> unit) {
		HashMap<SudokuNumber, ArrayList<Cell>> potentialCells = new HashMap<>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleValue : cell.getPossibleValues()) {
				Common.addToValueList(potentialCells, possibleValue, cell);
			}
		}
		boolean changeMade = false;
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : potentialCells.entrySet()) {
			if (entry.getValue().size() == 1) {
				puzzle.setValueAndUpdatePossibleValues(entry.getValue().get(0), entry.getKey());
				changeMade = true;
			}
		}
		return changeMade;
	}
}