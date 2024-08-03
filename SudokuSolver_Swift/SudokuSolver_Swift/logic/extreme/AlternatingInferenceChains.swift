import SwiftGraph

/*
 * https://www.sudokuwiki.org/Alternating_Inference_Chains
 *
 * Alternating Inference Chains are based on a graph type in which each vertex is a specific candidate in a cell and the
 * edges can either be strong or weak links. A strong link connects two vertices in a unit that share a candidate when
 * they are in the only unsolved cells in that unit with the candidate. A strong link also connects two vertices in a
 * single cell when they are the only two candidates in that cell. A weak link connects two vertices in a unit that
 * share a candidate when they are not the only unsolved cells in that unit with the candidate. A weak link also
 * connects two vertices in a single cell when there are more than two candidates in that cell. An Alternating Inference
 * Chain is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex of a link is
 * the solution, then the other vertex must not be the solution. If one vertex of a strong link is not the solution,
 * then the other vertex must be the solution. Alternating Inference Chains are very similar to X-Cycles and Grouped
 * X-Cycles.
 *
 * Note that this implementation of Alternating Inference Chains can handle cases in which the chain is not strictly
 * alternating between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak
 * link.
 *
 * Rule 1:
 *
 * If an Alternating Inference Chain has an even number of vertices and therefore continuously alternates between strong
 * and weak, then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link.
 * If a weak link connects a common candidate across two different cells, then that candidate can be removed from any
 * other cell which is in the same unit as the two vertices. If a weak link connects two candidates of the same cell,
 * then all other candidates can be removed from that cell.
 */
