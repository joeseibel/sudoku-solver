package sudokusolver.javanostreams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class BoardTest {
    @Test
    public void testConstructorWrongSize() {
        Assertions.assertEquals(
                "rows size is 0, must be 9.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new Board<>(Collections.emptyList())
                ).getMessage()
        );
    }

    @Test
    public void testConstructorWrongInnerSize() {
        Assertions.assertEquals(
                "rows.get(0) size is 0, must be 9.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new Board<>(Collections.nCopies(Board.UNIT_SIZE, Collections.emptyList()))
                ).getMessage()
        );
    }

    @Test
    public void testGetBlockBlockIndexTooLow() {
        var board = new Board<>(Collections.nCopies(Board.UNIT_SIZE, Collections.nCopies(Board.UNIT_SIZE, null)));
        Assertions.assertEquals(
                "blockIndex is -1, must be between 0 and 8.",
                Assertions.assertThrows(IllegalArgumentException.class, () -> board.getBlock(-1)).getMessage()
        );
    }

    @Test
    public void testGetBlockIndexTooHigh() {
        var board = new Board<>(Collections.nCopies(Board.UNIT_SIZE, Collections.nCopies(Board.UNIT_SIZE, null)));
        Assertions.assertEquals(
                "blockIndex is 9, must be between 0 and 8.",
                Assertions.assertThrows(IllegalArgumentException.class, () -> board.getBlock(9)).getMessage()
        );
    }
}