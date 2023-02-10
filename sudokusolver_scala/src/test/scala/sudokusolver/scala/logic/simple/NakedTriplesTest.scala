package sudokusolver.scala.logic.simple

import munit.FunSuite
import sudokusolver.scala.RemoveCandidates
import sudokusolver.scala.logic.assertLogicalSolution

class NakedTriplesTest extends FunSuite:
  test("test 1") {
    val board =
      """
        |{36}7{16}4{135}8{135}29
        |{369}{169}2{1579}{135}{5679}{1358}{3568}4
        |854{19}2{69}{13}{36}7
        |{569}{169}83742{59}{16}
        |{45679}2{15679}{589}{58}{59}{3589}{34589}{16}
        |{459}{49}32617{4589}{58}
        |{457}{48}{57}{578}93612
        |2{689}{5679}{1578}{158}{57}4{589}3
        |13{59}642{589}7{58}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(4, 0, 5, 9),
      RemoveCandidates(4, 2, 5, 9),
      RemoveCandidates(4, 6, 5, 8, 9),
      RemoveCandidates(4, 7, 5, 8, 9)
    )
    assertLogicalSolution(expected, board, nakedTriples)
  }

  test("test 2") {
    val board =
      """
        |294513{78}{78}6
        |6{57}{57}842319
        |3{18}{18}697254
        |{18}{1278}{123789}{23}56{14789}{24789}{238}
        |{15}4{1579}{23}8{19}{1579}6{23}
        |{158}{12568}{1235689}47{19}{1589}{289}{238}
        |73{28}164{89}{289}5
        |9{268}{268}735{48}{248}1
        |4{15}{15}928637
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(3, 1, 1, 8),
      RemoveCandidates(3, 2, 1, 8),
      RemoveCandidates(3, 6, 8),
      RemoveCandidates(3, 7, 2, 8),
      RemoveCandidates(4, 2, 1, 5),
      RemoveCandidates(5, 1, 1, 5, 8),
      RemoveCandidates(5, 2, 1, 5, 8),
      RemoveCandidates(5, 6, 8),
      RemoveCandidates(5, 7, 2, 8)
    )
    assertLogicalSolution(expected, board, nakedTriples)
  }