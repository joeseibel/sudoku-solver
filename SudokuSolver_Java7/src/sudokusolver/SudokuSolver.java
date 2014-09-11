package sudokusolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

public class SudokuSolver {
	private static final int FOCUS_INDEX = 20;
	
	public static void main(String[] args) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("puzzles/very_hard"));
		for (int puzzleIndex = 0; scanner.hasNextInt(); puzzleIndex++) {
			int[] initialValues = new int[81];
			for (int i = 0; i < initialValues.length; i++) {
				initialValues[i] = scanner.nextInt();
			}
			scanner.nextLine();
			if (puzzleIndex != FOCUS_INDEX && FOCUS_INDEX != -1)
				continue;
			Puzzle puzzle = new Puzzle(initialValues);
			
///			int solvedCount = 0;
///			int notSolvedCount = 0;
///			for (int i = 0; i < 10000; i++) {
///				if (i % 100 == 0) {
///					System.out.println(i);
///				}
///				Puzzle puzzle = new Puzzle(initialValues);
///				fillPossibleValues(puzzle);
///				if (solve(puzzle)) {
///					puzzle.validateCompletePuzzle();
///					solvedCount++;
///				} else {
///					notSolvedCount++;
///				}
///			}
///			System.out.println("solvedCount: " + solvedCount);
///			System.out.println("notSolvedCount: " + notSolvedCount);
			
			
			if (puzzleIndex == FOCUS_INDEX) {
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println(puzzle.getEmptyCellCount());
				System.out.println(puzzle);
				System.out.println();
			}
			if (solve(puzzle)) {
				puzzle.validateCompletePuzzle();
				System.out.println("Puzzle " + puzzleIndex + ": solved");
			} else {
				if (puzzleIndex == FOCUS_INDEX) {
					System.out.println();
					System.out.println();
					System.out.println(puzzle.getEmptyCellCount());
					System.out.println(puzzle);
					System.out.println();
					System.out.println();
					System.out.println();
					System.out.println(puzzle.getPossibleString());
					System.out.println();
					System.out.println();
					System.out.println();
				}
				System.out.println("Puzzle " + puzzleIndex + ": not solved");
			}
		}
		scanner.close();
	}
	
	private static boolean solve(Puzzle puzzle) {
		boolean changeMade;
		do {
			changeMade = false;
			int startingEmptyCellCount = puzzle.getEmptyCellCount();
			checkForSolvedCells(puzzle);
			if (startingEmptyCellCount != puzzle.getEmptyCellCount()) {
				changeMade = true;
			}
			if (puzzle.getEmptyCellCount() != 0) {
				if (hiddenSingles(puzzle) || nakedPairs(puzzle) || nakedTriples(puzzle) || intersectionRemoval(puzzle) || xWing(puzzle)) {
					changeMade = true;
				} else {
					HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains = buildChains(puzzle);
					if (simpleColoring(puzzle, chains) || yWing(puzzle) || xyzWing(puzzle) || xCycles(puzzle, chains) || xyChain(puzzle) ||
							medusa(puzzle) || hiddenUniqueRectangles(puzzle)) {
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
	
	private static boolean hiddenSingles(Puzzle puzzle) {
		boolean changeMade = false;
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			if (hiddenSingles(puzzle, row)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			if (hiddenSingles(puzzle, column)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			if (hiddenSingles(puzzle, block)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean hiddenSingles(Puzzle puzzle, Iterable<Cell> unit) {
		HashMap<SudokuNumber, ArrayList<Cell>> potentialCells = new HashMap<SudokuNumber, ArrayList<Cell>>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleValue : cell.getPossibleValues()) {
				ArrayList<Cell> potentialCellsForThisPossibleValue = potentialCells.get(possibleValue);
				if (potentialCellsForThisPossibleValue == null) {
					potentialCellsForThisPossibleValue = new ArrayList<Cell>();
					potentialCells.put(possibleValue, potentialCellsForThisPossibleValue);
				}
				potentialCellsForThisPossibleValue.add(cell);
			}
		}
		boolean changeMade = false;
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : potentialCells.entrySet()) {
			if (entry.getValue().size() == 1) {
				puzzle.setValueAndUpdatePossibleValues(entry.getValue().get(0), entry.getKey());
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean nakedPairs(Puzzle puzzle) {
		boolean changeMade = false;
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			ArrayList<Set<SudokuNumber>> nakedPairs = findNakedPairs(row);
			if (eliminatePossibleValuesBasedOnNakedPairs(puzzle, row, nakedPairs)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			ArrayList<Set<SudokuNumber>> nakedPairs = findNakedPairs(column);
			if (eliminatePossibleValuesBasedOnNakedPairs(puzzle, column, nakedPairs)) {
				changeMade = true;
			}
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			ArrayList<Set<SudokuNumber>> nakedPairs = findNakedPairs(block);
			if (eliminatePossibleValuesBasedOnNakedPairs(puzzle, block, nakedPairs)) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static ArrayList<Set<SudokuNumber>> findNakedPairs(Iterable<Cell> unit) {
		ArrayList<Set<SudokuNumber>> encounteredPairs = new ArrayList<Set<SudokuNumber>>();
		ArrayList<Set<SudokuNumber>> nakedPairs = new ArrayList<Set<SudokuNumber>>();
		for (Cell cell : unit) {
			if (cell.getPossibleValues().size() == 2) {
				if (encounteredPairs.contains(cell.getPossibleValues())) {
					nakedPairs.add(cell.getPossibleValues());
				} else {
					encounteredPairs.add(cell.getPossibleValues());
				}
			}
		}
		return nakedPairs;
	}
	
	private static boolean eliminatePossibleValuesBasedOnNakedPairs(Puzzle puzzle, Iterable<Cell> unit,
			ArrayList<Set<SudokuNumber>> nakedPairs) {
		boolean changeMade = false;
		for (Set<SudokuNumber> pair : nakedPairs) {
			for (Cell cell : unit) {
				if (!cell.getPossibleValues().equals(pair)) {
					if (puzzle.removePossibleValues(cell, pair)) {
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
	
	private static boolean nakedTriples(Puzzle puzzle) {
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
		ArrayList<Cell> cellsWithTwoOrThreePossibleValues = new ArrayList<Cell>();
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
	
	private static boolean intersectionRemoval(Puzzle puzzle) {
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
	
	private static boolean xWing(Puzzle puzzle) {
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
	
	private static ArrayList<ArrayList<Cell>> getUnitPairs(SudokuNumber possibleNumber, Iterable<Iterable<Cell>> units) {
		ArrayList<ArrayList<Cell>> unitPairs = new ArrayList<ArrayList<Cell>>();
		for (Iterable<Cell> unit : units) {
			ArrayList<Cell> pairForCurrentUnit = new ArrayList<Cell>();
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
	
	private static boolean simpleColoring(Puzzle puzzle, HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains) {
		boolean changeMade = false;
		for (Entry<SudokuNumber, Pseudograph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			ArrayList<HashMap<Cell, VertexColor>> coloredChains = new ArrayList<HashMap<Cell, VertexColor>>();
			for (Pseudograph<Cell, SudokuEdge> chain : getConnectedSubgraphs(entry.getValue(), SudokuEdge.class)) {
				HashMap<Cell, VertexColor> vertexColors = new HashMap<Cell, VertexColor>();
				Cell firstCell = chain.vertexSet().iterator().next();
				vertexColors.put(firstCell, VertexColor.BLACK);
				colorGraph(chain, vertexColors, firstCell, VertexColor.BLUE);
				coloredChains.add(vertexColors);
			}
			//Intentionally used bitwise or operator.  We want to execute both methods.
			if (simpleColoringRule2TwiceInAUnit(puzzle, coloredChains, entry.getKey()) |
					simpleColoringRule5TwoColorsElsewhere(puzzle, coloredChains, entry.getKey())) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static <V, E> ArrayList<Pseudograph<V, E>> getConnectedSubgraphs(Pseudograph<V, E> graph, Class<? extends E> edgeClass) {
		ArrayList<Pseudograph<V, E>> connectedSubgraphs = new ArrayList<Pseudograph<V,E>>();
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<V, E>(graph);
		if (inspector.isGraphConnected()) {
			connectedSubgraphs.add(graph);
		} else {
			for (Set<V> subgraphVerticies : inspector.connectedSets()) {
				Pseudograph<V, E> subgraph = new Pseudograph<V, E>(edgeClass);
				for (V vertex : subgraphVerticies) {
					subgraph.addVertex(vertex);
				}
				for (E edge : graph.edgeSet()) {
					if (subgraphVerticies.contains(graph.getEdgeSource(edge))) {
						subgraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
					}
				}
				connectedSubgraphs.add(subgraph);
			}
		}
		return connectedSubgraphs;
	}
	
	private static <V, E> void colorGraph(Pseudograph<V, E> graph, HashMap<V, VertexColor> vertexColors, V previousVertex,
			VertexColor nextColor) {
		for (E connectingEdge : graph.edgesOf(previousVertex)) {
			V nextVertex = graph.getEdgeSource(connectingEdge);
			if (nextVertex.equals(previousVertex)) {
				nextVertex = graph.getEdgeTarget(connectingEdge);
			}
			VertexColor colorOfNextVertex = vertexColors.get(nextVertex);
			if (colorOfNextVertex == null) {
				vertexColors.put(nextVertex, nextColor);
				colorGraph(graph, vertexColors, nextVertex, nextColor.getOpposite());
			} else if (colorOfNextVertex.equals(nextColor)) {
				//No worries. Unwind call stack.
			} else {
				assert false;
			}
		}
	}
	
	private static boolean simpleColoringRule2TwiceInAUnit(Puzzle puzzle, ArrayList<HashMap<Cell, VertexColor>> coloredChains,
			SudokuNumber possibleNumber) {
		boolean changeMade = false;
		for (HashMap<Cell, VertexColor> vertexColors : coloredChains) {
			VertexColor contradictingColor = searchForSameColorInSameUnit(vertexColors);
			if (contradictingColor != null) {
				removePossibleValuesFromColor(puzzle, vertexColors, contradictingColor, possibleNumber);
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static VertexColor searchForSameColorInSameUnit(HashMap<Cell, VertexColor> cellColors) {
		ArrayList<Entry<Cell, VertexColor>> entryList = new ArrayList<Entry<Cell,VertexColor>>(cellColors.entrySet());
		for (int i = 0; i < entryList.size() - 1; i++) {
			for (int j = i + 1; j < entryList.size(); j++) {
				if (entryList.get(i).getValue().equals(entryList.get(j).getValue()) &&
						entryList.get(i).getKey().isInSameUnit(entryList.get(j).getKey())) {
					return entryList.get(i).getValue();
				}
			}
		}
		return null;
	}
	
	private static void removePossibleValuesFromColor(Puzzle puzzle, HashMap<Cell, VertexColor> cellColors, VertexColor contradictingColor,
			SudokuNumber possibleNumber) {
		for (Entry<Cell, VertexColor> entry : cellColors.entrySet()) {
			if (entry.getValue().equals(contradictingColor)) {
				puzzle.removePossibleValue(entry.getKey(), possibleNumber);
			}
		}
	}
	
	private static boolean simpleColoringRule5TwoColorsElsewhere(Puzzle puzzle, ArrayList<HashMap<Cell, VertexColor>> coloredChains,
			SudokuNumber possibleNumber) {
		for (HashMap<Cell, VertexColor> vertexColors : coloredChains) {
			boolean changeMade = false;
			for (Cell cell : puzzle.getAllEmptyCells()) {
				if (!vertexColors.containsKey(cell) && canCellSeeColoredCell(cell, vertexColors, VertexColor.BLACK) &&
						canCellSeeColoredCell(cell, vertexColors, VertexColor.BLUE) && puzzle.removePossibleValue(cell, possibleNumber)) {
					changeMade = true;
				}
			}
			if (changeMade) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean canCellSeeColoredCell(Cell cell, HashMap<Cell, VertexColor> cellColors, VertexColor cellColor) {
		for (Entry<Cell, VertexColor> entry : cellColors.entrySet()) {
			if (entry.getValue().equals(cellColor) && cell.isInSameUnit(entry.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean yWing(Puzzle puzzle) {
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
	
	private static boolean xyzWing(Puzzle puzzle) {
		ArrayList<Cell> cellsWithThreePossibleValues = new ArrayList<Cell>();
		ArrayList<Cell> cellsWithTwoPossibleValues = new ArrayList<Cell>();
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 3) {
				cellsWithThreePossibleValues.add(cell);
			} else if (cell.getPossibleValues().size() == 2) {
				cellsWithTwoPossibleValues.add(cell);
			}
		}
		for (Cell pivotCell : cellsWithThreePossibleValues) {
			ArrayList<Cell> wingCandidates = new ArrayList<Cell>();
			for (Cell cell : cellsWithTwoPossibleValues) {
				if (pivotCell.isInSameUnit(cell) && pivotCell.getPossibleValues().containsAll(cell.getPossibleValues())) {
					wingCandidates.add(cell);
				}
			}
			for (int i = 0; i < wingCandidates.size() - 1; i++) {
				for (int j = i + 1; j < wingCandidates.size(); j++) {
					EnumSet<SudokuNumber> possibleNumberIntersection = EnumSet.copyOf(wingCandidates.get(i).getPossibleValues());
					possibleNumberIntersection.retainAll(wingCandidates.get(j).getPossibleValues());
					if (possibleNumberIntersection.size() == 1 && !wingCandidates.get(i).isInSameUnit(wingCandidates.get(j))) {
						boolean changeMade = false;
						for (Cell emptyCell : puzzle.getAllEmptyCells()) {
							if (!emptyCell.equals(pivotCell) && !emptyCell.equals(wingCandidates.get(i)) &&
									!emptyCell.equals(wingCandidates.get(j)) && emptyCell.isInSameUnit(pivotCell) &&
									emptyCell.isInSameUnit(wingCandidates.get(i)) && emptyCell.isInSameUnit(wingCandidates.get(j)) &&
									puzzle.removePossibleValues(emptyCell, possibleNumberIntersection)) {
								changeMade = true;
							}
						}
						return changeMade;
					}
				}
			}
		}
		return false;
	}
	
	private static boolean xCycles(Puzzle puzzle, HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains) {
		boolean changeMade = false;
		for (Entry<SudokuNumber, Pseudograph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			buildWeakLinks(entry.getValue());
			if (xCyclesForChain(puzzle, entry.getValue(), entry.getKey(), getAllGraphCycles(entry.getValue()))) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static void buildWeakLinks(Pseudograph<Cell, SudokuEdge> graph) {
		ArrayList<Cell> verticies = new ArrayList<Cell>(graph.vertexSet());
		for (int i = 0; i < verticies.size() - 1; i++) {
			for (int j = i + 1; j < verticies.size(); j++) {
				Cell a = verticies.get(i);
				Cell b = verticies.get(j);
				if (a.isInSameUnit(b) && !graph.containsEdge(a, b)) {
					graph.addEdge(a, b).setLinkType(SudokuEdge.LinkType.WEAK_LINK);
				}
			}
		}
	}
	
	private static ArrayList<ArrayList<Cell>> getAllGraphCycles(Pseudograph<Cell, SudokuEdge> graph) {
		ArrayList<ArrayList<Cell>> cycles = new ArrayList<ArrayList<Cell>>();
		ArrayDeque<Cell> path = new ArrayDeque<Cell>();
		for (Cell cell : graph.vertexSet()) {
			path.push(cell);
			findNewCycles(graph, cycles, path);
			path.pop();
		}
		return cycles;
	}
	
	private static void findNewCycles(Pseudograph<Cell, SudokuEdge> graph, ArrayList<ArrayList<Cell>> cycles, ArrayDeque<Cell> path) {
		Cell previouslyVisitedCell = path.peek();
		for (SudokuEdge edge : graph.edgeSet()) {
			Cell edgeSource = graph.getEdgeSource(edge);
			Cell edgeTarget = graph.getEdgeTarget(edge);
			if (edgeSource.equals(previouslyVisitedCell)) {
				findNewCyclesForNextCell(graph, cycles, path, edgeTarget);
			} else if (edgeTarget.equals(previouslyVisitedCell)) {
				findNewCyclesForNextCell(graph, cycles, path, edgeSource);
			}
		}
	}
	
	private static void findNewCyclesForNextCell(Pseudograph<Cell, SudokuEdge> graph, ArrayList<ArrayList<Cell>> cycles,
			ArrayDeque<Cell> path, Cell nextCell) {
		if (!path.contains(nextCell)) {
			path.push(nextCell);
			findNewCycles(graph, cycles, path);
			path.pop();
		} else if (path.size() > 2 && nextCell.equals(path.peekLast())) {
			ArrayList<Cell> pathAsList = new ArrayList<Cell>(path);
			if (isNewCycle(cycles, pathAsList)) {
				cycles.add(pathAsList);
			}
		}
	}
	
	private static boolean isNewCycle(ArrayList<ArrayList<Cell>> cycles, ArrayList<Cell> path) {
		if (cycles.contains(path)) {
			return false;
		}
		for (ArrayList<Cell> existingPath : cycles) {
			if (path.size() == existingPath.size()) {
				int firstCellIndex = path.indexOf(existingPath.get(0));
				if (firstCellIndex != -1) {
					int pathSize = path.size();
					if (path.subList(firstCellIndex, pathSize).equals(existingPath.subList(0, pathSize - firstCellIndex)) &&
							path.subList(0, firstCellIndex).equals(existingPath.subList(pathSize - firstCellIndex, pathSize))) {
						return false;
					}
					boolean invertedEquals = true;
					for (int i = 0; i < pathSize; i++) {
						int equivalentPathIndex = firstCellIndex - i >= 0 ? firstCellIndex - i : pathSize + firstCellIndex - i;
						if (!existingPath.get(i).equals(path.get(equivalentPathIndex))) {
							invertedEquals = false;
							break;
						}
					}
					if (invertedEquals) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private static boolean xCyclesForChain(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> chain, SudokuNumber possibleNumber,
			ArrayList<ArrayList<Cell>> cycles) {
		for (ArrayList<Cell> cycle : cycles) {
			if (xCyclesNiceLoopsRule2(puzzle, chain, possibleNumber, cycle) /*|| xCyclesNiceLoopsRule3(chain, possibleNumber, cycle)*/) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean xCyclesNiceLoopsRule2(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> chain, SudokuNumber possibleNumber,
			ArrayList<Cell> cycle) {
		if (cycle.size() % 2 == 1) {
			ArrayList<Cell> cellsWithTwoStrongLinks = new ArrayList<Cell>();
			for (int i = 0; i < cycle.size(); i++) {
				Cell currentCell = cycle.get(i);
				Cell previousCell = cycle.get(i == 0 ? cycle.size() - 1 : i - 1);
				Cell nextCell = cycle.get(i == cycle.size() - 1 ? 0 : i + 1);
				if (chain.getEdge(currentCell, previousCell).getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK) &&
						chain.getEdge(currentCell, nextCell).getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
					cellsWithTwoStrongLinks.add(currentCell);
				}
			}
			if (cellsWithTwoStrongLinks.size() == 1 && otherEdgesAlternate(chain, cycle, cellsWithTwoStrongLinks.get(0))) {
				puzzle.setValueAndUpdatePossibleValues(cellsWithTwoStrongLinks.get(0), possibleNumber);
				return true;
			}
		}
		return false;
	}
	
	private static boolean otherEdgesAlternate(Pseudograph<Cell, SudokuEdge> graph, ArrayList<Cell> cycle, Cell indexCell) {
		final int startingIndex = cycle.indexOf(indexCell);
		SudokuEdge.LinkType previousEdgeType =
				graph.getEdge(indexCell, cycle.get(incrementListIndex(startingIndex, cycle.size()))).getLinkType();
		int index = incrementListIndex(startingIndex, cycle.size());
		while (index != startingIndex) {
			SudokuEdge.LinkType currentEdgeType =
					graph.getEdge(cycle.get(index), cycle.get(incrementListIndex(index, cycle.size()))).getLinkType();
			if (currentEdgeType.equals(previousEdgeType)) {
				return false;
			}
			previousEdgeType = currentEdgeType;
			index = incrementListIndex(index, cycle.size());
		}
		return true;
	}
	
	private static int incrementListIndex(int index, int listSize) {
		index++;
		if (index == listSize) {
			return 0;
		}
		return index;
	}
	
//	private static boolean xCyclesNiceLoopsRule3(Pseudograph<Cell, SudokuEdge> chain, SudokuNumber possibleNumber, ArrayList<Cell> cycle) {
//		System.out.println("Posible Number: " + possibleNumber + "; Cycle Size: " + cycle.size());
//		if (cycle.size() % 2 == 1) {
//			ArrayList<Cell> cellsWithTwoWeakLinks = new ArrayList<Cell>();
//			for (int i = 0; i < cycle.size(); i++) {
//				Cell currentCell = cycle.get(i);
//				Cell previousCell = cycle.get(i == 0 ? cycle.size() - 1 : i - 1);
//				Cell nextCell = cycle.get(i == cycle.size() - 1 ? 0 : i + 1);
//				if (chain.getEdge(currentCell, previousCell).getLinkType().equals(SudokuEdge.LinkType.WEAK_LINK) &&
//						chain.getEdge(currentCell, nextCell).getLinkType().equals(SudokuEdge.LinkType.WEAK_LINK)) {
//					cellsWithTwoWeakLinks.add(currentCell);
//				}
//			}
//			if (cellsWithTwoWeakLinks.size() == 1 && otherEdgesAlternate(chain, cycle, cellsWithTwoWeakLinks.get(0))) {
//				cellsWithTwoWeakLinks.get(0).getPossibleValues().remove(possibleNumber);
//				return true;
//			}
//		}
//		return false;
//	}
	
	private static boolean xyChain(Puzzle puzzle) {
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
	
	private static boolean medusa(Puzzle puzzle) {
		Pseudograph<PossibleNumberInCell, DefaultEdge> graph = new Pseudograph<PossibleNumberInCell, DefaultEdge>(DefaultEdge.class);
		for (Iterable<Cell> row : puzzle.getAllRows()) {
			buildGraphForUnit(graph, row);
		}
		for (Iterable<Cell> column : puzzle.getAllColumns()) {
			buildGraphForUnit(graph, column);
		}
		for (Iterable<Cell> block : puzzle.getAllBlocks()) {
			buildGraphForUnit(graph, block);
		}
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().size() == 2) {
				Iterator<SudokuNumber> iter = cell.getPossibleValues().iterator();
				PossibleNumberInCell source = new PossibleNumberInCell(cell, iter.next());
				PossibleNumberInCell target = new PossibleNumberInCell(cell, iter.next());
				graph.addVertex(source);
				graph.addVertex(target);
				graph.addEdge(source, target);
			}
		}
		if (!graph.vertexSet().isEmpty()) {
			for (Pseudograph<PossibleNumberInCell, DefaultEdge> subgraph : getConnectedSubgraphs(graph, DefaultEdge.class)) {
				HashMap<PossibleNumberInCell, VertexColor> vertexColors = new HashMap<PossibleNumberInCell, VertexColor>();
				PossibleNumberInCell firstVertex = subgraph.vertexSet().iterator().next();
				vertexColors.put(firstVertex, VertexColor.BLACK);
				colorGraph(subgraph, vertexColors, firstVertex, VertexColor.BLUE);
				//Intentionally used bitwise or operator.  We want to execute both methods.
				if (medusaTwoColorsElsewhere(puzzle, vertexColors) | medusaTwoColorsUnitPlusCell(puzzle, vertexColors)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void buildGraphForUnit(Pseudograph<PossibleNumberInCell, DefaultEdge> graph, Iterable<Cell> unit) {
		HashMap<SudokuNumber, ArrayList<Cell>> cellsForPossibleNumber = new HashMap<SudokuNumber, ArrayList<Cell>>();
		for (Cell cell : unit) {
			for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
				ArrayList<Cell> cellsList = cellsForPossibleNumber.get(possibleNumber);
				if (cellsList == null) {
					cellsList = new ArrayList<Cell>();
					cellsForPossibleNumber.put(possibleNumber, cellsList);
				}
				cellsList.add(cell);
			}
		}
		for (Entry<SudokuNumber, ArrayList<Cell>> entry : cellsForPossibleNumber.entrySet()) {
			if (entry.getValue().size() == 2) {
				PossibleNumberInCell source = new PossibleNumberInCell(entry.getValue().get(0), entry.getKey());
				PossibleNumberInCell target = new PossibleNumberInCell(entry.getValue().get(1), entry.getKey());
				graph.addVertex(source);
				graph.addVertex(target);
				graph.addEdge(source, target);
			}
		}
	}
	
	private static boolean medusaTwoColorsElsewhere(Puzzle puzzle, HashMap<PossibleNumberInCell, VertexColor> vertexColors) {
		HashSet<Cell> cellsContainingColors = new HashSet<Cell>();
		for (PossibleNumberInCell possibleNumberInCell : vertexColors.keySet()) {
			cellsContainingColors.add(possibleNumberInCell.getCell());
		}
		boolean changeMade = false;
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (!cellsContainingColors.contains(cell)) {
				for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
					boolean canSeeBlackNumber = false;
					boolean canSeeBlueNumber = false;
					for (Iterator<Entry<PossibleNumberInCell, VertexColor>> iter = vertexColors.entrySet().iterator();
							iter.hasNext() && (!canSeeBlackNumber || !canSeeBlueNumber);) {
						Entry<PossibleNumberInCell, VertexColor> coloredNumber = iter.next();
						if (coloredNumber.getKey().getPossibleNumber().equals(possibleNumber) &&
								coloredNumber.getKey().getCell().isInSameUnit(cell)) {
							switch (coloredNumber.getValue()) {
								case BLACK:
									canSeeBlackNumber = true;
									break;
								case BLUE:
									canSeeBlueNumber = true;
									break;
								default:
									assert false;
							}
						}
					}
					if (canSeeBlackNumber && canSeeBlueNumber) {
						puzzle.removePossibleValue(cell, possibleNumber);
						changeMade = true;
					}
				}
			}
		}
		return changeMade;
	}
	
	private static boolean medusaTwoColorsUnitPlusCell(Puzzle puzzle, HashMap<PossibleNumberInCell, VertexColor> vertexColors) {
		boolean changeMade = false;
		for (Entry<PossibleNumberInCell, VertexColor> currentColoredNumber : vertexColors.entrySet()) {
			if (removeOtherPossibleNumberIfItCanSeeOppositeColorInOtherCell(puzzle, vertexColors, currentColoredNumber.getKey().getCell(),
					currentColoredNumber.getValue())) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static boolean removeOtherPossibleNumberIfItCanSeeOppositeColorInOtherCell(Puzzle puzzle, HashMap<PossibleNumberInCell,
			VertexColor> vertexColors, Cell currentCell, VertexColor currentColor) {
		for (SudokuNumber possibleNumberInCurrentCell : currentCell.getPossibleValues()) {
			if (!vertexColors.containsKey(new PossibleNumberInCell(currentCell, possibleNumberInCurrentCell))) {
				for (Entry<PossibleNumberInCell, VertexColor> otherColoredNumber : vertexColors.entrySet()) {
					Cell otherCell = otherColoredNumber.getKey().getCell();
					SudokuNumber otherPossibleNumber = otherColoredNumber.getKey().getPossibleNumber();
					VertexColor otherColor = otherColoredNumber.getValue();
					if (!otherCell.equals(currentCell) && otherCell.isInSameUnit(currentCell) &&
							otherPossibleNumber.equals(possibleNumberInCurrentCell) && otherColor.equals(currentColor.getOpposite())) {
						puzzle.removePossibleValue(currentCell, possibleNumberInCurrentCell);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static boolean hiddenUniqueRectangles(Puzzle puzzle) {
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