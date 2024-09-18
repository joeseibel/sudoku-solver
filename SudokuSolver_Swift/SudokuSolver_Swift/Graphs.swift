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

extension Graph where E: WeightedEdgeProtocol, E.Weight == Strength {
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
 * While using Strength as a weight might violate the mathematical concept of a weight, it doesn't break anything
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

/*
 * To understand why the retroactive annotation is used, see this Swift proposal:
 * https://github.com/swiftlang/swift-evolution/blob/main/proposals/0364-retroactive-conformance-warning.md
 *
 * If SwiftGraph ever adds its own conformance of WeightedEdge to Hashable, then this extension should be removed.
 */
extension WeightedEdge<Strength>: @retroactive Hashable {
    public func hash(into hasher: inout Hasher) {
        hasher.combine(u)
        hasher.combine(v)
        hasher.combine(directed)
        hasher.combine(weight)
    }
}

extension Graph where Index == Int, E: WeightedEdgeProtocol, E.Weight == Strength {
    /*
     * Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The returned graph
     * will either be empty or only contain vertices with a degree of two or more and be connected by at least one
     * strong link and one weak link.
     */
    mutating func trim() {
        var toRemove: Index?
        repeat {
            toRemove = indices.first { index in
                let edges = edgesForIndex(index)
                return edges.count < 2 || !edges.contains { $0.weight == .strong }
            }
            if let toRemove {
                removeVertexAtIndex(toRemove)
            }
        } while toRemove != nil
    }
}

func getWeakEdgesInAlternatingCycle<G: Graph>(
    graph: G
) -> Set<G.E> where G.Index == Int, G.E: WeightedEdgeProtocol, G.E.Weight == Strength {
    var weakEdgesInAlternatingCycle = Set<G.E>()
    for edge in graph.edgeList().filter({ $0.weight == .weak }) {
        if !weakEdgesInAlternatingCycle.contains(edge) {
            weakEdgesInAlternatingCycle.formUnion(getAlternatingCycleWeakEdges(graph: graph, startEdge: edge))
        }
    }
    return weakEdgesInAlternatingCycle
}

private func getAlternatingCycleWeakEdges<G: Graph>(
    graph: G,
    startEdge: G.E
) -> [G.E] where G.Index == Int, G.E: WeightedEdgeProtocol, G.E.Weight == Strength {
    precondition(startEdge.weight == .weak, "startEdge must be weak.")
    let start = startEdge.u
    let end = startEdge.v
    
    func getAlternatingCycleWeakEdges(
        currentIndex: G.Index,
        nextType: Strength,
        visited: Set<G.Index>,
        weakEdges: [G.E]
    ) -> [G.E] {
        let nextEdgesAndIndices = graph.edgesForIndex(currentIndex)
            .filter { $0.weight.isCompatible(with: nextType) }
            .map { ($0, $0.getOppositeIndex(index: currentIndex)) }
        return if nextType == .strong && nextEdgesAndIndices.contains(where: { _, nextIndex in nextIndex == end }) {
            weakEdges
        } else {
            nextEdgesAndIndices.lazy
                .filter { _, nextIndex in nextIndex != end && !visited.contains(nextIndex) }
                .map { nextEdge, nextIndex in
                    getAlternatingCycleWeakEdges(
                        currentIndex: nextIndex,
                        nextType: nextType.opposite,
                        visited: visited.union([nextIndex]),
                        weakEdges: nextEdge.weight == .weak ? weakEdges + [nextEdge] : weakEdges
                    )
                }
                .first { !$0.isEmpty }
                ?? []
        }
    }
    
    let weakEdges = getAlternatingCycleWeakEdges(
        currentIndex: start,
        nextType: .strong,
        visited: [start],
        weakEdges: [startEdge]
    )
    assert(!weakEdges.contains { $0.weight == .strong }, "There are strong edges in the return value.")
    return weakEdges
}

func alternatingCycleExists<G: Graph>(
    graph: G,
    index: G.Index,
    adjacentEdgesType: Strength
) -> Bool where G.Index == Int, G.E: WeightedEdgeProtocol, G.E.Weight == Strength {
    graph.edgesForIndex(index).filter { $0.weight == adjacentEdgesType }.zipEveryPair().contains { edgeA, edgeB in
        let start = edgeA.getOppositeIndex(index: index)
        let end = edgeB.getOppositeIndex(index: index)
        
        func alternatingCycleExists(currentIndex: G.Index, nextType: Strength, visited: Set<G.Index>) -> Bool {
            let nextIndices = graph.edgesForIndex(currentIndex)
                .filter { $0.weight.isCompatible(with: nextType) }
                .map { $0.getOppositeIndex(index: currentIndex) }
            return adjacentEdgesType.opposite == nextType && nextIndices.contains(end) ||
                Set(nextIndices).subtracting(visited).subtracting([end]).contains { nextIndex in
                    alternatingCycleExists(
                        currentIndex: nextIndex,
                        nextType: nextType.opposite,
                        visited: visited.union([nextIndex])
                    )
                }
        }
        
        return alternatingCycleExists(
            currentIndex: start,
            nextType: adjacentEdgesType.opposite,
            visited: [index, start]
        )
    }
}
