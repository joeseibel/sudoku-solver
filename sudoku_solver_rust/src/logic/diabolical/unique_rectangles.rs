use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt, SetValue},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::{BTreeSet, HashSet};
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Unique_Rectangles
//
// The Unique Rectangles solution works by identifying the potential for an invalid pattern of candidates called the
// Deadly Pattern and then removing candidates that if set as the value would lead to the Deadly Pattern. A Deadly
// Pattern is defined as a group of four unsolved cells arranged to form a rectangle, each cell containing the same two
// candidates and only those candidates, and the cells being located in two rows, two columns, and two blocks. If a
// board contains the Deadly Pattern, then the board cannot have a single solution, but would have multiple solutions.
// The advantage of recognizing this pattern comes when a board contains a pattern which is close to the Deadly Pattern
// and the removal of certain candidates would lead to the Deadly Pattern. If a valid board contains a pattern which is
// close to the Deadly Pattern, it is known that the board will never enter into the Deadly Pattern and candidates can
// be removed if setting those candidates as values would lead to the Deadly Pattern. A rectangle can be further
// described by identifying its floor cells and its roof cells. A rectangle's floor are the cells that only contain the
// two common candidates. A rectangle's roof are the cells that contain the two common candidates as well as additional
// candidates.
//
// Type 1
//
// If a rectangle has one roof cell, then this is a potential Deadly Pattern. If the additional candidates were to be
// removed from the roof, then that would lead to a Deadly Pattern. The two common candidates can be removed from the
// roof leaving only the additional candidates remaining.
pub fn unique_rectangles_type_1(board: &Board<Cell>) -> Vec<BoardModification> {
    create_rectangles(board)
        .flat_map(|rectangle| {
            if let [roof] = rectangle.roof()[..] {
                let removals = rectangle
                    .common_candidates()
                    .iter()
                    .map(|&candidate| (roof, candidate))
                    .collect::<Vec<_>>()
                    .into_iter();
                Some(removals)
            } else {
                None
            }
        })
        .flatten()
        .merge_to_remove_candidates()
}

// Type 2
//
// If a rectangle has two roof cells and there is only one additional candidate appearing in both roof cells, then this
// is a potential Deadly Pattern. If the additional candidate were to be removed from the roof cells, then that would
// lead to a Deadly Pattern, therefore the additional candidate must be the solution for one of the two roof cells. The
// common candidate can be removed from any other cell that can see both of the roof cells.
pub fn unique_rectangles_type_2(board: &Board<Cell>) -> Vec<BoardModification> {
    create_rectangles(board)
        .flat_map(|rectangle| {
            if let [roof_a, roof_b] = rectangle.roof()[..]
                && roof_a.candidates().len() == 3
                && roof_a.candidates() == roof_b.candidates()
            {
                let mut additional_candidates = roof_a.candidates().clone();
                let [common_candidate_a, common_candidate_b] = rectangle.common_candidates();
                additional_candidates.remove(common_candidate_a);
                additional_candidates.remove(common_candidate_b);
                assert!(additional_candidates.len() == 1);
                let additional_candidate = additional_candidates.into_iter().next().unwrap();
                let removals = board
                    .cells()
                    .unsolved_cells()
                    .filter(|&cell| {
                        cell.candidates().contains(&additional_candidate)
                            && cell != roof_a
                            && cell != roof_b
                            && cell.is_in_same_unit(roof_a)
                            && cell.is_in_same_unit(roof_b)
                    })
                    .map(|cell| (cell, additional_candidate))
                    .collect::<Vec<_>>()
                    .into_iter();
                Some(removals)
            } else {
                None
            }
        })
        .flatten()
        .merge_to_remove_candidates()
}

