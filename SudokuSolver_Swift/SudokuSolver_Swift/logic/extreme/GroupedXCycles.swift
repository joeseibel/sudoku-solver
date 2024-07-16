import SwiftGraph

/*
 * https://www.sudokuwiki.org/Grouped_X_Cycles
 *
 * Grouped X-Cycles are an extension of X-Cycles in which a vertex can be a cell or a group of cells. Just like
 * X-Cycles, a Grouped X-Cycles graph is for a single candidate. A group is a set of cells with the candidate which
 * share two units. This means that a group exists in the same block and the same row, or it exists in the same block
 * and the same column.
 *
 * Similar to X-Cycles, the edges of a Grouped X-Cycles graph are either strong or weak. Unlike X-Cycles, the edges can
 * connect two cells, a cell and a group, or two groups. A strong link connects two vertices in a unit when they are the
 * only non-overlapping vertices in that unit. A weak link connects two vertices in a unit when they are not the only
 * non-overlapping vertices in that unit.
 *
 * Since a vertex can be a cell or a group of cells, it is possible for vertices to overlap and even for edges to
 * overlap. For example, consider a unit which has three cells with a candidate, two of which form a group and one which
 * is outside the group. In this case there would be four vertices: three vertices for the cells and one for the group.
 * Two of the cell vertices overlap with the cells of the group. This example would also have one strong link and three
 * weak links. The strong link would connect the group to the cell outside the group. This is a strong link because when
 * we discount the cells that overlap with the group, there are only two vertices in the unit. The weak links connect
 * all the individual cells. They are weak because there are more than two cell vertices in the unit when we discount
 * the group.
 *
 * A Grouped X-Cycle is a cycle in the graph in which the edges alternate between strong and weak links. If one vertex
 * of a link contains the solution, then the other vertex must not contain the solution. If one cell of a strong link
 * does not contain the solution, then the other vertex must contain the solution. If a vertex is a group, containing
 * the solution means that one of the cells of the group is the solution. If a vertex is a cell, containing the solution
 * means that the cell is the solution.
 *
 * Note that this implementation of Grouped X-Cycles can handle cases in which the chain is not strictly alternating
 * between strong and weak links. It is tolerant of cases in which a strong link takes the place of a weak link.
 *
 * Rule 1:
 *
 * If a Grouped X-Cycle has an even number of vertices and therefore continuously alternates between strong and weak,
 * then the graph is perfect and has no flaws. Each of the weak links can be treated as a strong link. The candidate can
 * be removed from any cell which is in the same unit as both vertices of a weak link, but not contained in either of
 * the vertices.
 */
func groupedXCyclesRule1(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        var graph = buildGraph(board: board, candidate: candidate)
        graph.trim()
        return getWeakEdgesInAlternatingCycle(graph: graph).flatMap { edge in
            let source = graph.vertexAtIndex(edge.u)
            let target = graph.vertexAtIndex(edge.v)
            
            func removeFromUnit(
                sourceUnitIndex: Int?,
                targetUnitIndex: Int?,
                getUnit: (Int) -> [Cell]
            ) -> [LocatedCandidate] {
                if let sourceUnitIndex, sourceUnitIndex == targetUnitIndex {
                    getUnit(sourceUnitIndex)
                        .unsolvedCells
                        .filter {
                            $0.candidates.contains(candidate) &&
                                !source.cells.contains($0) &&
                                !target.cells.contains($0)
                        }
                        .map { ($0, candidate) }
                } else {
                    []
                }
            }
            
            let rowRemovals = removeFromUnit(
                sourceUnitIndex: source.row,
                targetUnitIndex: target.row,
                getUnit: board.getRow
            )
            let columnRemovals = removeFromUnit(
                sourceUnitIndex: source.column,
                targetUnitIndex: target.column,
                getUnit: board.getColumn
            )
            let blockRemovals = removeFromUnit(
                sourceUnitIndex: source.block,
                targetUnitIndex: target.block,
                getUnit: board.getBlock
            )
            return rowRemovals + columnRemovals + blockRemovals
        }
    }.mergeToRemoveCandidates()
}

/*
 * Rule 2:
 *
 * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
 * vertex which is a cell and is connected by two strong links, then the graph is a contradiction. Removing the
 * candidate from the vertex of interest implies that the candidate must be the solution for that vertex, thus causing
 * the cycle to contradict itself. However, considering the candidate to be the solution for that vertex does not cause
 * any contradiction in the cycle. Therefore, the candidate must be the solution for that vertex.
 */
