import Algorithms

enum SudokuNumber: Character, CaseIterable, Codable {
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

extension SudokuNumber: CustomStringConvertible {
    var description: String {
        String(rawValue)
    }
}

extension SudokuNumber: Comparable {
    static func < (lhs: SudokuNumber, rhs: SudokuNumber) -> Bool {
        lhs.rawValue < rhs.rawValue
    }
}

extension Board<SudokuNumber?> {
    init(optionalBoard board: String) {
        precondition(board.count == unitSizeSquared, "board count is \(board.count), must be \(unitSizeSquared).")
        self.init(elements: board.chunks(ofCount: unitSize).map { row in
            row.map { $0 == "0" ? nil : SudokuNumber(number: $0)}
        })
    }
}

extension Board<SudokuNumber> {
    init(board: String) {
        precondition(board.count == unitSizeSquared, "board count is \(board.count), must be \(unitSizeSquared).")
        self.init(elements: board.chunks(ofCount: unitSize).map { row in row.map { SudokuNumber(number: $0) } })
    }
}
