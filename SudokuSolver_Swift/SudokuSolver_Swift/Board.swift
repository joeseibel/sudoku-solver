let unitSizeSquareRoot = 3
let unitSize = unitSizeSquareRoot * unitSizeSquareRoot
let unitSizeSquared = unitSize * unitSize

struct Board<Element: Equatable>: Equatable {
    private(set) var rows: [[Element]]
    
    init(elements rows: [[Element]]) {
        precondition(rows.count == unitSize, "elements.count is \(rows.count), must be \(unitSize).")
        for (index, row) in rows.enumerated() {
            precondition(row.count == unitSize, "elements[\(index)].count is \(row.count), must be \(unitSize).")
        }
        self.rows = rows
    }
    
    var columns: [[Element]] {
        (0..<unitSize).map { index in rows.map(\.[index]) }
    }
    
    var blocks: [[Element]] {
        (0..<unitSize).map(getBlock)
    }
    
    var units: [[Element]] {
        rows + columns + blocks
    }
    
    var cells: [Element] {
        rows.flatMap { $0 }
    }
    
    subscript(rowIndex: Int, columnIndex: Int) -> Element {
        get {
            rows[rowIndex][columnIndex]
        }
        set {
            rows[rowIndex][columnIndex] = newValue
        }
    }
    
    func getRow(rowIndex: Int) -> [Element] {
        rows[rowIndex]
    }
    
    func getColumn(columnIndex: Int) -> [Element] {
        rows.map(\.[columnIndex])
    }
    
    func getBlock(blockIndex: Int) -> [Element] {
        precondition(
            (0..<unitSize).contains(blockIndex),
            "blockIndex is \(blockIndex), must be between 0 and \(unitSize - 1)."
        )
        let rowIndex = blockIndex / unitSizeSquareRoot * unitSizeSquareRoot
        let columnIndex = blockIndex % unitSizeSquareRoot * unitSizeSquareRoot
        return rows[rowIndex..<rowIndex + unitSizeSquareRoot].flatMap { row in
            row[columnIndex..<columnIndex + unitSizeSquareRoot]
        }
    }
    
    func mapCells<T>(_ transform: (Element) -> T) -> Board<T> {
        Board<T>(elements: rows.map { row in row.map(transform) })
    }
    
    /*
     * I originally thought of creating an enumerated() method instead of mapCellsIndexed(_:). This would have resulted
     * in more of a Swift-style approach at the call site by calling enumerated() and then calling mapCells(_:).
     * However, I ran into a problem when implementing enumerated(). I tried to have enumerated() return a
     * Board<(Int, Int, Element)>, but tuples in Swift do not conform to Equatable. This was surprising because the
     * operator == is available for tuples, but they cannot be used as a type when an Equatable is required. There is a
     * pitch for tuples to conform to Equatable, but there hasn't been much progress on it:
     * https://forums.swift.org/t/tuples-conform-to-equatable/32559
     *
     * Due to this issue, I have decided to stick with the Kotlin-style approach of having a mapCellsIndexed(_:) method.
     */
    func mapCellsIndexed<T>(_ transform: (_ row: Int, _ column: Int, Element) -> T) -> Board<T> {
        Board<T>(elements: rows.enumerated().map { rowIndex, row in
            row.enumerated().map { columnIndex, cell in transform(rowIndex, columnIndex, cell) }
        })
    }
}

extension Board: CustomStringConvertible {
    var description: String {
        func joinRows(fromIndex: Int, toIndex: Int) -> String {
            rows[fromIndex..<toIndex]
                .map { row in
                    func joinCells(fromIndex: Int, toIndex: Int) -> String {
                        /*
                         * Something strange is happening here that I don't really understand. Originally, joinCells
                         * returned this expression:
                         *
                         *   row[fromIndex..<toIndex].map(String.init(describing:)).joined(separator: " ")
                         *
                         * This became a problem after an update of Xcode which resulted in a warning with the message,
                         * "Capture of non-Sendable type 'Element.Type' in an isolated closure". I don't really
                         * understand why this warning is showing up here, but I have found two solutions to the
                         * warning:
                         *
                         * 1. Require that Element conform to Sendable.
                         * 2. Express the argument passed to map as a full closure instead of a reference to
                         *    String.init(describing:).
                         *
                         * The fact that #2 is a possible solution really confuses me because this is only a syntax
                         * change and doesn't impact the symantics of the call to map. I suspect that the warning may be
                         * a false positive, because this entire solver is single threaded and nothing is crossing a
                         * thread boundary here. It would be good for me to better understand Swift concurrency so that
                         * I can truly see if this is indeed a false positive.
                         *
                         * For now, I have opted for solution #2. While solution #1 is a more direct response to the
                         * warning's message, I don't want to require that Element conform to Sendable just because that
                         * should not be needed in a single-threaded program.
                         */
                        row[fromIndex..<toIndex].map { String(describing: $0) }.joined(separator: " ")
                    }
                    
                    let first = joinCells(fromIndex: 0, toIndex: unitSizeSquareRoot)
                    let second = joinCells(fromIndex: unitSizeSquareRoot, toIndex: unitSizeSquareRoot * 2)
                    let third = joinCells(fromIndex: unitSizeSquareRoot * 2, toIndex: unitSize)
                    return "\(first) | \(second) | \(third)"
                }
                .joined(separator: "\n")
        }
        
        return """
            \(joinRows(fromIndex: 0, toIndex: unitSizeSquareRoot))
            ------+-------+------
            \(joinRows(fromIndex: unitSizeSquareRoot, toIndex: unitSizeSquareRoot * 2))
            ------+-------+------
            \(joinRows(fromIndex: unitSizeSquareRoot * 2, toIndex: unitSize))
            """
    }
}

func getBlockIndex(rowIndex: Int, columnIndex: Int) -> Int {
    rowIndex / unitSizeSquareRoot * unitSizeSquareRoot + columnIndex / unitSizeSquareRoot
}

func validateRowAndColumn(row: Int, column: Int) {
    precondition((0..<unitSize).contains(row), "row is \(row), must be between 0 and \(unitSize - 1).")
    precondition((0..<unitSize).contains(column), "column is \(column), must be between 0 and \(unitSize - 1).")
}