// Type 3
//
// If a rectangle has two roof cells, each roof cell has one additional candidate, and the additional candidates are
// different, then this is a potential Deadly Pattern. One or both of these additional candidates must be the solution,
// so the roof cells can be treated as a single cell with the two additional candidates. If there is another cell that
// can see both roof cells and has the additional candidates as its candidates, then the roof cells and the other cell
// effectively form a Naked Pair. The additional candidates can be removed from any other cell in the unit.
pub fn unique_rectangles_type_3(board: &Board<Cell>) -> Vec<BoardModification> {
    create_rectangles(board)
        .flat_map(|rectangle| {
            if let [roof_a, roof_b] = rectangle.roof()[..]
                && roof_a.candidates().len() == 3
                && roof_b.candidates().len() == 3
                && roof_a.candidates() != roof_b.candidates()
            {
                let mut additional_candidates = roof_a.candidates().clone();
                additional_candidates.extend(roof_b.candidates());
                let [common_candidate_a, common_candidate_b] = rectangle.common_candidates();
                additional_candidates.remove(common_candidate_a);
                additional_candidates.remove(common_candidate_b);

                fn get_removals<'a, U: IteratorCellExt<'a>>(
                    roof_a: &UnsolvedCell,
                    roof_b: &UnsolvedCell,
                    additional_candidates: &BTreeSet<SudokuNumber>,
                    get_unit_index: impl Fn(&UnsolvedCell) -> usize,
                    get_unit: impl FnOnce(usize) -> U,
                ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                    let index_a = get_unit_index(roof_a);
                    let index_b = get_unit_index(roof_b);
                    let removals = if index_a == index_b {
                        let unit: Vec<_> = get_unit(index_a).unsolved_cells().collect();
                        unit.iter()
                            .find(|cell| cell.candidates() == additional_candidates)
                            .map(|&pair_cell| {
                                unit.iter()
                                    .filter(|&&cell| {
                                        cell != pair_cell && cell != roof_a && cell != roof_b
                                    })
                                    .flat_map(|&cell| {
                                        cell.candidates()
                                            .intersection(additional_candidates)
                                            .map(move |&candidate| (cell, candidate))
                                    })
                                    .collect::<Vec<_>>()
                                    .into_iter()
                            })
                    } else {
                        None
                    };
                    removals.into_iter().flatten()
                }

                let row_removals = get_removals(
                    roof_a,
                    roof_b,
                    &additional_candidates,
                    Location::row,
                    |index| board.get_row(index),
                );
                let column_removals = get_removals(
                    roof_a,
                    roof_b,
                    &additional_candidates,
                    Location::column,
                    |index| board.get_column(index),
                );
                let block_removals = get_removals(
                    roof_a,
                    roof_b,
                    &additional_candidates,
                    UnsolvedCell::block,
                    |index| board.get_block(index),
                );
                let removals = row_removals
                    .chain(column_removals)
                    .chain(block_removals)
                    .collect::<Vec<_>>()
                    .into_iter();
                Some(removals)
            } else {
                None
            }
        })
        .flatten()
        .merge_to_remove_candidates()
}

