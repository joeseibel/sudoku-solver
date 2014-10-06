package sudokusolver.solutions;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class IntersectionRemoval {
	public static boolean intersectionRemoval(Puzzle puzzle) {
		boolean changeMade = false;
		for (SudokuNumber possibleNumber : SudokuNumber.values()) {
			for (Iterable<Cell> row : puzzle.getAllRows()) {
				Cell firstCellWithPossibleNumber = getFirstCellWithPossibleNumberIfAllPossibleIsolatedToSameBlock(possibleNumber, row);
				if (firstCellWithPossibleNumber != null &&
						removePossibleNumberFromBlockExceptRow(puzzle, possibleNumber, firstCellWithPossibleNumber)) {
					changeMade = true;
				}
			}
			for (Iterable<Cell> column : puzzle.getAllColumns()) {
				Cell firstCellWithPossibleNumber = getFirstCellWithPossibleNumberIfAllPossibleIsolatedToSameBlock(possibleNumber, column);
				if (firstCellWithPossibleNumber != null &&
						removePossibleNumberFromBlockExceptColumn(puzzle, possibleNumber, firstCellWithPossibleNumber)) {
					changeMade = true;
				}
			}
			for (Iterable<Cell> block : puzzle.getAllBlocks()) {
				Cell firstCellWithPossibleNumber = null;
				boolean possibleNumberIsolatedToSameRow = true;
				boolean possibleNumberIsolatedToSameColumn = true;
				for (Cell cell : block) {
					if (cell.getPossibleValues().contains(possibleNumber)) {
						if (firstCellWithPossibleNumber == null) {
							firstCellWithPossibleNumber = cell;
						} else {
							if (!firstCellWithPossibleNumber.isInSameRow(cell)) {
								possibleNumberIsolatedToSameRow = false;
							}
							if (!firstCellWithPossibleNumber.isInSameColumn(cell)) {
								possibleNumberIsolatedToSameColumn = false;
							}
						}
					}
				}
				if (firstCellWithPossibleNumber != null) {
					if (possibleNumberIsolatedToSameRow &&
							removePossibleNumberFromRowExceptBlock(puzzle, possibleNumber, firstCellWithPossibleNumber)) {
						changeMade = true;
					}
					if (possibleNumberIsolatedToSameColumn &&
							removePossibleNumberFromColumnExceptBlock(puzzle, possibleNumber, firstCellWithPossibleNumber)) {
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
	
	private static Cell getFirstCellWithPossibleNumberIfAllPossibleIsolatedToSameBlock(SudokuNumber possibleNumber, Iterable<Cell> unit) {
		Cell firstCellWithPossibleNumber = null;
		for (Cell cell : unit) {
			if (cell.getPossibleValues().contains(possibleNumber)) {
				if (firstCellWithPossibleNumber == null) {
					firstCellWithPossibleNumber = cell;
				} else if (!firstCellWithPossibleNumber.isInSameBlock(cell)) {
					return null;
				}
			}
		}
		return firstCellWithPossibleNumber;
	}
	
	private static boolean removePossibleNumberFromBlockExceptRow(Puzzle puzzle, SudokuNumber possibleNumber, Cell indexCell) {
		boolean changeMade = false;
		for (Cell cell : puzzle.getBlockCells(indexCell)) {
			if (!cell.isInSameRow(indexCell) && puzzle.removePossibleValue(cell, possibleNumber)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean removePossibleNumberFromBlockExceptColumn(Puzzle puzzle, SudokuNumber possibleNumber, Cell indexCell) {
		boolean changeMade = false;
		for (Cell cell : puzzle.getBlockCells(indexCell)) {
			if (!cell.isInSameColumn(indexCell) && puzzle.removePossibleValue(cell, possibleNumber)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean removePossibleNumberFromRowExceptBlock(Puzzle puzzle, SudokuNumber possibleNumber, Cell indexCell) {
		boolean changeMade = false;
		for (Cell cell : puzzle.getRowCells(indexCell)) {
			if (!cell.isInSameBlock(indexCell) && puzzle.removePossibleValue(cell, possibleNumber)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean removePossibleNumberFromColumnExceptBlock(Puzzle puzzle, SudokuNumber possibleNumber, Cell indexCell) {
		boolean changeMade = false;
		for (Cell cell : puzzle.getColumnCells(indexCell)) {
			if (!cell.isInSameBlock(indexCell) && puzzle.removePossibleValue(cell, possibleNumber)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
}