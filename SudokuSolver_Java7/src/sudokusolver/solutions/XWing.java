package sudokusolver.solutions;

import java.util.ArrayList;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class XWing {
	public static boolean xWing(Puzzle puzzle) {
		boolean changeMade = false;
		for (SudokuNumber possibleNumber : SudokuNumber.values()) {
			if (xWingByRowPairs(puzzle, possibleNumber)) {
				changeMade = true;
			}
			if (xWingByColumnPairs(puzzle, possibleNumber)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean xWingByRowPairs(Puzzle puzzle, SudokuNumber possibleNumber) {
		ArrayList<ArrayList<Cell>> rowPairs = getUnitPairs(possibleNumber, puzzle.getAllRows());
		for (int i = 0; i < rowPairs.size() - 1; i++) {
			for (int j = i + 1; j < rowPairs.size(); j++) {
				Cell topLeft = rowPairs.get(i).get(0);
				Cell topRight = rowPairs.get(i).get(1);
				Cell bottomLeft = rowPairs.get(j).get(0);
				Cell bottomRight = rowPairs.get(j).get(1);
				if (topLeft.isInSameColumn(bottomLeft) && topRight.isInSameColumn(bottomRight)) {
					boolean changeMade = false;
					for (Cell cell : puzzle.getColumnCells(topLeft)) {
						if (!cell.equals(topLeft) && !cell.equals(bottomLeft) && puzzle.removePossibleValue(cell, possibleNumber)) {
							changeMade = true;
						}
					}
					for (Cell cell : puzzle.getColumnCells(topRight)) {
						if (!cell.equals(topRight) && !cell.equals(bottomRight) && puzzle.removePossibleValue(cell, possibleNumber)) {
							changeMade = true;
						}
					}
					if (changeMade) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static boolean xWingByColumnPairs(Puzzle puzzle, SudokuNumber possibleNumber) {
		ArrayList<ArrayList<Cell>> columnPairs = getUnitPairs(possibleNumber, puzzle.getAllColumns());
		for (int i = 0; i < columnPairs.size() - 1; i++) {
			for (int j = i + 1; j < columnPairs.size(); j++) {
				Cell topLeft = columnPairs.get(i).get(0);
				Cell topRight = columnPairs.get(j).get(0);
				Cell bottomLeft = columnPairs.get(i).get(1);
				Cell bottomRight = columnPairs.get(j).get(1);
				if (topLeft.isInSameRow(topRight) && bottomLeft.isInSameRow(bottomRight)) {
					boolean changeMade = false;
					for (Cell cell : puzzle.getRowCells(topLeft)) {
						if (!cell.equals(topLeft) && !cell.equals(topRight) && puzzle.removePossibleValue(cell, possibleNumber)) {
							changeMade = true;
						}
					}
					for (Cell cell : puzzle.getRowCells(bottomLeft)) {
						if (!cell.equals(bottomLeft) && !cell.equals(bottomRight) && puzzle.removePossibleValue(cell, possibleNumber)) {
							changeMade = true;
						}
					}
					if (changeMade) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static ArrayList<ArrayList<Cell>> getUnitPairs(SudokuNumber possibleNumber, Iterable<Iterable<Cell>> units) {
		ArrayList<ArrayList<Cell>> unitPairs = new ArrayList<>();
		for (Iterable<Cell> unit : units) {
			ArrayList<Cell> pairForCurrentUnit = new ArrayList<>();
			for (Cell cell : unit) {
				if (cell.getPossibleValues().contains(possibleNumber)) {
					pairForCurrentUnit.add(cell);
				}
			}
			if (pairForCurrentUnit.size() == 2) {
				unitPairs.add(pairForCurrentUnit);
			}
		}
		return unitPairs;
	}
}