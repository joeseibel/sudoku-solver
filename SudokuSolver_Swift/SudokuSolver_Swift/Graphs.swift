import SwiftGraph

extension Edge {
    func getOppositeIndex(index: Int) -> Int {
        if index == u {
            v
        } else if index == v {
            u
        } else {
            preconditionFailure("Index not found in edge: \(index)")
        }
    }
}

extension UnweightedUniqueElementsGraph {
    var connectedComponents: [UnweightedUniqueElementsGraph<V>] {
        var subgraphs = [UnweightedUniqueElementsGraph<V>]()
        for startIndex in indices {
            let startVertex = vertexAtIndex(startIndex)
            if subgraphs.allSatisfy({ !$0.contains(startVertex) }) {
                let subgraph = UnweightedUniqueElementsGraph<V>()
                _ = subgraph.addVertex(startVertex)
                _ = bfs(fromIndex: startIndex, goalTest: { _ in false }, visitOrder: { $0 }) { edge in
                    let u = vertexAtIndex(edge.u)
                    let v = vertexAtIndex(edge.v)
                    let uIndex = subgraph.indexOfVertex(u)!
                    let vIndex = subgraph.addVertex(v)
                    subgraph.addEdge(fromIndex: uIndex, toIndex: vIndex)
                    return true
                }
                subgraphs.append(subgraph)
            }
        }
        return subgraphs
    }
}

extension Graph where E == WeightedEdge<Strength> {
    func toDOT(graphId: String? = nil, vertexLabelProvider getVertexLabel: (V) -> String) -> String {
        var result = "strict graph "
        if let graphId {
            result += graphId + " "
        }
        result += "{\n"
        for edge in edgeList() {
            let u = vertexAtIndex(edge.u)
            let v = vertexAtIndex(edge.v)
            result += "  \"\(getVertexLabel(u))\" -- \"\(getVertexLabel(v))\""
            if edge.weight == .weak {
                result += " [style = dashed]"
            }
            result += "\n"
        }
        result += "}"
        return result
    }
}

/*
 * This type is the struct equivalent of LocatedCandidate and exists so that it can be used as a vertex type in graphs.
 *
 * I tried to use LocatedCandidate as a vertex type, but that didn't work for the following reasons:
 *   1. Graph requires the vertex type to be Codable.
 *   2. LocatedCandidate is a tuple type. Specifically, it is a type alias for (UnsolvedCell, SudokuNumber).
 *   3. Swift does not allow protocol conformance to be added to tuples.
 *
 * Therefore, tuple types cannot be used as a vertex type for Graph. The Swift community does seem interested in adding
 * support for this: https://forums.swift.org/t/protocol-conformance-for-tuples-anonymous-structs/24207
 *
 * If protocol conformance for tuples is added to Swift, then CodableLocatedCandidate can be removed.
 */
struct CodableLocatedCandidate: Hashable, Codable {
    let cell: UnsolvedCell
    let candidate: SudokuNumber
}

enum VertexColor {
    case colorOne, colorTwo
    
    var opposite: VertexColor {
        switch self {
        case .colorOne:
            .colorTwo
        case .colorTwo:
            .colorOne
        }
    }
}

extension Graph where V: Hashable {
    func colorToDictionary() -> [V: VertexColor] {
        var colors = [V: VertexColor]()
        if !isEmpty {
            colors[vertexAtIndex(startIndex)] = .colorOne
            _ = bfs(fromIndex: startIndex, goalTest: { _ in false }, visitOrder: { $0 }) { edge in
                let u = vertexAtIndex(edge.u)
                let v = vertexAtIndex(edge.v)
                colors[v] = colors[u]!.opposite
                return true
            }
        }
        return colors
    }
}

extension Graph {
    func colorToLists() -> ([V], [V]) {
        var colorOne = [V]()
        var colorTwo = [V]()
        if !isEmpty {
            colorOne.append(vertexAtIndex(startIndex))
            _ = bfs(fromIndex: startIndex, goalTest: { _ in false }, visitOrder: { $0 }) { edge in
                let u = vertexAtIndex(edge.u)
                let v = vertexAtIndex(edge.v)
                if colorOne.contains(u) {
                    colorTwo.append(v)
                } else {
                    colorOne.append(v)
                }
                return true
            }
        }
        return (colorOne, colorTwo)
    }
}

/*
 * Strength should be used as the weight for a WeightedUniqueElementsGraph.
 *
 * I considered having separate Strength and StrengthEdge types like I do for other languages. However, SwiftGraph
 * permits the weight to an arbitrary type as long as it is Equatable and Codable. This is different from JGraphT which
 * forces the weight to be a double.
 *
 * I am aware that this might be considered an abuse of the term "weight" since Strength is not a number. However, using
 * a weight makes the code simplier due to the methods that are available on Graph when its edge type is a WeightedEdge.
 * While using Strength as a weight might violate the mathematical concept of a weak, it doesn't break anything
 * regarding SwiftGraph.
 */
enum Strength: Codable {
    case strong, weak
    
    var opposite: Strength {
        switch self {
        case .strong:
            .weak
        case .weak:
            .strong
        }
    }
    
    /*
     * For solutions that look for alternating edge types in a graph, it can sometimes be the case that a strong link
     * can take the place of a weak link. In those cases, this method should be called instead of performing an equality
     * check.
     */
    func isCompatible(with requiredType: Strength) -> Bool {
        switch self {
        case .strong:
            true
        case .weak:
            requiredType == .weak
        }
    }
}

extension WeightedEdge<Strength>: Hashable {
    public func hash(into hasher: inout Hasher) {
        hasher.combine(u)
        hasher.combine(v)
        hasher.combine(directed)
        hasher.combine(weight)
    }
}
