package sudokusolver.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public record Pair<A, B>(A first, B second) {
    public static <T> Gatherer<T, ?, Pair<T, T>> zipEveryPair() {
        return Gatherer.<T, List<T>, Pair<T, T>>ofSequential(
                ArrayList::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    state.add(element);
                    if (state.size() >= 2) {
                        return downstream.push(new Pair<>(state.getFirst(), state.getLast()));
                    } else {
                        return true;
                    }
                }),
                (state, downstream) -> {
                    for (var i = 1; i < state.size() - 1; i++) {
                        for (var j = i + 1; j < state.size(); j++) {
                            if (downstream.isRejecting()) {
                                return;
                            }
                            downstream.push(new Pair<>(state.get(i), state.get(j)));
                        }
                    }
                }
        );
    }
}