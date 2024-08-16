import SwiftGraph

/*
 * https://www.sudokuwiki.org/XY_Chains
 *
 * XY-Chains is based on a graph type which shares many similarities to X-Cycles. Unlike X-Cycles, an XY-Chains graph
 * includes multiple candidates. This results in a single XY-Chains graph per board whereas there can be up to nine
 * X-Cycles graphs per board, one for each candidate. Each vertex in an XY-Chains graph is a particular candidate in a
 * cell and the edges are either strong or weak links. A strong link connects two candidates of a single cell when they
 * are the only candidates of that cell. A weak link connects two vertices which have the same candidate, are in
 * different cells, but are in the same unit. An XY-Chain is a chain between two vertices of the graph that have the
 * same candidate, the edges of the chain alternate between strong and weak links, and the last links on either end of
 * the chain are strong. If one vertex of a link is the solution, then the other vertex must not be the solution. If one
 * vertex of a strong link is not the solution, then the other vertex must be the solution. When there is a proper chain
 * in the graph it means that one of the two end points must be the solution. The candidate can be removed from any cell
 * of the board which is not an end point of the chain and that cell can see both end points.
 *
 * Note that this implementation of XY-Chains can handle cases in which the chain is not strictly alternating between
 * strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 */
func xyChains(board: Board<Cell>) -> [BoardModification] {
    let graph = createStrongLinks(board: board)
    graph.addWeakLinks()
    return Dictionary(grouping: graph.indices, by: { graph.vertexAtIndex($0).candidate })
        .flatMap { candidate, indices in
            indices.zipEveryPair()
                .flatMap { indexA, indexB -> [UnsolvedCell] in
                    let cellA = graph.vertexAtIndex(indexA).cell
                    let cellB = graph.vertexAtIndex(indexB).cell
                    let visibleCells = board.cells
                        .unsolvedCells
                        .filter {
                            $0.candidates.contains(candidate) &&
                                $0 != cellA &&
                                $0 != cellB &&
                                $0.isInSameUnit(as: cellA) &&
                                $0.isInSameUnit(as: cellB)
                        }
                    return if !visibleCells.isEmpty && alternatingPathExists(graph: graph, start: indexA, end: indexB) {
                        visibleCells
                    } else {
                        []
                    }
                }
                .map { ($0, candidate) }
        }
        .mergeToRemoveCandidates()
}

extension Graph where V == CodableLocatedCandidate, E: WeightedEdgeProtocol, E.Weight == Strength {
    func toDOT() -> String {
        toDOT { "[\($0.cell.row),\($0.cell.column)] : \($0.candidate)" }
    }
}

private func createStrongLinks(board: Board<Cell>) -> WeightedUniqueElementsGraph<CodableLocatedCandidate, Strength> {
    let graph = WeightedUniqueElementsGraph<CodableLocatedCandidate, Strength>()
    for cell in board.cells.unsolvedCells.filter({ $0.candidates.count == 2 }) {
        let candidates = Array(cell.candidates)
        let source = CodableLocatedCandidate(cell: cell, candidate: candidates.first!)
        let target = CodableLocatedCandidate(cell: cell, candidate: candidates.last!)
        let sourceIndex = graph.addVertex(source)
        let targetIndex = graph.addVertex(target)
        graph.addEdge(fromIndex: sourceIndex, toIndex: targetIndex, weight: .strong)
    }
    return graph
}

private extension WeightedUniqueElementsGraph<CodableLocatedCandidate, Strength> {
    func addWeakLinks() {
        indices.zipEveryPair()
            .filter { indexA, indexB in
                let vertexA = vertexAtIndex(indexA)
                let vertexB = vertexAtIndex(indexB)
                return vertexA.candidate == vertexB.candidate && vertexA.cell.isInSameUnit(as: vertexB.cell)
            }
            .forEach { indexA, indexB in addEdge(fromIndex: indexA, toIndex: indexB, weight: .weak) }
    }
}

private func alternatingPathExists<G: Graph>(
    graph: G,
    start: G.Index,
    end: G.Index
) -> Bool where G.Index == Int, G.E: WeightedEdgeProtocol, G.E.Weight == Strength {
    
    func alternatingPathExists(currentIndex: G.Index, nextType: Strength, visited: Set<G.Index>) -> Bool {
        let nextIndices = graph.edgesForIndex(currentIndex)
            .filter { $0.weight.isCompatible(with: nextType) }
            .map { $0.getOppositeIndex(index: currentIndex) }
        return nextType == .strong && nextIndices.contains(end) ||
            Set(nextIndices).subtracting(visited).subtracting([end]).contains { nextIndex in
                alternatingPathExists(
                    currentIndex: nextIndex,
                    nextType: nextType.opposite,
                    visited: visited.union([nextIndex])
                )
            }
    }
    
    return alternatingPathExists(currentIndex: start, nextType: .strong, visited: [start])
}
