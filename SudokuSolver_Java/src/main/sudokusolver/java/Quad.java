package sudokusolver.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public record Quad<A, B, C, D>(A first, B second, C third, D fourth) {
    public static <T> Gatherer<T, ?, Quad<T, T, T, T>> zipEveryQuad() {
        return Gatherer.<T, List<T>, Quad<T, T, T, T>>ofSequential(
                ArrayList::new,
                Gatherer.Integrator.ofGreedy((list, element, downstream) -> {
                    list.add(element);
                    if (list.size() >= 4) {
                        return downstream.push(new Quad<>(list.get(0), list.get(1), list.get(2), list.getLast()));
                    } else {
                        return true;
                    }
                }),
                (list, downstream) -> {
                    for (var i = 0; i < list.size() - 3; i++) {
                        for (var j = i + 1; j < list.size() - 2; j++) {
                            var k = i == 0 && j == 1 ? 3 : j + 1;
                            for (; k < list.size() - 1; k++) {
                                for (var l = k + 1; l < list.size(); l++) {
                                    if (downstream.isRejecting()) {
                                        return;
                                    }
                                    downstream.push(new Quad<>(list.get(i), list.get(j), list.get(k), list.get(l)));
                                }
                            }
                        }
                    }
                }
        );
    }
}