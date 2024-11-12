package sudokusolver.javanostreams;

public sealed interface BoardModification extends Comparable<BoardModification> permits RemoveCandidates, SetValue {
    int row();

    int column();

    @Override
    default int compareTo(BoardModification o) {
        var rowCompare = Integer.compare(row(), o.row());
        return rowCompare != 0 ? rowCompare : Integer.compare(column(), o.column());
    }
}