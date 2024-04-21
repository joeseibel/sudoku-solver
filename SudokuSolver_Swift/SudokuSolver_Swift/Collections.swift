extension Collection {
    func zipEveryPair() -> [(Element, Element)] {
        combinations(ofCount: 2).map { pair in (pair[0], pair[1]) }
    }
    
    func zipEveryTriple() -> [(Element, Element, Element)] {
        combinations(ofCount: 3).map { triple in (triple[0], triple[1], triple[2]) }
    }
}
