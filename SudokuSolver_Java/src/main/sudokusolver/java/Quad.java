package sudokusolver.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public record Quad<A, B, C, D>(A first, B second, C third, D fourth) {
    public static <T> Gatherer<T, ?, Quad<T, T, T, T>> zipEveryQuad() {
        return Gatherer.<T, List<T>, Quad<T, T, T, T>>ofSequential(
                ArrayList::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    state.add(element);
                    if (state.size() >= 4) {
                        return downstream.push(new Quad<>(state.get(0), state.get(1), state.get(2), state.getLast()));
                    } else {
                        return true;
                    }
                }),
                (state, downstream) -> {
                    for (var i = 0; i < state.size() - 3; i++) {
                        for (var j = i + 1; j < state.size() - 2; j++) {
                            var k = i == 0 && j == 1 ? 3 : j + 1;
                            for (; k < state.size() - 1; k++) {
                                for (var l = k + 1; l < state.size(); l++) {
                                    if (downstream.isRejecting()) {
                                        return;
                                    }
                                    downstream.push(new Quad<>(state.get(i), state.get(j), state.get(k), state.get(l)));
                                }
                            }
                        }
                    }
                }
        );
    }
}