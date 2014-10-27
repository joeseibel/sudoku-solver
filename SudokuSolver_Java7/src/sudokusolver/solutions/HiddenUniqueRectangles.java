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
			if (type1(puzzle, rectangle) || type2(puzzle, rectangle)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean type1(Puzzle puzzle, ArrayList<Cell> rectangle) {
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
			Cell oppositeCell = getOppositeCell(rectangle, biValueCell);
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
					return true;
				} else if (twoStrongLinksForNumberExists(puzzle, rectangle, oppositeCell, possibleNumberTwo)) {
					puzzle.removePossibleValue(oppositeCell, possibleNumberOne);
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean type2(Puzzle puzzle, ArrayList<Cell> rectangle) {
		ArrayList<Cell> floor = new ArrayList<>();
		for (Cell cell : rectangle) {
			if (cell.getPossibleValues().size() == 2) {
				floor.add(cell);
			}
		}
		if (floor.size() == 2 && floor.get(0).getPossibleValues().equals(floor.get(1).getPossibleValues())) {
			Iterator<SudokuNumber> possibleNumberIter = floor.get(0).getPossibleValues().iterator();
			SudokuNumber possibleNumberOne = possibleNumberIter.next();
			SudokuNumber possibleNumberTwo = possibleNumberIter.next();
			if (getOppositeCell(rectangle, floor.get(0)).getPossibleValues().contains(possibleNumberOne) &&
					getOppositeCell(rectangle, floor.get(0)).getPossibleValues().contains(possibleNumberTwo) &&
					getOppositeCell(rectangle, floor.get(1)).getPossibleValues().contains(possibleNumberOne) &&
					getOppositeCell(rectangle, floor.get(1)).getPossibleValues().contains(possibleNumberTwo)) {
				if (floor.get(0).getRow() == floor.get(1).getRow()) {
					if (strongLinkForNumberExists(puzzle.getColumnCells(floor.get(0)), rectangle, possibleNumberOne)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(0)), possibleNumberTwo);
						return true;
					} else if (strongLinkForNumberExists(puzzle.getColumnCells(floor.get(0)), rectangle, possibleNumberTwo)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(0)), possibleNumberOne);
						return true;
					} else if (strongLinkForNumberExists(puzzle.getColumnCells(floor.get(1)), rectangle, possibleNumberOne)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(1)), possibleNumberTwo);
						return true;
					} else if (strongLinkForNumberExists(puzzle.getColumnCells(floor.get(1)), rectangle, possibleNumberTwo)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(1)), possibleNumberOne);
						return true;
					}
				} else if (floor.get(0).getColumn() == floor.get(1).getColumn()) {
					if (strongLinkForNumberExists(puzzle.getRowCells(floor.get(0)), rectangle, possibleNumberOne)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(0)), possibleNumberTwo);
						return true;
					} else if (strongLinkForNumberExists(puzzle.getRowCells(floor.get(0)), rectangle, possibleNumberTwo)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(0)), possibleNumberOne);
						return true;
					} else if (strongLinkForNumberExists(puzzle.getRowCells(floor.get(1)), rectangle, possibleNumberOne)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(1)), possibleNumberTwo);
						return true;
					} else if (strongLinkForNumberExists(puzzle.getRowCells(floor.get(1)), rectangle, possibleNumberTwo)) {
						puzzle.removePossibleValue(getOppositeCell(rectangle, floor.get(1)), possibleNumberOne);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static Cell getOppositeCell(ArrayList<Cell> rectangle, Cell cell) {
		return rectangle.get(rectangle.size() - rectangle.indexOf(cell) - 1);
	}
	
	private static boolean twoStrongLinksForNumberExists(Puzzle puzzle, ArrayList<Cell> rectangle, Cell oppositeCell, SudokuNumber possibleNumber) {
		return strongLinkForNumberExists(puzzle.getRowCells(oppositeCell), rectangle, possibleNumber) &&
				strongLinkForNumberExists(puzzle.getColumnCells(oppositeCell), rectangle, possibleNumber);
	}
	
	private static boolean strongLinkForNumberExists(Iterable<Cell> linearUnit, ArrayList<Cell> rectangle, SudokuNumber possibleNumber) {
		for (Cell cell : linearUnit) {
			if (!rectangle.contains(cell) && cell.getPossibleValues().contains(possibleNumber)) {
				return false;
			}
		}
		return true;
	}
}