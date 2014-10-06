package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.Iterator;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class HiddenUniqueRectangles {
	public static boolean hiddenUniqueRectangles(Puzzle puzzle) {
		boolean changeMade = false;
		for (ArrayList<Cell> rectangle : puzzle.getRectangles()) {
			int numberOfCellsWithTwoPossibleValues = 0;
			for (Cell cell : rectangle) {
				if (cell.getPossibleValues().size() == 2) {
					numberOfCellsWithTwoPossibleValues++;
				}
			}
			if (numberOfCellsWithTwoPossibleValues == 1) {
				Cell biValueCell = null;
				for (Iterator<Cell> iter = rectangle.iterator(); biValueCell == null && iter.hasNext();) {
					Cell cell = iter.next();
					if (cell.getPossibleValues().size() == 2) {
						biValueCell = cell;
					}
				}
				Cell oppositeCell = rectangle.get(rectangle.size() - rectangle.indexOf(biValueCell) - 1);
				boolean possibleDeadlyPattern = true;
				for (Iterator<Cell> iter = rectangle.iterator(); possibleDeadlyPattern && iter.hasNext();) {
					Cell cell = iter.next();
					if (!cell.equals(biValueCell) && !cell.getPossibleValues().containsAll(biValueCell.getPossibleValues())) {
						possibleDeadlyPattern = false;
					}
				}
				if (possibleDeadlyPattern) {
					Iterator<SudokuNumber> possibleNumberIter = biValueCell.getPossibleValues().iterator();
					SudokuNumber possibleNumberOne = possibleNumberIter.next();
					SudokuNumber possibleNumberTwo = possibleNumberIter.next();
					if (twoStrongLinksForNumberExists(puzzle, rectangle, oppositeCell, possibleNumberOne)) {
						puzzle.removePossibleValue(oppositeCell, possibleNumberTwo);
						changeMade = true;
					} else if (twoStrongLinksForNumberExists(puzzle, rectangle, oppositeCell, possibleNumberTwo)) {
						puzzle.removePossibleValue(oppositeCell, possibleNumberOne);
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
	
	private static boolean twoStrongLinksForNumberExists(Puzzle puzzle, ArrayList<Cell> rectangleCells, Cell oppositeCell,
			SudokuNumber possibleNumber) {
		for (Cell cell : puzzle.getRowCells(oppositeCell)) {
			if (!rectangleCells.contains(cell) && cell.getPossibleValues().contains(possibleNumber)) {
				return false;
			}
		}
		for (Cell cell : puzzle.getColumnCells(oppositeCell)) {
			if (!rectangleCells.contains(cell) && cell.getPossibleValues().contains(possibleNumber)) {
				return false;
			}
		}
		return true;
	}
}