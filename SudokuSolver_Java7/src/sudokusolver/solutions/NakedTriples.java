package sudokusolver.solutions;

import java.util.ArrayList;
import java.util.EnumSet;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuNumber;

public class NakedTriples {
	public static boolean nakedTriples(Puzzle puzzle) {
		boolean changeMade = false;
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			if (nakedTriplesForUnit(puzzle, row)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			if (nakedTriplesForUnit(puzzle, column)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			if (nakedTriplesForUnit(puzzle, block)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean nakedTriplesForUnit(Puzzle puzzle, Iterable<Cell> unit) {
		ArrayList<Cell> cellsWithTwoOrThreePossibleValues = new ArrayList<>();
		for (Cell cell : unit) {
			if (cell.getPossibleValues().size() == 2 || cell.getPossibleValues().size() == 3) {
				cellsWithTwoOrThreePossibleValues.add(cell);
			}
		}
		for (int i = 0; i < cellsWithTwoOrThreePossibleValues.size() - 2; i++) {
			for (int j = i + 1; j < cellsWithTwoOrThreePossibleValues.size() - 1; j++) {
				for (int l = j + 1; l < cellsWithTwoOrThreePossibleValues.size(); l++) {
					EnumSet<SudokuNumber> unionOfPossibleValues =
							EnumSet.copyOf(cellsWithTwoOrThreePossibleValues.get(i).getPossibleValues());
					unionOfPossibleValues.addAll(cellsWithTwoOrThreePossibleValues.get(j).getPossibleValues());
					unionOfPossibleValues.addAll(cellsWithTwoOrThreePossibleValues.get(l).getPossibleValues());
					if (unionOfPossibleValues.size() == 3) {
						boolean changeMade = false;
						for (Cell cell : unit) {
							if (!cell.equals(cellsWithTwoOrThreePossibleValues.get(i)) &&
									!cell.equals(cellsWithTwoOrThreePossibleValues.get(j)) &&
									!cell.equals(cellsWithTwoOrThreePossibleValues.get(l)) &&
									puzzle.removePossibleValues(cell, unionOfPossibleValues)) {
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
		return false;
	}
}