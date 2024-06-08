import SwiftGraph

/*
 * https://www.sudokuwiki.org/3D_Medusa
 *
 * A 3D Medusa is a graph type in which each vertex is a particular candidate in a cell and each edge is a strong link.
 * A strong link is an edge such that if one vertex of the link is the solution, then the other vertex must not be the
 * solution. A strong link also means that if one vertex of the link is not the solution, then the other vertex must be
 * the solution. When a candidate is in only two cells of a unit, there is an edge between the candidate of those two
 * cells. Additionally, when a cell contains only two candidates, there is an edge between the two candidates of that
 * cell. Each medusa is colored with alternating colors such that for a given vertex with a given color, all adjacent
 * vertices have the opposite color. The two colors represent the two possible solutions. Either the first color is the
 * solution for the medusa or the second color is.
 *
 * Rule 1: Twice in a Cell
 *
 * If there are two vertices with the same color that are in the same cell, then that color cannot be the solution and
 * the opposite color must be the solution. All vertices with the opposite color can be set as the solution.
 */
func medusaRule1(board: Board<Cell>) -> [BoardModification] {
    createConnectedComponents(board: board).flatMap { graph in
        let colors = graph.colorToDictionary()
        return graph.zipEveryPair()
            .first { a, b in a.cell == b.cell && colors[a] == colors[b] }
            .flatMap { a, _ in colors[a] }
            .map(\.opposite)
            .map { colorToSet in
                graph.filter { colors[$0] == colorToSet }.map { BoardModification(cell: $0.cell, value: $0.candidate) }
            }
            ?? []
    }
}

/*
 * Rule 2: Twice in a Unit
 *
 * If there are two vertices with the same color and the same candidate that are in the same unit, then that color
 * cannot be the solution and the opposite color must be the solution. All vertices with the opposite color can be set
 * as the solution.
 */
func medusaRule2(board: Board<Cell>) -> [BoardModification] {
    createConnectedComponents(board: board).flatMap { graph in
        let colors = graph.colorToDictionary()
        return graph.zipEveryPair()
            .first { a, b in a.candidate == b.candidate && colors[a] == colors[b] && a.cell.isInSameUnit(as: b.cell) }
            .flatMap { a, _ in colors[a] }
            .map(\.opposite)
            .map { colorToSet in
                graph.filter { colors[$0] == colorToSet }.map { BoardModification(cell: $0.cell, value: $0.candidate) }
            }
            ?? []
    }
}

/*
 * Rule 3: Two colors in a cell
 *
 * If there are two differently colored candidates in a cell, then the solution must be one of the two candidates. All
 * other candidates in the cell can be removed.
 */
func medusaRule3(board: Board<Cell>) -> [BoardModification] {
    createConnectedComponents(board: board).flatMap { graph in
        let colors = graph.colorToDictionary()
        return graph.filter { $0.cell.candidates.count > 2 }
            .zipEveryPair()
            .first { a, b in a.cell == b.cell && colors[a] != colors[b] }
            .map { a, _ in a }
            .map(\.cell)
            .map { cell in
                cell.candidates
                    .map { (cell, $0) }
                    .filter { cell, candidate in
                        !graph.contains(CodableLocatedCandidate(cell: cell, candidate: candidate))
                    }
            }
            ?? []
    }.mergeToRemoveCandidates()
}

/*
 * Rule 4: Two colors 'elsewhere'
 *
 * Given a candidate, if there is an unsolved cell with that candidate, it is uncolored, and the cell can see two other
 * cells which both have that candidate, and they are differently colored, then the candidate must be the solution to
 * one of the other cells, and it cannot be the solution to the first cell with the uncolored candidate. The uncolored
 * candidate can be removed from the first cell.
 */
