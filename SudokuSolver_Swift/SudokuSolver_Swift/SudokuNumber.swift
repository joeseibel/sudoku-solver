enum SudokuNumber: Character, CaseIterable {
    case one = "1"
    case two = "2"
    case three = "3"
    case four = "4"
    case five = "5"
    case six = "6"
    case seven = "7"
    case eight = "8"
    case nine = "9"
    
    init(number: Character) {
        guard let number = SudokuNumber(rawValue: number) else {
            preconditionFailure(#"Invalid character: "\#(number)", must be between "1" and "9"."#)
        }
        self = number
    }
}

func parse(optionalBoard board: String) -> Board<SudokuNumber?> {
    precondition(board.count == unitSizeSquared, "board count is \(board.count), must be \(unitSizeSquared).")
    let board = Array(board)
    let numbers = stride(from: 0, to: board.count, by: unitSize).map { rowIndex in
        board[rowIndex ..< rowIndex + unitSize].map { $0 == "0" ? nil : SudokuNumber(number: $0) }
    }
    return Board(elements: numbers)
}

func parse(board: String) -> Board<SudokuNumber> {
    precondition(board.count == unitSizeSquared, "board count is \(board.count), must be \(unitSizeSquared).")
    let board = Array(board)
    let numbers = stride(from: 0, to: board.count, by: unitSize).map { rowIndex in
        board[rowIndex ..< rowIndex + unitSize].map { SudokuNumber(number: $0) }
    }
    return Board(elements: numbers)
}