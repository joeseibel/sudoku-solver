use crate::{
    board::Board,
    board_modification::{BoardModification, IteratorRemoveCandidatesExt},
    cell::{Cell, IteratorCellExt, UnsolvedCell},
    collections::IteratorZipExt,
    sudoku_number::SudokuNumber,
};
use std::collections::BTreeSet;

// https://www.sudokuwiki.org/Aligned_Pair_Exclusion
//
// To understand Aligned Pair Exclusion, it is helpful to first define what an Almost Locked Set is. An ALS is a set of
// n unsolved cells, all of which can see each other, and there are n + 1 candidates across all n cells. In the simplest
// case, any unsolved cell with two candidates is an ALS; there is one cell and two candidates. A pair of cells is an
// ALS if they can see each other and the union of candidates has a size of three. If there are three cells that see
// each other and there are four candidates across those three cells, then those three cells are an ALS.
//
// Aligned Pair Exclusion considers a pair of unsolved cells, which may or may not see each other, and checks for
// solution combinations for that pair which would cause problems for that pair or for Almost Locks Sets which are
// visible to that pair. This will result in a list of solution combinations for the pair, some of which are known to be
// invalid, and the others which could potentially be valid. If a particular candidate in one of the cells of the pair
// only appears among the invalid combinations, then that candidate cannot be the solution to that cell and can be
// removed.
//
// How is a solution combination for a pair checked for validity? The first simple thing to look at is if the candidates
// of the combination are the same and the two cells can see each other, then the combination is invalid. If the
// candidates are not the same, then it is time to look at the ALSs that are visible to both cells of the pair. If a
// solution combination is a subset of the candidates of a visible ALS, then that combination would cause problems for
// the ALS and the combination is invalid.
//
// The simplest case of checking an ALS is when the ALS has one cell and two candidates. If the solution combination has
// the same candidates as the ALS, then the solution combination would empty the ALS. This is a very obvious case, but
// it gets a little more complicated when an ALS has more than one cell and more than two candidates. The link at the
// start of this comment has some examples with ALSs that have two cells and three cells. It is helpful to walk through
// these examples to see how a solution combination which is a subset of the candidates of an ALS is invalid.
pub fn aligned_pair_exclusion(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .cells()
        .unsolved_cells()
        .zip_every_pair()
        .flat_map(|(cell_a, cell_b)| {
            let almost_locked_sets = get_almost_locked_sets(board, cell_a, cell_b);
            let (valid_a_candidates, valid_b_candidates): (Vec<SudokuNumber>, Vec<SudokuNumber>) =
                cell_a
                    .candidates()
                    .iter()
                    .flat_map(|&candidate_a| {
                        cell_b
                            .candidates()
                            .iter()
                            .map(move |&candidate_b| (candidate_a, candidate_b))
                    })
                    .filter(|&(candidate_a, candidate_b)| {
                        if candidate_a == candidate_b {
                            !cell_a.is_in_same_unit(cell_b)
                        } else {
                            let mut pair = BTreeSet::new();
                            pair.insert(candidate_a);
                            pair.insert(candidate_b);
                            !almost_locked_sets.iter().any(|als| als.is_superset(&pair))
                        }
                    })
                    .unzip();
            let removals_a = cell_a
                .candidates()
                .iter()
                .filter(move |candidate| !valid_a_candidates.contains(candidate))
                .map(move |&candidate| (cell_a, candidate));
            let removals_b = cell_b
                .candidates()
                .iter()
                .filter(move |candidate| !valid_b_candidates.contains(candidate))
                .map(move |&candidate| (cell_b, candidate));
            removals_a.chain(removals_b)
        })
        .merge_to_remove_candidates()
}

