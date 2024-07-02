/*
 * https://www.sudokuwiki.org/Aligned_Pair_Exclusion
 *
 * To understand Aligned Pair Exclusion, it is helpful to first define what an Almost Locked Set is. An ALS is a set of
 * n unsolved cells, all of which can see each other, and there are n + 1 candidates across all n cells. In the simplest
 * case, any unsolved cell with two candidates is an ALS; there is one cell and two candidates. A pair of cells is an
 * ALS if they can see each other and the union of candidates has a size of three. If there are three cells that see
 * each other and there are four candidates across those three cells, then those three cells are an ALS.
 *
 * Aligned Pair Exclusion considers a pair of unsolved cells, which may or may not see each other, and checks for
 * solution combinations for that pair which would cause problems for that pair or for Almost Locks Sets which are
 * visible to that pair. This will result in a list of solution combinations for the pair, some of which are known to be
 * invalid, and the others which could potentially be valid. If a particular candidate in one of the cells of the pair
 * only appears among the invalid combinations, then that candidate cannot be the solution to that cell and can be
 * removed.
 *
 * How is a solution combination for a pair checked for validity? The first simple thing to look at is if the candidates
 * of the combination are the same and the two cells can see each other, then the combination is invalid. If the
 * candidates are not the same, then it is time to look at the ALSs that are visible to both cells of the pair. If a
 * solution combination is a subset of the candidates of a visible ALS, then that combination would cause problems for
 * the ALS and the combination is invalid.
 *
 * The simplest case of checking an ALS is when the ALS has one cell and two candidates. If the solution combination has
 * the same candidates as the ALS, then the solution combination would empty the ALS. This is a very obvious case, but
 * it gets a little more complicated when an ALS has more than one cell and more than two candidates. The link at the
 * start of this comment has some examples with ALSs that have two cells and three cells. It is helpful to walk through
 * these examples to see how a solution combination which is a subset of the candidates of an ALS is invalid.
 */
func alignedPairExclusion(board: Board<Cell>) -> [BoardModification] {
    board.cells.unsolvedCells.zipEveryPair().flatMap { cellA, cellB in
        let almostLockedSets = getAlmostLockedSets(board: board, cellA: cellA, cellB: cellB)
        let (validACandidates, validBCandidates) = cellA.candidates
            .flatMap { candidateA in cellB.candidates.map { candidateB in (candidateA, candidateB) } }
            .filter { candidateA, candidateB in
                if candidateA == candidateB {
                    return !cellA.isInSameUnit(as: cellB)
                } else {
                    let pair = [candidateA, candidateB]
                    return !almostLockedSets.contains { $0.isSuperset(of: pair) }
                }
            }
            .reduce(into: (Set<SudokuNumber>(), Set<SudokuNumber>())) { validCandidates, candidates in
                validCandidates.0.insert(candidates.0)
                validCandidates.1.insert(candidates.1)
            }
        let removalsA = cellA.candidates.filter { !validACandidates.contains($0) }.map { (cellA, $0) }
        let removalsB = cellB.candidates.filter { !validBCandidates.contains($0) }.map { (cellB, $0) }
        return removalsA + removalsB
    }.mergeToRemoveCandidates()
}

private func getAlmostLockedSets(board: Board<Cell>, cellA: UnsolvedCell, cellB: UnsolvedCell) -> [Set<SudokuNumber>] {
    let visible = board.cells
        .unsolvedCells
        .filter { $0 != cellA && $0 != cellB && $0.isInSameUnit(as: cellA) && $0.isInSameUnit(as: cellB) }
    let almostLockedSets1 = visible.map(\.candidates).filter { $0.count == 2 }
    let almostLockedSets2 = visible.zipEveryPair()
        .filter { alsA, alsB in alsA.isInSameUnit(as: alsB) }
        .map { alsA, alsB in alsA.candidates.union(alsB.candidates) }
        .filter { $0.count == 3 }
    let almostLockedSets3 = visible.zipEveryTriple()
        .filter { alsA, alsB, alsC in
            alsA.isInSameUnit(as: alsB) && alsA.isInSameUnit(as: alsC) && alsB.isInSameUnit(as: alsC)
        }
        .map { alsA, alsB, alsC in alsA.candidates.union(alsB.candidates).union(alsC.candidates) }
        .filter { $0.count == 4 }
    let almostLockedSets4 = visible.zipEveryQuad()
        .filter { alsA, alsB, alsC, alsD in
            alsA.isInSameUnit(as: alsB) &&
                alsA.isInSameUnit(as: alsC) &&
                alsA.isInSameUnit(as: alsD) &&
                alsB.isInSameUnit(as: alsC) &&
                alsB.isInSameUnit(as: alsD) &&
                alsC.isInSameUnit(as: alsD)
        }
        .map { alsA, alsB, alsC, alsD in
            alsA.candidates.union(alsB.candidates).union(alsC.candidates).union(alsD.candidates)
        }
        .filter { $0.count == 5 }
    return almostLockedSets1 + almostLockedSets2 + almostLockedSets3 + almostLockedSets4
}
