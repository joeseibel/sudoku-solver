package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class XYChain {
	public static boolean xyChain(Puzzle puzzle) {
		ArrayList<Cell> cellsWithTwoPossibleValues = new ArrayList<Cell>();
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 2) {
				cellsWithTwoPossibleValues.add(cell);
			}
		}
		for (Cell startingCell : cellsWithTwoPossibleValues) {
			for (SudokuNumber sharedNumber : startingCell.getPossibleValues()) {
				SudokuNumber nextLinkNumber;
				Iterator<SudokuNumber> iter = startingCell.getPossibleValues().iterator();
				nextLinkNumber = iter.next();
				if (nextLinkNumber.equals(sharedNumber)) {
					nextLinkNumber = iter.next();
				}
				if (buildXyChainFromCell(puzzle, cellsWithTwoPossibleValues, startingCell, sharedNumber, startingCell, new HashSet<Cell>(),
						nextLinkNumber)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean buildXyChainFromCell(Puzzle puzzle, ArrayList<Cell> cellsWithTwoPossibleValues, Cell startingCell,
			SudokuNumber sharedNumber, Cell currentCell, HashSet<Cell> cellsInChain, SudokuNumber nextLinkNumber) {
		cellsInChain.add(currentCell);
		for (Cell nextCell : cellsWithTwoPossibleValues) {
			if (!cellsInChain.contains(nextCell) && nextCell.getPossibleValues().contains(nextLinkNumber) &&
					currentCell.isInSameUnit(nextCell)) {
				SudokuNumber otherPossibleValueOfNextCell;
				Iterator<SudokuNumber> iter = nextCell.getPossibleValues().iterator();
				otherPossibleValueOfNextCell = iter.next();
				if (otherPossibleValueOfNextCell.equals(nextLinkNumber)) {
					otherPossibleValueOfNextCell = iter.next();
				}
				if (otherPossibleValueOfNextCell.equals(sharedNumber) && !nextCell.isInSameUnit(startingCell) &&
						removePossibleNumberVisibleToEndPoints(puzzle, sharedNumber, startingCell, nextCell)) {
					return true;
				} else if (buildXyChainFromCell(puzzle, cellsWithTwoPossibleValues, startingCell, sharedNumber, nextCell, cellsInChain,
						otherPossibleValueOfNextCell)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean removePossibleNumberVisibleToEndPoints(Puzzle puzzle, SudokuNumber sharedNumber, Cell startingCell,
			Cell endingCell) {
		boolean changeMade = false;
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.isInSameUnit(startingCell) && cell.isInSameUnit(endingCell) && !cell.equals(startingCell) && !cell.equals(endingCell) &&
					puzzle.removePossibleValue(cell, sharedNumber)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
}