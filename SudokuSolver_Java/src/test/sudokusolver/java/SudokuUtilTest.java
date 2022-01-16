package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SudokuUtilTest {
    @Test
    public void testRowTooLow() {
        Assertions.assertEquals(
                "row is -1, must be between 0 and 8.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new RemoveCandidates(-1, 0, 1)
                ).getMessage()
        );
    }

    @Test
    public void testRowTooHigh() {
        Assertions.assertEquals(
                "row is 9, must be between 0 and 8.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new SetValue(9, 0, 1)
                ).getMessage()
        );
    }

    @Test
    public void testColumnTooLow() {
        Assertions.assertEquals(
                "column is -1, must be between 0 and 8.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new SolvedCell(0, -1, SudokuNumber.ONE)
                ).getMessage()
        );
    }

    @Test
    public void testColumnTooHigh() {
        Assertions.assertEquals(
                "column is 9, must be between 0 and 8.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new UnsolvedCell(0, 9)
                ).getMessage()
        );
    }
}