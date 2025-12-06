use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, LocatedCandidate, UnsolvedCell},
    collections::IteratorZipExtRef,
    sudoku_number::SudokuNumber,
};
use std::collections::BTreeSet;

pub fn y_wing(board: &Board<Cell>) -> Vec<BoardModification> {
    fn try_hinge<'a>(
        board: &'a Board<Cell>,
        hinge: &UnsolvedCell,
        wing_a: &UnsolvedCell,
        wing_b: &UnsolvedCell,
    ) -> impl Iterator<Item = LocatedCandidate<'a>> {
        let mut wing_candidates = wing_a.candidates().intersection(wing_b.candidates());
        let removals = if hinge.is_in_same_unit(wing_a)
            && hinge.is_in_same_unit(wing_b)
            && hinge.candidates().intersection(wing_a.candidates()).count() == 1
            && hinge.candidates().intersection(wing_b.candidates()).count() == 1
            && let Some(&candidate) = wing_candidates.next()
            && let None = wing_candidates.next()
        {
            let removals = board
                .cells()
                .unsolved_cells()
                .filter(move |&cell| {
                    cell != wing_a
                        && cell != wing_b
                        && cell.candidates().contains(&candidate)
                        && cell.is_in_same_unit(wing_a)
                        && cell.is_in_same_unit(wing_b)
                })
                .map(move |cell| (cell, candidate));
            Some(removals)
        } else {
            None
        };
        removals.into_iter().flatten()
    }

    board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() == 2)
        .zip_every_triple()
        .filter(|(a, b, c)| {
            let mut union: BTreeSet<SudokuNumber> = BTreeSet::new();
            union.extend(a.candidates());
            union.extend(b.candidates());
            union.extend(c.candidates());
            union.len() == 3
        })
        .flat_map(|(a, b, c)| {
            try_hinge(board, a, b, c)
                .chain(try_hinge(board, b, a, c))
                .chain(try_hinge(board, c, a, b))
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
            9{38}{1368}24{1378}{57}{58}{568}\
            {478}5{48}69{78}231\
            {13678}2{1368}{18}5{1378}{47}9{468}\
            {1468}9{14568}7{16}{48}32{458}\
            {148}{48}29356{148}7\
            {13468}7{134568}{48}{16}29{1458}{458}\
            {48}69{145}2{14}{1458}73\
            51{348}{34}79{48}62\
            2{34}7{1345}86{145}{45}9\
        ";
        let expected = [
            remove_candidates!(1, 0, 8),
            remove_candidates!(2, 0, 8),
            remove_candidates!(7, 2, 4),
        ];
        assertions::assert_logical_solution(&expected, board, y_wing);
    }

    #[test]
    fn test_2() {
        let board = "\
            65{379}{3478}{347}{34789}1{37}2\
            2{19}8{16}{37}{169}4{37}5\
            {13}4{137}52{137}896\
            {149}36{124}8{14}{29}57\
            {49}8{59}{2347}6{3457}{29}1{34}\
            72{15}{134}9{1345}68{34}\
            574912368\
            {39}{69}2{368}5{368}741\
            8{16}{13}{3467}{347}{3467}529\
        ";
        let expected = [remove_candidates!(7, 1, 9), remove_candidates!(7, 3, 6)];
        assertions::assert_logical_solution(&expected, board, y_wing)
    }

    #[test]
    fn test_3() {
        let board = "\
            {35}{69}28{579}4{79}{3679}1\
            {35}{19}4{179}6{159}2{379}8\
            87{16}32{19}4{69}5\
            923618{57}{57}4\
            4{18}5{279}{79}{29}6{18}3\
            7{168}{16}543{18}29\
            258{19}37{19}46\
            649{12}8{125}3{15}7\
            1374{59}6{589}{589}2\
        ";
        let expected = [remove_candidates!(0, 4, 9), remove_candidates!(1, 7, 9)];
        assertions::assert_logical_solution(&expected, board, y_wing)
    }

    #[test]
    fn test_4() {
        let board = "\
            {46}9172385{46}\
            7{346}{24}851{346}{269}{469}\
            {25}{35}8469{13}{12}7\
            {15}73248{16}{169}{569}\
            {125}8{25}396{147}{17}{45}\
            {46}{46}9175283\
            917684532\
            8{45}{45}932{67}{67}1\
            326517948\
        ";
        let expected = [
            remove_candidates!(1, 8, 4),
            remove_candidates!(3, 7, 1, 6),
            remove_candidates!(4, 6, 7),
            remove_candidates!(7, 6, 6),
            remove_candidates!(7, 7, 7),
        ];
        assertions::assert_logical_solution(&expected, board, y_wing)
    }
}
