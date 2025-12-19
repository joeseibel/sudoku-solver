use crate::{
    board::Board,
    board_modification::{BoardModification, SetValue},
    cell::{Cell, IteratorCellExt, Location},
};

// http://www.sudokuwiki.org/BUG
//
// BUG applies to boards with exactly one unsolved cell with three candidates and every other unsolved cell has two
// candidates. Removing one of the candidates from the cell with three candidates will result in a board in which all of
// its unsolved cells have two candidates, which would have multiple solutions. Since removing that candidate from that
// cell would lead to an invalid board, that candidate must be the solution to that cell.
//
// For the three candidates of the cell, two candidates will appear twice in the cell's row, twice in the cell's column,
// and twice in the cell's block, while one candidate will appear three times in the cell's row, three times in the
// cell's column, and three times in the cell's block. This check is only performed against the cell's row.
pub fn bug(board: &Board<Cell>) -> Option<BoardModification> {
    let mut cells = board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() != 2);
    if let Some(cell) = cells.next()
        && let None = cells.next()
        && cell.candidates().len() == 3
    {
        let row: Vec<_> = board.get_row(cell.row()).unsolved_cells().collect();
        let mut candidates = cell.candidates().iter().filter(|candidate| {
            row.iter()
                .filter(|row_cell| row_cell.candidates().contains(candidate))
                .count()
                == 3
        });
        let &candidate = candidates.next().unwrap();
        assert!(candidates.next().is_none());
        Some(SetValue::from_cell(cell, candidate))
    } else {
        None
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{board_modification::SetValue, logic::assertions};

    #[test]
    fn test_1() {
        let board = "\
            174832596\
            593461278\
            682957{34}{34}1\
            {28}675{12}{48}9{123}{34}\
            {28}197{48}36{24}5\
            435{12}968{12}7\
            3{24}16{28}{48}759\
            9{24}8{12}75{13}6{34}\
            7563{14}9{14}82\
        ";
        let expected = [SetValue::from_indices(3, 7, 2)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_2() {
        let board = "\
            821{39}{59}{35}746\
            736{28}{48}{24}915\
            549{67}{16}{17}382\
            {49}{16}{35}{46}782{59}{13}\
            {49}{167}{38}52{46}{68}{79}{13}\
            2{67}{58}139{68}{57}4\
            392{47}{14}{17}568\
            684{29}{59}{25}137\
            157{38}{68}{36}429\
        ";
        let expected = [SetValue::from_indices(4, 1, 6)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_3() {
        let board = "\
            289476531\
            751238496\
            436915728\
            97{58}{15}{68}2{16}43\
            36{28}{17}94{12}{78}5\
            14{258}{57}{68}3{26}{78}9\
            594821367\
            627359814\
            813647952\
        ";
        let expected = [SetValue::from_indices(5, 2, 8)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_4() {
        let board = "\
            142895763\
            {57}{89}62{47}3{49}1{58}\
            {57}{89}361{47}{49}{58}2\
            8671293{45}{45}\
            3514{67}{67}829\
            924538671\
            67{58}3{45}129{48}\
            23{58}9{456}{46}1{48}7\
            419782536\
        ";
        let expected = [SetValue::from_indices(7, 4, 4)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_5() {
        let board = "\
            4{38}{39}56{89}127\
            {67}{27}{267}413859\
            {89}5172{89}643\
            {56}{34}{36}297{45}18\
            1{24}863597{24}\
            {57}9{27}184{25}36\
            3{78}{79}{89}51{24}6{24}\
            214{89}763{89}5\
            {89}653427{89}1\
        ";
        let expected = [SetValue::from_indices(1, 2, 7)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_6() {
        let board = "\
            8915{24}7{34}6{23}\
            425631789\
            6739{24}8{45}{25}1\
            56{24}7{18}9{18}3{24}\
            31{49}{28}{568}{26}{89}7{45}\
            78{29}4{15}3{19}{25}6\
            1{45}7{38}{68}{46}29{35}\
            9{45}6{23}7{24}{35}18\
            238195647\
        ";
        let expected = [SetValue::from_indices(4, 4, 8)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_7() {
        let board = "\
            {48}{58}9{45}12376\
            {57}{67}389{56}412\
            {24}{26}137{46}598\
            346287951\
            1{25}{25}639{78}{48}{47}\
            {89}{89}7{45}{45}1623\
            {57}3892{45}16{47}\
            {29}{279}{24}163{78}{48}5\
            61{45}7{45}8239\
        ";
        let expected = [SetValue::from_indices(7, 1, 2)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }

    #[test]
    fn test_8() {
        let board = "\
            916{47}{347}852{34}\
            7451{36}2{36}98\
            3829{46}571{46}\
            134{67}{78}9{68}52\
            67{89}251{38}4{39}\
            25{89}{46}{48}317{69}\
            427816935\
            893527461\
            561394287\
        ";
        let expected = [SetValue::from_indices(0, 4, 4)];
        assertions::assert_logical_solution(&expected, board, |board| vec![bug(board).unwrap()]);
    }
}
