package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.List;

public record Triple<A, B, C>(A first, B second, C third) {
    /*
     * Similar to Pair.zipEveryPair, this method exists to deal with warnings about methods being too complex to analyze
     * by data flow algorithm.
     */
    public static <T> List<Triple<T, T, T>> zipEveryTriple(List<T> list) {
        var triples = new ArrayList<Triple<T, T, T>>();
        for (var i = 0; i < list.size() - 2; i++) {
            for (var j = i + 1; j < list.size() - 1; j++) {
                for (var k = j + 1; k < list.size(); k++) {
                    triples.add(new Triple<>(list.get(i), list.get(j), list.get(k)));
                }
            }
        }
        return triples;
    }
}