func medusaRule4(board: Board<Cell>) -> [BoardModification] {
    createConnectedComponents(board: board).flatMap { graph in
        let (colorOne, colorTwo) = graph.colorToLists()
        return board.cells
            .unsolvedCells
            .flatMap { cell in cell.candidates.map { candidate in (cell, candidate) } }
            .filter { cell, candidate in !graph.contains(CodableLocatedCandidate(cell: cell, candidate: candidate)) }
            .filter { cell, candidate in
                
                func canSeeColor(color: [CodableLocatedCandidate]) -> Bool {
                    color.contains { colored in candidate == colored.candidate && cell.isInSameUnit(as: colored.cell) }
                }
                
                return canSeeColor(color: colorOne) && canSeeColor(color: colorTwo)
            }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 5: Two colors Unit + Cell
 *
 * If there is an unsolved cell with an uncolored candidate, that candidate can see a colored candidate of the same
 * number, and the unsolved cell contains a candidate colored with the opposite color, then either the candidate in the
 * same unit is the solution for that cell or the candidate in the same cell is the solution. In either case, the
 * uncolored candidate cannot be the solution and can be removed from the unsolved cell.
 */
func medusaRule5(board: Board<Cell>) -> [BoardModification] {
    createConnectedComponents(board: board).flatMap { graph in
        let (colorOne, colorTwo) = graph.colorToLists()
        return board.cells
            .unsolvedCells
            .flatMap { cell in cell.candidates.map { candidate in (cell, candidate) } }
            .filter { cell, candidate in !graph.contains(CodableLocatedCandidate(cell: cell, candidate: candidate)) }
            .filter { cell, candidate in
                
                func canSeeColor(color: [CodableLocatedCandidate]) -> Bool {
                    color.contains { colored in candidate == colored.candidate && cell.isInSameUnit(as: colored.cell) }
                }
                
                func colorInCell(color: [CodableLocatedCandidate]) -> Bool {
                    cell.candidates.contains { color.contains(CodableLocatedCandidate(cell: cell, candidate: $0)) }
                }
                
                return canSeeColor(color: colorOne) && colorInCell(color: colorTwo) ||
                    canSeeColor(color: colorTwo) && colorInCell(color: colorOne)
            }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 6: Cell Emptied by Color
 *
 * If there is an unsolved cell in which every candidate is uncolored and every candidate can see the same color, then
 * that color cannot be the solution since it would lead to the cell being emptied of candidates and still have no
 * solution. All vertices with the opposite color can be set as the solution.
 */
func medusaRule6(board: Board<Cell>) -> [BoardModification] {
    createConnectedComponents(board: board).flatMap { graph in
        let (colorOne, colorTwo) = graph.colorToLists()
        return board.cells
            .unsolvedCells
            .filter { cell in
                cell.candidates.allSatisfy { candidate in
                    !graph.contains(CodableLocatedCandidate(cell: cell, candidate: candidate))
                }
            }
            .firstNonNil { cell -> [CodableLocatedCandidate]? in
                
                func everyCandidateCanSeeColor(color: [CodableLocatedCandidate]) -> Bool {
                    cell.candidates.allSatisfy { candidate in
                        color.contains { colored in
                            candidate == colored.candidate && cell.isInSameUnit(as: colored.cell)
                        }
                    }
                }
                
                return if everyCandidateCanSeeColor(color: colorOne) {
                    colorTwo
                } else if everyCandidateCanSeeColor(color: colorTwo) {
                    colorOne
                } else {
                    nil
                }
            }? // I really want the '?' to be on the next line, but Swift requires that it be on this line.
            .map { colored in BoardModification(cell: colored.cell, value: colored.candidate) }
            ?? []
    }
}

private func createConnectedComponents(board: Board<Cell>) -> [UnweightedUniqueElementsGraph<CodableLocatedCandidate>] {
    let graph = UnweightedUniqueElementsGraph<CodableLocatedCandidate>()
    board.cells.unsolvedCells.filter { $0.candidates.count == 2 }.forEach { cell in
        let candidates = Array(cell.candidates)
        let a = graph.addVertex(CodableLocatedCandidate(cell: cell, candidate: candidates.first!))
        let b = graph.addVertex(CodableLocatedCandidate(cell: cell, candidate: candidates.last!))
        graph.addEdge(fromIndex: a, toIndex: b)
    }
    SudokuNumber.allCases.forEach { candidate in
        board.units
            .map { $0.unsolvedCells.filter { $0.candidates.contains(candidate) } }
            .filter { $0.count == 2 }
            .forEach { unit in
                let a = graph.addVertex(CodableLocatedCandidate(cell: unit.first!, candidate: candidate))
                let b = graph.addVertex(CodableLocatedCandidate(cell: unit.last!, candidate: candidate))
                graph.addEdge(fromIndex: a, toIndex: b)
            }
    }
    return graph.connectedComponents
}
