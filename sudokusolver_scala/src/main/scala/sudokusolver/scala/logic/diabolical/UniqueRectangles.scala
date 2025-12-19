package sudokusolver.scala.logic.diabolical

import sudokusolver.scala.*

/*
 * https://www.sudokuwiki.org/Unique_Rectangles
 *
 * The Unique Rectangles solution works by identifying the potential for an invalid pattern of candidates called the
 * Deadly Pattern and then removing candidates that if set as the value would lead to the Deadly Pattern. A Deadly
 * Pattern is defined as a group of four unsolved cells arranged to form a rectangle, each cell containing the same two
 * candidates and only those candidates, and the cells being located in two rows, two columns, and two blocks. If a
 * board contains the Deadly Pattern, then the board cannot have a single solution, but would have multiple solutions.
 * The advantage of recognizing this pattern comes when a board contains a pattern which is close to the Deadly Pattern
 * and the removal of certain candidates would lead to the Deadly Pattern. If a valid board contains a pattern which is
 * close to the Deadly Pattern, it is known that the board will never enter into the Deadly Pattern and candidates can
 * be removed if setting those candidates as values would lead to the Deadly Pattern. A rectangle can be further
 * described by identifying its floor cells and its roof cells. A rectangle's floor are the cells that only contain the
 * two common candidates. A rectangle's roof are the cells that contain the two common candidates as well as additional
 * candidates.
 *
 * Type 1
 *
 * If a rectangle has one roof cell, then this is a potential Deadly Pattern. If the additional candidates were to be
 * removed from the roof, then that would lead to a Deadly Pattern. The two common candidates can be removed from the
 * roof leaving only the additional candidates remaining.
 */
def uniqueRectanglesType1(board: Board[Cell]): Seq[RemoveCandidates] =
  createRectangles(board).flatMap { rectangle =>
    rectangle.roof match
      case Seq(roof) => rectangle.commonCandidates.map(roof -> _)
      case _ => Nil
  }.mergeToRemoveCandidates

/*
 * Type 2
 *
 * If a rectangle has two roof cells and there is only one additional candidate appearing in both roof cells, then this
 * is a potential Deadly Pattern. If the additional candidate were to be removed from the roof cells, then that would
 * lead to a Deadly Pattern, therefore the additional candidate must be the solution for one of the two roof cells. The
 * common candidate can be removed from any other cell that can see both of the roof cells.
 */
def uniqueRectanglesType2(board: Board[Cell]): Seq[RemoveCandidates] =
  createRectangles(board).flatMap { rectangle =>
    rectangle.roof match
      case Seq(roofA, roofB) if roofA.candidates.size == 3 && roofA.candidates == roofB.candidates =>
        val additionalCandidates = roofA.candidates &~ rectangle.commonCandidates
        assert(additionalCandidates.size == 1)
        val additionalCandidate = additionalCandidates.head
        for
          cell <- board.cells.collect { case cell: UnsolvedCell => cell }
          if cell.candidates.contains(additionalCandidate) &&
            cell != roofA &&
            cell != roofB &&
            cell.isInSameUnit(roofA) &&
            cell.isInSameUnit(roofB)
        yield cell -> additionalCandidate
      case _ => Nil
  }.mergeToRemoveCandidates

/*
 * Type 3
 *
 * If a rectangle has two roof cells, each roof cell has one additional candidate, and the additional candidates are
 * different, then this is a potential Deadly Pattern. One or both of these additional candidates must be the solution,
 * so the roof cells can be treated as a single cell with the two additional candidates. If there is another cell that
 * can see both roof cells and has the additional candidates as its candidates, then the roof cells and the other cell
 * effectively form a Naked Pair. The additional candidates can be removed from any other cell in the unit.
 */
def uniqueRectanglesType3(board: Board[Cell]): Seq[RemoveCandidates] =
  createRectangles(board).flatMap { rectangle =>
    rectangle.roof match
      case Seq(roofA, roofB)
        if roofA.candidates.size == 3 && roofB.candidates.size == 3 && roofA.candidates != roofB.candidates =>
        val additionalCandidates = (roofA.candidates | roofB.candidates) &~ rectangle.commonCandidates

        def getRemovals(getUnitIndex: Cell => Int, getUnit: Int => Seq[Cell]) =
          val indexA = getUnitIndex(roofA)
          val indexB = getUnitIndex(roofB)
          if indexA == indexB then
            val unit = getUnit(indexA).collect { case cell: UnsolvedCell => cell }
            unit.find(_.candidates == additionalCandidates)
              .map { pairCell =>
                unit.filter(cell => cell != pairCell && cell != roofA && cell != roofB)
                  .flatMap(cell => (cell.candidates & additionalCandidates).map(cell -> _))
              }
              .getOrElse(Nil)
          else
            Nil

        getRemovals(_.row, board.getRow) ++
          getRemovals(_.column, board.getColumn) ++
          getRemovals(_.block, board.getBlock)
      case _ => Nil
  }.mergeToRemoveCandidates

