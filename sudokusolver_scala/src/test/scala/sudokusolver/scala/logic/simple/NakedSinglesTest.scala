package sudokusolver.scala.logic.simple

import munit.FunSuite
import sudokusolver.scala.logic.BruteForceSolution.SingleSolution
import sudokusolver.scala.logic.bruteForce
import sudokusolver.scala.{SetValue, SolvedCell, UnsolvedCell, parseCellsWithCandidates}

class NakedSinglesTest extends FunSuite:
  test("test") {
    val board =
      """
        |{2367}{379}{29}1{3468}5{2389}{9}{289}
        |14{259}{389}{38}{38}67{289}
        |{3567}8{59}{3679}{36}24{59}{19}
        |{2458}63{58}7{48}{89}1{489}
        |9{57}{2458}{568}{124568}{1468}{78}{46}3
        |{478}1{48}{368}9{3468}52{4678}
        |{345}{359}72{1356}{136}{19}8{1469}
        |{48}26{78}{18}{178}{179}35
        |{358}{35}{158}4{13568}9{127}{6}{1267}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      SetValue(0, 7, 9),
      SetValue(8, 7, 6)
    )
    val cellBoard = parseCellsWithCandidates(board)
    val numberBoard = cellBoard.mapCells {
      case SolvedCell(_, _, value) => Some(value)
      case _: UnsolvedCell => None
    }
    val bruteForceSolution = bruteForce(numberBoard).asInstanceOf[SingleSolution].solution
    val actual = nakedSingles(cellBoard).sorted
    actual.foreach { modification =>
      val row = modification.row
      val column = modification.column
      val solution = bruteForceSolution(row, column)
      assertEquals(
        modification.value,
        solution,
        s"Cannot set value ${modification.value} to [$row, $column]. Solution is $solution"
      )
    }
    assertEquals(actual, expected)
  }