package sudokusolver.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.graph.Pseudograph;

import sudokusolver.Cell;
import sudokusolver.Puzzle;
import sudokusolver.SudokuEdge;
import sudokusolver.SudokuNumber;

public class XCycles {
	public static boolean xCycles(Puzzle puzzle, HashMap<SudokuNumber, Pseudograph<Cell, SudokuEdge>> chains) {
		boolean changeMade = false;
		for (Entry<SudokuNumber, Pseudograph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			buildWeakLinks(puzzle, entry.getKey(), entry.getValue());
			if (xCyclesForChain(puzzle, entry.getValue(), entry.getKey(), getAllGraphCycles(entry.getValue()))) {
				changeMade = true;
			}
		}
		return changeMade;
	}
	
	private static void buildWeakLinks(Puzzle puzzle, SudokuNumber possibleNumber, Pseudograph<Cell, SudokuEdge> graph) {
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
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().contains(possibleNumber) && !graph.vertexSet().contains(cell)) {
				ArrayList<Cell> visibleVerticies = new ArrayList<Cell>();
				for (Cell vertex : graph.vertexSet()) {
					if (cell.isInSameUnit(vertex)) {
						visibleVerticies.add(vertex);
					}
				}
				if (visibleVerticies.size() == 2) {
					graph.addVertex(cell);
					for (Cell vertex : visibleVerticies) {
						graph.addEdge(cell, vertex).setLinkType(SudokuEdge.LinkType.WEAK_LINK);
					}
				}
			}
		}
	}
	
	//TODO: This method is too expensive and needs to be removed.  Figure out an alternative to finding all possible cycles.
	private static <T> ArrayList<ArrayList<T>> getAllGraphCycles(Pseudograph<T, SudokuEdge> graph) {
		ArrayList<ArrayList<T>> cycles = new ArrayList<ArrayList<T>>();
		ArrayDeque<T> path = new ArrayDeque<T>();
		for (T vertex : graph.vertexSet()) {
			path.push(vertex);
			findNewCycles(graph, cycles, path);
			path.pop();
		}
		return cycles;
	}
	
	private static <T> void findNewCycles(Pseudograph<T, SudokuEdge> graph, ArrayList<ArrayList<T>> cycles, ArrayDeque<T> path) {
		T previouslyVisitedVertex = path.peek();
		for (SudokuEdge edge : graph.edgeSet()) {
			T edgeSource = graph.getEdgeSource(edge);
			T edgeTarget = graph.getEdgeTarget(edge);
			if (edgeSource.equals(previouslyVisitedVertex)) {
				findNewCyclesForNextVertex(graph, cycles, path, edgeTarget);
			} else if (edgeTarget.equals(previouslyVisitedVertex)) {
				findNewCyclesForNextVertex(graph, cycles, path, edgeSource);
			}
		}
	}
	
	private static <T> void findNewCyclesForNextVertex(Pseudograph<T, SudokuEdge> graph, ArrayList<ArrayList<T>> cycles,
			ArrayDeque<T> path, T nextVertex) {
		if (!path.contains(nextVertex)) {
			path.push(nextVertex);
			findNewCycles(graph, cycles, path);
			path.pop();
		} else if (path.size() > 2 && nextVertex.equals(path.peekLast())) {
			ArrayList<T> pathAsList = new ArrayList<T>(path);
			if (isNewCycle(cycles, pathAsList)) {
				cycles.add(pathAsList);
			}
		}
	}
	
	private static <T> boolean isNewCycle(ArrayList<ArrayList<T>> cycles, ArrayList<T> path) {
		if (cycles.contains(path)) {
			return false;
		}
		for (ArrayList<T> existingPath : cycles) {
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
			if (xCyclesNiceLoopsRule2(puzzle, chain, possibleNumber, cycle) || xCyclesNiceLoopsRule3(puzzle, chain, possibleNumber, cycle)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean xCyclesNiceLoopsRule2(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> chain, SudokuNumber possibleNumber,
			ArrayList<Cell> cycle) {
		if (cycle.size() % 2 == 1) {
			for (int i = 0; i < cycle.size(); i++) {
				if (everyOtherEdgeIsStrong(chain, cycle, i, i)) {
					Cell previousCell = cycle.get(i == 0 ? cycle.size() - 1 : i - 1);
					Cell currentCell = cycle.get(i);
					Cell nextCell = cycle.get(i == cycle.size() - 1 ? 0 : i + 1);
					assert chain.getEdge(previousCell, currentCell).getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK);
					assert chain.getEdge(currentCell, nextCell).getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK);
					puzzle.setValueAndUpdatePossibleValues(currentCell, possibleNumber);
					return true;
				}
			}
		}
		return false;
	}
	
	private static <T> boolean everyOtherEdgeIsStrong(Pseudograph<T, SudokuEdge> graph, ArrayList<T> cycle, int startingIndex,
			int endingIndex) {
		if (!SudokuEdge.LinkType.STRONG_LINK.equals(
				graph.getEdge(cycle.get(startingIndex), cycle.get(incrementListIndex(startingIndex, cycle.size()))).getLinkType())) {
			return false;
		}
		boolean expectingStrongLink = false;
		int index = incrementListIndex(startingIndex, cycle.size());
		while (index != endingIndex) {
			if (expectingStrongLink && !SudokuEdge.LinkType.STRONG_LINK.equals(
					graph.getEdge(cycle.get(index), cycle.get(incrementListIndex(index, cycle.size()))).getLinkType())) {
				return false;
			}
			expectingStrongLink = !expectingStrongLink;
			index = incrementListIndex(index, cycle.size());
		}
		return true;
	}
	
	private static boolean xCyclesNiceLoopsRule3(Puzzle puzzle, Pseudograph<Cell, SudokuEdge> chain, SudokuNumber possibleNumber,
			ArrayList<Cell> cycle) {
		if (cycle.size() % 2 == 1) {
			for (int i = 0; i < cycle.size(); i++) {
				if (everyOtherEdgeIsStrong(chain, cycle, incrementListIndex(i, cycle.size()), i == 0 ? cycle.size() - 1 : i - 1)) {
					puzzle.removePossibleValue(cycle.get(i), possibleNumber);
					return true;
				}
			}
		}
		return false;
	}
	
	private static int incrementListIndex(int index, int listSize) {
		index++;
		if (index == listSize) {
			return 0;
		}
		return index;
	}
}