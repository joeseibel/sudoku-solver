package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SudokuNumberTest {
    @Test
    public void testUnexpectedChar() {
        Assertions.assertEquals(
                "ch is 'a', must be between '1' and '9'.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        /*
                         * Ideally, there would be no call to println here. Without it, IntelliJ reports a warning on
                         * the call to valueOf complaining that the result of the method call is ignored. While this
                         * warning is helpful in most cases, it doesn't make sense here inside assertThrows when I am
                         * specifically testing for the fact that the method doesn't return properly. Wrapping this in a
                         * call to println is enough to convince IntelliJ that the result of valueOf is used.
                         */
                        () -> System.out.println(SudokuNumber.valueOf('a'))
                ).getMessage()
        );
    }
}