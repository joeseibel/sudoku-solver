package sudokusolver.java;

public sealed interface BoardModification permits RemoveCandidates, SetValue {
    int row();

    int column();
}