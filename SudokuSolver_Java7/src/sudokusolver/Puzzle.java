package sudokusolver;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Puzzle {
	private static final int UNIT_SIZE = 9;
	
	private Cell[][] board = new Cell[UNIT_SIZE][UNIT_SIZE];
	
	public Puzzle(int[] initialValues) {
		if (initialValues.length != UNIT_SIZE * UNIT_SIZE) {
			throw new IllegalArgumentException("initialValues must have a length of " + UNIT_SIZE * UNIT_SIZE);
		}
		for (int i = 0; i < initialValues.length; i++) {
			board[i / 9][i % 9] = new Cell(i / 9, i % 9, initialValues[i] == 0 ? null : SudokuNumber.values()[initialValues[i] - 1]);
		}
	}
	
	public Iterable<Cell> getAllEmptyCells() {
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new AllCellIterator(AllCellIteratorCellCondition.EMPTY);
			}
		};
	}
	
	public Iterable<Cell> getEmptyCellsWithOnePossibleValue() {
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new AllCellIterator(AllCellIteratorCellCondition.ONE_POSSIBLE);
			}
		};
	}
	
	public Iterable<Iterable<Cell>> getAllRows() {
		return new Iterable<Iterable<Cell>>() {
			@Override
			public Iterator<Iterable<Cell>> iterator() {
				return new Iterator<Iterable<Cell>>() {
					private int currentRowIndex = -1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Iterable<Cell> next() {
						if (currentRowIndex + 1 < UNIT_SIZE) {
							currentRowIndex++;
							return getRowCells(currentRowIndex);
						} else {
							throw new NoSuchElementException();
						}
					}
					
					@Override
					public boolean hasNext() {
						return currentRowIndex + 1 < UNIT_SIZE;
					}
				};
			}
		};
	}
	
	public Iterable<Iterable<Cell>> getAllColumns() {
		return new Iterable<Iterable<Cell>>() {
			@Override
			public Iterator<Iterable<Cell>> iterator() {
				return new Iterator<Iterable<Cell>>() {
					private int currentColumnIndex = -1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Iterable<Cell> next() {
						if (currentColumnIndex + 1 < UNIT_SIZE) {
							currentColumnIndex++;
							return getColumnCells(currentColumnIndex);
						} else {
							throw new NoSuchElementException();
						}
					}
					
					@Override
					public boolean hasNext() {
						return currentColumnIndex + 1 < UNIT_SIZE;
					}
				};
			}
		};
	}
	
	public Iterable<Iterable<Cell>> getAllBlocks() {
		return new Iterable<Iterable<Cell>>() {
			@Override
			public Iterator<Iterable<Cell>> iterator() {
				return new Iterator<Iterable<Cell>>() {
					private int currentBlockIndex = -1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Iterable<Cell> next() {
						if (currentBlockIndex + 1 < UNIT_SIZE) {
							currentBlockIndex++;
							return getBlockCells(currentBlockIndex);
						} else {
							throw new NoSuchElementException();
						}
					}
					
					@Override
					public boolean hasNext() {
						return currentBlockIndex + 1 < UNIT_SIZE;
					}
				};
			}
		};
	}
	
	public Iterable<Cell> getRowCells(Cell indexCell) {
		return getRowCells(indexCell.getRow());
	}
	
	public Iterable<Cell> getColumnCells(Cell indexCell) {
		return getColumnCells(indexCell.getColumn());
	}
	
	public Iterable<Cell> getBlockCells(Cell indexCell) {
		return getBlockCells(indexCell.getBlock());
	}
	
	public ArrayList<ArrayList<Cell>> getRectangles() {
		ArrayList<ArrayList<Cell>> rectangles = new ArrayList<ArrayList<Cell>>();
		for (int topRow = 0; topRow < UNIT_SIZE - 1; topRow++) {
			for (int bottomRow = topRow + 1; bottomRow < UNIT_SIZE; bottomRow++) {
				for (int leftColumn = 0; leftColumn < UNIT_SIZE - 1; leftColumn++) {
					Cell topLeft = board[topRow][leftColumn];
					Cell bottomLeft = board[bottomRow][leftColumn];
					if (topLeft.getValue() == null && bottomLeft.getValue() == null) {
						for (int rightColumn = leftColumn + 1; rightColumn < UNIT_SIZE; rightColumn++) {
							Cell topRight = board[topRow][rightColumn];
							Cell bottomRight = board[bottomRow][rightColumn];
							int topRowBlock = topRow / 3;
							int bottomRowBlock = bottomRow / 3;
							int leftColumnBlock = leftColumn / 3;
							int rightColumnBlock = rightColumn / 3;
							if (topRight.getValue() == null && bottomRight.getValue() == null &&
									((topRowBlock == bottomRowBlock && leftColumnBlock != rightColumnBlock) ||
											(topRowBlock != bottomRowBlock && leftColumnBlock == rightColumnBlock))) {
								ArrayList<Cell> rectangle = new ArrayList<Cell>();
								rectangle.add(topLeft);
								rectangle.add(topRight);
								rectangle.add(bottomLeft);
								rectangle.add(bottomRight);
							}
						}
					}
				}
			}
		}
		return rectangles;
	}
	
	public void setValueAndUpdatePossibleValues(Cell cell, SudokuNumber newValue) {
		for (Cell rowCell : getRowCells(cell)) {
			assert !newValue.equals(rowCell.getValue()) :
				"Value already exists in row. value: " + newValue + ", row: " + cell.getRow() + ", column: " + cell.getColumn();
		}
		for (Cell columnCell : getColumnCells(cell)) {
			assert !newValue.equals(columnCell.getValue()) :
				"Value already exists in column. value: " + newValue + ", row: " + cell.getRow() + ", column: " + cell.getColumn();
		}
		for (Cell blockCell : getBlockCells(cell)) {
			assert !newValue.equals(blockCell.getValue()) :
				"Value already exists in block. value: " + newValue + ", row: " + cell.getRow() + ", column: " + cell.getColumn();
		}
		cell.setValue(newValue);
		assert cell.getPossibleValues().contains(newValue);
		cell.getPossibleValues().clear();
		for (Cell rowCell : getRowCells(cell)) {
			rowCell.getPossibleValues().remove(newValue);
			assert rowCell.getValue() != null || !rowCell.getPossibleValues().isEmpty();
		}
		for (Cell columnCell : getColumnCells(cell)) {
			columnCell.getPossibleValues().remove(newValue);
			assert columnCell.getValue() != null || !columnCell.getPossibleValues().isEmpty();
		}
		for (Cell blockCell : getBlockCells(cell)) {
			blockCell.getPossibleValues().remove(newValue);
			assert blockCell.getValue() != null || !blockCell.getPossibleValues().isEmpty();
		}
	}
	
	public int getEmptyCellCount() {
		int emptyCellCount = 0;
		for (Iterator<Cell> iter = getAllEmptyCells().iterator(); iter.hasNext(); iter.next()) {
			emptyCellCount++;
		}
		return emptyCellCount;
	}
	
	public String getPossibleString() {
		StringBuilder result = new StringBuilder();
		for (Iterator<Iterable<Cell>> rowIter = getAllRows().iterator(); rowIter.hasNext();) {
			for (Iterator<Cell> cellIter = rowIter.next().iterator(); cellIter.hasNext();) {
				result.append(cellIter.next());
				if (rowIter.hasNext() || cellIter.hasNext()) {
					result.append('\n');
				}
			}
			if (rowIter.hasNext()) {
				result.append('\n');
			}
		}
		return result.toString();
	}
	
	public void validateCompletePuzzle() {
		assert getEmptyCellCount() == 0 : "Empty cell count is " + getEmptyCellCount();
		for (Iterable<Cell> row : getAllRows()) {
			EnumSet<SudokuNumber> valuesEncountered = EnumSet.noneOf(SudokuNumber.class);
			for (Cell cell : row) {
				assert !valuesEncountered.contains(cell.getValue()) : cell.getValue() + " appears multiple times in row " + cell.getRow();
				valuesEncountered.add(cell.getValue());
			}
		}
		for (Iterable<Cell> column : getAllColumns()) {
			EnumSet<SudokuNumber> valuesEncountered = EnumSet.noneOf(SudokuNumber.class);
			for (Cell cell : column) {
				assert !valuesEncountered.contains(cell.getValue()) : cell.getValue() + " appears multiple times in column " + cell.getColumn();
				valuesEncountered.add(cell.getValue());
			}
		}
		for (Iterable<Cell> block : getAllBlocks()) {
			EnumSet<SudokuNumber> valuesEncountered = EnumSet.noneOf(SudokuNumber.class);
			for (Cell cell : block) {
				assert !valuesEncountered.contains(cell.getValue()) : cell.getValue() + " appears multiple times in block " + cell.getBlock();
				valuesEncountered.add(cell.getValue());
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Iterator<Iterable<Cell>> rowIter = getAllRows().iterator(); rowIter.hasNext();) {
			for (Iterator<Cell> cellIter = rowIter.next().iterator(); cellIter.hasNext();) {
				SudokuNumber value = cellIter.next().getValue();
				if (value == null) {
					result.append('0');
				} else {
					result.append(value);
				}
				if (cellIter.hasNext()) {
					result.append(' ');
				}
			}
			if (rowIter.hasNext()) {
				result.append('\n');
			}
		}
		return result.toString();
	}
	
	private static enum AllCellIteratorCellCondition {EMPTY, ONE_POSSIBLE, ALL}
	
	private class AllCellIterator implements Iterator<Cell> {
		private final AllCellIteratorCellCondition cellCondition;
		
		private int currentRowIndex = 0;
		private int currentColumnIndex = -1;
		
		public AllCellIterator(AllCellIteratorCellCondition cellCondition) {
			this.cellCondition = cellCondition;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Cell next() {
			for (int rowIndex = currentRowIndex; rowIndex < UNIT_SIZE; rowIndex++) {
				for (int columnIndex = rowIndex == currentRowIndex ? currentColumnIndex + 1 : 0; columnIndex < UNIT_SIZE; columnIndex++) {
					boolean validCell;
					switch (cellCondition) {
						case EMPTY:
							validCell = board[rowIndex][columnIndex].getValue() == null;
							break;
						case ONE_POSSIBLE:
							validCell = board[rowIndex][columnIndex].getPossibleValues().size() == 1;
							break;
						case ALL:
							validCell = true;
							break;
						default:
							throw new AssertionError();
					}
					if (validCell) {
						currentRowIndex = rowIndex;
						currentColumnIndex = columnIndex;
						return board[rowIndex][columnIndex];
					}
				}
			}
			throw new NoSuchElementException();
		}
		
		@Override
		public boolean hasNext() {
			for (int rowIndex = currentRowIndex; rowIndex < UNIT_SIZE; rowIndex++) {
				for (int columnIndex = rowIndex == currentRowIndex ? currentColumnIndex + 1 :0; columnIndex < UNIT_SIZE; columnIndex++) {
					switch (cellCondition) {
						case EMPTY:
							if (board[rowIndex][columnIndex].getValue() == null) {
								return true;
							}
							break;
						case ONE_POSSIBLE:
							if (board[rowIndex][columnIndex].getPossibleValues().size() == 1) {
								return true;
							}
							break;
						case ALL:
							return true;
						default:
							assert false : cellCondition;
					}
				}
			}
			return false;
		}
	}
	
	private Iterable<Cell> getAllCells() {
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new AllCellIterator(AllCellIteratorCellCondition.ALL);
			}
		};
	}
	
	private Iterable<Cell> getRowCells(final int rowIndex) {
		if (rowIndex < 0 || rowIndex >= UNIT_SIZE) {
			throw new IllegalArgumentException();
		}
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new Iterator<Cell>() {
					private int currentColumnIndex = -1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Cell next() {
						if (currentColumnIndex + 1 < UNIT_SIZE) {
							currentColumnIndex++;
							return board[rowIndex][currentColumnIndex];
						} else {
							throw new NoSuchElementException();
						}
					}
					
					@Override
					public boolean hasNext() {
						return currentColumnIndex + 1 < UNIT_SIZE;
					}
				};
			}
		};
	}
	
	private Iterable<Cell> getColumnCells(final int columnIndex) {
		if (columnIndex < 0 || columnIndex >= UNIT_SIZE) {
			throw new IllegalArgumentException();
		}
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new Iterator<Cell>() {
					private int currentRowIndex = -1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Cell next() {
						if (currentRowIndex + 1 < UNIT_SIZE) {
							currentRowIndex++;
							return board[currentRowIndex][columnIndex];
						}
						throw new NoSuchElementException();
					}
					
					@Override
					public boolean hasNext() {
						return currentRowIndex + 1 < UNIT_SIZE;
					}
				};
			}
		};
	}
	
	private Iterable<Cell> getBlockCells(final int blockIndex) {
		if (blockIndex < 0 || blockIndex >= UNIT_SIZE) {
			throw new IllegalArgumentException();
		}
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new Iterator<Cell>() {
					private int topLeftRow = blockIndex - blockIndex % 3;
					private int topLeftColumn = 3 * (blockIndex % 3);
					private int currentRowIndex = topLeftRow;
					private int currentColumnIndex = topLeftColumn - 1;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Cell next() {
						if (hasNext()) {
							currentColumnIndex++;
							if (currentColumnIndex >= topLeftColumn + 3) {
								currentColumnIndex = topLeftColumn;
								currentRowIndex++;
							}
							return board[currentRowIndex][currentColumnIndex];
						}
						throw new NoSuchElementException();
					}
					
					@Override
					public boolean hasNext() {
						if (currentColumnIndex + 1 < topLeftColumn + 3) {
							return true;
						} else
							return currentRowIndex + 1 < topLeftRow + 3;
					}
				};
			}
		};
	}
}