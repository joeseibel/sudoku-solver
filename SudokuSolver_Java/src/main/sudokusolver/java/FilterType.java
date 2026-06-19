package sudokusolver.java;

import java.util.stream.Gatherer;

public class FilterType {
    public static <T, R> Gatherer<T, ?, R> of(Class<R> type) {
        return Gatherer.of(Gatherer.Integrator.ofGreedy((_, object, downstream) -> {
            if (type.isInstance(object)) {
                return downstream.push(type.cast(object));
            } else {
                return true;
            }
        }));
    }
}