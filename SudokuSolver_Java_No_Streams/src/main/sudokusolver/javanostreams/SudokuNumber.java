package sudokusolver.javanostreams;

/*
 * The only collection that should be used for SudokuNumber is EnumSet. Sometimes this will lead to more complicated
 * code to avoid using a List or Set.
 */
public enum SudokuNumber {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;

    @Override
    public String toString() {
        return Integer.toString(ordinal() + 1);
    }

    public static SudokuNumber valueOf(char ch) {
        return switch (ch) {
            case '1' -> ONE;
            case '2' -> TWO;
            case '3' -> THREE;
            case '4' -> FOUR;
            case '5' -> FIVE;
            case '6' -> SIX;
            case '7' -> SEVEN;
            case '8' -> EIGHT;
            case '9' -> NINE;
            default -> throw new IllegalArgumentException("ch is '" + ch + "', must be between '1' and '9'.");
        };
    }
}