// Type 3/3b with Triple Pseudo-Cells
//
// If a rectangle has two roof cells, then this is a potential Deadly Pattern. If the roof cells can see two other cells
// and the union of candidates among the roof cells' additional candidates and the other cells' candidates is three
// candidates, then the roof cells and the other two cells effectively form a Naked Triple. The three candidates in the
// union can be removed from any other cell in the unit.
pub fn unique_rectangles_type_3_b_with_triple_pseudo_cells(
    board: &Board<Cell>,
) -> Vec<BoardModification> {
    create_rectangles(board)
        .flat_map(|rectangle| {
            if let [roof_a, roof_b] = rectangle.roof()[..] {
                let mut additional_candidates = roof_a.candidates().clone();
                additional_candidates.extend(roof_b.candidates());
                let [common_candidate_a, common_candidate_b] = rectangle.common_candidates();
                additional_candidates.remove(common_candidate_a);
                additional_candidates.remove(common_candidate_b);

                fn get_removals<'a, U: IteratorCellExt<'a>>(
                    roof_a: &UnsolvedCell,
                    roof_b: &UnsolvedCell,
                    additional_candidates: &BTreeSet<SudokuNumber>,
                    get_unit_index: impl Fn(&UnsolvedCell) -> usize,
                    get_unit: impl FnOnce(usize) -> U,
                ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                    let index_a = get_unit_index(roof_a);
                    let index_b = get_unit_index(roof_b);
                    let removals = if index_a == index_b {
                        let unit: Vec<_> = get_unit(index_a)
                            .unsolved_cells()
                            .filter(|&cell| cell != roof_a && cell != roof_b)
                            .collect();
                        let removals = unit
                            .iter()
                            .zip_every_pair()
                            .flat_map(|(triple_a, triple_b)| {
                                let mut triple_candidates = additional_candidates.clone();
                                triple_candidates.extend(triple_a.candidates());
                                triple_candidates.extend(triple_b.candidates());
                                if triple_candidates.len() == 3 {
                                    let removals = unit
                                        .iter()
                                        .filter(move |&cell| cell != triple_a && cell != triple_b)
                                        .flat_map(move |&cell| {
                                            cell.candidates()
                                                .intersection(&triple_candidates)
                                                .map(|&candidate| (cell, candidate))
                                                .collect::<Vec<_>>()
                                                .into_iter()
                                        });
                                    Some(removals)
                                } else {
                                    None
                                }
                            })
                            .flatten()
                            .collect::<Vec<_>>()
                            .into_iter();
                        Some(removals)
                    } else {
                        None
                    };
                    removals.into_iter().flatten()
                }

                let row_removals = get_removals(
                    roof_a,
                    roof_b,
                    &additional_candidates,
                    Location::row,
                    |index| board.get_row(index),
                );
                let column_removals = get_removals(
                    roof_a,
                    roof_b,
                    &additional_candidates,
                    Location::column,
                    |index| board.get_column(index),
                );
                let block_removals = get_removals(
                    roof_a,
                    roof_b,
                    &additional_candidates,
                    UnsolvedCell::block,
                    |index| board.get_block(index),
                );
                let removals = row_removals
                    .chain(column_removals)
                    .chain(block_removals)
                    .collect::<Vec<_>>()
                    .into_iter();
                Some(removals)
            } else {
                None
            }
        })
        .flatten()
        .merge_to_remove_candidates()
}

