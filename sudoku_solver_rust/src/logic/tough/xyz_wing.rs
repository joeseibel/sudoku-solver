use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::BTreeSet;

// http://www.sudokuwiki.org/XYZ_Wing
//
// Given a hinge cell and two wing cells such that the hinge can see both wings, the hinge has three candidates, the
// wings have two candidates each, there is one candidate shared among all three cells, two candidates are shared
// between the hinge and one wing and two candidates between the hinge and the other wing, and the common candidate is
// the only one shared between the wings, then the common candidate must be the solution to one of the cells. The common
// candidate can be removed from any cell which can see all three cells.
pub fn xyz_wing(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() == 3)
        .flat_map(|hinge| {
            board
                .cells()
                .unsolved_cells()
                .zip_every_pair()
                .filter(|(wing_a, wing_b)| {
                    wing_a.candidates().len() == 2
                        && wing_b.candidates().len() == 2
                        && hinge.is_in_same_unit(wing_a)
                        && hinge.is_in_same_unit(wing_b)
                        && {
                            let mut union: BTreeSet<SudokuNumber> = BTreeSet::new();
                            union.extend(hinge.candidates());
                            union.extend(wing_a.candidates());
                            union.extend(wing_b.candidates());
                            union.len() == 3
                        }
                })
                .flat_map(move |(wing_a, wing_b)| {
                    let mut candidates = wing_a.candidates().intersection(wing_b.candidates());
                    if let Some(&candidate) = candidates.next()
                        && candidates.next().is_none()
                    {
                        let removals = board
                            .cells()
                            .unsolved_cells()
                            .filter(move |&cell| {
                                cell != hinge
                                    && cell != wing_a
                                    && cell != wing_b
                                    && cell.candidates().contains(&candidate)
                                    && cell.is_in_same_unit(hinge)
                                    && cell.is_in_same_unit(wing_a)
                                    && cell.is_in_same_unit(wing_b)
                            })
                            .map(move |cell| (cell, candidate));
                        Some(removals)
                    } else {
                        None
                    }
                })
        })
        .flatten()
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            {38}92{46}{48}175{346}\
            5{134}{1467}2{47}{679}{346}{19}8\
            {146}{148}{1467}{4569}3{56789}2{19}{46}\
            {38}75{13}{128}496{12}\
            2{38}{14}{139}6{89}{148}75\
            {14}697{125}{258}{148}3{124}\
            {146}{145}8{1456}9{567}{1356}2{1367}\
            7{1245}{146}{1456}{1245}3{156}89\
            9{125}38{1257}{2567}{156}4{167}\
        ";
        let expected = [remove_candidates!(5, 6, 1)];
        assertions::assert_logical_solution(&expected, board, xyz_wing);
    }

    #[test]
    fn text_2() {
        let board = "\
            6{79}{13479}{57}{23457}{234}{125}{145}8\
            5{13}{134}9{2346}8{1246}{146}7\
            82{47}{567}{4567}1{4569}3{69}\
            34{567}2{15}9{67}8{16}\
            2{79}{5679}{15}8{46}3{467}{169}\
            18{69}3{46}7{469}25\
            75{138}4{136}{36}{168}92\
            9{136}{1238}{1678}{12367}5{1678}{167}4\
            4{16}{128}{1678}9{26}{15678}{1567}3\
        ";
        let expected = [remove_candidates!(4, 8, 6)];
        assertions::assert_logical_solution(&expected, board, xyz_wing);
    }

    #[test]
    fn test_3() {
        let board = "\
            9{46}{46}1{38}{38}{25}{25}7\
            312{45}7{45}986\
            785962314\
            1{26}9{58}{58}74{26}3\
            5{2346}{46}{346}1{3469}{267}{2679}8\
            8{346}72{39}{3469}1{69}5\
            69174{35}8{35}2\
            {24}53{68}{289}{689}{67}{467}1\
            {24}78{356}{235}1{56}{3456}9\
        ";
        let expected = [remove_candidates!(8, 4, 5)];
        assertions::assert_logical_solution(&expected, board, xyz_wing);
    }

    #[test]
    fn test_4() {
        let board = "\
            9{47}{2347}85{467}{24}{36}1\
            85{34}2{69}1{49}{36}7\
            61{247}{49}3{47}{249}58\
            4{69}5{369}78{36}12\
            281{34569}{69}{456}{36}7{49}\
            73{69}{469}1258{49}\
            1{79}{79}{56}2{56}843\
            3{46}{46}189725\
            528743196\
        ";
        let expected = [remove_candidates!(3, 3, 9), remove_candidates!(4, 3, 9)];
        assertions::assert_logical_solution(&expected, board, xyz_wing);
    }
}
