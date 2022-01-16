package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

class SetValueTest {
    @Test
    public void testNotACandidateForCell() {
        Assertions.assertEquals(
                "1 is not a candidate for [0, 0].",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> new SetValue(
                                new UnsolvedCell(0, 0, EnumSet.of(SudokuNumber.TWO)),
                                SudokuNumber.ONE
                        )
                ).getMessage()
        );
    }
}