func groupedXCyclesRule2(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        let graph = buildGraph(board: board, candidate: candidate)
        return graph.vertices
            .filter {
                $0.type == .cell &&
                    alternatingCycleExists(graph: graph, index: graph.indexOfVertex($0)!, adjacentEdgesType: .strong)
            }
            .map { BoardModification(cell: $0.cells.first!, value: candidate) }
    }
}

/*
 * Rule 3:
 *
 * If a Grouped X-Cycle has an odd number of vertices and the edges alternate between strong and weak, except for one
 * vertex which is a cell and is connected by two weak links, then the graph is a contradiction. Considering the
 * candidate to be the solution for the vertex of interest implies that the candidate must be removed from that vertex,
 * thus causing the cycle to contradict itself. However, removing the candidate from that vertex does not cause any
 * contradiction in the cycle. Therefore, the candidate can be removed from the vertex.
 */
func groupedXCyclesRule3(board: Board<Cell>) -> [BoardModification] {
    SudokuNumber.allCases.flatMap { candidate in
        let graph = buildGraph(board: board, candidate: candidate)
        return graph.vertices
            .filter {
                $0.type == .cell &&
                    alternatingCycleExists(graph: graph, index: graph.indexOfVertex($0)!, adjacentEdgesType: .weak)
            }
            .map { ($0.cells.first!, candidate) }
    }.mergeToRemoveCandidates()
}

extension WeightedUniqueElementsGraph<Node, Strength> {
    func toDOT(candidate: SudokuNumber) -> String {
        toDOT(graphId: String(describing: candidate), vertexLabelProvider: String.init)
    }
}

private func buildGraph(board: Board<Cell>, candidate: SudokuNumber) -> WeightedUniqueElementsGraph<Node, Strength> {
    let graph = WeightedUniqueElementsGraph<Node, Strength>()
    
    //Connect cells.
    board.units
        .map { unit in unit.unsolvedCells.filter { $0.candidates.contains(candidate) } }
        .forEach { withCandidate in
            let strength = withCandidate.count == 2 ? Strength.strong : .weak
            withCandidate.zipEveryPair().forEach { a, b in
                let aIndex = graph.addVertex(Node(cell: a))
                let bIndex = graph.addVertex(Node(cell: b))
                graph.addEdge(fromIndex: aIndex, toIndex: bIndex, weight: strength)
            }
        }
    
    //Add groups.
    func createGroups(units: [[Cell]], groupConstructor: ([UnsolvedCell]) -> Node) -> [Node] {
        units.flatMap { unit in
            Dictionary(grouping: unit.unsolvedCells.filter { $0.candidates.contains(candidate) }, by: \.block)
                .values
                .filter { $0.count >= 2 }
                .map(groupConstructor)
        }
    }
    
    let rowGroups = createGroups(units: board.rows, groupConstructor: Node.init(rowGroup:))
    let columnGroups = createGroups(units: board.columns, groupConstructor: Node.init(columnGroup:))
    let groups = rowGroups + columnGroups
    for group in groups {
        _ = graph.addVertex(group)
    }
    
    //Connect groups to cels.
    func connectGroupsToCells(groups: [Node], getUnit: (Int) -> [Cell], getUnitIndex: (Node) -> Int) {
        groups.forEach { group in
            let otherCellsInUnit = getUnit(getUnitIndex(group))
                .unsolvedCells
                .filter { $0.candidates.contains(candidate) && !group.cells.contains($0) }
            let strength = otherCellsInUnit.count == 1 ? Strength.strong : .weak
            otherCellsInUnit.forEach { cell in graph.addEdge(from: group, to: Node(cell: cell), weight: strength) }
        }
    }
    
    connectGroupsToCells(groups: rowGroups, getUnit: board.getRow, getUnitIndex: \.row!)
    connectGroupsToCells(groups: columnGroups, getUnit: board.getColumn, getUnitIndex: \.column!)
    connectGroupsToCells(groups: groups, getUnit: board.getBlock, getUnitIndex: \.block)
    
    //Connect groups to groups.
    func connectGroupsToGroups(groups: [Node], getUnit: (Int) -> [Cell], getUnitIndex: (Node) -> Int) {
        groups.zipEveryPair()
            .filter { a, b in getUnitIndex(a) == getUnitIndex(b) && Set(a.cells).intersection(b.cells).isEmpty }
            .forEach { a, b in
                let otherCellsInUnit = getUnit(getUnitIndex(a))
                    .unsolvedCells
                    .filter { $0.candidates.contains(candidate) && !a.cells.contains($0) && !b.cells.contains($0) }
                let strength = otherCellsInUnit.isEmpty ? Strength.strong : .weak
                graph.addEdge(from: a, to: b, weight: strength)
            }
    }
    
    connectGroupsToGroups(groups: rowGroups, getUnit: board.getRow, getUnitIndex: \.row!)
    connectGroupsToGroups(groups: columnGroups, getUnit: board.getColumn, getUnitIndex: \.column!)
    connectGroupsToGroups(groups: groups, getUnit: board.getBlock, getUnitIndex: \.block)
    
    return graph
}