/*
 * Type 3/3b with Triple Pseudo-Cells
 *
 * If a rectangle has two roof cells, then this is a potential Deadly Pattern. If the roof cells can see two other cells
 * and the union of candidates among the roof cells' additional candidates and the other cells' candidates is three
 * candidates, then the roof cells and the other two cells effectively form a Naked Triple. The three candidates in the
 * union can be removed from any other cell in the unit.
 */
def uniqueRectanglesType3BWithTriplePseudoCells(board: Board[Cell]): Seq[RemoveCandidates] =
  createRectangles(board).flatMap { rectangle =>
    rectangle.roof match
      case Seq(roofA, roofB) =>
        val additionalCandidates = (roofA.candidates | roofB.candidates) &~ rectangle.commonCandidates

        def getRemovals(getUnitIndex: Cell => Int, getUnit: Int => Seq[Cell]) =
          val indexA = getUnitIndex(roofA)
          val indexB = getUnitIndex(roofB)
          if indexA == indexB then
            val unit = getUnit(indexA).collect { case cell: UnsolvedCell if cell != roofA && cell != roofB => cell }
            for
              (tripleA, tripleB) <- unit.toIndexedSeq.zipEveryPair
              tripleCandidates = additionalCandidates | tripleA.candidates | tripleB.candidates
              if tripleCandidates.size == 3
              cell <- unit
              if cell != tripleA && cell != tripleB
              candidate <- cell.candidates & tripleCandidates
            yield cell -> candidate
          else
            Nil

        getRemovals(_.row, board.getRow) ++
          getRemovals(_.column, board.getColumn) ++
          getRemovals(_.block, board.getBlock)
      case _ => Nil
  }.mergeToRemoveCandidates

/*
 * Type 4
 *
 * If a rectangle has two roof cells, then this is a potential Deadly Pattern. For a unit common to the roof cells, if
 * one of the common candidates are only found in the roof cells of that unit, then setting the other candidate as the
 * solution to one of the roof cells would lead to the Deadly Pattern. The other common candidate can be removed from
 * the roof cells.
 */
def uniqueRectanglesType4(board: Board[Cell]): Seq[RemoveCandidates] =
  createRectangles(board).flatMap { rectangle =>
    val roof = rectangle.roof
    roof match
      case Seq(roofA, roofB) =>
        val Seq(commonCandidateA, commonCandidateB) = rectangle.commonCandidates.toSeq

        def getRemovals(getUnitIndex: Cell => Int, getUnit: Int => Seq[Cell]) =
          val indexA = getUnitIndex(roofA)
          val indexB = getUnitIndex(roofB)
          if indexA == indexB then
            val unit = getUnit(indexA).collect { case cell: UnsolvedCell => cell }

            def searchUnit(search: SudokuNumber, removal: SudokuNumber) =
              if unit.count(_.candidates.contains(search)) == 2 then roof.map(_ -> removal) else Nil

            searchUnit(commonCandidateA, commonCandidateB) ++ searchUnit(commonCandidateB, commonCandidateA)
          else
            Nil

        getRemovals(_.row, board.getRow) ++
          getRemovals(_.column, board.getColumn) ++
          getRemovals(_.block, board.getBlock)
      case _ => Nil
  }.mergeToRemoveCandidates

/*
 * Type 5
 *
 * If a rectangle has two floor cells in diagonally opposite corners of the rectangle and one of the common candidates
 * only appears in the rectangle for the rows and columns that the rectangle exists in, thus forming strong links for
 * the candidate along the four edges of the rectangle, then this is a potential Deadly Pattern. If the non-strong
 * link candidate were to be set as the solution to one of the floor cells, then the strong link candidate would have to
 * be the solution for the roof cells and the non-strong link candidate would need to be set as the solution to the
 * other floor cell, leading to the Deadly Pattern. The non-strong link candidate cannot be the solution to either floor
 * cell. Since each floor cell only contains two candidates, this means that the strong link candidate must be the
 * solution for the floor cells.
 */
def uniqueRectanglesType5(board: Board[Cell]): Seq[SetValue] =
  createRectangles(board).flatMap { rectangle =>
    val floor = rectangle.floor
    floor match
      case Seq(floorA, floorB) if floorA.row != floorB.row && floorA.column != floorB.column =>
        val strongLinkCandidate = rectangle.commonCandidates.find { candidate =>

          def hasStrongLink(unit: Seq[Cell]) =
            unit.collect { case cell: UnsolvedCell => cell }.count(_.candidates.contains(candidate)) == 2

          floor.forall(cell => hasStrongLink(board.getRow(cell.row)) && hasStrongLink(board.getColumn(cell.column)))
        }
        strongLinkCandidate match
          case Some(strongLinkCandidate) => floor.map(SetValue(_, strongLinkCandidate))
          case None => Nil
      case _ => Nil
  }