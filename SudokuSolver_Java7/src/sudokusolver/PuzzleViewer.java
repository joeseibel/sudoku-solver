package sudokusolver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class PuzzleViewer extends JFrame {
	private static final int BOARD_PIXEL_SIZE = 500;
	
	private final BoardComponent drawingComponent;
	
	public PuzzleViewer(String name, int puzzleIndex, Puzzle puzzle) {
		super(name + " #" + puzzleIndex);
		drawingComponent = new BoardComponent(puzzle);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		layoutComponents();
		pack();
		setVisible(true);
	}

	public static void main(String[] args) throws FileNotFoundException {
		final File puzzleFile = new File(args[0]);
		Scanner scanner = new Scanner(puzzleFile);
		final int puzzleIndex = Integer.parseInt(args[1]);
		for (int currentPuzzleIndex = 0; currentPuzzleIndex < puzzleIndex; currentPuzzleIndex++) {
			scanner.nextLine();
		}
		int[] initialValues = new int[Puzzle.UNIT_SIZE * Puzzle.UNIT_SIZE];
		for (int i = 0; i < initialValues.length; i++) {
			initialValues[i] = scanner.nextInt();
		}
		scanner.close();
		final Puzzle puzzle = new Puzzle(initialValues);
		SudokuSolver.solve(puzzle);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new PuzzleViewer(puzzleFile.getName(), puzzleIndex, puzzle);
			}
		});
	}
	
	private void layoutComponents() {
		drawingComponent.setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
		add(drawingComponent);
	}
	
	private static class BoardComponent extends JComponent {
		private static final int BOARD_PADDING = 5;
		private static final Color BLOCK_SHADE_GRAY = new Color(255, 255, 255, 0);
		private static final BasicStroke THICK_STROKE = new BasicStroke(3);
		private static final BasicStroke THIN_STROKE = new BasicStroke();
		private static final Color MAIN_VALUE_COLOR = new Color(187, 0, 0, 200);
		
		private final Puzzle puzzle;
		
		public BoardComponent(Puzzle puzzle) {
			this.puzzle = puzzle;
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
				//Horizontal
				g.drawLine(startX,
						startY + (int)Math.round(cellSize * i),
						startX + boardSize,
						startY + (int)Math.round(cellSize * i));
				//Vertical
				g.drawLine(startX + (int)Math.round(cellSize * i),
						startY,
						startX + (int)Math.round(cellSize * i),
						startY + boardSize);
			}
			
			//Draw values
			Font mainValueFont = g.getFont().deriveFont(Font.BOLD, 20);
			Font possibleValueFont = g.getFont().deriveFont(10.0f);
			double possibleNumberSquareSize = boardSize / (double)(Puzzle.UNIT_SIZE * Puzzle.UNIT_SIZE_SQUARE_ROOT);
			for (Cell cell : puzzle.getAllCells()) {
				if (cell.getValue() != null) {
					g.setColor(MAIN_VALUE_COLOR);
					g.setFont(mainValueFont);
					Dimension stringSize = getStringSize(g2, cell.getValue().toString());
					g.drawString(cell.getValue().toString(),
							startX + (int)Math.round(cellSize * cell.getColumn() + (cellSize - stringSize.width) / 2),
							startY + (int)Math.round(cellSize * cell.getRow() + (cellSize + stringSize.height) / 2));
				}
				for (SudokuNumber possibleNumber : cell.getPossibleValues()) {
					g.setColor(Color.BLACK);
					g.setFont(possibleValueFont);
					Dimension stringSize = getStringSize(g2, possibleNumber.toString());
					g.drawString(possibleNumber.toString(),
							startX + (int)Math.round(cellSize * cell.getColumn() +
									possibleNumberSquareSize * (possibleNumber.ordinal() % Puzzle.UNIT_SIZE_SQUARE_ROOT) +
									(possibleNumberSquareSize - stringSize.width) / 2),
							startY + (int)Math.round(cellSize * cell.getRow() +
									possibleNumberSquareSize * (possibleNumber.ordinal() / Puzzle.UNIT_SIZE_SQUARE_ROOT) +
									(possibleNumberSquareSize + stringSize.height) / 2));
				}
			}
		}
		
		private Dimension getStringSize(Graphics2D g, String str) {
			Rectangle bounds = g.getFont().createGlyphVector(g.getFontRenderContext(), str).getPixelBounds(null, 0, 0);
			return new Dimension(bounds.width, bounds.height);
		}
	}
}