/*
 * The structure of Node in Swfit is significantly different from the structure of Node in the JVM languages.
 * Originally, I tried to mimic the structure that I have in Kotlin: Node and Group would be protocols while CellNode,
 * RowGroup, and ColumnGroup would be structs. When I tried this, I was met with an unexpected error when trying to
 * create a graph with a vertex type of Node. The error was on the call to the initializer
 * WeightedUniqueElementsGraph<Node, Strength>() and had the message, "Type 'any Node' cannot conform to 'Decodable'.
 * Only concrete types such as structs, enums and classes can conform to protocols."
 *
 * This message really confused me. My protocol Node was declared to extend Codable which means that every concrete type
 * that conforms to Node also conforms to Decodable, so why does Node itself not conform to Decodable. Luckily, the
 * error message told me how to address this issue. I should simply use a vertex type that is a concrete type instead of
 * a protocol. However, I didn't understand why this was necessary. The intiuitive approach that works in the JVM
 * languages doesn't work here.
 *
 * I then found the explaination I was looking for here:
 * https://github.com/swiftlang/swift/blob/main/userdocs/diagnostics/protocol-type-non-conformance.md
 *
 * One of the key differences between protocols in Swift and interfaces in Java is that Swift protocols can declare
 * required initializers that concrete types must implement. I can imagine that this can be useful as it allows generic
 * functions and generic types to create instances of the generic type without knowing what the actual type will be, at
 * least not until the compiler fills in the actual type. Trying to do this in Java would require passing a
 * java.lang.Class object and then using reflection to call a constructor whose presence can only be checked for at
 * runtime.
 *
 * While allowing protocols to declare initilizers has some benefit, it introduces a restriction that I am running into
 * in this case. The protocol Decodable specifies an initializer and a graph type can use that initializer to create an
 * instance of the vertex type. When the vertex type is a concrete type, this works fine. However, when I try to specify
 * Node as the vertex type, it then becomes ambiguous which specific subtype of Node should be created when the required
 * initializer is called. Should it create a CellNode, a RowGroup, or a ColumnGroup? For this reason and a few others,
 * this is why a protocol type cannot be used as a vertex type for a graph.
 *
 * All of this makes sense when I take time to think about Swift protocols, but it is extremely counter-intuitive when
 * coming to Swift from a JVM background.
 */
struct Node: Codable, Equatable {
    let type: NodeType
    let row: Int?
    let column: Int?
    let block: Int
    let cells: [UnsolvedCell]
    
    init(cell: UnsolvedCell) {
        type = .cell
        row = cell.row
        column = cell.column
        block = cell.block
        cells = [cell]
    }
    
    init(rowGroup cells: [UnsolvedCell]) {
        precondition(
            (2 ... unitSizeSquareRoot).contains(cells.count),
            "Group can only be constructed with 2 or \(unitSizeSquareRoot) cells, but cells.count is \(cells.count)."
        )
        precondition(Set(cells.map(\.block)).count == 1, "Group cells must be in the same block.")
        precondition(Set(cells.map(\.row)).count == 1, "rowGroup cells must be in the same row.")
        type = .rowGroup
        row = cells.first!.row
        column = nil
        block = cells.first!.block
        self.cells = cells
    }
    
    init(columnGroup cells: [UnsolvedCell]) {
        precondition(
            (2 ... unitSizeSquareRoot).contains(cells.count),
            "Group can only be constructed with 2 or \(unitSizeSquareRoot) cells, but cells.count is \(cells.count)."
        )
        precondition(Set(cells.map(\.block)).count == 1, "Group cells must be in the same block.")
        precondition(Set(cells.map(\.column)).count == 1, "columnGroup cells must be in the same column.")
        type = .columnGroup
        row = nil
        column = cells.first!.column
        block = cells.first!.block
        self.cells = cells
    }
}

enum NodeType : Codable {
    case cell, rowGroup, columnGroup
}

extension Node: CustomStringConvertible {
    var description: String {
        let coordinates = cells.map { "[\($0.row),\($0.column)]" }.joined(separator: ", ")
        return cells.count == 1 ? coordinates : "{\(coordinates)}"
    }
}
