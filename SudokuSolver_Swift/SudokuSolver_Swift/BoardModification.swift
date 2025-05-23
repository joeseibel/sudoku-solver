//The use of Swift's Enumerations with Associated Values follows the same pattern that was used for Cell.
enum BoardModification: Hashable {
    case removeCandidates(RemoveCandidates)
    case setValue(SetValue)
    
    var row: Int {
        switch self {
        case .removeCandidates(let removeCandidates):
            removeCandidates.row
        case .setValue(let setValue):
            setValue.row
        }
    }
    
    var column: Int {
        switch self {
        case .removeCandidates(let removeCandidates):
            removeCandidates.column
        case .setValue(let setValue):
            setValue.column
        }
    }
    
    init(cell: UnsolvedCell, candidates: Set<SudokuNumber>) {
        self = .removeCandidates(RemoveCandidates(row: cell.row, column: cell.column, candidates: candidates))
        for candidate in candidates {
            precondition(
                cell.candidates.contains(candidate),
                "\(candidate) is not a candidate for [\(row), \(column)]."
            )
        }
    }
    
    init(row: Int, column: Int, candidates: Int...) {
        let candidates = Set(candidates.map { SudokuNumber.allCases[$0 - 1] })
        self = .removeCandidates(RemoveCandidates(row: row, column: column, candidates: candidates))
    }
    
    init(cell: UnsolvedCell, value: SudokuNumber) {
        self = .setValue(SetValue(row: cell.row, column: cell.column, value: value))
        precondition(cell.candidates.contains(value), "\(value) is not a candidate for [\(row), \(column)].")
    }
    
    init(row: Int, column: Int, value: Int) {
        self = .setValue(SetValue(row: row, column: column, value: SudokuNumber.allCases[value - 1]))
    }
}

struct RemoveCandidates: Hashable {
    let row: Int
    let column: Int
    let candidates: Set<SudokuNumber>
    
    fileprivate init(row: Int, column: Int, candidates: Set<SudokuNumber>) {
        validateRowAndColumn(row: row, column: column)
        precondition(!candidates.isEmpty, "candidates must not be empty.")
        self.row = row
        self.column = column
        self.candidates = candidates
    }
}

struct SetValue: Hashable {
    let row: Int
    let column: Int
    let value: SudokuNumber
    
    fileprivate init(row: Int, column: Int, value: SudokuNumber) {
        validateRowAndColumn(row: row, column: column)
        self.row = row
        self.column = column
        self.value = value
    }
}

extension Sequence<LocatedCandidate> {
    /*
     * The receiver represents a list of numbers that should be removed from specific cells. This helper function allows
     * the logic functions to focus on simply marking the numbers to be removed, then at the end use this function to
     * produce at most one RemoveCandidates per cell.
     */
    func mergeToRemoveCandidates() -> [BoardModification] {
        Dictionary(grouping: self, by: { cell, _ in cell })
            .mapValues { $0.map { _, candidate in candidate } }
            .map { cell, candidates in BoardModification(cell: cell, candidates: Set(candidates)) }
    }
}
