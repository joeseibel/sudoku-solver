package sudokusolver.java;

import java.util.Optional;
import java.util.stream.IntStream;

public class BoardFactory {
    public static Board<Optional<SudokuNumber>> parseOptionalBoard(String board) {
        if (board.length() != Board.UNIT_SIZE_SQUARED) {
            var message = "board length is " + board.length() + ", must be " + Board.UNIT_SIZE_SQUARED + '.';
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
            var message = "board length is " + board.length() + ", must be " + Board.UNIT_SIZE_SQUARED + '.';
            throw new IllegalArgumentException(message);
        }
        var boardAsNumbers = IntStream.range(0, Board.UNIT_SIZE)
                .mapToObj(row -> IntStream.range(0, Board.UNIT_SIZE)
                        .mapToObj(column -> SudokuNumber.valueOf(board.charAt(row * Board.UNIT_SIZE + column)))
                        .toList())
                .toList();
        return new Board<>(boardAsNumbers);
    }
}
