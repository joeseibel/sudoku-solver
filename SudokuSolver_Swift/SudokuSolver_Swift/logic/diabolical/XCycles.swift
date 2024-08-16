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

extension Graph where V == UnsolvedCell, E: WeightedEdgeProtocol, E.Weight == Strength {
    func toDOT(candidate: SudokuNumber) -> String {
        toDOT(graphId: String(describing: candidate)) { "[\($0.row),\($0.column)]" }
    }
}

private func createStrongLinks(
    board: Board<Cell>,
    candidate: SudokuNumber
) -> WeightedUniqueElementsGraph<UnsolvedCell, Strength> {
    let graph = WeightedUniqueElementsGraph<UnsolvedCell, Strength>()
    board.units
        .map { unit in unit.unsolvedCells.filter { $0.candidates.contains(candidate) } }
        .filter { withCandidate in withCandidate.count == 2 }
        .forEach { withCandidate in
            let a = withCandidate[0]
            let b = withCandidate[1]
            let aIndex = graph.addVertex(a)
            let bIndex = graph.addVertex(b)
            graph.addEdge(fromIndex: aIndex, toIndex: bIndex, weight: .strong)
        }
    return graph
}

private extension WeightedUniqueElementsGraph<UnsolvedCell, Strength> {
    func addWeakLinks() {
        indices.zipEveryPair()
            .filter { aIndex, bIndex in
                let a = vertexAtIndex(aIndex)
                let b = vertexAtIndex(bIndex)
                return a.isInSameUnit(as: b) && !edgeExists(fromIndex: aIndex, toIndex: bIndex)
            }
            .forEach { aIndex, bIndex in addEdge(fromIndex: aIndex, toIndex: bIndex, weight: .weak) }
    }
    
    func additionalWeakLinks(board: Board<Cell>, candidate: SudokuNumber) {
        for cell in board.cells.unsolvedCells.filter({ $0.candidates.contains(candidate) && !contains($0) }) {
            for index in indices.filter({ vertexAtIndex($0).isInSameUnit(as: cell) }) {
                addEdge(fromIndex: index, toIndex: addVertex(cell), weight: .weak)
            }
        }
    }
}
