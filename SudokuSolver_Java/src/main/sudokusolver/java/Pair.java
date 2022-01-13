package sudokusolver.java;

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
                        .mapToObj(i -> IntStream.range(i + 1, list.size())
                                .mapToObj(j -> new Pair<>(list.get(i), list.get(j))))
                        .flatMap(Function.identity())
        );
    }
}