package sudokusolver.javanostreams;

public class UnableToSolveException extends Exception {
    public UnableToSolveException(Board<Cell> board) {
        var message = "Unable to solve:\n" +
                board + '\n' +
                '\n' +
                "Simple String: " + Board.toSimpleString(board) + '\n' +
                '\n' +
                "With Candidates:\n" +
                Board.toStringWithCandidates(board);
        super(message);
    }
}