extension Collection {
    func zipEveryPair() -> [(Element, Element)] {
        combinations(ofCount: 2).map { pair in (pair[0], pair[1]) }
    }
    
    func zipEveryTriple() -> [(Element, Element, Element)] {
        combinations(ofCount: 3).map { triple in (triple[0], triple[1], triple[2]) }
    }
    
    func zipEveryQuad() -> [(Element, Element, Element, Element)] {
        combinations(ofCount: 4).map { quad in (quad[0], quad[1], quad[2], quad[3]) }
    }
}
