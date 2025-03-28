/*
 * Swift implements Algebraic Data Types as Enumerations with Associated Values. A disadvantage with the way that Swift
 * implements ADTs is that the specific cases of an enum are not types themselves. This is different from Kotlin in
 * which ADTs are implements as sealed classes or interfaces with limited numbers of direct subtypes. In Kotlin, each
 * subtype of a sealed type is a type itself.
 *
 * Originally, I wanted to define the cases of the Cell enum with the individual fields within the case itself. This
 * would have consisted of "case solvedCell(row: Int, column: Int, value: SudokuNumber)" and
 * "case unsolvedCell(row: Int, column: Int, candidates: Set<SudokuNumber>)". This is the natural way of defining an
 * Enumeration with Associated Values as described in the Swift book. However, this approach creates two problems:
 *
 * 1. There is no way to validate the values used to initialize each case. There is nothing to prevent solvedCell from
 *    being initialized with a row of 42. I could have added an initializer to Cell to perform such validation, but
 *    there is no way to enforce the use of such an initializer.
 *
 * 2. The cases are not types themselves, so it is impossible to write a function that takes a solvedCell as a
 *    parameter. The only parameter type permitted is Cell.
 *
 * I considered trying to tolerate these limitations, but decided against it. Instead of including all of the values
 * directly in the cases, I opted to create the structs SolvedCell and UnsolvedCell and then have the cases contain
 * instances of these structs. This solves the first problem since I was able to validate the values in the initializer
 * of each struct. It also solves the second problem as there are now the explicit types Cell, SolvedCell, and
 * UnsolvedCell.
 *
 * One drawback of this approach is that pattern matching can no longer be used to extract the individual values from a
 * SolvedCell or UnsolvedCell.
 *
 * Overall, I believe this is a limitation in the way that Swift implements ADTs. I prefer the approach found in Kotlin,
 * Java, and Scala in which the members of each ADT are types themselves.
 */
enum Cell: Location, Equatable {
    case solvedCell(SolvedCell)
    case unsolvedCell(UnsolvedCell)
    
    init(row: Int, column: Int, value: SudokuNumber) {
        self = .solvedCell(SolvedCell(row: row, column: column, value: value))
    }
    
    init(row: Int, column: Int, candidates: Set<SudokuNumber> = Set(SudokuNumber.allCases)) {
        self = .unsolvedCell(UnsolvedCell(row: row, column: column, candidates: candidates))
    }
    
    var row: Int {
        switch self {
        case .solvedCell(let cell):
            cell.row
        case .unsolvedCell(let cell):
            cell.row
        }
    }
    
    var column: Int {
        switch self {
        case .solvedCell(let cell):
            cell.column
        case .unsolvedCell(let cell):
            cell.column
        }
    }
    
    var block: Int {
        switch self {
        case .solvedCell(let cell):
            cell.block
        case .unsolvedCell(let cell):
            cell.block
        }
    }
}

extension Cell: CustomStringConvertible {
    var description: String {
        switch self {
        case .solvedCell(let cell):
            String(describing: cell)
        case .unsolvedCell(let cell):
            String(describing: cell)
        }
    }
}

/*
 * Some of the logical solutions require the ability to get the row or column from a Cell or UnsolvedCell. Since Cell,
 * SolvedCell, and UnsolvedCell are all independent types, I decided to create this Location protocol so that the row
 * and column values can be retrieved regardless of whether the cell is a Cell or an UnsolvedCell. Currently, SolvedCell
 * does not conform to Location because that functionality has not been needed.
 */
protocol Location {
    var row: Int { get }
    var column: Int { get }
}

struct SolvedCell: Equatable {
    let row: Int
    let column: Int
    let block: Int
    let value: SudokuNumber
    
    fileprivate init(row: Int, column: Int, value: SudokuNumber) {
        validateRowAndColumn(row: row, column: column)
        self.row = row
        self.column = column
        block = getBlockIndex(rowIndex: row, columnIndex: column)
        self.value = value
    }
}

extension SolvedCell: CustomStringConvertible {
    var description: String {
        String(describing: value)
    }
}

struct UnsolvedCell: Location, Hashable, Codable {
    let row: Int
    let column: Int
    let block: Int
    let candidates: Set<SudokuNumber>
    
