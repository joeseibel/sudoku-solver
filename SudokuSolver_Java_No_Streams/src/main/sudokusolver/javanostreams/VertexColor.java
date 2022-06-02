package sudokusolver.javanostreams;

import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum VertexColor {
    COLOR_ONE {
        @Override
        public VertexColor getOpposite() {
            return COLOR_TWO;
        }
    },

    COLOR_TWO {
        @Override
        public VertexColor getOpposite() {
            return COLOR_ONE;
        }
    };

    public abstract VertexColor getOpposite();

    public static <V, E> Map<V, VertexColor> colorToMap(Graph<V, E> graph) {
        var result = new HashMap<V, VertexColor>();
        var breadthFirst = new BreadthFirstIterator<>(graph);
        breadthFirst.forEachRemaining(vertex -> {
            var color = breadthFirst.getDepth(vertex) % 2 == 0 ? COLOR_ONE : COLOR_TWO;
            result.put(vertex, color);
        });
        return result;
    }

    public static <V, E> Map<VertexColor, List<V>> colorToLists(Graph<V, E> graph) {
        var result = new HashMap<VertexColor, List<V>>();
        result.put(COLOR_ONE, new ArrayList<>());
        result.put(COLOR_TWO, new ArrayList<>());
        var breadthFirst = new BreadthFirstIterator<>(graph);
        breadthFirst.forEachRemaining(vertex -> {
            var color = breadthFirst.getDepth(vertex) % 2 == 0 ? COLOR_ONE : COLOR_TWO;
            result.get(color).add(vertex);
        });
        return result;
    }
}
