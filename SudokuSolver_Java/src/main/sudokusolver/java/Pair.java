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
                        .mapToObj(first -> IntStream.range(first + 1, list.size())
                                .mapToObj(second -> new Pair<>(list.get(first), list.get(second))))
                        .flatMap(Function.identity())
        );
    }
}