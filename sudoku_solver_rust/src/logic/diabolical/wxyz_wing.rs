use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt},
    collections::IteratorZipExt,
};
use std::collections::HashSet;

// https://www.sudokuwiki.org/WXYZ_Wing
//
// WXYZ-Wing applies for a quad of unsolved cells that has a total of four candidates among the quad. The quad may
// contain restricted candidates and non-restricted candidates. A restricted candidate is one in which each cell of the
// quad with the candidate can see every other cell of the quad with the candidate. A non-restricted candidate is one in
// which at least one cell of the quad with the candidate cannot see every other cell of the quad with the candidate. If
// a quad contains exactly one non-restricted candidate, then that candidate must be the solution to one of the cells of
// the quad. The non-restricted candidate can be removed from any cell outside the quad that can see every cell of the
// quad with the candidate.
pub fn wxyz_wing(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() <= 4)
        .zip_every_quad()
        .flat_map(|(a, b, c, d)| {
            let quad = [a, b, c, d];
            let mut candidates = HashSet::new();
            for cell in quad {
                candidates.extend(cell.candidates());
            }
            if candidates.len() == 4 {
                let mut non_restricted_iter = candidates.iter().filter(|candidate| {
                    quad.iter()
                        .filter(|cell| cell.candidates().contains(candidate))
                        .zip_every_pair()
                        .any(|(a, b)| !a.is_in_same_unit(b))
                });
                if let Some(&non_restricted) = non_restricted_iter.next()
                    && non_restricted_iter.next().is_none()
                {
                    let with_candidate: Vec<_> = quad
                        .iter()
                        .filter(|cell| cell.candidates().contains(&non_restricted))
                        .collect();
                    let removals = board
                        .cells()
                        .unsolved_cells()
                        .filter(|cell| {
                            cell.candidates().contains(&non_restricted)
                                && !quad.contains(cell)
                                && with_candidate.iter().all(|with_candidate_cell| {
                                    cell.is_in_same_unit(with_candidate_cell)
                                })
                        })
                        .map(|cell| (cell, non_restricted))
                        .collect::<Vec<_>>()
                        .into_iter();
                    Some(removals)
                } else {
                    None
                }
            } else {
                None
            }
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
            {1689}{169}{189}{1589}2473{158}\
            54{189}37{89}26{18}\
            237{1568}{15}{568}{159}{189}4\
            7{12569}{1259}{59}3{259}84{156}\
            {69}{2569}3481{59}{279}{567}\
            {19}84{579}6{2579}{159}{12}3\
            3{12}{128}{1678}{14}{678}{46}59\
            {148}7{158}{568}93{46}{18}2\
            {1489}{159}62{145}{58}3{178}{178}\
        ";
        let expected = [remove_candidates!(3, 1, 9), remove_candidates!(7, 2, 1)];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_2() {
        let board = "\
            {1689}{169}{189}{1589}2473{158}\
            54{189}37{89}26{18}\
            237{1568}{15}{568}{159}{189}4\
            7{1256}{1259}{59}3{259}84{156}\
            {69}{2569}3481{59}{279}{567}\
            {19}84{579}6{2579}{159}{12}3\
            3{12}{128}{1678}{14}{678}{46}59\
            {148}7{158}{1568}93{46}{18}2\
            {1489}{159}62{145}{58}3{178}{178}\
        ";
        let expected = [remove_candidates!(7, 2, 1)];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_3() {
        let board = "\
            {18}6{89}{189}24735\
            54{189}37{89}26{18}\
            237{168}5{68}{19}{89}4\
            7{15}{12}{59}3{29}846\
            6{25}3481{59}{29}7\
            984{57}6{27}{15}{12}3\
            3{12}{28}{678}{14}{678}{46}59\
            {148}75{68}93{46}{18}2\
            {48}962{14}537{18}\
        ";
        let expected = [
            remove_candidates!(1, 2, 1),
            remove_candidates!(6, 4, 1),
            remove_candidates!(8, 4, 4),
        ];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_4() {
        let board = "\
            842{56}{56}3719\
            {67}{679}31{789}4{568}{258}{26}\
            5{679}1{27}{2789}{289}{68}34\
            {69}38{26}1{269}475\
            {49}2{45}3{59}7168\
            1{56}7{456}{4568}{568}293\
            3{58}6{2457}{2457}{25}9{458}1\
            {47}{578}{45}931{568}{258}{26}\
            2198{456}{56}3{45}7\
        ";
        let expected = [
            remove_candidates!(5, 4, 6),
            remove_candidates!(5, 5, 5),
            remove_candidates!(6, 4, 5),
            remove_candidates!(8, 4, 5),
        ];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_5() {
        let board = "\
            842{56}{56}3719\
            {67}{679}31{789}4{568}{258}{26}\
            5{679}1{27}{2789}{289}{68}34\
            {69}38{26}1{269}475\
            {49}2{45}3{59}7168\
            1{56}7{456}{4568}{68}293\
            3{58}6{247}{247}{25}9{458}1\
            {47}{578}{45}931{568}{258}{26}\
            2198{46}{56}3{45}7\
        ";
        let expected = [remove_candidates!(5, 4, 6)];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_6() {
        let board = "\
            {2457}{135}{1257}{3467}{2367}{247}8{3456}9\
            {2479}{39}8{34679}{23679}5{13}{346}{136}\
            {459}6{59}1{389}{489}7{345}2\
            {589}{589}{2569}{679}{15679}3{1259}{279}4\
            {259}7{23569}{69}4{19}{12359}8{135}\
            14{359}2{5789}{789}6{379}{357}\
            3{589}4{789}{279}6{259}1{578}\
            {789}2{179}5{1379}{179}4{3679}{3678}\
            6{1589}{1579}{34789}{12379}{12479}{2359}{2379}{3578}\
        ";
        let expected = [
            remove_candidates!(1, 6, 3),
            remove_candidates!(1, 7, 3),
            remove_candidates!(1, 8, 3),
        ];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_7() {
        let board = "\
            96{48}{258}{28}137{45}\
            {134}{14}2{35}9786{45}\
            {38}5764{38}{12}{129}{19}\
            {18}2{1368}{3489}5{3489}7{189}{169}\
            7{489}{348}1{38}65{89}2\
            5{19}{168}{289}7{289}43{1689}\
            {1248}7{14}{2348}6{2348}95{138}\
            {24}35{2489}1{2489}6{28}7\
            6{18}97{238}5{12}4{13}\
        ";
        let expected = [
            remove_candidates!(3, 3, 8),
            remove_candidates!(3, 5, 8),
            remove_candidates!(4, 1, 8),
            remove_candidates!(5, 2, 8),
        ];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }

    #[test]
    fn test_8() {
        let board = "\
            96{48}{258}{28}137{45}\
            {134}{14}2{35}9786{45}\
            {38}5764{38}{12}{129}{19}\
            {18}2{1368}{349}5{349}7{189}{169}\
            7{489}{348}1{38}65{89}2\
            5{19}{16}{289}7{289}43{1689}\
            {1248}7{14}{2348}6{2348}95{138}\
            {24}35{2489}1{2489}6{28}7\
            6{18}97{238}5{12}4{13}\
        ";
        let expected = [
            remove_candidates!(3, 7, 9),
            remove_candidates!(3, 8, 9),
            remove_candidates!(4, 1, 8),
        ];
        assertions::assert_logical_solution(&expected, board, wxyz_wing);
    }
}
