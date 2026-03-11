package sudokusolver.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public record Pair<A, B>(A first, B second) {
    public static <T> Gatherer<T, ?, Pair<T, T>> zipEveryPair() {
        return Gatherer.<T, List<T>, Pair<T, T>>ofSequential(
                ArrayList::new,
                Gatherer.Integrator.ofGreedy((list, element, downstream) -> {
                    list.add(element);
                    if (list.size() >= 2) {
                        return downstream.push(new Pair<>(list.getFirst(), list.getLast()));
                    } else {
                        return true;
                    }
                }),
                (list, downstream) -> {
                    for (var i = 1; i < list.size() - 1; i++) {
                        for (var j = i + 1; j < list.size(); j++) {
                            if (downstream.isRejecting()) {
                                return;
                            }
                            downstream.push(new Pair<>(list.get(i), list.get(j)));
                        }
                    }
                }
        );
    }
}