package sudokusolver.javanostreams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

class RemoveCandidatesTest {
    @Test
    public void testCandidatesAreEmpty() {
        Assertions.assertEquals(
                "candidates must not be empty.",
                Assertions.assertThrows(
                        IllegalArgumentException.class, () ->
                                new RemoveCandidates(0, 0)
                ).getMessage()
        );
    }

    @Test
    public void testNotACandidateForCell() {
        Assertions.assertEquals(
                "1 is not a candidate for [0, 0].",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new RemoveCandidates(
                                new UnsolvedCell(0, 0, EnumSet.of(SudokuNumber.TWO)),
                                EnumSet.of(SudokuNumber.ONE)
                        )
                ).getMessage()
        );
    }
}