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
 * Overall, I believe this is a limitation in the way that Swift implements ADTs. I prefer the approach found in Kotlin,
 * Java, and Scala in which the members of each ADT are types themselves.
 */
enum Cell: Equatable {
    case solvedCell(SolvedCell)
    case unsolvedCell(UnsolvedCell)
}

struct SolvedCell: Equatable {
    let row: Int
    let column: Int
    let value: SudokuNumber
    
    init(row: Int, column: Int, value: SudokuNumber) {
        validateRowAndColumn(row: row, column: column)
        self.row = row
        self.column = column
        self.value = value
    }
}

struct UnsolvedCell: Equatable {
    let row: Int
    let column: Int
    let candidates: Set<SudokuNumber>
    
    init(row: Int, column: Int, candidates: Set<SudokuNumber> = Set(SudokuNumber.allCases)) {
        validateRowAndColumn(row: row, column: column)
        self.row = row
        self.column = column
        self.candidates = candidates
    }
}

private func validateRowAndColumn(row: Int, column: Int) {
    precondition((0 ..< unitSize).contains(row), "row is \(row), must be between 0 and \(unitSize - 1).")
    precondition((0 ..< unitSize).contains(column), "column is \(column), must be between 0 and \(unitSize - 1).")
}

extension Board<Cell> {
    init(toCellBoard board: Board<SudokuNumber?>) {
        self = board.mapCellsIndexed { row, column, cell in
            if let cell {
                .solvedCell(SolvedCell(row: row, column: column, value: cell))
            } else {
                .unsolvedCell(UnsolvedCell(row: row, column: column))
            }
        }
    }
    
    init(simpleBoard board: String) {
        precondition(board.count == unitSizeSquared, "simpleBoard.count is \(board.count), must be \(unitSizeSquared).")
        let board = Array(board)
        let numbers: [[Cell]] = (0 ..< unitSize).map { rowIndex in
            board[rowIndex * unitSize ..< rowIndex * unitSize + unitSize].enumerated().map { columnIndex, cell in
                if cell == "0" {
                    .unsolvedCell(UnsolvedCell(row: rowIndex, column: columnIndex))
                } else {
                    .solvedCell(SolvedCell(row: rowIndex, column: columnIndex, value: SudokuNumber(number: cell)))
                }
            }
        }
        self.init(elements: numbers)
    }
}
