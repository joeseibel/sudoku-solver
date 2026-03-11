package sudokusolver.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public record Triple<A, B, C>(A first, B second, C third) {
    public static <T> Gatherer<T, ?, Triple<T, T, T>> zipEveryTriple() {
        return Gatherer.<T, List<T>, Triple<T, T, T>>ofSequential(
                ArrayList::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    state.add(element);
                    if (state.size() >= 3) {
                        return downstream.push(new Triple<>(state.get(0), state.get(1), state.getLast()));
                    } else {
                        return true;
                    }
                }),
                (state, downstream) -> {
                    for (var i = 0; i < state.size() - 2; i++) {
                        var j = i == 0 ? 2 : i + 1;
                        for (; j < state.size() - 1; j++) {
                            for (var k = j + 1; k < state.size(); k++) {
                                if (downstream.isRejecting()) {
                                    return;
                                }
                                downstream.push(new Triple<>(state.get(i), state.get(j), state.get(k)));
                            }
                        }
                    }
                }
        );
    }
}