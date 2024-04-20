extension Collection {
    func zipEveryPair() -> [(Element, Element)] {
        combinations(ofCount: 2).map { pair in (pair[0], pair[1]) }
    }
}
