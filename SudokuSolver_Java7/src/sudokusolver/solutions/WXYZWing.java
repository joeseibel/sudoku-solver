package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;
import sudokusolver.Util;

public class WXYZWing {
	public static boolean wxyzWing(Puzzle puzzle) {
		ArrayList<Cell> emptyCells = new ArrayList<>();
		for (Cell cell : puzzle.getAllEmptyCells()) {
			emptyCells.add(cell);
		}
		for (int i = 0; i < emptyCells.size() - 3; i++) {
			Cell firstCell = emptyCells.get(i);
			for (int j = i + 1; j < emptyCells.size() - 2; j++) {
				Cell secondCell = emptyCells.get(j);
				for (int k = j + 1; k < emptyCells.size() - 1; k++) {
					Cell thirdCell = emptyCells.get(k);
					for (int l = k + 1; l < emptyCells.size(); l++) {
						Cell fourthCell = emptyCells.get(l);
						HashMap<SudokuNumber, ArrayList<Cell>> cellsForPossibleNumbers = new HashMap<>();
						Cell[] fourCells = new Cell[]{firstCell, secondCell, thirdCell, fourthCell};
						for (Cell cell : fourCells) {
							for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
								Util.addToValueList(cellsForPossibleNumbers, possibleNumber, cell);
							}
						}
						if (cellsForPossibleNumbers.size() == 4) {
							EnumSet<SudokuNumber> nonRestrictedNumbers = EnumSet.noneOf(SudokuNumber.class);
							for (Entry<SudokuNumber, ArrayList<Cell>> entry : cellsForPossibleNumbers.entrySet()) {
								for (int m = 0; m < entry.getValue().size() - 1; m++) {
									Cell cellOne = entry.getValue().get(m);
									for (int n = m + 1; n < entry.getValue().size(); n++) {
										Cell cellTwo = entry.getValue().get(n);
										if (!cellOne.isInSameUnit(cellTwo)) {
											nonRestrictedNumbers.add(entry.getKey());
										}
									}
								}
							}
							if (nonRestrictedNumbers.size() == 1) {
								boolean changeMade = false;
								for (Cell cell : puzzle.getAllEmptyCells()) {
									if (!cell.equals(firstCell) && !cell.equals(secondCell) && !cell.equals(thirdCell) && !cell.equals(fourthCell)) {
										boolean canSeeAllNonRestrictedNumberCells = true;
										for (Cell nonRestrictedNumberCell : cellsForPossibleNumbers.get(nonRestrictedNumbers.iterator().next())) {
											if (!cell.isInSameUnit(nonRestrictedNumberCell)) {
												canSeeAllNonRestrictedNumberCells = false;
												break;
											}
										}
										if (canSeeAllNonRestrictedNumberCells && puzzle.removePossibleValue(cell, nonRestrictedNumbers.iterator().next())) {
											changeMade = true;
										}
									}
								}
								if (changeMade) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
}