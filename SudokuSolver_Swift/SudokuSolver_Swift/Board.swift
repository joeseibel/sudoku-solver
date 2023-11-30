private let unitSizeSquareRoot = 3
let unitSize = unitSizeSquareRoot * unitSizeSquareRoot
let unitSizeSquared = unitSize * unitSize

struct Board<Element> {
    private var rows: [[Element]]
    
    init(elements rows: [[Element]]) {
        precondition(rows.count == unitSize, "elements count is \(rows.count), must be \(unitSize).")
        for (index, row) in rows.enumerated() {
            precondition(row.count == unitSize, "elements[\(index)] count is \(row.count), must be \(unitSize).")
        }
        self.rows = rows
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
        rows.map { row in row[columnIndex] }
    }
    
    func getBlock(blockIndex: Int) -> [Element] {
        precondition(
            (0 ..< unitSize).contains(blockIndex),
            "blockIndex is \(blockIndex), must be between 0 and \(unitSize - 1)."
        )
        let rowIndex = blockIndex / unitSizeSquareRoot * unitSizeSquareRoot
        let columnIndex = blockIndex % unitSizeSquareRoot * unitSizeSquareRoot
        return rows[rowIndex ..< rowIndex + unitSizeSquareRoot].flatMap { row in
            row[columnIndex ..< columnIndex + unitSizeSquareRoot]
        }
    }
    
    func mapCells<T>(_ transform: (Element) -> T) -> Board<T> {
        Board<T>(elements: rows.map { row in row.map(transform) })
    }
}

func getBlockIndex(rowIndex: Int, columnIndex: Int) -> Int {
    rowIndex / unitSizeSquareRoot * unitSizeSquareRoot + columnIndex / unitSizeSquareRoot
}
