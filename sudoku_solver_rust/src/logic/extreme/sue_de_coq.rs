use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use itertools::Itertools;
use std::collections::{BTreeSet, HashSet};

// https://www.sudokuwiki.org/Sue_De_Coq
//
// This solution starts with looking for two or three cells in the same linear unit (row or column) and the same block.
// The union of candidates across these cells must have a size which is at least two more than the number of cells. In
// other words, if two cells are selected, then they must have at least four candidates. If three cells are selected,
// they must have at least five candidates. These cells are the main group in this solution.
//
// Once the main group is identified, this solution then searches for an Almost Locked Set in the same linear unit as
// the main group and also for an ALS in the same block as the main group. As a reminder, an ALS is a set of n unsolved
// cells, all of which can see each other, and there are n + 1 candidates across all n cells. The two ALSs can only
// contain candidates found in the main group, they must contain all the candidates of the main group, and there can be
// no common candidates across the two ALSs.
//
// Once we have the main group and the two ALSs, it is then certain that each of the common candidates must appear in
// one of the three groups. Therefore, for any common candidate, that candidate cannot be the solution for any cell
// which can see the main group and can see the ALS that has the candidate. The candidates of the linear unit ALS can be
// removed from other cells of that linear unit which are not a part of the main group. The candidates of the block unit
// ALS can be removed from other cells of that block which are not a part of the main group.
pub fn sue_de_coq(board: &Board<Cell>) -> Vec<BoardModification> {
    fn sue_de_coq<'a>(
        board: &'a Board<Cell>,
        units: impl Iterator<Item = impl Iterator<Item = &'a Cell>>,
        get_unit_index: impl Fn(&UnsolvedCell) -> usize,
    ) -> impl Iterator<Item = LocatedCandidate<'a>> {
        units
            .map(|unit| unit.unsolved_cells())
            .flat_map(|unit| {
                let unit: Vec<_> = unit.collect();
                unit.iter().copied().into_group_map_by(|cell| cell.block()).into_iter().flat_map({
                    let get_unit_index = &get_unit_index;
                    move |(block_index, unit_by_block)| {
                        let other_cells_in_unit: Vec<_> =
                            unit.iter().copied().filter(|cell| cell.block() != block_index).collect();
                        let block: Vec<_> = board.get_block(block_index).unsolved_cells().collect();
                        let other_cells_in_block: Vec<_> = block
                            .iter()
                            .copied()
                            .filter(|cell| get_unit_index(cell) != get_unit_index(unit.first().unwrap()))
                            .collect();

                        fn get_group_removals<'a>(
                            unit: &[&'a UnsolvedCell],
                            other_cells_in_unit: &[&UnsolvedCell],
                            block: &[&'a UnsolvedCell],
                            other_cells_in_block: &[&UnsolvedCell],
                            group: &[&UnsolvedCell],
                        ) -> Vec<LocatedCandidate<'a>> {
                            let candidates: BTreeSet<_> =
                                group.iter().flat_map(|cell| cell.candidates()).copied().collect();
                            if candidates.len() >= group.len() + 2 {
                                get_almost_locked_sets(other_cells_in_unit, &candidates)
                                    .flat_map(|unit_als| {
                                        get_almost_locked_sets(other_cells_in_block, &candidates)
                                            .filter(|block_als| {
                                                unit_als.candidates.len() + block_als.candidates.len()
                                                    == candidates.len()
                                                    && unit_als
                                                        .candidates
                                                        .intersection(&block_als.candidates)
                                                        .next()
                                                        .is_none()
                                            })
                                            .flat_map(|block_als| {
                                                let unit_removals = unit
                                                    .iter()
                                                    .filter(|&cell| {
                                                        !group.contains(cell) && !unit_als.cells.contains(cell)
                                                    })
                                                    .flat_map(|&cell| {
                                                        cell.candidates()
                                                            .intersection(&unit_als.candidates)
                                                            .map(move |&candidate| (cell, candidate))
                                                    });
                                                let block_removals = block
                                                    .iter()
                                                    .filter(|&cell| {
                                                        !group.contains(cell) && !block_als.cells.contains(cell)
                                                    })
                                                    .flat_map(|&cell| {
                                                        cell.candidates()
                                                            .intersection(&block_als.candidates)
                                                            .map(move |&candidate| (cell, candidate))
                                                    });
                                                unit_removals.chain(block_removals).collect::<Vec<_>>()
                                            })
                                            .collect::<Vec<_>>()
                                    })
                                    .collect()
                            } else {
                                Vec::new()
                            }
                        }

                        if unit_by_block.len() == 2 {
                            get_group_removals(
                                &unit,
                                &other_cells_in_unit,
                                &block,
                                &other_cells_in_block,
                                &unit_by_block,
                            )
                        } else if unit_by_block.len() == 3 {
                            let all_three = get_group_removals(
                                &unit,
                                &other_cells_in_unit,
                                &block,
                                &other_cells_in_block,
                                &unit_by_block,
                            );
                            let by_pairs = unit_by_block.iter().zip_every_pair().flat_map(|(&a, b)| {
                                get_group_removals(&unit, &other_cells_in_unit, &block, &other_cells_in_block, &[a, b])
                            });
                            all_three.into_iter().chain(by_pairs).collect()
                        } else {
                            Vec::new()
                        }
                    }
                })
            })
            .collect::<Vec<_>>()
            .into_iter()
    }

    let row_removals = sue_de_coq(board, board.rows(), Location::row);
    let column_removals = sue_de_coq(board, board.columns(), Location::column);
    row_removals.chain(column_removals).merge_to_remove_candidates()
}

