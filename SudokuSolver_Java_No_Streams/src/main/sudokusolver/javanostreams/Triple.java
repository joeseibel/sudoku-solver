package sudokusolver.javanostreams;

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Triple<A, B, C>(A first, B second, C third) {
    public static <T> Collector<T, ?, Stream<Triple<T, T, T>>> zipEveryTriple() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> IntStream.range(0, list.size() - 2)
                        .mapToObj(first -> IntStream.range(first + 1, list.size() - 1)
                                .mapToObj(second -> IntStream.range(second + 1, list.size())
                                        .mapToObj(third -> new Triple<>(
                                                list.get(first),
                                                list.get(second),
                                                list.get(third)
                                        )))
                                .flatMap(Function.identity()))
                        .flatMap(Function.identity())
        );
    }
}
