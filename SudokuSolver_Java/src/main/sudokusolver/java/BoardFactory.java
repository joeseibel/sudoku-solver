package sudokusolver.java;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class BoardFactory {
    public static Board<Optional<SudokuNumber>> parseOptionalBoard(String board) {
        if (board.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "board.length() is " + board.length() + ", must be " + Board.UNIT_SIZE_SQUARED + '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsNumbers = IntStream.range(0, Board.UNIT_SIZE)
                .mapToObj(row -> IntStream.range(0, Board.UNIT_SIZE)
                        .mapToObj(column -> {
                            var ch = board.charAt(row * Board.UNIT_SIZE + column);
                            return ch == '0' ? Optional.<SudokuNumber>empty() : Optional.of(SudokuNumber.valueOf(ch));
                        })
                        .toList())
                .toList();
        return new Board<>(boardAsNumbers);
    }

    public static Board<SudokuNumber> parseBoard(String board) {
        if (board.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "board.length() is " + board.length() + ", must be " + Board.UNIT_SIZE_SQUARED + '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsNumbers = IntStream.range(0, Board.UNIT_SIZE)
                .mapToObj(row -> IntStream.range(0, Board.UNIT_SIZE)
                        .mapToObj(column -> SudokuNumber.valueOf(board.charAt(row * Board.UNIT_SIZE + column)))
                        .toList())
                .toList();
        return new Board<>(boardAsNumbers);
    }

    public static Board<Cell> parseSimpleCells(String simpleBoard) {
        if (simpleBoard.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "simpleBoard.length() is " + simpleBoard.length() + ", must be " + Board.UNIT_SIZE_SQUARED +
                    '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsCells = IntStream.range(0, Board.UNIT_SIZE)
                .mapToObj(row -> IntStream.range(0, Board.UNIT_SIZE)
                        .<Cell>mapToObj(column -> {
                            var cell = simpleBoard.charAt(row * Board.UNIT_SIZE + column);
                            if (cell == '0') {
                                return new UnsolvedCell(row, column);
                            } else {
                                return new SolvedCell(row, column, SudokuNumber.valueOf(cell));
                            }
                        })
                        .toList())
                .toList();
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
        var cellBoard = IntStream.range(0, Board.UNIT_SIZE)
                .mapToObj(row -> IntStream.range(0, Board.UNIT_SIZE)
                        .mapToObj(column -> board.get(row, column)
                                .<Cell>map(cell -> new SolvedCell(row, column, cell))
                                .orElseGet(() -> new UnsolvedCell(row, column)))
                        .toList())
                .toList();
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
        var board = IntStream.range(0, Board.UNIT_SIZE)
                .mapToObj(row -> IntStream.range(0, Board.UNIT_SIZE)
                        .mapToObj(column -> cellBuilders.get(row * Board.UNIT_SIZE + column).apply(row, column))
                        .toList())
                .toList();
        return new Board<>(board);
    }
}