    fileprivate init(row: Int, column: Int, candidates: Set<SudokuNumber>) {
        validateRowAndColumn(row: row, column: column)
        precondition(!candidates.isEmpty, "candidates must not be empty.")
        self.row = row
        self.column = column
        block = getBlockIndex(rowIndex: row, columnIndex: column)
        self.candidates = candidates
    }
    
    func isInSameUnit(as other: UnsolvedCell) -> Bool {
        row == other.row || column == other.column || block == other.block
    }
    
    var vertexLabel: String {
        "[\(row),\(column)]"
    }
}

extension UnsolvedCell: CustomStringConvertible {
    var description: String {
        "0"
    }
}

typealias LocatedCandidate = (UnsolvedCell, SudokuNumber)

extension String {
    init(simpleBoard: Board<Cell>) {
        self = simpleBoard.cells.map(String.init).joined()
    }
    
    init(withCandidates: Board<Cell>) {
        self = withCandidates.rows
            .map { row in
                row.map { cell in
                    switch cell {
                    case .solvedCell(let cell):
                        String(describing: cell.value)
                    case .unsolvedCell(let cell):
                        "{\(cell.candidates.sorted(by: <).map(String.init(describing:)).joined())}"
                    }
                }.joined()
            }
            .joined(separator: "\n")
    }
}

extension Board<Cell> {
    init(toCellBoard board: Board<SudokuNumber?>) {
        self = board.mapCellsIndexed { row, column, cell in
            if let cell {
                Cell(row: row, column: column, value: cell)
            } else {
                Cell(row: row, column: column)
            }
        }
    }
    
    init(simpleBoard board: String) {
        precondition(board.count == unitSizeSquared, "simpleBoard.count is \(board.count), must be \(unitSizeSquared).")
        self.init(elements: board.chunks(ofCount: unitSize).enumerated().map { rowIndex, row in
            row.enumerated().map { columnIndex, cell in
                if cell == "0" {
                    Cell(row: rowIndex, column: columnIndex)
                } else {
                    Cell(row: rowIndex, column: columnIndex, value: SudokuNumber(number: cell))
                }
            }
        })
    }
    
    init(withCandidates: String) {
        var cellBuilders: [(_ row: Int, _ column: Int) -> Cell] = []
        var index = withCandidates.startIndex
        while index < withCandidates.endIndex {
            let ch = withCandidates[index]
            switch ch {
            case "{":
                index = withCandidates.index(after: index)
                guard let closingBrace = withCandidates[index...].firstIndex(of: "}") else {
                    preconditionFailure("Unmatched \"{\".")
                }
                precondition(closingBrace != index, "Empty \"{}\".")
                let charsInBraces = withCandidates[index..<closingBrace]
                precondition(!charsInBraces.contains("{"), "Nested \"{\".")
                let candidates = Set(charsInBraces.map { SudokuNumber(number: $0) })
                cellBuilders.append({ row, column in Cell(row: row, column: column, candidates: candidates) })
                index = withCandidates.index(after: closingBrace)
            case "}":
                preconditionFailure("Unmatched \"}\".")
            default:
                let value = SudokuNumber(number: ch)
                cellBuilders.append({ row, column in Cell(row: row, column: column, value: value)})
                index = withCandidates.index(after: index)
            }
        }
        precondition(
            cellBuilders.count == unitSizeSquared,
            "Found \(cellBuilders.count) cells, required \(unitSizeSquared)."
        )
        self.init(elements: cellBuilders.chunks(ofCount: unitSize).enumerated().map { rowIndex, row in
            row.enumerated().map { columnIndex, cell in cell(rowIndex, columnIndex) }
        })
    }
}

extension [Cell] {
    var solvedCells: [SolvedCell] {
        self.compactMap {
            switch $0 {
            case .solvedCell(let solvedCell):
                solvedCell
            case .unsolvedCell:
                nil
            }
        }
    }
    
    var unsolvedCells: [UnsolvedCell] {
        self.compactMap {
            switch $0 {
            case .solvedCell:
                nil
            case .unsolvedCell(let unsolvedCell):
                unsolvedCell
            }
        }
    }
}
