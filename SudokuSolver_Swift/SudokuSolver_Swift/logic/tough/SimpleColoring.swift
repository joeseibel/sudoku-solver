import SwiftGraph

/*
 * http://www.sudokuwiki.org/Singles_Chains
 *
 * A single's chain is a graph for a particular candidate that connects two cells when those are the only two cells in a
 * unit with that candidate. Each chain is colored with alternating colors such that for a given vertex with a given
 * color, all adjacent vertices have the opposite color. The two colors represent the two possible solutions for each
 * cell in the chain. Either the first color is the solution for the chain or the second color is.
 *
 * Rule 2: Twice in a Unit
 *
 * If there are two or more vertices with the same color that are in the same unit, then that color cannot be the
 * solution. All candidates with that color in that chain can be removed.
 */
func simpleColoringRule2(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        createConnectedComponents(board: board, candidate: candidate).flatMap { graph in
            let colors = graph.colorToDictionary()
            return graph.zipEveryPair()
                .first { a, b in colors[a] == colors[b] && a.isInSameUnit(as: b) }
                .map { a, b in colors[a] }
                .map { colorToRemove in graph.filter { colors[$0] == colorToRemove }.map { ($0, candidate) } }
                ?? []
        }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 4: Two colors 'elsewhere'
 *
 * If an unsolved cell with a given candidate is outside the chain, and it is in the same units as two differently
 * colored vertices, then one of those two vertices must be the solution for the candidate. The candidate can be removed
 * from the cell outside the chain.
 */
func simpleColoringRule4(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        createConnectedComponents(board: board, candidate: candidate).flatMap { graph in
            let (colorOne, colorTwo) = graph.colorToLists()
            return board.cells
                .unsolvedCells
                .filter { cell in
                    cell.candidates.contains(candidate) &&
                        !graph.contains(cell) &&
                        colorOne.contains(where: cell.isInSameUnit) &&
                        colorTwo.contains(where: cell.isInSameUnit)
                }
                .map { ($0, candidate) }
        }
    }.mergeToRemoveCandidates()
}

extension Graph where V == UnsolvedCell, E == UnweightedEdge {
    func toDOT(candidate: SudokuNumber) -> String {
        toDOT(graphId: String(describing: candidate), vertexLabelProvider: \.vertexLabel)
    }
}

private func createConnectedComponents(
    board: Board<Cell>,
    candidate: SudokuNumber
) -> [UnweightedUniqueElementsGraph<UnsolvedCell>] {
    let graph = UnweightedUniqueElementsGraph<UnsolvedCell>()
    for unit in board.units {
        let withCandidate = unit.unsolvedCells.filter { $0.candidates.contains(candidate) }
        if withCandidate.count == 2 {
            let a = withCandidate.first!
            let b = withCandidate.last!
            let aIndex = graph.addVertex(a)
            let bIndex = graph.addVertex(b)
            graph.addEdge(fromIndex: aIndex, toIndex: bIndex)
        }
    }
    return graph.connectedComponents
}
