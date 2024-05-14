import SwiftGraph

/*
 * http://www.sudokuwiki.org/X_Cycles
 * http://www.sudokuwiki.org/X_Cycles_Part_2
 *
 * X-Cycles is based on a graph type which is an extension of single's chain. An X-Cycles graph is for a single
 * candidate and can have either strong or weak links. A strong link connects two cells in a unit when they are the only
 * unsolved cells in that unit with the candidate. A weak link connects two cells in a unit when they are not the only
 * unsolved cells in that unit with the candidate. An X-Cycle is a cycle in the graph in which the edges alternate
 * between strong and weak links. If one cell of a link is the solution, then the other cell must not be the solution.
 * If one cell of a strong link is not the solution, then the other cell must be the solution.
 *
 * Note that this implementation of X-Cycles can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 *
 * Rule 1:
 *
 * If an X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak, then the
 * graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can be
 * removed from any other cell which is in the same unit as both vertices of a weak link.
 */
func xCyclesRule1(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        var graph = createStrongLinks(board: board, candidate: candidate)
        graph.addWeakLinks()
        graph.trim()
        return getWeakEdgesInAlternatingCycle(graph: graph).flatMap { edge -> [LocatedCandidate] in
            let source = graph.vertexAtIndex(edge.u)
            let target = graph.vertexAtIndex(edge.v)
            
            func removeFromUnit(getUnitIndex: (UnsolvedCell) -> Int, getUnit: (Int) -> [Cell]) -> [LocatedCandidate] {
                if getUnitIndex(source) == getUnitIndex(target) {
                    return getUnit(getUnitIndex(source))
                        .unsolvedCells
                        .filter { $0.candidates.contains(candidate) && $0 != source && $0 != target }
                        .map { ($0, candidate) }
                } else {
                    return []
                }
            }
            
            let rowRemovals = removeFromUnit(getUnitIndex: \.row, getUnit: board.getRow)
            let columnRemovals = removeFromUnit(getUnitIndex: \.column, getUnit: board.getColumn)
            let blockRemovals = removeFromUnit(getUnitIndex: \.block, getUnit: board.getBlock)
            return rowRemovals + columnRemovals + blockRemovals
        }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 2:
 *
 * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
 * which is connected by two strong links, then the graph is a contradiction. Removing the candidate from the vertex of
 * interest implies that the candidate must be the solution for that vertex, thus causing the cycle to contradict
 * itself. However, considering the candidate to be the solution for that vertex does not cause any contradiction in the
 * cycle. Therefore, the candidate must be the solution for that vertex.
 */
func xCyclesRule2(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        let graph = createStrongLinks(board: board, candidate: candidate)
        graph.addWeakLinks()
        return graph.indices
            .filter { index in alternatingCycleExists(graph: graph, index: index, adjacentEdgesType: .strong) }
            .map { BoardModification(cell: graph.vertexAtIndex($0), value: candidate) }
    }
}

/*
 * Rule 3:
 *
 * If an X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one vertex
 * which is connected by two weak links, then the graph is a contradiction. Considering the candidate to be the solution
 * for the vertex of interest implies that the candidate must be removed from that vertex, thus causing the cycle to
 * contradict itself. However, removing the candidate from that vertex does not cause any contradiction in the cycle.
 * Therefore, the candidate can be removed from the vertex.
 */
func xCyclesRule3(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        let graph = createStrongLinks(board: board, candidate: candidate)
        graph.addWeakLinks()
        graph.additionalWeakLinks(board: board, candidate: candidate)
        return graph.indices
            .filter { index in alternatingCycleExists(graph: graph, index: index, adjacentEdgesType: .weak) }
            .map { (graph.vertexAtIndex($0), candidate) }
    }.mergeToRemoveCandidates()
}

extension UniqueElementsGraph<UnsolvedCell, StrengthEdge> {
    func toDOT(candidate: SudokuNumber) -> String {
        var result = "strict graph \(candidate) {\n"
        for edge in edgeList() {
            let u = vertexAtIndex(edge.u)
            let v = vertexAtIndex(edge.v)
            result += "  \"[\(u.row),\(u.column)]\" -- \"[\(v.row),\(v.column)]\""
            if edge.strength == .weak {
                result += " [style = dashed]"
            }
            result += "\n"
        }
        result += "}"
        return result
    }
}

private func createStrongLinks(
    board: Board<Cell>,
    candidate: SudokuNumber
) -> UniqueElementsGraph<UnsolvedCell, StrengthEdge> {
    let graph = UniqueElementsGraph<UnsolvedCell, StrengthEdge>()
    board.units
        .map { unit in unit.unsolvedCells.filter { $0.candidates.contains(candidate) } }
        .filter { withCandidate in withCandidate.count == 2 }
        .forEach { withCandidate in
            let a = withCandidate[0]
            let b = withCandidate[1]
            let aIndex = graph.addVertex(a)
            let bIndex = graph.addVertex(b)
            graph.addEdge(StrengthEdge(u: aIndex, v: bIndex, strength: .strong))
        }
    return graph
}

