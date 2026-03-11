package sudokusolver.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public record Triple<A, B, C>(A first, B second, C third) {
    public static <T> Gatherer<T, ?, Triple<T, T, T>> zipEveryTriple() {
        return Gatherer.<T, List<T>, Triple<T, T, T>>ofSequential(
                ArrayList::new,
                Gatherer.Integrator.ofGreedy((list, element, downstream) -> {
                    list.add(element);
                    if (list.size() >= 3) {
                        return downstream.push(new Triple<>(list.get(0), list.get(1), list.getLast()));
                    } else {
                        return true;
                    }
                }),
                (list, downstream) -> {
                    for (var i = 0; i < list.size() - 2; i++) {
                        var j = i == 0 ? 2 : i + 1;
                        for (; j < list.size() - 1; j++) {
                            for (var k = j + 1; k < list.size(); k++) {
                                if (downstream.isRejecting()) {
                                    return;
                                }
                                downstream.push(new Triple<>(list.get(i), list.get(j), list.get(k)));
                            }
                        }
                    }
                }
        );
    }
}