struct Als<'a> {
    cells: HashSet<&'a UnsolvedCell>,
    candidates: BTreeSet<SudokuNumber>,
}

fn get_almost_locked_sets<'a>(
    cells: &[&'a UnsolvedCell],
    group_candidates: &BTreeSet<SudokuNumber>,
) -> impl Iterator<Item = Als<'a>> {
    let almost_locked_sets_1 = cells
        .iter()
        .filter(|cell| cell.candidates().len() == 2 && group_candidates.is_superset(cell.candidates()))
        .map(|&cell| {
            let mut cells = HashSet::new();
            cells.insert(cell);
            Als { cells, candidates: cell.candidates().clone() }
        });
    let almost_locked_sets_2 = cells
        .iter()
        .zip_every_pair()
        .map(|(&a, b)| {
            let mut cells = HashSet::new();
            cells.insert(a);
            cells.insert(b);
            Als { cells, candidates: a.candidates() | b.candidates() }
        })
        .filter(|als| als.candidates.len() == 3 && group_candidates.is_superset(&als.candidates));
    let almost_locked_sets_3 = cells
        .iter()
        .zip_every_triple()
        .map(|(&a, b, c)| {
            let mut cells = HashSet::new();
            cells.insert(a);
            cells.insert(b);
            cells.insert(c);
            let mut candidates = a.candidates().clone();
            candidates.extend(b.candidates());
            candidates.extend(c.candidates());
            Als { cells, candidates }
        })
        .filter(|als| als.candidates.len() == 4 && group_candidates.is_superset(&als.candidates));
    let almost_locked_sets_4 = cells
        .iter()
        .zip_every_quad()
        .map(|(&a, b, c, d)| {
            let mut cells = HashSet::new();
            cells.insert(a);
            cells.insert(b);
            cells.insert(c);
            cells.insert(d);
            let mut candidates = a.candidates().clone();
            candidates.extend(b.candidates());
            candidates.extend(c.candidates());
            candidates.extend(d.candidates());
            Als { cells, candidates }
        })
        .filter(|als| als.candidates.len() == 5 && group_candidates.is_superset(&als.candidates));
    almost_locked_sets_1.chain(almost_locked_sets_2).chain(almost_locked_sets_3).chain(almost_locked_sets_4)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            {47}1{34}958{346}{467}2\
            {289}{28}634751{89}\
            5{378}{3489}621{348}{4789}{78}\
            {1289}{258}7{148}6{25}{1489}3{189}\
            6{238}{12389}{1478}{78}{23}{148}{479}5\
            {18}4{35}{178}9{35}2{678}{1678}\
            {1278}{2567}{125}{78}39{16}{268}4\
            {248}9{248}5167{28}3\
            3{678}{18}2{78}4{1689}5{1689}\
        ";
        let expected = [
            remove_candidates!(2, 1, 8),
            remove_candidates!(4, 2, 3),
            remove_candidates!(6, 1, 2),
            remove_candidates!(8, 1, 8),
        ];
        assertions::assert_logical_solution(&expected, board, sue_de_coq);
    }

    #[test]
    fn test_2() {
        let board = "\
            15{78}432{78}69\
            9{27}4186{23}{237}5\
            {26}{268}3{59}7{59}{128}{128}4\
            {567}{69}2{3568}{16}{1578}{3689}4{137}\
            {4567}{68}{158}{3568}9{134578}{136}{1378}2\
            {467}3{189}{68}2{478}5{1789}{17}\
            {25}{279}{59}{3689}{16}{1389}4{137}{137}\
            34675{19}{129}{129}8\
            81{79}24{39}{379}56\
        ";
        let expected = [
            remove_candidates!(3, 6, 3),
            remove_candidates!(4, 0, 6),
            remove_candidates!(4, 2, 8),
            remove_candidates!(4, 3, 6, 8),
            remove_candidates!(4, 5, 8),
            remove_candidates!(5, 7, 1, 7),
        ];
        assertions::assert_logical_solution(&expected, board, sue_de_coq);
    }

    #[test]
    fn test_3() {
        let board = "\
            15{78}432{78}69\
            9{27}4186{23}{237}5\
            {26}{268}3{59}7{59}{128}{128}4\
            {567}{69}2{3568}{16}{1578}{3689}4{137}\
            {4567}{68}{158}{35}9{13457}{136}{1378}2\
            {467}3{189}{68}2{478}5{1789}{17}\
            {25}{279}{59}{3689}{16}{1389}4{137}{137}\
            34675{19}{129}{129}8\
            81{79}24{39}{379}56\
        ";
        let expected = [
            remove_candidates!(3, 6, 3),
            remove_candidates!(4, 0, 6),
            remove_candidates!(4, 2, 8),
            remove_candidates!(5, 7, 1, 7),
        ];
        assertions::assert_logical_solution(&expected, board, sue_de_coq);
    }
}