private extension UniqueElementsGraph<UnsolvedCell, StrengthEdge> {
    func addWeakLinks() {
        indices.zipEveryPair()
            .filter { aIndex, bIndex in
                let a = vertexAtIndex(aIndex)
                let b = vertexAtIndex(bIndex)
                //Don't need to check for weak links because there are only strong links in the graph.
                return a.isInSameUnit(as: b) && !edgeExists(StrengthEdge(u: aIndex, v: bIndex, strength: .strong))
            }
            .forEach { aIndex, bIndex in addEdge(StrengthEdge(u: aIndex, v: bIndex, strength: .weak)) }
    }
    
    func additionalWeakLinks(board: Board<Cell>, candidate: SudokuNumber) {
        board.cells.unsolvedCells.filter { $0.candidates.contains(candidate) && !contains($0) }.forEach { cell in
            indices.filter { vertexAtIndex($0).isInSameUnit(as: cell) }.forEach { index in
                addEdge(StrengthEdge(u: index, v: addVertex(cell), strength: .weak))
            }
        }
    }
}

private extension UniqueElementsGraph<UnsolvedCell, StrengthEdge> {
    func getOppositeIndex(edge: StrengthEdge, index: Index) -> Index {
        if index == edge.u {
            edge.v
        } else if index == edge.v {
            edge.u
        } else {
            preconditionFailure("Index not found in edge: \(index)")
        }
    }
}

struct StrengthEdge: Edge, Hashable {
    var u: Int
    var v: Int
    var directed: Bool = false
    let strength: Strength
    
    func reversed() -> StrengthEdge {
        StrengthEdge(u: v, v: u, directed: directed, strength: strength)
    }
    
    var description: String {
        return "\(u) -> \(v)"
    }
}

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

private extension Graph where Index == Int, E == StrengthEdge {
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
                return edges.count < 2 || !edges.contains { $0.strength == .strong }
            }
            if let toRemove {
                removeVertexAtIndex(toRemove)
            }
        } while toRemove != nil
    }
}

private func getWeakEdgesInAlternatingCycle(
    graph: UniqueElementsGraph<UnsolvedCell, StrengthEdge>
) -> Set<StrengthEdge> {
    var weakEdgesInAlternatingCycle = Set<StrengthEdge>()
    graph.edgeList().filter { $0.strength == .weak }.forEach { edge in
        if !weakEdgesInAlternatingCycle.contains(edge) {
            weakEdgesInAlternatingCycle.formUnion(getAlternatingCycleWeakEdges(graph: graph, startEdge: edge))
        }
    }
    return weakEdgesInAlternatingCycle
}

private func getAlternatingCycleWeakEdges(
    graph: UniqueElementsGraph<UnsolvedCell, StrengthEdge>,
    startEdge: StrengthEdge
) -> [StrengthEdge] {
    precondition(startEdge.strength == .weak, "startEdge must be weak.")
    let start = startEdge.u
    let end = startEdge.v
    
    func getAlternatingCycleWeakEdges(
        currentIndex: UniqueElementsGraph.Index,
        nextType: Strength,
        visited: Set<UniqueElementsGraph.Index>,
        weakEdges: [StrengthEdge]
    ) -> [StrengthEdge] {
        let nextEdgesAndIndices = graph.edgesForIndex(currentIndex)
            .filter { $0.strength.isCompatible(with: nextType) }
            .map { ($0, graph.getOppositeIndex(edge: $0, index: currentIndex)) }
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
                        weakEdges: nextEdge.strength == .weak ? weakEdges + [nextEdge] : weakEdges
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
    assert(!weakEdges.contains { $0.strength == .strong }, "There are strong edges in the return value.")
    return weakEdges
}

private func alternatingCycleExists(
    graph: UniqueElementsGraph<UnsolvedCell, StrengthEdge>,
    index: UniqueElementsGraph.Index,
    adjacentEdgesType: Strength
) -> Bool {
    graph.edgesForIndex(index).filter { $0.strength == adjacentEdgesType }.zipEveryPair().contains { edgeA, edgeB in
        let start = graph.getOppositeIndex(edge: edgeA, index: index)
        let end = graph.getOppositeIndex(edge: edgeB, index: index)
        
        func alternatingCycleExists(
            currentIndex: UniqueElementsGraph.Index,
            nextType: Strength,
            visited: Set<UniqueElementsGraph.Index>
        ) -> Bool {
            let nextIndices = graph.edgesForIndex(currentIndex)
                .filter { $0.strength.isCompatible(with: nextType) }
                .map { graph.getOppositeIndex(edge: $0, index: currentIndex) }
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