// Type 4
//
// If a rectangle has two roof cells, then this is a potential Deadly Pattern. For a unit common to the roof cells, if
// one of the common candidates are only found in the roof cells of that unit, then setting the other candidate as the
// solution to one of the roof cells would lead to the Deadly Pattern. The other common candidate can be removed from
// the roof cells.
pub fn unique_rectangles_type_4(board: &Board<Cell>) -> Vec<BoardModification> {
    create_rectangles(board)
        .flat_map(|rectangle| {
            if let roof @ [roof_a, roof_b] = &rectangle.roof()[..] {
                fn get_removals<'a, U: IteratorCellExt<'a>>(
                    roof: &[&'a UnsolvedCell],
                    roof_a: &UnsolvedCell,
                    roof_b: &UnsolvedCell,
                    rectangle: &Rectangle,
                    get_unit_index: impl Fn(&UnsolvedCell) -> usize,
                    get_unit: impl FnOnce(usize) -> U,
                ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                    let index_a = get_unit_index(roof_a);
                    let index_b = get_unit_index(roof_b);
                    let removals = if index_a == index_b {
                        let unit = get_unit(index_a).unsolved_cells().collect();
                        let &[common_candidate_a, common_candidate_b] =
                            rectangle.common_candidates();

                        fn search_unit<'a>(
                            roof: &[&'a UnsolvedCell],
                            unit: &Vec<&UnsolvedCell>,
                            search: SudokuNumber,
                            removal: SudokuNumber,
                        ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                            let removals = if unit
                                .iter()
                                .filter(|cell| cell.candidates().contains(&search))
                                .count()
                                == 2
                            {
                                Some(roof.iter().map(move |&roof_cell| (roof_cell, removal)))
                            } else {
                                None
                            };
                            removals.into_iter().flatten()
                        }

                        let removals =
                            search_unit(roof, &unit, common_candidate_a, common_candidate_b)
                                .chain(search_unit(
                                    roof,
                                    &unit,
                                    common_candidate_b,
                                    common_candidate_a,
                                ))
                                .collect::<Vec<_>>()
                                .into_iter();
                        Some(removals)
                    } else {
                        None
                    };
                    removals.into_iter().flatten()
                }

                let row_removals =
                    get_removals(roof, roof_a, roof_b, &rectangle, Location::row, |index| {
                        board.get_row(index)
                    });
                let column_removals = get_removals(
                    roof,
                    roof_a,
                    roof_b,
                    &rectangle,
                    Location::column,
                    |index| board.get_column(index),
                );
                let block_removals = get_removals(
                    roof,
                    roof_a,
                    roof_b,
                    &rectangle,
                    UnsolvedCell::block,
                    |index| board.get_block(index),
                );
                let removals = row_removals
                    .chain(column_removals)
                    .chain(block_removals)
                    .collect::<Vec<_>>()
                    .into_iter();
                Some(removals)
            } else {
                None
            }
        })
        .flatten()
        .merge_to_remove_candidates()
}

// Type 5
//
// If a rectangle has two floor cells in diagonally opposite corners of the rectangle and one of the common candidates
// only appears in the rectangle for the rows and columns that the rectangle exists in, thus forming strong links for
// the candidate along the four edges of the rectangle, then this is a potential Deadly Pattern. If the non-strong
// link candidate were to be set as the solution to one of the floor cells, then the strong link candidate would have to
// be the solution for the roof cells and the non-strong link candidate would need to be set as the solution to the
// other floor cell, leading to the Deadly Pattern. The non-strong link candidate cannot be the solution to either floor
// cell. Since each floor cell only contains two candidates, this means that the strong link candidate must be the
// solution for the floor cells.
pub fn unique_rectangles_type_5(board: &Board<Cell>) -> Vec<BoardModification> {
    create_rectangles(board)
        .flat_map(|rectangle| {
            if let floor @ [floor_a, floor_b] = &rectangle.floor()[..]
                && floor_a.row() != floor_b.row()
                && floor_a.column() != floor_b.column()
            {
                let modifications = rectangle
                    .common_candidates()
                    .iter()
                    .find(|&&candidate| {
                        fn has_strong_link<'a>(
                            candidate: SudokuNumber,
                            unit: impl IteratorCellExt<'a>,
                        ) -> bool {
                            unit.unsolved_cells()
                                .filter(|cell| cell.candidates().contains(&candidate))
                                .count()
                                == 2
                        }

                        floor.iter().all(|floor_cell| {
                            has_strong_link(candidate, board.get_row(floor_cell.row()))
                                && has_strong_link(candidate, board.get_column(floor_cell.column()))
                        })
                    })
                    .into_iter()
                    .flat_map(|&strong_link_candidate| {
                        floor
                            .iter()
                            .map(|floor_cell| {
                                SetValue::from_cell(floor_cell, strong_link_candidate)
                            })
                            .collect::<Vec<_>>()
                            .into_iter()
                    })
                    .collect::<Vec<_>>()
                    .into_iter();
                Some(modifications)
            } else {
                None
            }
        })
        .flatten()
        .collect()
}

struct Rectangle<'a> {
    cells: [&'a UnsolvedCell; 4],
    common_candidates: [SudokuNumber; 2],
}

impl<'a> Rectangle<'a> {
    fn common_candidates(&self) -> &[SudokuNumber; 2] {
        &self.common_candidates
    }

    fn floor(&self) -> Vec<&'a UnsolvedCell> {
        self.cells
            .into_iter()
            .filter(|cell| cell.candidates().len() == 2)
            .collect()
    }

    fn roof(&self) -> Vec<&'a UnsolvedCell> {
        self.cells
            .into_iter()
            .filter(|cell| cell.candidates().len() > 2)
            .collect()
    }
}

