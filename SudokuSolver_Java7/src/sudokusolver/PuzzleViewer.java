package sudokusolver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jgrapht.graph.SimpleGraph;

public class PuzzleViewer extends JFrame {
	private static final int BOARD_PIXEL_SIZE = 500;
	
	private final Object lock;
	
	private final BoardComponent drawingComponent;
	
	public PuzzleViewer(String title, Puzzle puzzle, SimpleGraph<PossibleNumberInCell, SudokuEdge> graph, ArrayDeque<PossibleNumberInCell> cycle, Object lock) {
		super(title);
		this.lock = lock;
		drawingComponent = new BoardComponent(puzzle, graph, cycle);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		layoutComponents();
		addListeners();
		pack();
		setVisible(true);
	}
	
	public static void main(final String[] args) throws IOException {
		ArrayList<int[]> puzzlesFromFile = null;
		for (int i = 0; i < SudokuSolver.NO_DELIMITER_PUZZLES.length && puzzlesFromFile == null; i++) {
			if (SudokuSolver.NO_DELIMITER_PUZZLES[i].equals(args[0])) {
				puzzlesFromFile = SudokuSolver.loadNoDelimiterTextFile(new File(args[0]));
			}
		}
		for (int i = 0; i < SudokuSolver.GNOME_PUZZLES.length && puzzlesFromFile == null; i++) {
			if (SudokuSolver.GNOME_PUZZLES[i].equals(args[0])) {
				puzzlesFromFile = SudokuSolver.loadGnomeSudokuPuzzles(new File(args[0]));
			}
		}
		final Puzzle puzzle = new Puzzle(puzzlesFromFile.get(Integer.parseInt(args[1])));
		SudokuSolver.solve(puzzle);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new PuzzleViewer(args[0] + " #" + args[1], puzzle, null, null, null);
			}
		});
	}
	
	private void layoutComponents() {
		drawingComponent.setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
		add(drawingComponent);
	}
	
	private void addListeners() {
		if (lock != null) {
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					synchronized (lock) {
						lock.notify();
					}
				}
			});
		}
	}
	
	private static class BoardComponent extends JComponent {
		private static final int BOARD_PADDING = 5;
		private static final Color BLOCK_SHADE_GRAY = new Color(255, 255, 255, 0);
		private static final BasicStroke THICK_STROKE = new BasicStroke(3);
		private static final BasicStroke DASHED_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{9}, 0);
		private static final BasicStroke THIN_STROKE = new BasicStroke();
		private static final Color MAIN_VALUE_COLOR = new Color(187, 0, 0, 200);
		
		private final Puzzle puzzle;
		private final SimpleGraph<PossibleNumberInCell, SudokuEdge> graph;
		private final ArrayDeque<PossibleNumberInCell> cycle;
		
		public BoardComponent(Puzzle puzzle, SimpleGraph<PossibleNumberInCell, SudokuEdge> graph, ArrayDeque<PossibleNumberInCell> cycle) {
			this.puzzle = puzzle;
			this.graph = graph;
			this.cycle = cycle;
		}
		
		@Override
		public void paint(Graphics g) {
			//Shade blocks
			int boardSize = Math.min(getWidth(), getHeight());
			int startX = (getWidth() - boardSize) / 2 + BOARD_PADDING;
			int startY = (getHeight() - boardSize) / 2 + BOARD_PADDING;
			boardSize = boardSize - BOARD_PADDING * 2;
			double blockSize = boardSize / (double)Puzzle.UNIT_SIZE_SQUARE_ROOT;
			for (int block = 0; block < Puzzle.UNIT_SIZE; block++) {
				if (block % 2 == 0) {
					g.setColor(Color.WHITE);
				} else {
					g.setColor(BLOCK_SHADE_GRAY);
				}
				int blockRow = block / 3;
				int blockColumn = block % 3;
				g.fillRect(startX + (int)Math.round(blockSize * blockColumn),
						startY + (int)Math.round(blockSize * blockRow),
						(int)Math.round(blockSize),
						(int)Math.round(blockSize));
			}
			g.setColor(Color.BLACK);
			
			//Draw lines
			Graphics2D g2 = (Graphics2D)g;
			double cellSize = boardSize / (double)Puzzle.UNIT_SIZE;
			for (int i = 0; i <= Puzzle.UNIT_SIZE; i++) {
				if (i % Puzzle.UNIT_SIZE_SQUARE_ROOT == 0) {
					g2.setStroke(THICK_STROKE);
				} else {
					g2.setStroke(THIN_STROKE);
				}
				int cellOffset = (int)Math.round(cellSize * i);
				//Horizontal
				g.drawLine(startX,
						startY + cellOffset,
						startX + boardSize,
						startY + cellOffset);
				//Vertical
				g.drawLine(startX + cellOffset,
						startY,
						startX + cellOffset,
						startY + boardSize);
			}
			g2.setStroke(THIN_STROKE);

			double possibleNumberSquareSize = boardSize / (double)(Puzzle.UNIT_SIZE * Puzzle.UNIT_SIZE_SQUARE_ROOT);
			if (graph != null && cycle != null) {
				//Draw edge
				g.setColor(Color.BLUE);
				PossibleNumberInCell[] cycleArray = cycle.toArray(new PossibleNumberInCell[cycle.size()]);
				for (int i = 0; i < cycleArray.length; i++) {
					PossibleNumberInCell firstVertex = cycleArray[i];
					PossibleNumberInCell secondVertex = cycleArray[i < cycleArray.length - 1 ? i + 1 : 0];
					double firstVertexCellOffsetX = cellSize * firstVertex.getCell().getColumn();
					double firstVertexCellOffsetY = cellSize * firstVertex.getCell().getRow();
					double firstVertexPossibleNumberSquareOffsetX = possibleNumberSquareSize * (firstVertex.getPossibleNumber().ordinal() % Puzzle.UNIT_SIZE_SQUARE_ROOT);
					double firstVertexPossibleNumberSquareOffsetY = possibleNumberSquareSize * (firstVertex.getPossibleNumber().ordinal() / Puzzle.UNIT_SIZE_SQUARE_ROOT);
					
					double secondVertexCellOffsetX = cellSize * secondVertex.getCell().getColumn();
					double secondVertexCellOffsetY = cellSize * secondVertex.getCell().getRow();
					double secondVertexPossibleNumberSquareOffsetX = possibleNumberSquareSize * (secondVertex.getPossibleNumber().ordinal() % Puzzle.UNIT_SIZE_SQUARE_ROOT);
					double secondVertexPossibleNumberSquareOffsetY = possibleNumberSquareSize * (secondVertex.getPossibleNumber().ordinal() / Puzzle.UNIT_SIZE_SQUARE_ROOT);
					
					g2.setStroke(graph.getEdge(firstVertex, secondVertex).getLinkType().equals(SudokuEdge.LinkType.STRONG_LINK) ? THICK_STROKE : DASHED_STROKE);
					g.drawLine(startX + (int)Math.round(firstVertexCellOffsetX + firstVertexPossibleNumberSquareOffsetX + possibleNumberSquareSize / 2),
							startY + (int)Math.round(firstVertexCellOffsetY + firstVertexPossibleNumberSquareOffsetY + possibleNumberSquareSize / 2),
							startX + (int)Math.round(secondVertexCellOffsetX + secondVertexPossibleNumberSquareOffsetX + possibleNumberSquareSize / 2),
							startY + (int)Math.round(secondVertexCellOffsetY + secondVertexPossibleNumberSquareOffsetY + possibleNumberSquareSize / 2));
				}
				g2.setStroke(THIN_STROKE);
				
				//Draw vertex
				for (Iterator<PossibleNumberInCell> iter = cycle.iterator(); iter.hasNext();) {
					PossibleNumberInCell vertex = iter.next();
					double firstVertexCellOffsetX = cellSize * vertex.getCell().getColumn();
					double firstVertexCellOffsetY = cellSize * vertex.getCell().getRow();
					double firstVertexPossibleNumberSquareOffsetX = possibleNumberSquareSize * (vertex.getPossibleNumber().ordinal() % Puzzle.UNIT_SIZE_SQUARE_ROOT);
					double firstVertexPossibleNumberSquareOffsetY = possibleNumberSquareSize * (vertex.getPossibleNumber().ordinal() / Puzzle.UNIT_SIZE_SQUARE_ROOT);
					g.setColor(iter.hasNext() ? Color.CYAN : Color.PINK);
					g.fillOval(startX + (int)Math.round(firstVertexCellOffsetX + firstVertexPossibleNumberSquareOffsetX) + 3,
						startY + (int)Math.round(firstVertexCellOffsetY + firstVertexPossibleNumberSquareOffsetY) + 3,
						(int)Math.round(possibleNumberSquareSize) - 6,
						(int)Math.round(possibleNumberSquareSize) - 6);
				}
				g.setColor(Color.BLACK);
			}
			
			//Draw values
			Font mainValueFont = g.getFont().deriveFont(Font.BOLD, 20);
			Font possibleValueFont = g.getFont().deriveFont(10.0f);
			for (Cell cell : puzzle.getAllCells()) {
				double cellOffsetX = cellSize * cell.getColumn();
				double cellOffsetY = cellSize * cell.getRow();
				if (cell.getValue() != null) {
					g.setColor(MAIN_VALUE_COLOR);
					g.setFont(mainValueFont);
					Dimension stringSize = getStringSize(g2, cell.getValue().toString());
					g.drawString(cell.getValue().toString(),
							startX + (int)Math.round(cellOffsetX + (cellSize - stringSize.width) / 2),
							startY + (int)Math.round(cellOffsetY + (cellSize + stringSize.height) / 2));
				}
				for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
					double possibleNumberSquareOffsetX = possibleNumberSquareSize * (possibleNumber.ordinal() % Puzzle.UNIT_SIZE_SQUARE_ROOT);
					double possibleNumberSquareOffsetY = possibleNumberSquareSize * (possibleNumber.ordinal() / Puzzle.UNIT_SIZE_SQUARE_ROOT);
					g.setColor(Color.BLACK);
					g.setFont(possibleValueFont);
					Dimension stringSize = getStringSize(g2, possibleNumber.toString());
					g.drawString(possibleNumber.toString(),
							startX + (int)Math.round(cellOffsetX + possibleNumberSquareOffsetX + (possibleNumberSquareSize - stringSize.width) / 2),
							startY + (int)Math.round(cellOffsetY + possibleNumberSquareOffsetY + (possibleNumberSquareSize + stringSize.height) / 2));
				}
			}
			g.setColor(Color.BLACK);
		}
		
		private Dimension getStringSize(Graphics2D g, String str) {
			Rectangle bounds = g.getFont().createGlyphVector(g.getFontRenderContext(), str).getPixelBounds(null, 0, 0);
			return new Dimension(bounds.width, bounds.height);
		}
	}
}