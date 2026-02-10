use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, Location, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::HashSet;
use strum::IntoEnumIterator;

// https://www.sudokuwiki.org/Jelly_Fish_Strategy
//
// For a quad of rows, if a candidate appears in two, three, or four cells for each row and the candidate appears in
// exactly four columns across the four rows, forming a four by four grid, then the candidate must be placed in four of
// the sixteen cells. The candidate can be removed from cells which are in the four columns, but different rows.
//
// For a quad of columns, if a candidate appears in two, three, or four cells for each column and the candidate appears
// in exactly four rows across the four columns, forming a four by four grid, then the candidate must be placed in four
// of the sixteen cells. The candidate can be removed from cells which are in the four rows, but different columns
pub fn jellyfish(board: &Board<Cell>) -> Vec<BoardModification> {
    SudokuNumber::iter()
        .flat_map(|candidate| {
            fn jellyfish<'a, U: Iterator<Item = &'a Cell> + Clone, Z: IteratorCellExt<'a>>(
                candidate: SudokuNumber,
                units: impl Iterator<Item = impl Iterator<Item = &'a Cell>> + IteratorZipExt<Z>,
                get_other_unit: impl Fn(usize) -> U,
                get_other_unit_index: impl Fn(&UnsolvedCell) -> usize,
            ) -> impl Iterator<Item = LocatedCandidate<'a>> {
                units
                    .zip_every_quad()
                    .flat_map(move |(unit_a, unit_b, unit_c, unit_d)| {
                        let a_with_candidate: Vec<_> = unit_a
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        let b_with_candidate: Vec<_> = unit_b
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        let c_with_candidate: Vec<_> = unit_c
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        let d_with_candidate: Vec<_> = unit_d
                            .unsolved_cells()
                            .filter(|cell| cell.candidates().contains(&candidate))
                            .collect();
                        if (2..=4).contains(&a_with_candidate.len())
                            && (2..=4).contains(&b_with_candidate.len())
                            && (2..=4).contains(&c_with_candidate.len())
                            && (2..=4).contains(&d_with_candidate.len())
                        {
                            let with_candidate: HashSet<_> = a_with_candidate
                                .iter()
                                .chain(b_with_candidate.iter())
                                .chain(c_with_candidate.iter())
                                .chain(d_with_candidate.iter())
                                .collect();
                            let other_unit_indices: HashSet<_> = with_candidate
                                .iter()
                                .map(|cell| get_other_unit_index(cell))
                                .collect();
                            if other_unit_indices.len() == 4 {
                                let removals = other_unit_indices
                                    .iter()
                                    .flat_map(|&other_unit_index| get_other_unit(other_unit_index))
                                    .unsolved_cells()
                                    .filter(|cell| {
                                        cell.candidates().contains(&candidate)
                                            && !with_candidate.contains(cell)
                                    })
                                    .map(|cell| (cell, candidate))
                                    .collect::<Vec<_>>();
                                Some(removals)
                            } else {
                                None
                            }
                        } else {
                            None
                        }
                    })
                    .flatten()
            }

            let row_removals = jellyfish(
                candidate,
                board.rows(),
                |index| board.get_column(index),
                Location::column,
            );
            let column_removals = jellyfish(
                candidate,
                board.columns(),
                |index| board.get_row(index),
                Location::row,
            );
            row_removals.chain(column_removals)
        })
        .merge_to_remove_candidates()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            {249}{469}17538{2469}{269}\
            {23489}5{23489}{12}{14}{246}{236}{2469}7\
            7{346}{234}89{246}1{2456}{2356}\
            {3489}{3489}{3489}6{23}157{28}\
            625478931\
            {38}179{23}54{268}{268}\
            {123589}{389}{2389}{125}67{23}{2589}4\
            {234589}7{23489}{25}{48}{24}{236}1{235689}\
            {12458}{48}63{148}97{258}{258}\
        ";
        let expected = [
            remove_candidates!(1, 0, 2),
            remove_candidates!(1, 7, 2),
            remove_candidates!(2, 7, 2),
            remove_candidates!(2, 8, 2),
            remove_candidates!(6, 0, 2),
            remove_candidates!(6, 7, 2),
            remove_candidates!(7, 0, 2),
            remove_candidates!(7, 8, 2),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish);
    }

    #[test]
    fn test_2() {
        let board = "\
            {234568}{2368}{368}{46789}{178}{4689}{178}{58}{4578}\
            {4568}7{68}{468}3{1468}92{1458}\
            {48}19{478}2563{478}\
            {3689}{368}4{5678}{578}{368}21{35789}\
            {23689}{2368}{1368}{24568}{1578}{23468}{78}{58}{35789}\
            {1238}57{28}9{1238}46{38}\
            {68}9514{28}37{26}\
            7{368}{1368}{2589}{58}{289}{18}4{26}\
            {18}4236759{18}\
        ";
        let expected = [
            remove_candidates!(0, 0, 8),
            remove_candidates!(0, 2, 8),
            remove_candidates!(0, 3, 8),
            remove_candidates!(0, 5, 8),
            remove_candidates!(0, 8, 8),
            remove_candidates!(1, 0, 8),
            remove_candidates!(1, 3, 8),
            remove_candidates!(1, 5, 8),
            remove_candidates!(1, 8, 8),
            remove_candidates!(3, 0, 8),
            remove_candidates!(3, 3, 8),
            remove_candidates!(3, 5, 8),
            remove_candidates!(3, 8, 8),
            remove_candidates!(4, 0, 8),
            remove_candidates!(4, 2, 8),
            remove_candidates!(4, 3, 8),
            remove_candidates!(4, 5, 8),
            remove_candidates!(4, 8, 8),
            remove_candidates!(7, 2, 8),
            remove_candidates!(7, 3, 8),
            remove_candidates!(7, 5, 8),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_3() {
        let board = "\
            {123}5{123}749{126}8{126}\
            {124}89{56}{256}3{47}{257}{1245}\
            6{27}{247}{58}{258}139{245}\
            {2389}4{2358}{13589}{3589}7{125}6{1235}\
            {1237}{237}{123567}4{356}{26}8{257}9\
            {23789}{2379}{235678}{135689}{35689}{268}{47}{257}{12345}\
            {2789}6{278}{389}{3789}4{259}1{258}\
            5{39}{38}21{68}{69}47\
            {24789}1{2478}{689}{6789}5{269}3{268}\
        ";
        let expected = [
            remove_candidates!(1, 0, 2),
            remove_candidates!(1, 8, 2),
            remove_candidates!(2, 2, 2),
            remove_candidates!(2, 8, 2),
            remove_candidates!(4, 0, 2),
            remove_candidates!(4, 2, 2),
            remove_candidates!(5, 0, 2),
            remove_candidates!(5, 2, 2),
            remove_candidates!(5, 8, 2),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_4() {
        let board = "\
            {245689}{259}{69}{13456789}{3689}{456789}{19}{15679}{79}\
            {4569}17{4569}2{4569}8{569}3\
            {5689}{569}3{156789}{689}{56789}2{15679}4\
            {19}84{29}537{129}6\
            {1356}{56}{169}{246789}{689}{246789}{1349}{123489}{89}\
            {369}72{4689}1{4689}{349}{3489}5\
            {69}48{369}715{39}2\
            {279}35{289}4{289}6{789}1\
            {12679}{269}{169}{235689}{3689}{25689}{349}{34789}{789}\
        ";
        let expected = [
            remove_candidates!(0, 0, 9),
            remove_candidates!(0, 3, 9),
            remove_candidates!(0, 5, 9),
            remove_candidates!(0, 6, 9),
            remove_candidates!(0, 7, 9),
            remove_candidates!(2, 0, 9),
            remove_candidates!(2, 3, 9),
            remove_candidates!(2, 5, 9),
            remove_candidates!(2, 7, 9),
            remove_candidates!(4, 3, 9),
            remove_candidates!(4, 5, 9),
            remove_candidates!(4, 6, 9),
            remove_candidates!(4, 7, 9),
            remove_candidates!(5, 0, 9),
            remove_candidates!(5, 3, 9),
            remove_candidates!(5, 5, 9),
            remove_candidates!(5, 7, 9),
            remove_candidates!(8, 0, 9),
            remove_candidates!(8, 3, 9),
            remove_candidates!(8, 5, 9),
            remove_candidates!(8, 6, 9),
            remove_candidates!(8, 7, 9),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_5() {
        let board = "\
            {3589}4{589}{289}7{268}{2569}1{2359}\
            {1789}{19}235{168}{4679}{469}{79}\
            {13579}6{1579}{249}{24}{12}{2579}8{23579}\
            2{159}{14589}{46}37{14569}{4569}{1589}\
            {58}3{458}1{468}9{4567}2{578}\
            67{1489}{245}{248}{25}3{49}{189}\
            4{15}3{258}9{258}{128}76\
            {159}8{1569}7{26}4{1259}3{1259}\
            {79}2{679}{568}13{589}{59}4\
        ";
        let expected = [
            remove_candidates!(3, 2, 5),
            remove_candidates!(3, 6, 5),
            remove_candidates!(3, 8, 5),
            remove_candidates!(8, 6, 5),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_6() {
        let board = "\
            {245}{24567}{456}{18}{59}{18}{23457}{4567}{3679}\
            8{567}3{59}24{57}1{679}\
            9{245}1376{245}8{245}\
            6{145}7{145}8392{45}\
            {2345}{23458}{45}{24567}{456}91{4567}{3678}\
            {2345}{1234589}{459}{124567}{456}{1257}{34578}{4567}{3678}\
            7{69}8{69}1{25}{245}3{245}\
            {345}{34569}{4569}{2456789}{4569}{2578}{2578}{57}1\
            1{45}2{4578}3{578}69{78}\
        ";
        let expected = [
            remove_candidates!(0, 1, 4, 5),
            remove_candidates!(0, 6, 4, 5),
            remove_candidates!(4, 1, 4, 5),
            remove_candidates!(4, 3, 4, 5),
            remove_candidates!(5, 1, 4, 5),
            remove_candidates!(5, 3, 4, 5),
            remove_candidates!(5, 5, 5),
            remove_candidates!(5, 6, 4, 5),
            remove_candidates!(7, 1, 4, 5),
            remove_candidates!(7, 3, 4, 5),
            remove_candidates!(7, 5, 5),
            remove_candidates!(7, 6, 5),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_7() {
        let board = "\
            14{2358}{2568}{38}{256}{2358}97\
            97{2358}{258}{348}{245}{2358}16\
            {2358}{58}6{79}{19}{179}{23458}{2458}{2458}\
            {28}9145376{28}\
            {25}6{245}1789{245}3\
            73{458}{69}2{69}1{458}{458}\
            {358}{58}{35789}{235789}{19}{124579}6{2458}{2458}\
            42{3589}{3589}6{59}{58}71\
            61{578}{2578}{48}{2457}{2458}39\
        ";
        let expected = [
            remove_candidates!(2, 6, 5, 8),
            remove_candidates!(4, 2, 5),
            remove_candidates!(5, 2, 5, 8),
            remove_candidates!(6, 2, 5, 8),
            remove_candidates!(6, 3, 5, 8),
            remove_candidates!(6, 5, 5),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_8() {
        let board = "\
            {1245678}{145689}{2678}{15789}{1578}{89}{348}{23478}{278}\
            {1578}{158}3{1578}246{78}9\
            {24789}{489}{278}{789}365{2478}1\
            {138}24{38}679{18}5\
            {13689}{13689}{68}{23589}{58}{289}{48}{14678}{678}\
            {689}75{89}412{68}3\
            {268}{68}1{268}93754\
            {234678}{3468}9{24678}{78}51{2368}{268}\
            {2345678}{34568}{2678}{124678}{178}{28}{38}9{268}\
        ";
        let expected = [
            remove_candidates!(0, 0, 8),
            remove_candidates!(0, 1, 8),
            remove_candidates!(0, 2, 8),
            remove_candidates!(0, 3, 8),
            remove_candidates!(0, 7, 8),
            remove_candidates!(2, 0, 8),
            remove_candidates!(2, 1, 8),
            remove_candidates!(2, 3, 8),
            remove_candidates!(2, 7, 8),
            remove_candidates!(4, 0, 8),
            remove_candidates!(4, 1, 8),
            remove_candidates!(4, 2, 8),
            remove_candidates!(4, 3, 8),
            remove_candidates!(4, 7, 8),
            remove_candidates!(7, 0, 8),
            remove_candidates!(7, 1, 8),
            remove_candidates!(7, 3, 8),
            remove_candidates!(7, 7, 8),
            remove_candidates!(8, 0, 8),
            remove_candidates!(8, 1, 8),
            remove_candidates!(8, 2, 8),
            remove_candidates!(8, 3, 8),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }

    #[test]
    fn test_9() {
        let board = "\
            {234567}{3457}{27}{156789}{16789}{178}{678}{34678}{2467}\
            {24567}{457}9{5678}{678}31{24678}{2467}\
            {367}81{67}249{367}5\
            {178}64{178}592{78}3\
            {15789}{57}{78}{123478}{13478}{1278}{678}{46789}{467}\
            {789}23{478}{478}65{4789}1\
            {2478}15{24678}{4678}{278}3{267}9\
            {23478}{347}{278}{12346789}{1346789}{1278}{67}5{267}\
            {237}96{237}{37}5418\
        ";
        let expected = [
            remove_candidates!(0, 0, 7),
            remove_candidates!(0, 3, 7),
            remove_candidates!(0, 4, 7),
            remove_candidates!(0, 5, 7),
            remove_candidates!(0, 7, 7),
            remove_candidates!(1, 0, 7),
            remove_candidates!(1, 3, 7),
            remove_candidates!(1, 4, 7),
            remove_candidates!(1, 7, 7),
            remove_candidates!(4, 0, 7),
            remove_candidates!(4, 3, 7),
            remove_candidates!(4, 4, 7),
            remove_candidates!(4, 5, 7),
            remove_candidates!(4, 7, 7),
            remove_candidates!(6, 0, 7),
            remove_candidates!(6, 3, 7),
            remove_candidates!(6, 4, 7),
            remove_candidates!(6, 7, 7),
            remove_candidates!(7, 0, 7),
            remove_candidates!(7, 3, 7),
            remove_candidates!(7, 4, 7),
            remove_candidates!(7, 5, 7),
        ];
        assertions::assert_logical_solution(&expected, board, jellyfish)
    }
}
