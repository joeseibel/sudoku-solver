package sudokusolver.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.SimpleGraph;

import sudokusolver.Cell;
import sudokusolver.CellOrGroup;
import sudokusolver.Puzzle;
import sudokusolver.SudokuEdge;
import sudokusolver.SudokuNumber;

public class GroupedXCycles {
	public static boolean groupedXCycles(Puzzle puzzle, Map<SudokuNumber, AsUnmodifiableGraph<Cell, SudokuEdge>> chains) {
		for (Entry<SudokuNumber, AsUnmodifiableGraph<Cell, SudokuEdge>> entry : chains.entrySet()) {
			SimpleGraph<CellOrGroup, SudokuEdge> graph = copyGraph(entry.getValue());
			addGroupsToGraph(puzzle, entry.getKey(), graph);
			if (groupedXCyclesNiceLoopsRule3(puzzle, graph, entry.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	private static SimpleGraph<CellOrGroup, SudokuEdge> copyGraph(AsUnmodifiableGraph<Cell, SudokuEdge> graph) {
		SimpleGraph<CellOrGroup, SudokuEdge> copy = new SimpleGraph<>(SudokuEdge.class);
		for (Cell cell : graph.vertexSet()) {
			copy.addVertex(cell);
		}
		for (SudokuEdge edge : graph.edgeSet()) {
			copy.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), new SudokuEdge(edge.getLinkType()));
		}
		return copy;
	}
	
	private static void addGroupsToGraph(Puzzle puzzle, SudokuNumber possibleNumber, SimpleGraph<CellOrGroup, SudokuEdge> graph) {
		ArrayList<RowGroup> rowGroups = createRowGroups(puzzle, possibleNumber);
		ArrayList<ColumnGroup> columnGroups = createColumnGroups(puzzle, possibleNumber);
		for (Cell cell : puzzle.getAllEmptyCells()) {
			if (cell.getPossibleValues().contains(possibleNumber)) {
				for (RowGroup rowGroup : rowGroups) {
					if (!rowGroup.cells.contains(cell)) {
						if (rowGroup.isInSameRow(cell)) {
							boolean makeStrongLink = true;
							for (Iterator<Cell> iter = puzzle.getRowCells(cell).iterator(); makeStrongLink && iter.hasNext();) {
								Cell otherCellInRow = iter.next();
								if (otherCellInRow.getPossibleValues().contains(possibleNumber) && !otherCellInRow.equals(cell) &&
										!rowGroup.cells.contains(otherCellInRow)) {
									makeStrongLink = false;
								}
							}
							graph.addVertex(rowGroup);
							graph.addEdge(cell, rowGroup, new SudokuEdge(makeStrongLink ? SudokuEdge.LinkType.STRONG_LINK : SudokuEdge.LinkType.WEAK_LINK));
						} else if (rowGroup.isInSameBlock(cell)) {
							boolean makeStrongLink = true;
							for (Iterator<Cell> iter = puzzle.getColumnCells(cell).iterator(); makeStrongLink && iter.hasNext();) {
								Cell otherCellInBlock = iter.next();
								if (otherCellInBlock.getPossibleValues().contains(possibleNumber) && !otherCellInBlock.equals(cell) &&
										!rowGroup.cells.contains(otherCellInBlock)) {
									makeStrongLink = false;
								}
							}
							graph.addVertex(rowGroup);
							graph.addEdge(cell, rowGroup, new SudokuEdge(makeStrongLink ? SudokuEdge.LinkType.STRONG_LINK : SudokuEdge.LinkType.WEAK_LINK));
						}
					}
				}
				for (ColumnGroup columnGroup : columnGroups) {
					if (!columnGroup.cells.contains(cell)) {
						if (columnGroup.isInSameColumn(cell)) {
							boolean makeStrongLink = true;
							for (Iterator<Cell> iter = puzzle.getColumnCells(cell).iterator(); makeStrongLink && iter.hasNext();) {
								Cell otherCellInColumn = iter.next();
								if (otherCellInColumn.getPossibleValues().contains(possibleNumber) && !otherCellInColumn.equals(cell) &&
										!columnGroup.cells.contains(otherCellInColumn)) {
									makeStrongLink = false;
								}
							}
							graph.addVertex(columnGroup);
							graph.addEdge(cell, columnGroup,
									new SudokuEdge(makeStrongLink ? SudokuEdge.LinkType.STRONG_LINK : SudokuEdge.LinkType.WEAK_LINK));
						} else if (columnGroup.isInSameBlock(cell)) {
							boolean makeStrongLink = true;
							for (Iterator<Cell> iter = puzzle.getColumnCells(cell).iterator(); makeStrongLink && iter.hasNext();) {
								Cell otherCellInBlock = iter.next();
								if (otherCellInBlock.getPossibleValues().contains(possibleNumber) && !otherCellInBlock.equals(cell) &&
										!columnGroup.cells.contains(otherCellInBlock)) {
									makeStrongLink = false;
								}
							}
							graph.addVertex(columnGroup);
							graph.addEdge(cell, columnGroup,
									new SudokuEdge(makeStrongLink ? SudokuEdge.LinkType.STRONG_LINK : SudokuEdge.LinkType.WEAK_LINK));
						}
					}
				}
			}
		}
	}
	
	private static ArrayList<RowGroup> createRowGroups(Puzzle puzzle, SudokuNumber possibleNumber) {
		ArrayList<RowGroup> rowGroups = new ArrayList<>();
		for (ArrayList<Cell> row : puzzle.getAllRowsAsLists()) {
			for (List<Cell> blockInRow : getBlocksInLinearUnit(row)) {
				ArrayList<Cell> cellsWithPossibleNumber = new ArrayList<>();
				for (Cell cell : blockInRow) {
					if (cell.getPossibleValues().contains(possibleNumber)) {
						cellsWithPossibleNumber.add(cell);
					}
				}
				if (cellsWithPossibleNumber.size() > 1) {
					rowGroups.add(new RowGroup(cellsWithPossibleNumber));
				}
			}
		}
		return rowGroups;
	}
	
	private static ArrayList<ColumnGroup> createColumnGroups(Puzzle puzzle, SudokuNumber possibleNumber) {
		ArrayList<ColumnGroup> columnGroups = new ArrayList<>();
		for (ArrayList<Cell> column : puzzle.getAllColumnsAsLists()) {
			for (List<Cell> blockInColumn : getBlocksInLinearUnit(column)) {
				ArrayList<Cell> cellsWithPossibleNumber = new ArrayList<>();
				for (Cell cell : blockInColumn) {
					if (cell.getPossibleValues().contains(possibleNumber)) {
						cellsWithPossibleNumber.add(cell);
					}
				}
				if (cellsWithPossibleNumber.size() > 1) {
					columnGroups.add(new ColumnGroup(cellsWithPossibleNumber));
				}
			}
		}
		return columnGroups;
	}
	
	private static Iterable<List<Cell>> getBlocksInLinearUnit(final ArrayList<Cell> linearUnit) {
		return new Iterable<List<Cell>>() {
			@Override
			public Iterator<List<Cell>> iterator() {
				return new Iterator<List<Cell>>() {
					private int currentBlockIndex = -1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public List<Cell> next() {
						if (currentBlockIndex + 1 < Puzzle.UNIT_SIZE_SQUARE_ROOT) {
							currentBlockIndex++;
							return linearUnit.subList(currentBlockIndex * Puzzle.UNIT_SIZE_SQUARE_ROOT, (currentBlockIndex + 1) * Puzzle.UNIT_SIZE_SQUARE_ROOT);
						} else {
							throw new NoSuchElementException();
						}
					}
					
					@Override
					public boolean hasNext() {
						return currentBlockIndex + 1 < Puzzle.UNIT_SIZE_SQUARE_ROOT;
					}
				};
			}
		};
	}
	
	private static boolean groupedXCyclesNiceLoopsRule3(Puzzle puzzle, SimpleGraph<CellOrGroup, SudokuEdge> graph, SudokuNumber possibleNumber) {
		for (CellOrGroup vertex : graph.vertexSet()) {
			if (vertex instanceof Cell) {
				SudokuEdge[] edges = graph.edgesOf(vertex).toArray(new SudokuEdge[0]);
				for (int i = 0; i < edges.length - 1; i++) {
					CellOrGroup nextVertex = Common.getOtherVertex(graph, edges[i], vertex);
					for (int j = i + 1; j < edges.length; j++) {
						ArrayDeque<CellOrGroup> cycle = new ArrayDeque<>();
						cycle.push(vertex);
						cycle.push(nextVertex);
						if (findAlternatingLinkCycle(graph, edges[j], false, cycle, nextVertex, true)) {
							assert cycle.size() % 2 == 1;
							puzzle.removePossibleValue((Cell)vertex, possibleNumber);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean findAlternatingLinkCycle(SimpleGraph<CellOrGroup, SudokuEdge> graph, SudokuEdge finalEdge, boolean finalLinkMustBeStrong,
			ArrayDeque<CellOrGroup> cycle, CellOrGroup vertex, boolean nextLinkMustBeStrong) {
		ArrayList<CellOrGroup> possibleNextVerticies = new ArrayList<>();
		for (SudokuEdge nextEdge : graph.edgesOf(vertex)) {
			if (nextEdge.equals(finalEdge)) {
				if (finalLinkMustBeStrong) {
					if (nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) {
						return true;
					}
				} else {
					if (cycle.size() % 2 == 1) {
						return true;
					}
				}
			} else {
				CellOrGroup nextVertex = Common.getOtherVertex(graph, nextEdge, vertex);
				if (!containsCellOrGroup(cycle, nextVertex) &&
						((nextLinkMustBeStrong && nextEdge.getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK)) || !nextLinkMustBeStrong)) {
					possibleNextVerticies.add(nextVertex);
				}
			}
		}
		for (CellOrGroup nextVertex : possibleNextVerticies) {
			cycle.push(nextVertex);
			if (findAlternatingLinkCycle(graph, finalEdge, finalLinkMustBeStrong, cycle, nextVertex, !nextLinkMustBeStrong)) {
				return true;
			}
			cycle.pop();
		}
		return false;
	}
	
	private static boolean containsCellOrGroup(ArrayDeque<CellOrGroup> cycle, CellOrGroup vertex) {
		if (cycle.contains(vertex)) {
			return true;
		}
		for (CellOrGroup cycleMember : cycle) {
			if (cycleMember instanceof Cell && vertex instanceof Group) {
				if (((Group)vertex).cells.contains(cycleMember)) {
					return true;
				}
			} else if (cycleMember instanceof Group && vertex instanceof Cell) {
				if (((Group)cycleMember).cells.contains(cycleMember)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static abstract class Group implements CellOrGroup {
		protected final List<Cell> cells;
		
		public Group(ArrayList<Cell> cells) {
			assert cells.size() >= 2;
			assert cells.size() <= Puzzle.UNIT_SIZE_SQUARE_ROOT;
			int block = cells.get(0).getBlock();
			for (ListIterator<Cell> iter = cells.listIterator(1); iter.hasNext();) {
				Cell cell = iter.next();
				assert block == cell.getBlock();
			}
			this.cells = Collections.unmodifiableList(cells);
		}
		
		public boolean isInSameBlock(Cell cell) {
			return cell.getBlock() == cells.get(0).getBlock();
		}
	}
	
	private static class RowGroup extends Group {
		public RowGroup(ArrayList<Cell> cells) {
			super(cells);
			int row = cells.get(0).getRow();
			for (ListIterator<Cell> iter = cells.listIterator(1); iter.hasNext();) {
				Cell cell = iter.next();
				assert row == cell.getRow();
			}
		}
	
		public boolean isInSameRow(Cell cell) {
			return cell.getRow() == cells.get(0).getRow();
		}
	}
	
	private static class ColumnGroup extends Group {
		public ColumnGroup(ArrayList<Cell> cells) {
			super(cells);
			int column = cells.get(0).getColumn();
			for (ListIterator<Cell> iter = cells.listIterator(1); iter.hasNext();) {
				Cell cell = iter.next();
				assert column == cell.getColumn();
			}
		}
		
		public boolean isInSameColumn(Cell cell) {
			return cell.getColumn() == cells.get(0).getColumn();
		}
	}
}