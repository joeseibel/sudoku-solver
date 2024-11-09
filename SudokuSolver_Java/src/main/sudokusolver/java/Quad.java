package sudokusolver.java;

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Quad<A, B, C, D>(A first, B second, C third, D fourth) {
    public static <T> Collector<T, ?, Stream<Quad<T, T, T, T>>> zipEveryQuad() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> IntStream.range(0, list.size() - 3)
                        .mapToObj(first -> IntStream.range(first + 1, list.size() - 2)
                                .mapToObj(second -> IntStream.range(second + 1, list.size() - 1)
                                        .mapToObj(third -> IntStream.range(third + 1, list.size())
                                                .mapToObj(fourth -> new Quad<>(
                                                        list.get(first),
                                                        list.get(second),
                                                        list.get(third),
                                                        list.get(fourth)
                                                )))
                                        .flatMap(Function.identity()))
                                .flatMap(Function.identity()))
                        .flatMap(Function.identity())
        );
    }
}