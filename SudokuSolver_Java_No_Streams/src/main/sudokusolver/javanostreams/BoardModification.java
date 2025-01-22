package sudokusolver.javanostreams;

public sealed interface BoardModification permits RemoveCandidates, SetValue {
    int row();

    int column();
}