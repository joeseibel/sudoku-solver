package sudokusolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.jgrapht.graph.Pseudograph;

import sudokusolver.solutions.AlternatingInferenceChains;
import sudokusolver.solutions.HiddenSingles;
import sudokusolver.solutions.HiddenUniqueRectangles;
import sudokusolver.solutions.IntersectionRemoval;
import sudokusolver.solutions.Medusa;
import sudokusolver.solutions.NakedPairs;
import sudokusolver.solutions.NakedTriples;
import sudokusolver.solutions.SimpleColoring;
import sudokusolver.solutions.XCycles;
import sudokusolver.solutions.XWing;
import sudokusolver.solutions.XYChain;
import sudokusolver.solutions.XYZWing;
import sudokusolver.solutions.YWing;

public class SudokuSolver {
	public static void main(String[] args) throws FileNotFoundException {
		solveGnomeSudokuPuzzle("../Puzzles/easy");
		solveGnomeSudokuPuzzle("../Puzzles/medium");
		solveGnomeSudokuPuzzle("../Puzzles/hard");
		solveGnomeSudokuPuzzle("../Puzzles/very_hard");
	}
	
	private static void solveGnomeSudokuPuzzle(String filename) throws FileNotFoundException {
		File puzzleFile = new File(filename);
		Scanner scanner = new Scanner(puzzleFile);
		for (int puzzleIndex = 0; scanner.hasNextInt(); puzzleIndex++) {
			int[] initialValues = new int[Puzzle.UNIT_SIZE * Puzzle.UNIT_SIZE];
			for (int i = 0; i < initialValues.length; i++) {
				initialValues[i] = scanner.nextInt();
			}
			scanner.nextLine();
			Puzzle puzzle = new Puzzle(initialValues);
			if (solve(puzzle)) {
				puzzle.validateCompletePuzzle();
				System.out.println(puzzleFile.getName() + " #" + puzzleIndex + ": solved");
			} else {
				System.out.println(puzzleFile.getName() + " #" + puzzleIndex + ": not solved");
				System.out.println();
				System.out.println();
				System.out.println("Original puzzle:");
				System.out.println();
				for (int i = 0 ; i < initialValues.length; i++) {
					System.out.print(initialValues[i]);
					if ((i + 1) % Puzzle.UNIT_SIZE == 0) {
						System.out.println();
					} else {
						System.out.print(' ');
					}
				}
				System.out.println();
				System.out.println();
				System.out.println("Final state:");
				System.out.println();
				System.out.println(puzzle);
				System.out.println();
				System.out.println(puzzle.getSudokuWikiURL());
				System.exit(0);
			}
		}
		scanner.close();
	}
	
	public static boolean solve(Puzzle puzzle) {
		boolean changeMade;
		do {
			changeMade = false;
			int startingEmptyCellCount = puzzle.getEmptyCellCount();
			checkForSolvedCells(puzzle);
			if (startingEmptyCellCount != puzzle.getEmptyCellCount()) {
				changeMade = true;
			}
			if (puzzle.getEmptyCellCount() != 0) {
				if (HiddenSingles.hiddenSingles(puzzle) || NakedPairs.nakedPairs(puzzle) || NakedTriples.nakedTriples(puzzle) ||
						IntersectionRemoval.intersectionRemoval(puzzle) || XWing.xWing(puzzle)) {
					changeMade = true;
				} else {
					HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains = buildChains(puzzle);
					if (SimpleColoring.simpleColoring(puzzle, chains) || YWing.yWing(puzzle) || XYZWing.xyzWing(puzzle) || XCycles.xCycles(puzzle, chains) ||
							XYChain.xyChain(puzzle) || Medusa.medusa(puzzle) || HiddenUniqueRectangles.hiddenUniqueRectangles(puzzle) ||
							AlternatingInferenceChains.alternatingInferenceChains(puzzle)) {
						changeMade = true;
					}
					//TODO: Put solutions here.
				}
			}
		} while (changeMade && puzzle.getEmptyCellCount() != 0);
		return puzzle.getEmptyCellCount() == 0;
	}
	
	private static void checkForSolvedCells(Puzzle puzzle) {
		int emptyCellCountBeforeSolution;
		do {
			emptyCellCountBeforeSolution = puzzle.getEmptyCellCount();
			for (Cell cell : puzzle.getEmptyCellsWithOnePossibleValue()) {
				puzzle.setValueAndUpdatePossibleValues(cell, cell.getPossibleValues().iterator().next());
			}
		} while (emptyCellCountBeforeSolution != puzzle.getEmptyCellCount());
	}
	
	private static HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> buildChains(Puzzle puzzle) {
		HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains = new HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>>();
		for (SudokuNumber possibleNumber : SudokuNumber.values()) {
			Pseudograph<Cell, SudokuEdge> possibleGraph = new Pseudograph<Cell, SudokuEdge>(SudokuEdge.class);
			for (Iterable<Cell> row : puzzle.getAllRows()) {
				addConjugatePairToGraph(row, possibleNumber, possibleGraph);
			}
			for (Iterable<Cell> column : puzzle.getAllColumns()) {
				addConjugatePairToGraph(column, possibleNumber, possibleGraph);
			}
			for (Iterable<Cell> block : puzzle.getAllBlocks()) {
				addConjugatePairToGraph(block, possibleNumber, possibleGraph);
			}
			if (!possibleGraph.vertexSet().isEmpty()) {
				chains.put(possibleNumber, possibleGraph);
			}
		}
		return chains;
	}
	
	private static void addConjugatePairToGraph(Iterable<Cell> unit, SudokuNumber possibleNumber,
			Pseudograph<Cell, SudokuEdge> possibleGraph) {
		ArrayList<Cell> possibleCellsInUnit = new ArrayList<Cell>();
		for (Cell cell : unit) {
			if (cell.getPossibleValues().contains(possibleNumber)) {
				possibleCellsInUnit.add(cell);
			}
		}
		if (possibleCellsInUnit.size() == 2) {
			Cell firstCell = possibleCellsInUnit.get(0);
			Cell secondCell = possibleCellsInUnit.get(1);
			if (!possibleGraph.containsEdge(firstCell, secondCell)) {
				possibleGraph.addVertex(firstCell);
				possibleGraph.addVertex(secondCell);
				possibleGraph.addEdge(firstCell, secondCell).setLinkType(SudokuEdge.LinkType.STRONG_LINK);
			}
		}
	}
}