package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class BoardFactory {
    public static Board<Optional<SudokuNumber>> parseOptionalBoard(String board) {
        if (board.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "board.length() is " + board.length() + ", must be " + Board.UNIT_SIZE_SQUARED + '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsNumbers = new ArrayList<List<Optional<SudokuNumber>>>();
        for (var row = 0; row < Board.UNIT_SIZE; row++) {
            var rowAsNumbers = new ArrayList<Optional<SudokuNumber>>();
            for (var column = 0; column < Board.UNIT_SIZE; column++) {
                var ch = board.charAt(row * Board.UNIT_SIZE + column);
                rowAsNumbers.add(ch == '0' ? Optional.empty() : Optional.of(SudokuNumber.valueOf(ch)));
            }
            boardAsNumbers.add(rowAsNumbers);
        }
        return new Board<>(boardAsNumbers);
    }

    public static Board<SudokuNumber> parseBoard(String board) {
        if (board.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "board.length() is " + board.length() + ", must be " + Board.UNIT_SIZE_SQUARED + '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsNumbers = new ArrayList<List<SudokuNumber>>();
        for (var row = 0; row < Board.UNIT_SIZE; row++) {
            var rowAsNumbers = new ArrayList<SudokuNumber>();
            for (var column = 0; column < Board.UNIT_SIZE; column++) {
                rowAsNumbers.add(SudokuNumber.valueOf(board.charAt(row * Board.UNIT_SIZE + column)));
            }
            boardAsNumbers.add(rowAsNumbers);
        }
        return new Board<>(boardAsNumbers);
    }

    public static Board<Cell> parseSimpleCells(String simpleBoard) {
        if (simpleBoard.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "simpleBoard.length() is " + simpleBoard.length() + ", must be " + Board.UNIT_SIZE_SQUARED +
                    '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsCells = new ArrayList<List<Cell>>();
        for (var row = 0; row < Board.UNIT_SIZE; row++) {
            var rowAsCells = new ArrayList<Cell>();
            for (var column = 0; column < Board.UNIT_SIZE; column++) {
                var cell = simpleBoard.charAt(row * Board.UNIT_SIZE + column);
                if (cell == '0') {
                    rowAsCells.add(new UnsolvedCell(row, column));
                } else {
                    rowAsCells.add(new SolvedCell(row, column, SudokuNumber.valueOf(cell)));
                }
            }
            boardAsCells.add(rowAsCells);
        }
        return new Board<>(boardAsCells);
    }

    /*
     * For the implementation of this method, I decided to perform all the iteration here. This is unlike the Kotlin
     * version in which I had this method call Board.mapCellsToMutableBoardIndexed. Why did I not create a Java method
     * on Board called mapCellsIndexed? This is simply because Java does not provide a TriFunction class in its standard
     * libraries. It provides Function and BiFunction, but not TriFunction. I know that I could have written such a
     * class myself, but I figured that since it would likely only be used once, I then decided to simply implement the
     * iteration here.
     */
    public static Board<Cell> createCellBoard(Board<Optional<SudokuNumber>> board) {
        var cellBoard = new ArrayList<List<Cell>>();
        for (var row = 0; row < Board.UNIT_SIZE; row++) {
            var cellRow = new ArrayList<Cell>();
            for (var column = 0; column < Board.UNIT_SIZE; column++) {
                var cell = board.get(row, column);
                if (cell.isPresent()) {
                    cellRow.add(new SolvedCell(row, column, cell.get()));
                } else {
                    cellRow.add(new UnsolvedCell(row, column));
                }
            }
            cellBoard.add(cellRow);
        }
        return new Board<>(cellBoard);
    }

    public static Board<Cell> parseCellsWithCandidates(String withCandidates) {
        var cellBuilders = new ArrayList<BiFunction<Integer, Integer, Cell>>();
        var index = 0;
        while (index < withCandidates.length()) {
            var ch = withCandidates.charAt(index);
            if (ch == '{') {
                index++;
                var closingBrace = withCandidates.indexOf('}', index);
                if (closingBrace == -1) {
                    throw new IllegalArgumentException("Unmatched '{'.");
                }
                if (closingBrace == index) {
                    throw new IllegalArgumentException("Empty \"{}\".");
                }
                var charsInBraces = withCandidates.substring(index, closingBrace);
                if (charsInBraces.indexOf('{') != -1) {
                    throw new IllegalArgumentException("Nested '{'.");
                }
                var candidates = EnumSet.noneOf(SudokuNumber.class);
                for (int i = 0; i < charsInBraces.length(); i++) {
                    candidates.add(SudokuNumber.valueOf(charsInBraces.charAt(i)));
                }
                cellBuilders.add((row, column) -> new UnsolvedCell(row, column, candidates));
                index = closingBrace + 1;
            } else if (ch == '}') {
                throw new IllegalArgumentException("Unmatched '}'.");
            } else {
                var value = SudokuNumber.valueOf(ch);
                cellBuilders.add((row, column) -> new SolvedCell(row, column, value));
                index++;
            }
        }
        if (cellBuilders.size() != Board.UNIT_SIZE_SQUARED) {
            var message = "Found " + cellBuilders.size() + " cells, required " + Board.UNIT_SIZE_SQUARED + '.';
            throw new IllegalArgumentException(message);
        }
        var board = new ArrayList<List<Cell>>();
        for (var row = 0; row < Board.UNIT_SIZE; row++) {
            var rowList = new ArrayList<Cell>();
            for (var column = 0; column < Board.UNIT_SIZE; column++) {
                rowList.add(cellBuilders.get(row * Board.UNIT_SIZE + column).apply(row, column));
            }
            board.add(rowList);
        }
        return new Board<>(board);
    }
}