package sudokusolver.javanostreams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

class UnsolvedCellTest {
    @Test
    public void testCandidatesAreEmpty() {
        Assertions.assertEquals(
                "candidates must not be empty.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new UnsolvedCell(0, 0, EnumSet.noneOf(SudokuNumber.class))
                ).getMessage()
        );
    }
}