package sudokusolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.SimpleGraph;

import sudokusolver.solutions.AlternatingInferenceChains;
import sudokusolver.solutions.GroupedXCycles;
import sudokusolver.solutions.HiddenPairs;
import sudokusolver.solutions.HiddenSingles;
import sudokusolver.solutions.HiddenUniqueRectangles;
import sudokusolver.solutions.IntersectionRemoval;
import sudokusolver.solutions.Medusa;
import sudokusolver.solutions.NakedPairs;
import sudokusolver.solutions.NakedTriples;
import sudokusolver.solutions.SimpleColoring;
import sudokusolver.solutions.WXYZWing;
import sudokusolver.solutions.XCycles;
import sudokusolver.solutions.XWing;
import sudokusolver.solutions.XYChain;
import sudokusolver.solutions.XYZWing;
import sudokusolver.solutions.YWing;

public class SudokuSolver {
	public static final String[] NO_DELIMITER_PUZZLES = new String[]{"../Puzzles/PuzzlesFromSudokuWiki"};
	public static final String[] GNOME_PUZZLES = new String[]{"../Puzzles/easy", "../Puzzles/medium", "../Puzzles/hard", "../Puzzles/very_hard"};
	
	public static void main(String[] args) throws IOException {
		for (String filename : NO_DELIMITER_PUZZLES) {
			File puzzleFile = new File(filename);
			solvePuzzles(loadNoDelimiterTextFile(puzzleFile), puzzleFile.getName());
		}
		for (String filename : GNOME_PUZZLES) {
			File puzzleFile = new File(filename);
			solvePuzzles(loadGnomeSudokuPuzzles(puzzleFile), puzzleFile.getName());
		}
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
				if (HiddenSingles.hiddenSingles(puzzle) || NakedPairs.nakedPairs(puzzle) || NakedTriples.nakedTriples(puzzle) || HiddenPairs.hiddenPairs(puzzle) ||
						IntersectionRemoval.intersectionRemoval(puzzle) || XWing.xWing(puzzle)) {
					changeMade = true;
				} else {
					HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> chains = buildChains(puzzle);
					if (SimpleColoring.simpleColoring(puzzle, unmodifiableView(chains)) || YWing.yWing(puzzle) || XYZWing.xyzWing(puzzle)) {
						changeMade = true;
					} else {
						buildWeakLinks(puzzle, chains);
						if (XCycles.xCycles(puzzle, unmodifiableView(chains)) || XYChain.xyChain(puzzle) || Medusa.medusa(puzzle) ||
								HiddenUniqueRectangles.hiddenUniqueRectangles(puzzle) || WXYZWing.wxyzWing(puzzle) ||
								GroupedXCycles.groupedXCycles(puzzle, unmodifiableView(chains)) ||
								AlternatingInferenceChains.alternatingInferenceChains(puzzle)) {
							changeMade = true;
						}
						//TODO: Put solutions here.
					}
				}
			}
		} while (changeMade && puzzle.getEmptyCellCount() != 0);
		return puzzle.getEmptyCellCount() == 0;
	}
	
	public static ArrayList<int[]> loadNoDelimiterTextFile(File puzzleFile) throws IOException {
		ArrayList<int[]> puzzles = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(puzzleFile));
		for (int nextChar = reader.read(); nextChar != -1; nextChar = reader.read()) {
			int[] initialValues = new int[Puzzle.UNIT_SIZE * Puzzle.UNIT_SIZE];
			initialValues[0] = Character.getNumericValue(nextChar);
			for (int i = 1; i < initialValues.length; i++) {
				initialValues[i] = Character.getNumericValue(reader.read());
			}
			reader.readLine();
			puzzles.add(initialValues);
		}
		reader.close();
		return puzzles;
	}
	
	public static ArrayList<int[]> loadGnomeSudokuPuzzles(File puzzleFile) throws FileNotFoundException{
		ArrayList<int[]> puzzles = new ArrayList<>();
		Scanner scanner = new Scanner(puzzleFile);
		while (scanner.hasNextInt()) {
			int[] initialValues = new int[Puzzle.UNIT_SIZE * Puzzle.UNIT_SIZE];
			for (int i = 0; i < initialValues.length; i++) {
				assert scanner.hasNextInt();
				initialValues[i] = scanner.nextInt();
			}
			scanner.nextLine();
			puzzles.add(initialValues);
		}
		scanner.close();
		return puzzles;
	}
	
	private static void solvePuzzles(ArrayList<int[]> puzzles, String filename) {
		for (int i = 0; i < puzzles.size(); i++) {
			Puzzle puzzle = new Puzzle(puzzles.get(i));
			if (solve(puzzle)) {
				puzzle.validateCompletePuzzle();
				System.out.println(filename + " #" + i + ": solved");
			} else {
				System.out.println(filename + " #" + i + ": not solved");
				System.out.println();
				System.out.println();
				System.out.println("Original puzzle:");
				System.out.println();
				for (int j = 0 ; j < puzzles.get(i).length; j++) {
					System.out.print(puzzles.get(i)[j]);
					if ((j + 1) % Puzzle.UNIT_SIZE == 0) {
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
	
	private static HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> buildChains(Puzzle puzzle) {
		HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> chains = new HashMap<>();
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			addConjugatePairsToGraph(row, chains);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			addConjugatePairsToGraph(column, chains);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			addConjugatePairsToGraph(block, chains);
		}
		return chains;
	}
	
	private static void addConjugatePairsToGraph(Iterable<Cell> unit, HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> chains) {
		HashMap<SudokuNumber, ArrayList<Cell>> possibleCellsInUnit = new HashMap<>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
				Util.addToValueList(possibleCellsInUnit, possibleNumber, cell);
			}
		}
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : possibleCellsInUnit.entrySet()) {
			if (entry.getValue().size() == 2) {
				SimpleGraph<Cell, SudokuEdge> graph = chains.get(entry.getKey());
				if (graph == null) {
					graph = new SimpleGraph<>(SudokuEdge.class);
					chains.put(entry.getKey(), graph);
				}
				Cell firstCell = entry.getValue().get(0);
				Cell secondCell = entry.getValue().get(1);
				if (!graph.containsEdge(firstCell, secondCell)) {
					graph.addVertex(firstCell);
					graph.addVertex(secondCell);
					graph.addEdge(firstCell, secondCell, new SudokuEdge(SudokuEdge.LinkType.STRONG_LINK));
				}
			}
		}
	}
	
	private static void buildWeakLinks(Puzzle puzzle, HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> chains) {
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			addWeakPairsToGraph(row, chains);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			addWeakPairsToGraph(column, chains);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			addWeakPairsToGraph(block, chains);
		}
	}
	
	private static void addWeakPairsToGraph(Iterable<Cell> unit, HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> chains) {
		HashMap<SudokuNumber, ArrayList<Cell>> possibleCellsInUnit = new HashMap<>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
				Util.addToValueList(possibleCellsInUnit, possibleNumber, cell);
			}
		}
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : possibleCellsInUnit.entrySet()) {
			SimpleGraph<Cell, SudokuEdge> graph = chains.get(entry.getKey());
			if (graph == null) {
				graph = new SimpleGraph<>(SudokuEdge.class);
				chains.put(entry.getKey(), graph);
			}
			for (int i = 0; i < possibleCellsInUnit.get(entry.getKey()).size() - 1; i++) {
				Cell firstCell = possibleCellsInUnit.get(entry.getKey()).get(i);
				for (int j = i + 1; j < possibleCellsInUnit.get(entry.getKey()).size(); j++) {
					Cell secondCell = possibleCellsInUnit.get(entry.getKey()).get(j);
					if (!graph.containsEdge(firstCell, secondCell)) {
						graph.addVertex(firstCell);
						graph.addVertex(secondCell);
						graph.addEdge(firstCell, secondCell, new SudokuEdge(SudokuEdge.LinkType.WEAK_LINK));
					}
				}
			}
		}
	}
	
	private static Map<SudokuNumber, AsUnmodifiableGraph<Cell, SudokuEdge>> unmodifiableView(HashMap<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> chains) {
		HashMap<SudokuNumber, AsUnmodifiableGraph<Cell, SudokuEdge>> unmodifiableChains = new HashMap<>();
		for (Entry<SudokuNumber, SimpleGraph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			unmodifiableChains.put(entry.getKey(), new AsUnmodifiableGraph<>(entry.getValue()));
		}
		return Collections.unmodifiableMap(unmodifiableChains);
	}
}