package sudokusolver.javanostreams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Pair<A, B>(A first, B second) {
    public static <T> Collector<T, ?, Stream<Pair<T, T>>> zipEveryPair() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> IntStream.range(0, list.size() - 1)
                        .mapToObj(first -> IntStream.range(first + 1, list.size())
                                .mapToObj(second -> new Pair<>(list.get(first), list.get(second))))
                        .flatMap(Function.identity())
        );
    }

    /*
     * When I started writing the No Streams version of the Java implementation, I wanted to inline the pair, triple,
     * and quad operations in the form of nested index loops and therefore remove the Pair, Triple, and Quad classes
     * entirely. However, when I tried this approach with Naked Pairs, the first solution to utilize pairs, I
     * encountered an unexpected warning: "Method 'nakedPairs' is too complex to analyze by data flow algorithm".
     * IntelliJ seems to be unable to cope with the following construct:
     *
     * for (var i = 0; i < unit.size() - 1; i++) {
     *     if (unit.get(i) instanceof UnsolvedCell a && a.candidates().size() == 2) {
     *         for (var j = i + 1; j < unit.size(); j++) {
     *             if (unit.get(j) instanceof UnsolvedCell b && a.candidates().equals(b.candidates())) {
     *                 ...
     *             }
     *         }
     *     }
     * }
     *
     * Unfortunately, IntelliJ does not offer a Quick Fix to suppress this warning. I was also unable to find the name
     * of this warning that would be recognized by the @SuppressWarnings annotation. I could have chosen to use
     * @SuppressWarnings("all") on the method nakedPairs, but I really didn't want to do that.
     *
     * Extracting the two loops to their own method satisfied the IntelliJ warning.
     */
    public static <T> List<Pair<T, T>> zipEveryPair(List<T> list) {
        var pairs = new ArrayList<Pair<T, T>>();
        for (var i = 0; i < list.size() - 1; i++) {
            for (var j = i + 1; j < list.size(); j++) {
                pairs.add(new Pair<>(list.get(i), list.get(j)));
            }
        }
        return pairs;
    }
}