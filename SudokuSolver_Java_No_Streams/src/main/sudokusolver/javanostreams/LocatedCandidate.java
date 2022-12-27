package sudokusolver.javanostreams;

public record LocatedCandidate(UnsolvedCell cell, SudokuNumber candidate) {
}