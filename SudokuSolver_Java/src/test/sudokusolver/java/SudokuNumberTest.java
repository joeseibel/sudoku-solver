package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SudokuNumberTest {
    @Test
    public void testUnexpectedChar() {
        Assertions.assertEquals(
                "ch is 'a', must be between '1' and '9'.",
                Assertions.assertThrows(IllegalArgumentException.class, () -> SudokuNumber.valueOf('a'))
                        .getMessage()
        );
    }
}