func alternatingInferenceChainsRule1(board: Board<Cell>) -> [BoardModification] {
    var graph = buildGraph(board: board)
    graph.trim()
    return getWeakEdgesInAlternatingCycle(graph: graph).flatMap { edge in
        let source = graph.vertexAtIndex(edge.u)
        let sourceCell = source.cell
        let sourceCandidate = source.candidate
        let target = graph.vertexAtIndex(edge.v)
        let targetCell = target.cell
        let targetCandidate = target.candidate
        
        if sourceCell == targetCell {
            return sourceCell.candidates.subtracting([sourceCandidate, targetCandidate]).map { (sourceCell, $0) }
        } else {
            
            func removeFromUnit(
                sourceUnitIndex: Int,
                targetUnitIndex: Int,
                getUnit: (Int) -> [Cell]
            ) -> [LocatedCandidate] {
                if sourceUnitIndex == targetUnitIndex {
                    getUnit(sourceUnitIndex)
                        .unsolvedCells
                        .filter { $0.candidates.contains(sourceCandidate) && $0 != sourceCell && $0 != targetCell }
                        .map { ($0, sourceCandidate) }
                } else {
                    []
                }
            }
            
            let rowRemovals = removeFromUnit(
                sourceUnitIndex: sourceCell.row,
                targetUnitIndex: targetCell.row,
                getUnit: board.getRow
            )
            let columnRemovals = removeFromUnit(
                sourceUnitIndex: sourceCell.column,
                targetUnitIndex: targetCell.column,
                getUnit: board.getColumn
            )
            let blockRemovals = removeFromUnit(
                sourceUnitIndex: sourceCell.block,
                targetUnitIndex: targetCell.block,
                getUnit: board.getBlock
            )
            
            return rowRemovals + columnRemovals + blockRemovals
        }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 2:
 *
 * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
 * except for one vertex which is connected by two strong links, then the graph is a contradiction. Removing the
 * candidate from the cell of interest implies that the candidate must be the solution for that cell, thus causing the
 * cycle to contradict itself. However, considering the candidate to be the solution for that cell does not cause any
 * contradiction in the cycle. Therefore, the candidate must be the solution for that cell.
 *
 * Note that this implementation of rule 2 does not allow for a candidate to be revisited in the chain. A candidate can
 * appear multiple times in a chain, but only if all the occurrences are consecutive.
 */
func alternatingInferenceChainsRule2(board: Board<Cell>) -> [BoardModification] {
    var graph = buildGraph(board: board)
    graph.trim()
    return graph.indices
        .filter { alternatingCycleExistsAIC(graph: graph, index: $0, adjacentEdgesType: .strong) }
        .map { index in
            let vertex = graph.vertexAtIndex(index)
            return BoardModification(cell: vertex.cell, value: vertex.candidate)
        }
}

/*
 * Rule 3:
 *
 * If an Alternating Inference Chain has an odd number of vertices and the edges alternate between strong and weak,
 * except for one vertex which is connected by two weak links, then the graph is a contradiction. Considering the
 * candidate to be the solution for the cell of interest implies that the candidate must be removed from that cell, thus
 * causing the cycle to contradict itself. However, removing the candidate from that cell does not cause any
 * contradiction in the cycle. Therefore, the candidate can be removed from the cell.
 *
 * Note that this implementation of rule 3 does not allow for a candidate to be revisited in the chain. A candidate can
 * appear multiple times in a chain, but only if all the occurrences are consecutive.
 */
func alternatingInferenceChainsRule3(board: Board<Cell>) -> [BoardModification] {
    let graph = buildGraph(board: board)
    return graph.indices
        .filter { alternatingCycleExistsAIC(graph: graph, index: $0, adjacentEdgesType: .weak) }
        .map { index in
            let vertex = graph.vertexAtIndex(index)
            return (vertex.cell, vertex.candidate)
        }
        .mergeToRemoveCandidates()
}

private func buildGraph(board: Board<Cell>) -> WeightedUniqueElementsGraph<CodableLocatedCandidate, Strength> {
    let graph = WeightedUniqueElementsGraph<CodableLocatedCandidate, Strength>()
    
    //Connect cells.
    board.units.forEach { unit in
        SudokuNumber.allCases.forEach { candidate in
            let withCandidates = unit.unsolvedCells.filter { $0.candidates.contains(candidate) }
            let strength = withCandidates.count == 2 ? Strength.strong : .weak
            withCandidates.zipEveryPair().forEach { a, b in
                let aIndex = graph.addVertex(CodableLocatedCandidate(cell: a, candidate: candidate))
                let bIndex = graph.addVertex(CodableLocatedCandidate(cell: b, candidate: candidate))
                graph.addEdge(fromIndex: aIndex, toIndex: bIndex, weight: strength)
            }
        }
    }
    
    //Connect candidates in cells.
    board.cells.unsolvedCells.forEach { cell in
        let strength = cell.candidates.count == 2 ? Strength.strong : .weak
        cell.candidates.zipEveryPair().forEach { a, b in
            let aIndex = graph.addVertex(CodableLocatedCandidate(cell: cell, candidate: a))
            let bIndex = graph.addVertex(CodableLocatedCandidate(cell: cell, candidate: b))
            graph.addEdge(fromIndex: aIndex, toIndex: bIndex, weight: strength)
        }
    }
    
    return graph
}

private func alternatingCycleExistsAIC<G: Graph>(
    graph: G,
    index: G.Index,
    adjacentEdgesType: Strength
) -> Bool where G.Index == Int, G.V == CodableLocatedCandidate, G.E: WeightedEdgeProtocol, G.E.Weight == Strength {
    let indexCandidate = graph.vertexAtIndex(index).candidate
    return graph.edgesForIndex(index)
        .filter { $0.weight == adjacentEdgesType }
        .zipEveryPair()
        .contains { edgeA, edgeB in
            let start = edgeA.getOppositeIndex(index: index)
            let startCandidate = graph.vertexAtIndex(start).candidate
            let end = edgeB.getOppositeIndex(index: index)
            let endCandidate = graph.vertexAtIndex(end).candidate
            
            func alternatingCycleExists(
                currentIndex: G.Index,
                nextType: Strength,
                visited: Set<G.Index>,
                visitedCandidates: Set<SudokuNumber>
            ) -> Bool {
                let currentCandidate = graph.vertexAtIndex(currentIndex).candidate
                let nextIndices = graph.edgesForIndex(currentIndex)
                    .filter { $0.weight.isCompatible(with: nextType) }
                    .map { $0.getOppositeIndex(index: currentIndex) }
                    .filter { oppositeIndex in
                        let oppositeCandidate = graph.vertexAtIndex(oppositeIndex).candidate
                        return oppositeCandidate == currentCandidate || !visitedCandidates.contains(oppositeCandidate)
                    }
                return adjacentEdgesType.opposite == nextType && nextIndices.contains(end) ||
                    Set(nextIndices).subtracting(visited).subtracting([end]).contains { nextIndex in
                        let nextVisited = visited.union([nextIndex])
                        let nextCandidate = graph.vertexAtIndex(nextIndex).candidate
                        let nextVisitedCandidates = if currentCandidate == nextCandidate {
                            visitedCandidates
                        } else {
                            visitedCandidates.union([nextCandidate])
                        }
                        return alternatingCycleExists(
                            currentIndex: nextIndex,
                            nextType: nextType.opposite,
                            visited: nextVisited,
                            visitedCandidates: nextVisitedCandidates
                        )
                    }
            }
            
            return alternatingCycleExists(
                currentIndex: start,
                nextType: adjacentEdgesType.opposite,
                visited: [index, start],
                visitedCandidates: Set([indexCandidate, startCandidate]).subtracting([endCandidate])
            )
        }
}