fn create_rectangles(board: &Board<Cell>) -> impl Iterator<Item = Rectangle<'_>> {
    board.rows().zip_every_pair().flat_map(|(row_a, row_b)| {
        row_a
            .zip(row_b)
            .flat_map(|(cell_a, cell_b)| match (cell_a, cell_b) {
                (Cell::UnsolvedCell(cell_a), Cell::UnsolvedCell(cell_b)) => Some((cell_a, cell_b)),
                _ => None,
            })
            .zip_every_pair()
            .flat_map(|((cell_a, cell_b), (cell_c, cell_d))| {
                let cells = [cell_a, cell_b, cell_c, cell_d];
                let mut common_candidates: HashSet<_> = SudokuNumber::iter().collect();
                for cell in cells {
                    common_candidates.retain(|candidate| cell.candidates().contains(candidate));
                }
                common_candidates
                    .into_iter()
                    .collect::<Vec<_>>()
                    .try_into()
                    .ok()
                    .map(|common_candidates| Rectangle {
                        cells,
                        common_candidates,
                    })
            })
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn type_1_test_1() {
        let board = "\
            {79}{79}6324815\
            85{23}691{24}7{34}\
            {24}{234}1785{29}{369}{36}\
            {1259}{129}4{59}3768{29}\
            38{59}{59}62147\
            {29}6741835{29}\
            {24569}{249}{259}173{459}{69}8\
            {579}{379}{359}846{59}21\
            {146}{14}82597{36}{346}\
        ";
        let expected = [remove_candidates!(3, 0, 2, 9)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_1);
    }

    #[test]
    fn type_1_test_2() {
        let board = "\
            {589}{15}{169}{69}3{25}{24578}{1578}{12457}\
            37418{25}{25}69\
            {589}2{169}{69}74{58}3{15}\
            {59}8{139}4{12}7{235}{159}6\
            6{145}{137}8{12}9{23457}{157}{12457}\
            2{14}{179}356{478}{1789}{147}\
            4982631{57}{57}\
            732591648\
            165748923\
        ";
        let expected = [remove_candidates!(0, 6, 2, 5)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_1);
    }

    #[test]
    fn type_1_test_3() {
        let board = "\
            {4569}8{469}72{59}{456}13\
            {345679}{45679}{234679}8{46}1{456}{49}{27}\
            1{45679}{24679}3{46}{59}{4568}{489}{27}\
            {45789}{4579}{4789}2{78}6{48}31\
            {78}315{78}4269\
            {468}2{468}9137{48}5\
            2{46}{46}137958\
            {37}{17}5698{13}24\
            {389}{19}{389}452{13}76\
        ";
        let expected = [remove_candidates!(3, 0, 7, 8)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_1);
    }

    #[test]
    fn type_1_test_4() {
        let board = "\
            12{58}{568}{568}7943\
            96{35}{345}1{34}872\
            47{38}9{238}{238}156\
            3{48}71{468}9{46}25\
            6{48}9{248}{248}5731\
            512{346}7{346}{46}98\
            7{59}1{3568}{35689}{368}2{68}4\
            8{59}6{245}{2459}{24}317\
            2347{68}15{68}9\
        ";
        let expected = [remove_candidates!(6, 4, 6, 8)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_1);
    }

    #[test]
    fn type_2_test_1() {
        let board = "\
            42{157}9{157}{157}386\
            {135}6{135}2{1358}{158}794\
            8{37}9{34}6{47}251\
            7{14}{168}{468}{489}3{19}25\
            9{45}{58}1{478}26{47}3\
            2{134}{136}5{479}{4679}{19}{47}8\
            {13}{139}4{38}2{89}567\
            6827{15}{15}439\
            {35}{579}{57}{346}{349}{469}812\
        ";
        let expected = [remove_candidates!(0, 2, 7), remove_candidates!(2, 5, 7)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_2_test_2() {
        let board = "\
            {145}7{146}9{156}3{456}82\
            {13458}{35689}{13469}{1568}2{168}{345679}{3579}{347}\
            2{35689}{369}4{568}7{3569}{359}1\
            6{34}5{78}{38}21{347}9\
            {13}2{139}{57}{359}4{378}6{378}\
            7{349}8{16}{39}{16}2{34}5\
            9{38}{347}2{148}5{3478}{137}6\
            {3458}{356}2{168}7{168}{34589}{1359}{348}\
            {458}1{467}3{468}9{4578}2{478}\
        ";
        let expected = [
            remove_candidates!(6, 4, 8),
            remove_candidates!(7, 0, 8),
            remove_candidates!(7, 6, 8),
            remove_candidates!(7, 8, 8),
            remove_candidates!(8, 4, 8),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_2_test_3() {
        let board = "\
            {589}{15}{169}{69}3{25}{478}{1578}{12457}\
            37418{25}{25}69\
            {589}2{169}{69}74{58}3{15}\
            {59}8{139}4{12}7{235}{159}6\
            6{145}{137}8{12}9{23457}{157}{12457}\
            2{14}{179}356{478}{1789}{147}\
            4982631{57}{57}\
            732591648\
            165748923\
        ";
        let expected = [
            remove_candidates!(0, 1, 1),
            remove_candidates!(3, 2, 1),
            remove_candidates!(4, 2, 1),
            remove_candidates!(5, 2, 1),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_2_test_4() {
        let board = "\
            319856{47}{47}2\
            245973{16}{16}8\
            {68}7{68}412935\
            98{67}34125{67}\
            {67}34529{167}8{167}\
            1527683{49}{49}\
            {48}6{18}2375{149}{149}\
            {457}23{16}9{45}8{1467}{1467}\
            {457}9{17}{16}8{45}{1467}23\
        ";
        let expected = [
            remove_candidates!(4, 0, 7),
            remove_candidates!(6, 2, 1),
            remove_candidates!(7, 7, 1),
            remove_candidates!(7, 8, 1),
            remove_candidates!(8, 2, 7),
            remove_candidates!(8, 6, 1),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_2_test_5() {
        let board = "\
            654728{19}{139}{39}\
            321964{58}7{58}\
            978315642\
            8{34}7652{49}{39}1\
            1{36}{256}497{258}{236}{358}\
            {245}9{256}831{2457}{26}{457}\
            {247}83{12}{47}9{1247}56\
            {247}{146}{269}5{47}3{12479}8{479}\
            {457}{14}{59}{12}863{129}{479}\
        ";
        let expected = [remove_candidates!(5, 0, 2), remove_candidates!(7, 2, 2)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_2_b_test() {
        let board = "\
            {27}4186539{27}\
            {278}9{257}{13}4{13}{578}6{278}\
            {68}3{56}7924{58}1\
            {36}28{135}{357}{137}94{56}\
            519624{78}{78}3\
            {346}7{46}9{35}821{56}\
            15{347}{34}8{37}629\
            {247}6{247}{45}19{578}3{478}\
            98{347}2{357}61{57}{47}\
        ";
        let expected = [remove_candidates!(1, 6, 8)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_2_c_test() {
        let board = "\
            8{247}9{234}{146}{26}{37}5{1367}\
            53{16}8{146}7{269}{1246}{29}\
            {146}{24}{167}{2345}9{256}8{146}{1367}\
            2946{57}813{57}\
            78{36}9{235}1{256}{26}4\
            {36}15{27}{237}4{267}98\
            {1349}{47}2{457}8{569}{35}{16}{13569}\
            {49}581{46}3{29}7{269}\
            {139}6{137}{257}{27}{259}48{1359}\
        ";
        let expected = [remove_candidates!(0, 8, 6), remove_candidates!(2, 8, 6)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_2);
    }

    #[test]
    fn type_3_test() {
        let board = "\
            {69}{69}{128}5{12}347{18}\
            5{37}{12}8{127}4{139}6{39}\
            4{37}{18}{17}96{138}52\
            857{123}{13}96{12}4\
            3246{18}759{18}\
            {19}{19}6{24}{48}5{28}37\
            285{37}61{379}4{39}\
            {167}{16}9{347}{347}8{1237}{12}5\
            {17}43952{17}86\
        ";
        let expected = [remove_candidates!(2, 6, 1), remove_candidates!(7, 6, 1, 7)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_3);
    }

    #[test]
    fn type_3_b_test() {
        let board = "\
            419{78}2{37}{3578}{35}6\
            {25}6{25}1{378}9{3478}{34}{347}\
            {78}3{78}465921\
            {56}9{345}2{37}1{3467}8{3457}\
            {38}{48}1{678}5{367}29{347}\
            {256}7{2358}9{38}4{36}1{35}\
            {138}{48}65{14}2{134}79\
            {17}5{47}398{14}62\
            92{34}{67}{14}{67}{1345}{345}8\
        ";
        let expected = [
            remove_candidates!(3, 4, 3),
            remove_candidates!(4, 1, 8),
            remove_candidates!(4, 8, 3),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_3);
    }

    #[test]
    fn type_3_b_with_triple_pseudo_cells_test_1() {
        let board = "\
            7529{36}8{16}4{136}\
            3{48}{48}21{56}{79}{79}{56}\
            {69}1{69}{3457}{345}{357}28{35}\
            {145}63{457}82{47}{17}9\
            {14}27{346}9{36}5{16}8\
            8{49}{459}1{456}{567}{467}32\
            271{356}{356}98{56}4\
            {469}{489}{4689}{56}213{569}7\
            {569}3{569}874{169}2{16}\
        ";
        let expected = [remove_candidates!(7, 0, 6, 9)];
        assertions::assert_logical_solution(
            &expected,
            board,
            unique_rectangles_type_3_b_with_triple_pseudo_cells,
        );
    }

    #[test]
    fn type_3_b_with_triple_pseudo_cells_test_2() {
        let board = "\
            654728{19}{139}{39}\
            321964{58}7{58}\
            978315642\
            8{34}7652{49}{39}1\
            1{36}{256}497{258}{236}{358}\
            {45}9{256}831{2457}{26}{457}\
            {247}83{12}{47}9{1247}56\
            {247}{146}{69}5{47}3{12479}8{479}\
            {457}{14}{59}{12}863{129}{479}\
        ";
        let expected = [
            remove_candidates!(3, 7, 3),
            remove_candidates!(4, 2, 2, 6),
            remove_candidates!(5, 6, 2),
        ];
        assertions::assert_logical_solution(
            &expected,
            board,
            unique_rectangles_type_3_b_with_triple_pseudo_cells,
        );
    }

    #[test]
    fn type_4_test_1() {
        let board = "\
            {79}{79}6324815\
            85{23}691{24}7{34}\
            {24}{234}1785{29}{369}{36}\
            {15}{129}4{59}3768{29}\
            38{59}{59}62147\
            {29}6741835{29}\
            {24569}{249}{259}173{459}{69}8\
            {579}{379}{359}846{59}21\
            {146}{14}82597{36}{346}\
        ";
        let expected = [remove_candidates!(7, 0, 9), remove_candidates!(7, 1, 9)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_4_test_2() {
        let board = "\
            {4569}8{469}72{59}{456}13\
            {345679}{45679}{234679}8{46}1{456}{49}{27}\
            1{45679}{24679}3{46}{59}{4568}{489}{27}\
            {459}{4579}{4789}2{78}6{48}31\
            {78}315{78}4269\
            {468}2{468}9137{48}5\
            2{46}{46}137958\
            {37}{17}5698{13}24\
            {389}{19}{389}452{13}76\
        ";
        let expected = [remove_candidates!(1, 2, 7), remove_candidates!(2, 2, 7)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_4_test_3() {
        let board = "\
            12{58}{568}{568}7943\
            96{35}{345}1{34}872\
            47{38}9{238}{238}156\
            3{48}71{468}9{46}25\
            6{48}9{248}{248}5731\
            512{346}7{346}{46}98\
            7{59}1{3568}{359}{368}2{68}4\
            8{59}6{245}{2459}{24}317\
            2347{68}15{68}9\
        ";
        let expected = [remove_candidates!(6, 4, 5), remove_candidates!(7, 4, 5)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_4_test_4() {
        let board = "\
            31{789}2{789}{89}645\
            {26789}{78}5{3679}4{689}1{28}{37}\
            {2678}4{678}{1367}5{168}9{28}{37}\
            {167}32{1567}{17}{1456}{47}98\
            {178}5{478}{179}{1789}{12489}{247}36\
            {678}9{4678}{67}3{2468}{247}51\
            421863579\
            {59}63{59}27814\
            {5789}{78}{789}4{19}{159}362\
        ";
        let expected = [
            remove_candidates!(1, 0, 8),
            remove_candidates!(1, 3, 7),
            remove_candidates!(2, 0, 8),
            remove_candidates!(2, 3, 7),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_4_test_5() {
        let board = "\
            173924{58}{58}6\
            249865317\
            856173429\
            96{28}{237}5{278}{178}4{138}\
            73{28}41{268}{68}95\
            514{37}9{68}{678}{378}2\
            6{28}1{27}49{2578}{3578}{38}\
            4{289}563{27}{12789}{78}{18}\
            3{29}7581{29}64\
        ";
        let expected = [
            remove_candidates!(6, 6, 8),
            remove_candidates!(6, 7, 8),
            remove_candidates!(7, 1, 2),
            remove_candidates!(7, 6, 2),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_4_b_test_1() {
        let board = "\
            748359126\
            {359}{59}172684{39}\
            {39}264{18}{18}7{39}5\
            2{56}{39}{169}4{15}{359}87\
            {56}74{689}3{258}{259}{169}{19}\
            18{39}{69}7{25}{25}{369}4\
            4{39}2{15}{19}76{1359}8\
            {69}17{58}{689}34{59}2\
            8{369}52{169}4{39}7{139}\
        ";
        let expected = [remove_candidates!(4, 5, 5), remove_candidates!(4, 6, 5)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_4_b_test_2() {
        let board = "\
            173924{58}{58}6\
            249865317\
            856173429\
            96{28}{237}5{278}{178}4{138}\
            73{28}41{268}{68}95\
            514{37}9{68}{678}{378}2\
            6{28}1{27}49{257}{357}{38}\
            4{289}563{27}{12789}{78}{18}\
            3{29}7581{29}64\
        ";
        let expected = [remove_candidates!(7, 1, 2), remove_candidates!(7, 6, 2)];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_4);
    }

    #[test]
    fn type_5_test_1() {
        let board = "\
            7{589}{3589}4{259}6{28}{238}1\
            {49}2{369}8{179}{17}{467}{367}5\
            1{4568}{568}3{257}{27}{24678}9{2467}\
            3{169}4{279}{127}5{2678}{2678}{267}\
            {289}7{56}{29}3{28}{456}1{46}\
            {28}{15}{125}6{1247}{12478}3{257}9\
            {249}3{1279}5{2467}{247}{19}{267}8\
            5{189}{12789}{27}{2678}3{19}4{267}\
            6{48}{278}1{2478}9{257}{257}3\
        ";
        let expected = [
            SetValue::from_indices(4, 5, 8),
            SetValue::from_indices(5, 0, 8),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_5);
    }

    #[test]
    fn type_5_test_2() {
        let board = "\
            {267}{1267}895{167}3{167}4\
            {567}94{17}3{167}{28}{1567}{28}\
            {567}{367}{13}824{17}{1567}9\
            {246}5{12}3{147}{17}{678}9{78}\
            387269541\
            {469}{16}{19}5{147}8{67}23\
            1{47}5{467}8293{67}\
            {279}{237}{239}{167}{17}548{267}\
            8{247}6{47}93{127}{17}5\
        ";
        let expected = [
            SetValue::from_indices(6, 1, 4),
            SetValue::from_indices(8, 3, 4),
        ];
        assertions::assert_logical_solution(&expected, board, unique_rectangles_type_5);
    }
}
