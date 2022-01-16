package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoardFactoryTest {
    @Test
    public void testParseOptionalBoardWrongLength() {
        Assertions.assertEquals(
                "board length is 0, must be 81.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseOptionalBoard("")
                ).getMessage()
        );
    }

    @Test
    public void testParseBoardWrongLength() {
        Assertions.assertEquals(
                "board length is 0, must be 81.",
                Assertions.assertThrows(IllegalArgumentException.class, () -> BoardFactory.parseBoard("")).getMessage()
        );
    }

    @Test
    public void testParseSimpleCellsWrongLength() {
        Assertions.assertEquals(
                "simpleBoard.length() is 0, must be 81.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseSimpleCells("")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesUnmatchedOpeningBrace() {
        Assertions.assertEquals(
                "Unmatched '{'.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("{")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesEmptyBraces() {
        Assertions.assertEquals(
                "Empty \"{}\".",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("{}")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesNestedBrace() {
        Assertions.assertEquals(
                "Nested '{'.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("{{}")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesInvalidCharacterInBraces() {
        Assertions.assertEquals(
                "Invalid character: 'a'.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("{a}")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesUnmatchedClosingBrace() {
        Assertions.assertEquals(
                "Unmatched '}'.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("}")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesInvalidCharacter() {
        Assertions.assertEquals(
                "Invalid character: 'a'.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("a")
                ).getMessage()
        );
    }

    @Test
    public void testParseCellsWithCandidatesWrongLength() {
        Assertions.assertEquals(
                "Found 0 cells, required 81.",
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> BoardFactory.parseCellsWithCandidates("")
                ).getMessage()
        );
    }
}