fn get_almost_locked_sets(
    board: &Board<Cell>,
    cell_a: &UnsolvedCell,
    cell_b: &UnsolvedCell,
) -> Vec<BTreeSet<SudokuNumber>> {
    let visible: Vec<_> = board
        .cells()
        .unsolved_cells()
        .filter(|&cell| {
            cell != cell_a
                && cell != cell_b
                && cell.is_in_same_unit(cell_a)
                && cell.is_in_same_unit(cell_b)
        })
        .collect();
    let almost_locked_sets_1 = visible
        .iter()
        .map(|cell| cell.candidates().clone())
        .filter(|candidates| candidates.len() == 2);
    let almost_locked_sets_2 = visible
        .iter()
        .zip_every_pair()
        .filter(|(als_a, als_b)| als_a.is_in_same_unit(als_b))
        .map(|(als_a, als_b)| als_a.candidates() | als_b.candidates())
        .filter(|candidates| candidates.len() == 3);
    let almost_locked_sets_3 = visible
        .iter()
        .zip_every_triple()
        .filter(|(als_a, als_b, als_c)| {
            als_a.is_in_same_unit(als_b)
                && als_a.is_in_same_unit(als_c)
                && als_b.is_in_same_unit(als_c)
        })
        .map(|(als_a, als_b, als_c)| {
            let mut candidates = als_a.candidates().clone();
            candidates.extend(als_b.candidates());
            candidates.extend(als_c.candidates());
            candidates
        })
        .filter(|candidates| candidates.len() == 4);
    let almost_locked_sets_4 = visible
        .iter()
        .zip_every_quad()
        .filter(|(als_a, als_b, als_c, als_d)| {
            als_a.is_in_same_unit(als_b)
                && als_a.is_in_same_unit(als_c)
                && als_a.is_in_same_unit(als_d)
                && als_b.is_in_same_unit(als_c)
                && als_b.is_in_same_unit(als_d)
                && als_c.is_in_same_unit(als_d)
        })
        .map(|(als_a, als_b, als_c, als_d)| {
            let mut candidates = als_a.candidates().clone();
            candidates.extend(als_b.candidates());
            candidates.extend(als_c.candidates());
            candidates.extend(als_d.candidates());
            candidates
        })
        .filter(|candidates| candidates.len() == 5);
    almost_locked_sets_1
        .chain(almost_locked_sets_2)
        .chain(almost_locked_sets_3)
        .chain(almost_locked_sets_4)
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{logic::assertions, remove_candidates};

    #[test]
    fn test_1() {
        let board = "\
            {568}971{258}3{268}4{258}\
            {568}3{128}{258}4{259}7{1269}{2589}\
            {458}{124}{128}67{259}{289}{129}3\
            273914586\
            986{235}{25}71{23}4\
            154{23}68{239}{239}7\
            7{24}{258}{2458}91{36}{36}{28}\
            {48}{124}9736{248}5{12}\
            36{1258}{2458}{258}{25}{2489}7{1289}\
        ";
        let expected = [remove_candidates!(6, 2, 8), remove_candidates!(7, 6, 8)];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_2() {
        let board = "\
            {56}971{258}3{268}4{25}\
            {568}3{128}{258}4{259}7{1269}{2589}\
            {458}{124}{128}67{259}{289}{129}3\
            273914586\
            986{235}{25}71{23}4\
            154{23}68{239}{239}7\
            7{24}{25}{2458}91{36}{36}{28}\
            {48}{124}9736{248}5{12}\
            36{1258}{2458}{258}{25}{2489}7{1289}\
        ";
        let expected = [remove_candidates!(7, 6, 8)];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_3() {
        let board = "\
            {17}{1458}{457}{14}23{69}{69}{78}\
            62{37}598{137}{137}4\
            {149}{138}{349}7{14}652{38}\
            598{13}62{137}4{137}\
            2{34}1{349}758{39}6\
            {34}768{14}{19}{139}52\
            {1349}62{19}{58}7{134}{138}{135}\
            8{15}{57}6342{17}9\
            {13479}{134}{3479}2{58}{19}{13467}{3678}{1357}\
        ";
        let expected = [remove_candidates!(2, 1, 1), remove_candidates!(2, 2, 3)];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_4() {
        let board = "\
            {17}{1458}{457}{14}23{69}{69}{78}\
            62{37}598{137}{137}4\
            {149}{38}{49}7{14}652{38}\
            598{13}62{137}4{137}\
            2{34}1{349}758{39}6\
            {34}768{14}{19}{139}52\
            {1349}62{19}{58}7{134}{138}{135}\
            8{15}{57}6342{17}9\
            {13479}{134}{3479}2{58}{19}{13467}{3678}{157}\
        ";
        let expected = [remove_candidates!(1, 2, 7), remove_candidates!(6, 7, 1)];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_5() {
        let board = "\
            {124}{159}{2589}{689}{568}{49}37{15}\
            7{1459}6{89}{458}3{128}{458}{1245}\
            3{45}{58}172{68}{4568}9\
            9672385{14}{14}\
            531496728\
            824517936\
            6{1479}{29}3{248}5{128}{18}{127}\
            {12}{157}{25}{6789}{268}{19}4{1689}3\
            {124}83{679}{26}{149}{126}{1569}{157}\
        ";
        let expected = [remove_candidates!(0, 5, 9), remove_candidates!(1, 8, 4)];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_6() {
        let board = "\
            {18}24{19}536{1789}{78}\
            {168}{13678}5{1789}4{89}{139}{189}2\
            9{137}{178}6{78}2{134}{14}5\
            7{4689}{2689}5{289}1{249}3{468}\
            {148}{1489}{1289}3675{2489}{48}\
            35{2689}{89}{289}4{279}{26789}1\
            2{4678}{678}{478}35{147}{1467}9\
            5{46789}{79}{4789}1{689}{247}{2467}3\
            {146}{1469}32{79}{69}85{467}\
        ";
        let expected = [
            remove_candidates!(1, 0, 8),
            remove_candidates!(1, 1, 8),
            remove_candidates!(1, 5, 8),
            remove_candidates!(7, 5, 9),
            remove_candidates!(8, 5, 9),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_7() {
        let board = "\
            45{37}9{136}{367}{278}{168}{1268}\
            {127}{123}68{134}{3457}9{15}{14}\
            {179}8{79}{146}2{4567}{457}3{146}\
            {279}{239}{3789}5{3469}{23469}{2348}{1689}{12468}\
            64{389}{123}{139}{239}{238}75\
            5{239}1{2346}78{234}{69}{246}\
            375{26}8{26}149\
            {189}{19}2{34}{3459}{349}6{58}7\
            {89}647{59}1{58}23\
        ";
        let expected = [
            remove_candidates!(1, 0, 1),
            remove_candidates!(1, 1, 1),
            remove_candidates!(2, 8, 1),
            remove_candidates!(3, 8, 4),
            remove_candidates!(5, 8, 4),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_8() {
        let board = "\
            45{37}9{136}{367}{278}{168}{1268}\
            {27}{123}68{134}{3457}9{15}{14}\
            {179}8{79}{146}2{4567}{457}3{146}\
            {279}{239}{3789}5{3469}{23469}{2348}{1689}{12468}\
            64{389}{123}{139}{239}{238}75\
            5{239}1{2346}78{234}{69}{246}\
            375{26}8{26}149\
            {189}{19}2{34}{3459}{349}6{58}7\
            {89}647{59}1{58}23\
        ";
        let expected = [
            remove_candidates!(1, 1, 1),
            remove_candidates!(2, 8, 1),
            remove_candidates!(3, 8, 4),
            remove_candidates!(5, 8, 4),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_9() {
        let board = "\
            9{45}{47}{378}{28}{237}1{235}6\
            {67}{16}259{1367}{478}{38}{478}\
            3{156}8{167}4{1267}{257}9{257}\
            {246}8{1346}{369}{16}{3569}{235}7{125}\
            57{13}2{18}4{38}69\
            {26}9{136}{368}7{356}{2358}4{1258}\
            8{24}9{147}5{127}6{12}3\
            {467}{246}{467}{14}389{125}{245}\
            135{469}{26}{269}{2478}{28}{2478}\
        ";
        let expected = [
            remove_candidates!(1, 6, 7),
            remove_candidates!(1, 8, 7),
            remove_candidates!(2, 3, 7),
            remove_candidates!(2, 5, 7),
            remove_candidates!(3, 0, 6),
            remove_candidates!(3, 2, 4),
            remove_candidates!(5, 0, 6),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_10() {
        let board = "\
            9{45}{47}{378}{28}{237}1{235}6\
            {67}{16}259{1367}{478}{38}{478}\
            3{156}8{167}4{127}{257}9{257}\
            48{136}{369}{16}{3569}{235}7{125}\
            57{13}2{18}4{38}69\
            29{136}{368}7{356}{358}4{158}\
            8{24}9{147}5{127}6{12}3\
            {67}{246}{47}{14}389{125}{245}\
            135{469}{26}{269}{2478}{28}{2478}\
        ";
        let expected = [
            remove_candidates!(1, 6, 7),
            remove_candidates!(1, 8, 7),
            remove_candidates!(2, 3, 7),
            remove_candidates!(2, 5, 7),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_11() {
        let board = "\
            9{45}{47}{378}{28}{237}1{235}6\
            {67}{16}259{1367}{478}{38}{478}\
            3{156}8{16}4{1267}{257}9{257}\
            48{136}{369}{16}{3569}{235}7{125}\
            57{13}2{18}4{38}69\
            29{136}{368}7{356}{358}4{158}\
            8{24}9{147}5{127}6{12}3\
            {67}{246}{47}{14}389{125}{245}\
            135{469}{26}{269}{2478}{28}{2478}\
        ";
        let expected = [
            remove_candidates!(1, 6, 7),
            remove_candidates!(1, 8, 7),
            remove_candidates!(2, 5, 7),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_12() {
        let board = "\
            {135}72{159}8{35}64{59}\
            {1345}967{124}{2345}8{235}{25}\
            {345}{35}86{249}{2345}{23579}{23579}1\
            {579}{25}{579}3{12679}{26}{129}84\
            681{49}{2479}{247}{2359}{2359}{2579}\
            {379}{23}4{19}58{129}6{279}\
            21{57}8394{57}6\
            84{579}2{67}{567}{579}13\
            {579}63{45}{47}1{2579}{2579}8\
        ";
        let expected = [
            remove_candidates!(1, 4, 4),
            remove_candidates!(2, 4, 4),
            remove_candidates!(4, 5, 4),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_13() {
        let board = "\
            {135}72{159}8{35}64{59}\
            {1345}967{12}{2345}8{235}{25}\
            {345}{35}86{29}{2345}{23579}{23579}1\
            {579}{25}{579}3{12679}{26}{129}84\
            681{49}{2479}{27}{2359}{2359}{2579}\
            {379}{23}4{19}58{129}6{279}\
            21{57}8394{57}6\
            84{579}2{67}{567}{579}13\
            {579}63{45}{47}1{2579}{2579}8\
        ";
        let expected = [remove_candidates!(4, 4, 2)];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }

    #[test]
    fn test_14() {
        let board = "\
            185{49}2637{49}\
            {234}6{234}{3579}{134}{1357}{2458}{28}{2589}\
            {234}97{345}{34}81{26}{2456}\
            {4678}1{48}{348}52{68}9{37}\
            {245789}{27}{2489}{348}6{34}{258}{13}{137}\
            {2568}3{28}179{2568}4{2568}\
            {2378}416{38}{37}95{238}\
            {23789}{27}{2389}{357}{1348}{13457}{2468}{12368}{123468}\
            {38}5629{134}7{138}{1348}\
        ";
        let expected = [
            remove_candidates!(4, 0, 4, 8),
            remove_candidates!(4, 2, 4, 8),
            remove_candidates!(7, 0, 2),
        ];
        assertions::assert_logical_solution(&expected, board, aligned_pair_exclusion);
    }
}
