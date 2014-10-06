package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class YWing {
	public static boolean yWing(Puzzle puzzle) {
		ArrayList<Cell> cellsWithTwoPossibleValues = new ArrayList<Cell>();
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 2) {
				cellsWithTwoPossibleValues.add(cell);
			}
		}
		for (Cell pivotCell : cellsWithTwoPossibleValues) {
			Iterator<SudokuNumber> iter = pivotCell.getPossibleValues().iterator();
			SudokuNumber possibleA = iter.next();
			SudokuNumber possibleB = iter.next();
			ArrayList<Cell> wingACandidates = new ArrayList<Cell>();
			ArrayList<Cell> wingBCandidates = new ArrayList<Cell>();
			for (Cell wingCandidate : cellsWithTwoPossibleValues) {
				if (!wingCandidate.equals(pivotCell) && wingCandidate.isInSameUnit(pivotCell)) {
					if (wingCandidate.getPossibleValues().contains(possibleA) && !wingCandidate.getPossibleValues().contains(possibleB)) {
						wingACandidates.add(wingCandidate);
					} else if (wingCandidate.getPossibleValues().contains(possibleB) &&
							!wingCandidate.getPossibleValues().contains(possibleA)) {
						wingBCandidates.add(wingCandidate);
					}
				}
			}
			if (!wingACandidates.isEmpty() && !wingBCandidates.isEmpty()) {
				for (Cell wingA : wingACandidates) {
					for (Cell wingB : wingBCandidates) {
						EnumSet<SudokuNumber> possibleNumberIntersection = EnumSet.copyOf(wingA.getPossibleValues());
						possibleNumberIntersection.retainAll(wingB.getPossibleValues());
						if (possibleNumberIntersection.size() == 1) {
							boolean changeMade = false;
							for (Cell emptyCell : puzzle.getAllEmptyCells()) {
								if (!emptyCell.equals(pivotCell) && !emptyCell.equals(wingA) && !emptyCell.equals(wingB) &&
										emptyCell.isInSameUnit(wingA) && emptyCell.isInSameUnit(wingB) &&
										puzzle.removePossibleValues(emptyCell, possibleNumberIntersection)) {
									changeMade = true;
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
		return false;
	}
}