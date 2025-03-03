use crate::{
    board::Board,
    board_modification::BoardModification,
    cell::{Cell, IteratorCellExt},
};

// http://www.sudokuwiki.org/Getting_Started
//
// If an unsolved cell has exactly one candidate, then the candidate must be placed in that cell.
pub fn naked_singles(board: &Board<Cell>) -> Vec<BoardModification> {
    board
        .cells()
        .unsolved_cells()
        .filter(|cell| cell.candidates().len() == 1)
        .map(|cell| {
            BoardModification::new_set_value_with_cell(
                cell,
                *cell.candidates().iter().next().unwrap(),
            )
        })
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{cell, logic::brute_force};

    #[test]
    fn test() {
        let board = "\
            {2367}{379}{29}1{3468}5{2389}{9}{289}\
            14{259}{389}{38}{38}67{289}\
            {3567}8{59}{3679}{36}24{59}{19}\
            {2458}63{58}7{48}{89}1{489}\
            9{57}{2458}{568}{124568}{1468}{78}{46}3\
            {478}1{48}{368}9{3468}52{4678}\
            {345}{359}72{1356}{136}{19}8{1469}\
            {48}26{78}{18}{178}{179}35\
            {358}{35}{158}4{13568}9{127}{6}{1267}\
        ";
        let expected = vec![
            BoardModification::new_set_value_with_indices(0, 7, 9),
            BoardModification::new_set_value_with_indices(8, 7, 6),
        ];

        // TODO: Factor out to assert_logical_solution
        let board = cell::parse_cells_with_candidates(board);
        let optional_board = board.map_cells(|cell| match cell {
            Cell::SolvedCell(solved_cell) => Some(solved_cell.value()),
            Cell::UnsolvedCell(_) => None,
        });
        let brute_force_solution = brute_force::brute_force(&optional_board).unwrap();
        let mut actual = naked_singles(&board);
        // Why am I using sort_unstable_by_key instead of sort_unstable and implementing Ord for BoardModification?
        // In short, implementing Ord for BoardModification would lead to PartialOrd and PartialEq disagreeing with each
        // other. I want to sort BoardModifications by the row and column indices only while ignoring other fields.
        // However, I want equality to check all fields, as that is useful in unit tests. Having a different standard of
        // equality between PartialOrd and PartialEq breaks the contract of PartialOrd.
        actual.sort_unstable_by_key(|modification| (modification.row(), modification.column()));
        for modification in &actual {
            let row = modification.row();
            let column = modification.column();
            let solution = brute_force_solution[(row, column)];
            match modification {
                BoardModification::RemoveCandidates(remove_candidates) => assert!(
                    !remove_candidates.candidates().contains(&solution),
                    "Cannot remove candidate {solution} from [{row}, {column}]"
                ),
                BoardModification::SetValue(set_value) => assert_eq!(
                    solution,
                    set_value.value(),
                    "Cannot set value {} to [{row}, {column}]. Solution is {solution}",
                    set_value.value()
                ),
            }
        }
        assert_eq!(expected, actual);
    }
}
